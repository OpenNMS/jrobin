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

class AboutDialog extends JDialog {
	static final String TITLE = "About JRobin";
	static final String LOGO = "res/logo.png";
	private static final int GAP = 3;

	AboutDialog(Frame parent) {
		super(parent, TITLE);
		constructUI();
		pack();
		Util.centerOnScreen(this);
		setResizable(false);
		setModal(true);
		setVisible(true);
	}

	private void constructUI() {
		Box box = Box.createVerticalBox();
        JLabel logoLabel = new JLabel();
		try {
			logoLabel.setIcon(Resources.getImageIcon(LOGO));
			logoLabel.setAlignmentX(0.5F);
			logoLabel.setBorder(BorderFactory.createLoweredBevelBorder());
			box.add(logoLabel);
			box.add(Box.createVerticalStrut(GAP));
		}
		catch(MrtgException e) {
			e.printStackTrace();
		}
		JLabel versionLabel = new JLabel(Client.TITLE);
		versionLabel.setHorizontalAlignment(JLabel.CENTER);
		versionLabel.setAlignmentX(0.5F);
		versionLabel.setMaximumSize(logoLabel.getPreferredSize());
		box.add(versionLabel);
		box.add(Box.createVerticalStrut(GAP));
		JLabel subtitleLabel = new JLabel(Client.SUBTITLE);
		subtitleLabel.setHorizontalAlignment(JLabel.CENTER);
		subtitleLabel.setAlignmentX(0.5F);
		subtitleLabel.setMaximumSize(logoLabel.getPreferredSize());
		box.add(subtitleLabel);
		box.add(Box.createVerticalStrut(GAP));
		JLabel copyrightLabel = new JLabel(Client.COPYRIGHT);
		copyrightLabel.setHorizontalAlignment(JLabel.CENTER);
		copyrightLabel.setAlignmentX(0.5F);
		copyrightLabel.setMaximumSize(logoLabel.getPreferredSize());
		box.add(copyrightLabel);
		box.add(Box.createVerticalStrut(GAP));
		JLabel emailLabel = new JLabel("saxon@eunet.yu");
		emailLabel.setHorizontalAlignment(JLabel.CENTER);
		emailLabel.setAlignmentX(0.5F);
		emailLabel.setMaximumSize(logoLabel.getPreferredSize());
		box.add(emailLabel);
		box.add(Box.createVerticalStrut(2 * GAP));
		JButton okButton = Util.standardButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { ok(); }
		});
		okButton.setAlignmentX(0.5F);
		box.add(okButton);
		box.add(Box.createVerticalStrut(GAP));
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
