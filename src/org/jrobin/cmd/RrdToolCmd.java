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

package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdDef;

import java.io.IOException;

abstract class RrdToolCmd {

	private RrdCmdScanner cmdScanner;

	abstract String getCmdType();
	abstract Object execute() throws RrdException, IOException;

	public void setCommand(String command) throws RrdException {
		cmdScanner = new RrdCmdScanner(command);
	}

	String getOptionValue(String shortForm, String longForm) throws RrdException {
		return cmdScanner.getOptionValue(shortForm, longForm);
	}

	String getOptionValue(String shortForm, String longForm, String defaultValue) throws RrdException {
		return cmdScanner.getOptionValue(shortForm, longForm, defaultValue);
	}

	boolean getBooleanOption(String shortForm, String longForm) {
		return cmdScanner.getBooleanOption(shortForm, longForm);
	}

	String[] getRemainingWords() {
		return cmdScanner.getRemainingWords();
	}

	static boolean rrdDbPoolUsed = true;
	static boolean standardOutUsed = true;

	static boolean isRrdDbPoolUsed() {
		return rrdDbPoolUsed;
	}

	static void setRrdDbPoolUsed(boolean rrdDbPoolUsed) {
		RrdToolCmd.rrdDbPoolUsed = rrdDbPoolUsed;
	}

	static boolean isStandardOutUsed() {
		return standardOutUsed;
	}

	static void setStandardOutUsed(boolean standardOutUsed) {
		RrdToolCmd.standardOutUsed = standardOutUsed;
	}

	static long parseLong(String value) throws RrdException {
		try {
			return Long.parseLong(value);
		}
		catch(NumberFormatException nfe) {
			throw new RrdException(nfe);
		}
	}

	static int parseInt(String value) throws RrdException {
		try {
			return Integer.parseInt(value);
		}
		catch(NumberFormatException nfe) {
			throw new RrdException(nfe);
		}
	}

	static double parseDouble(String value) throws RrdException {
		if(value.equals("U")) {
			return Double.NaN;
		}
		try {
			return Double.parseDouble(value);
		}
		catch(NumberFormatException nfe) {
			throw new RrdException(nfe);
		}
	}

	static void print(String s) {
		if(standardOutUsed) {
			System.out.print(s);
		}
	}

	static void println(String s) {
		if(standardOutUsed) {
			System.out.println(s);
		}
	}

	static RrdDb getRrdDbReference(String path) throws IOException, RrdException {
		if(rrdDbPoolUsed) {
			return RrdDbPool.getInstance().requestRrdDb(path);
		}
		else {
			return new RrdDb(path);
		}
	}

	static RrdDb getRrdDbReference(String path, String xmlPath) throws IOException, RrdException {
		if(rrdDbPoolUsed) {
			return RrdDbPool.getInstance().requestRrdDb(path, xmlPath);
		}
		else {
			return new RrdDb(path, xmlPath);
		}
	}

	static RrdDb getRrdDbReference(RrdDef rrdDef) throws IOException, RrdException {
		if(rrdDbPoolUsed) {
			return RrdDbPool.getInstance().requestRrdDb(rrdDef);
		}
		else {
			return new RrdDb(rrdDef);
		}
	}

	static void releaseRrdDbReference(RrdDb rrdDb) throws IOException, RrdException {
		if(rrdDbPoolUsed) {
			RrdDbPool.getInstance().release(rrdDb);
		}
		else {
			rrdDb.close();
		}
	}
}
