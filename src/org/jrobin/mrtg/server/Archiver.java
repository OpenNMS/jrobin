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

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.mrtg.Debug;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

class Archiver extends Thread {
	private int sampleCount, badSavesCount, goodSavesCount;
	private LinkedList queue = new LinkedList();

    public void run() {
		while(true) {
			RawSample rawSample = null;
			synchronized(queue) {
            	while(queue.size() == 0) {
					try {
						queue.wait();
					}
					catch (InterruptedException e) {
					}
				}
				rawSample = (RawSample) queue.removeFirst();
			}
			// we have a sample for sure
			process(rawSample);
		}
	}

	private void process(RawSample rawSample) {
		String rrdFile = getRrdFilename(rawSample);
		RrdDb rrdDb = null;
		try {
			rrdDb = openRrdFile(rrdFile);
			Sample sample = rrdDb.createSample();
			sample.setTime(rawSample.getTimestamp());
			if(rawSample.isValid()) {
				sample.setValue("in", rawSample.getIfInOctets());
				sample.setValue("out", rawSample.getIfOutOctets());
			}
			sample.update();
			rrdDb.close();
			goodSavesCount++;
			Debug.print("Updated: " + rrdFile);
		} catch (Exception e) {
			badSavesCount++;
			Debug.print("Error while storing sample to RRD file " + rrdFile + ": " + e);
		}
		finally {
			if(rrdDb != null) {
				try {
					rrdDb.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private String getRrdFilename(RawSample rawSample) {
		return getRrdFilename(rawSample.getHost(), rawSample.getIfDescr());
	}

	static String getRrdFilename(String host, String ifDescr) {
		String filename = ifDescr.replaceAll("[^0-9a-zA-Z]", "_") + "@" + host + ".rrd";
		return Config.getRrdDir() + filename;
	}

	void store(RawSample sample) {
		synchronized(queue) {
			queue.add(sample);
			queue.notify();
		}
		sampleCount++;
	}

	private RrdDb openRrdFile(String rrdFile) throws IOException, RrdException {
		if(new File(rrdFile).exists()) {
			return new RrdDb(rrdFile);
		}
		else {
			// create RRD file first
			RrdDef rrdDef = new RrdDef(rrdFile);
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
			return new RrdDb(rrdDef);
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
