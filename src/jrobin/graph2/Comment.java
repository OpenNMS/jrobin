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
 * <p>Represent a piece of aligned text to be drawn on the graph.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Comment 
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final int CMT_DEFAULT	= 0;
	protected static final int CMT_LEGEND	= 1;
	protected static final int CMT_GPRINT	= 2;
	
	protected static final Byte TKN_ALF		= new Byte( (byte) 1);		// Align left with Linefeed
	protected static final Byte TKN_ARF		= new Byte( (byte) 2);		// Align right with linefeed
	protected static final Byte TKN_ACF		= new Byte( (byte) 3);		// Align center with linefeed
	protected static final Byte TKN_AL		= new Byte( (byte) 4);		// Align right no linefeed
	protected static final Byte TKN_AR		= new Byte( (byte) 5);		// Align left no linefeed
	protected static final Byte TKN_AC		= new Byte( (byte) 6);		// Align center no linefeed
	protected static final Byte TKN_NULL 	= null;
	
	protected int lineCount 				= 0;
	protected boolean endLf					= false;
	protected int commentType				= CMT_DEFAULT;
	protected Byte lfToken					= TKN_ALF;
	
	protected String text;
	protected Vector oList 					= new Vector();


	// ================================================================
	// -- Constructors
	// ================================================================	
	Comment( ) {		
	}
	
	/**
	 * Constructs a <code>Comment</code> object of a given text string.
	 * The original text string is parsed into new string/token pairs
	 * where byte tokens are used to specify alignment markers. 
	 * @param text Text with alignment/new-line tokens as a single string.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	Comment( String text ) throws RrdException
	{
		this.text = text;
		parseComment();		
	}


	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Splits the string up in string/token pairs.
	 * The tokens specify alignment or new-lines.
	 * @throws RrdException Thrown in case of a JRobin specific error.
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
	 * Retrieves the corresponding token-byte for a given token character.
	 * @param tokenChar Character to retrieve corresponding bytevalue of.
	 * @return Token bytevalue for the corresponding token character.
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
	
	/**
	 * Used to check it a <code>Comment</code> item ends with a linefeed.
	 * @return True if this Comment ends with a linefeed.
	 */
	boolean isCompleteLine()
	{
		return endLf;
	}
	
	/**
	 * Retrieves a <code>Vector</code> containing all string/token pairs in order of <code>String</code> - <code>Byte</code>.
	 * @return Vector containing all string/token pairs of this Comment.
	 */
	Vector getTokens()
	{
		return oList;
	}
	
	/**
	 * Counts the number of complete lines (linefeed markers) in the <code>Comment</code> object.
	 * @return Number of complete lines in this Comment.
	 */
	int getLineCount()
	{
		return lineCount;
	}
}
