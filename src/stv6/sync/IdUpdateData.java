package stv6.sync;

/**
 * Represents an ID update. Very simple data packaging
 *  
 * @author dhleong
 *
 */
public class IdUpdateData {
	public final int oldId, newId;
	
	public IdUpdateData(int oldId, int newId) {
		this.oldId = oldId;
		this.newId = newId;
	}
}
