/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran@codeloop.com>
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 * $Id$
 */
package org.jrobin.core.jrrd;

import java.io.*;
import java.util.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * Instances of this class model
 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/">Round Robin Database</a>
 * (RRD) files.
 *
 * @author <a href="mailto:ciaran@codeloop.com">Ciaran Treanor</a>
 * @version $Revision$
 */
public class RRDatabase {

	RRDFile rrdFile;

	// RRD file name
	private String name;
	Header header;
	ArrayList dataSources;
	ArrayList archives;
	Date lastUpdate;

	/**
	 * Creates a database to read from.
	 *
	 * @param name the filename of the file to read from.
	 * @throws IOException if an I/O error occurs.
	 */
	public RRDatabase(String name) throws IOException {
		this(new File(name));
	}

	/**
	 * Creates a database to read from.
	 *
	 * @param file the file to read from.
	 * @throws IOException if an I/O error occurs.
	 */
	public RRDatabase(File file) throws IOException {

		name = file.getName();
		rrdFile = new RRDFile(file);
		header = new Header(rrdFile);

		// Load the data sources
		dataSources = new ArrayList();

		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = new DataSource(rrdFile);

			dataSources.add(ds);
		}

		// Load the archives
		archives = new ArrayList();

		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = new Archive(this);

