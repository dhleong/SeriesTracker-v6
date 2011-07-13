package stv6.handlers;

import stv6.Profile;
import stv6.STServer;
import stv6.episodes.BasicEpisode;
import stv6.episodes.SeriesEpisode;
import stv6.handlers.settings.SeriesManageHandler;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.series.TrackedSeries;
import stv6.templating.Templator.Template;

public class BrowseHandler extends AbstractHandler implements RequestHandler {

	public static final String HANDLED_PAGE = "browse";
	public static final String TEMPLATE_NAME = "browse.tpl";

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
			t.putVariable("errorLink", SeriesManageHandler.HANDLED_PAGE);
			if (s != null)
				t.putObject(s);
			return true;
		}
		
		t.putObject(s);
				
		for (BasicEpisode e : s.getEpisodes())
			t.putObject( new SeriesEpisode(e, s.getId()) );
		
		return true;
	}

}
