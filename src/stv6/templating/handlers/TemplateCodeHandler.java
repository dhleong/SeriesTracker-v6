package stv6.templating.handlers;

import java.io.IOException;

import stv6.templating.TemplateReader;
import stv6.templating.environment.Environment;


public interface TemplateCodeHandler {
	public String getHandledCommand();

	public void handle(StringBuilder line, TemplateReader tpl, Environment env,
			Appendable out) throws IOException;
}
