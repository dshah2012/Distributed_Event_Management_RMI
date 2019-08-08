package Client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ImplementRemoteInterface.Helpers;
import ServerInterface.McInterface;

public class Client {

	static McInterface mcobj;

	public static String get_event_type(String key, String userName) {
		String eventType = "";
		String message = "";

		if (key.equals("c"))
			eventType = "conferences";
		else if (key.equals("s"))
			eventType = "seminars";
		else if (key.equals("t"))
			eventType = "trade shows";
		else {
			System.out.println("Error: Invalid event type");
			eventType = "invalid";

			message = " Event type : " + key + " , "
					+ "Action Performed : check_event" + " , "
					+ "Message : You have entered invalid event type\n";

			Helpers.ServerLog(userName.substring(0, 3), message);
			Helpers.usersLog(userName, message);
		}

		return eventType;
	}

	public String get_user(String name) throws Exception {

		if (Character.toString(name.charAt(3)).equals("C")) {
			return "user";
		} else if (Character.toString(name.charAt(3)).equals("M")) {
			return "manager";
		}
		return "invalid";
	}

	public void set_server_obj(String libServer, int port) throws Exception {
		Registry registry = LocateRegistry.getRegistry(port);
		mcobj = (McInterface) registry.lookup(libServer);
	}

	public static boolean check_event_id(String eventId, String userName) {
		String message = "";
		if (eventId.length() == 10
				&& eventId.matches("^(TOR|MTL|OTW)(A|M|E)\\d{6}$")) {

			return true;
		} else {
			message = " Manager ID : " + userName + " , " + "Event ID : "
					+ eventId + " , " + "Action Performed : check_event_id"
					+ " , " + "Message : You have entered invalid event id.\n";

			System.out.println("You have entered invalid event id. Try again");
			Helpers.ServerLog(userName.substring(0, 3), message);
			Helpers.usersLog(userName, message);

			return false;
		}
	}

	public String checkUsername(String userName) throws Exception {

		// Validate user e.g. TORM2345
		if (userName.length() == 8
				&& userName.matches("^(TOR|MTL|OTW)(M|C)\\d{4}$")) {

			System.out.println("Username verified");

			// -----------------
			// Toronto Server
			// -----------------
			if (userName.substring(0, 4).equals("TORC")
					|| userName.substring(0, 4).equals("TORM")) {

				set_server_obj("toronto_server", 6002);
				return get_user(userName);
			}
			// -----------------
			// Ottawa Server
			// -----------------
			else if (userName.substring(0, 4).equals("OTWC")
					|| userName.substring(0, 4).equals("OTWM")) {

				set_server_obj("ottawa_server", 6001);
				return get_user(userName);
			}
			// -----------------
			// Montreal Server
			// -----------------
			else if (userName.substring(0, 4).equals("MTLC")
					|| userName.substring(0, 4).equals("MTLM")) {

				set_server_obj("montreal_server", 6000);
				return get_user(userName);
			}
		}

		return "invalid";
	}

