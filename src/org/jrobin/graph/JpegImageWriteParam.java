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

/**
 * <i>Based on http://javaalmanac.com/egs/javax.imageio/JpegWrite.html?l=rel</i>
 * <p>JPEG creation parameters.</p>
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
import java.util.Locale;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;;

class JpegImageWriteParam extends JPEGImageWriteParam
{
	public JpegImageWriteParam() {
		super(Locale.getDefault());
	}
    
	// This method accepts quality levels between 0 (lowest) and 1 (highest) and simply converts
	// it to a range between 0 and 256
	public void setCompressionQuality( float quality ) 
	{
		if (quality < 0.0F || quality > 1.0F) {
			throw new IllegalArgumentException("Quality out-of-bounds!");
		}
		this.compressionQuality = (quality * 256);
	}
}
