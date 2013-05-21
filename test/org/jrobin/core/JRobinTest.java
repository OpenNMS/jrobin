/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011-2013 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JRobinTest {
    RrdDef m_def = null;
    RrdDb m_db = null;

    Random m_random = new Random();

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (!RrdBackendFactory.isInstanceCreated()) {
            RrdBackendFactory.setDefaultFactory("FILE");
        }
    }

    @Before
    public void setUp() throws Exception {
        m_def = new RrdDef("target/test.jrb");
        m_def.setStep(300);
        m_def.addDatasource("test", "GAUGE", 300L, Double.NaN, Double.NaN);
        m_def.addArchive("RRA:AVERAGE:0.5:1:2016");
    }

    @After
    public void tearDown() throws Exception {
        if (m_db != null) m_db.close();
    }

    @Test
    public void testRrdUpdate() throws Exception {
        m_db = new RrdDb(m_def);
        Sample sample = null;
        long startTime = System.currentTimeMillis() - 1000;
        // for (int i = 0; i < 1000000; i++) {
        sample = m_db.createSample();
        sample.setAndUpdate(startTime + ":" + m_random.nextInt());
        // sample.setAndUpdate(startTime++ + ":" + m_random.nextInt());
        // }
    }
    
    @Test
    public void testRrdNioBackend() throws Exception {
        final RrdNioBackendFactory factory = (RrdNioBackendFactory)RrdBackendFactory.getFactory("NIO");
        m_db = new RrdDb(m_def, factory);
        Sample sample = null;
        long startTime = System.currentTimeMillis() - 1000;
        sample = m_db.createSample();
        sample.setAndUpdate(startTime + ":" + m_random.nextInt());

        final SyncManager syncManager = factory.getSyncManager();
        assertNotNull(syncManager);
        assertNotNull(syncManager.getTimer());
        m_db.close();
        m_db = null;
        assertNull(syncManager.getTimer());
    }
}
