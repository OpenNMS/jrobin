package org.jrobin.core;

import java.io.IOException;

/**
 * Factory class which creates actual {@link RrdSafeFileBackend} objects.
 */
public class RrdSafeFileBackendFactory extends RrdFileBackendFactory {
	/** factory name, "SAFE" */
	public static final String NAME = "SAFE";

	/**
	 * Creates RrdSafeFileBackend object for the given file path.
	 * @param path File path
	 * @param readOnly True, if the file should be accessed in read/only mode.
	 * False otherwise.
	 * @param lockMode This parameter is ignored since this backend implements its own
	 * locking mechanism.
	 * @return RrdSafeFileBackend object which handles all I/O operations for the given file path
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected RrdBackend open(String path, boolean readOnly, int lockMode) throws IOException {
		return new RrdSafeFileBackend(path, readOnly, lockMode);
	}

	/**
	 * Returns the name of this factory.
	 * @return Factory name (equals to string "SAFE")
	 */
	public String getFactoryName() {
		return NAME;
	}
}
