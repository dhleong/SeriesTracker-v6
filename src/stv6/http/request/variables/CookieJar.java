package stv6.http.request.variables;

public class CookieJar extends VariableList {
	
	@Override
	public Cookie get(String variableName) {
		return (Cookie) super.get(variableName);
	}
	
	@Override
	public String getValue(String variableName) {
		return super.getValue(variableName);
	}
	
	public void put(Cookie v) {
		super.put(v);
	}

	@Override
	public void put(String key, String value) {
		put( new Cookie(key, value) );
	}
	
	@Override
	public Cookie remove(String variableName) {
		return (Cookie) super.remove(variableName);
	}
}
