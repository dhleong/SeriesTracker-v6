package stv6.templating;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TemplateReader {
    private File sourceFile;
	private BufferedReader file;
	private StringBuilder lastLine = null;
	private boolean useLast = false;
	
	/** 
	 * For extending this class to not use the filesystem
	 */
	protected TemplateReader() {}
	
	public TemplateReader(File source) throws FileNotFoundException {
	    sourceFile = source;
		file = new BufferedReader(
			new FileReader(source)
		);
	}
	
	/**
	 * 
	 * @return The File from which we are reading this template
	 */
	public File getSource() {
	    return sourceFile;
	}

	/**
	 * @return The last line returned by {@link #next()}
	 */
	public StringBuilder last() {
		return lastLine;
	}
	
	/**
	 * Instructs the next call to {@link #next()} to return
	 * 	the same line it just returned. Cannot be used more than
	 * 	once in succession
	 */
	public void rewind() {
		useLast = true;
	}
	
	/**
	 * @return The next line in the file as a StringBuilder,
	 * 	or null on error
	 * @throws EOFException if it reaches the EOF
	 */
	public StringBuilder next() throws EOFException {
		if (useLast) {
			useLast = false;
			return lastLine;
		}
		
		String raw;
		if ((raw = readLine()) == null)
			throw new EOFException();
		
		// should be more efficient with replaces, etc.
		lastLine = new StringBuilder( raw.trim() );
		return lastLine;
	}
	
	/**
	 * You know, in case you want to override
	 * 	with your own somehow
	 */
	protected String readLine() {
		try {
			return file.readLine();
		} catch (IOException e) {
			return null;
		}
	}

}
