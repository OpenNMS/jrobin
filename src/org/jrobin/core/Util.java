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

package org.jrobin.core;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.GregorianCalendar;
import java.io.File;
import java.io.IOException;

/**
 * Class defines various utility functions used in JRobin. 
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class Util {
	
	// pattern RRDTool uses to format doubles in XML files
	static final String PATTERN = "0.0000000000E00";
	// directory under $USER_HOME used for demo graphs storing
	static final String JROBIN_DIR = "jrobin-demo";

	static final DecimalFormat df;
	static {
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * Returns current timestamp in seconds (without milliseconds). Returned timestamp
	 * is obtained with the following expression:
	 *
	 * <code>(System.currentTimeMillis() + 500L) / 1000L</code>
	 * @return Current timestamp
	 */
	public static long getTime() {
		return (System.currentTimeMillis() + 500L) / 1000L;
	}

	static long normalize(long timestamp, long step) {
		return timestamp - timestamp % step;
	}

	/**
	 * Returns the greater of two double values, but treats NaN as the smallest possible
	 * value. Note that <code>Math.max()</code> behaves differently for NaN arguments.
	 *
	 * @param x an argument
	 * @param y another argument
	 * @return the lager of arguments
	 */
	public static double max(double x, double y) {
		return Double.isNaN(x)? y: Double.isNaN(y)? x: Math.max(x, y);
	}

	/**
	 * Returns the smaller of two double values, but treats NaN as the greatest possible
	 * value. Note that <code>Math.min()</code> behaves differently for NaN arguments.
	 *
	 * @param x an argument
	 * @param y another argument
	 * @return the smaller of arguments
	 */
	public static double min(double x, double y) {
		return Double.isNaN(x)? y: Double.isNaN(y)? x: Math.min(x, y);
	}

	static double sum(double x, double y) {
		return Double.isNaN(x)? y: Double.isNaN(y)? x: x + y;
	}

	static String formatDouble(double x, String nanString, boolean forceExponents) {
		if(Double.isNaN(x)) {
			return nanString;
		}
		if(forceExponents) {
			return df.format(x);
		}
		return "" + x;
	}

	static String formatDouble(double x, boolean forceExponents) {
		return formatDouble(x, "" + Double.NaN, forceExponents);
	}

	static void debug(String message) {
		if(RrdDb.DEBUG) {
			System.out.println(message);
		}
	}

	/**
	 * Returns <code>Date</code> object for the given timestamp (in seconds, without
	 * milliseconds)
	 * @param timestamp Timestamp in seconds.
	 * @return Corresponding Date object.
	 */
	public static Date getDate(long timestamp) {
		return new Date(timestamp * 1000L);
	}

	/**
	 * Returns timestamp (unix epoch) for the given Date object
	 * @param date Date object
	 * @return Corresponding timestamp (without milliseconds)
	 */
	public static long getTimestamp(Date date) {
		return (date.getTime() + 500L) / 1000L;
	}

	/**
	 * Returns timestamp (unix epoch) for the given GregorianCalendar object
	 * @param gc GregorianCalendar object
	 * @return Corresponding timestamp (without milliseconds)
	 */
	public static long getTimestamp(GregorianCalendar gc) {
		return getTimestamp(gc.getTime());
	}

	/**
	 * Returns timestamp (unix epoch) for the given year, month, day, hour and minute.
	 * @param year Year
	 * @param month Month (zero-based)
	 * @param day Day in month
	 * @param hour Hour
	 * @param min Minute
	 * @return Corresponding timestamp
	 */
	public static long getTimestamp(int year, int month, int day, int hour, int min) {
		GregorianCalendar gc = new GregorianCalendar(year, month, day, hour, min);
		return Util.getTimestamp(gc);
	}

	/**
	 * Returns timestamp (unix epoch) for the given year, month and day.
	 * @param year Year
	 * @param month Month (zero-based)
	 * @param day Day in month
	 * @return Corresponding timestamp
	 */
	public static long getTimestamp(int year, int month, int day) {
		return Util.getTimestamp(year, month, day, 0, 0);
	}

	static double parseDouble(String valueStr) {
		double value;
		try {
			value = Double.parseDouble(valueStr);
		}
		catch(NumberFormatException nfe) {
			value = Double.NaN;
		}
		return value;
	}

	private static final File homeDirFile;
	private static final String homeDirPath;

	static {
		String delim = System.getProperty("file.separator");
		homeDirPath = System.getProperty("user.home") + delim + JROBIN_DIR + delim;
		homeDirFile = new File(homeDirPath);
	}

	/**
	 * Returns path to directory used for placement of JRobin demo graphs.  and creates it
	 * if necessary.
	 * @return Path to demo directory (defaults to $HOME/jrobin/) if directory exists or
	 * was successfully created. Null if such directory could not be created.
	 */
	public static String getJRobinDemoDirectory() {
		return (homeDirFile.exists() || homeDirFile.mkdirs())? homeDirPath: null;
	}

	static boolean sameFilePath(String path1, String path2) throws IOException {
		File file1 = new File(path1);
		File file2 = new File(path2);
		return file1.getCanonicalPath().equals(file2.getCanonicalPath());
	}

}


