/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.jrobin.demo;

import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class AppletDemo extends JApplet {
	public static final String RRD_PATH = "random";

	static {
		try {
			RrdDb.setDefaultFactory("MEMORY");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private RrdGraphDef rrdGraphDef;
	private RrdDef rrdDef;
	private long startTime = Util.getTime();

	private UpdaterThread updater;

	public void init() {
		// UI
		JPanel contentPane = (JPanel) getContentPane();
		contentPane.add(new GraphPanel(), BorderLayout.CENTER);
		JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restart();
			}
		});
		contentPane.add(restartButton, BorderLayout.SOUTH);
		// RRD
		try {
			rrdDef = new RrdDef(RRD_PATH, startTime - 1, 300);
			rrdDef.addDatasource("a", "GAUGE", 600, Double.NaN, Double.NaN);
			rrdDef.addArchive("AVERAGE", 0.5, 1, 300);
			rrdDef.addArchive("MIN", 0.5, 12, 300);
			rrdDef.addArchive("MAX", 0.5, 12, 300);
			rrdGraphDef = new RrdGraphDef(startTime, startTime + 86400);
			rrdGraphDef.setTitle("JRobin MIN/MAX demo");
			rrdGraphDef.setLowerLimit(0);
			rrdGraphDef.datasource("a", RRD_PATH, "a", "AVERAGE");
			rrdGraphDef.datasource("b", RRD_PATH, "a", "MIN");
			rrdGraphDef.datasource("c", RRD_PATH, "a", "MAX");
			rrdGraphDef.area("a", new Color(0, 0xb6, 0xe4), "real");
			rrdGraphDef.line("b", new Color(0, 0x22, 0xe9), "min", 2);
			rrdGraphDef.line("c", new Color(0, 0xee, 0x22), "max", 2);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void restart() {
		updater.terminate();
		start();
	}

	public void start() {
		updater = new UpdaterThread();
		updater.start();
	}

	public void stop() {
		updater.terminate();
	}

	class GraphPanel extends JPanel {
		protected void paintComponent(Graphics g) {
			try {
				RrdGraph graph = new RrdGraph(rrdGraphDef);
				graph.specifyImageSize(true);
				graph.renderImage((Graphics2D) g, getWidth(), getHeight());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class UpdaterThread extends Thread {
		public static final int DELAY = 100;
		public static final int STEPS_PER_REPAINT = 5;

		private boolean shouldStop = false;
		private RrdDb rrdDb;

		public UpdaterThread() {
			try {
				this.rrdDb = new RrdDb(rrdDef);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				int steps = 0;
				for(long t = startTime; t < startTime + 300 * 300 && !shouldStop; t += 300) {
					double value = Math.sin(t / 3000.0) * 50.0 + 50.0;
					rrdDb.createSample().setTime(t).setValue(0, value).update();
					if(steps++ % STEPS_PER_REPAINT == 0) {
						repaint();
					}
					Thread.sleep(DELAY);
				}
				repaint();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		void terminate() {
			shouldStop = true;
			while(isAlive()) {
				try {
					Thread.sleep(DELAY);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
