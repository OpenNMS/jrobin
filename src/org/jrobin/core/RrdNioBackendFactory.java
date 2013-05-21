/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.core;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory class which creates actual {@link RrdNioBackend} objects. This is
 * the default factory since 1.4.0 version
 */
public class RrdNioBackendFactory extends RrdFileBackendFactory {
    private static ScheduledExecutorService m_executor = null;

    /**
     * factory name, "NIO"
     */
    public static final String NAME = "NIO";

    /**
     * Period in seconds between consecutive synchronizations when sync-mode
     * is set to SYNC_BACKGROUND. By default in-memory cache will be
     * transferred to the disc every 300 seconds (5 minutes). Default value
     * can be changed via {@link #setSyncPeriod(int)} method.
     */
    public static final int DEFAULT_SYNC_PERIOD = 300; // seconds

    private static int m_syncPeriod = DEFAULT_SYNC_PERIOD;

    /**
     * Returns time between two consecutive background synchronizations. If
     * not changed via {@link #setSyncPeriod(int)} method call, defaults to
     * {@link #DEFAULT_SYNC_PERIOD}. See {@link #setSyncPeriod(int)} for more
     * information.
     * 
     * @return Time in seconds between consecutive background
     *         synchronizations.
     */
    public static int getSyncPeriod() {
        return m_syncPeriod;
    }

    /**
     * Sets time between consecutive background synchronizations.
     * 
     * @param syncPeriod
     *            Time in seconds between consecutive background
     *            synchronizations.
     */
    public static void setSyncPeriod(final int syncPeriod) {
        m_syncPeriod = syncPeriod;
    }

    /**
     * Creates RrdNioBackend object for the given file path.
     * 
     * @param path
     *            File path
     * @param m_readOnly
     *            True, if the file should be accessed in read/only mode.
     *            False otherwise.
     * @return RrdNioBackend object which handles all I/O operations for the
     *         given file path
     * @throws IOException
     *             Thrown in case of I/O error.
     */
    protected RrdBackend open(final String path, final boolean readOnly) throws IOException {
        if (!readOnly && m_executor == null) {
            m_executor = Executors.newScheduledThreadPool(Integer.getInteger("org.jrobin.RrdNioBackend.syncPoolSize", 50), new NioThreadFactory());
        }
        return new RrdNioBackend(path, readOnly, m_syncPeriod, m_executor);
    }

    public void shutdown() {
        if (m_executor != null) {
            m_executor.shutdown();
            m_executor = null;
        }
    }

    /**
     * Returns the name of this factory.
     * 
     * @return Factory name (equals to string "NIO")
     */
    public String getFactoryName() {
        return NAME;
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    private static class NioThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);

        final ThreadGroup group;

        final AtomicInteger threadNumber = new AtomicInteger(1);

        final String namePrefix;

        public NioThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "RrdNioBackend-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
