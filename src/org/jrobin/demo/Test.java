package org.jrobin.demo;

import org.jrobin.core.RrdToolkit;
import org.jrobin.core.RrdException;

import java.io.IOException;

class Test {
	public static void main(String[] args) throws IOException, RrdException {
		String dir = ".";
		String[] list = RrdToolkit.getCanonicalPaths(dir, ".rrd", false);
		for (String path : list) {
			System.out.println(path);
		}
	}
}
