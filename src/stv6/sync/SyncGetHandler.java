package stv6.sync;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import stv6.Profile;
import stv6.http.HttpRequestor;
import stv6.http.HttpResponseCallback;
import stv6.http.request.Request;
import stv6.http.request.Response;
import stv6.http.request.variables.ListVariable;
import stv6.http.request.variables.VariableList;
import stv6.series.BasicSeries;
import stv6.series.Series;
import stv6.sync.SyncSettings.SyncPage;

public class SyncGetHandler {

	private static void buildRequest(SyncSettings settings, Request r) {
		if (settings.isUsingPassword())
			r.getPostVars().put("p", settings.getPassword());
		
		// last sync
		r.getPostVars().put("ls", Profile.getInstance().getLastSyncFor(SyncPage.GET));
	}
	
	public static void handle(SyncSettings settings) throws UnknownHostException, IOException {
		HttpRequestor rq = settings.getRequestorFor(SyncPage.GET);
		Request r = rq.getRequest();
		buildRequest(settings, r);
		
		rq.request(new HttpResponseCallback() {
			@Override
			public void callback(Response r) {				
				List<Series> toCreate = new LinkedList<Series>();
				VariableList vars = new VariableList();
				Request.parseUrlEncodedVars(r.getBody(), vars);
				
				if (vars.isSet("error")) {
					System.err.println("\nGET: " + vars.getValue("error"));
				}
				
				if (vars.isSet("i") && vars.isSet("n")) {
					ListVariable ids = (ListVariable) vars.get("i");
					ListVariable names = (ListVariable) vars.get("n");
					if (ids.size() != names.size()) {
						System.err.println("\nGET: Invalid server response (data length mismatch)");
						return;
					}
					
					int id;
					for(int i=0; i<ids.size(); i++) {
						id = Integer.parseInt(ids.get(i).value);
						toCreate.add(new BasicSeries(id, names.get(i).value));
					}
				}
				
				Profile.getInstance().createNewSeries(toCreate);
			}			
		});
	}

}
