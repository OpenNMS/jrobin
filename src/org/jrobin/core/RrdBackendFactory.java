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

import java.util.HashMap;
import java.io.IOException;

/**
 * Base (abstract) backend factory class which holds references to all concrete
 * backend factories and defines abstract methods which must be implemented in
 * all concrete factory implementations.<p>
 *
 * Factory classes are used to create concrete {@link RrdBackend} implementations.
 * Each factory creates unlimited number of specific backend objects.
 *
 * JRobin supports three different backend types (backend factories) out of the box:<p>
 * <ul>
 * <li>{@link RrdFileBackend}: objects of this class are created from the
 * {@link RrdFileBackendFactory} class. This is the default backend used in all
 * JRobin releases. It uses java.io.* package and RandomAccessFile class to store
 * RRD data in files on the disk.
 *
 * <li>{@link RrdNioBackend}: objects of this class are created from the
 * {@link RrdNioBackendFactory} class. The backend uses java.io.* and java.nio.*
 * classes (mapped ByteBuffer) to store RRD data in files on the disk.
 *
 * <li>{@link RrdMemoryBackend}: objects of this class are created from the
 * {@link RrdMemoryBackendFactory} class. This backend stores all data in memory. Once
 * JVM exits, all data gets lost. The backend is extremely fast and memory hungry.
 * </ul>
 *
 * Each backend factory is identifed by its {@link #getFactoryName() name}. Constructors
 * are provided in the {@link RrdDb} class to create RrdDb objects (RRD databases)
 * backed with a specific backend.<p>
 *
 * See javadoc for {@link RrdBackend} to find out how to create your custom backends.
 */
public abstract class RrdBackendFactory {
	private static final HashMap factories = new HashMap();
	private static RrdBackendFactory defaultFactory;

	static {
		try {
			RrdFileBackendFactory fileFactory = new RrdFileBackendFactory();
			registerFactory(fileFactory);
			RrdMemoryBackendFactory memoryFactory = new RrdMemoryBackendFactory();
			registerFactory(memoryFactory);
			RrdNioBackendFactory nioFactory = new RrdNioBackendFactory();
			registerFactory(nioFactory);

			// Here is the default backend factory
			defaultFactory = fileFactory;

		} catch (RrdException e) {
			throw new RuntimeException("FATAL: Cannot register RRD backend factories: " + e);
		}
	}

	/**
	 * Returns backend factory for the given backend factory name.
	 * @param name Backend factory name. Initially supported names are:<p>
	 * <ul>
	 * <li><b>FILE</b>: Default factory which creates backends based on the
	 * java.io.* package. RRD data is stored in files on the disk
	 * <li><b>NIO</b>: Factory which creates backends based on the
	 * java.nio.* package. RRD data is stored in files on the disk
	 * <li><b>MEMORY</b>: Factory which creates memory-oriented backends.
	 * RRD data is stored in memory, it gets lost as soon as JVM exits.
	 * </ul>
	 * @return Backend factory for the given factory name
	 * @throws RrdException Thrown if no factory with the given name
	 * is available.
	 */
	public static synchronized RrdBackendFactory getFactory(String name) throws RrdException {
		RrdBackendFactory factory = (RrdBackendFactory) factories.get(name);
		if(factory != null) {
			return factory;
		}
		else {
			throw new RrdException("No backend factory found with the name specified [" + name + "]");
		}
	}

	/**
	 * Registers new (custom) backend factory within the JRobin framework.
	 * @param factory Factory to be registered
	 * @throws RrdException Thrown if the name of the specified factory is already
	 * used.
	 */
	public static synchronized void registerFactory(RrdBackendFactory factory)
		throws RrdException {
		String name = factory.getFactoryName();
		if(!factories.containsKey(name)) {
			factories.put(name, factory);
		}
		else {
			throw new RrdException("Backend factory of this name2 (" + name +
				") already exists and cannot be registered");
		}
	}

	/**
	 * Returns the defaul backend factory. This factory is used to construct
	 * {@link RrdDb} objects if no factory is specified in the RrdDb constructor.
	 * @return Default backend factory.
	 */
	public static RrdBackendFactory getDefaultFactory() {
		return defaultFactory;
	}

	/**
	 * Creates RrdBackend object for the given storage path.
	 * @param path Storage path
	 * @param readOnly True, if the storage should be accessed in read/only mode.
	 * False otherwise.
	 * @param lockMode One of the following constants: {@link RrdDb.NO_LOCKS},
	 * {@link RrdDb.EXCEPTION_IF_LOCKED} or {@link RrdDb.WAIT_IF_LOCKED}.
	 * @return Backend object which handles all I/O operations for the given storage path
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected abstract RrdBackend open(String path, boolean readOnly, int lockMode)
			throws IOException;

	/**
	 * Method to determine if a storage with the given path already exists.
	 * @param path Storage path
	 * @return True, if such storage exists, false otherwise.
	 */
	protected abstract boolean exists(String path) throws IOException;

	/**
	 * Returns the name (primary ID) for the factory.
	 * @return Name of the factory.
	 */
	protected abstract String getFactoryName();
}
