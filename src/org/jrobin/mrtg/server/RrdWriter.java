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
package org.jrobin.mrtg.server;

import org.jrobin.core.*;
import org.jrobin.mrtg.Debug;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class RrdWriter extends Thread {
	private int sampleCount, badSavesCount, goodSavesCount;
	private List queue = Collections.synchronizedList(new LinkedList());

	private static final RrdDbPool pool = RrdDbPool.getInstance();

	private volatile boolean active = true;

	RrdWriter() {
		start();
	}

    public void run() {
		Debug.print("Archiver started");
		// the code is plain ugly but it should work
		while(active) {
           	while(active && queue.size() == 0) {
			   synchronized(this) {
				   try {
					   wait();
				   } catch (InterruptedException e) {
					   Debug.print(e.toString());
				   }
			   }
			}
			if(active && queue.size() > 0) {
				RawSample rawSample = (RawSample) queue.remove(0);
				process(rawSample);
			}
		}
		Debug.print("Archiver ended");
	}

	void terminate() {
		active = false;
		synchronized(this) {
			notify();
		}
	}

	private void process(RawSample rawSample) {
		RrdDb rrdDb = null;
		try {
			rrdDb = openRrdFileFor(rawSample);
			Sample sample = rrdDb.createSample();
			sample.setTime(rawSample.getTimestamp());
			if(rawSample.isValid()) {
				sample.setValue("in", rawSample.getIfInOctets());
				sample.setValue("out", rawSample.getIfOutOctets());
			}
			sample.update();
			goodSavesCount++;
		} catch (IOException e) {
			Debug.print(e.toString());
			badSavesCount++;
		} catch (RrdException e) {
			Debug.print(e.toString());
			badSavesCount++;
		} finally {
			try {
				pool.release(rrdDb);
			} catch (IOException e) {
				Debug.print(e.toString());
			} catch (RrdException e) {
				Debug.print(e.toString());
			}
		}
	}

	private String getRrdFilenameFor(RawSample rawSample) {
		return getRrdFilename(rawSample.getHost(), rawSample.getIfDescr());
	}

	static String getRrdFilename(String host, String ifDescr) {
		String filename = ifDescr.replaceAll("[^0-9a-zA-Z]", "_") + "@" + host + ".rrd";
		return Config.getRrdDir() + filename;
	}

	void store(RawSample sample) {
		queue.add(sample);
		sampleCount++;
		synchronized(this) {
			notify();
		}
	}

	private RrdDb openRrdFileFor(RawSample rawSample)
		throws IOException, RrdException {
		String rrdFile = getRrdFilenameFor(rawSample);
		if(new File(rrdFile).exists()) {
			return pool.requestRrdDb(rrdFile);
		}
		else {
			// create RRD file first
			final RrdDef rrdDef = new RrdDef(rrdFile);
			rrdDef.setStep(300);
			rrdDef.setStartTime(rawSample.getTimestamp() - 10);
			rrdDef.addDatasource("in", "COUNTER", 600, Double.NaN, Double.NaN);
			rrdDef.addDatasource("out", "COUNTER", 600, Double.NaN, Double.NaN);
			rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
			rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
			rrdDef.addArchive("AVERAGE", 0.5, 24, 775);
			rrdDef.addArchive("AVERAGE", 0.5, 288, 797);
			rrdDef.addArchive("MAX", 0.5, 1, 600);
			rrdDef.addArchive("MAX", 0.5, 6, 700);
			rrdDef.addArchive("MAX", 0.5, 24, 775);
			rrdDef.addArchive("MAX", 0.5, 288, 797);
			rrdDef.addArchive("MIN", 0.5, 1, 600);
			rrdDef.addArchive("MIN", 0.5, 6, 700);
			rrdDef.addArchive("MIN", 0.5, 24, 775);
			rrdDef.addArchive("MIN", 0.5, 288, 797);
			Debug.print("Created: " + rrdFile);
			return pool.requestRrdDb(rrdDef);
		}
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public int getBadSavesCount() {
		return badSavesCount;
	}

	public int getGoodSavesCount() {
		return goodSavesCount;
	}

	public int getSavesCount() {
		return getGoodSavesCount() + getBadSavesCount();
	}

}
