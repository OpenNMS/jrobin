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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
/**
 * <p>Class used to perform various complex operations on RRD files. Use an instance of the
 * RrdToolkit class to:</p>
 * <ul>
 * <li>add datasource to a RRD file.
 * <li>add archive to a RRD file.
 * <li>remove datasource from a RRD file.
 * <li>remove archive from a RRD file.
 * </ul>
 * <p>All these operations can be performed on the copy of the original RRD file, or on the
 * original file itself (with possible backup file creation)</p>
 */
public class RrdToolkit {
	private static RrdToolkit ourInstance;

	/**
	 * Returns an instance of RrdToolkit.
	 * @return Toolkint instance to work with
	 */
	public synchronized static RrdToolkit getInstance() {
		if (ourInstance == null) {
			ourInstance = new RrdToolkit();
		}
		return ourInstance;
	}

	private RrdToolkit() {
	}

	/**
	 * Creates a new RRD file with one more datasource in it. RRD file is created based on the
	 * existing one (the original RRD file is not modified at all). All data from
	 * the original RRD file is copied to the new one.
	 * @param sourcePath path to a RRD file to import data from (will not be modified)
	 * @param destPath path to a new RRD file (will be created)
	 * @param newDatasource Datasource definition to be added to the new RRD file
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void addDatasource(String sourcePath, String destPath, DsDef newDatasource)
		throws IOException, RrdException {
		if(Util.sameFilePath(sourcePath, destPath)) {
			throw new RrdException("Source and destination paths are the same");
		}
        RrdDb rrdSource = new RrdDb(sourcePath);
		RrdDef rrdDef = rrdSource.getRrdDef();
		rrdDef.setPath(destPath);
		rrdDef.addDatasource(newDatasource);
		RrdDb rrdDest = new RrdDb(rrdDef);
		rrdSource.copyStateTo(rrdDest);
		rrdSource.close();
		rrdDest.close();
	}

	/**
	 * <p>Adds one more datasource to a RRD file.</p>
	 * <p>WARNING: This method is potentialy dangerous! It will modify your RRD file.
	 * It is highly recommended to preserve the original RRD file (<i>saveBackup</i>
	 * should be set to <code>true</code>). The backup file will be created in the same
	 * directory as the original one with <code>.bak</code> extension added to the
	 * original name.</p>
	 * <p>Before applying this method, be sure that the specified RRD file is not in use
	 * (not open)</p>
	 * @param sourcePath path to a RRD file to add datasource to.
	 * @param newDatasource Datasource definition to be added to the RRD file
	 * @param saveBackup true, if backup of the original file should be created;
	 * false, otherwise
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void addDatasource(String sourcePath, DsDef newDatasource, boolean saveBackup)
		throws IOException, RrdException {
		String destPath = Util.getTmpFilename();
		addDatasource(sourcePath, destPath, newDatasource);
		copyFile(destPath, sourcePath, saveBackup);
	}

	/**
	 * Creates a new RRD file with one datasource removed. RRD file is created based on the
	 * existing one (the original RRD file is not modified at all). All remaining data from
	 * the original RRD file is copied to the new one.
	 * @param sourcePath path to a RRD file to import data from (will not be modified)
	 * @param destPath path to a new RRD file (will be created)
	 * @param dsName Name of the Datasource to be removed from the new RRD file
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void removeDatasource(String sourcePath, String destPath, String dsName)
		throws IOException, RrdException {
		if(Util.sameFilePath(sourcePath, destPath)) {
			throw new RrdException("Source and destination paths are the same");
		}
        RrdDb rrdSource = new RrdDb(sourcePath);
		RrdDef rrdDef = rrdSource.getRrdDef();
		rrdDef.setPath(destPath);
		rrdDef.removeDatasource(dsName);
		RrdDb rrdDest = new RrdDb(rrdDef);
		rrdSource.copyStateTo(rrdDest);
		rrdSource.close();
		rrdDest.close();
	}

	/**
	 * <p>Removes single datasource from a RRD file.</p>
	 * <p>WARNING: This method is potentialy dangerous! It will modify your RRD file.
	 * It is highly recommended to preserve the original RRD file (<i>saveBackup</i>
	 * should be set to <code>true</code>). The backup file will be created in the same
	 * directory as the original one with <code>.bak</code> extension added to the
	 * original name.</p>
	 * <p>Before applying this method, be sure that the specified RRD file is not in use
	 * (not open)</p>
	 * @param sourcePath path to a RRD file to remove datasource from.
	 * @param dsName Name of the Datasource to be removed from the RRD file
	 * @param saveBackup true, if backup of the original file should be created;
	 * false, otherwise
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void removeDatasource(String sourcePath, String dsName, boolean saveBackup)
		throws IOException, RrdException {
		String destPath = Util.getTmpFilename();
		removeDatasource(sourcePath, destPath, dsName);
		copyFile(destPath, sourcePath, saveBackup);
	}

	/**
	 * Creates a new RRD file with one more archive in it. RRD file is created based on the
	 * existing one (the original RRD file is not modified at all). All data from
	 * the original RRD file is copied to the new one.
	 * @param sourcePath path to a RRD file to import data from (will not be modified)
	 * @param destPath path to a new RRD file (will be created)
	 * @param newArchive Archive definition to be added to the new RRD file
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void addArchive(String sourcePath, String destPath, ArcDef newArchive)
		throws IOException, RrdException {
		if(Util.sameFilePath(sourcePath, destPath)) {
			throw new RrdException("Source and destination paths are the same");
		}
        RrdDb rrdSource = new RrdDb(sourcePath);
		RrdDef rrdDef = rrdSource.getRrdDef();
		rrdDef.setPath(destPath);
		rrdDef.addArchive(newArchive);
		RrdDb rrdDest = new RrdDb(rrdDef);
		rrdSource.copyStateTo(rrdDest);
		rrdSource.close();
		rrdDest.close();
	}

	/**
	 * <p>Adds one more archive to a RRD file.</p>
	 * <p>WARNING: This method is potentialy dangerous! It will modify your RRD file.
	 * It is highly recommended to preserve the original RRD file (<i>saveBackup</i>
	 * should be set to <code>true</code>). The backup file will be created in the same
	 * directory as the original one with <code>.bak</code> extension added to the
	 * original name.</p>
	 * <p>Before applying this method, be sure that the specified RRD file is not in use
	 * (not open)</p>
	 * @param sourcePath path to a RRD file to add datasource to.
	 * @param newArchive Archive definition to be added to the RRD file
	 * @param saveBackup true, if backup of the original file should be created;
	 * false, otherwise
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void addArchive(String sourcePath, ArcDef newArchive, boolean saveBackup)
		throws IOException, RrdException {
		String destPath = Util.getTmpFilename();
		addArchive(sourcePath, destPath, newArchive);
		copyFile(destPath, sourcePath, saveBackup);
	}

	/**
	 * Creates a new RRD file with one archive removed. RRD file is created based on the
	 * existing one (the original RRD file is not modified at all). All relevant data from
	 * the original RRD file is copied to the new one.
	 * @param sourcePath path to a RRD file to import data from (will not be modified)
	 * @param destPath path to a new RRD file (will be created)
	 * @param consolFun Consolidation function of Archive which should be removed
	 * @param steps Number of steps for Archive which should be removed
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void removeArchive(String sourcePath, String destPath, String consolFun, int steps)
		throws IOException, RrdException {
		if(Util.sameFilePath(sourcePath, destPath)) {
			throw new RrdException("Source and destination paths are the same");
		}
        RrdDb rrdSource = new RrdDb(sourcePath);
		RrdDef rrdDef = rrdSource.getRrdDef();
		rrdDef.setPath(destPath);
		rrdDef.removeArchive(consolFun, steps);
		RrdDb rrdDest = new RrdDb(rrdDef);
		rrdSource.copyStateTo(rrdDest);
		rrdSource.close();
		rrdDest.close();
	}

	/**
	 * <p>Removes one archive from a RRD file.</p>
	 * <p>WARNING: This method is potentialy dangerous! It will modify your RRD file.
	 * It is highly recommended to preserve the original RRD file (<i>saveBackup</i>
	 * should be set to <code>true</code>). The backup file will be created in the same
	 * directory as the original one with <code>.bak</code> extension added to the
	 * original name.</p>
	 * <p>Before applying this method, be sure that the specified RRD file is not in use
	 * (not open)</p>
	 * @param sourcePath path to a RRD file to add datasource to.
	 * @param consolFun Consolidation function of Archive which should be removed
	 * @param steps Number of steps for Archive which should be removed
	 * @param saveBackup true, if backup of the original file should be created;
	 * false, otherwise
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void removeArchive(String sourcePath, String consolFun, int steps,
							  boolean saveBackup) throws IOException, RrdException {
		String destPath = Util.getTmpFilename();
		removeArchive(sourcePath, destPath, consolFun, steps);
		copyFile(destPath, sourcePath, saveBackup);
	}

	private static void copyFile(String sourcePath, String destPath, boolean saveBackup)
		throws IOException {
		File source = new File(sourcePath);
		File dest = new File(destPath);
		if(saveBackup) {
			String backupPath = destPath + ".bak";
			File backup = new File(backupPath);
			deleteFile(backup);
			if(!dest.renameTo(backup)) {
				throw new IOException("Could not create backup file " + backupPath);
			}
		}
		deleteFile(dest);
		if(!source.renameTo(dest)) {
			throw new IOException("Could not create file " + destPath + " from " + sourcePath);
		}
	}

	/**
	 * Sets datasource heartbeat to a new value.
	 * @param sourcePath Path to exisiting RRD file (will be updated)
	 * @param datasourceName Name of the datasource in the specified RRD file
	 * @param newHeartbeat New datasource heartbeat
	 * @throws RrdException Thrown in case of JRobin specific error
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setDsHeartbeat(String sourcePath, String datasourceName,
		long newHeartbeat) throws RrdException, IOException {
		RrdDb rrd = new RrdDb(sourcePath);
		Datasource ds = rrd.getDatasource(datasourceName);
		ds.setHeartbeat(newHeartbeat);
		rrd.close();
	}

	/**
	 * Sets datasource min value to a new value
	 * @param sourcePath Path to exisiting RRD file (will be updated)
	 * @param datasourceName Name of the datasource in the specified RRD file
	 * @param newMinValue New min value for the datasource
	 * @param filterArchivedValues set to <code>true</code> if archived values less than
	 * <code>newMinValue</code> should be set to NaN; set to false, otherwise.
	 * @throws RrdException Thrown in case of JRobin specific error
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setDsMinValue(String sourcePath, String datasourceName,
		double newMinValue, boolean filterArchivedValues) throws RrdException, IOException {
		RrdDb rrd = new RrdDb(sourcePath);
		Datasource ds = rrd.getDatasource(datasourceName);
		ds.setMinValue(newMinValue, filterArchivedValues);
		rrd.close();
	}

	/**
	 * Sets datasource max value to a new value.
	 * @param sourcePath Path to exisiting RRD file (will be updated)
	 * @param datasourceName Name of the datasource in the specified RRD file
	 * @param newMaxValue New max value for the datasource
	 * @param filterArchivedValues set to <code>true</code> if archived values greater than
	 * <code>newMaxValue</code> should be set to NaN; set to false, otherwise.
	 * @throws RrdException Thrown in case of JRobin specific error
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setDsMaxValue(String sourcePath, String datasourceName,
		double newMaxValue, boolean filterArchivedValues) throws RrdException, IOException {
		RrdDb rrd = new RrdDb(sourcePath);
		Datasource ds = rrd.getDatasource(datasourceName);
		ds.setMaxValue(newMaxValue, filterArchivedValues);
		rrd.close();
	}

	/**
	 * Updates valid value range for the given datasource.
	 * @param sourcePath Path to exisiting RRD file (will be updated)
	 * @param datasourceName Name of the datasource in the specified RRD file
	 * @param newMinValue New min value for the datasource
	 * @param newMaxValue New max value for the datasource
	 * @param filterArchivedValues set to <code>true</code> if archived values outside
	 * of the specified min/max range should be replaced with NaNs.
	 * @throws RrdException Thrown in case of JRobin specific error
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setDsMinMaxValue(String sourcePath, String datasourceName,
		double newMinValue, double newMaxValue, boolean filterArchivedValues)
		throws RrdException, IOException {
		RrdDb rrd = new RrdDb(sourcePath);
		Datasource ds = rrd.getDatasource(datasourceName);
		ds.setMinMaxValue(newMinValue, newMaxValue, filterArchivedValues);
		rrd.close();
	}

	/**
	 * Sets single archive's X-files factor to a new value.
	 * @param sourcePath Path to existing RRD file (will be updated)
	 * @param consolFun Consolidation function of the target archive
	 * @param steps Number of sptes of the target archive
	 * @param newXff New X-files factor for the target archive
	 * @throws RrdException Thrown in case of JRobin specific error
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setArcXff(String sourcePath, String consolFun, int steps,
		double newXff) throws RrdException, IOException {
        RrdDb rrd = new RrdDb(sourcePath);
		Archive arc = rrd.getArchive(consolFun, steps);
		arc.setXff(newXff);
		rrd.close();
	}

	/**
	 * Creates new RRD file based on the existing one, but with a different
	 * size (number of rows) for a single archive. The archive to be resized
	 * is identified by its consolidation function and the number of steps.
	 * @param sourcePath Path to the source RRD file (will not be modified)
	 * @param destPath Path to the new RRD file (will be created)
	 * @param consolFun Consolidation function of the archive to be resized
	 * @param numSteps Number of steps of the archive to be resized
	 * @param newRows New archive size (number of archive rows)
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void resizeArchive(String sourcePath, String destPath, String consolFun,
							  int numSteps, int newRows)
		throws IOException, RrdException {
		if(Util.sameFilePath(sourcePath, destPath)) {
			throw new RrdException("Source and destination paths are the same");
		}
		if(newRows < 2) {
			throw new RrdException("New arcihve size must be at least 2");
		}
        RrdDb rrdSource = new RrdDb(sourcePath);
		RrdDef rrdDef = rrdSource.getRrdDef();
		ArcDef arcDef = rrdDef.findArchive(consolFun, numSteps);
		if(arcDef.getRows() != newRows) {
			arcDef.setRows(newRows);
			rrdDef.setPath(destPath);
			RrdDb rrdDest = new RrdDb(rrdDef);
			rrdSource.copyStateTo(rrdDest);
			rrdDest.close();
		}
		rrdSource.close();
	}

	/**
	 * Modifies existing RRD file, by resizing its chosen archive. The archive to be resized
	 * is identified by its consolidation function and the number of steps.
	 * @param sourcePath Path to the RRD file (will be modified)
	 * @param consolFun Consolidation function of the archive to be resized
	 * @param numSteps Number of steps of the archive to be resized
	 * @param newRows New archive size (number of archive rows)
	 * @param saveBackup true, if backup of the original file should be created;
	 * false, otherwise
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void resizeArchive(String sourcePath, String consolFun,
							  int numSteps, int newRows, boolean saveBackup)
		throws IOException, RrdException {
		String destPath = Util.getTmpFilename();
		resizeArchive(sourcePath, destPath, consolFun, numSteps, newRows);
		copyFile(destPath, sourcePath, saveBackup);
	}

	private static void deleteFile(File file) throws IOException {
		if(file.exists() && !file.delete()) {
			throw new IOException("Could not delete file: " + file.getCanonicalPath());
		}
	}

    /**
	 * Creates RrdDef object from its XML string equivalent. The format of the input string is
	 * the same as the format described in
	 * {@link #createRrdDefFromXmlFile createRrdDefFromXmlFile} method.
	 * @param xmlString XML formatted string containing complete RRD definition
	 * @return RrdDef object which can be used to create new RrdDb object
	 * @throws RrdException thrown in case of bad XML format or bad RRD definition parameters
	 * @throws IOException thrown in case of I/O error
	 */
	public RrdDef createRrdDefFromXmlString(String xmlString) throws RrdException, IOException {
        return createRrdDefFromXmlSource(new InputSource(new StringReader(xmlString)));
	}

