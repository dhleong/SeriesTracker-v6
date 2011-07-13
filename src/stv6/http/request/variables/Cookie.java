package stv6.http.request.variables;

import java.util.Date;

import stv6.http.HttpServer;

public class Cookie extends Variable {
	private static final String SET_FORMAT = "Set-Cookie: %s=%s; path=%s; %s\r\n";
	
	public final Date expires;
	public final String path;

	public Cookie(String key, String value) {
		this(key, value, null);
	}
	
	public Cookie(String key, String value, Date expires) {
		this(key, value, expires, "/");
	}
	
	public Cookie(String key, String value, Date expires, String path) {
		super(key, value);
		
		this.expires = expires;
		this.path = path;
	}

	/**
	 * Generate the string to set this cookie
	 * @return
	 */
	public String getSetString() {
		String date = (expires == null) ? "" :
			 HttpServer.formatDate(expires);
		
		return String.format(SET_FORMAT, key, value, path, date);
	}
}
