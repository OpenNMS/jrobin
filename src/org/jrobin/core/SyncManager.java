package org.jrobin.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public final class SyncManager {
    private static final class SyncTimerTask extends TimerTask {
        private final RrdNioBackend m_rrdNioBackend;

        private SyncTimerTask(RrdNioBackend rrdNioBackend) {
            m_rrdNioBackend = rrdNioBackend;
        }

        @Override public void run() {
            m_rrdNioBackend.sync();
        }
    }

    private int m_syncPeriod = RrdNioBackendFactory.DEFAULT_SYNC_PERIOD;
    private Timer m_timer = null;
    private Map<RrdNioBackend,TimerTask> m_tasks = new HashMap<RrdNioBackend,TimerTask>();

    public SyncManager(final int syncPeriod) {
        m_syncPeriod = syncPeriod;
    }

    public int getSyncPeriod() {
        return m_syncPeriod;
    }

    public void setSyncPeriod(final int syncPeriod) {
        m_syncPeriod = syncPeriod;
        synchronized(m_tasks) {
            final Timer oldTimer = m_timer;
            m_timer = new Timer(true);
            for (final RrdNioBackend backend : m_tasks.keySet()) {
                m_tasks.get(backend).cancel();
                scheduleTask(backend);
            }
            cancelTimer(oldTimer);
        }
    }

    public void add(final RrdNioBackend rrdNioBackend) {
        synchronized(m_tasks) {
            if (m_tasks.size() == 0) {
                m_timer = new Timer(true);
            }
            scheduleTask(rrdNioBackend);
        }
    }

    public void remove(final RrdNioBackend rrdNioBackend) {
        synchronized (m_tasks) {
            final TimerTask oldTask = m_tasks.remove(rrdNioBackend);
            if (oldTask != null) oldTask.cancel();
            if (m_tasks.size() == 0) {
                cancelTimer(m_timer);
                m_timer = null;
            }
        }
    }

    public void shutdown() {
        synchronized(m_tasks) {
            for (final Map.Entry<RrdNioBackend, TimerTask> entry : m_tasks.entrySet()) {
                entry.getValue().cancel();
            }
            cancelTimer(m_timer);
        }
    }

    protected void scheduleTask(final RrdNioBackend rrdNioBackend) {
        final TimerTask task = new SyncTimerTask(rrdNioBackend);
        m_tasks.put(rrdNioBackend, task);
        m_timer.schedule(task,getSyncPeriod() * 1000L, getSyncPeriod() * 1000L);
    }

    private void cancelTimer(final Timer timer) {
        timer.cancel();
        timer.purge();
    }

    Timer getTimer() {
        return m_timer;
    }

}