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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.*;

class HostDialog extends JDialog {
	private static final String HOST_FILENAME = System.getProperty("user.home") +
		System.getProperty("file.separator") + "last-mrtg-host";
	private static final String DEFAULT_HOST = "localhost";
	private static final String TITLE = "Select JRobin-MRTG host";

	private String host;

	private JLabel hostLabel = Util.standardLabel("Host address:");
	private JTextField hostField = Util.standardTextField();
	private JButton okButton = Util.standardButton("OK");
	private JButton cancelButton = Util.standardButton("Cancel");

	HostDialog(Frame parent) {
		super(parent, TITLE, true);
		constructUserInterface();
		pack();
		setVisible(true);
	}

	private void constructUserInterface() {
		JPanel content = (JPanel) getContentPane();
		Box box = Box.createVerticalBox();
		box.add(Util.getPanelFor(hostLabel, hostField));
		box.add(Util.getPanelFor(Util.standardLabel(), okButton, cancelButton));
		content.add(box);

		String mrtgHost = MrtgData.getInstance().getMrtgHost();
		if(mrtgHost != null) {
			hostField.setText(mrtgHost);
		}
		else {
			String savedHost = getHostFromFile();
			hostField.setText(savedHost == null? DEFAULT_HOST: savedHost);
		}
		hostField.selectAll();
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { ok();	}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { cancel(); }
		});
		okButton.setMnemonic(KeyEvent.VK_O);
		cancelButton.setMnemonic(KeyEvent.VK_C);
		getRootPane().setDefaultButton(okButton);

		// finalzie
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Util.centerOnScreen(this);
	}

	String getHost() {
		return host;
	}

	private void ok() {
        String hostEntered = hostField.getText();
		if(hostEntered.length() == 0) {
			Util.warn(this, "Please enter host address");
		}
		else {
			host = hostEntered;
			saveHostToFile();
			close();
		}
	}

	private void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}

	private void cancel() {
		close();
	}

	private void saveHostToFile() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(HOST_FILENAME, false));
			pw.println(host);
		}
		catch (IOException e) {
		}
		finally {
			if(pw != null) {
				pw.close();
			}
		}
	}

	private String getHostFromFile() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(HOST_FILENAME));
			return reader.readLine();
		}
		catch (IOException e) {
			return null;
		}
		finally {
			if(reader != null) {
				try {
					reader.close();
				}
				catch(IOException e) { }
			}
		}
	}
}
