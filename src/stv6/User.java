package stv6;

import stv6.templating.TemplateObject;

public class User implements TemplateObject {
	
	private final int id;
	private final String name;
	
	public User(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getClassName() {
		return "user";
	}

}
