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
package org.jrobin.test;

import java.util.Random;
import org.jrobin.core.ArcDef;

import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JRobinRRATimePeriodTest {
	RrdDb m_db = null;
	Random m_random = new Random();

	@BeforeClass
	public static void beforeClass() throws Exception {
		if (!RrdBackendFactory.isInstanceCreated()) {
			RrdBackendFactory.setDefaultFactory("FILE");
		}
	}

  @Test
  public void testRRATimePeriod() throws Exception {
    RrdDef def = new RrdDef("target/test.jrb");
    def.setStep(1);
    def.addDatasource("test", "GAUGE", 300L, Double.NaN, Double.NaN);
    def.addArchive("RRA:AVERAGE:0.5:1s:10d");
    def.addArchive("RRA:AVERAGE:0.5:1m:90d");
    def.addArchive("RRA:AVERAGE:0.5:1h:18M");
    def.addArchive("RRA:AVERAGE:0.5:1d:10y");
    
    ArcDef[] arcs = def.getArcDefs();
    for(int i = 0; i < arcs.length; i++) {
      System.out.printf("arc[%d]: steps=%d, rows=%d\n", i, arcs[i].getSteps(), arcs[i].getRows());
    }
    Assert.assertEquals(4, arcs.length);
    Assert.assertEquals(1, arcs[0].getSteps());
    Assert.assertEquals(10 * 24 * 60 * 60, arcs[0].getRows());
    Assert.assertEquals(60, arcs[1].getSteps());
    Assert.assertEquals(90 * 24 * 60, arcs[1].getRows());
    Assert.assertEquals(60 * 60, arcs[2].getSteps());
    Assert.assertEquals(18 *  31 * 24, arcs[2].getRows());
    Assert.assertEquals(24 * 60 * 60, arcs[3].getSteps());
    Assert.assertEquals(10 * 366, arcs[3].getRows());
  }
}
