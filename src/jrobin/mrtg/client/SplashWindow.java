package jrobin.mrtg.client;

import jrobin.mrtg.MrtgException;

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
			imageLabel.setIcon(Resources.getImageIcon("res/logo.png"));
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
