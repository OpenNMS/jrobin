/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

package jrobin.graph;

import jrobin.core.RrdException;

import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.*;

import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Class to represent JRobin graphs. This class is a light wrapper around
 * <a href="http://www.jfree.org/jfreechart/javadoc/org/jfree/chart/JFreeChart.html">JFreeChart
 * class</a>.
 * <a href="http://www.jfree.org/jfreechart/index.html">JFreeChart</a>
 * is an excellent free Java library for generating charts and graphs.</p>
 */
public class RrdGraph implements Serializable 
{
	private Grapher grapher;
	private BufferedImage img = null;

	/**
	 * Constructs new JRobin graph from the supplied definition.
	 * @param graphDef Graph definition.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public RrdGraph(RrdGraphDef graphDef) throws IOException, RrdException 
	{
		grapher = new Grapher( graphDef );
	}

    /**
	 * Creates buffered image object from the graph.
	 * @param width Image width.
	 * @param height Image height.
	 * @return Graph as a buffered image.
	 */
   	// Check, if width/height has changed since last generation
   	// Regenerate the graph
	public BufferedImage getBufferedImage(int width, int height) 
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
		catch (RrdException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Saves graph in PNG format.
	 * @param path Path to PNG file.
	 * @param width Image width
	 * @param height Image height
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsPNG(String path, int width, int height)	throws IOException 
	{
		ImageIO.write( (RenderedImage) getBufferedImage(width, height), "png", new File(path) );
	}
	
	/**
	 * Saves graph in JPEG format
	 * @param path Path to JPEG file.
	 * @param width Image width.
	 * @param height Image height.
	 * @param quality JPEG qualitty (between 0 and 1).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public void saveAsJPEG(String path, int width, int height, float quality)	throws IOException 
	{
		// Based on http://javaalmanac.com/egs/javax.imageio/JpegWrite.html?l=rel
		try {
			// Retrieve jpg image to be compressed
			BufferedImage gImage 	= getBufferedImage(width, height);
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
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns panel object so that graph can be easily embedded in swing applications.
	 * @return Swing panel object with graph embedded in panel.
	 */
	public ChartPanel getChartPanel()
	{
		ChartPanel p = new ChartPanel();
		p.setChart( getBufferedImage(0, 0) );
		
		return p;
	} 

	/**
	 * Returns graph as an array of PNG bytes
	 * @param width Image width.
	 * @param height Image height.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getPNGBytes(int width, int height) throws IOException 
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		ImageIO.write( (RenderedImage) getBufferedImage(width, height), "png", outputStream );
				
		return outputStream.toByteArray();
	}

	/**
	 * Returns graph as an array of JPEG bytes
	 * @param width Image width.
	 * @param height Image height.
	 * @param quality JPEG quality between 0 and 1.
	 * @return Array of PNG bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public byte[] getJPEGBytes(int width, int height, float quality) throws IOException 
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			// Retrieve jpg image to be compressed
			BufferedImage gImage 	= getBufferedImage(width, height);
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
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}
	
}
