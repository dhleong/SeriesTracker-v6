package stv6.handlers;

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
		if (exe == null || !exe.exists()) 
			return false; 
			
		File plugin = new File("plugins/" + pluginName);
		if (plugin == null || !plugin.exists()) 
			return false;
		
		try {			
			String[] cmdarray = {exe.getAbsolutePath(), 
					plugin.getAbsolutePath(), args};
//			System.out.print("Executing: ");
//			for (String c : cmdarray)
//				System.out.print(c +"\n");
//			System.out.println();
			Process p = Runtime.getRuntime().exec(cmdarray);
			
			// read the results right into the response
			BufferedReader stdInput = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			String ln = null;
			while ((ln = stdInput.readLine()) != null) {
//				System.out.println(ln);
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
