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
import java.util.*;

/**
 * Class to represent the pool of open RRD files.<p>
 *
 * To open already existing RRD file with JRobin, you have to create a
 * {@link org.jrobin.core.RrdDb RrdDb} object by specifying RRD file path
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
 * RrdDbPool has a 'garbage collector' which runs in a separate
 * thread and gets activated only when the number of RRD files kept in the
 * pool is too big (greater than number returned from {@link #getCapacity getCapacity()}).
 * Only RRD files with a reference count equal to zero
 * will be eligible for closing. Unreleased RrdDb references are never invalidated.
 * RrdDbPool object keeps track of the time when each RRD file
 * becomes eligible for closing so that the oldest RRD file gets closed first.<p>
 *
 * Initial RrdDbPool capacity is set to {@link #INITIAL_CAPACITY}. Use {@link #setCapacity(int)} method to
 * change it at any time.<p>
 *
 * <b>WARNING:</b>Never use close() method on the reference returned from the pool.
 * When the reference is no longer needed, return it to the pool with the
 * {@link #release(RrdDb) release()} method.<p>
 *
 * However, you are not forced to use RrdDbPool methods to obtain RrdDb references
 * to RRD files, 'ordinary' RrdDb constructors are still available. But RrdDbPool class
 * offers serious performance improvement especially in complex applications with many
 * threads and many simultaneously open RRD files.<p>
 *
 * The pool is thread-safe.<p>
 *
 * <b>WARNING:</b> The pool cannot be used to manipulate RrdDb objects
 * with {@link RrdBackend backends} different from default.<p>
 */
public class RrdDbPool implements Runnable {
	private static RrdDbPool ourInstance;
	private static final boolean DEBUG = false;

	/**
	 * Constant to represent the maximum number of internally open RRD files
	 * which still does not force garbage collector (the process which closes RRD files) to run.
	 */
	public static final int INITIAL_CAPACITY = 100;
	private int capacity = INITIAL_CAPACITY;

	private Map rrdMap = new HashMap();
	private List rrdGcList = new LinkedList();
	private RrdBackendFactory factory;
	private int poolHitsCount, poolRequestsCount;

	/**
	 * Returns an instance to RrdDbPool object. Only one such object may exist in each JVM.
	 * @return Instance to RrdDbPool object.
	 */
	public synchronized static RrdDbPool getInstance() {
		if (ourInstance == null) {
			ourInstance = new RrdDbPool();
			ourInstance.startGarbageCollector();
		}
		return ourInstance;
	}

	private RrdDbPool() {
	}

	private void startGarbageCollector() {
		Thread gcThread = new Thread(this);
		gcThread.setDaemon(true);
		gcThread.start();
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
		RrdDb rrdDbRequested;
		if (rrdMap.containsKey(keypath)) {
			// already open
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			reportUsage(rrdEntry);
			debug("EXISTING: " + rrdEntry.dump());
			rrdDbRequested = rrdEntry.getRrdDb();
			poolHitsCount++;
		} else {
			// not found, open it
			RrdDb rrdDb = new RrdDb(path, getFactory());
			addRrdEntry(keypath, rrdDb);
			rrdDbRequested = rrdDb;
		}
		poolRequestsCount++;
		return rrdDbRequested;
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
		prooveInactive(keypath);
		RrdDb rrdDb = new RrdDb(path, xmlPath, getFactory());
		addRrdEntry(keypath, rrdDb);
		poolRequestsCount++;
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
		prooveInactive(keypath);
		RrdDb rrdDb = new RrdDb(rrdDef, getFactory());
		addRrdEntry(keypath, rrdDb);
		poolRequestsCount++;
		return rrdDb;
	}

	private void reportUsage(RrdEntry rrdEntry) {
		if(rrdEntry.reportUsage() == 1) {
			// must not be garbage collected
			rrdGcList.remove(rrdEntry);
		}
	}

	private void reportRelease(RrdEntry rrdEntry) {
		if(rrdEntry.reportRelease() == 0) {
			// ready to be garbage collected
			rrdGcList.add(rrdEntry);
		}
	}

	private void addRrdEntry(String keypath, RrdDb rrdDb) throws IOException {
		RrdEntry newEntry = new RrdEntry(rrdDb);
		reportUsage(newEntry);
		debug("NEW: " + newEntry.dump());
		rrdMap.put(keypath, newEntry);
		// notify garbage collector
		notify();
	}

	private void prooveInactive(String keypath) throws RrdException, IOException {
		if(rrdMap.containsKey(keypath)) {
			// already open, check if active (not released)
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			if(rrdEntry.isInUse()) {
				// not released, not allowed here
				throw new RrdException("VALIDATOR: Cannot create new RrdDb file. " +
					"File " + keypath + " already active in pool");
			}
			else {
				// open but released... safe to close it
				debug("WILL BE RECREATED: " + rrdEntry.dump());
				removeRrdEntry(rrdEntry);
			}
		}
	}

