package Server;


public class EventDetails {

	private String requestType;
	private String clientId;
	private int bookingCapacity;
	
	public String getRequestType() {
		return requestType;
	}
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public int getBookingCapacity() {
		return bookingCapacity;
	}
	public void setBookingCapacity(int bookingCapacity) {
		this.bookingCapacity = bookingCapacity;
	}
}
