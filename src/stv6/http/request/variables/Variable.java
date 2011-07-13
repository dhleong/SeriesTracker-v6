package stv6.http.request.variables;

import java.io.PrintWriter;

import stv6.http.HttpSocket;

public class Variable {
	
	public final String key, value;
	
	public Variable(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other.getClass() != this.getClass())
			return false;
		
		return key.equals( ((Variable)other).key);
	}


	public int length() {
		return 1+key.length()+HttpSocket.encode(value).length();
	}
	
	public boolean valueEquals(Variable other) {
		return value.equals(other.value);
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public String toString() {
		return key + ": " + value;
	}

	public void writeTo(PrintWriter output) {
		output.write(key+"="+HttpSocket.encode(value));
	}
}
