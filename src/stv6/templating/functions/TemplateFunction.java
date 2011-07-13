package stv6.templating.functions;

public interface TemplateFunction {
	
	/**
	 * Execute the function on the given value and return the result
	 * @param value
	 * @return
	 */
	public String execute(String value);
	
	/**
	 * @return The name of the function as it should
	 * 	be called in the template
	 */
	public String getFuncName();
}
