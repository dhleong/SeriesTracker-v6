package stv6.templating.functions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class EncodeFunction implements TemplateFunction {

	private static final String FUNC_NAME = "encode";
	
	@Override
	public String execute(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// oh well....?
			e.printStackTrace();
			return value;
		}
	}

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

}