	/**
	 * Creates RrdDef object from the file containing its XML equivalent. Here is an example
	 * of a properly formatted XML file:</p>
	 * <pre><code>
	 * &lt;rrd_def&gt;
	 *     &lt;path&gt;test.rrd&lt;/path&gt;
	 *     &lt;!-- not mandatory --&gt;
	 *     &lt;start&gt;1000123123&lt;/start&gt;
	 *     &lt;!-- not mandatory --&gt;
	 *     &lt;step&gt;150&lt;/step&gt;
	 *     &lt;!-- at least one datasource must be supplied --&gt;
	 *     &lt;datasource&gt;
	 *         &lt;name&gt;input&lt;/name&gt;
	 *         &lt;type&gt;COUNTER&lt;/type&gt;
	 *         &lt;heartbeat&gt;300&lt;/heartbeat&gt;
	 *         &lt;min&gt;0&lt;/min&gt;
	 *         &lt;max&gt;U&lt;/max&gt;
	 *     &lt;/datasource&gt;
	 *     &lt;datasource&gt;
	 *         &lt;name&gt;temperature&lt;/name&gt;
	 *         &lt;type&gt;GAUGE&lt;/type&gt;
	 *         &lt;heartbeat&gt;400&lt;/heartbeat&gt;
	 *         &lt;min&gt;U&lt;/min&gt;
	 *         &lt;max&gt;1000&lt;/max&gt;
	 *     &lt;/datasource&gt;
	 *     &lt;!-- at least one archive must be supplied --&gt;
	 *     &lt;archive&gt;
	 *         &lt;cf&gt;AVERAGE&lt;/cf&gt;
	 *         &lt;xff&gt;0.5&lt;/xff&gt;
	 *         &lt;steps&gt;1&lt;/steps&gt;
	 *         &lt;rows&gt;700&lt;/rows&gt;
	 *     &lt;/archive&gt;
	 *     &lt;archive&gt;
	 *         &lt;cf&gt;MAX&lt;/cf&gt;
	 *         &lt;xff&gt;0.6&lt;/xff&gt;
	 *         &lt;steps&gt;6&lt;/steps&gt;
	 *         &lt;rows&gt;7000&lt;/rows&gt;
	 *     &lt;/archive&gt;
     * &lt;/rrd_def&gt;
	 * </code></pre>
	 *
	 * @param filepath path containing XML formatted RRD definition (like the one given above)
	 * @return RrdDef object which can be used to create new RrdDb object
	 * @throws RrdException thrown in case of bad XML format or bad RRD definition parameters
	 * @throws IOException thrown in case of I/O error
	 */
	public RrdDef createRrdDefFromXmlFile(String filepath) throws RrdException, IOException {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(filepath);
        	return createRrdDefFromXmlSource(new InputSource(fileReader));
		}
		finally {
			if(fileReader != null) {
				fileReader.close();
			}
		}
	}

	/**
	 * Creates RrdDef object from any parsable XML source. The format of the underlying input
	 * source data must conform to the format described for the
	 * {@link #createRrdDefFromXmlFile createRrdDefFromXmlFile} method.
	 * @param inputSource parsable XML source containing complete RRD definition
	 * @return RrdDef object which can be used to create new RrdDb object
	 * @throws RrdException thrown in case of bad XML format or bad RRD definition parameters
	 * @throws IOException thrown in case of I/O error
	 */
	public RrdDef createRrdDefFromXmlSource(InputSource inputSource) throws RrdException, IOException {
		try {
			Element	root = XmlReader.getRootElement(inputSource);
			// must start with <rrd_def>
			if(!root.getTagName().equals("rrd_def")) {
				throw new RrdException("XML definition must start with <rrd_def>");
			}
			// PATH must be supplied or exception is thrown
			String path = XmlReader.getChildValue(root, "path");
			RrdDef rrdDef = new RrdDef(path);
			try {
				// START is not mandatory
				long start = XmlReader.getChildValueAsLong(root, "start");
				rrdDef.setStartTime(start);
			}
			catch(RrdException e) {	}
			try {
				// STEP is not mandatory
				long step = XmlReader.getChildValueAsLong(root, "step");
				rrdDef.setStep(step);
			}
			catch(RrdException e) {
				// NOP
			}
			// datsources
			Node[] dsNodes = XmlReader.getChildNodes(root, "datasource");
			for(int i = 0; i < dsNodes.length; i++) {
				String name = XmlReader.getChildValue(dsNodes[i], "name");
				String type = XmlReader.getChildValue(dsNodes[i], "type");
				long heartbeat = XmlReader.getChildValueAsLong(dsNodes[i], "heartbeat");
				double min = XmlReader.getChildValueAsDouble(dsNodes[i], "min");
				double max = XmlReader.getChildValueAsDouble(dsNodes[i], "max");
				rrdDef.addDatasource(name, type, heartbeat, min, max);
			}
			// archives
			Node[] arcNodes = XmlReader.getChildNodes(root, "archive");
            for(int i = 0; i < arcNodes.length; i++) {
				String consolFun = XmlReader.getChildValue(arcNodes[i], "cf");
				double xff = XmlReader.getChildValueAsDouble(arcNodes[i], "xff");
				int steps = XmlReader.getChildValueAsInt(arcNodes[i], "steps");
				int rows = XmlReader.getChildValueAsInt(arcNodes[i], "rows");
				rrdDef.addArchive(consolFun, xff, steps, rows);
			}
			return rrdDef;
		} catch (FactoryConfigurationError e) {
			throw new RrdException("XML error: " + e);
		} catch (ParserConfigurationException e) {
			throw new RrdException("XML error: " + e);
		} catch (SAXException e) {
			throw new RrdException("XML error: " + e);
		} catch(NumberFormatException e) {
			throw new RrdException("XML error: " + e);
		}
	}

	/*
	public static void main(String[] args) throws RrdException, IOException {
		String s =
			"<rrd_def>                            " +
			"	<path>test.rrd</path>             " +
			"	<start>1000000000</start>         " +
			"	<step>151</step>                  " +
			"	<datasource>                      " +
			"		<name>input</name>            " +
			"		<type>COUNTER</type>          " +
			"		<heartbeat>300</heartbeat>    " +
			"		<min>10</min>                 " +
			"		<max>U</max>                  " +
			"	</datasource>                     " +
			"	<datasource>                      " +
			"		<name>temperature</name>      " +
			"		<type>GAUGE</type>            " +
			"		<heartbeat>400</heartbeat>    " +
			"		<min>U</min>                  " +
			"		<max>1000</max>               " +
			"	</datasource>                     " +
			"	<archive>                         " +
			"		<cf>AVERAGE</cf>              " +
			"		<xff>0.5</xff>                " +
			"		<steps>1</steps>              " +
			"		<rows>700</rows>              " +
			"	</archive>                        " +
			"	<archive>                         " +
			"		<cf>MAX</cf>                  " +
			"		<xff>0.6</xff>                " +
			"		<steps>6</steps>              " +
			"		<rows>7000</rows>             " +
			"	</archive>                        " +
			"</rrd_def>                           ";
		RrdDef def = RrdToolkit.getInstance().createRrdDefFromXmlString(s);
		RrdDb db = new RrdDb(def);
		db.close();
	}
	*/
}

