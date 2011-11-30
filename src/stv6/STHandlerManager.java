package stv6;

import stv6.http.request.Request;
import stv6.http.request.RequestHandlerManager;
import stv6.http.request.Response;

public class STHandlerManager extends RequestHandlerManager {

	private static final STHandlerManager instance_ = new STHandlerManager();
	
	private volatile Boolean isStatic = Boolean.TRUE;

	public static STHandlerManager getInstance() {
		return instance_;
	}
	
	public Response handle(Request r) {		
		// first of all, file requests or plugins should always be okay
		// plugins allowed basically so I can reset mediatomb if it died
		if (r.getPage().equals(STServer.FILES) ||
				r.getPage().equals(STServer.PLUGINS))
			return super.handle(r);
		 
		Response resp = new Response();
		
		// set the content type for PS3 to be happy
		// it's not a file, so it should be html!
		resp.setContentType("text/html"); 
			 
		// make sure we have a profile!
		if (!Profile.getInstance().isSelected() && 
				specificHandler(STServer.PROFILE_SELECT, r, "display Profile Select page", resp)) {
			return resp;
		}
		
		// check for a static message (eg: reloading)
		synchronized(isStatic) {
			if (isStatic && specificHandler(STServer.STATIC_MESSAGE, r, 
					"display Static Message page", resp)) {
				return resp;
			} 
		}
		// make sure we know the user		
		if (!Profile.getInstance().fillUser(r) &&
				specificHandler(STServer.USER_SELECT, r, "request a user", resp)) {			
			return resp;
		}
		
		// default actions
		return super.handle(r);
	}
	
	/**
	 * Sets the server to only display the StaticMessageHandler (or not)
	 * 
	 * @param isOn
	 */
	public void setStaticMessage(boolean isOn) {
		synchronized(isStatic) {
			this.isStatic = isOn;
		}
	}
	
	/**
	 * Try a specific handler; dies on failure, but "resp" will be
	 * 	filled on success
	 * 
	 * @param page
	 * @param r
	 * @param needToDo
	 * @param resp
	 * @return True on success, else false
	 */
	private boolean specificHandler(String page, Request r, String needToDo, Response resp) {		
		if (tryWith(page, r, resp)) 
			return true;
		else {
			System.err.println("Need to "+needToDo+", but handler unavailable....");
			System.err.println("Please fix the code!");
			System.exit(1);
			return false;
		}
	}
}
