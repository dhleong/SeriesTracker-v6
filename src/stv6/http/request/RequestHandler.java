package stv6.http.request;


public interface RequestHandler {
	
	/**
	 * @return the String name of the page
	 * 	this handler handles
	 */
	public String getHandledPage();
	
	/**
	 * 
	 * @param r
	 * @param resp
	 * @return True if successfully handled, else false
	 */
	public boolean handle(Request r, Response resp);
}
