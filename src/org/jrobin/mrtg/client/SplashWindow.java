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
package org.jrobin.mrtg.client;

import org.jrobin.mrtg.MrtgException;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Sep 4, 2003
 * Time: 10:45:37 AM
 * To change this template use Options | File Templates.
 */
class SplashWindow extends JWindow {
	SplashWindow() {
		JLabel imageLabel = new JLabel();
		try {
			imageLabel.setIcon(Resources.getImageIcon(Client.RESOURCE_PATH + "logo.png"));
		} catch (MrtgException e) {
			// NOP
		}
		imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
		getContentPane().add(imageLabel);
		pack();
		Util.centerOnScreen(this);
		setVisible(true);
	}

	void close() {
		dispose();
	}
}
