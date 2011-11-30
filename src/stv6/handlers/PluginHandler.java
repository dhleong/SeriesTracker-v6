package stv6.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import stv6.Profile;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.http.request.Response;

public class PluginHandler implements RequestHandler {
	public static final String HANDLED_PAGE = "plugins";

	@Override
	public String getHandledPage() {
		return HANDLED_PAGE;
	}

	@Override
	public boolean handle(Request r, Response resp) {
		String pluginName = r.path.substring(r.path.indexOf('/', 1)+1);
		String args = null;
		int argsPos = pluginName.indexOf('?');
		if (argsPos > -1) {
			args = pluginName.substring(argsPos+1);
			pluginName = pluginName.substring(0, argsPos);
		}
		File exe = Profile.getInstance().getPluginExe();
//		if (exe == null || !exe.exists()) 
//			return false; 
			
		File plugin = new File("plugins/" + pluginName);
		if (plugin == null || !plugin.exists()) 
			return false;
		
		try {			
			String[] cmdarray;
			if ((exe == null || !exe.exists()) && plugin.canExecute()) {
				// no exe, but the plugin itself can be executed
				cmdarray = (args != null) ? 
						new String[] {plugin.getAbsolutePath(), args} :
						new String[] {plugin.getAbsolutePath()};
			} else {
				cmdarray = (args != null) ? 
					new String[] {exe.getAbsolutePath(), plugin.getAbsolutePath(), args} :
					new String[] {exe.getAbsolutePath(), plugin.getAbsolutePath()};
			}
//			System.out.print("Executing: ");
//			for (String c : cmdarray)
//				System.out.print(c +"\n");
//			System.out.println();
			Process p = Runtime.getRuntime().exec(cmdarray);
			
			// read the results right into the response
			p.getInputStream().mark(2000); // should be way more than enough
			BufferedReader stdInput = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String ln = null;
			
			// first, read any headers
			String contentType = null;
			int contentLength = 0; // we require Content-length for binary!
			int read = 0;
			while ((ln = stdInput.readLine()) != null) {
//				System.out.println(ln);
				if (ln.length() == 0) {
					read += 2;
					break;
				}
				if (ln.startsWith("Content-type")) 
					contentType = ln.substring(13); // get the type
				if (ln.startsWith("Content-length")) {
					try {
						contentLength = Integer.parseInt(ln.substring(15));
					} catch (NumberFormatException e) {
						contentLength = 0;
					}
				}
				read += ln.length() + 1; // +1 for newline
			}
			
			if (contentType != null) {
				resp.setContentType(contentType);
				
				if (!(contentType.startsWith("application") || 
						contentType.startsWith("text")) &&
						contentLength > 0) {
					// binary data
					byte[] data  = new byte[contentLength];
					p.getInputStream().reset();
					p.getInputStream().skip(read);
					BufferedInputStream is = new BufferedInputStream(
							p.getInputStream());
			        
					int offset = 0;
			        int numRead = 0;
			        
			        while (offset < data.length
			               && (numRead=is.read(data, offset, 
			            		   data.length-offset)) >= 0) {
			            offset += numRead;
			        }
			        
					resp.setBody(data);
					stdInput.close();
					return true;
				}
			}
			
			// okay, if we got here it should just be
			//	plain text
			while ((ln = stdInput.readLine()) != null) {
				resp.append(ln);
			}
			
			stdInput.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
