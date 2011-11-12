package stv6.http;

import java.io.IOException;
import java.net.UnknownHostException;

import stv6.http.request.Request;
import stv6.http.request.Response;

public class HttpRequestor implements Runnable {
	private final HttpSocket skt;
	private final Request r;
	private HttpResponseCallback cb;
	
	private HttpRequestor(Request r, HttpSocket skt) {
		this.skt = skt;
		this.r = r;
	}
	
	public Request getRequest() {
		return r;
	}
	
	/**
	 * Blocking version; returns the Response from the server
	 * 	as soon as it's received
	 * 
	 * @return The Response
	 * @throws IOException
	 */
	public Response request() throws IOException {
		skt.write(r);
		
		return Response.readFrom(skt);
	}
	
	/**
	 * Non-blocking version; writes the request, then
	 * 	waits until the it gets a response before reading.
	 * 
	 * @param cb Called with the Response when received
	 */
	public void request(HttpResponseCallback cb) {
		this.cb = cb;
		
		skt.write(r);
		new Thread(this).start();
	}
	
	/**
	 * Fairly naive implementation, but... oh well. We're shouldn't be
	 * 	doing that much heavy lifting, so...
	 */
	@Override
	public void run() {
		try {
			cb.callback( Response.readFrom(skt) );
		} catch (IOException e) {
			cb.callback( null );
		}
	}
	
	private static HttpRequestor createFor(String url, String method, int port) 
			throws UnknownHostException, IOException {
		if (!url.startsWith("http://")) {
			// unsupported protocol
			return null;
		}
		
		String server, path;
		int pos = url.indexOf('/', 8);
		if (pos < 0) {
			server = url.substring(7);
			path = "/";
		} else {
			server = url.substring(7, pos);
			path = url.substring(pos);
		}
		
		
		HttpSocket skt = new HttpSocket(server, port);
		Request r = new Request(method, path, skt);
		
		return new HttpRequestor(r, skt);
	}
	
	public static HttpRequestor get(String url) throws UnknownHostException, IOException {
		return get(url, 80);
	}
	
	public static HttpRequestor get(String url, int port) throws UnknownHostException, IOException {
	    return createFor(url, "GET", port);
	}
	
	public static HttpRequestor post(String url) throws UnknownHostException, IOException {
        return post(url, 80);
    }
	
	public static HttpRequestor post(String url, int port) throws UnknownHostException, IOException {
		return createFor(url, "POST", port);
	}
}
