/*
 * Created on Oct 8, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jrobin.graph;

import java.awt.*;
import javax.swing.*;
import java.awt.image.*;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ChartPanel extends JPanel
{
	private BufferedImage chart;
	
	void setChart( BufferedImage chart ) {
		this.chart = chart;
	}
	
	public void paintComponent( Graphics g )
	{
		if ( chart != null ) g.drawImage( chart, 0, 0, null );
	}
}