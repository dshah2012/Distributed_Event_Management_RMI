package ImplementRemoteInterface;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Server.EventDetails;

public class Helpers {

	public boolean checkEventType(String eventType,
			HashMap<String, HashMap<String, EventDetails>> servdb) {

		if (servdb.containsKey(eventType))
			return true;
		else {
			System.out.println("Please Enter the Correct Event Type.");
			return false;
		}
	}

	public boolean add_event_in_db(String managerID, String eventID,
			String eventType, int bookingCapacity,
			HashMap<String, HashMap<String, EventDetails>> servdb,
			String serverName) {

		String message = "";

		if (serverName == "MTL") {
			if (!eventID.substring(0, 3).equals("MTL")) {
				System.out.println(serverName + ": "
						+ "You are not authorized to create this event: "
						+ eventID);

				message = " Manager ID : "
						+ managerID
						+ " , "
						+ "Event type : "
						+ eventType
						+ " , "
						+ "EventId : "
						+ eventID
						+ " , "
						+ "Action Performed : addEvent"
						+ " , "
						+ "Message : You are not authorized to create this event\n";

				ServerLog(serverName, message);
				usersLog(managerID, message);

				return false;
			}
		} else if (serverName == "TOR") {
			if (!eventID.substring(0, 3).equals("TOR")) {
				System.out.println(serverName + ": "
						+ "You are not authorized to create this event: "
						+ eventID);

				message = " Manager ID : "
						+ managerID
						+ " , "
						+ "Event type : "
						+ eventType
						+ " , "
						+ "EventId : "
						+ eventID
						+ " , "
						+ "Action Performed : addEvent"
						+ " , "
						+ "Message : You are not authorized to create this event\n";

				ServerLog(serverName, message);
				usersLog(managerID, message);

				return false;
			}
		} else if (serverName == "OTW") {
			if (!eventID.substring(0, 3).equals("OTW")) {
				System.out.println(serverName + ": "
						+ "You are not authorized to create this event: "
						+ eventID);

				message = " Manager ID : "
						+ managerID
						+ " , "
						+ "Event type : "
						+ eventType
						+ " , "
						+ "EventId : "
						+ eventID
						+ " , "
						+ "Action Performed : addEvent"
						+ " , "
						+ "Message : You are not authorized to create this event\n";

				ServerLog(serverName, message);
				usersLog(managerID, message);

				return false;
			}
		}

		if (checkEventType(eventType, servdb)) {
			HashMap<String, EventDetails> event = servdb.get(eventType);

			if (event.containsKey(eventID)) {
				System.out.println(serverName + ": "
						+ "Event already exists for event id: " + eventID);

				EventDetails new_eveDet = event.get(eventID);
				int current_book_cap = new_eveDet.getBookingCapacity();

				System.out.println(serverName + ": "
						+ "Booking capacity updated from: " + current_book_cap
						+ " to: " + bookingCapacity);
				new_eveDet.setBookingCapacity(bookingCapacity);

				message = " Manager ID : " + managerID + " , "
						+ "Event type : " + eventType + " , " + "EventId : "
						+ eventID + " , " + "Action Performed : addEvent"
						+ " , " + "Message : Event already exists" + " , "
						+ "Updating booking capacity from: " + current_book_cap
						+ " to: " + bookingCapacity + "\n";
				ServerLog(serverName, message);
				usersLog(managerID, message);

				event.put(eventID, new_eveDet);
				servdb.put(eventType, event);

				return true;
			} else {
				HashMap<String, EventDetails> evd_hash = servdb.get(eventType);
				EventDetails ed = new EventDetails();
				ed.setBookingCapacity(bookingCapacity);

				evd_hash.put(eventID, ed);
				servdb.put(eventType, evd_hash);

				System.out.println(serverName + ": "
						+ "Event added having event id: " + eventID);

				message = " Manager ID : " + managerID + " , "
						+ "Event type : " + eventType + " , " + "EventId : "
						+ eventID + " , " + "Action Performed : addEvent"
						+ " , " + "Message : Event added successfully\n";
				ServerLog(serverName, message);
				usersLog(managerID, message);

				return true;
			}
		}

		return false;
	}

	public String get_events(String managerID, String eventType,
			HashMap<String, HashMap<String, EventDetails>> servdb,
			String serverName) {

		String events = "";

		if (checkEventType(eventType, servdb)) {
			if (servdb.containsKey(eventType)) {

				for (Map.Entry<String, EventDetails> entry : servdb.get(
						eventType).entrySet()) {

					// Event Id
					events += entry.getKey();
					events += " ";
					// booking capacity
					events += entry.getValue().getBookingCapacity();
					events += ", ";
				}
			}
		}

		return events;
	}