	public static void main(String args[]) throws Exception {

		Client cl = new Client();
		Scanner sc = new Scanner(System.in);
		String res = "start";

		System.out.println("Enter your Username");
		while (res.equalsIgnoreCase("invalid") || res.equals("start")) {
			String userName = sc.next();

			res = cl.checkUsername(userName);

			if (res.equals("invalid")) {
				System.out
						.println("Invalid user, please enter valid user name like MTLM1234, OTWC4567, TORM2587");
			}
			// ------------------------------
			// ------- Manager methods ------
			// ------------------------------
			else if (res.equals("manager")) {
				int choice = 0;

				do {
					System.out.println("Select any of the below entries\n"
							+ "1. Add Event\n" + "2. Remove Event\n"
							+ "3. List Event availability\n"
							+ "4. Book Event\n" + "5. Get Booking Schedulde\n"
							+ "6. cancel Event\n" + "7. Multi Threading");

					try {
						choice = sc.nextInt();
					} catch (Exception e) {
						System.out.println("Invalid Choice");
					}

					String eventId = "";
					String eventType = "";

					switch (choice) {

					case 1:
						System.out
								.print("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out.println(" e.g. MTLE130519 c 3)");
						System.out
								.println("Enter eventId eventType and BookingCapacity to add");

						eventId = sc.next();
						eventType = sc.next();
						int bookCapacity = sc.nextInt();

						eventType = get_event_type(eventType, userName);

						if (eventType.equals("invalid"))
							break;

						if (!check_event_id(eventId, userName))
							break;

						boolean addRes = mcobj.addEvent(eventId, eventType,
								bookCapacity, userName);
						if (addRes)
							System.out
									.println("Event has been added/updated successfully");
						else
							System.out
									.println("Error in adding the Event! Check server logs ...");
						break;

					case 2:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out
								.println("Enter eventId eventType to be deleted");

						String remove_eventId = sc.next();
						String remove_eventType = sc.next();

						remove_eventType = get_event_type(remove_eventType,
								userName);
						if (remove_eventType.equals("invalid"))
							break;

						if (!check_event_id(remove_eventId, userName))
							break;

						boolean remRes = mcobj.removeEvent(remove_eventId,
								remove_eventType, userName);

						if (remRes)
							System.out
									.println("Successfully deleted the eventId from the Event");
						else
							System.out.println("Could not delete the Event");
						break;

					case 3:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out.println("Enter any EventType");
						String list_eventType = sc.next();
						
						
						list_eventType = get_event_type(list_eventType,
								userName);
						if (list_eventType.equals("invalid"))
							break;

						String list_events = mcobj.listEventAvailability(
								list_eventType, userName);

						System.out.println("--------------------------");
						System.out.println(list_events);
						System.out.println("--------------------------");
						break;

					case 4:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");

						System.out.println("Enter Event Id and Event Type ");
						eventId = sc.next();
						eventType = sc.next();

						eventType = get_event_type(eventType, userName);
						if (eventType.equals("invalid"))
							break;

						boolean bookRes = mcobj.bookEvent(userName, eventId,
								eventType);

						if (bookRes)
							System.out
									.println("You succesfully booked the event");
						else
							System.out
									.println("You have already booked the Event with the same EventId :"
											+ eventId
											+ "\n or The event is not Present ");
						break;

					case 5:
						System.out.println("-- Booking Schedule --");
						System.out.println(mcobj.getBookingSchedule(userName));
						break;

					case 6:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out
								.println("Enter eventID and eventType which you want to cancel");
						String cancel_eventID = sc.next();
						eventType = sc.next();

						eventType = get_event_type(eventType, userName);
						if (eventType.equals("invalid"))
							break;

						boolean returnRes = mcobj.cancelEvent(userName,
								cancel_eventID, eventType);

						if (returnRes) {
							System.out.println("Event cancelled");
						} else
							System.out.println("Could not cancel vent");
						break;

					case 7:
						System.out.println("Running Threads Now");
						Runnable cus4 = new Runnable() {
							public void run() {
								try {
									mcobj.bookEvent("TORC1237", "TORM123456", "conferences");
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						Thread thread4 = new Thread(cus4);
						thread4.start();
						
						
						Runnable udp_task = new Runnable() {
							public void run() {
								try {
									mcobj.addEvent("TORM123456", "conferences", 2, "TORM1234");
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						Thread thread = new Thread(udp_task);
						thread.start();
						
						
						Runnable cus = new Runnable() {
							public void run() {
								try {
									mcobj.bookEvent("TORC1234", "TORM123456", "conferences");
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						Thread thread1 = new Thread(cus);
						thread1.start();
						
						Runnable cus2 = new Runnable() {
							public void run() {
								try {
									mcobj.bookEvent("TORC1235", "TORM123456", "conferences");
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						Thread thread2 = new Thread(cus2);
						thread2.start();
						
						Runnable cus3 = new Runnable() {
							public void run() {
								try {
									mcobj.bookEvent("TORC1236", "TORM123456", "conferences");
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};

						Thread thread3 = new Thread(cus3);
						thread3.start();
						
						break;
					
					}
					
					

				} while (choice != 8);
			}

			// -------------------------------
			// ------- Customer methods ------
			// -------------------------------
			else if (res.equals("user")) {
				int userSel = 0;
				String customerId = userName;

				do {
					System.out
							.println("Select any of the below entries\n"
									+ "(Event types: conferences, trade-shows and seminars)\n"
									+ "1. Book Event\n"
									+ "2. Get Booking Schedulde\n"
									+ "3. cancel Event\n" + "4. Exit");

					try {
						userSel = sc.nextInt();
					} catch (Exception e) {
						System.out.println("Invalid Choice");
					}

					switch (userSel) {

					case 1:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out.println("Enter Event Id and Event Type ");
						String eventID = sc.next();
						String eventType = sc.next();

						eventType = get_event_type(eventType, userName);
						if (eventType.equals("invalid"))
							break;

						boolean bookRes = mcobj.bookEvent(customerId, eventID,
								eventType);

						if (bookRes)
							System.out
									.println("You succesfully booked the event");
						else
							System.out
									.println("You have already booked the Event with the same EventId :"
											+ eventID
											+ "\n or The event is not Present ");
						break;

					case 2:
						System.out.println("-- Booking Schedule --");
						System.out
								.println(mcobj.getBookingSchedule(customerId));
						break;

					case 3:
						System.out
								.println("(For event type enter: c - conferences, "
										+ "t - trade-shows and s - seminars");
						System.out
								.println("Enter eventID and eventType which you want to cancel");
						String cancel_eventID = sc.next();
						eventType = sc.next();

						eventType = get_event_type(eventType, userName);
						if (eventType.equals("invalid"))
							break;

						boolean returnRes = mcobj.cancelEvent(userName,
								cancel_eventID, eventType);

						if (returnRes) {
							System.out.println("Event cancelled");
						} else
							System.out.println("Could not cancel vent");
						break;

					}

				} while (userSel != 4);
			} else {
				System.out.println("Wrong user ID entered");
			}
		}
	}
}
