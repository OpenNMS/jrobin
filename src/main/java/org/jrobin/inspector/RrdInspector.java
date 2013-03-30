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

package org.jrobin.inspector;

import org.jrobin.core.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

/**
 * Utility application (swing) to analyze, change and plot content of JRobin RRD files.
 */
public class RrdInspector extends JFrame {
	private static final long serialVersionUID = 1L;
	static final boolean SHOULD_CREATE_BACKUPS = true;
	static final String TITLE = "RRD File Inspector";
	static final boolean SHOULD_FIX_ARCHIVED_VALUES = false;
	static final Dimension MAIN_TREE_SIZE = new Dimension(250, 400);
	static final Dimension INFO_PANE_SIZE = new Dimension(450, 400);
	static final String ABOUT = "JRobinLite project\nRrdInspector utility\n" +
			"Copyright (C) 2003-2005 Sasa Markovic. All rights reserved.";

	JTabbedPane tabbedPane = new JTabbedPane();
	private JTree mainTree = new JTree();
	private JTable generalTable = new JTable();
	private JTable datasourceTable = new JTable();
	private JTable archiveTable = new JTable();
	private JTable dataTable = new JTable();

	private InspectorModel inspectorModel = new InspectorModel();

	private String lastDirectory = null;

	private RrdInspector(String path) {
		super(TITLE);
		constructUI();
		pack();
		Util.placeWindow(this);
		setVisible(true);
		if (path == null) {
			selectFile();
		}
		else {
			loadFile(new File(path));
		}
	}

