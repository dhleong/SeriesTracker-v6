package stv6.templating.handlers;

import java.io.EOFException;
import java.io.IOException;

import stv6.http.request.variables.Variable;
import stv6.templating.CodeHandlerManager;
import stv6.templating.TemplateReader;
import stv6.templating.environment.Environment;

public class IfHandler extends AbstractHandler implements TemplateCodeHandler {
	
	public static final String HANDLED_CMD = "if";

	@Override
	public String getHandledCommand() {		
		return HANDLED_CMD;
	}
	
	private boolean isBlockEnder(StringBuilder line) {
		String cmd = getCmd(line);
		return cmd.equals("else") || cmd.equals("elseif") || cmd.equals("endif");
	}

	@Override
	public void handle(StringBuilder line, TemplateReader tpl, Environment env, 
			Appendable out) throws IOException {
		boolean done = false;
		while (!done) {
			if (evaluateExpression(line, env)) {
				// success! read through the block, evaluating as necessary				
				while ((line = tpl.next()) != null) {
					if (isCodeLine(line)) {
						if (isBlockEnder(line))
							break;
						else
							CodeHandlerManager.getInstance().handle(line, tpl, env, out);
					} else 
						defaultAction(line, env, out);
				}
				
				// skip past all remaining if/elseif/else
				skipToEnd(line, tpl);
				done = true;
			} else {				
				done = skipToNext(line, tpl);
				line = tpl.last(); // make sure we're looking in the right place
			}
			
			if (done)
				break;
		}
	}
	
	private static void skipToEnd(StringBuilder line, TemplateReader tpl) throws EOFException {		
		while ( line != null ) {			
			if (isCodeLine(line)) {
				String cmd = getCmd(line);
				if (cmd.equals("if")) {
					// skip nested ifs					
					skipToEnd(tpl.next(), tpl);
				} else if (cmd.equals("endif")) {
					// FIXME: Why did I think I needed this?
					//line = tpl.next();
					return;
				}
			}
			
			line = tpl.next();
		}
	}
	
	/**
	 * Skip to next block, or end of the if statement
	 * @param line
	 * @param tpl
	 * @return True if we reached the end of the statement, else false
	 * @throws EOFException 
	 */
	private static boolean skipToNext(StringBuilder line, TemplateReader tpl) throws EOFException {
		while((line = tpl.next()) != null) {	
			if (isCodeLine(line)) {
				String cmd = getCmd(line);
				if (cmd.equals("if")) {
					// nested if
					skipToEnd(tpl.next(), tpl);
				} else if (cmd.equals("endif")) {
					return true;
				} else if (cmd.equals("elseif") || cmd.equals("else"))
					return false;
			}
		}
		
		return true;
	}

	private boolean evaluateExpression(StringBuilder line, Environment env) {
		String args = getArgs(line);
		if (args.charAt(0) == '$') {
	
			Variable v = getVariableAt(env, args, 0);
			
			if (args.length() > v.key.length()) {
				// check for boolean stuff
				int keyEnd = v.key.length();
				if (args.charAt(keyEnd+1) == '=') {
					if (args.charAt(keyEnd+2) == '$') {
						Variable v2 = getVariableAt(env, args, keyEnd+2);
						return args.charAt(keyEnd) == '=' ? v.valueEquals(v2) : !v.valueEquals(v2);
					} else {
						// string literal comparison
						return args.charAt(keyEnd) == '=' ? 
								v.value.equals(args.substring(keyEnd+2)) : 
									!v.value.equals(args.substring(keyEnd+2));
					}
				}
			} 
			
			// just checking if the value is available
			return v.value != null && !v.value.equals("false");
		} else if (args.equals("else"))
			return true;
		
		return false;
	}

}
