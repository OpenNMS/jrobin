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
package org.jrobin.demo.graph;

import javax.swing.*;
import java.awt.*;

import org.jrobin.graph.RrdGraph;

/**
 * <p>Extended JPanel for use in the GUI demo application.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class SwingDemoPanel extends JPanel
{
	private RrdGraph graph;
	private int width, height;

	SwingDemoPanel( RrdGraph graph )
	{
		this.graph = graph;
	}

	public void paintComponent( Graphics g )
	{
		try
		{
			// Render the image directly on the Graphics object of the JPanel
			// Width and height of 0 means autoscale the graph
			graph.specifyImageSize(true);
			graph.renderImage( (Graphics2D) g, width, height );

		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	void setGraphDimension(Dimension d) {
		width = d.width;
		height = d.height;
		repaint();
	}
}
