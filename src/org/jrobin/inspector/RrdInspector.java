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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

class RrdInspector extends JFrame {
	static final String TITLE = "RRD File Inspector";
	static Dimension MAIN_TREE_SIZE = new Dimension(250, 400);
	static Dimension INFO_PANE_SIZE = new Dimension(450, 400);

	JTabbedPane tabbedPane = new JTabbedPane();
	private JTree mainTree = new JTree();
	private JTable generalTable = new JTable();
	private JTable datasourceTable = new JTable();
	private JTable archiveTable = new JTable();
	private JTable dataTable = new JTable();

	private InspectorModel inspectorModel = new InspectorModel();

    RrdInspector() {
		super(TITLE);
		constructUI();
		showCentered();
		selectFile();
	}

	private void showCentered() {
		pack();
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension screenSize = t.getScreenSize(), frameSize = getPreferredSize();
		double x = (screenSize.getWidth() - frameSize.getWidth()) / 2;
		double y = (screenSize.getHeight() - frameSize.getHeight()) / 2;
		setLocation((int) x, (int) y);
		setVisible(true);
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
			public void valueChanged(TreeSelectionEvent e) { nodeChangedAction(); }
		});
		mainTree.setModel(inspectorModel.getMainTreeModel());

		// EAST, tabbed pane

		// GENERAL TAB
		JScrollPane spGeneral = new JScrollPane(generalTable);
		spGeneral.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("General info", spGeneral);
		generalTable.setModel(inspectorModel.getGeneralTableModel());
		generalTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		generalTable.getColumnModel().getColumn(0).setMaxWidth(150);
		//generalTable.getColumnModel().getColumn(0).setMinWidth(150);
		// DATASOURCE TAB
		JScrollPane spDatasource = new JScrollPane(datasourceTable);
		spDatasource.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("Datasource info", spDatasource);
		datasourceTable.setModel(inspectorModel.getDatasourceTableModel());
		datasourceTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		datasourceTable.getColumnModel().getColumn(0).setMaxWidth(150);
		//datasourceTable.getColumnModel().getColumn(0).setMinWidth(150);
		// ARCHIVE TAB
		JScrollPane spArchive = new JScrollPane(archiveTable);
		archiveTable.setModel(inspectorModel.getArchiveTableModel());
		archiveTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		archiveTable.getColumnModel().getColumn(0).setMaxWidth(150);
		//archiveTable.getColumnModel().getColumn(0).setMinWidth(150);
		spArchive.setPreferredSize(INFO_PANE_SIZE);
        tabbedPane.add("Archive info", spArchive);
		// DATA TAB
		JScrollPane spData = new JScrollPane(dataTable);
		dataTable.setModel(inspectorModel.getDataTableModel());
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		dataTable.getColumnModel().getColumn(0).setMaxWidth(100);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		spData.setPreferredSize(INFO_PANE_SIZE);
		tabbedPane.add("Archive data", spData);

		content.add(tabbedPane, BorderLayout.CENTER);

		// MENU
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileMenuItem = new JMenuItem("Open RRD file...", KeyEvent.VK_O);
		fileMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		fileMenu.add(fileMenuItem);
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		// finalize UI
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) { System.exit(0); }
		});

	}

	private void nodeChangedAction() {
		TreePath path = mainTree.getSelectionPath();
		if(path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object obj = node.getUserObject();
			if(obj instanceof RrdNode) {
				RrdNode rrdNode = (RrdNode) obj;
				inspectorModel.selectModel(rrdNode.getDsIndex(), rrdNode.getArcIndex());
				if(rrdNode.getDsIndex() >= 0 && rrdNode.getArcIndex() >= 0) {
					// archive node
					if(tabbedPane.getSelectedIndex() < 2) {
						tabbedPane.setSelectedIndex(2);
					}
				}
				else if(rrdNode.getDsIndex() >= 0) {
					tabbedPane.setSelectedIndex(1);
				}
				else {
					tabbedPane.setSelectedIndex(0);
				}
			}
		}
	}

	private void selectFile() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()? true:
					f.getAbsolutePath().toLowerCase().endsWith(".rrd");
			}
			public String getDescription() {
				return "JRobin RRD files";
			}
		};
		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(this);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			inspectorModel.setFile(file);
			tabbedPane.setSelectedIndex(0);
		}
	}

	public static void main(String[] args) {
		new RrdInspector();
	}

}
