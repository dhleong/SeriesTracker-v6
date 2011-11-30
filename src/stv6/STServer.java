package stv6;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import stv6.http.Client;
import stv6.http.HttpServer;
import stv6.http.HttpSocket;
import stv6.http.request.RequestHandlerManager;

public class STServer extends HttpServer {
	
	private static STServer instance_ = null;
	/**
	 * This page name is for a custom 404 handler
	 */
	public static final String ERROR_404 = "@error_404";
	/**
	 * A handler with this page name will catch ANY page,
	 * 	if it is not already caught by a specific handler
	 */
	public static final String ANY = "@any";
	/**
	 * Requests user to pick a user for the session. In
	 * 	multi-user environments, this MUST be set
	 */
	public static final String USER_SELECT = "@user";
	/**
	 * Status screen while we reload. MUST be set
	 */
	public static final String STATIC_MESSAGE = "@reload";
	/**
	 * Static files
	 */
	public static final String FILES = "files";
	/**
	 * Plugins
	 */
	public static final String PLUGINS = "plugins";
	/**
	 * Selecting a profile
	 */
	public static final String PROFILE_SELECT = "@profile";

	private STServer(int port) throws IOException {
		super(port);
	}

	@Override
	public Client createClient(HttpSocket socket, RequestHandlerManager callback) {
		Client c = new STClient( socket, callback );	
		
		// check user settings; if it's been set on command line or there's only one,
		//	let the client know
		// TODO
		
		// return the client
		return c;
	}
	
	/**
	 * @return The IP address this server is broadcasting on.
	 */
	public static String getBroadcastingIp() {
		String broadIp = null;
		try {
			broadIp = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return broadIp;
	}
	
	@Override
	public RequestHandlerManager getHandlerManager() {
		return STHandlerManager.getInstance();
	}
	
	public static String getHomeLink() {
		// apparently PS3 is retarded
		return "./?dor="+System.currentTimeMillis();
	}
	
	public static STServer getInstance() {
		return instance_;
	}
	
	public static STServer getInstance(int port) throws IOException {
		if (instance_ == null)
			instance_ = new STServer(port);
		else if (port != instance_.getPort())
			instance_.setPort(port);
		
		return instance_;
	}

}
