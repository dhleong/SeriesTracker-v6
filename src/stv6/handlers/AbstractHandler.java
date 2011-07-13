package stv6.handlers;

import java.io.FileNotFoundException;

import stv6.Profile;
import stv6.http.request.Request;
import stv6.http.request.RequestHandler;
import stv6.http.request.Response;
import stv6.templating.TemplateObject;
import stv6.templating.Templator;
import stv6.templating.Templator.Template;

/**
 * Make it easier/faster to implement new handlers 
 * @author dhleong
 *
 */
public abstract class AbstractHandler implements RequestHandler {

	protected abstract String getTemplatePath();

	@Override
	public boolean handle(Request r, Response resp) {
		String templatePath = Profile.getInstance().getTemplatePathFor(r, getTemplatePath());		

		// load the template
		Templator tr = new Templator();
		Template t;
		try {
			t = tr.newTemplate(templatePath);
			t.putObject(r.getGetVars());
		} catch (FileNotFoundException e) {
			resp.setBody("Specified template file not found.");
			return true;
		}
		
		boolean handledResult = wrappedHandle(r, t);
		if (handledResult)
			t.writeTo(resp);
		
		return handledResult;
	}
	
	/**
	 * AbstractHandler will handle loading the template and only call this if
	 * 	successsful; it will also write to the response for you if this returns true
	 * 
	 * @param r
	 * @param resp
	 * @param t
	 * @return
	 */
	protected abstract boolean wrappedHandle(Request r, Template t);

	public static class SelectItem implements TemplateObject {

		private final String name, link;
		
		public SelectItem(String name, int index) {
			this.name = name;			
			link = "?i=" + index;
		}

		@Override
		public String getClassName() {
			return "select_item";
		}
		
		public String getLink() {
			return link;
		}
		
		public String getName() {
			return name;
		}
	}
}
