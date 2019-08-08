package ImplementRemoteInterface;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import Server.EventDetails;
import ServerInterface.McInterface;

public class MontrealClass extends UnicastRemoteObject implements McInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<String, HashMap<String, EventDetails>> montr_db = new HashMap<String, HashMap<String, EventDetails>>();

	HashMap<String, HashMap<String, ArrayList<String>>> user_db = new HashMap<String, HashMap<String, ArrayList<String>>>();
	Helpers hlp = new Helpers();

	public MontrealClass() throws Exception {
		super();

		montr_db.put("conferences", new HashMap<String, EventDetails>());
		montr_db.put("trade shows", new HashMap<String, EventDetails>());
		montr_db.put("seminars", new HashMap<String, EventDetails>());

		Runnable udp_task = new Runnable() {
			public void run() {
				udp_packet_recv();
			}
		};

		Thread thread = new Thread(udp_task);
		thread.start();
	}

	public String udp_packet_send(String destServer, String actionType,
			String eventType, String customerId, String eventId) {
		DatagramSocket aSocket = null;
		String retVal = "";

		System.out
				.println("Montreal UDP Server: Sending UDP packet with eventType: "
						+ eventType
						+ " for: "
						+ actionType
						+ " to: "
						+ destServer);
		byte[] message = null;

		try {
			aSocket = new DatagramSocket(); // reference of the original socket
			if (actionType.equalsIgnoreCase("book")
					|| actionType.equalsIgnoreCase("getbookings")
					|| actionType.equalsIgnoreCase("cancel"))

				message = (actionType + "-" + customerId + "-" + eventId + "-" + eventType)
						.getBytes();
			else if (actionType.equalsIgnoreCase("listEventAvailability"))
				message = (actionType + "-" + eventType + "-" + customerId)
						.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");
			int serverPort = 0;

			if (destServer.equals("TOR")) {
				serverPort = 6702;
			} else if (destServer.equals("OTW")) {
				serverPort = 6701;
			} else {
				System.out.println("Server not found");
				return "invalid request";
			}

			DatagramPacket request = new DatagramPacket(message,
					message.length, aHost, serverPort);// request packet
			aSocket.send(request);// request sent out

			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			// Client waits until the reply is received-
			aSocket.receive(reply);// reply received and will populate reply
									// packet now.

			retVal = hlp.data(buffer).toString();

			String logMessages = "Montreal UDP Server: Reply received from the server is: "
					+ retVal;
			System.out.println(logMessages);// print reply
			Helpers.ServerLog("MTL", logMessages);
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

		return retVal;
	}

	public void udp_packet_recv() {
		DatagramSocket aSocket = null;
		int portNo = 6700;
		try {
			aSocket = new DatagramSocket(portNo);
			byte[] buffer = new byte[1000];// to stored the received data from
											// the client.
			System.out
					.println("Montreal UDP Server Started on port: " + portNo);

			while (true) {// non-terminating loop as the server is always in
							// listening mode.

				DatagramPacket request = new DatagramPacket(buffer,
						buffer.length);
				// Server waits for the request to come
				aSocket.receive(request);// request received

				// Parse the request
				String receivedData = hlp.data(buffer).toString();
				String logMessages = "Montreal UDP Server: Request received from client: "
						+ receivedData;
				System.out.println(logMessages);
				Helpers.ServerLog("MTL", logMessages);

				String[] receivedParams = receivedData.split("-");
				DatagramPacket reply;
				String response = "";
				boolean check = false;
				if (receivedParams[0].equals("listEventAvailability")) {
					response = hlp.get_events(receivedParams[2],
							receivedParams[1], montr_db, "MTL");
				} else if (receivedParams[0].equals("book")) {
					check = bookEvent(receivedParams[1], receivedParams[2],
							receivedParams[3]);
					if (check)
						response = "true";
					else
						response = "false";
				} else if (receivedParams[0].equals("getbookings")) {
					response = getBookingSchedule(receivedParams[1]);
				} else if (receivedParams[0].equals("cancel")) {
					check = cancelEvent(receivedParams[1], receivedParams[2],
							receivedParams[3]);
					if (check)
						response = "true";
					else
						response = "false";
				}

				byte[] result = response.getBytes();

				reply = new DatagramPacket(result, result.length,
						request.getAddress(), request.getPort());// reply

				aSocket.send(reply);// reply sent
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public boolean addEvent(String eventID, String eventType,
			int bookingCapacity, String managerID) {

		return hlp.add_event_in_db(managerID, eventID, eventType,
				bookingCapacity, montr_db, "MTL");
	}

	public boolean removeEvent(String eventID, String eventType,
			String managerID) {

		return hlp.remove_event_from_db(managerID, eventID, eventType,
				montr_db, user_db, "MTL");
	}

	public String listEventAvailability(String eventType, String managerID) {
		String disp_events = "";
		String message = "";
		String serverName = managerID.substring(0, 3);

		disp_events = hlp.get_events(managerID, eventType, montr_db, "MTL");

		// Send UDP packet for listEventAvailability
		disp_events += udp_packet_send("TOR", "listEventAvailability",
				eventType, managerID, "");
		disp_events += udp_packet_send("OTW", "listEventAvailability",
				eventType, managerID, "");

		// We have found events
		if (!disp_events.isEmpty()) {
			disp_events = eventType + " - " + disp_events;
			disp_events = disp_events.substring(0, disp_events.length() - 2);
			
			message = " Manager ID : " + managerID + " , "
					+ "Event type : " + eventType + " , "
					+ "Action Performed : listEventAvailability"
					+ " , " + "Message : Event list - " + disp_events + "\n";
			Helpers.ServerLog(serverName, message);
			Helpers.usersLog(managerID, message);
			
		} else {
			disp_events = serverName + ": "
					+ "No events found with event type: " + eventType;

			message = " Manager ID : " + managerID + " , " + "Event type : "
					+ eventType + " , "
					+ "Action Performed : listEventAvailability" + " , "
					+ "Message : No events found\n";
			Helpers.ServerLog(serverName, message);
			Helpers.usersLog(managerID, message);
		}

		return disp_events;
	}

	public boolean bookEvent(String customerID, String eventID, String eventType) {
		boolean checkEvent = false;
		if (hlp.checkEventType(eventType, montr_db)) {
			if (user_db.containsKey(customerID)) {
				checkEvent = hlp.checkAndAddEvent(eventID, customerID,
						eventType, montr_db, user_db);
			} else {
				user_db.put(customerID,
						new HashMap<String, ArrayList<String>>());
				checkEvent = hlp.checkAndAddEvent(eventID, customerID,
						eventType, montr_db, user_db);
			}
			if (eventID.substring(0, 3).equals("TOR")
					|| eventID.substring(0, 3).equals("OTW")) {
				String[] displayEvents = getBookingSchedule(customerID).split(
						",");
				if (hlp.checkOutsideEvents(displayEvents, customerID,eventID)) {
					String bookResult = udp_packet_send(
							eventID.substring(0, 3), "book", eventType,
							customerID, eventID);
					if (bookResult.equalsIgnoreCase("true"))
						checkEvent = true;
					else
						checkEvent = false;
				}
				else {
					Helpers.usersLog(customerID, "Number of Booking of Outside Events Reached");
					Helpers.ServerLog("MTL", "Number of Booking of Outside Events Reached");
					checkEvent=false;
				}
			}
			String message = "";
			if (checkEvent)
				message = "Customer ID : "
						+ customerID
						+ " , "
						+ " Event type : "
						+ eventType
						+ " , "
						+ " EventId : "
						+ eventID
						+ ","
						+ " Action Performed : Book Event "
						+ " Message : Event has been Booked and updated accordingly\n";
			else
				message = "Customer ID : " + customerID + " , "
						+ " Event type : " + eventType + " , " + " EventId : "
						+ eventID + "," + " Action Performed : Book Event "
						+ " Message : Event has not been Booked\n";

			Helpers.usersLog(customerID, message);
			Helpers.ServerLog("MTL", message);
		}
		return checkEvent;
	}

	public String getBookingSchedule(String customerID) {
		String finalEvents = "", message = "";
		if (user_db.containsKey(customerID)) {
			HashMap<String, ArrayList<String>> eventType = user_db
					.get(customerID);
			for (String keyevent : eventType.keySet()) {
				for (String event : eventType.get(keyevent)) {
					finalEvents = finalEvents + event + ",";
				}
			}

			if (customerID.substring(0, 3).equalsIgnoreCase("MTL")) {
				String tor = udp_packet_send("TOR", "getbookings", "",
						customerID, "TORA999999");
				String otw = udp_packet_send("OTW", "getbookings", "",
						customerID, "OTWA999999");
				if (!(tor.substring(0, 2).equalsIgnoreCase("No"))) {
					finalEvents += tor;
				}
				if (!(otw.substring(0, 2).equalsIgnoreCase("No"))) {
					finalEvents += otw;
				}
			}
			if (finalEvents.equals("")) {
				finalEvents = "No Events for this Customer";
			}
		} else {
			finalEvents = "No customer with CustomerId " + customerID;
		}
		message = "Customer ID : " + customerID + " , " + finalEvents;
		Helpers.usersLog(customerID, message);
		Helpers.ServerLog("MTL", message);
		return finalEvents;
	}

	public boolean cancelEvent(String customerID, String eventID,
			String eventType) {
		String message = " Customer ID :" + customerID + " , " + " EventId : "
				+ eventID + " , " + " Event Type " + eventType + " , "
				+ " Action Performed : Cancel Event" + " , "
				+ " Message : Event has been cancelled\n";
		for (String user : user_db.keySet()) {
			HashMap<String, ArrayList<String>> eventColl = user_db
					.get(user);
			for (String keyevent : eventColl.keySet()) {
				if (keyevent.equalsIgnoreCase(eventType)) {
					for (String event : eventColl.get(keyevent)) {
						if (event.equalsIgnoreCase(eventID)) {
							user_db.get(user).get(eventType)
									.remove(eventID);
							Helpers.usersLog(user, message);
							Helpers.ServerLog("MTL", message);
							return true;
						}
					}
				}
			}

			if (eventID.substring(0, 3).equals("TOR")
					|| eventID.substring(0, 3).equals("OTW")) {
				String bookResult = udp_packet_send(eventID.substring(0, 3),
						"cancel", eventType, customerID, eventID);
				if (bookResult.equalsIgnoreCase("true")) {
					Helpers.usersLog(customerID, message);
					Helpers.ServerLog("MTL", message);
					return true;
				} else {
					message = "Customer ID :" + customerID + " , "
							+ " EventId : " + eventID + " , " + " Event Type "
							+ eventType + " , "
							+ " Action Performed : Cancel Event " + " , "
							+ " Message : Event can't be cancelled\n";
					Helpers.usersLog(customerID, message);
					Helpers.ServerLog("MTL", message);
					return false;
				}
			}
		}
		message = "Customer ID :" + customerID + " , " + " EventId : "
				+ eventID + " , " + " Event Type " + eventType + " , "
				+ " Action Performed : Cancel Event  " + " , "
				+ " Message : Event can't be cancelled\n";
		Helpers.usersLog(customerID, message);
		Helpers.ServerLog("MTL", message);
		return false;
	}
}
