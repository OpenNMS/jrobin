/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 * 
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package jrobin.graph2;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import jrobin.core.RrdDb;
import jrobin.core.Util;
import jrobin.core.RrdException;

/**
 * <p>Class to represent JRobin graphs.  This class needs an appropriate RrdGraphDef to generate graphs.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
public class RrdGraph implements Serializable
{
	// ================================================================
	// -- Members
	// ================================================================
	private static int DEFAULT_POOLSIZE	= 15;
	
	private Grapher grapher;
	private BufferedImage img;
	private ArrayList rrdDbPool;
		
	private int maxPoolSize				= DEFAULT_POOLSIZE;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new JRobin graph object. 
	 */
	public RrdGraph() 
	{	
		this( DEFAULT_POOLSIZE );
	}

	/**
	 * Constructs a new JRobin graph object.
	 * @param poolSize Maximum number of concurrent open rrd files in the RrdGraph object.
	 */
	public RrdGraph( int poolSize )
	{
		maxPoolSize	= poolSize;
		rrdDbPool 	= new ArrayList( poolSize );
	}

	/**
	 * Constructs a new JRobin graph object from the supplied definition.
	 * @param graphDef Graph definition.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdGraph( RrdGraphDef graphDef ) throws IOException, RrdException
	{
		this( graphDef, DEFAULT_POOLSIZE );
	}

	/**
	 * Constructs a new JRobin graph from the supplied definition.
	 * @param graphDef Graph definition.
	 * @param poolSize Maximum number of concurrent open rrd files in the RrdGraph object.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdGraph( RrdGraphDef graphDef, int poolSize ) throws IOException, RrdException
	{
		maxPoolSize	= poolSize;
		rrdDbPool 	= new ArrayList( poolSize );

		grapher		= new Grapher( graphDef, this );
	}
	
	
	// ================================================================
	// -- Public mehods
	// ================================================================
	/**
	 * Sets the graph definition to use for the graph construction.
	 * @param graphDef Graph definition.
	 */
	public void setGraphDef( RrdGraphDef graphDef ) 
	{
		img		= null;
		grapher = new Grapher( graphDef, this );
	}
	
	/**
	 * Creates and saves a graph image with default dimensions as a PNG file.
	 * By default the chart area is 400 by 100 pixels, the size of the entire image is dependant
	 * on number of title/legend/comment lines and some other settings.
	 * @param path Path to the PNG file to be created.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsPNG( String path ) throws RrdException, IOException
	{
		saveAsPNG( path, 0, 0 );
	}
	
	/**
	 * Creates and saves a graph image with custom chart dimensions as a PNG file.
	 * The resulting size of the entire image is also influenced by many other settings like number of comment lines.
	 * @param path Path to the PNG file to be created.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsPNG( String path, int width, int height ) throws RrdException, IOException
	{
		Util.time();
		RenderedImage r =(RenderedImage) getBufferedImage(width, height);
		Util.time();
		ImageIO.write( r, "png", new File(path) );
		Util.time(5);
	}
	
	/**
	 * Creates and saves a graph image with default dimensions as a JPEG file.
	 * By default the chart area is 400 by 100 pixels, the size of the entire image is dependant
	 * on number of title/legend/comment lines and some other settings.
	 * @param path Path to the JPEG file to be created.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsJPEG( String path, float quality ) throws IOException
	{
		saveAsJPEG( path, 0, 0, quality );
	}
	
	/**
	 * Creates and saves a graph image with custom chart dimensions as a JPEG file.
	 * The resulting size of the entire image is also influenced by many other settings like number of comment lines.
	 * @param path Path to the JPEG file to be created.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsJPEG( String path, int width, int height, float quality ) throws IOException
	{
		System.err.println( "Method not implemented..." );
	}
	
	/**
	 * Returns graph width default chart dimensions (400 by 100) as an array of PNG bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getPNGBytes() throws IOException
	{
		return getPNGBytes( 0, 0 );
	}
	
	/**
	 * Returns graph width custom chart dimensions as an array of PNG bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getPNGBytes( int width, int height ) throws IOException
	{
		System.err.println( "Method not implemented..." );
		return null;
	}
	
	/**
	 * Returns graph width default chart dimensions (400 by 100) as an array of JPEG bytes.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getJPEGBytes( float quality ) throws IOException
	{
		return getJPEGBytes( 0, 0, quality );
	}
	
	/**
	 * Returns graph width custom chart dimensions as an array of JPEG bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @return Array of JPEG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getJPEGBytes( int width, int height, float quality ) throws IOException
	{
		System.err.println( "Method not implemented..." );
		return null;
	}
	
	/**
	 * Returns panel object so that graph can be easily embedded in swing applications.
	 * @return Swing JPanel object with graph embedded in panel.
	 */
	public JPanel getChartPanel()
	{
		System.err.println( "Method not implemented..." );
		return null;
	}
	
	/**
	 * Closes all open RRD files in the rrd database pool of the object, and removes each file from the pool.
	 */
	public void closeFiles()
	{
		// Close all RrdDb objects
		for (int i = 0; i < rrdDbPool.size(); i++) {
			try
			{
				((RrdDb) rrdDbPool.get(i)).close();
			}
			catch (IOException e) {
				// Ignore this exception, continue trying to close the next RrdDb
			}
		}
		
		// Empty the pool
		rrdDbPool.clear();
	}
	
	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	RrdDb getRrd( String rrdFile ) throws IOException, RrdException
	{
		RrdDb rrd;
		
		// Look for an open rrdDb
		for (int i = 0; i < rrdDbPool.size(); i++) {
			rrd = (RrdDb) rrdDbPool.get(i);
			if ( rrd.getRrdFile().getFilePath().equalsIgnoreCase(rrdFile) )
				return rrd;
		}
		
		// Not in the pool, create new object
		rrd = new RrdDb( rrdFile );
		if ( rrdDbPool.size() < maxPoolSize )
			rrdDbPool.add( rrd );
		else if ( maxPoolSize > 1 ) {
			rrdDbPool.remove(0);		// Remove the first (oldest) RrdDb
			rrdDbPool.add( rrd );
		}
		
		return rrd;
	}
	
	// ================================================================
	// -- Private methods
	// ================================================================
	private BufferedImage getBufferedImage(int width, int height)
	{
		try 
		{
			if ( img != null )
				return img;
			else
			{
				img = grapher.createImage( width, height );
				return img;
			}
		}
		catch (Exception e) { 	// Temporary
			e.printStackTrace();
		}
		
		return null;
	}
}
