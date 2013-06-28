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

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

class Util {
	static void centerOnScreen(Window window) {
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension screenSize = t.getScreenSize();
		Dimension frameSize = window.getPreferredSize();
		double x = (screenSize.getWidth() - frameSize.getWidth()) / 2;
		double y = (screenSize.getHeight() - frameSize.getHeight()) / 2;
		window.setLocation((int) x, (int) y);
	}

	static void error(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	static void error(Component parent, Exception e) {
		e.printStackTrace();
		error(parent, e.toString());
	}

	private static Vector<Window> windows = new Vector<Window>();
	private static final int WINDOW_POSITION_SHIFT = 20;

	static void placeWindow(Window window) {
		int count = windows.size();
		if (count == 0) {
			centerOnScreen(window);
		}
		else {
			Window last = windows.get(count - 1);
			int x = last.getX() + WINDOW_POSITION_SHIFT;
			int y = last.getY() + WINDOW_POSITION_SHIFT;
			window.setLocation(x, y);
		}
		windows.add(window);
	}

	static void dismissWindow(Window window) {
		windows.remove(window);
		if (windows.size() == 0) {
			System.exit(0);
		}
	}
}
