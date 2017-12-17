package saac.utils;

import java.io.OutputStream;
import java.io.PrintStream;

public class Output {
	public static PrintStream none = new PrintStream(new OutputStream() { @Override public void write(int b) { } });
	public static PrintStream debug = none;
	public static PrintStream debug1 = none;//System.err;
	public static PrintStream info = System.out;
	public static PrintStream state = none;//System.out;
	public static PrintStream jumping_info = System.out;
}