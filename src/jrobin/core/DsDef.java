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

/**
 * Class to represent single data source definition within the RRD file.
 * Datasource definition consists of the following five elements:
 *
 * <ul>
 * <li>data source name
 * <li>data soruce type
 * <li>heartbeat
 * <li>minimal value
 * <li>maximal value
 * </ul>
 * <p>For the complete explanation of all source definition parameters, see RRDTool's
 * <a href="../../../man/rrdcreate.html" target="man">rrdcreate man page</a>.</p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class DsDef {
	/** array of valid source types */
	public static final String[] DS_TYPES = { "GAUGE", "COUNTER", "DERIVE", "ABSOLUTE" };

	private String dsName, dsType;
	private long heartbeat;
    private double minValue, maxValue;

	/**
	 * <p>Creates new data source definition object. This object should be passed as argument
	 * to {@link jrobin.core.RrdDef#addDatasource(jrobin.core.DsDef) addDatasource()} method of
	 * {@link jrobin.core.RrdDb RrdDb} object.</p>
	 *
     * <p>For the complete explanation of all source definition parameters, see RRDTool's
     * <a href="../../../man/rrdcreate.html" target="man">rrdcreate man page</a></p>
	 *
	 * @param dsName Data source name.
	 * @param dsType Data source type. Valid values are "COUNTER", "GAUGE", "DERIVE"
	 * and "ABSOLUTE"
	 * @param heartbeat Hearbeat
	 * @param minValue Minimal value. Use <code>Double.NaN</code> if unknown.
	 * @param maxValue Maximal value. Use <code>Double.NaN</code> if unknown.
	 * @throws RrdException Thrown if any parameter has illegal value.
	 */
	public DsDef(String dsName, String dsType, long heartbeat,
				 double minValue, double maxValue) throws RrdException {
		this.dsName = dsName;
		this.dsType = dsType;
		this.heartbeat = heartbeat;
		this.minValue = minValue;
		this.maxValue = maxValue;
		validate();
	}

	/**
	 * Returns data source name.
	 * @return Data source name.
	 */
	public String getDsName() {
		return dsName;
	}

	/**
	 * Returns source type.
	 * @return Source type ("COUNTER", "GAUGE", "DERIVE" or "ABSOLUTE").
	 */
	public String getDsType() {
		return dsType;
	}

	/**
	 * Returns source heartbeat.
	 * @return Source heartbeat.
	 */
	public long getHeartbeat() {
		return heartbeat;
	}

	/**
	 * Returns minimal calculated source value.
	 * @return Minimal value.
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * Returns maximal calculated source value.
	 * @return Maximal value.
	 */
	public double getMaxValue() {
		return maxValue;
	}

	private void validate() throws RrdException {
		if(dsName == null || dsName.length() == 0) {
			throw new RrdException("Invalid datasource name specified");
		}
		if(!isValidDsType(dsType)) {
			throw new RrdException("Invalid datasource type specified: " + dsType);
		}
		if(heartbeat <= 0) {
			throw new RrdException("Invalid heartbeat, must be positive: " + heartbeat);
		}
		if(!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
			throw new RrdException("Invalid min/max values specified: " +
				minValue + "/" + maxValue);
		}
	}

	/**
	 * Checks if function argument represents valid source type.
	 * @param dsType Source type to be checked.
	 * @return <code>true</code> if <code>dsType</code> is valid type,
	 * <code>false</code> otherwise.
	 */
	public static boolean isValidDsType(String dsType) {
		for(int i = 0; i < DS_TYPES.length; i++) {
			if(DS_TYPES[i].equals(dsType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns string representing source definition (RRDTool format).
	 * @return String containing all data source definition parameters.
	 */
	public String dump() {
		return "DS:" + dsName + ":" + dsType + ":" + heartbeat +
			":" + Util.formatDouble(minValue, "U") +
			":" + Util.formatDouble(maxValue, "U");
	}

	/**
	 * Checks if two source definitions are equal.
	 * Source definitions are treated as equal if they have the same source name.
	 * It is not possible to create RRD file with two equal archive definitions.
	 * @param obj Archive definition to compare with.
	 * @return <code>true</code> if archive definitions are equal,
	 * <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if(obj instanceof DsDef) {
			DsDef dsObj = (DsDef) obj;
			return dsName.equals(dsObj.dsName);
		}
		return false;
	}

}
