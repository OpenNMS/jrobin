/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

package jrobin.core;

import java.util.ArrayList;

/**
 * <p>Class to represent definition of new RRD file. Object of this class is used to create
 * new RRD file from scratch - pass its reference as a <code>RrdDb</code> constructor
 * argument (see documentation for {@link jrobin.core.RrdDb RrdDb} class). <code>RrdDef</code>
 * object <b>does not</b> actually create new RRD file. It just holds all necessary
 * information which will be used during the actual creation process</p>
 *
 * <p>RRD file definition (RrdDef object) consists of the following elements:</p>
 *
 * <ul>
 * <li> path to RRD file that will be created
 * <li> starting timestamp
 * <li> step
 * <li> one or more datasource definitions
 * <li> one or more archive definitions
 * </ul>
 * <p>RrdDef provides API to set all these elements. For the complete explanation of all
 * RRD definition parameters, see RRDTool's
 * <a href="../../../man/rrdcreate.html" target="man">rrdcreate man page</a>.</p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class RrdDef {
	/** default RRD step to be used if not specified in constructor (300 seconds) */
	public static final long DEFAULT_STEP = 300L;
	/** if not specified in constructor, starting timestamp will be set to the
	 * current timestamp plus DEFAULT_INITIAL_SHIFT seconds (-10) */
	public static final long DEFAULT_INITIAL_SHIFT = -10L;

	private String path;
	private long startTime = Util.getTime() + DEFAULT_INITIAL_SHIFT;
	private long step = DEFAULT_STEP;
    private ArrayList dsDefs = new ArrayList(), arcDefs = new ArrayList();

	/**
	 * <p>Creates new RRD definition object with the given file path.
	 * When this object is passed to
	 * <code>RrdDb</code> constructor, new RRD file will be created using the
	 * specified file path. </p>
	 * @param path Path to new RRD file.
	 * @throws RrdException Thrown if file name is invalid (null or empty).
	 */
	public RrdDef(String path) throws RrdException {
		if(path == null || path.length() == 0) {
			throw new RrdException("No filename specified");
		}
		this.path = path;
	}

	/**
	 * <p>Creates new RRD definition object with the given file path and step.</p>
	 * @param path Path to new RRD file.
	 * @param step RRD step.
	 * @throws RrdException Thrown if supplied parameters are invalid.
	 */
	public RrdDef(String path, long step) throws RrdException {
		this(path);
		if(step <= 0) {
			throw new RrdException("Invalid RRD step specified: " + step);
		}
		this.step = step;
	}

	/**
	 * <p>Creates new RRD definition object with the given file path, starting timestamp
	 * and step.</p>
	 * @param path Path to new RRD file.
	 * @param startTime RRD starting timestamp.
	 * @param step RRD step.
	 * @throws RrdException Thrown if supplied parameters are invalid.
	 */
	public RrdDef(String path, long startTime, long step) throws RrdException {
		this(path, step);
		if(startTime < 0) {
			throw new RrdException("Invalid RRD start time specified: " + startTime);
		}
		this.startTime = startTime;
	}

	/**
	 * Returns file path for the new RRD file
	 * @return path to the new RRD file
	 */

	public String getPath() {
		return path;
	}

	/**
	 * Returns starting timestamp for the RRD file that will be created.
	 * @return RRD starting timestamp
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Returns time step for the RRD file that will be created.
	 * @return RRD step
	 */
	public long getStep() {
		return step;
	}

	/**
	 * Sets path to RRD file.
	 * @param path to new RRD file.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Sets RRD starting timestamp.
	 * @param startTime RRD starting timestamp.
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Sets RRD time step.
	 * @param step RRD time step.
	 */
	public void setStep(long step) {
		this.step = step;
	}

	/**
	 * Adds single datasource definition represented with object of class <code>DsDef</code>.
	 * @param dsDef Datasource definition.
	 * @throws RrdException Thrown if new datasource definition uses already used data
	 * source name.
	 */
	public void addDatasource(DsDef dsDef) throws RrdException {
		if(dsDefs.contains(dsDef)) {
			throw new RrdException("Datasource already defined: " + dsDef.dump());
		}
		dsDefs.add(dsDef);
	}

	/**
	 * Adds single datasource to RRD definition by specifying its data source name, source type,
	 * heartbeat, minimal and maximal value. For the complete explanation of all data
	 * source definition parameters see RRDTool's
	 * <a href="../../../man/rrdcreate.html" target="man">rrdcreate man page</a>.</p>
	 *
	 * @param dsName Data source name.
	 * @param dsType Data source type. Valid types are "COUNTER",
	 * "GAUGE", "DERIVE" and "ABSOLUTE".
	 * @param heartbeat Data source heartbeat.
	 * @param minValue Minimal acceptable value. Use <code>Double.NaN</code> if unknown.
	 * @param maxValue Maximal acceptable value. Use <code>Double.NaN</code> if unknown.
	 * @throws RrdException Thrown if new datasource definition uses already used data
	 * source name.
	 */
	public void addDatasource(String dsName, String dsType, long heartbeat,
		double minValue, double maxValue) throws RrdException {
		addDatasource(new DsDef(dsName, dsType, heartbeat, minValue, maxValue));
	}

	/**
	 * Adds data source definitions to RRD definition in bulk.
	 * @param dsDefs Array of data source definition objects.
	 * @throws RrdException Thrown if duplicate data source name is used.
	 */
	public void addDatasource(DsDef[] dsDefs) throws RrdException {
		for(int i = 0; i < dsDefs.length; i++) {
			addDatasource(dsDefs[i]);
		}
	}

	/**
	 * Adds single archive definition represented with object of class <code>ArcDef</code>.
	 * @param arcDef Archive definition.
	 * @throws RrdException Thrown if archive with the same consolidation function
	 * and the same number of steps is already added.
	 */
	public void addArchive(ArcDef arcDef) throws RrdException {
		if(arcDefs.contains(arcDef)) {
			throw new RrdException("Archive already defined: " + arcDef.dump());
		}
		arcDefs.add(arcDef);
	}

	/**
	 * Adds archive definitions to RRD definition in bulk.
	 * @param arcDefs Array of archive definition objects
	 * @throws RrdException Thrown if RRD definition already contains archive with
	 * the same consolidation function and the same number of steps.
	 */
	public void addArchive(ArcDef[] arcDefs) throws RrdException {
		for(int i = 0; i < arcDefs.length; i++) {
			addArchive(arcDefs[i]);
		}
	}

	/**
	 * Adds single archive definition by specifying its consolidation function, X-files factor,
	 * number of steps and rows. For the complete explanation of all archive
	 * definition parameters see RRDTool's
	 * <a href="../../../man/rrdcreate.html" target="man">rrdcreate man page</a>.</p>
	 * @param consolFun Consolidation function. Valid values are "AVERAGE",
	 * "MIN", "MAX" and "LAST"
	 * @param xff X-files factor. Valid values are between 0 and 1.
	 * @param steps Number of archive steps
	 * @param rows Number of archive rows
	 * @throws RrdException Thrown if archive with the same consolidation function
	 * and the same number of steps is already added.
	 */
	public void addArchive(String consolFun, double xff, int steps, int rows)
		throws RrdException {
		addArchive(new ArcDef(consolFun, xff, steps, rows));
	}

	void validate() throws RrdException {
        if(dsDefs.size() == 0) {
			throw new RrdException("No RRD datasource specified. At least one is needed.");
		}
    	if(arcDefs.size() == 0) {
			throw new RrdException("No RRD archive specified. At least one is needed.");
		}
	}

	/**
	 * Returns all data source definition objects specified so far.
	 * @return Array of data source definition objects
	 */
	public DsDef[] getDsDefs() {
		return (DsDef[]) dsDefs.toArray(new DsDef[0]);
	}

	/**
	 * Returns all archive definition objects specified so far.
	 * @return Array of archive definition objects.
	 */
	public ArcDef[] getArcDefs() {
		return (ArcDef[]) arcDefs.toArray(new ArcDef[0]);
	}

	/**
	 * Returns number of defined datasources.
	 * @return Number of defined datasources.
	 */
	public int getDsCount() {
		return dsDefs.size();
	}

	/**
	 * Returns number of defined archives.
	 * @return Number of defined archives.
	 */
	public int getArcCount() {
		return arcDefs.size();
	}

	/**
	 * Returns string that represents all specified RRD creation parameters. Returned string
	 * has the syntax of RRDTool's <code>create</code> command.
	 * @return Dumped content of <code>RrdDb</code> object.
	 */
	public String dump() {
		StringBuffer buffer = new StringBuffer(RrdDb.RRDTOOL);
		buffer.append(" create " + path);
		buffer.append(" --start " + getStartTime());
		buffer.append(" --step " + getStep() + " ");
		for(int i = 0; i < dsDefs.size(); i++) {
			DsDef dsDef = (DsDef) dsDefs.get(i);
			buffer.append(dsDef.dump() + " ");
		}
		for(int i = 0; i < arcDefs.size(); i++) {
			ArcDef arcDef = (ArcDef) arcDefs.get(i);
			buffer.append(arcDef.dump() + " ");
		}
		return buffer.toString().trim();
	}

	String getRrdToolCommand() {
		return dump();
	}
}
