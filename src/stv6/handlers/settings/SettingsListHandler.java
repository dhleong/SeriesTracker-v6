package stv6.handlers.settings;

import stv6.Profile;
import stv6.STServer;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.templating.TemplateObject;
import stv6.templating.Templator.Template;

public class SettingsListHandler extends AbstractHandler implements RequestHandler {

	public static final String HANDLED_PAGE = "settings";
	public static final String TEMPLATE_NAME = "settings.list.tpl";

	@Override
	public String getHandledPage() {
		return HANDLED_PAGE;
	}

	@Override
	protected String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	protected boolean wrappedHandle(Request r, Template t) {
		t.putVariable("homeLink", STServer.getHomeLink());
		
		t.putObject(new SettingsLink("settings-manage", "Manage Series", 
				"Add/Remove Series from this profile"));
		if (Profile.getInstance().hasMultipleProfiles())
			t.putObject(new SettingsLink(STServer.PROFILE_SELECT, "Change Profiles", 
				"Switch to a different profile"));
		if (Profile.getInstance().hasMultipleUsers())
			t.putObject(new SettingsLink(STServer.USER_SELECT, "Change User", 
				"Select a different user"));
		t.putObject(new SettingsLink(STServer.STATIC_MESSAGE + "?to=.", "Reload Settings", 
			"Force SeriesTracker to reload everything. If you encounter problems, try this!"));
		return true;
	}
	
	public static final class SettingsLink implements TemplateObject {
		private static final String CLASS_NAME = "link";
		
		private final String url, text, desc;
		private SettingsLink(String url, String text, String desc) {
			this.url = url;
			this.text = text;
			this.desc = desc;
		}
		
		@Override
		public String getClassName() {
			return CLASS_NAME;
		}
		
		public String getDesc() {
			return desc;
		}

		public String getText() {
			return text;
		}
		
		public String getUrl() {
			return url;
		}
	}

}
