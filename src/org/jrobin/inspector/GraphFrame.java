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
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.File;
import java.util.Date;

class GraphFrame extends JFrame {
	private static final Color COLOR = Color.RED;
	private static final int WIDTH = 400, HEIGHT = 240;

	private Color color = COLOR;
	private GraphPanel graphPanel = new GraphPanel();
	private JComboBox graphCombo = new JComboBox();
	private RrdGraph rrdGraph;

	private String sourcePath, dsName;
	private int dsIndex, arcIndex;
	private long t1, t2;

	GraphFrame(String sourcePath, int dsIndex, int arcIndex) {
		this.sourcePath = sourcePath;
		this.dsIndex = dsIndex;
		this.arcIndex = arcIndex;
		createRrdGraph();
		fillGraphCombo();
		constructUI();
		pack();
		Util.placeWindow(this);
		setVisible(true);
	}

	private void createRrdGraph() {
		try {
			RrdDb rrdDb = new RrdDb(sourcePath, true);
			Datasource ds = rrdDb.getDatasource(dsIndex);
			Archive arc = rrdDb.getArchive(arcIndex);
			Robin robin = arc.getRobin(dsIndex);
			dsName = ds.getDsName();
			t1 = arc.getStartTime();
			t2 = arc.getEndTime();
			long step = arc.getArcStep();
			int count = robin.getSize();
			long[] timestamps = new long[count];
			for(int i = 0; i < count; i++) {
				timestamps[i] = t1 + i * step;
			}
			double[] values = robin.getValues();
			RrdDef rrdDef = rrdDb.getRrdDef();
			rrdDb.close();
			RrdGraphDef rrdGraphDef = new RrdGraphDef(t1, t2);
			rrdGraphDef.setTitle(rrdDef.getDsDefs()[dsIndex].dump() + " " +
				rrdDef.getArcDefs()[arcIndex].dump());
			LinearInterpolator linearInterpolator = new LinearInterpolator(timestamps, values);
			linearInterpolator.setInterpolationMethod(LinearInterpolator.INTERPOLATE_RIGHT);
			rrdGraphDef.datasource(dsName, linearInterpolator);
			rrdGraphDef.area(dsName, color, dsName + "@r");
			rrdGraphDef.comment("START: " + new Date(t1 * 1000L) + "@r");
			rrdGraphDef.comment("END: " + new Date(t2 * 1000L) + "@r");
			rrdGraph = new RrdGraph(rrdGraphDef);
			rrdGraph.specifyImageSize(true);
		} catch (IOException e) {
			Util.error(this, e);
		} catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void fillGraphCombo() {
		try {
			RrdDb rrdDb = new RrdDb(sourcePath, true);
			RrdDef rrdDef = rrdDb.getRrdDef();
			final DsDef[] dsDefs = rrdDef.getDsDefs();
			final ArcDef[] arcDefs = rrdDef.getArcDefs();
			GraphComboItem[] items = new GraphComboItem[rrdDef.getDsCount() * rrdDef.getArcCount()];
			int selectedItem = -1;
			for(int i = 0, k = 0; i <  rrdDef.getDsCount(); i++) {
				for(int j = 0; j < rrdDef.getArcCount(); k++, j++) {
					String description = dsDefs[i].dump() + " " + arcDefs[j].dump();
					items[k] = new GraphComboItem(description, i, j);
					if(i == dsIndex && j == arcIndex) {
						selectedItem = k;
					}
				}
			}
			graphCombo.setModel(new DefaultComboBoxModel(items));
			graphCombo.setSelectedIndex(selectedItem);
		}
		catch (IOException e) {
			Util.error(this, e);
		}
		catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void constructUI() {
		setTitle(new File(sourcePath).getName());
		JPanel content = (JPanel) getContentPane();
		content.setLayout(new BorderLayout(3, 3));
		content.add(graphCombo, BorderLayout.NORTH);
		graphPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		content.add(graphPanel, BorderLayout.CENTER);
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JButton colorButton = new JButton("Change graph color");
		southPanel.add(colorButton);
		colorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeColor();
			}
		});
		JButton saveButton = new JButton("Save graph");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveGraph();
			}
		});
		southPanel.add(Box.createHorizontalStrut(3));
		southPanel.add(saveButton);
		content.add(southPanel, BorderLayout.SOUTH);
		// EVENT HANDLERS
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}
		});
		graphCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					GraphComboItem item = (GraphComboItem) e.getItem();
					dsIndex = item.getDsIndex();
					arcIndex = item.getArcIndex();
					createRrdGraph();
					graphPanel.repaint();
				}
			}
		});
	}

	private void closeWindow() {
		Util.dismissWindow(this);
	}

	private void changeColor() {
		final JColorChooser picker = new JColorChooser(color);
		ActionListener okListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				color = picker.getColor();
				createRrdGraph();
				graphPanel.repaint();
			}
		};
		JColorChooser.createDialog(this, "Select color", true, picker, okListener, null).show();
	}

	private void saveGraph() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()? true:
					f.getAbsolutePath().toLowerCase().endsWith(".png");
			}
			public String getDescription() {
				return "PNG images";
			}
		};
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			try {
				File selectedFile = chooser.getSelectedFile();
				String path = selectedFile.getAbsolutePath();
				if(!path.toLowerCase().endsWith(".png")) {
					path += ".png";
					selectedFile = new File(path);
				}
				if(selectedFile.exists()) {
					// ask user to overwrite
					String message = "File [" + selectedFile.getName() +
						"] already exists. Do you want to overwrite it?";
                    int answer = JOptionPane.showConfirmDialog(this,
						message, "File exists", JOptionPane.YES_NO_OPTION);
					if(answer == JOptionPane.NO_OPTION) {
						return;
					}
				}
				rrdGraph.saveAsPNG(selectedFile.getAbsolutePath(),
						graphPanel.getWidth(), graphPanel.getHeight());
			} catch (IOException e) {
				Util.error(this, "Could not save graph to file:\n" + e);
			}
			catch (RrdException e) {
				Util.error(this, "Could not save graph to file:\n" + e);
			}
		}
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

	class GraphComboItem {
		private String description;
		private int dsIndex, arcIndex;

		GraphComboItem(String description, int dsIndex, int arcIndex) {
			this.description = description;
			this.dsIndex = dsIndex;
			this.arcIndex = arcIndex;
		}

		public String toString() {
			return description;
		}

		int getDsIndex() {
			return dsIndex;
		}

		int getArcIndex() {
			return arcIndex;
		}
	}
}
