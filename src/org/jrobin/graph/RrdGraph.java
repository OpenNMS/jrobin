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
package org.jrobin.graph;

import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDbPool;
import org.jrobin.core.RrdException;

/**
 * <p>Class to represent JRobin graphs.  This class needs an appropriate RrdGraphDef to generate graphs.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
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
	
	private RrdDbPool pool;
		
	private int maxPoolSize				= DEFAULT_POOLSIZE;
	private boolean useImageSize		= false;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new JRobin graph object, without a shared database pool.
	 */
	public RrdGraph() 
	{	
	}

	/**
	 * Constructs a new JRobin graph object.
	 * @param usePool True if this should object should use RrdDbPool
	 */
	public RrdGraph( boolean usePool )
	{
		if ( usePool )
			this.pool = RrdDbPool.getInstance();
	}

	/**
	 * Constructs a new JRobin graph object from the supplied definition.
	 * @param graphDef Graph definition.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdGraph( RrdGraphDef graphDef ) throws IOException, RrdException
	{
		this( graphDef, false );
	}

	/**
	 * Constructs a new JRobin graph from the supplied definition.
	 * @param graphDef Graph definition.
	 * @param usePool True if this should object should use RrdDbPool
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdGraph( RrdGraphDef graphDef, boolean usePool ) throws IOException, RrdException
	{
		if ( usePool )
			this.pool = RrdDbPool.getInstance();
		grapher		= new Grapher( graphDef, this );
	}
	
	
	// ================================================================
	// -- Public mehods
	// ================================================================
	/**
	 * Determines if graph creation should specify dimensions for the chart graphing
	 * are, of for the entire image size.  Default is the only the chart graphing
	 * area, this has an impact on the entire image size.
	 * @param specImgSize True if the dimensions for the entire image will be specified, false if only for the chart area. 
	 */
	public void specifyImageSize( boolean specImgSize )
	{
		this.useImageSize = specImgSize;
	}
	
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
	 * @throws RrdException Thrown in case of JRobin specific error.
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
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void saveAsPNG( String path, int width, int height ) throws RrdException, IOException
	{
		ImageIO.write( (RenderedImage) getBufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", new File(path) );
	}
	
	/**
	 * Creates and saves a graph image with default dimensions as a GIF file.
	 * By default the chart area is 400 by 100 pixels, the size of the entire image is dependant
	 * on number of title/legend/comment lines and some other settings.
	 * @param path Path to the GIF file to be created.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void saveAsGIF( String path ) throws RrdException, IOException
	{
		saveAsGIF( path, 0, 0 );
	}
	
	/**
	 * Creates and saves a graph image with custom chart dimensions as a GIF file.
	 * The resulting size of the entire image is also influenced by many other settings like number of comment lines.
	 * @param path Path to the GIF file to be created.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void saveAsGIF(String path, int width, int height) throws RrdException, IOException
	{
		GifEncoder gifEncoder 		= new GifEncoder( getBufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED) );
		FileOutputStream stream 	= new FileOutputStream( path, false );
		
		gifEncoder.encode(stream);
		
		stream.close();

	}

	/**
	 * Creates and saves a graph image with default dimensions as a JPEG file.
	 * By default the chart area is 400 by 100 pixels, the size of the entire image is dependant
	 * on number of title/legend/comment lines and some other settings.
	 * @param path Path to the JPEG file to be created.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsJPEG( String path, float quality ) throws RrdException, IOException
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
	public void saveAsJPEG( String path, int width, int height, float quality ) throws RrdException, IOException
	{
		// Based on http://javaalmanac.com/egs/javax.imageio/JpegWrite.html?l=rel
		// Retrieve jpg image to be compressed
		BufferedImage gImage 	= getBufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		RenderedImage rndImage	= (RenderedImage) gImage;
	
		// Find a jpeg writer
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
		if (iter.hasNext()) {
			writer = (ImageWriter)iter.next();
		}

		// Prepare output file
		ImageOutputStream ios = ImageIO.createImageOutputStream(new File(path));
		writer.setOutput(ios);

		// Set the compression quality
		ImageWriteParam iwparam = new JpegImageWriteParam();
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
		iwparam.setCompressionQuality(quality);

		// Write the image
		writer.write(null, new IIOImage(rndImage, null, null), iwparam);

		// Cleanup
		ios.flush();
		writer.dispose();
		ios.close();
	}
	
	/**
	 * Returns graph with default chart dimensions (400 by 100) as an array of PNG bytes.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getPNGBytes() throws IOException, RrdException
	{
		return getPNGBytes( 0, 0 );
	}
	
	/**
	 * Returns graph with custom chart dimensions as an array of PNG bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getPNGBytes( int width, int height ) throws IOException, RrdException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		ImageIO.write( (RenderedImage) getBufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", outputStream );
				
		return outputStream.toByteArray();
	}
	
	/**
	 * Returns graph with default chart dimensions (400 by 100) as an array of JPEG bytes.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getJPEGBytes( float quality ) throws IOException, RrdException
	{
		return getJPEGBytes( 0, 0, quality );
	}
	
	/**
	 * Returns graph with custom chart dimensions as an array of JPEG bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @param quality JPEG quality, between 0 (= low) and 1.0f (= high).
	 * @return Array of JPEG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getJPEGBytes( int width, int height, float quality ) throws IOException, RrdException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		// Retrieve jpg image to be compressed
		BufferedImage gImage 	= getBufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		RenderedImage rndImage	= (RenderedImage) gImage;
	
		// Find a jpeg writer
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
		if (iter.hasNext()) {
			writer = (ImageWriter)iter.next();
		}

		// Prepare output file
		ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
		writer.setOutput(ios);

		// Set the compression quality
		ImageWriteParam iwparam = new JpegImageWriteParam();
		iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT) ;
		iwparam.setCompressionQuality(quality);

		// Write the image
		writer.write(null, new IIOImage(rndImage, null, null), iwparam);

		// Cleanup
		ios.flush();
		writer.dispose();
		ios.close();
		
		return outputStream.toByteArray();
	}

	/**
	 * Returns graph with default chart dimensions (400 by 100) as an array of GIF bytes.
	 * @return Array of GIF bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getGIFBytes() throws RrdException, IOException
	{
		return getGIFBytes( 0, 0 );	
	}
	
	/**
	 * Returns graph with custom chart dimensions as an array of GIF bytes.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 * @return Array of GIF bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getGIFBytes(int width, int height) throws RrdException, IOException
	{
		BufferedImage image 			= getBufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
		ByteArrayOutputStream bStream 	= new ByteArrayOutputStream();
	
		GifEncoder gifEncoder 			= new GifEncoder( image );
		gifEncoder.encode( bStream );
		
		return bStream.toByteArray();
	}
		
	/**
	 * Returns panel object so that graph can be easily embedded in swing applications.
	 * @return Swing JPanel object with graph embedded in panel.
	 */
	public JPanel getChartPanel() throws RrdException, IOException
	{
		ChartPanel p = new ChartPanel();
		p.setChart( getBufferedImage(0, 0, BufferedImage.TYPE_INT_RGB) );
		
		return p;
	}
	
	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	RrdDb getRrd( String rrdFile ) throws IOException, RrdException
	{
		if ( pool != null ) {
			return pool.requestRrdDb( rrdFile );
		}
		else 
			return new RrdDb( rrdFile );
	}

	void releaseRrd(RrdDb rrdDb) throws RrdException, IOException {
		if(pool != null) {
			pool.release(rrdDb);
		}
		else {
			rrdDb.close();
		}
	}

	// ================================================================
	// -- Private methods
	// ================================================================
	private BufferedImage getBufferedImage(int width, int height, int colorType) throws RrdException, IOException
	{
		// Always regenerate graph
		if ( useImageSize )
			img = grapher.createImageGlobal( width, height, colorType );
		else
			img = grapher.createImage( width, height, colorType );
		
		return img;
	}
}
