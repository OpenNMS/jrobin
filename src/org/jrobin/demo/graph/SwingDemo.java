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

import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;

import javax.swing.*;
import java.io.IOException;
import java.awt.*;
import java.util.Date;

/**
 * <p>Swing demonstration of the minmax graph.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class SwingDemo
{
	static JFrame frame				= null;
	static JPanel demoPanel			= null;
	static RrdGraph graph			= null;
	static RrdGraphDef gDef			= null;

	static final String rrd			= "SwingDemo.rrd";
	static final long START 		= Util.getTimestamp( 2004, 1, 1 );

	static Date end 				= new Date(START);

	static void prepareRrd() throws IOException, RrdException
	{
		RrdDef rrdDef 			= new RrdDef( rrd, START - 300, 300 );
		rrdDef.addDatasource("a", "GAUGE", 600, Double.NaN, Double.NaN);
		rrdDef.addArchive("AVERAGE", 0.5, 1, 300);
		rrdDef.addArchive("MIN", 0.5, 6, 300);
		rrdDef.addArchive("MAX", 0.5, 6, 300);

		RrdDb rrdDb 			= new RrdDb( rrdDef, RrdBackendFactory.getFactory("MEMORY") );
		rrdDb.close();
	}

	static void prepareFrame() throws RrdException
	{
		gDef = new RrdGraphDef();
		gDef.setImageBorder( Color.WHITE, 0 );
		gDef.setTitle("JRobin Swing minmax demo");
		gDef.setVerticalLabel( "value" );
		gDef.setTimeAxisLabel( "time" );
		gDef.datasource("a", rrd, "a", "AVERAGE", "MEMORY" );
		gDef.datasource("b", rrd, "a", "MIN", "MEMORY" );
		gDef.datasource("c", rrd, "a", "MAX", "MEMORY");
		gDef.datasource( "avg", "a", "AVERAGE" );
		gDef.area("a", Color.decode("0xb6e4"), "real");
		gDef.line("b", Color.decode("0x22e9"), "min", 2 );
		gDef.line("c", Color.decode("0xee22"), "max", 2 );
		gDef.line("avg", Color.RED,  "Average" );
		gDef.time( "@l@lTime period: @t", "MMM dd, yyyy   HH:mm:ss", START );
		gDef.time( "to  @t@l", "HH:mm:ss", end );
		gDef.time("@l@lGenerated: @t@c", "HH:mm:ss" );

		// create graph finally
		graph 				= new RrdGraph(gDef);

		// Create JFrame
		frame 				= new JFrame( "JRobin Swing Demo" );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		demoPanel			= new SwingDemoPanel( graph );
		frame.getContentPane().add( demoPanel );

		frame.pack();
		frame.setBounds( 10, 10, 504, 303 );
		frame.show();
	}

	public static void main( String[] args ) throws Exception
	{
		prepareRrd();

		prepareFrame();

		// Let's update the RRD and generate the graphs many times
		RrdDb rrdDb 			= new RrdDb( rrd, RrdBackendFactory.getFactory("MEMORY") );
		Sample sample 			= rrdDb.createSample();

		long t = 0, start, stop, generationTime;

		// First we do 2 hours worth of 5 minute updates
		for ( int i = 1; i < 25; i++ )
		{
			start			= System.currentTimeMillis();

			t 				= START + i*300;

			// Update the rrd
			sample.setTime( t );
			sample.setValue("a", Math.sin(t / 3000.0) * 50 + 50);
			sample.update();

			// Set custom graph settings
			gDef.setTimePeriod( START, t );
			end.setTime( t *1000 );

			// Regenerate the graph
			frame.repaint();

			stop			= System.currentTimeMillis();
			generationTime 	= stop - start;

			// Sleep if necessary, don't update more than once per second
			if ( generationTime < 1000 )
				Thread.sleep( 1000 - generationTime );
		}

		// Now we do some more updates, but we move per hour
		for ( int i = 0; i < 22; i++ )
		{
			start			= System.currentTimeMillis();

			for ( int j = 1; j < 13; j++ )
			{
				t 				= t + 300;

				// Update the rrd
				sample.setTime( t );
				sample.setValue("a", Math.sin(t / 3000.0) * 50 + 50);
				sample.update();
			}

			// Set custom graph settings
			gDef.setTimePeriod( START, t );
			end.setTime( t * 1000 );

			// Regenerate the graph
			frame.repaint();

			stop			= System.currentTimeMillis();
			generationTime 	= stop - start;

			// Sleep if necessary, don't update more than once per second
			if ( generationTime < 1000 )
				Thread.sleep( 1000 - generationTime );
		}
	}
}
