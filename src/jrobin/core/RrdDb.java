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

import java.io.*;

/**
 * <p>Main class used for RRD files manipulation. Use this class to perform
 * update and fetch operations on exisiting
 * RRD files. This class is also used to create new RRD file from
 * the definition (object of class {@link jrobin.core.RrdDef RrdDef}) or
 * from XML file (dumped content of RRDTool's or JRobin's RRD file).</p>
 *
 * <p>
 * Note that JRobin uses binary format different from RRDTool's format. You cannot
 * use this class to manipulate RRD files created with RRDTool. <b>However, if you perform
 * the same sequence of create, update and fetch operations, you will get exactly the same
 * results from JRobin and RRDTool.</b>
 * </p>
 *
 * <p>
 * You will not be able to use JRobin API if you are not familiar with
 * basic RRDTool concepts. Good place to start is
 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/tutorial/rrdtutorial.html">official RRD tutorial</a>
 * and relevant RRDTool man pages: <a href="../../../man/rrdcreate.html" target="man">rrdcreate</a>,
 * <a href="../../../man/rrdupdate.html" target="man">rrdupdate</a>,
 * <a href="../../../man/rrdfetch.html" target="man">rrdfetch</a> and
 * <a href="../../../man/rrdgraph.html" target="man">rrdgraph</a>.
 * For RRDTool's advanced graphing capabilities (RPN extensions), also supported in JRobin,
 * there is an excellent
 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/tutorial/cdeftutorial.html" target="man">CDEF tutorial</a>.
 * </p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class RrdDb implements RrdUpdater {
	/** See {@link #getLockMode() getLockMode()} for explanation */
	public static final int NO_LOCKS = 0;
	/** See {@link #getLockMode() getLockMode()} for explanation */
	public static final int WAIT_IF_LOCKED = 1;
	/** See {@link #getLockMode() getLockMode()} for explanation */
	public static final int EXCEPTION_IF_LOCKED = 2;

    static final String RRDTOOL = "rrdtool";
	static boolean DEBUG = false;
	static final int XML_INITIAL_BUFFER_CAPACITY = 100000; // bytes

	private RrdFile file;
	private Header header;
	private Datasource[] datasources;
	private Archive[] archives;

	/**
	 * <p>Constructor used to create new RRD file from the definition. New RRD file structure is specified by object of class
	 * {@link jrobin.core.RrdDef <b>RrdDef</b>}. RRD file is created on
	 * the disk as soon as this constructor returns. Once created, the structure and size
	 * of the newly created RRD file cannot be modified.</p>
	 *
	 * <p>Typical scenario:</p>
	 *
	 * <pre>
	 * // create new RRD definition
     * RrdDef def = new RrdDef("test.rrd", 300);
     * def.addDatasource("input", "COUNTER", 600, 0, Double.NaN);
     * def.addDatasource("output", "COUNTER", 600, 0, Double.NaN);
     * def.addArchive("AVERAGE", 0.5, 1, 600);
     * def.addArchive("AVERAGE", 0.5, 6, 700);
     * def.addArchive("AVERAGE", 0.5, 24, 797);
     * def.addArchive("AVERAGE", 0.5, 288, 775);
     * def.addArchive("MAX", 0.5, 1, 600);
     * def.addArchive("MAX", 0.5, 6, 700);
     * def.addArchive("MAX", 0.5, 24, 797);
     * def.addArchive("MAX", 0.5, 288, 775);
     *
     * // RRD file definition is now completed, create the database!
     * RrdDb rrd = new RrdDb(def);
	 * // new RRD file has been created on your disk
	 * </code>
	 *
	 * @param rrdDef Object describing the structure of the new RRD file.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown if invalid RrdDef object is supplied.
	 */
	public RrdDb(RrdDef rrdDef) throws IOException, RrdException {
		rrdDef.validate();
		initializeSetup(rrdDef.getPath());
		// create header
		header = new Header(this, rrdDef);
		// create datasources
        DsDef[] dsDefs = rrdDef.getDsDefs();
		datasources = new Datasource[dsDefs.length];
		for(int i = 0; i < dsDefs.length; i++) {
			datasources[i] = new Datasource(this, dsDefs[i]);
		}
		// create archives
		ArcDef[] arcDefs = rrdDef.getArcDefs();
		archives = new Archive[arcDefs.length];
		for(int i = 0; i < arcDefs.length; i++) {
			archives[i] = new Archive(this, arcDefs[i]);
		}
		// finalize
		finalizeSetup(true);
		Util.debug(rrdDef.getRrdToolCommand());
	}

	/**
	 * <p>Constructor used to open already existing RRD file.
	 * Obtains read/write access to RRD file so that future
	 * fetch and update operations are possible.</p>
	 *
	 * @param path Path to existing RRD file.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */

	public RrdDb(String path) throws IOException, RrdException {
		try {
			initializeSetup(path);
			// restore header
			header = new Header(this);
			// restore datasources
			int dsCount = header.getDsCount();
			datasources = new Datasource[dsCount];
			for(int i = 0; i < dsCount; i++) {
				datasources[i] = new Datasource(this);
			}
			// restore archives
			int arcCount = header.getArcCount();
			archives = new Archive[arcCount];
			for(int i = 0; i < arcCount; i++) {
				archives[i] = new Archive(this);
			}
			finalizeSetup(false);
		}
		catch(RuntimeException e) {
			throw new RrdException(e);
		}
	}

	/**
	 * <p>Constructor used to create new RRD file from XML dump. JRobin and RRDTool
	 * use the same format for XML dump and this constructor should be used to
	 * (re)create JRobin RRD file from XML. In other words, it is possible to convert
	 * RRDTool RRD files to JRobin RRD files: first, dump the content of RRDTool
	 * RRD file (use command line):</p>
	 *
	 * <code>rrdtool dump original.rrd > original.xml</code>
	 *
	 * <p>Than, use file <code>original.xml</code> to create JRobin RRD file
	 * <code>copy.rrd</code>:</p>
	 *
	 * <code>RrdDb rrd = new RrdDb("copy.rrd", "original.xml");</code>
	 *
	 * <p>See documentation for {@link #dumpXml(java.lang.String) dumpXml()} method
	 * how to convert JRobin files to RRDTool format.</p>
	 *
	 * @param rrdPath Path to RRD file which will be created on the disk
	 * @param xmlPath Path to file containing XML dump of RRDTool's or JRobin's RRD file
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */

	public RrdDb(String rrdPath, String xmlPath) throws IOException, RrdException {
		initializeSetup(rrdPath);
		XmlReader reader = new XmlReader(xmlPath);
		// create header
		header = new Header(this, reader);
		// create datasources
		datasources = new Datasource[reader.getDsCount()];
		for(int i = 0; i < datasources.length; i++) {
			datasources[i] = new Datasource(this, reader, i);
		}
		// create archives
		archives = new Archive[reader.getArcCount()];
		for(int i = 0; i < archives.length; i++) {
			archives[i] = new Archive(this, reader, i);
		}
		// XMLReader is a rather huge DOM tree, release memory ASAP
		reader = null;
		// finalize
		finalizeSetup(true);
	}


	private void initializeSetup(String path) throws IOException {
		file = new RrdFile(path);
		file.setSafeMode(true);
	}

	private void finalizeSetup(boolean newFile) throws IOException {
		if(newFile) {
			file.truncate();
		}
		else if(!file.isEndReached()) {
			throw new IOException("Extra bytes found in RRD file. Not a RRD file at all?");
		}
		file.setSafeMode(false);
	}

	/**
	 * Closes RRD file. No further operations are allowed on this RrdDb object.
	 *
	 * @throws IOException Thrown in case of I/O related error.
	 */

	public synchronized void close() throws IOException {
		if(file != null) {
			file.close();
			file = null;
		}
	}

	/**
	 * <p>Returns underlying <code>RrdFile</code> object. <code>RrdFile</code> is a light
	 * wrapper around <code>RandomAccessFile</code> class.
	 * It is used for all I/O operations on RRD files.</p>
	 *
	 * @return Underlying RrdFile object
	 */

	public RrdFile getRrdFile() {
		return file;
	}

	// TODO: ADD JAVADOC for methods made public!
	public Header getHeader() {
		return header;
	}

	public Datasource getDatasource(int dsIndex) {
		return datasources[dsIndex];
	}

	public Archive getArchive(int arcIndex) {
		return archives[arcIndex];
	}

	/**
	 * <p>Returns an array of data source names defined in RRD file.</p>
	 *
	 * @return Array of data source names.
	 * @throws IOException In case of I/O related error.
	 */
	public String[] getDsNames() throws IOException {
		int n = datasources.length;
		String[] dsNames = new String[n];
		for(int i = 0; i < n; i++) {
			dsNames[i] = datasources[i].getDsName();
		}
		return dsNames;
	}

	/**
	 * <p>Creates new sample with the given timestamp and all data source values set to
	 * 'unknown'. Use returned <code>Sample</code> object to specify
	 * datasource values for the given timestamp. See documentation for
	 * {@link jrobin.core.Sample Sample} for an explanation how to do this.</p>
	 *
	 * <p>Once populated with data source values, call Sample's
	 * {@link jrobin.core.Sample#update() update()} method to actually
	 * store sample in the RRD file associated with it.</p>
	 * @param time Sample timestamp rounded to the nearest second (without milliseconds).
	 * @return Fresh sample with the given timestamp and all data source values set to 'unknown'.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public Sample createSample(long time) throws IOException {
		return new Sample(this, time);
	}

	/**
	 * <p>Creates new sample with the current timestamp and all data source values set to
	 * 'unknown'. Use returned <code>Sample</code> object to specify
	 * datasource values for the current timestamp. See documentation for
	 * {@link jrobin.core.Sample Sample} for an explanation how to do this.</p>
	 *
	 * <p>Once populated with data source values, call Sample's
	 * {@link jrobin.core.Sample#update() update()} method to actually
	 * store sample in the RRD file associated with it.</p>
	 * @return Fresh sample with the current timestamp and all
	 * data source values set to 'unknown'.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public Sample createSample() throws IOException {
		return createSample(Util.getTime());
	}

	/**
	 * <p>Prepares fetch request to be executed on the underlying RRD file. Use returned
	 * <code>FetchRequest</code> object and its {@link jrobin.core.FetchRequest#fetch() fetch()}
	 * method to actually fetch data from the RRD file.</p>
	 * @param consolFun Consolidation function to be used in fetch request. Allowed values are
	 * "AVERAGE", "MIN", "MAX" and "LAST".
	 * @param fetchStart Starting timestamp for fetch request.
	 * @param fetchEnd Ending timestamp for fetch request.
	 * @param resolution Fetch resolution (see RRDTool's
	 * <a href="../../man/rrdfetch.html" target="man">rrdfetch man page</a> for an
	 * explanation of this parameter.
	 * @return Request object that should be used to actually fetch data from RRD file.
	 * @throws RrdException In case of JRobin related error (invalid consolidation function or
	 * invalid time span).
	 */
	public FetchRequest createFetchRequest(String consolFun, long fetchStart, long fetchEnd,
		long resolution) throws RrdException {
        return new FetchRequest(this, consolFun, fetchStart, fetchEnd, resolution);
	}

	/**
	 * <p>Prepares fetch request to be executed on the underlying RRD file. Use returned
	 * <code>FetchRequest</code> object and its {@link jrobin.core.FetchRequest#fetch() fetch()}
	 * method to actually fetch data from the RRD file. Data will be fetched with the smallest
	 * possible resolution (see RRDTool's
	 * <a href="../../../man/rrdfetch.html" target="man">rrdfetch man page</a>
	 * for the explanation of the resolution parameter).</p>
	 *
	 * @param consolFun Consolidation function to be used in fetch request. Allowed values are
	 * "AVERAGE", "MIN", "MAX" and "LAST".
	 * @param fetchStart Starting timestamp for fetch request.
	 * @param fetchEnd Ending timestamp for fetch request.
	 * explanation of this parameter.
	 * @return Request object that should be used to actually fetch data from RRD file.
	 * @throws RrdException In case of JRobin related error (invalid consolidation function or
	 * invalid time span).
	 */
	public FetchRequest createFetchRequest(String consolFun, long fetchStart, long fetchEnd)
		throws RrdException {
        return createFetchRequest(consolFun, fetchStart, fetchEnd, 1);
	}

	synchronized void store(Sample sample) throws IOException, RrdException {
		long newTime = sample.getTime();
		long lastTime = header.getLastUpdateTime();
		if(lastTime >= newTime) {
			throw new RrdException("Bad sample timestamp " + newTime +
				". Last update time was " + lastTime + ", at least one second step is required");
		}
		double[] newValues = sample.getValues();
        for(int i = 0; i < datasources.length; i++) {
			double newValue = newValues[i];
			datasources[i].process(newTime, newValue);
		}
		header.setLastUpdateTime(newTime);
		Util.debug(sample.getRrdToolCommand());
	}

	synchronized FetchPoint[] fetch(FetchRequest request) throws IOException, RrdException {
		Archive archive = findMatchingArchive(request);
		FetchPoint[] points = archive.fetch(request);
		Util.debug(request.getRrdToolCommand());
		return points;
	}

	private Archive findMatchingArchive(FetchRequest request) throws IOException, RrdException {
		String consolFun = request.getConsolFun();
		long fetchStart = request.getFetchStart();
		long fetchEnd = request.getFetchEnd();
		long resolution = request.getResolution();
		Archive bestFullMatch = null, bestPartialMatch = null;
		long bestStepDiff = 0, bestMatch = 0;
		for(int i = 0; i < archives.length; i++) {
            if(archives[i].getConsolFun().equals(consolFun)) {
				long arcStep = archives[i].getArcStep();
				long arcStart = archives[i].getStartTime() - arcStep;
                long arcEnd = archives[i].getEndTime();
                long fullMatch = fetchEnd - fetchStart;
                // best full match
                if(arcEnd >= fetchEnd && arcStart <= fetchStart) {
					long tmpStepDiff = Math.abs(archives[i].getArcStep() - resolution);
                    if(bestFullMatch == null || tmpStepDiff < bestStepDiff) {
                        bestStepDiff = tmpStepDiff;
						bestFullMatch = archives[i];
					}
				}
				// best partial match
				else {
                    long tmpMatch = fullMatch;
                    if(arcStart > fetchStart) {
                        tmpMatch -= (arcStart - fetchStart);
					}
					if(arcEnd < fetchEnd) {
						tmpMatch -= (fetchEnd - arcEnd);
					}
                    if(bestPartialMatch == null || bestMatch < tmpMatch) {
                        bestPartialMatch = archives[i];
						bestMatch = tmpMatch;
					}
				}
			}
		}
		if(bestFullMatch != null) {
			return bestFullMatch;
		}
		else if(bestPartialMatch != null) {
			return bestPartialMatch;
		}
		else {
			throw new RrdException("RRD file does not contain RRA:" + consolFun + " archive");
		}
	}

	/**
	 * <p>Returns string representing complete internal state of RRD file. The returned
	 * string can be printed to <code>stdout</code> and/or used for debugging purposes.</p>
	 * @return String representing internal state of RRD file.
	 * @throws IOException Thrown in case of I/O related error.
	 */
	public synchronized String dump() throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append(header.dump());
		for(int i = 0; i < datasources.length; i++) {
			buffer.append(datasources[i].dump());
		}
		for(int i = 0; i < archives.length; i++) {
			buffer.append(archives[i].dump());
		}
		return buffer.toString();
	}

	void archive(Datasource datasource, double value, long numUpdates)
		throws IOException, RrdException {
		int dsIndex = getDsIndex(datasource.getDsName());
		for(int i = 0; i < archives.length; i++) {
			archives[i].archive(dsIndex, value, numUpdates);
		}
	}

	/**
	 * <p>Returns internal index number for the given datasource name. This index is heavily
	 * used by jrobin.graph package and has no value outside of it.</p>
	 * @param dsName Data source name.
	 * @return Internal index of the given data source name in RRD file.
	 * @throws IOException Thrown in case of I/O related error.
	 * @throws RrdException Thrown in case of JRobin related error (invalid data source name,
	 * for example)
	 */
	public int getDsIndex(String dsName) throws IOException, RrdException {
		for(int i = 0; i < datasources.length; i++) {
			if(datasources[i].getDsName().equals(dsName)) {
				return i;
			}
		}
		throw new RrdException("Unknown datasource name: " + dsName);
	}

	Datasource[] getDatasources() {
		return datasources;
	}

	Archive[] getArchives() {
		return archives;
	}

	/**
	 * <p>Writes the content of RRD file to OutputStream using XML format. This format
	 * is fully compatible with RRDTool's XML dump format and can be used for conversion
	 * purposes or debugging.</p>
	 * @param destination Output stream to receive XML data
	 * @throws IOException Thrown in case of I/O related error
	 */
	public synchronized void dumpXml(OutputStream destination) throws IOException {
		XmlWriter writer = new XmlWriter(destination);
		writer.startTag("rrd");
		// dump header
		header.appendXml(writer);
		// dump datasources
		for(int i = 0; i < datasources.length; i++) {
			datasources[i].appendXml(writer);
		}
		// dump archives
		for(int i = 0; i < archives.length; i++) {
			archives[i].appendXml(writer);
		}
		writer.closeTag();
		writer.finish();
	}

	/**
	 * <p>Returns string representing internal state of RRD file in XML format. This format
	 * is fully compatible with RRDTool's XML dump format and can be used for conversion
	 * purposes or debugging.</p>
	 * @return Internal state of RRD file in XML format.
	 * @throws IOException Thrown in case of I/O related error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public synchronized String getXml() throws IOException, RrdException {
		ByteArrayOutputStream destination = new ByteArrayOutputStream(XML_INITIAL_BUFFER_CAPACITY);
		dumpXml(destination);
		return destination.toString();
	}

	/**
	 * <p>Dumps internal state of RRD file to XML file.
	 * Use this XML file to convert your JRobin RRD file to RRDTool format.</p>
	 *
	 * <p>Suppose that you have a JRobin RRD file <code>original.rrd</code> and you want
	 * to convert it to RRDTool format. First, execute the following java code:</p>
	 *
	 * <code>RrdDb rrd = new RrdDb("original.rrd");
	 * rrd.dumpXml("original.xml");</code>
	 *
	 * <p>Use <code>original.xml</code> file to create the corresponding RRDTool file
	 * (from your command line):
	 *
	 * <code>rrdtool restore copy.rrd original.xml</code>
	 *
	 * @param filename Path to XML file which will be created.
	 * @throws IOException Thrown in case of I/O related error.
	 * @throws RrdException Thrown in case of JRobin related error.
	 */

	public synchronized void dumpXml(String filename) throws IOException, RrdException {
		OutputStream destination = new FileOutputStream(filename, false);
		dumpXml(destination);
		destination.close();
	}

	/**
	 * Returns time of last update operation as timestamp (in seconds).
	 * @return Last update time (in seconds).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getLastUpdateTime() throws IOException {
		return header.getLastUpdateTime();
	}

	/**
	 * <p>Returns RRD definition object which can be used to create new RRD file
	 * with the same creation parameters but with no data in it.</p>
	 *
	 * <p>Example:</p>
	 *
	 * <pre>
	 * RrdDb rrd1 = new RrdDb("original.rrd");
	 * RrdDef def = rrd1.getRrdDef();
	 * // fix path
	 * def.setPath("empty_copy.rrd");
	 * // create new RRD file
	 * RrdDb rrd2 = new RrdDb(def);
	 * </pre>
	 * @return RRD file definition.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdDef getRrdDef() throws IOException, RrdException {
		// set header
		long startTime = header.getLastUpdateTime();
		long step = header.getStep();
		String path = getRrdFile().getFilePath();
		RrdDef rrdDef = new RrdDef(path, startTime, step);
		// add datasources
		for(int i = 0; i < datasources.length; i++) {
			DsDef dsDef = new DsDef(datasources[i].getDsName(),
				datasources[i].getDsType(), datasources[i].getHeartbeat(),
				datasources[i].getMinValue(), datasources[i].getMaxValue());
			rrdDef.addDatasource(dsDef);
		}
		// add archives
		for(int i = 0; i < archives.length; i++) {
            ArcDef arcDef = new ArcDef(archives[i].getConsolFun(),
				archives[i].getXff(), archives[i].getSteps(), archives[i].getRows());
			rrdDef.addArchive(arcDef);
		}
		return rrdDef;
	}

	/**
	 * <p>Returns current lock mode. This function can return one of the following values:
	 * <ul>
	 * <li><code>NO_LOCKS</code>: RRD files are not locked (default). Simultaneous access
	 * to the same RRD file is allowed. This locking mode provides fastest read/write
	 * (fetch/update) operations, but could lead to inconsisten RRD data.</li>
	 * <li><code>WAIT_IF_LOCKED</code>: RRD files are locked exclusively.
	 * Simultaneous access to the same underlying RRD file is not allowed.
	 * If a <code>RrdDb</code> object tries to access already locked RRD file,
	 * it will wait until the lock is released. The lock is released when
	 * the {@link #close() close()} method is called.</li>
	 * <li><code>EXCEPTION_IF_LOCKED</code>: RRD files are locked exclusively.
	 * Simultaneous access to the same underlying RRD file is not allowed.
	 * If a <code>RrdDb</code> object tries to access already locked RRD file,
	 * an exception is thrown.</li></p>
	 * </ul>
	 * <p>Note that <code>WAIT_IF_LOCKED</code> and <code>EXCEPTION_IF_LOCKED</code>
	 * modes guarantee data consistency but affect the speed of read/write operations.
	 * Call {@link #close() close()} as soon as possible in your code to avoid long wait states
	 * (or locking exceptions). </p>
	 * @return The current locking behaviour.
	 */
	public static int getLockMode() {
		return RrdFile.getLockMode();
	}

	/**
	 * Sets the current locking mode. See {@link #getLockMode() getLockMode()} for more
	 * information.
	 * @param lockMode Lock mode. Valid values are <code>NO_LOCKS</code>,
	 * <code>WAIT_IF_LOCKED</code> and <code>EXCEPTION_IF_LOCKED</code>.
	 */
	public static void setLockMode(int lockMode) {
		RrdFile.setLockMode(lockMode);
	}

	protected void finalize() throws Throwable {
		close();
	}

}
