/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
 *
 * (C) Copyright 2003, by Sasa Markovic.
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

package jrobin.core;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Class defines various utility functions used in JRobin. 
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class Util {
	private static String DOUBLE_FORMAT = "0.0000000000E00";
	private static final DecimalFormat df = new DecimalFormat(DOUBLE_FORMAT);

	private Util() { };

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

	static String formatDouble(double x, boolean translateNans) {
		if(Double.isNaN(x)) {
			return translateNans? "U": "" + Double.NaN;
		}
		if(x >= Long.MIN_VALUE && x <= Long.MAX_VALUE && Math.round(x) == x) {
			return "" + (long) x;
		}
		return df.format(x);
	}

	static String formatDouble(double x) {
		return formatDouble(x, false);
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

}