	public boolean remove_event_from_db(String managerID, String eventID,
			String eventType,
			HashMap<String, HashMap<String, EventDetails>> servdb,
			HashMap<String, HashMap<String, ArrayList<String>>> user_db,
			String serverName) {

		boolean removeEventCheck = false;
		String message = "";
		if (checkEventType(eventType, servdb)) {
			HashMap<String, EventDetails> event = servdb.get(eventType);

			if (event.containsKey(eventID)) {

				System.out.println(serverName + ": "
						+ "Event deleted having event id: " + eventID
						+ " from db");
				event.remove(eventID);

				for (String user : user_db.keySet()) {

					if (user_db.get(user).get(eventType).contains(eventID)) {
						System.out.println(serverName + ": "
								+ "Event deleted having event id: " + eventID
								+ " from user db");
						user_db.get(user).get(eventType).remove(eventID);
					} else {
						System.out.println(serverName + ": "
								+ "This Event is not booked by any user");
					}
				}

				message = " Manager ID : " + managerID + " , "
						+ "Event type : " + eventType + " , " + "EventId : "
						+ eventID + " , " + "Action Performed : removeEvent"
						+ " , " + "Message : Event removed successfully\n";
				ServerLog(serverName, message);
				usersLog(managerID, message);

				removeEventCheck = true;
			} else {
				System.out.println(serverName + ": "
						+ "No such event found having event id: " + eventID);

				message = " Manager ID : "
						+ managerID
						+ " , "
						+ "Event type : "
						+ eventType
						+ " , "
						+ "EventId : "
						+ eventID
						+ " , "
						+ "Action Performed : removeEvent"
						+ " , "
						+ "Message : No such event found. Could not removed the event\n";
				ServerLog(serverName, message);
				usersLog(managerID, message);

				removeEventCheck = false;
			}
		}
		return removeEventCheck;
	}

	// Book And Event
	public boolean checkAndAddEvent(String eventID, String customerID,
			String eventType,
			HashMap<String, HashMap<String, EventDetails>> servdb,
			HashMap<String, HashMap<String, ArrayList<String>>> user_db) {

		HashMap<String, EventDetails> generalEvent = servdb.get(eventType);
		boolean checkev = user_db.get(customerID).containsKey(eventType);
		if (checkev && user_db.get(customerID).get(eventType).contains(eventID)) {
			System.out
					.println("Same Event is been Booked with same EventType : "
							+ eventID);
			return false;
		}
		if (generalEvent.containsKey(eventID)
				&& ((generalEvent.get(eventID).getBookingCapacity()) > 0)) {
			int bookingCapacityUpdated = generalEvent.get(eventID)
					.getBookingCapacity() - 1;
			generalEvent.get(eventID)
					.setBookingCapacity(bookingCapacityUpdated);
			servdb.put(eventType, generalEvent);
			HashMap<String, ArrayList<String>> getEventTypes = user_db
					.get(customerID);
			ArrayList<String> temp = getEventTypes.get(eventType);
			if (temp == null) {
				temp = new ArrayList<String>();
			}

			temp.add(eventID);
			getEventTypes.put(eventType, temp);
			user_db.put(customerID, getEventTypes);
			System.out.println("Your Event Has been Booked with EventId :"
					+ eventID);
		} else {
			System.out.println("Event Does not Exist : " + eventID);
			return false;
		}
		return true;
	}

	public boolean checkOutsideEvents(String event[], String customerId, String eventId) {
		int count = 0;
		boolean check = true;
		HashMap<String, Integer> checkDates = new HashMap<String, Integer>();
		for (String ev : event) {
			if (!ev.substring(0, 3)
					.equalsIgnoreCase(customerId.substring(0, 3))
					&& !ev.substring(0, 3).equalsIgnoreCase("")
					&& !ev.substring(0, 2).equalsIgnoreCase("No")) {
				if (!checkDates.containsKey(ev.substring(6, 8))) {
					checkDates.put(ev.substring(6, 8), 1);
					count=1;
				}
				else
				{	
					checkDates.put(ev.substring(6, 8),
							checkDates.get(ev.substring(6, 8)) + 1);
					count++;
				}
				

				if (checkDates.get(ev.substring(6, 8)) >= 3) {
					check = false;					
				}
			}
		}
		if(checkDates.get(eventId.substring(6, 8))!=null){
			if((checkDates.get(eventId.substring(6, 8))+1)<=3) {
			check=true;
			}
		}else if(!checkDates.containsKey(eventId.substring(6, 8))) {
				check=true;
		}

		if (count <= 3 && check)
			return check;
		else
			return false;
	}

	// Reference:
	// https://www.geeksforgeeks.org/working-udp-datagramsockets-java/
	public StringBuilder data(byte[] a) {
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0) {
			ret.append((char) a[i]);
			i++;
		}
		return ret;
	}

	public static void usersLog(String customerId, String logInfo) {
		// System.out.println("User/Manager logging");
		String fileName = customerId;
		String fileDirectory = null;
		String toWrite = null;
		if (!customerId.equals("") || !customerId.equals(" ")) {
			if (customerId.substring(3, 4).equals("M")) {
				fileDirectory = "ManagersLog";

			} else
				fileDirectory = "UsersLog";
			String path = "";
			try {
				path = new File(".").getCanonicalPath();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			File logFile = new File(path + "/logs/" + fileDirectory + "/"
					+ customerId.substring(0, 3) + "/" + fileName + ".txt");
			try {
				if (!logFile.exists())
					logFile.createNewFile();

				BufferedWriter wr = new BufferedWriter(new FileWriter(logFile,
						true));
				String timeStamp = new java.text.SimpleDateFormat(
						"dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
				toWrite = "[" + timeStamp + "]" + logInfo;
				wr.newLine();
				wr.append(toWrite);
				wr.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public static void ServerLog(String serverName, String logInfo) {
		// System.out.println("SERVER logging");
		String toWrite = null;
		String path = "";
		try {
			path = new File(".").getCanonicalPath();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File logFile = new File(path + "/logs/serverLogs/" + serverName
				+ ".txt");

		try {
			logFile.createNewFile();
			BufferedWriter wr = new BufferedWriter(
					new FileWriter(logFile, true));

			String timeStamp = new java.text.SimpleDateFormat(
					"dd/MM/yyyy HH:mm:ss").format(new java.util.Date());
			toWrite = "[" + timeStamp + "]" + logInfo;

			wr.newLine();
			wr.write(toWrite);
			wr.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
