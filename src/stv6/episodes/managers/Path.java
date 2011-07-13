package stv6.episodes.managers;

public class Path {
	protected final String pathId, localDirectory;
	
	public Path(String pathId, String localDirectory) {
		this.pathId = pathId;
		this.localDirectory = localDirectory;
	}

	/**
	 * Maybe you don't need an ID...
	 * 
	 * @param localDirectory
	 */
	public Path(String localDirectory) {
		this("", localDirectory);
	}
}
