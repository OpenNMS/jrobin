/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran@codeloop.com>
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 * $Id$
 */
package org.jrobin.core.jrrd;

import java.io.IOException;

/**
 * Show some of the things jRRD can do.
 *
 * @author <a href="mailto:ciaran@codeloop.com">Ciaran Treanor</a>
 * @version $Revision$
 */
public class Main {

	public Main(String rrdFile) {

		RRDatabase rrd = null;
		DataChunk chunk = null;

		try {
			rrd = new RRDatabase(rrdFile);
			chunk = rrd.getData(ConsolidationFunctionType.AVERAGE);
		} catch (Exception e) {
			e.printStackTrace();

			return;
		}

		rrd.toXml(System.out);        // Dump the database as XML.
		rrd.printInfo(System.out);    // Dump the database header information.
		System.out.println(rrd);      // Dump a summary of the contents of the database.
		System.out.println(chunk);    // Dump the chunk.

		try {
			rrd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void usage(int status) {
		System.err.println("Usage: " + Main.class.getName() + " rrdfile");
		System.exit(status);
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			usage(1);
		}
		new Main(args[0]);
	}
}
