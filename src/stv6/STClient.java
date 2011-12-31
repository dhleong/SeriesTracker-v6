package stv6;

import java.io.IOException;

import stv6.http.Client;
import stv6.http.HttpSocket;
import stv6.http.pools.ClientPool;
import stv6.http.request.Request;
import stv6.http.request.RequestHandlerManager;
import stv6.http.request.Response;

public class STClient implements Client {
	
	/**
	 * Cookie name that holds the user_id
	 */
	public static final String COOKIE_USERID = "st_userid";

	public static final String DEFAULT_USERNAME = "Default";
	
	private final HttpSocket skt;
	private final RequestHandlerManager callback;
	
	public STClient(HttpSocket skt, RequestHandlerManager callback) {
		this.skt = skt;
		this.callback = callback;
	}

	@Override
	public boolean isReady() {
		return skt.ready();
	}

	@Override
	public void process(String name) {
		
		// see what they want to do
		Request r;
		try {
			r = Request.readFrom(skt);
			if (ClientPool.DEBUG) System.out.println("! " + name + ":  " + r.path);
		} catch (IOException e) {
			e.printStackTrace();
			skt.close();
			return;
		}
		
		// handle the request
		Response resp = callback.handle(r);		
		skt.write(resp);
		
		// clean up
		skt.close();	
	}

    @Override
    public void timeout() {
        Response resp = new Response();
        resp.setStatus("408 Request Timeout");
        skt.write(resp);
        skt.flush();
        skt.close();
    }

}
