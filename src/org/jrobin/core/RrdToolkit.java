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

import java.io.IOException;
import java.io.File;
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

	private static void deleteFile(File file) throws IOException {
		if(file.exists() && !file.delete()) {
			throw new IOException("Could not delete file: " + file.getCanonicalPath());
		}
	}

    public static void main(String[] args) throws RrdException, IOException {
		String file = "c:/test.rrd";
		RrdToolkit tool = RrdToolkit.getInstance();
		DsDef dsDef1 = new DsDef("XXX", "GAUGE", 666, -1, Double.NaN);
		DsDef dsDef2 = new DsDef("YYY", "GAUGE", 777, +1, Double.NaN);
		tool.addDatasource(file, dsDef1, true);
		tool.addDatasource(file, dsDef2, false);
		tool.removeDatasource(file, "ftpUsers", false);
		tool.removeDatasource(file, "XXX", false);
		ArcDef arcDef1 = new ArcDef("LAST", 0.22222, 13, 567);
		tool.addArchive(file, arcDef1, false);
		tool.removeArchive(file, "MAX", 6, false);
	}
}

