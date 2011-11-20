package stv6.http.request;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;

import stv6.http.HttpServer;
import stv6.http.HttpSocket;
import stv6.http.HttpWritable;
import stv6.http.request.variables.Cookie;
import stv6.http.request.variables.CookieJar;
import stv6.http.request.variables.Variable;
import stv6.http.request.variables.VariableList;

/**
 * Wraps the HTTP response to a client
 * 
 * @author dhleong
 *
 */
public class Response implements Appendable, HttpWritable {
	
	private String contentType = null, status = "200 OK";
	private StringBuffer stringBody = null;
	private byte[] byteBody = null;
	
	private final VariableList headers;
	private final CookieJar cookies;
	
	public Response() {
		stringBody = new StringBuffer();
		headers = new VariableList();
		cookies = new CookieJar();
	}
	
	/**
	 * Alias for {@link #addBody(String)}
	 * @param addition
	 */
	@Override
	public Appendable append(CharSequence addition) {
		addBody(addition);
		return this;
	}
	
	@Override
	public Appendable append(char c) throws IOException {
		if (stringBody == null)
			stringBody = new StringBuffer(c);
		else stringBody.append(c);
		
		return this;
	}

	@Override
	public Appendable append(CharSequence csq, int start, int end)
			throws IOException {
		return append(csq.subSequence(start, end));
	}
	
	/**
	 * Add some text to the body of this response
	 * 
	 * @param addition
	 */
	public void addBody(CharSequence addition) {
		if (stringBody == null)
			stringBody = new StringBuffer(addition);
		else stringBody.append(addition);
	}

	public String getBody() {
		return stringBody.toString();
	}
	
	/**
	 * This is a dumb process and doesn't consult
	 * 	{@link #isText()} to see if it is wise to use
	 * 	this or not
	 * 
	 * @return
	 */
	@Override
	public byte[] getBytes() {
		return byteBody;
	}
	
	/**
	 * @return How big the body is, regardless of how it was created
	 */
	public int getLength() {
		if (isText()) {
			// silliness? Oh well. If we're calling this, then
			//  we shouldn't be making any more changes...
			byteBody = stringBody.toString().getBytes(); 
		} 
		return byteBody.length;
	}	
	
	public String getStatus() {
		return status;
	}
	
	/**
	 * @return True if the body was created {@link #addBody(String)}
	 * 	or {@link #append(String)} or {@link #setBody(String)},
	 * 	and can be sent using something that writes straight text,
	 * 	like PrintWriter
	 */
	@Override
	public boolean isText() {
		return byteBody == null;
	}
	
	public void setBody(String body) {
		stringBody = new StringBuffer(body);
	}
	
	public void setBody(byte[] data) {
		byteBody = data;
	}
	
	public void setCookie(Cookie c) {
		cookies.put(c);
	}
	
	public void setHeader(String name, String value) {
	    if ("content-type".equals(name.toLowerCase()))
	        // TODO Could probably just make contentType
	        // into a header as it should be, but... whatever
	        setContentType(value);
	    else
	        headers.put(name, value);
    }

    /**
	 * EG: "200 OK" or "404 Not Found"
	 * 
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * EG: "text/html", "text/css", "image/png" 
	 * @param type
	 */
	public void setContentType(String type) {
		contentType = type;
	}
	
	/**
	 * This is a dumb process, and doesn't check if
	 * 	{@link #isText()} or not. Just so you know
	 *  
	 * @param output
	 */
	@Override
	public void writeBody(PrintWriter output) {
		output.append( stringBody );
	}
	
	/**
	 * Dump the header informations into the PrintWriter
	 * 
	 * @param output
	 */
	@Override
	public void writeHeaders(PrintWriter output) {
		output.write("HTTP/1.0 "+status+"\r\n");
		output.write("Date: " + HttpServer.formatDate(new Date()) + "\r\n");
		
		if (contentType != null && contentType.indexOf("image") == -1) {
			// make sure foolish browsers (*caugh*PS3*caugh*) don't cache
			//  stuff and end up displaying the wrong things
			// caching images is okay though
			output.append("Cache-Control: no-cache\r\n");
			output.append("Expires: " + HttpServer.formatDate(new Date()) + "\r\n");
		}
		
		// the PS3 browser is retarded and needs Content-Type, but
		//  I don't have any great way to serve it correctly right now,
		//  so this is a bit of a hack...		
		if (contentType != null)
			output.append("Content-type: " + contentType + "\r\n");
		
		// any other headers?
		for (Variable v : headers)
		    output.append(v.key + ": " + v.value + "\r\n");
		
		Cookie c;
		for (Variable v : cookies) {
			c = (Cookie) v;
			output.append(c.getSetString());
		}
					
		output.append("Content-length: " + getLength() + "\r\n");		
		output.append("\r\n");	
		output.flush();
	}

	public void writeHeaders(PrintStream out) {
		writeHeaders( new PrintWriter(out) );
	}

	/**
	 * Blocking read 
	 * 
	 * @param skt
	 * @return
	 * @throws IOException
	 */
	public static Response readFrom(HttpSocket skt) throws IOException {
		while (!skt.ready()) {}
		
		String line = skt.readLine();
		int pos = line.indexOf(' ');
		if (pos < 0)
			// invald somehow...
			throw new IOException();
		
		Response ret = new Response();
		ret.setStatus( line.substring(pos+1) );
		
		// skip the rest of the headers except length... :P
		int length = 0;
		for (;;) {
			line = skt.readLine();
			if (line == null || line.isEmpty())
				break;
			
			if (line.startsWith("Content-Length"))
				length = Integer.parseInt(line.substring(16));
		}
		
		ret.append(skt.read(length));
		
		return ret;
	}
}