	private void constructUI() {
		JPanel content = (JPanel) getContentPane();
		content.setLayout(new BorderLayout());

		// WEST, tree pane
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		JScrollPane treePane = new JScrollPane(mainTree);
		leftPanel.add(treePane);
		leftPanel.setPreferredSize(MAIN_TREE_SIZE);
		content.add(leftPanel, BorderLayout.WEST);
		mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		mainTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				nodeChangedAction();
			}
		});
		mainTree.setModel(inspectorModel.getMainTreeModel());

		////////////////////////////////////////////
		// EAST, tabbed pane
		////////////////////////////////////////////

		// GENERAL TAB
		JScrollPane spGeneral = new JScrollPane(generalTable);
		spGeneral.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("General info", spGeneral);
		generalTable.setModel(inspectorModel.getGeneralTableModel());
		generalTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		generalTable.getColumnModel().getColumn(0).setMaxWidth(150);

		// DATASOURCE TAB
		JScrollPane spDatasource = new JScrollPane(datasourceTable);
		spDatasource.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("Datasource info", spDatasource);
		datasourceTable.setModel(inspectorModel.getDatasourceTableModel());
		datasourceTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		datasourceTable.getColumnModel().getColumn(0).setMaxWidth(150);

		// ARCHIVE TAB
		JScrollPane spArchive = new JScrollPane(archiveTable);
		archiveTable.setModel(inspectorModel.getArchiveTableModel());
		archiveTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		archiveTable.getColumnModel().getColumn(0).setMaxWidth(150);
		spArchive.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("Archive info", spArchive);

		// DATA TAB
		JScrollPane spData = new JScrollPane(dataTable);
		dataTable.setModel(inspectorModel.getDataTableModel());
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		dataTable.getColumnModel().getColumn(0).setMaxWidth(100);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		dataTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			{
				setBackground(Color.YELLOW);
			}
		});
		spData.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("Archive data", spData);

		content.add(tabbedPane, BorderLayout.CENTER);

		////////////////////////////////////////
		// MENU
		////////////////////////////////////////
		JMenuBar menuBar = new JMenuBar();

		// FILE MENU
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		// Open file
		JMenuItem fileMenuItem = new JMenuItem("Open RRD file...", KeyEvent.VK_O);
		fileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		fileMenu.add(fileMenuItem);

		// Open file in new window
		JMenuItem fileMenuItem2 = new JMenuItem("Open RRD file in new window...");
		fileMenuItem2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RrdInspector(null);
			}
		});
		fileMenu.add(fileMenuItem2);
		fileMenu.addSeparator();

		// Add datasource
		JMenuItem addDatasourceMenuItem = new JMenuItem("Add datasource...");
		addDatasourceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addDatasource();
			}
		});
		fileMenu.add(addDatasourceMenuItem);

		// Edit datasource
		JMenuItem editDatasourceMenuItem = new JMenuItem("Edit datasource...");
		editDatasourceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editDatasource();
			}
		});
		fileMenu.add(editDatasourceMenuItem);

		// Remove datasource
		JMenuItem removeDatasourceMenuItem = new JMenuItem("Remove datasource");
		removeDatasourceMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDatasource();
			}
		});
		fileMenu.add(removeDatasourceMenuItem);
		fileMenu.addSeparator();

		// Add archive
		JMenuItem addArchiveMenuItem = new JMenuItem("Add archive...");
		addArchiveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addArchive();
			}
		});
		fileMenu.add(addArchiveMenuItem);

		// Edit archive
		JMenuItem editArchiveMenuItem = new JMenuItem("Edit archive...");
		editArchiveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editArchive();
			}
		});
		fileMenu.add(editArchiveMenuItem);

		// Remove archive
		JMenuItem removeArchiveMenuItem = new JMenuItem("Remove archive...");
		removeArchiveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeArchive();
			}
		});
		fileMenu.add(removeArchiveMenuItem);

		// Plot archive values
		JMenuItem plotArchiveMenuItem = new JMenuItem("Plot archive values...");
		plotArchiveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				plotArchive();
			}
		});
		fileMenu.add(plotArchiveMenuItem);
		fileMenu.addSeparator();

		// Exit
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);

		// HELP MENU
		JMenu helpMenu = new JMenu("Help");
		fileMenu.setMnemonic(KeyEvent.VK_H);

		// About
		JMenuItem aboutMenuItem = new JMenuItem("About...");
		aboutMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				about();
			}
		});
		helpMenu.add(aboutMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		// finalize UI
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

	private void about() {
		JOptionPane.showMessageDialog(this, ABOUT, "About", JOptionPane.INFORMATION_MESSAGE);
	}

	private void nodeChangedAction() {
		RrdNode rrdNode = getSelectedRrdNode();
		if (rrdNode != null) {
			inspectorModel.selectModel(rrdNode.getDsIndex(), rrdNode.getArcIndex());
			if (rrdNode.getDsIndex() >= 0 && rrdNode.getArcIndex() >= 0) {
				// archive node
				if (tabbedPane.getSelectedIndex() < 2) {
					tabbedPane.setSelectedIndex(2);
				}
			}
			else if (rrdNode.getDsIndex() >= 0) {
				tabbedPane.setSelectedIndex(1);
			}
			else {
				tabbedPane.setSelectedIndex(0);
			}
		}
	}

	private RrdNode getSelectedRrdNode() {
		TreePath path = mainTree.getSelectionPath();
		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object obj = node.getUserObject();
			if (obj instanceof RrdNode) {
				return (RrdNode) obj;
			}
		}
		return null;
	}

	private void selectFile() {
		JFileChooser chooser = new JFileChooser(lastDirectory);
		FileFilter filter = new FileFilter() {
			public boolean accept(File file) {
				String path = file.getAbsolutePath().toLowerCase();
				return file.isDirectory() || path.endsWith(".rrd") ||
						path.endsWith(".jrb") || path.endsWith(".jrobin");
			}

			public String getDescription() {
				return "JRobin RRD files (*.rrd;*.jrb;*.jrobin)";
			}
		};
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file != null) {
				lastDirectory = file.getParent();
				loadFile(file);
			}
		}
	}

	private void loadFile(File file) {
		inspectorModel.setFile(file);
		tabbedPane.setSelectedIndex(0);
	}


	private void addDatasource() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		DsDef newDsDef = new EditDatasourceDialog(this, null).getDsDef();
		if (newDsDef != null) {
			// action
			try {
				String sourcePath = inspectorModel.getFile().getCanonicalPath();
				RrdToolkit .addDatasource(sourcePath, newDsDef, SHOULD_CREATE_BACKUPS);
				inspectorModel.refresh();
				tabbedPane.setSelectedIndex(0);
			}
			catch (IOException e) {
				Util.error(this, e);
			}
			catch (RrdException e) {
				Util.error(this, e);
			}
		}
	}

	private void addArchive() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		ArcDef newArcDef = new EditArchiveDialog(this, null).getArcDef();
		if (newArcDef != null) {
			// action
			try {
				String sourcePath = inspectorModel.getFile().getCanonicalPath();
				RrdToolkit.addArchive(sourcePath, newArcDef, SHOULD_CREATE_BACKUPS);
				inspectorModel.refresh();
				tabbedPane.setSelectedIndex(0);
			}
			catch (IOException e) {
				Util.error(this, e);
			}
			catch (RrdException e) {
				Util.error(this, e);
			}
		}
	}

	private void editDatasource() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		RrdNode rrdNode = getSelectedRrdNode();
		int dsIndex;
		if (rrdNode == null || (dsIndex = rrdNode.getDsIndex()) < 0) {
			Util.error(this, "Select datasource first");
			return;
		}
		try {
			String sourcePath = inspectorModel.getFile().getCanonicalPath();
			RrdDb rrd = new RrdDb(sourcePath, true);
			try {
				DsDef dsDef = rrd.getRrdDef().getDsDefs()[dsIndex];
				rrd.close();
				DsDef newDsDef = new EditDatasourceDialog(this, dsDef).getDsDef();
				if (newDsDef != null) {
					// action!
					RrdToolkit.setDsHeartbeat(sourcePath, newDsDef.getDsName(),
							newDsDef.getHeartbeat());
					RrdToolkit.setDsMinMaxValue(sourcePath, newDsDef.getDsName(),
							newDsDef.getMinValue(), newDsDef.getMaxValue(), SHOULD_FIX_ARCHIVED_VALUES);
					inspectorModel.refresh();
					tabbedPane.setSelectedIndex(0);
				}
			}
			finally {
				rrd.close();
			}
		}
		catch (IOException e) {
			Util.error(this, e);
		}
		catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void editArchive() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		RrdNode rrdNode = getSelectedRrdNode();
		int arcIndex;
		if (rrdNode == null || (arcIndex = rrdNode.getArcIndex()) < 0) {
			Util.error(this, "Select archive first");
			return;
		}
		try {
			String sourcePath = inspectorModel.getFile().getCanonicalPath();
			RrdDb rrd = new RrdDb(sourcePath, true);
			try {
				ArcDef arcDef = rrd.getRrdDef().getArcDefs()[arcIndex];
				rrd.close();
				ArcDef newArcDef = new EditArchiveDialog(this, arcDef).getArcDef();
				if (newArcDef != null) {
					// action!
					// fix X-files factor
					RrdToolkit.setArcXff(sourcePath, newArcDef.getConsolFun(),
							newArcDef.getSteps(), newArcDef.getXff());
					// fix archive size
					RrdToolkit.resizeArchive(sourcePath, newArcDef.getConsolFun(),
							newArcDef.getSteps(), newArcDef.getRows(), SHOULD_CREATE_BACKUPS);
					inspectorModel.refresh();
					tabbedPane.setSelectedIndex(0);
				}
			}
			finally {
				rrd.close();
			}
		}
		catch (IOException e) {
			Util.error(this, e);
		}
		catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void removeDatasource() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		RrdNode rrdNode = getSelectedRrdNode();
		int dsIndex;
		if (rrdNode == null || (dsIndex = rrdNode.getDsIndex()) < 0) {
			Util.error(this, "Select datasource first");
			return;
		}
		try {
			String sourcePath = inspectorModel.getFile().getCanonicalPath(), dsName;
			RrdDb rrd = new RrdDb(sourcePath, true);
			try {
				dsName = rrd.getRrdDef().getDsDefs()[dsIndex].getDsName();
			}
			finally {
				rrd.close();
			}
			RrdToolkit.removeDatasource(sourcePath, dsName, SHOULD_CREATE_BACKUPS);
			inspectorModel.refresh();
			tabbedPane.setSelectedIndex(0);
		}
		catch (IOException e) {
			Util.error(this, e);
		}
		catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void removeArchive() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		RrdNode rrdNode = getSelectedRrdNode();
		int arcIndex;
		if (rrdNode == null || (arcIndex = rrdNode.getArcIndex()) < 0) {
			Util.error(this, "Select archive first");
			return;
		}
		try {
			String sourcePath = inspectorModel.getFile().getCanonicalPath(), consolFun;
			int steps;
			RrdDb rrd = new RrdDb(sourcePath, true);
			try {
				ArcDef arcDef = rrd.getRrdDef().getArcDefs()[arcIndex];
				consolFun = arcDef.getConsolFun();
				steps = arcDef.getSteps();
			}
			finally {
				rrd.close();
			}
			RrdToolkit.removeArchive(sourcePath, consolFun, steps, SHOULD_CREATE_BACKUPS);
			inspectorModel.refresh();
			tabbedPane.setSelectedIndex(0);
		}
		catch (IOException e) {
			Util.error(this, e);
		}
		catch (RrdException e) {
			Util.error(this, e);
		}
	}

	private void plotArchive() {
		if (!inspectorModel.isOk()) {
			Util.error(this, "Open a valid RRD file first.");
			return;
		}
		RrdNode rrdNode = getSelectedRrdNode();
		int arcIndex;
		if (rrdNode == null || (arcIndex = rrdNode.getArcIndex()) < 0) {
			Util.error(this, "Select archive first");
			return;
		}
		String sourcePath = inspectorModel.getFile().getAbsolutePath();
		int dsIndex = rrdNode.getDsIndex();
		new GraphFrame(sourcePath, dsIndex, arcIndex);
	}

	private static void printUsageAndExit() {
		System.err.println("usage: " + RrdInspector.class.getName() + " [<filename>]");
		System.exit(1);
	}

	/**
	 * <p>To start the application use the following syntax:</p>
	 * <pre>
	 * java -cp JRobinLite.jar org.jrobin.inspector.RrdInspector
	 * java -cp JRobinLite.jar org.jrobin.inspector.RrdInspector [path to RRD file]
	 * </pre>
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1) {
			printUsageAndExit();
		}
		String path = (args.length == 1) ? args[0] : null;
		new RrdInspector(path);
	}
}
