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

import java.util.Vector;

import jrobin.core.RrdException;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class Comment 
{
	static final int CMT_DEFAULT	= 0;
	static final int CMT_LEGEND		= 1;
	static final int CMT_GPRINT		= 2;
	
	static final Byte TKN_ALF	= new Byte( (byte) 1);		// Align left with Linefeed
	static final Byte TKN_ARF	= new Byte( (byte) 2);		// Align right with linefeed
	static final Byte TKN_ACF	= new Byte( (byte) 3);		// Align center with linefeed
	static final Byte TKN_AL	= new Byte( (byte) 4);		// Align right no linefeed
	static final Byte TKN_AR	= new Byte( (byte) 5);		// Align left no linefeed
	static final Byte TKN_AC	= new Byte( (byte) 6);		// Align center no linefeed
	static final Byte TKN_NULL 	= null;
	
	String text;
	Vector oList = new Vector();
	
	protected int lineCount 	= 0;
	protected boolean endLf		= false;
	protected int commentType	= CMT_DEFAULT;
	protected Byte lfToken		= TKN_ALF;
	
	/**
	 * Implicit super constructor.
	 */
	Comment( ) {		
	}
	
	/**
	 * 
	 * @param text
	 */
	Comment( String text ) throws RrdException
	{
		this.text = text;
		parseComment();		
	}
	
	/**
	 * Splits the string up in string/token pairs.
	 * The tokens specify alignment or new-lines.
	 */
	protected void parseComment() throws RrdException
	{
		// @l and \n are the same
		Byte tkn;
		int lastPos	= 0;
		int mpos 	= text.indexOf("@");
		int lfpos	= text.indexOf("\n");
		if ( mpos == text.length() ) mpos = -1;
		if ( lfpos == text.length() ) lfpos = -1;
	
		while ( mpos >= 0 || lfpos >= 0 )
		{
			if ( mpos >= 0 && lfpos >= 0 ) 
			{
				if ( mpos < lfpos ) 
				{
					tkn = getToken( text.charAt(mpos + 1) );
					if ( tkn != TKN_NULL ) {
						oList.add( text.substring(lastPos, mpos) );
						oList.add( tkn );
						lastPos = mpos + 2;
						mpos	= text.indexOf("@", lastPos);
					}
					else {
						mpos	= text.indexOf("@", mpos + 1);
					}
				}
				else 
				{
					oList.add( text.substring(lastPos, lfpos) );
					oList.add( lfToken );
					endLf = true;
					lineCount++;
					lastPos = lfpos + 1;
					lfpos	= text.indexOf("\n", lastPos); 
				}
			}
			else if ( mpos >= 0 ) 
			{
				tkn = getToken( text.charAt(mpos + 1) );
				if ( tkn != TKN_NULL ) {
					oList.add( text.substring(lastPos, mpos) );
					oList.add( tkn );
					lastPos = mpos + 2;
					mpos	= text.indexOf("@", lastPos);
				}
				else
					mpos	= text.indexOf("@", mpos + 1);
			}
			else 
			{
				oList.add( text.substring(lastPos, lfpos) );
				oList.add( lfToken );
				endLf = true;
				lineCount++;
				lastPos = lfpos + 1;
				lfpos	= text.indexOf("\n", lastPos); 
			}
		
			// Check if the 'next token', isn't at end of string
			if ( mpos == text.length() ) mpos = -1;
			if ( lfpos == text.length() ) lfpos = -1;
		}
	
		// Add last part of the string if necessary
		if ( lastPos < text.length() )
		{
			oList.add( text.substring(lastPos) );
			oList.add( TKN_NULL );
		}
	}
	
	/**
	 * 
	 * @param tokenChar
	 * @return
	 */
	protected Byte getToken( char tokenChar )
	{
		switch ( tokenChar )
		{
			case 'l':
				lineCount++;
				endLf = true;
				return TKN_ALF;
			case 'L':
				return TKN_AL;
			case 'r':
				lineCount++;
				endLf = true;
				return TKN_ARF;
			case 'R':
				return TKN_AR;
			case 'c':
				lineCount++;
				endLf = true;
				return TKN_ACF;
			case 'C':
				return TKN_AC;
			default:
				return TKN_NULL;
		}
	}
	
	boolean isCompleteLine()
	{
		return endLf;
	}
	
	Vector getTokens()
	{
		return oList;
	}
	
	/**
	 * 
	 * @return
	 */
	int getLineCount()
	{
		return lineCount;
	}
}
