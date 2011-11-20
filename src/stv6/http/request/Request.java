package stv6.http.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import stv6.User;
import stv6.http.HttpSocket;
import stv6.http.HttpWritable;
import stv6.http.request.variables.CookieJar;
import stv6.http.request.variables.Variable;
import stv6.http.request.variables.VariableList;

public class Request implements HttpWritable {
	
	public final String method, path;
	private final HttpSocket skt;
	private final String pageName;
		
	private final VariableList headers, getVars, postVars;
	private final CookieJar cookies;
	private StringBuilder body;
	
	private User user;
	
	private boolean hasHeaders = false, hasBody = false;
		
	public Request(String method, String path, HttpSocket skt) {
		this.method = method;
		this.path = path;
		this.skt = skt;
		
		headers = new VariableList();
		getVars = new VariableList();
		postVars = new VariableList();
		cookies = new CookieJar();
		
		// figure out page name
		int nameEnd = path.indexOf('/', 1);
		if (nameEnd < 0) {
			nameEnd = path.indexOf('?');
			if (nameEnd > -1) {
				// extract GET vars
				parseUrlEncodedVars(path.substring(nameEnd+1), getVars);
			}
		}
		if (nameEnd < 0 && path.length() > 1)
			pageName = path.substring(1);
		else if (nameEnd > -1 && path.charAt(1) != '?')
			pageName = path.substring(1, nameEnd);
		else 
			pageName = "index";
	}
	
	/**
	 * For now, at least, this should never have binary data
	 */
	@Override
	public byte[] getBytes() {
		return null;
	}
	
	private long getBodyLength() {
	    return (body == null) ?
	            postVars.length() :
	                body.length();
	}
	
	public CookieJar getCookies() {
		return cookies;
	}
	
	public VariableList getGetVars() {
		return getVars;
	}
	
	public VariableList getHeaders() {
	    return headers;
	}
	
	public String getPage() {
		return pageName;
	}
	
	public VariableList getPostVars() {
		return postVars;
	}

	public User getUser() {
		return user;
	}
	
	public boolean isLocal() {
		return skt.isLocal();
	}
	
	/**
	 * For now, at least, this should never have binary data
	 */
	@Override
	public boolean isText() {
		return true;
	}
	
	public boolean readBody() {
		if (hasBody)
			return true;
		
		if (!method.equals("POST")) {
			// there shouldn't be a body
			hasBody = true;
			return false;
		}
		
		// ensure we've read past headers!
		readHeaders();
		
		int length = 0;
		try {
			length = Integer.parseInt( headers.getValue("Content-Length") );
		} catch (Exception e) {
			// either not set or not an int; fail
			hasBody = true;
			return false;
		}
		
		try {
			String body = URLDecoder.decode( skt.read(length), "UTF-8" );			
			parseVars(body, postVars, "&");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Can't decode POSTs. What the heck?");
			System.exit(1);
		}

		hasBody = true;
		return true;
	}

	/**
	 * Read headers from the socket. Note that this
	 * 	will only read once, so feel free to call
	 * 	it wherever you want to make sure the headers
	 * 	have been read
	 * 
	 * @param skt
	 * @return
	 */
	public boolean readHeaders() {
		if (hasHeaders)
			return true;
		
		try {
			String line = "-";
			String[] tmp;		
			while (true) {
				line = skt.readLine();
				if (line == null || line.isEmpty()) 
					break;
				
				tmp = line.split(": ", 2);
				if (tmp.length == 1)
					continue;
				
				setHeader(tmp[0], tmp[1]);
			}
		} catch (IOException e) {
			return false;
		}		

		hasHeaders = true;
		return true;
	}
	
	public void setBody(StringBuilder body) {
	    this.body = body;
	}
	
	public void setHeader(String key, String value) {
		headers.put(key, value);
		if (key.equals("Cookie")) 
			parseCookies(value, cookies);
		
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public void writeBody(PrintWriter output) {
		// post vars, I guess?
	    if (body != null)
	        output.write(body.toString());
	    else 
	        postVars.writeTo(output);
		output.flush();
	}

	@Override
	public void writeHeaders(PrintWriter output) {
		output.write(method+" " + path + " HTTP/1.0\r\n");
		if (getBodyLength() > 0) {
		    if (!headers.isSet("Content-Type")) // we might set it manually
		        output.write("Content-Type: application/x-www-form-urlencoded\r\n");
			output.write("Content-Length: " + getBodyLength() + "\r\n");
		}
		for (Variable v : headers) {
			output.write(v.key);
			output.write(": ");
			output.write(v.value);
			output.write("\r\n");
		}
		if (!headers.isSet("Cookies") && cookies.size() > 0) {
			output.write("Cookies: ");
			cookies.writeTo(output);
			output.write("\r\n");
		}
		
		output.write("\r\n");
		output.flush();
	}

	/*
	public static void extractVars(String input, VariableList target) {		
		String[] tmp, rawVars = input.split("&");
		for (String raw : rawVars) {
			tmp = raw.split("=", 2);
			if (tmp != null && tmp.length == 2) {
				target.put(tmp[0], tmp[1]);
			}
		}
	}
	*/

	public static void parseCookies(String rawCookies, CookieJar target) {
		parseVars(rawCookies, target, "; ");
	}
	
	public static void parseUrlEncodedVars(String rawVars, VariableList target) {
		parseVars(rawVars, target, "&");
	}

	private static void parseVars(String raw, VariableList target, String separator) {
		String[] rawList = raw.split(separator);
		int eqPos;
		for (String s : rawList) {
			eqPos = s.indexOf('=');
			
			// improper format; ignore the rest
			if (eqPos == -1)
				break;
			
			String value;
			try {
				value = URLDecoder.decode(s.substring(eqPos+1), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				value = s.substring(eqPos+1);
			}
			target.put(s.substring(0, eqPos), value);
		}
	}

	/**
	 * Reads the basic info from the stream and returns a request
	 * 	If you need more info (EG: headers, post variables), call
	 * 	the relevant methods on the returned Request object. 
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	public static Request readFrom(HttpSocket input) throws IOException {
				
		// handle initial request lines
		String initialRequest = input.readLine();
		int methodEnd = initialRequest.indexOf(' ');
		int pathEnd = initialRequest.indexOf(' ', methodEnd+1);
		
		String method = initialRequest.substring(0, methodEnd);
		String path = initialRequest.substring(methodEnd+1, pathEnd);
		Request ret = new Request(method, path, input);
		
		return ret;
	}

}
