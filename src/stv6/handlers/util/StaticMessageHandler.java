package stv6.handlers.util;

import stv6.Profile;
import stv6.STServer;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.templating.Templator.Template;

public class StaticMessageHandler extends AbstractHandler implements RequestHandler {
	
	private static final StaticMessageHandler instance_ = new StaticMessageHandler();

	private static final String TEMPLATE_NAME = "settings.generic.tpl";
	
	private static final String defaultBody = "Reloading settings. Please wait...";
	private static final String defaultRefresh = "2";
	
	private String body = defaultBody, refresh = defaultRefresh;
	
	private StaticMessageHandler() {
		super();
	}
	
	@Override
	protected String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	protected boolean wrappedHandle(Request r, Template t) {
		// MAYBE I want to customize the title.... MAYBE. for now this is fine
		t.putVariable("title", (body.equals(defaultBody)) ? "ST - Reloading Settings": "SeriesTracker");
		t.putVariable("body", body);
		t.putVariable("static", "true"); // it could be useful, say, in an ajax version...
		
		if (r.getGetVars().isSet("to")) {
			// we're requesting a reload
			String to = r.getGetVars().getValue("to");
			if (to.contains("?"))
				to += "&dor=" + System.currentTimeMillis();
			else to += "?dor=" + System.currentTimeMillis();
			t.putVariable("refresh", "0;URL=" + r.getGetVars().getValue("to"));
			Profile.getInstance().reload();
		} else
			t.putVariable("refresh", refresh);
		return true;
	}

	@Override
	public String getHandledPage() {
		return STServer.STATIC_MESSAGE;
	}
	
	/**
	 * Reset to default message (Reloading)
	 */
	public void setBody() {
		body = defaultBody;
	}
	
	public void setBody(String msg) {
		body = msg;
	}
	
	/**
	 * Reset to default refresh (2 seconds)
	 */
	public void setRefresh() {
		refresh = defaultRefresh;
	}
	
	/**
	 * Pass null to disable refreshing 
	 * @param msg
	 */
	public void setRefresh(String msg) {
		refresh = msg;
	}
	
	public static StaticMessageHandler getInstance() {
		return instance_;
	}

}
