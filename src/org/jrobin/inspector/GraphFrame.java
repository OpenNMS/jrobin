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
package org.jrobin.inspector;

import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.LinearInterpolator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.File;
import java.util.Date;

class GraphFrame extends JFrame {
	private static final Color COLOR = Color.RED;
	private static final int WIDTH = 350, HEIGHT = 200;

	private GraphPanel graphPanel = new GraphPanel();
	private RrdGraph rrdGraph;

	private String sourcePath, dsName, consolFun;
	private int dsIndex, arcIndex, arcSteps;
	private long t1, t2;

	GraphFrame(String sourcePath, int dsIndex, int arcIndex) {
		this.sourcePath = sourcePath;
		this.dsIndex = dsIndex;
		this.arcIndex = arcIndex;
		readRrdFile();
		constructUI();
		pack();
		Util.placeWindow(this);
		setVisible(true);
	}

	private void readRrdFile() {
		try {
			RrdDb rrdDb = new RrdDb(sourcePath, true);
			Datasource ds = rrdDb.getDatasource(dsIndex);
			Archive arc = rrdDb.getArchive(arcIndex);
			Robin robin = arc.getRobin(dsIndex);
			dsName = ds.getDsName();
			consolFun = arc.getConsolFun();
			arcSteps = arc.getSteps();
			t1 = arc.getStartTime();
			t2 = arc.getEndTime();
			long step = arc.getArcStep();
			int count = robin.getSize();
			long[] timestamps = new long[count];
			for(int i = 0; i < count; i++) {
				timestamps[i] = t1 + i * step;
			}
			double[] values = robin.getValues();
			rrdDb.close();
			RrdGraphDef def = new RrdGraphDef(t1, t2);
			// def.datasource(dsName, sourcePath, dsName, consolFun);
			LinearInterpolator linearInterpolator = new LinearInterpolator(timestamps, values);
			linearInterpolator.setInterpolationMethod(LinearInterpolator.INTERPOLATE_RIGHT);
			def.datasource(dsName, linearInterpolator);
			def.area(dsName, COLOR, dsName + "@r");
			rrdGraph = new RrdGraph(def);
			rrdGraph.specifyImageSize(true);
		} catch (IOException e) {
			Util.error(this, e);
		} catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void constructUI() {
		JPanel content = (JPanel) getContentPane();
		Box box = Box.createVerticalBox();
		graphPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		box.add(graphPanel);
		box.add(Box.createVerticalStrut(3));
		String s1 = "START: " + t1 + " (" + new Date(t1 * 1000L) + ")";
		box.add(new JLabel(s1));
		String s2 = "END  : " + t2 + " (" + new Date(t2 * 1000L) + ")";
		box.add(new JLabel(s2));
		content.add(box);
		// finalize
		setTitle(new File(sourcePath).getName() + ":" + dsName + ":" + consolFun + ":" + arcSteps);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
	}

	private void closeWindow() {
		Util.dismissWindow(this);
	}

	class GraphPanel extends JPanel {
		public void paintComponent(Graphics g) {
			try {
				rrdGraph.renderImage((Graphics2D) g, getWidth(), getHeight());
			} catch (RrdException e) {
				Util.error(this, e);
			} catch (IOException e) {
				Util.error(this, e);
			}
		}
	}
}
