/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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
package jrobin.mrtg.client;

import jrobin.mrtg.MrtgException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

class HelpDialog extends JDialog {
	static final String TITLE = "Help";
	static final String HTML = "res/help.html";
	static final Dimension SIZE = new Dimension(600, 300);

	HelpDialog(Frame parent) {
		super(parent, TITLE);
		constructUI();
		pack();
		Util.centerOnScreen(this);
		setVisible(true);
	}

	private void constructUI() {
		Box box = Box.createVerticalBox();
		JTextPane textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		try {
			textPane.setText(Resources.getString(HTML));
			textPane.setCaretPosition(0);
		}
		catch(MrtgException e) {
			e.printStackTrace();
		}
		JScrollPane scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(SIZE);
		scrollPane.setAlignmentX(0.5F);
		box.add(scrollPane);
		box.add(Box.createVerticalStrut(2));
		JButton okButton = Util.standardButton("Close");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { ok(); }
		});
		okButton.setAlignmentX(0.5F);
		box.add(okButton);
		box.add(Box.createVerticalStrut(2));
		getContentPane().add(box);
		getRootPane().setDefaultButton(okButton);
	}

	private void ok() {
		close();
	}

	private void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
}
