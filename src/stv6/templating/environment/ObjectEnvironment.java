package stv6.templating.environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import stv6.http.request.variables.VariableList;
import stv6.templating.TemplateObject;

/**
 * The Environment when iterating over a class
 * 
 * @author dhleong
 *
 */
public class ObjectEnvironment extends GlobalEnvironment implements Environment {
	
	private static final String[] GET_METHOD_NAMES = new String[] {"getValue","get"};
	
	private final TemplateObject obj;

	public ObjectEnvironment(VariableList globalVars, ObjectManager objects,
			TemplateObject o) {
		super(globalVars, objects);
		
		this.obj = o;
	}

	@Override
	public String getValue(String name) {		
		Method m = getObjMethod(name);
		if (m == null)
			return super.getValue(name);
		
		Object value = invoke(m, name);
		if (value == null)
			return null;
		else if (value instanceof String)
			return (String) value;
		else return String.valueOf(value);
	}

	@Override
	public boolean isSet(String name) {
		Method m = getObjMethod(name);
		if (m == null)
			return super.isSet(name);
		
		return true;
		/*
		Object value = invoke(m);
		return (value != null && (value instanceof String || value == Boolean.TRUE));
		*/
	}
	
	private Method getObjMethod(String name) {
		if (!("size".equals(name) || name.startsWith("is") || name.startsWith("has"))) {
			name = String.format("get%c%s", 
					Character.toUpperCase( name.charAt(0) ), name.substring(1)
			);
		}
		
		try {
			return obj.getClass().getMethod(name);
		} catch (SecurityException e) {
			return null;
		} catch (NoSuchMethodException e) {
			// see if there's a generic get(String method)
			for (String mName : GET_METHOD_NAMES) {
				try {
					return obj.getClass().getMethod(mName, String.class);
				} catch (SecurityException e1) {
					return null;
				} catch (NoSuchMethodException e1) {
					return null;
				}
			}
			return null;
		}
	}
	
	private Object invoke(Method m, String... args) {
		try {
			return m.invoke(obj);			 
		} catch (IllegalArgumentException e) {
			try {
				// attempt to invoke with arg
				return m.invoke(obj, args[0]);
			} catch (IllegalArgumentException e1) {
			} catch (IllegalAccessException e1) {
			} catch (InvocationTargetException e1) {
			}
			return null;
		} catch (IllegalAccessException e) {
			return null;
		} catch (InvocationTargetException e) {
			return null;
		}
	}

}
