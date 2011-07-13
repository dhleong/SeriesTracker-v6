package stv6.sync;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import stv6.Profile;
import stv6.User;
import stv6.http.HttpRequestor;
import stv6.http.HttpResponseCallback;
import stv6.http.request.Request;
import stv6.http.request.Response;
import stv6.http.request.variables.ListVariable;
import stv6.http.request.variables.VariableList;
import stv6.sync.SyncSettings.SyncPage;

public class SyncTrackHandler {
	
	/**
	 * Fill a Request with data needed for the request
	 * @param settings
	 * @param r
	 */
	private static void buildRequest(SyncSettings settings, Request r) {
		if (settings.isUsingPassword())
			r.getPostVars().put("p", settings.getPassword());
		
		// last sync
		r.getPostVars().put("ls", Profile.getInstance().getLastSyncFor(SyncPage.TRACK));
		
		// managed users, so we don't pull unnecessary tracking data
		for (User u : Profile.getInstance().getPossibleUsers())
			r.getPostVars().put("mu[]", u.getId());		
		
		// our new syncs
		List<TrackData> data = Profile.getInstance().getSyncNewTracks();
		if (data.size() > 0) {
			for (TrackData d : data) {
				r.getPostVars().put("s[]", d.seriesId);
				r.getPostVars().put("u[]", d.userId);
				r.getPostVars().put("e[]", d.episode);
				r.getPostVars().put("l[]", d.lastView);
			}
		}
	}
	
	public static void handle(SyncSettings settings) throws UnknownHostException, IOException {
		HttpRequestor rq = settings.getRequestorFor(SyncPage.TRACK);
		Request r = rq.getRequest();
		buildRequest(settings, r);
		
		// we don't need to handle it immediately
		rq.request(new HttpResponseCallback() {

			@Override
			public void callback(Response r) {
				VariableList vars = new VariableList();
				Request.parseUrlEncodedVars(r.getBody(), vars);
				/*
				System.out.println(r.getBody());
				int a = 1;
				if (a == 1) return;
				*/
				
				if (vars.isSet("error")) {
					System.err.println("\nTRACK: " + vars.getValue("error"));
					return;
				}
				
				if (!(vars.isSet("s") && vars.isSet("u") && vars.isSet("e") && vars.isSet("l"))) {
					if (vars.isSet("n")) {
						// there's just nothing to do
						Profile.getInstance().updateSyncTime(SyncPage.TRACK);
					} else {
						System.err.println("\nTRACK: Invalid server response (incomplete)");
						System.err.println("Full response: " + r.getBody());
					}
					return;
				}

				ListVariable seriesIds = (ListVariable) vars.get("s");
				ListVariable userIds = (ListVariable) vars.get("u");
				ListVariable episodes = (ListVariable) vars.get("e");
				ListVariable lastViews = (ListVariable) vars.get("l");
				if (!(seriesIds.size() == userIds.size() && 
						userIds.size() == episodes.size() &&
						episodes.size() == lastViews.size())) {
					System.err.println("\nTRACK: Invalid server response (size mismatch)");
				}
				
				// create the array
				TrackData[] data = new TrackData[ seriesIds.size() ];
				int seriesId, userId, episode;
				long lastView;
				for (int i=0; i<seriesIds.size(); i++) {
					seriesId = Integer.parseInt(seriesIds.get(i).value);
					userId = Integer.parseInt(userIds.get(i).value);
					episode = Integer.parseInt(episodes.get(i).value);
					lastView = Long.parseLong(lastViews.get(i).value);
					
					data[i] = new TrackData(seriesId, userId, episode, lastView);
				}
				
				Profile.getInstance().updateSeriesTracking(data);
				Profile.getInstance().updateSyncTime(SyncPage.TRACK);
			}
			
		}); 
	}
}
