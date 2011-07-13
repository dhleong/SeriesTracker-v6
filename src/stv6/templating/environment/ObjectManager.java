package stv6.templating.environment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import stv6.templating.TemplateObject;

/**
 * Manages Objects (Instances of a Template class)
 * 
 * @author dhleong
 *
 */
public class ObjectManager {
	public HashMap<String, LinkedList<TemplateObject>> objects;
	
	public ObjectManager() {
		objects = new HashMap<String, LinkedList<TemplateObject>>();
	}
	
	public void put(TemplateObject obj) {
		getList(obj.getClassName()).add(obj);
	}
	
	public List<TemplateObject> getObjectsFor(String klass) {
		return getList(klass);
	}
	
	/**
	 * Get a reference to the list for objects of class klass. If
	 * 	it's not in the map, create it
	 * 
	 * @param className
	 * @return
	 */
	private LinkedList<TemplateObject> getList(String klass) {
		LinkedList<TemplateObject> ret; 
		if (objects.containsKey(klass)) {
			ret = objects.get(klass);
		} else {
			ret = new LinkedList<TemplateObject>();
			objects.put(klass, ret);
		}
		
		return ret;
	}
}
