package org.jrobin.core;

import java.io.IOException;
import java.nio.channels.FileLock;

/**
 * JRobin backend which is used to store RRD data to ordinary files on the disk. This backend
 * is SAFE: it locks the underlying RRD file during update/fetch operations, and caches only static 
 * parts of a RRD file in memory. Therefore, this backend is safe to be used when RRD files should
 * be shared between several JVMs at the same time. However, this backend is *slow* since it does
 * not use fast java.nio.* package (it's still based on the RandomAccessFile class).
 */
public class RrdSafeFileBackend extends RrdFileBackend {
	/** Number of locking retries. This backend will not give up if the first locking request fails. */
	public static final int LOCK_RETRY_COUNT = 40;
	/**	Number of milliseconds between locking retries */
	public static final int LOCK_RETRY_DELAY = 50; // milliseconds

	private FileLock lock;

	/**
	 * Creates RrdFileBackend object for the given file path, backed by RandomAccessFile object.
	 * @param path Path to a file
	 * @param readOnly True, if file should be open in a read-only mode. False otherwise
	 * @param lockMode Ignored, since this backend implements its own locking mechanism
	 * @throws IOException Thrown in case of I/O error
	 */
	public RrdSafeFileBackend(String path, boolean readOnly, int lockMode) throws IOException {
		// this backend implements its own locking mechanism - we'll simply ignore the suggested lockMode
		super(path, readOnly, RrdDb.NO_LOCKS);
	}

	private void lockFile() throws IOException {
		if(lock != null) {
			// lock already obtained
			return;
		}
		for(int i = 0; i < LOCK_RETRY_COUNT; i++) {
			lock = channel.tryLock();
			if(lock != null) {
				return;
			}
			try {
				Thread.sleep(LOCK_RETRY_DELAY);
			}
			catch (InterruptedException e) {
				// NOP
			}
		}
		throw new IOException("Could not obtain lock on file: " + getPath() +
				"] after " + LOCK_RETRY_COUNT + " consecutive retries");
	}

	private void unlockFile() throws IOException {
		if(lock != null) {
			lock.release();
			lock = null;
		}
	}

	/**
	 * Defines the caching policy for this backend.
	 * @return <code>false</code>
	 */
	protected boolean isCachingAllowed() {
		return false;
	}

	/**
	 * Locks the underlying RRD file just before the next RRD update operation.
	 * @throws IOException Thrown if the file lock could not be obtained.
	 */
	protected void beforeUpdate() throws IOException {
		super.beforeUpdate();
		lockFile();
	}

	/**
	 * Unlocks the underlying RRD file just after the RRD update operation.
	 * @throws IOException Thrown if the lock could not be released.
	 */
	protected void afterUpdate() throws IOException {
		unlockFile();
		super.afterUpdate();
	}

	/**
	 * Locks the underlying RRD file just before the next RRD fetch operation.
	 * @throws IOException Thrown if the file lock could not be obtained.
	 */
	protected void beforeFetch() throws IOException {
		super.beforeFetch();
		lockFile();
	}

	/**
	 * Unlocks the underlying RRD file just after the RRD fetch operation.
	 * @throws IOException Thrown if the lock could not be released.
	 */
	protected void afterFetch() throws IOException {
		unlockFile();
		super.afterFetch();
	}

	/**
	 * Releases the file lock before closing the RRD file (if not already released).
	 * @throws IOException Thrown if the lock could not be released.
	 */
	protected void beforeClose() throws IOException {
		// just in case
		unlockFile();
		super.beforeClose();
	}
}
