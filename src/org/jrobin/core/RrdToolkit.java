/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Nov 27, 2003
 * Time: 12:21:37 PM
 * To change this template use Options | File Templates.
 */
package org.jrobin.core;

import java.io.IOException;
/**
 * Class used to perform less important but complex operations on RRD files (like adding
 * a new datasource to a RRD file).
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
	 * Creates a new RRD file with one datasource removed. RRD file is created based on the
	 * existing one (the original RRD file is not modified at all). All remaining data from
	 * the original RRD file is copied to the new one.
	 * @param sourcePath path to a RRD file to import data from (will not be modified)
	 * @param destPath path to a new RRD file (will be created)
	 * @param dsName Name of the Datasource to be removed from the new RRD file
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of JRobin specific error
	 */

	/*
	Still buggy! Will fix later
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
	*/

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

    public static void main(String[] args) throws RrdException, IOException {
		DsDef dsDef = new DsDef("XXX", "GAUGE", 666, -1, Double.NaN);
		RrdToolkit.getInstance().addDatasource("demo.rrd", "demo2.rrd", dsDef);
		ArcDef arcDef = new ArcDef("LAST", 0.666, 77, 888);
		RrdToolkit.getInstance().addArchive("demo2.rrd", "demo3.rrd", arcDef);
	}
}

