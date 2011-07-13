/**
 * 
 */
package stv6.http.request;

import java.util.HashMap;

import stv6.STServer;
import stv6.http.HttpServer;

public class RequestHandlerManager {
	private HashMap<String, RequestHandler> handlers;
	
	public RequestHandlerManager() {
		handlers = new HashMap<String, RequestHandler>();
	}
	
	public Response handle(Request r) {			
		Response resp = new Response();
		resp.setContentType("text/html"); // stupid PS3 browser
		
		if (tryWith(r.getPage(), r, resp))
			return resp;
		
		// invalid page; first look for a catch-all
		if (tryWith(STServer.ANY, r, resp))
			return resp;
		
		// nope; look for a 404 handler
		if (tryWith(STServer.ERROR_404, r, resp)) 
			return resp;
		
		// generate 404
		HttpServer.generate404(resp);
		return resp;
	}
	
	protected boolean tryWith(String pageName, Request r, Response resp) {
		if (handlers.containsKey(pageName)) {
			RequestHandler handler = handlers.get(pageName);				
			if (handler.handle(r, resp))
				return true;
		}
		
		return false;
	}

	public void registerHandler(RequestHandler handler) {
		handlers.put( handler.getHandledPage(), handler );
	}
	
}