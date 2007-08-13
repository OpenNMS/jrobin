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

/**
 * Factory class which creates actual {@link RrdNioBackend} objects. This is the default factory since
 * 1.4.0 version
 */
public class RrdNioBackendFactory extends RrdFileBackendFactory{
	/** factory name, "NIO" */
	public static final String NAME = "NIO";

	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_ONCLOSE = 0; // will sync() only on close()
	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_BEFOREUPDATE = 1;
	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_AFTERUPDATE = 2;
	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_BEFOREFETCH = 3;
	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_AFTERFETCH = 4;
	/** See {@link #setSyncMode(int)} for explanation  */
	public static final int SYNC_BACKGROUND = 5;
	/**
	 * Period in seconds between consecutive synchronizations when
	 * sync-mode is set to SYNC_BACKGROUND. By default in-memory cache will be
	 * transferred to the disc every 300 seconds (5 minutes). Default value can be
	 * changed via {@link #setSyncPeriod(int)} method.
	 */
	public static final int DEFAULT_SYNC_PERIOD = 300; // seconds

	private static int syncMode = SYNC_BACKGROUND;
	private static int syncPeriod = DEFAULT_SYNC_PERIOD;

	/**
	 * Returns the current synchronization mode between backend data in memory and data
	 * in the persistent storage (disk file).
	 *
	 * @return Integer representing current synchronization mode (SYNC_ONCLOSE,
	 * SYNC_BEFOREUPDATE, SYNC_AFTERUPDATE, SYNC_BEFOREFETCH, SYNC_AFTERFETCH or
	 * SYNC_BACKGROUND). See {@link #setSyncMode(int)} for full explanation of these return values.
	 */
	public static int getSyncMode() {
		return syncMode;
	}

	/**
	 * Sets the current synchronization mode between backend data in memory (backend cache) and
	 * RRD data in the persistant storage (disk file).<p>
	 * @param syncMode Desired synchronization mode. Possible values are:<p>
	 * <ul>
	 * <li>SYNC_ONCLOSE: synchronization will be performed only when {@link RrdDb#close()}
	 * is called (RRD file is closed) or when {@link RrdDb#sync()} method is called.
	 * <li>SYNC_BEFOREUPDATE: synchronization will be performed before each {@link Sample#update()}
	 * call (right before RRD file is about to be updated).
	 * <li>SYNC_AFTERUPDATE: synchronization will be performed after each {@link Sample#update()}
	 * call (right after RRD file is updated).
	 * <li>SYNC_BEFOREFETCH: synchronization will be performed before each
	 * {@link FetchRequest#fetchData()} call (right before data is about to be fetched from a RRD file,
	 * for example for graph creation)
	 * <li>SYNC_AFTERFETCH: synchronization will be performed after each
	 * {@link FetchRequest#fetchData()} call (right after data is fetched from a RRD file)
	 * <li>SYNC_BACKGROUND (<b>default</b>): synchronization will be performed automatically
	 * from a separate thread on a regular basis. Period of time between the two consecutive
	 * synchronizations can be controlled with {@link #setSyncPeriod(int)}.
	 * </ul>
	 */
	public static void setSyncMode(int syncMode) {
		RrdNioBackendFactory.syncMode = syncMode;
	}

	/**
	 * Returns time between two consecutive background synchronizations. If not changed via
	 * {@link #setSyncPeriod(int)} method call, defaults to {@link #DEFAULT_SYNC_PERIOD}.
	 * See {@link #setSyncPeriod(int)} for more information.
	 * @return Time in seconds between consecutive background synchronizations.
	 */
	public static int getSyncPeriod() {
		return syncPeriod;
	}

	/**
	 * Sets time between consecutive background synchronizations. Method is effective only if
	 * synchronization mode is set to SYNC_BACKGROUND.
	 * @param syncPeriod Time in seconds between consecutive background synchronizations.
	 */
	public static void setSyncPeriod(int syncPeriod) {
		RrdNioBackendFactory.syncPeriod = syncPeriod;
	}

	/**
	 * Creates RrdNioBackend object for the given file path.
	 * @param path File path
	 * @param readOnly True, if the file should be accessed in read/only mode.
	 * False otherwise.
	 * @param lockMode One of the following constants: {@link RrdDb#NO_LOCKS},
	 * {@link RrdDb#EXCEPTION_IF_LOCKED} or {@link RrdDb#WAIT_IF_LOCKED}.
	 * @return RrdNioBackend object which handles all I/O operations for the given file path
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected RrdBackend open(String path, boolean readOnly, int lockMode) throws IOException {
		return new RrdNioBackend(path, readOnly, lockMode, syncMode, syncPeriod);
	}

	/**
	 * Returns the name of this factory.
	 * @return Factory name (equals to string "NIO")
	 */
	public String getFactoryName() {
		return NAME;    
	}
}
