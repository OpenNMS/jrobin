/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.convertor;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * Simple utility class to convert RRD files created with RRDTool 1.0.x to
 * JRobin's native RRD format. Conversion process is quite fast.
 */
public class Convertor {
	private static final String FACTORY_NAME = "FILE";
	private static final String SUFFIX = ".jrb";
	private static final DecimalFormat secondsFormatter = new DecimalFormat("##0.000");
	private static final DecimalFormat countFormatter = new DecimalFormat("0000");

	private String[] files;
	private int totalCount, badCount, goodCount;

	private Convertor(final String[] files) throws RrdException {
		RrdDb.setDefaultFactory(FACTORY_NAME);
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
		for (String file : files) {
			convertFile(file);
		}
		println(ruler);
		println("Finished: " + totalCount + " total, " +
				goodCount + " OK, " + badCount + " failed");
		Date t2 = new Date();
		double secs = (t2.getTime() - t1.getTime()) / 1000.0;
		println("Conversion took " + secondsFormatter.format(secs) + " sec");
		if (totalCount > 0) {
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
		}
		catch (Exception e) {
			badCount++;
			println("[" + e + "]");
		}
	}

	private static void println(String msg) {
		System.out.println(msg);
	}

	private static void print(String msg) {
		System.out.print(msg);
	}

	/**
	 * <p>To convert RRD files created with RRDTool use the following syntax:</p>
	 * <pre>
	 * java -cp jrobin-{version} org.jrobin.convertor.Convert [path to RRD file(s)]
	 * <pre>
	 * <p>For example:</p>
	 * <pre>
	 * java -cp jrobin-{version} org.jrobin.convertor.Convert rrdtool/files/*.rrd
	 * </pre>
	 * <p>...and enjoy the show.</p>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			println("Usage  : java -jar convertor.jar <RRD file pattern> ...");
			println("Example: java -jar convertor.jar files/*.rrd");
			System.exit(1);
		}
		try {
			Convertor c = new Convertor(args);
			c.convertAll();
		} catch (final RrdException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
