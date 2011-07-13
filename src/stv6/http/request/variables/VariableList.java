package stv6.http.request.variables;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import stv6.templating.TemplateObject;

/**
 * A list of variables, not to be confused with ListVariable.
 * Actually, this is more like a table of variables, but whatever
 * @author dhleong
 *
 */
public class VariableList implements Iterable<Variable>, TemplateObject {
	private static final String CLASS_NAME = "getvars";
	
	private HashMap<String, Variable> list;
	
	public VariableList() {
		list = new HashMap<String, Variable>();
	}
	
	/**
	 * Make sure there's a ListVariable in the map for
	 * 	the variable with the given name
	 * 
	 * @param variableName
	 */
	private void ensureListFor(String variableName) {
		if (!list.containsKey(variableName))
			list.put(variableName, new ListVariable(variableName));
	}
		
	public Variable get(String variableName) {
		return list.get(variableName);
	}
	
	public String getValue(String variableName) {
		if (isSet(variableName))
			return list.get(variableName).value;
		
		return null;
	}

	/**
	 * This is ugly, but what can you do?
	 * @return
	 */
	public int length() {
		int len = 0;
		for (Variable v : this)
			len += 1 + v.length();
		// we add one above to each for the "&"
		//  and subtract one below because there isn't
		/// a trailing "&"
		return len-1;
	}
	
	public boolean isSet(String variableName) {
		return list.containsKey(variableName);
	}
	
	public Iterator<Variable> iterator() {
		return list.values().iterator();
	}

	public void put(Variable v) {
		if (v.key.endsWith("[]")) {
			v = new Variable( v.key.substring(0, v.key.length()-2), v.value );
			ensureListFor(v.key);
			ListVariable l = (ListVariable) list.get(v.key);
			l.add(v);
		} else {
			list.put(v.key, v);
		}
	}

	public void put(String key, String value) {
		put( new Variable(key, value) );
	}

	public void put(String key, int value) {
		put(key, String.valueOf(value));
	}

	public void put(String key, long value) {
		put(key, String.valueOf(value));
	}
	
	public Variable remove(String variableName) {
		return list.remove(variableName);
	}

	public int size() {
		return list.size();
	}

	public void writeTo(PrintWriter output) {
		if (size() == 0)
			return;
		
		int i=0;
		for (Variable v : this) {
			v.writeTo(output);
			if (++i < size())
				output.append('&');
		}
		
		output.flush();
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

}