			archives.add(archive);
		}

		rrdFile.align();

		lastUpdate = new Date((long) (rrdFile.readInt()) * 1000);

		// Load PDPStatus(s)
		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = (DataSource) dataSources.get(i);

			ds.loadPDPStatusBlock(rrdFile);
		}

		// Load CDPStatus(s)
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);

			archive.loadCDPStatusBlocks(rrdFile, header.dsCount);
		}

		// Load current row information for each archive
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);

			archive.loadCurrentRow(rrdFile);
		}

		// Now load the data
		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);

			archive.loadData(rrdFile, header.dsCount);
		}
	}

	/**
	 * Returns the <code>Header</code> for this database.
	 *
	 * @return the <code>Header</code> for this database.
	 */
	public Header getHeader() {
		return header;
	}

	/**
	 * Returns the date this database was last updated. To convert this date to
	 * the form returned by <code>rrdtool last</code> call Date.getTime() and
	 * divide the result by 1000.
	 *
	 * @return the date this database was last updated.
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Returns the <code>DataSource</code> at the specified position in this database.
	 *
	 * @param index index of <code>DataSource</code> to return.
	 * @return the <code>DataSource</code> at the specified position in this database
	 */
	public DataSource getDataSource(int index) {
		return (DataSource) dataSources.get(index);
	}

	/**
	 * Returns an iterator over the data sources in this database in proper sequence.
	 *
	 * @return an iterator over the data sources in this database in proper sequence.
	 */
	public Iterator getDataSources() {
		return dataSources.iterator();
	}

	/**
	 * Returns the <code>Archive</code> at the specified position in this database.
	 *
	 * @param index index of <code>Archive</code> to return.
	 * @return the <code>Archive</code> at the specified position in this database.
	 */
	public Archive getArchive(int index) {
		return (Archive) archives.get(index);
	}

	/**
	 * Returns an iterator over the archives in this database in proper sequence.
	 *
	 * @return an iterator over the archives in this database in proper sequence.
	 */
	public Iterator getArchives() {
		return archives.iterator();
	}

	/**
	 * Returns the number of archives in this database.
	 *
	 * @return the number of archives in this database.
	 */
	public int getNumArchives() {
		return header.rraCount;
	}

	/**
	 * Returns an iterator over the archives in this database of the given type
	 * in proper sequence.
	 *
	 * @param type the consolidation function that should have been applied to
	 *             the data.
	 * @return an iterator over the archives in this database of the given type
	 *         in proper sequence.
	 */
	public Iterator getArchives(ConsolidationFunctionType type) {
		return getArchiveList(type).iterator();
	}

	ArrayList getArchiveList(ConsolidationFunctionType type) {

		ArrayList subset = new ArrayList();

		for (int i = 0; i < archives.size(); i++) {
			Archive archive = (Archive) archives.get(i);

			if (archive.getType().equals(type)) {
				subset.add(archive);
			}
		}

		return subset;
	}

	/**
	 * Closes this database stream and releases any associated system resources.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void close() throws IOException {
		rrdFile.close();
	}

	/**
	 * Outputs the header information of the database to the given print stream
	 * using the default number format. The default format for <code>double</code>
	 * is 0.0000000000E0.
	 *
	 * @param s the PrintStream to print the header information to.
	 */
	public void printInfo(PrintStream s) {

		NumberFormat numberFormat = new DecimalFormat("0.0000000000E0");

		printInfo(s, numberFormat);
	}

	/**
	 * Returns data from the database corresponding to the given consolidation
	 * function and a step size of 1.
	 *
	 * @param type the consolidation function that should have been applied to
	 *             the data.
	 * @return the raw data.
	 * @throws RRDException if there was a problem locating a data archive with
	 *                      the requested consolidation function.
	 * @throws IOException  if there was a problem reading data from the database.
	 */
	public DataChunk getData(ConsolidationFunctionType type)
			throws RRDException, IOException {
		return getData(type, 1L);
	}

	/**
	 * Returns data from the database corresponding to the given consolidation
	 * function.
	 *
	 * @param type the consolidation function that should have been applied to
	 *             the data.
	 * @param step the step size to use.
	 * @return the raw data.
	 * @throws RRDException if there was a problem locating a data archive with
	 *                      the requested consolidation function.
	 * @throws IOException  if there was a problem reading data from the database.
	 */
	public DataChunk getData(ConsolidationFunctionType type, long step)
			throws RRDException, IOException {

		ArrayList possibleArchives = getArchiveList(type);

		if (possibleArchives.size() == 0) {
			throw new RRDException("Database does not contain an Archive of consolidation function type "
					+ type);
		}

		Calendar endCal = Calendar.getInstance();

		endCal.set(Calendar.MILLISECOND, 0);

		Calendar startCal = (Calendar) endCal.clone();

		startCal.add(Calendar.DATE, -1);

		long end = endCal.getTime().getTime() / 1000;
		long start = startCal.getTime().getTime() / 1000;
		Archive archive = findBestArchive(start, end, step, possibleArchives);

		// Tune the parameters
		step = header.pdpStep * archive.pdpCount;
		start -= start % step;

		if (end % step != 0) {
			end += step - end % step;
		}

		int rows = (int) ((end - start) / step + 1);

		//cat.debug("start " + start + " end " + end + " step " + step + " rows "
		//          + rows);

		// Find start and end offsets
		// todo: This is terrible - some of this should be encapsulated in Archive - CT.
		long lastUpdateLong = lastUpdate.getTime() / 1000;
		long archiveEndTime = lastUpdateLong - (lastUpdateLong % step);
		long archiveStartTime = archiveEndTime - (step * (archive.rowCount - 1));
		int startOffset = (int) ((start - archiveStartTime) / step);
		int endOffset = (int) ((archiveEndTime - end) / step);

		//cat.debug("start " + archiveStartTime + " end " + archiveEndTime
		//          + " startOffset " + startOffset + " endOffset "
		//          + (archive.rowCount - endOffset));

		DataChunk chunk = new DataChunk(start, startOffset, endOffset, step,
				header.dsCount, rows);

		archive.loadData(chunk);

		return chunk;
	}

	/*
	 * This is almost a verbatim copy of the original C code by Tobias Oetiker.
	 * I need to put more of a Java style on it - CT
	 */
	private Archive findBestArchive(long start, long end, long step,
									ArrayList archives) {

		Archive archive = null;
		Archive bestFullArchive = null;
		Archive bestPartialArchive = null;
		long lastUpdateLong = lastUpdate.getTime() / 1000;
		int firstPart = 1;
		int firstFull = 1;
		long bestMatch = 0;
		long bestPartRRA = 0;
		long bestStepDiff = 0;
		long tmpStepDiff = 0;

		for (int i = 0; i < archives.size(); i++) {
			archive = (Archive) archives.get(i);

			long calEnd = lastUpdateLong
					- (lastUpdateLong
					% (archive.pdpCount * header.pdpStep));
			long calStart = calEnd
					- (archive.pdpCount * archive.rowCount
					* header.pdpStep);
			long fullMatch = end - start;

			if ((calEnd >= end) && (calStart < start)) {    // Best full match
				tmpStepDiff = Math.abs(step - (header.pdpStep * archive.pdpCount));

				if ((firstFull != 0) || (tmpStepDiff < bestStepDiff)) {
					firstFull = 0;
					bestStepDiff = tmpStepDiff;
					bestFullArchive = archive;
				}
			} else {                                        // Best partial match
				long tmpMatch = fullMatch;

				if (calStart > start) {
					tmpMatch -= calStart - start;
				}

				if (calEnd < end) {
					tmpMatch -= end - calEnd;
				}

				if ((firstPart != 0) || (bestMatch < tmpMatch)) {
					firstPart = 0;
					bestMatch = tmpMatch;
					bestPartialArchive = archive;
				}
			}
		}

		// See how the matching went
		// todo: optimise this
		if (firstFull == 0) {
			archive = bestFullArchive;
		} else if (firstPart == 0) {
			archive = bestPartialArchive;
		}

		return archive;
	}

	/**
	 * Outputs the header information of the database to the given print stream
	 * using the given number format. The format is almost identical to that
	 * produced by
	 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrdinfo.html">rrdtool info</a>
	 *
	 * @param s            the PrintStream to print the header information to.
	 * @param numberFormat the format to print <code>double</code>s as.
	 */
	public void printInfo(PrintStream s, NumberFormat numberFormat) {

		s.print("filename = \"");
		s.print(name);
		s.println("\"");
		s.print("rrd_version = \"");
		s.print(header.version);
		s.println("\"");
		s.print("step = ");
		s.println(header.pdpStep);
		s.print("last_update = ");
		s.println(lastUpdate.getTime() / 1000);

		for (Iterator i = dataSources.iterator(); i.hasNext();) {
			DataSource ds = (DataSource) i.next();

			ds.printInfo(s, numberFormat);
		}

		int index = 0;

		for (Iterator i = archives.iterator(); i.hasNext();) {
			Archive archive = (Archive) i.next();

			archive.printInfo(s, numberFormat, index++);
		}
	}

	/**
	 * Outputs the content of the database to the given print stream
	 * as a stream of XML. The XML format is almost identical to that produced by
	 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrddump.html">rrdtool dump</a>
	 *
	 * @param s the PrintStream to send the XML to.
	 */
	public void toXml(PrintStream s) {

		s.println("<!--");
		s.println("  -- Round Robin RRDatabase Dump ");
		s.println("  -- Generated by jRRD <ciaran@codeloop.com>");
		s.println("  -->");
		s.println("<rrd>");
		s.print("\t<version> ");
		s.print(header.version);
		s.println(" </version>");
		s.print("\t<step> ");
		s.print(header.pdpStep);
		s.println(" </step> <!-- Seconds -->");
		s.print("\t<lastupdate> ");
		s.print(lastUpdate.getTime() / 1000);
		s.print(" </lastupdate> <!-- ");
		s.print(lastUpdate.toString());
		s.println(" -->");
		s.println();

		for (int i = 0; i < header.dsCount; i++) {
			DataSource ds = (DataSource) dataSources.get(i);

			ds.toXml(s);
		}

		s.println("<!-- Round Robin Archives -->");

		for (int i = 0; i < header.rraCount; i++) {
			Archive archive = (Archive) archives.get(i);

			archive.toXml(s);
		}

		s.println("</rrd>");
	}

	/**
	 * Returns a summary the contents of this database.
	 *
	 * @return a summary of the information contained in this database.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer("\n");

		sb.append(header.toString());

		for (Iterator i = dataSources.iterator(); i.hasNext();) {
			DataSource ds = (DataSource) i.next();

			sb.append("\n\t");
			sb.append(ds.toString());
		}

		for (Iterator i = archives.iterator(); i.hasNext();) {
			Archive archive = (Archive) i.next();

			sb.append("\n\t");
			sb.append(archive.toString());
		}

		return sb.toString();
	}
}
