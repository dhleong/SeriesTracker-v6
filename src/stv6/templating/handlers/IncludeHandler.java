package stv6.templating.handlers;

import java.io.File;
import java.io.IOException;

import stv6.templating.TemplateReader;
import stv6.templating.Templator.Template;
import stv6.templating.environment.Environment;

public class IncludeHandler extends AbstractHandler implements TemplateCodeHandler {

    public static final String HANDLED_CMD = "include";

    @Override
    public String getHandledCommand() {
        return HANDLED_CMD;
    }

    @Override
    public void handle(StringBuilder line, TemplateReader tpl, Environment env,
            Appendable out) throws IOException {
        // get the file name
        String filename = getArgs(line);
        File tmpFile = tpl.getSource();
        File incFile = new File(tmpFile.getParentFile(), filename);
        if (!incFile.exists()) {
            System.err.println(tmpFile.getName() +
                    "] Couldn't find include file: " + 
                    incFile.getAbsolutePath());
            return;
        }
        
        TemplateReader incTemp = new TemplateReader(incFile);
        Template.doTemplateLoop(incTemp, env, out);
    }

}
