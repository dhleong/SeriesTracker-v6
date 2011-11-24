package stv6.templating.functions;

/**
 * The "first" function just gets the first character of
 *  the string. Yay for very specific-use functions...
 *  
 * @author dhleong
 *
 */
public class FirstFunction implements TemplateFunction {

	@Override
	public String execute(String value) {
		return String.valueOf(value.charAt(0));
	}

	@Override
	public String getFuncName() {
		return "first";
	}

}
