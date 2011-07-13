package stv6.handlers;


import stv6.Profile;
import stv6.STServer;
import stv6.episodes.SeriesEpisode;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.series.TrackedSeries;
import stv6.templating.Templator.Template;

public class ViewHandler extends AbstractHandler implements RequestHandler {
	
	public static final String HANDLED_PAGE = "view";
	public static final String TEMPLATE_NAME = "view.tpl";

	@Override
	public String getHandledPage() {
		return HANDLED_PAGE;
	}
	
	@Override
	public String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	public boolean wrappedHandle(Request r, Template t) {
		
		t.putVariable("homeLink", STServer.getHomeLink());
		
		TrackedSeries s = Profile.getInstance().getSeriesFromRequest(r);
		if (s == null || s.size() == 0) {
			t.putVariable("error", "Invalid series/Could not load");
			return true;
		}
				
		SeriesEpisode e = s.getEpisodeFromRequest(r);
		if (e == null) {			
			t.putVariable("error", "Invalid episode");
			return true;
		}
				
		// update the position
		s.update(e.getId(), Profile.getNowSeconds());
		
		t.putObject(s); // add series
		t.putObject(e); // add episode
	
		// we want to save!
		if (r.getGetVars().isSet("save")) {
			t.putVariable("saved", "true");
			
			Profile.getInstance().updateSeriesTracking(s, r.getUser());
		}
		
		if (e.getId() == -1) {
			// quit early; we're just resetting, so we don't need fanciness
			t.putVariable("reset", "true");
			return true;
		}
		
		if (r.isLocal()) {
			// it's a local request... do things a bit differently
			t.putVariable("local", "true");
			
			if (!r.getGetVars().isSet("local")) {
				// give the link to load
				String link = r.path+"&amp;local=1";
				t.putVariable("localLoadLink", link);
				
			} else {
				// load it!
				t.putVariable("localLoading", "true");
				
				String filePath = s.getLocalPathFor(e);
				if (filePath == null || !loadLocalFile(filePath)) {
					// if the next if doesn't get hit, then
					//  we couldn't find the file
					t.putVariable("localLoadingError", "true");
					
					if (filePath != null) {
						// couldn't start player
						t.putVariable("localLoadingErrorPlayer", "true");
						t.putVariable("localLoadPath", filePath);
						t.putVariable("player", Profile.getInstance().getLocalPlayer());
					}
				}	
			}
		}
		
		return true;
	}
	
	private boolean loadLocalFile(String path) {
		try {
			Runtime.getRuntime().exec(
					new String[]{Profile.getInstance().getLocalPlayer(),path});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
