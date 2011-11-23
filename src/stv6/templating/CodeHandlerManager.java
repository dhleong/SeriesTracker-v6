package stv6.templating;

import java.io.IOException;
import java.util.HashMap;

import stv6.templating.environment.Environment;
import stv6.templating.handlers.AbstractHandler;
import stv6.templating.handlers.ClassHandler;
import stv6.templating.handlers.IfHandler;
import stv6.templating.handlers.IncludeHandler;
import stv6.templating.handlers.TemplateCodeHandler;

public class CodeHandlerManager implements TemplateCodeHandler {
	
	private static CodeHandlerManager instance_ = new CodeHandlerManager();

	static {
		instance_.registerHandler(new IfHandler());
		instance_.registerHandler(new ClassHandler());
		instance_.registerHandler(new IncludeHandler());
	}
	
	private final HashMap<String, TemplateCodeHandler> handlers;
	
	private CodeHandlerManager() {
		handlers = new HashMap<String, TemplateCodeHandler>();
	}
	
	public static CodeHandlerManager getInstance() {
		return instance_;
	}

	private void registerHandler(TemplateCodeHandler handler) {
		handlers.put(handler.getHandledCommand(), handler);
	}

	@Override
    public void handle(StringBuilder line, TemplateReader tpl, 
			Environment env, Appendable out) throws IOException {
		if (AbstractHandler.isCodeLine(line)) {
			String cmd = AbstractHandler.getCmd(line);
			if (handlers.containsKey(cmd)) {
				handlers.get(cmd).handle(line, tpl, env, out);
				return;
			}
		} 
		
		// just straight html				
		AbstractHandler.defaultAction(line, env, out);	
	}

	/**
	 * Does nothing; just here so we can implement TemplateCodeHandler
	 */
	@Override
	public String getHandledCommand() {
		return null;
	}
}
