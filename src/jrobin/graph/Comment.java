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

import com.jrefinery.chart.TextTitle;
import jrobin.core.RrdException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Comment {
	static final int NO_ALIGN = -1;
	private static final String MONKEY = "\u8888";
	private static final String ALIGN_REGEX = "(.*)@(l|r|c)";
	private static final Pattern ALIGN_PATTERN = Pattern.compile(ALIGN_REGEX);

	private int align = NO_ALIGN;

	String comment;
	int scaleIndex = ValueScaler.NO_SCALE;

	Comment(String comment) throws RrdException {
		comment = comment.replaceAll("@@", MONKEY);
		Matcher matcher = ALIGN_PATTERN.matcher(comment);
		if(matcher.matches()) {
			// alignment specified
			char alignChar = matcher.group(2).charAt(0);
			switch(alignChar) {
				case 'l':
					align = TextTitle.LEFT;
					break;
				case 'r':
					align = TextTitle.RIGHT;
					break;
				case 'c':
					align = TextTitle.CENTER;
					break;
			}
			comment = matcher.group(1);
		}
		this.comment = comment;
	}

	String getMessage() throws RrdException {
		return comment.replaceAll(MONKEY, "@");
	}

	int getAlign() {
    	return align;
	}

	boolean isAlignSet() {
		return align != NO_ALIGN;
	}

	int getScaleIndex() {
		return scaleIndex;
	}

	void setScaleIndex(int scaleIndex) {
		this.scaleIndex = scaleIndex;
	}

	public static void main(String[] args) throws RrdException {
		Comment c = new Comment("@r");
		System.out.println("Comment = [" + c.getMessage() + "], align = " + c.getAlign());
	}

}
