package jrobin.mrtg;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Jul 30, 2003
 * Time: 11:25:12 AM
 * To change this template use Options | File Templates.
 */
public class Debug {
	static final boolean DEBUG = false;

	public static void print(String msg) {
		if(DEBUG) {
			System.out.println(msg);
		}
	}

}

