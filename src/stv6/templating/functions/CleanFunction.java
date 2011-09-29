package stv6.templating.functions;

/**
 * The "clean" function cleans up a string (especially a
 * 	filename) for displaying. For now, we just replace
 * 	underscores with a space. 
 * 
 * I really need to redo this templating system to be more
 * 	flexible (like Django templates... I like those.)
 * @author dhleong
 *
 */
public class CleanFunction implements TemplateFunction {

	@Override
	public String execute(String value) {
		String ret = value.replaceAll("[_]+", " ");
		return ret;
	}

	@Override
	public String getFuncName() {
		return "clean";
	}

}
