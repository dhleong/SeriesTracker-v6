package stv6.handlers.util;

import java.io.File;

import stv6.Profile;
import stv6.STServer;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.templating.Templator.Template;

public class ProfileSelectHandler extends AbstractHandler {	

	private static final String TEMPLATE_NAME = "item-select.tpl";

	@Override
	protected String getTemplatePath() {
		return TEMPLATE_NAME;
	}

	@Override
	protected boolean wrappedHandle(Request r, Template t) {
		t.putVariable("title", "Profile");
		
		File[] possibles = Profile.getInstance().getPossibleProfiles();
		if (r.getGetVars().isSet("i")) {
			// select the profile
			int i = -1;
			try {
				i = Integer.parseInt( r.getGetVars().getValue("i") );
				if (i < 0 || i > possibles.length)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				t.putVariable("body", "Illegal profile selection...");
				return true;
			}
			
			t.putVariable("body", "Profile loaded successfully. Please wait while we reload settings");
			t.putVariable("refresh", "1;URL="+STServer.getHomeLink()); 
			Profile.getInstance().loadFromFile(possibles[i]);
			Profile.getInstance().reload();
		} else if (possibles == null) {
			t.putVariable("body", "What the heck...? ");
		} else {	
			String fname;
			for (int i=0; i<possibles.length; i++) {
				fname = possibles[i].getName();
				int pos = fname.lastIndexOf(File.separatorChar);
				if (pos > -1)
					fname = fname.substring(pos);
				t.putObject(new SelectItem(fname, i));
			}
			// we already have a profile, so this is optional
			if (Profile.getInstance().isSelected())
				t.putVariable("homeLink", STServer.getHomeLink());
		}
		
		return true;
	}

	@Override
	public String getHandledPage() {
		return STServer.PROFILE_SELECT;
	}
}
