package stv6.templating.environment;

import java.util.List;

import stv6.http.request.variables.VariableList;
import stv6.templating.TemplateObject;

public class GlobalEnvironment implements Environment {
	
	private final VariableList globalVars;
	private final ObjectManager objects;

	public GlobalEnvironment(VariableList globalVars, ObjectManager objects) {
		this.globalVars = globalVars;
		this.objects = objects;
	}

	@Override
	public ObjectEnvironment getEnvironmentFor(TemplateObject o) {
		return new ObjectEnvironment(globalVars, objects, o);
	}
	
	@Override
	public List<TemplateObject> getObjectsFor(String klass) {
		return objects.getObjectsFor(klass);
	}

	@Override
	public String getValue(String name) {
		return globalVars.getValue(name);
	}
	
	@Override
	public void setValue(String key, String value) {
		globalVars.put(key, value);
	}

	@Override
	public boolean isSet(String name) {
		return globalVars.isSet(name);
	}

}
