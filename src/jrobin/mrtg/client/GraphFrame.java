/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
 *
 * (C) Copyright 2003, by Sasa Markovic.
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

package jrobin.mrtg.client;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class GraphFrame extends JFrame {

	static final int TYPE_QUICK = 1, TYPE_DAILY = 2,
		TYPE_WEEKLY = 3, TYPE_MONTHLY = 4, TYPE_YEARLY = 5, TYPE_CUSTOM = 6;
	static int START_YEAR = 2000, END_YEAR = 2010;
	static Dimension GRAPH_SIZE = new Dimension(600, 400);
	static final int REFRESH_INTERVAL = 300; // 5 minutes

	private JComboBox startDay = new JComboBox();
	private JComboBox startMonth = new JComboBox();
	private JComboBox startYear = new JComboBox();
	private JComboBox startHour = new JComboBox();
	private JComboBox endDay = new JComboBox();
	private JComboBox endMonth = new JComboBox();
	private JComboBox endYear = new JComboBox();
	private JComboBox endHour = new JComboBox();
	private JButton refreshButton= Util.largeButton("Refresh");
	private JButton leftButton= Util.standardButton("<< Left");
	private JButton rightButton= Util.standardButton("Right >>");
	private JButton saveButton= Util.standardButton("Save graph...");
	private JButton closeButton= Util.standardButton("Close");
	private JLabel graphLabel = new JLabel();

	private byte[] graphBytes;
	private String mrtgHost = MrtgData.getInstance().getMrtgHost();
	private RpcClient client;
	private RouterInfo routerInfo;
	private LinkInfo linkInfo;
	private int type;

	private Refresher refresher = new Refresher();
	private JLabel infoLabel = new JLabel();

	GraphFrame(JFrame parent, RouterInfo routerInfo, LinkInfo linkInfo, int type) {
		super(linkInfo.getIfDescr() + "@" + routerInfo.getHost() + " [graph]");
		setResizable(false);
		this.routerInfo = routerInfo;
		this.linkInfo = linkInfo;
		this.type = type;
		constructUI();
		setInitialDates();
		createGraph();
		pack();
		setVisible(true);
	}

	private void constructUI() {
		JPanel mainContent = (JPanel) getContentPane();
		mainContent.setLayout(new BorderLayout(3, 3));

		JPanel content = new JPanel();
		content.setLayout(new BorderLayout(3, 3));

    	JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		topPanel.add(new JLabel("From: "));
		topPanel.add(startMonth);
		topPanel.add(startDay);
		topPanel.add(startYear);
		topPanel.add(startHour);
		topPanel.add(new JLabel(" To: "));
		topPanel.add(endMonth);
		topPanel.add(endDay);
		topPanel.add(endYear);
		topPanel.add(endHour);
		if(type != TYPE_CUSTOM) {
			startMonth.setEnabled(false);
			startDay.setEnabled(false);
			startYear.setEnabled(false);
			startHour.setEnabled(false);
			endMonth.setEnabled(false);
			endDay.setEnabled(false);
			endYear.setEnabled(false);
			endHour.setEnabled(false);
		}
		content.add(topPanel, BorderLayout.NORTH);

		// graph panel
		graphLabel.setPreferredSize(GRAPH_SIZE);
		graphLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		content.add(graphLabel, BorderLayout.CENTER);
		// botom panel
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        leftButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { shift(false); }
		});
		if(type != TYPE_CUSTOM) {
			bottomPanel.add(leftButton);
		}
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { createGraph(); }
		});
		bottomPanel.add(refreshButton);
		rightButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { shift(true); }
		});
		if(type != TYPE_CUSTOM) {
			bottomPanel.add(rightButton);
		}
		content.add(bottomPanel, BorderLayout.SOUTH);
		mainContent.add(content, BorderLayout.CENTER);

		// east panel
		Box box = Box.createVerticalBox();
        box.add(Box.createVerticalStrut((int)topPanel.getPreferredSize().getHeight()));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { save(); }
		});
		box.add(saveButton);
		box.add(Box.createVerticalStrut(3));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { close(); }
		});
		box.add(closeButton);
		box.add(Box.createVerticalStrut(20));
		box.add(infoLabel);
		mainContent.add(box, BorderLayout.EAST);

		// populate controls
        fillDays(startDay); fillDays(endDay);
		fillMonths(startMonth); fillMonths(endMonth);
		fillYears(startYear); fillYears(endYear);
		fillHours(startHour); fillHours(endHour);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				refresher.terminate();
			}
		});
		getRootPane().setDefaultButton(saveButton);
		saveButton.setMnemonic(KeyEvent.VK_S);
		closeButton.setMnemonic(KeyEvent.VK_C);
		refreshButton.setMnemonic(KeyEvent.VK_F);
        leftButton.setMnemonic(KeyEvent.VK_L);
		rightButton.setMnemonic(KeyEvent.VK_R);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Util.centerOnScreen(this);
	}

	private void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	private void save() {
		if(graphBytes == null) {
			return;
		}
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
				BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(selectedFile));
				out.write(graphBytes);
				out.close();
			} catch (IOException e) {
				Util.error(this, "Could not save graph to file:\n" + e);
			}
		}
	}

	private void shift(boolean right) {
		int sign = right? +1: -1;
		GregorianCalendar start = getDate(false);
		GregorianCalendar end = getDate(true);
		switch(type) {
			case TYPE_QUICK:
			case TYPE_DAILY:
				start.add(Calendar.DAY_OF_MONTH, sign * 1);
				end.add(Calendar.DAY_OF_MONTH, sign * 1);
				break;
			case TYPE_WEEKLY:
				start.add(Calendar.DAY_OF_MONTH, sign * 7);
				end.add(Calendar.DAY_OF_MONTH, sign * 7);
				break;
			case TYPE_MONTHLY:
				start.add(Calendar.MONTH, sign * 1);
				end.add(Calendar.MONTH, sign * 1);
				break;
			case TYPE_YEARLY:
				start.add(Calendar.YEAR, sign * 1);
				end.add(Calendar.YEAR, sign * 1);
				break;
		}
		setDate(start, false);
		setDate(end, true);
		createGraph();
	}

	private void createGraph() {
		refresher.terminate();
		refreshButton.setText("Refresh");
		Date start = getDate(false).getTime();
		Date end = getDate(true).getTime();
		Date now = new Date();
		if(start.getTime() >= end.getTime()) {
			Util.error(this, "Invalid time span");
			return;
		}
		try {
			if(client == null) {
            	client = new RpcClient(mrtgHost);
			}
			graphBytes = client.getPngGraph(routerInfo, linkInfo, start, end);
			ImageIcon icon = new ImageIcon(graphBytes, "PNG graph");
			graphLabel.setIcon(icon);
			if(start.getTime() <= now.getTime() && now.getTime() < end.getTime()) {
				refresher = new Refresher();
				refresher.start();
			}
		} catch (Exception e) {
            Util.error(this, "Graph could not be generated:\n" + e);
		}
	}

	private GregorianCalendar getDate(boolean isEnd) {
		JComboBox hourCombo = isEnd? endHour: startHour;
		JComboBox dayCombo = isEnd? endDay: startDay;
		JComboBox monthCombo = isEnd? endMonth: startMonth;
		JComboBox yearCombo = isEnd? endYear: startYear;
        int hour = hourCombo.getSelectedIndex();
		int day = dayCombo.getSelectedIndex() + 1;
		int month = monthCombo.getSelectedIndex();
		int year = yearCombo.getSelectedIndex() + START_YEAR;
		return new GregorianCalendar(year, month, day, hour, 0);
	}

	private void fillDays(JComboBox combo) {
		for(int i = 1; i <= 31; i++) {
			combo.insertItemAt((i < 10? "0": "") + i, i - 1);
		}
		combo.setSelectedIndex(0);
	}

	private void fillMonths(JComboBox combo) {
		String names[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Avg", "Sep",
						   "Oct", "Nov", "Dec" };
		for(int i = 0; i < names.length; i++) {
			combo.insertItemAt(names[i], i);
		}
		combo.setSelectedIndex(0);
	}

	private void fillYears(JComboBox combo) {
		for(int i = START_YEAR; i <= END_YEAR; i++) {
			combo.insertItemAt("" + i, i - START_YEAR);
		}
		combo.setSelectedIndex(0);
	}

	private void fillHours(JComboBox combo) {
		for(int i = 0; i < 24; i++) {
			String str = (i < 10? "0": "") + i + ":00";
			combo.insertItemAt(str, i);
		}
		combo.setSelectedIndex(0);
	}

	private void setDate(GregorianCalendar gc, boolean isEnd) {
		JComboBox hourCombo = isEnd? endHour: startHour;
		JComboBox dayCombo = isEnd? endDay: startDay;
		JComboBox monthCombo = isEnd? endMonth: startMonth;
		JComboBox yearCombo = isEnd? endYear: startYear;
		hourCombo.setSelectedIndex(gc.get(Calendar.HOUR_OF_DAY));
		dayCombo.setSelectedIndex(gc.get(Calendar.DAY_OF_MONTH) - 1);
		monthCombo.setSelectedIndex(gc.get(Calendar.MONTH));
		yearCombo.setSelectedIndex(gc.get(Calendar.YEAR) - START_YEAR);
	}

	private void setInitialDates() {
		GregorianCalendar start = null, end = null, gc = new GregorianCalendar();
		switch(type) {
			case TYPE_QUICK:
				start = new GregorianCalendar(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
					gc.get(Calendar.DAY_OF_MONTH) - 1, gc.get(Calendar.HOUR_OF_DAY) + 1, 0);
				end = new GregorianCalendar(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
					gc.get(Calendar.DAY_OF_MONTH), gc.get(Calendar.HOUR_OF_DAY) + 1, 0);
				break;
			case TYPE_DAILY:
			case TYPE_CUSTOM:
				start = new GregorianCalendar(gc.get(Calendar.YEAR),
					gc.get(Calendar.MONTH), gc.get(Calendar.DAY_OF_MONTH));
				end = new GregorianCalendar(gc.get(Calendar.YEAR),
					gc.get(Calendar.MONTH), gc.get(Calendar.DAY_OF_MONTH) + 1);
				break;
			case TYPE_WEEKLY:
				int shift = gc.get(Calendar.DAY_OF_WEEK) - 1;
				start = new GregorianCalendar(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
					gc.get(Calendar.DAY_OF_MONTH) - shift);
				end = new GregorianCalendar(gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
					gc.get(Calendar.DAY_OF_MONTH) - shift + 7);
				break;
			case TYPE_MONTHLY:
				start = new GregorianCalendar(gc.get(Calendar.YEAR),
					gc.get(Calendar.MONTH), 1);
				end = new GregorianCalendar(gc.get(Calendar.YEAR),
					gc.get(Calendar.MONTH) + 1, 1);
				break;
			case TYPE_YEARLY:
				start = new GregorianCalendar(gc.get(Calendar.YEAR), 0, 1);
				end = new GregorianCalendar(gc.get(Calendar.YEAR) + 1, 0, 1);
				break;
		}
		setDate(start, false);
		setDate(end, true);
	}

	class Refresher extends Thread {
		private boolean active = true;
		private int secs = REFRESH_INTERVAL;

		Refresher() {
			setDaemon(true);
		}

		public void run() {
			while(active) {
				for(secs = REFRESH_INTERVAL; secs >= 0 && active; secs--) {
					refreshButton.setText("Refreshing in " + secs + '"');
					try {
						sleep(1000L);
					}
					catch (InterruptedException e) { }
				}
				if(active) {
					refreshButton.doClick();
				}
			}
		}

		void terminate() {
			active = false;
		}

		void reset() {
			secs = REFRESH_INTERVAL;
		}
	}
}
