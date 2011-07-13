package stv6.handlers.settings;

import stv6.Profile;
import stv6.STServer;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.series.BasicSeries;
import stv6.series.Series;
import stv6.series.SeriesList;
import stv6.templating.Templator.Template;

public class SeriesManageHandler extends AbstractHandler implements
		RequestHandler {
	
	public static final String HANDLED_PAGE = "settings-manage";
	private static final String TEMPLATE_NAME = "settings.manage.tpl";

	@Override
	protected String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	protected boolean wrappedHandle(Request r, Template t) {
		
		SeriesList availableSeries = Profile.getInstance().getAvailableSeries();
		SeriesList profileSeries = Profile.getInstance().getSeries();
		Profile.getInstance().getAllSeries(availableSeries, false);
		
		t.putVariable("available", availableSeries.size());
		t.putVariable("formAction", "settings-manage-save");
		t.putVariable("homeLink", STServer.getHomeLink());
				
		for (Series s : availableSeries) {
			t.putObject(new AvailableSeries(s,
					s.getId() > 0,
					profileSeries.contains(s.getName())
			));
		}	
		
		return true;
	}

	@Override
	public String getHandledPage() {
		return HANDLED_PAGE;
	}
	
	public class AvailableSeries extends BasicSeries {
		private static final String CLASS_NAME = "availableSeries";
		private final boolean exists, inProfile;

		private AvailableSeries(Series s, boolean exists, boolean inProfile) {
			super(s.getId(), s.getName());
			this.exists = exists;
			this.inProfile = inProfile;
			
			if (s instanceof BasicSeries) {				
				BasicSeries t = ((BasicSeries)s);				
				this.manageify( t.getLocalPath(), null );
			}
		}
		
		public boolean getExists() {
			return exists;
		}
		
		public boolean isInProfile() {
			return inProfile;
		}

		@Override
		public String getClassName() {
			return CLASS_NAME;
		}
	}

}
