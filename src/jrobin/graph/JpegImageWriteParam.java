/*
 * Created on Oct 7, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jrobin.graph;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
// Based on http://javaalmanac.com/egs/javax.imageio/JpegWrite.html?l=rel
import java.util.*;
import javax.imageio.plugins.jpeg.*;

public class JpegImageWriteParam extends JPEGImageWriteParam
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
