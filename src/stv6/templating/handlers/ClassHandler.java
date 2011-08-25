package stv6.templating.handlers;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import stv6.templating.TemplateObject;
import stv6.templating.TemplateReader;
import stv6.templating.Templator.Template;
import stv6.templating.environment.Environment;

/**
 * Note that it sets a special variable INDEX that's available
 * 	as $INDEX for each class instance, if you need some unique identifier
 * 	that you don't get otherwise. It's 0-indexed as you would expect!
 * @author dhleong
 *
 */
public class ClassHandler extends AbstractHandler implements TemplateCodeHandler {
	
	public static final String HANDLED_CMD = "class";

	@Override
	public String getHandledCommand() {
		return HANDLED_CMD;
	}

	@Override
	public void handle(StringBuilder line, TemplateReader tpl,
			Environment env, Appendable out) throws IOException {		
		// get the class name
		String klass = getArgs(line);
		
		// read in the class
		ClassReader rdr = new ClassReader();			
		while (true) {
			line = tpl.next();
			
			if (line == null || (isCodeLine(line) && getCmd(line).equals("endclass")))
				break;
			
			rdr.add( line.toString() );
		}
		
		// format and print objects!
		Environment oenv; int i = 0;
		for (TemplateObject o : env.getObjectsFor(klass)) {
			rdr.rewind();
			oenv = env.getEnvironmentFor(o);	
			oenv.setValue("FIRST", (i == 0) ? "1" : null);
			oenv.setValue("NOTFIRST", (i != 0) ? "1" : null);
			oenv.setValue("INDEX", String.valueOf(i++));			
			Template.doTemplateLoop(rdr, oenv, out);
		}
	}
	
	private static class ClassReader extends TemplateReader {
		private final LinkedList<String> lines;
		private Iterator<String> iter = null;
		
		private ClassReader() {
			lines = new LinkedList<String>();
		}
		
		public void add(String line) {
			lines.add(line);
		}
		
		@Override
		protected String readLine() {
			if (iter == null)
				iter = lines.iterator();
			
			try {
				return iter.next();
			} catch (NoSuchElementException e) {
				return null;
			}
		}
		
		@Override
        public void rewind() {
			iter = null;			
		}
	}

}
