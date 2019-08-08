package ServerInterface;

import java.rmi.*;
import java.util.*;


public interface McInterface extends Remote {
	
	// Manager methods
	public boolean addEvent(String eventID, String eventType, int bookingCapacity, String managerID) throws RemoteException;

	public boolean removeEvent(String eventID, String eventType, String managerID) throws RemoteException;

	public String listEventAvailability(String eventType, String managerID) throws RemoteException;

	// Customer methods
	public boolean bookEvent(String customerID, String eventID, String eventType) throws RemoteException;

	public String getBookingSchedule(String customerID) throws RemoteException;

	public boolean cancelEvent(String customerID, String eventID, String eventType) throws RemoteException;

}
