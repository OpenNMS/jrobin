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

/**
 * Class to represent single archive definition within the RRD.
 * Archive definition consists of the following four elements:
 *
 * <ul>
 * <li>consolidation function
 * <li>X-files factor
 * <li>number of steps
 * <li>number of rows.
 * </ul>
 * <p>For the complete explanation of all archive definition parameters, see RRDTool's
 * <a href="../../../../man/rrdcreate.html" target="man">rrdcreate man page</a>
 * </p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */

public class ArcDef {
	/** array of valid consolidation function names */
	public static final String CONSOL_FUNS[] = { "AVERAGE", "MAX", "MIN", "LAST" };

	private String consolFun;
	private double xff;
	private int steps, rows;

	/**
	 * <p>Creates new archive definition object. This object should be passed as argument to
	 * {@link org.jrobin.core.RrdDef#addArchive(org.jrobin.core.ArcDef) addArchive()} method of
	 * {@link org.jrobin.core.RrdDb RrdDb} object.</p>
	 *
     * <p>For the complete explanation of all archive definition parameters, see RRDTool's
	 * <a href="../../../../man/rrdcreate.html" target="man">rrdcreate man page</a></p>
	 *
	 * @param consolFun Consolidation function. Allowed values are "AVERAGE", "MIN",
	 * "MAX" and "LAST".
	 * @param xff X-files factor, between 0 and 1.
	 * @param steps Number of archive steps.
	 * @param rows Number of archive rows.
	 * @throws RrdException Thrown if any parameter has illegal value.
	 */
	public ArcDef(String consolFun, double xff, int steps, int rows) throws RrdException {
		this.consolFun = consolFun;
		this.xff = xff;
		this.steps = steps;
		this.rows = rows;
		validate();
	}

	/**
	 * Returns consolidation function.
	 * @return Consolidation function.
	 */
	public String getConsolFun() {
		return consolFun;
	}

	/**
	 * Returns the X-files factor.
	 * @return X-files factor value.
	 */
	public double getXff() {
		return xff;
	}

	/**
	 * Returns the number of primary RRD steps which complete a single archive step.
	 * @return Number of steps.
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * Returns the number of rows (aggregated values) stored in the archive.
	 * @return Number of rows.
	 */
	public int getRows() {
		return rows;
	}

	private void validate() throws RrdException {
		if(!isValidConsolFun(consolFun)) {
			throw new RrdException("Invalid consolidation function specified: " + consolFun);
		}
		if(Double.isNaN(xff) || xff < 0.0 || xff >= 1.0) {
			throw new RrdException("Invalid xff, must be >= 0 and < 1: " + xff);
		}
		if(steps <= 0 || rows <= 0) {
			throw new RrdException("Invalid steps/rows number: " + steps + "/" + rows);
		}
	}

	/**
	 * Returns string representing archive definition (RRDTool format).
	 * @return String containing all archive definition parameters.
	 */
	public String dump() {
		return "RRA:" + consolFun + ":" + xff + ":" +	steps + ":" + rows;
	}

	/**
	 * Checks if two archive definitions are equal.
	 * Archive definitions are considered equal if they have the same number of steps
	 * and the same consolidation function. It is not possible to create RRD with two
	 * equal archive definitions.
	 * @param obj Archive definition to compare with.
	 * @return <code>true</code> if archive definitions are equal,
	 * <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof ArcDef) {
			ArcDef arcObj = (ArcDef) obj;
			return consolFun.equals(arcObj.consolFun) && steps == arcObj.steps;
		}
		return false;
	}

	/**
	 * Checks if function argument represents valid consolidation function name.
	 * @param consolFun Consolidation function to be checked
	 * @return <code>true</code> if <code>consolFun</code> is valid consolidation function,
	 * <code>false</code> otherwise.
	 */
	public static boolean isValidConsolFun(String consolFun) {
		for(int i = 0; i < CONSOL_FUNS.length; i++) {
			if(CONSOL_FUNS[i].equals(consolFun)) {
				return true;
			}
		}
		return false;
	}

	void setRows(int rows) {
		this.rows = rows;
	}

	boolean exactlyEqual(ArcDef def) {
		return consolFun.equals(def.consolFun) && xff == def.xff &&
				steps == def.steps && rows == def.rows;
	}
}
