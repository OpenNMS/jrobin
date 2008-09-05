/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
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

import java.util.ArrayList;
import java.util.List;

/**
 * Class to represent successfully created JRobin graph. Objects of this class are created by method
 * {@link RrdGraph#getRrdGraphInfo()}.
 */
public class RrdGraphInfo {
	String filename;
	int width, height;
	byte[] bytes;
	String imgInfo;
	private List<String> printLines = new ArrayList<String>();

	RrdGraphInfo() {
		// cannot instantiate this class
	}

	void addPrintLine(String printLine) {
		printLines.add(printLine);
	}

	/**
	 * Returns filename of the graph
	 *
	 * @return filename of the graph. '-' denotes in-memory graph (no file created)
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Returns total graph width
	 *
	 * @return total graph width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Returns total graph height
	 *
	 * @return total graph height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns graph bytes
	 *
	 * @return Graph bytes
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Returns PRINT lines requested by {@link RrdGraphDef#print(String, String, String)} method.
	 *
	 * @return An array of formatted PRINT lines
	 */
	public String[] getPrintLines() {
		return printLines.toArray(new String[printLines.size()]);
	}

	/**
	 * Returns image information requested by {@link RrdGraphDef#setImageInfo(String)} method
	 *
	 * @return Image information
	 */
	public String getImgInfo() {
		return imgInfo;
	}

	/**
	 * Returns the number of bytes in the graph file
	 *
	 * @return Length of the graph file
	 */
	public int getByteCount() {
		return bytes != null ? bytes.length : 0;
	}

	/**
	 * Dumps complete graph information. Useful for debugging purposes.
	 *
	 * @return String containing complete graph information
	 */
	public String dump() {
		StringBuffer b = new StringBuffer();
		b.append("filename = \"").append(getFilename()).append("\"\n");
		b.append("width = ").append(getWidth()).append(", height = ").append(getHeight()).append("\n");
		b.append("byteCount = ").append(getByteCount()).append("\n");
		b.append("imginfo = \"").append(getImgInfo()).append("\"\n");
		String[] plines = getPrintLines();
		if (plines.length == 0) {
			b.append("No print lines found\n");
		}
		else {
			for (int i = 0; i < plines.length; i++) {
				b.append("print[").append(i).append("] = \"").append(plines[i]).append("\"\n");
			}
		}
		return b.toString();
	}
}
