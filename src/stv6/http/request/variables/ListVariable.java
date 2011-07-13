package stv6.http.request.variables;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import stv6.http.HttpSocket;

public class ListVariable extends Variable implements Iterable<Variable> {
	
	private ArrayList<Variable> list = new ArrayList<Variable>();

	public ListVariable(String variableName) {
		super(variableName, null);
	}

	public void add(Variable v) {
		list.add(v);
	}
	
	public Variable get(int index) {
		return list.get(index);
	}
	
	@Override
	public int length() {
		int len = 0;
		for (Variable v : this)
			len += 4 + key.length() + HttpSocket.encode(v.value).length();
		// we add 4 for "[]=" and the "&"
		// then we subtract one at the end since we don't 
		//	have "&" at the end
		return len-1;
	}

	@Override
	public Iterator<Variable> iterator() {
		return list.iterator();
	}
	
	public int size() {
		return list.size();
	}

	@Override
	public void writeTo(PrintWriter output) {
		if (size() == 0)
			return;
		
		int i=0;
		for (Variable v : this) {
			output.append(key);
			output.append("[]=");
			output.append(HttpSocket.encode(v.value));
			if (++i < size())
				output.append('&');
		}
	}
}