	private void removeRrdEntry(RrdEntry rrdEntry) throws IOException {
		rrdEntry.closeRrdDb();
		rrdMap.values().remove(rrdEntry);
		rrdGcList.remove(rrdEntry);
		debug("REMOVED: " + rrdEntry.dump());
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
		if(rrdDb == null) {
			// we don't want NullPointerException
			return;
		}
		if(rrdDb.isClosed()) {
			throw new RrdException("Cannot release: already closed");
		}
		String keypath = rrdDb.getCanonicalPath();
		if(rrdMap.containsKey(keypath)) {
			RrdEntry rrdEntry = (RrdEntry) rrdMap.get(keypath);
			reportRelease(rrdEntry);
			debug("RELEASED: " + rrdEntry.dump());
		}
		else {
			throw new RrdException("RRD file " + keypath + " not in the pool");
		}
		// notify garbage collector
		notify();
	}

	/**
	 * This method runs garbage collector in a separate thread. If the number of
	 * open RRD files kept in the pool is too big (greater than number
	 * returned from {@link #getCapacity getCapacity()}), garbage collector will try
	 * to close and remove RRD files with a reference count equal to zero.
	 * Never call this method directly.
	 */
	public void run() {
		debug("GC: started");
		synchronized (this) {
			for (; ;) {
				while (rrdMap.size() > capacity && rrdGcList.size() > 0) {
					try {
						RrdEntry oldestRrdEntry = (RrdEntry) rrdGcList.get(0);
						debug("GC: closing " + oldestRrdEntry.dump());
						removeRrdEntry(oldestRrdEntry);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					debug("GC: waiting: " +
							rrdMap.size() + " open, " +
							rrdGcList.size() + " released, " +
							"capacity = " + capacity + ", " +
							"hits = " + poolHitsCount + ", " +
							"requests = " + poolRequestsCount);
					wait();
					debug("GC: running");
				} catch (InterruptedException e) {
				}
			}
		}
	}

	protected void finalize() throws IOException {
		reset();
	}

	/**
	 * Clears the internal state of the pool entirely. All open RRD files are closed.
	 * @throws IOException Thrown in case of I/O related error.
	 */
	public synchronized void reset() throws IOException {
		Iterator it = rrdMap.values().iterator();
		while(it.hasNext()) {
            RrdEntry rrdEntry = (RrdEntry) it.next();
			rrdEntry.closeRrdDb();
		}
		rrdMap.clear();
		rrdGcList.clear();
		debug("Nothing left in the pool");
	}

	private static String getCanonicalPath(String path) throws IOException {
		return RrdFileBackend.getCanonicalPath(path);
	}

	private static void debug(String msg) {
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

	/**
	 * Returns maximum number of internally open RRD files
	 * which still does not force garbage collector to run.
	 *
	 * @return Desired nuber of open files held in the pool.
	 */
	public synchronized int getCapacity() {
		return capacity;
	}

	/**
	 * Sets maximum number of internally open RRD files
	 * which still does not force garbage collector to run.
	 *
	 * @param capacity Desired number of open files to hold in the pool
	 */
	public synchronized void setCapacity(int capacity) {
		this.capacity = capacity;
		debug("Capacity set to: " + capacity);
	}

	private RrdBackendFactory getFactory() throws RrdException {
		if(factory == null) {
			factory = RrdBackendFactory.getDefaultFactory();
			if(!(factory instanceof RrdFileBackendFactory)) {
				factory = null;
				throw new RrdException(
					"RrdDbPool cannot work with factories not derived from RrdFileBackendFactory");
			}
		}
		return factory;
	}

	private class RrdEntry {
		private RrdDb rrdDb;
		private int usageCount;

		public RrdEntry(RrdDb rrdDb) {
			this.rrdDb = rrdDb;
		}

		RrdDb getRrdDb() {
			return rrdDb;
		}

		int reportUsage() {
			assert usageCount >= 0: "Unexpected reportUsage count: " + usageCount;
			return ++usageCount;
		}

		int reportRelease() {
			assert usageCount > 0: "Unexpected reportRelease count: " + usageCount;
			return --usageCount;
		}

		boolean isInUse() {
			return usageCount > 0;
		}

		void closeRrdDb() throws IOException {
			rrdDb.close();
		}

		String dump() throws IOException {
			String keypath = rrdDb.getCanonicalPath();
			return keypath + " [" + usageCount + "]";
		}
	}

	/**
	 * Calculates pool's efficency ratio. The ratio is obtained by dividing the number of
	 * RrdDb requests served from the internal pool of open RRD files
	 * with the number of total RrdDb requests.
	 * @return Pool's efficiency ratio as a double between 1 (best) and 0 (worst). If no RrdDb reference
	 * was ever requested, 1 would be returned.
	 */
	public synchronized double getPoolEfficency() {
		if(poolRequestsCount == 0) {
			return 1.0;
		}
		double ratio = (double) poolHitsCount / (double) poolRequestsCount;
		// round to 3 decimal digits
		return Math.round(ratio * 1000.0) / 1000.0;
	}

	/**
	 * Returns the number of RRD requests served from the internal pool of open RRD files
	 * @return The number of pool "hits".
	 */
	public synchronized int getPoolHitsCount() {
		return poolHitsCount;
	}

	/**
	 * Returns the total number of RRD requests successfully served by this pool.
	 * @return Total number of RRD requests
	 */
	public synchronized int getPoolRequestsCount() {
		return poolRequestsCount;
	}
}

