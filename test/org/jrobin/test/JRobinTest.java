package org.jrobin.test;

import java.util.Random;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JRobinTest {
	RrdDb m_db = null;
	Random m_random = new Random();

	@Before
	public void setUp() throws Exception {
		RrdDb.setDefaultFactory("FILE");
		RrdDef def = new RrdDef("target/test.jrb");
		def.setStep(300);
		def.addDatasource("test", "GAUGE", 300L, Double.NaN, Double.NaN);
		def.addArchive("RRA:AVERAGE:0.5:1:2016");
		m_db = new RrdDb(def);
	}

	@After
	public void tearDown() throws Exception {
		m_db.close();
	}

	@Test
	public void testRrdUpdate() throws Exception {
		Sample sample = null;
		long startTime = System.currentTimeMillis() - 1000;
//		for (int i = 0; i < 1000000; i++) {
			sample = m_db.createSample();
			sample.setAndUpdate(startTime++ + ":" + m_random.nextInt());
//		}
	}
}
