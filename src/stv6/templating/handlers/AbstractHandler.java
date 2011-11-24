package stv6.templating.handlers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import stv6.http.request.variables.Variable;
import stv6.templating.TemplateObject;
import stv6.templating.environment.Environment;
import stv6.templating.functions.CleanFunction;
import stv6.templating.functions.EncodeFunction;
import stv6.templating.functions.FirstFunction;
import stv6.templating.functions.TemplateFunction;

public abstract class AbstractHandler {
	
	private static final HashMap<String, TemplateFunction> functions = 
		new HashMap<String, TemplateFunction>();
	static {
		// I should really make a manager for this instead of a HashMap, but
		//	whatever... I've only a few functions right now, anyway
		functions.put("encode", new EncodeFunction());
		functions.put("clean", new CleanFunction());
		functions.put("first", new FirstFunction());
	}

	public static void defaultAction(StringBuilder line, Environment env, Appendable out) throws IOException {
		parseVars( env, line );
		
		out.append(line);
		out.append('\n');	
	}
	
	public static String getCmd(StringBuilder line) {
		int cmdEnd = line.indexOf(":");
		if (cmdEnd > -1) 
			return line.substring(2, cmdEnd).trim();
			
		return stripLine(line);
	}
		
	/**
	 * 
	 * @param line
	 */
	public static String getArgs(StringBuilder line) {
		int i = line.indexOf(":");
		if (i > -1)
			return line.substring(i+1, line.length()-1).trim();
		
		return stripLine(line);
	}
	
	/**
	 * @param env
	 * @param line
	 * @param start
	 * @return The variable starting at position "start" in the line, 
	 * 	from the given Environment; with the key as the full variable name
	 * 	as it appeared in the template, and the value as, well, the value
	 */
	public static Variable getVariableAt(Environment env, String line, int dollarPos) {
		int len = line.length();
		String name, varKey = null, value = null;
		
		// check for class-var syntax: ${classname:var}
		//	or function-var syntax: ${functionname>var)}
		if (line.charAt(dollarPos+1) == '{') {
			// figure out the class/name
			int start = dollarPos+2;
			int colonPos = line.indexOf(":", start);
			int funcPos = line.indexOf(">", start);
			if (colonPos < 0 && funcPos < 0) {
				//dollarPos++;
				//continue;
				return null;
			} 
			int endPos = line.indexOf("}", colonPos);
			if (endPos < 0) {
				//dollarPos = endPos+1;
				//continue;
				return null;
			}
			
			String funcName = null, className = null;
			if (funcPos > -1 && funcPos < endPos) {
				funcName = line.substring(start, funcPos);
				start = funcPos+1;
			}
			if (colonPos > -1) {
				className = line.substring(start, colonPos);
				start = colonPos+1;
			}
			
			name = line.substring(start, endPos);

			// voila
			varKey = line.substring(dollarPos, endPos+1);
			
			if (className != null) {
				// carefully load the obj/environment/value
				List<TemplateObject> objs = env.getObjectsFor(className);
				if (objs == null || objs.size() == 0) {
					return new Variable(varKey, null);
				}
				
				value = env.getEnvironmentFor(objs.get(0)).getValue(name);
			} else if (env.isSet(name)) {
				// standard var version
				value = env.getValue(name);
			}
			
			if (value == null)
				return new Variable(varKey, null); 
			
			// check for a function
			if (funcName != null && functions.containsKey(funcName)) 
				value = functions.get(funcName).execute(value);
							
			
		} else {
			// it's just a regular var
			int i;
			for (i = dollarPos+1;
				i < len && isIdentifier(line.charAt(i));
				i++);
			
			name = line.substring(dollarPos+1, i);
			if (env.isSet(name)) {
				value = env.getValue(name);
				//if (value == null)
				//	value = ""; // maybe we want to just skip?
				
				varKey = line.substring(dollarPos, i);
				//len = line.length(); // it might have changed!
				//dollarPos += value.length();
			} //else {
				//dollarPos = i;
			//}
			else return new Variable(line.substring(dollarPos, i), null);
		
		}
		
		return new Variable(varKey, value);
	}
	
	public static boolean isCodeLine(CharSequence line) {			
		return (line != null && line.length() > 0 && line.charAt(0) == '<' && line.charAt(1) == '!');
	}

	public static boolean isIdentifier(char ch) {
		return Character.isLetterOrDigit(ch) || ch == '_';
	}
	
	public static void parseVars( Environment env, StringBuilder line ) {
		int dollarPos = 0;
		Variable v;
		while ( (dollarPos = line.indexOf("$", dollarPos)) > -1 ) {
			v = getVariableAt(env, line.toString(), dollarPos);
			if (v == null)
				dollarPos++;
			else if (v.value == null)
				dollarPos += v.key.length();
			else {
				line.replace(dollarPos, dollarPos+v.key.length(), v.value);
				dollarPos += v.value.length();
			}
		}
	}
	
	/**
	 * @param line
	 * @return The line without brackets
	 */
	private static String stripLine(StringBuilder line) {
		return line.substring(2, line.length()-1).trim();
	}
}
