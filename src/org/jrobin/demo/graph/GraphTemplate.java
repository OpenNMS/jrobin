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

import org.jrobin.graph.RrdGraphDefTemplate;
import org.jrobin.graph.RrdGraph;
import org.jrobin.core.RrdException;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * <p>Simple command line application that allows you to generate a graph
 * from a RrdGraphDefTemplate.  Pretty straightforward in use.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class GraphTemplate
{
	private static String format 	= "gif";
	private static int width		= 0;			// Auto scale
	private static int height		= 0;			// Auto scale
	private static float quality	= 1.0f;			// JPEG quality

	private static String templateFile, imageName;

	private static void die( String msg )
	{
		System.err.println( msg );
		System.exit( -1 );
	}

	private static void parseArguments( String[] args )
	{
		int rpos		= args.length - 1;

		// Last two arguments should be templateFile and imageName
		imageName		= args[rpos--];
		templateFile	= args[rpos];

		// Remaining number of parameters should be even
		if ( rpos % 2 > 0 )
			die( "Invalid number of arguments." );

		for ( int i = 0; i < rpos; i += 2 )
		{
			String arg = args[i];
			String val = args[i + 1];

			try
			{
				if ( arg.equalsIgnoreCase("-img") )
					format = val;
				else if ( arg.equalsIgnoreCase("-w") )
					width = Integer.parseInt(val);
				else if ( arg.equalsIgnoreCase("-h") )
					height = Integer.parseInt(val);
				else if ( arg.equalsIgnoreCase("-q") )
					quality = Float.parseFloat(val);
			}
			catch ( Exception e ) {
				die( "Error with option '" + arg + "': " + e.getMessage() );
			}
		}
	}

	private static String readVariable( BufferedReader in, String name ) throws IOException
	{
		System.out.print( "Variable '" + name + "' = " );

		return in.readLine();
	}

	public static void main( String[] args )
	{
		if ( args.length < 2 )
		{
			System.out.println( "Usage: GraphTemplate [-img (png|gif|jpg)] [-w width] [-h height] [-q jpegQuality] <template_file> <image_name>" );
			System.exit(0);
		}

		parseArguments( args );

		try
		{
			// -- Read the RrdGraphDefTemplate (XML format)
			System.out.println( ">>> Reading XML template" );
			RrdGraphDefTemplate template = new RrdGraphDefTemplate( new File(templateFile) );

			// -- Set the parameters (if there are any)
			System.out.println( ">>> Setting template variables" );
			if ( template.hasVariables() )
			{
				BufferedReader in = new BufferedReader( new InputStreamReader(System.in) );

				String[] variables = template.getVariables();
				for ( int i = 0; i < variables.length; i++ )
					template.setVariable( variables[i], readVariable( in, variables[i] ) );
			}

			System.out.println( ">>> Generating graph..." );

			long start 		= System.currentTimeMillis();

			// -- Generate the actual graph
			RrdGraph graph	= new RrdGraph( template.getRrdGraphDef() );

			if ( format.equalsIgnoreCase("png") )
				graph.saveAsPNG( imageName, width, height );
			else if ( format.equalsIgnoreCase("gif") )
				graph.saveAsGIF( imageName, width, height );
			else if ( format.equalsIgnoreCase("jpg") )
				graph.saveAsJPEG( imageName, width, height, quality );

			long stop		= System.currentTimeMillis();

			System.out.println( ">>> Graph generated and saved in " + (stop - start) + " milliseconds" );
		}
		catch ( RrdException rrde ) {
			die( "RrdException occurred: " + rrde.getMessage() );
		}
		catch ( IOException ioe ) {
			die( "IOException occurred: " + ioe.getMessage() );
		}
	}
}
