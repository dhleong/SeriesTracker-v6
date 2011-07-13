package stv6.handlers.util;

import java.io.FileNotFoundException;
import java.util.List;

import stv6.Profile;
import stv6.STClient;
import stv6.STServer;
import stv6.User;
import stv6.handlers.AbstractHandler;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.http.request.Response;
import stv6.http.request.variables.Cookie;
import stv6.templating.Templator;
import stv6.templating.Templator.Template;

public class UserSelectHandler implements RequestHandler {

	private static final String TEMPLATE_NAME = "item-select.tpl";
	
	@Override
	public String getHandledPage() {
		return STServer.USER_SELECT;
	}

	@Override
	public boolean handle(Request r, Response resp) {
		String templatePath = Profile.getInstance().getTemplatePathFor(r, TEMPLATE_NAME);		

		// load the template
		Templator tr = new Templator();
		Template t;
		try {
			t = tr.newTemplate(templatePath);			
		} catch (FileNotFoundException e) {
			resp.setBody("Specified template file not found.");
			return true;
		}
		
		t.putVariable("title", "User");
		
		List<User> possibles = Profile.getInstance().getPossibleUsers();
		if (r.getGetVars().isSet("i")) {
			// select the user 
			int i = -1;
			try {
				i = Integer.parseInt( r.getGetVars().getValue("i") );
				if (i < 0 || i > possibles.size())
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				t.putVariable("body", "Illegal user selection...");
				t.writeTo(resp);
				return true;
			}
			
			resp.setCookie(new Cookie(STClient.COOKIE_USERID, 
					String.valueOf(possibles.get(i).getId()))
			);
			t.putVariable("body", "Successfully logged in for this session as " + 
					possibles.get(i).getName() + "!");
			t.putVariable("refresh", "1;URL="+STServer.getHomeLink());
		} else {	
			int i=0;
			for (User u : possibles)
				t.putObject(new AbstractHandler.SelectItem(u.getName(), i++));
			
			// we already have a user, so this is optional
			if (r.getUser() != null)
				t.putVariable("homeLink", STServer.getHomeLink());
		}
		
		t.writeTo(resp);
		return true;
	}

}
