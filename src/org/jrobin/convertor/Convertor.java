/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.jrobin.convertor;

import org.jrobin.core.RrdDb;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;

class Convertor {
	private static final String SUFFIX = ".jrb";
	private static final DecimalFormat secondsFormatter = new DecimalFormat("##0.000");
	private static final DecimalFormat countFormatter = new DecimalFormat("0000");

	private String[] files;
	private int totalCount, badCount, goodCount;

	private Convertor(String[] files) {
		this.files = files;
	}

	private void convertAll() {
		Date t1 = new Date();
		final String ruler = "=======================================================================";
		println(ruler);
		println("Converting RRDTool files to JRobin native format.");
		println("Original RRDTool files will not be modified in any way");
		println("JRobin files created during the process will have a " + SUFFIX + " suffix");
		println(ruler);
		for(int i = 0; i < files.length; i++) {
			convertFile(files[i]);
		}
		println(ruler);
		println("Finished: " + totalCount + " total, " +
			goodCount + " OK, " + badCount + " failed");
		Date t2 = new Date();
		double secs = (t2.getTime() - t1.getTime()) / 1000.0;
		println("Conversion took " + secondsFormatter.format(secs) + " sec");
		if(totalCount > 0) {
			double avgSec = secs / totalCount;
			println("Average per-file conversion time: " + secondsFormatter.format(avgSec) + " sec");
		}
	}

	private void convertFile(String path) {
		long start = System.currentTimeMillis();
		totalCount++;
		try {
			File rrdFile = new File(path);
			print(countFormatter.format(totalCount) + "/" + countFormatter.format(files.length) +
					" " + rrdFile.getName() + " ");
			String sourcePath = rrdFile.getCanonicalPath();
			String destPath = sourcePath + SUFFIX;
			RrdDb rrd = new RrdDb(destPath, RrdDb.PREFIX_RRDTool + sourcePath);
			rrd.close();
			goodCount++;
			double seconds = (System.currentTimeMillis() - start) / 1000.0;
			println("[OK, " + secondsFormatter.format(seconds) + " sec]");
		} catch (Exception e) {
			badCount++;
			println("[" + e + "]");
		}
	}

	private final static void println(String msg) {
		System.out.println(msg);
	}

	private final static void print(String msg) {
		System.out.print(msg);
	}

	public static void main(String[] args) {
		if(args.length == 0) {
			println("Usage  : java -jar convertor.jar <RRD file pattern> ...");
			println("Example: java -jar convertor.jar files/*.rrd");
			System.exit(1);
		}
		Convertor c = new Convertor(args);
		c.convertAll();
	}
}
