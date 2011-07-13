package stv6.sync;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import stv6.Profile;
import stv6.User;
import stv6.handlers.util.StaticMessageHandler;
import stv6.http.HttpRequestor;
import stv6.http.request.Request;
import stv6.http.request.Response;
import stv6.http.request.variables.ListVariable;
import stv6.http.request.variables.VariableList;
import stv6.series.Series;
import stv6.series.SeriesList;
import stv6.sync.SyncSettings.SyncPage;

public class SyncNewHandler {
	
	/**
	 * Fill a Request with data needed for the NEW request
	 * @param settings
	 * @param r
	 */
	private static void buildRequest(SyncSettings settings, Request r) {
		if (settings.isUsingPassword())
			r.getPostVars().put("p", settings.getPassword());
		
		SeriesList list = Profile.getInstance().getSyncNewSeries();
		if (list.size() > 0) {			
			// build request list
			for (Series s : list) {
				r.getPostVars().put("sn[]", s.getName());
				r.getPostVars().put("si[]", s.getId());
			}			
		}
		List<User> users = Profile.getInstance().getSyncNewUsers();
		if (users.size() > 0) {
			for (User u : users) {
				r.getPostVars().put("un[]", u.getName());
				r.getPostVars().put("ui[]", u.getId());
			}
		}
	}
	
	
	public static boolean handle(SyncSettings settings) throws UnknownHostException, IOException {
		HttpRequestor rq = settings.getRequestorFor(SyncPage.NEW);
		Request r = rq.getRequest();
		buildRequest(settings, r);
		
		// only actually request if necessary
		int minSize = settings.isUsingPassword() ? 1 : 0;
		if (r.getPostVars().size() > minSize) {
			Response resp = rq.request();
			VariableList vars = new VariableList();
			Request.parseUrlEncodedVars(resp.getBody(), vars);
			// TODO consider users exists error w/ --join option 
			if (vars.isSet("error")) {
				// go ahead and stop everything... this is probably
				// a client-side input error
				StaticMessageHandler.getInstance().setBody(
						"Synchronization returned the following error: " +
						vars.getValue("error")
				);
				StaticMessageHandler.getInstance().setRefresh(null);
				return false;
			}			
			
			// success! do what the server tells us ;)
			if (vars.isSet("uo")) {
				// update user ids
				ListVariable oldIds = (ListVariable) vars.get("uo");
				ListVariable newIds = (ListVariable) vars.get("un");
				if (oldIds.size() != newIds.size()) {
					System.out.println("Failed: user id count mismatch] ");
					return true;
				}
				// build the correct data type				
				Profile.getInstance().updateUserIds( buildIdDataList(oldIds, newIds) );
			}
			
			if (vars.isSet("so")) {
				// update series ids
				ListVariable oldIds = (ListVariable) vars.get("so");
				ListVariable newIds = (ListVariable) vars.get("sn");
				if (oldIds.size() != newIds.size()) {
					System.out.println("Failed: series id count mismatch] ");
					return true;
				}
				Profile.getInstance().updateSeriesIds( buildIdDataList(oldIds, newIds) );
			}
		}
		
		// we successfully sync'd; save the date
		Profile.getInstance().updateSyncTime(SyncPage.NEW);
		
		return true;
	}


	private static List<IdUpdateData> buildIdDataList(ListVariable oldIds,
			ListVariable newIds) {
		List<IdUpdateData> list = new ArrayList<IdUpdateData>();
		for (int i=0; i<oldIds.size(); i++) {
			list.add(
				new IdUpdateData(
					Integer.parseInt(oldIds.get(i).value), 
					Integer.parseInt(newIds.get(i).value)
				));
		}
		return list;
	}
}
