package stv6.handlers;

import stv6.Profile;
import stv6.User;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.series.RecentSeries;
import stv6.series.Series;
import stv6.series.TrackedSeries;
import stv6.templating.Templator.Template;

public class IndexHandler extends AbstractHandler implements RequestHandler {
	
	public static final String HANDLED_PAGE = "index";
	public static final String TEMPLATE_NAME = "index.tpl";

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
		
		// fill in some global vars
		t.putVariable("config_link", "./settings");
		
		User usr = r.getUser();
		
		t.putObject(usr);
		
		// fill in some objects
		char lastLetter = '-';
		for (Series s : Profile.getInstance().getAllSeriesAsUser(r.getUser())) {
			if (s.getName().charAt(0) != lastLetter) {
				((TrackedSeries)s).setLetter();
				lastLetter = s.getName().charAt(0);
			}
			
			t.putObject(s);
		}
		
		// TODO get recent for user
		for (Series s : Profile.getInstance().getRecentSeries(r.getUser())) {
		    // wrap to change the class name
		    t.putObject(new RecentSeries((TrackedSeries) s));
		}
		
		return true;
	}

}
