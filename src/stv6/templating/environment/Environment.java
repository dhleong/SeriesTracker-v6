package stv6.templating.environment;

import java.util.List;

import stv6.templating.TemplateObject;

public interface Environment {

	public boolean isSet(String name);
	
	public ObjectEnvironment getEnvironmentFor(TemplateObject o);

	public List<TemplateObject> getObjectsFor(String klass);
	
	public String getValue(String name);

	public void setValue(String key, String value);
}
