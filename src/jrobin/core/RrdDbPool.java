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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class to represent the pool of open RRD files.
 *
 * To open already existing RRD file with JRobin, you have to create a
 * {@link jrobin.core.RrdDb RrdDb} object by specifying RRD file path
 * as constructor argument. This operation can be time consuming
 * especially with large RRD files with many datasources and
 * several long archives.<p>
 *
 * In a multithreaded environment you might probably need a reference to the
 * same RRD file from two different threads (RRD file updates are performed in
 * one thread but data fetching and graphing is performed in another one). To make
 * the RrdDb construction process more efficient it might be convenient to open all
 * RRD files in a centralized place. That's the purpose of RrdDbPool class.<p>
 *
 * How does it work? The typical usage scenario goes like this:<p>
 *
 * <pre>
 * // obtain instance to RrdDbPool object
 * RrdDbPool pool = RrdDbPool.getInstance();
 *
 * // request a reference to RrdDb object
 * String path = "some_relative_or_absolute_path_to_any_RRD_file";
 * RrdDb rrdDb = RrdDbPool.requestRrdDb(path);
 *
 * // reference obtained, do whatever you want with it...
 * ...
 * ...
 *
 * // once you don't need the reference, release it.
 * // DO NOT CALL rrdDb.close() - files no longer in use are eventually closed by the pool
 * pool.release(rrdDb);
 *</pre>
 *
 * It's that simple. When the reference is requested for
 * the first time, RrdDbPool will open the RRD file
 * for you and make some internal note that the RRD file is used only once. When the reference
 * to the same file (same RRD file path) is requested for the second time, the same RrdDb
 * reference will be returned, and its usage count will be increased by one. When the
 * reference is released its usage count will be decremented by one.<p>
 *
 * When the reference count drops to zero, RrdDbPool will not close the underlying
 * RRD file immediatelly. Instead of it, it will be marked as 'eligible for closing'.
 * If someone request the same RRD file again (before it gets closed), the same
 * reference will be returned again.<p>
 *
 * RrdDbPool has a 'garbage collector' which gets activated only when the number of open
 * RRD files is to big (> 50). Only RRD files with a reference count equal to zero
 * will be eligible for closing. Unreleased RrdDb references will be never invalidated.
 * RrdDbPool object keeps track of the time when each RRD file
 * becomes eligible for closing so that the oldest RRD file gets closed first.<p>
 *
 * Never use close() method on the reference returned from the pool. When the reference
 * is no longer needed, return it to the pool with a {@link #release(RrdDb) release()}
 * method.<p>
 *
 * However, you are not forced to use RrdDbPool methods to obtain RrdDb references
 * to RRD files, 'ordinary' RrdDb constructors are still available. But RrdDbPool class
 * offers serious performance improvement especially in complex applications with many
 * threads and many simultaneously open RRD files.<p>
 *
 * The pool is thread-safe.
 *
 */
public class RrdDbPool {
	private static RrdDbPool ourInstance;
	private static final boolean DEBUG = false;
	/**
	 * Constant to represent the maximum number of internally open RRD files
	 * which still does not force garbage collector to run.
	 */
	public static final int OPEN_FILES_DESIRED = 50;

	private HashMap rrdMap = new HashMap();

	/**
	 * Returns an instance to RrdDbPool object. Only one such object may exist in each JVM.
	 * @return Instance to RrdDbPool object.
	 */
	public synchronized static RrdDbPool getInstance() {
		if (ourInstance == null) {
			ourInstance = new RrdDbPool();
		}
		return ourInstance;
	}

	private RrdDbPool() {
	}

	/**
	 * Returns a reference to an existing RRD file with the specified path.
	 * If the file is already open in the pool, existing reference to it will be returned.
	 * Otherwise, the file is open and a newly created reference to it is returned.
	 *
	 * @param path Relative or absolute path to a RRD file.
	 * @return Reference to a RrdDb object (RRD file).
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public synchronized RrdDb requestRrdDb(String path) throws IOException, RrdException {
		String keypath = getCanonicalPath(path);
		if(rrdMap.containsKey(keypath)) {
			// already open
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			rrdEntry.reportUsage();
			debug("EXISTING: " + rrdEntry.dump());
			return rrdEntry.getRrdDb();
		}
		else {
			// not found, open it
			RrdDb rrdDb = new RrdDb(path);
			put(keypath, rrdDb);
			return rrdDb;
		}
	}

	/**
	 * Returns a reference to a new RRD file. The new file will have the specified
	 * relative or absolute path, and its contents will be provided from the specified
	 * XML file (RRDTool comaptible).
	 * @param path Relative or absolute path to a new RRD file.
	 * @param xmlPath Relative or absolute path to an existing XML dump file (RRDTool comaptible)
	 * @return Reference to a RrdDb object (RRD file).
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public synchronized RrdDb requestRrdDb(String path, String xmlPath)
		throws IOException, RrdException {
		String keypath = getCanonicalPath(path);
		validateInactive(keypath);
		RrdDb rrdDb = new RrdDb(path, xmlPath);
		put(keypath, rrdDb);
		return rrdDb;
	}

	/**
	 * Returns a reference to a new RRD file. The new file will be created based on the
	 * definition contained in a RrdDef object.
	 * @param rrdDef RRD definition object
	 * @return Reference to a RrdDb object (RRD file).
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public synchronized RrdDb requestRrdDb(RrdDef rrdDef) throws IOException, RrdException {
		String path = rrdDef.getPath();
		String keypath = getCanonicalPath(path);
		validateInactive(keypath);
		RrdDb rrdDb = new RrdDb(rrdDef);
		put(keypath, rrdDb);
		return rrdDb;
	}

	private void put(String keypath, RrdDb rrdDb) throws IOException {
		RrdEntry newEntry = new RrdEntry(rrdDb);
		debug("NEW: " + newEntry.dump());
		rrdMap.put(keypath, newEntry);
		gc();
	}

	private void validateInactive(String keypath) throws RrdException, IOException {
		if(rrdMap.containsKey(keypath)) {
			// already open, check if active (not released)
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			if(!rrdEntry.isReleased()) {
				// not released, not allowed here
				throw new RrdException("VALIDATOR: Cannot create new RrdDb file. " +
					"File " + keypath + " already active in pool");
			}
			else {
				// open but released... safe to close it
				debug("WILL BE RECREATED: " + rrdEntry.dump());
				rrdEntry.close();
				rrdMap.values().remove(rrdEntry);
			}
		}
	}

	/**
	 * Method used to report that the reference to a RRD file is no longer needed. File that
	 * is no longer needed (all references to it are released) is marked 'eligible for
	 * closing'. It will be eventually closed by the pool when the number of open RRD files
	 * becomes too big. Most recently released files will be closed last.
	 * @param rrdDb Reference to RRD file that is no longer needed.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public synchronized void release(RrdDb rrdDb) throws IOException, RrdException {
		RrdFile rrdFile = rrdDb.getRrdFile();
		if(rrdFile == null) {
			throw new RrdException("Cannot release: already closed");
		}
		String path = rrdFile.getFilePath();
		String keypath = getCanonicalPath(path);
		if(rrdMap.containsKey(keypath)) {
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			rrdEntry.release();
			debug("RELEASED: " + rrdEntry.dump());
		}
		else {
			throw new RrdException("RrdDb with path " + keypath + " not in pool");
		}
		gc();
	}

	private void gc() throws IOException {
		int mapSize = rrdMap.size();
		if(mapSize <= OPEN_FILES_DESIRED) {
			// nothing to do
			debug("GC: no need to run");
			return;
		}
		// prepare collection of released entries
		debug("GC: finding the oldest released entry");
		Iterator valueIterator = rrdMap.values().iterator();
		LinkedList releasedEntries = new LinkedList();
		while(valueIterator.hasNext()) {
			RrdEntry rrdEntry = (RrdEntry) valueIterator.next();
			if(rrdEntry.isReleased()) {
				releasedEntries.add(rrdEntry);
			}
		}
		if(releasedEntries.size() == 0) {
			debug("GC: no released entries found, nothing to do");
			return;
		}
		debug("GC: found " + releasedEntries.size() + " released entries");
		Comparator releaseDateComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				RrdEntry r1 = (RrdEntry) o1, r2 = (RrdEntry) o2;
				long diff = r1.getReleaseDate().getTime() - r2.getReleaseDate().getTime();
				return diff < 0? -1: (diff == 0? 0: +1);
			}
		};
		RrdEntry oldestEntry = (RrdEntry)
			Collections.min(releasedEntries, releaseDateComparator);
		debug("GC: oldest released entry found: " + oldestEntry.dump());
		oldestEntry.close();
		rrdMap.values().remove(oldestEntry);
		debug("GC: oldest entry closed and removed. ");
		debug("GC: number of entries reduced from " + mapSize + " to " + rrdMap.size());
	}

	protected void finalize() throws IOException {
		reset();
	}

	/**
	 * Clears the internal state of the pool entirely. All open RRD files are closed.
	 * Use with extreme caution.
	 * @throws IOException Thrown in case of I/O related error.
	 */
	public synchronized void reset() throws IOException {
		Iterator it = rrdMap.values().iterator();
		while(it.hasNext()) {
            RrdEntry rrdEntry = (RrdEntry) it.next();
			rrdEntry.close();
		}
		rrdMap.clear();
		debug("Nothing left in the pool");
	}

	static String getCanonicalPath(String path) throws IOException {
		return new File(path).getCanonicalPath();
	}

	static void debug(String msg) {
		if(DEBUG) {
			System.out.println("POOL: " + msg);
		}
	}

	/**
	 * Returns the internal state of the pool. Useful for debugging purposes.
	 * @return Internal pool state (list of open RRD files, with the number of usages for
	 * each one).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public synchronized String dump() throws IOException {
		StringBuffer buff = new StringBuffer();
		Iterator it = rrdMap.values().iterator();
		while(it.hasNext()) {
            RrdEntry rrdEntry = (RrdEntry) it.next();
			buff.append(rrdEntry.dump());
			buff.append("\n");
		}
		return buff.toString();
	}

	private class RrdEntry {
		private RrdDb rrdDb;
		private Date releaseDate;
		private int usageCount;

		public RrdEntry(RrdDb rrdDb) {
			this.rrdDb = rrdDb;
			reportUsage();
		}

		RrdDb getRrdDb() {
			return rrdDb;
		}

		void reportUsage() {
			releaseDate = null;
			usageCount++;
		}

		void release() {
			if(usageCount > 0) {
				usageCount--;
				if(usageCount == 0) {
					releaseDate = new Date();
				}
			}
		}

		boolean isReleased() {
			return usageCount == 0;
		}

		int getUsageCount() {
			return usageCount;
		}

		Date getReleaseDate() {
			return releaseDate;
		}

		void close() throws IOException {
			rrdDb.close();
		}

		String dump() throws IOException {
			String keypath = getCanonicalPath(rrdDb.getRrdFile().getFilePath());
			return keypath + " [" + usageCount + "]";
		}
	}
}

