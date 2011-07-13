package stv6.http;

import java.io.PrintWriter;

public interface HttpWritable {

	void writeHeaders(PrintWriter output);

	void writeBody(PrintWriter output);

	boolean isText();

	byte[] getBytes();

}
