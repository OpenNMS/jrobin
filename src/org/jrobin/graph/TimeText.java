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

import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.jrobin.core.XmlWriter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

/**
 * <p>Comment object containing a formatted timestamp (current time of timestamp given).</p>
 */
public class TimeText extends Comment
{
	private static final String TIME_MARKER 			= "@t";

	private Date textDate								= null;
	private DateFormat dateFormat;
	private ArrayList parsedList;

	TimeText( String text, String pattern ) throws RrdException
	{
		this( text, new SimpleDateFormat( pattern ) );
	}

	TimeText( String text, DateFormat dateFormat ) throws RrdException
	{
		super( text );
		this.dateFormat = dateFormat;

		// Check if we can locate the placeholder for the timestamp (@t)
		if ( text.indexOf(TIME_MARKER) < 0 )
			throw new RrdException( "Could not find where to place timestamp. No @t placeholder found.");
	}

	TimeText( String text, String pattern, long timestamp ) throws RrdException
	{
		this( text, new SimpleDateFormat( pattern ), new Date( timestamp * 1000 ) );
	}

	TimeText( String text, DateFormat dateFormat, long timestamp ) throws RrdException
	{
		this( text, dateFormat, new Date( timestamp * 1000 ) );
	}

	TimeText( String text, String pattern, Date date ) throws RrdException
	{
		this( text, new SimpleDateFormat( pattern ), date );
	}

	TimeText( String text, DateFormat dateFormat, Date date ) throws RrdException
	{
		super( text );
		this.textDate	= date;
		this.dateFormat = dateFormat;

		// Check if we can locate the placeholder for the timestamp (@t)
		if ( text.indexOf(TIME_MARKER) < 0 )
			throw new RrdException( "Could not find where to place timestamp. No @t placeholder found.");
	}

	TimeText( String text, String pattern, Calendar cal ) throws RrdException
	{
		this( text, new SimpleDateFormat( pattern ), cal.getTime() );
	}

	TimeText( String text, DateFormat dateFormat, Calendar cal ) throws RrdException
	{
		this( text, dateFormat, cal.getTime() );
	}

	ArrayList getTokens()
	{
		parsedList		= new ArrayList( oList );

		// Create time string
		String timeStr	= dateFormat.format( (textDate != null ? textDate : new Date()) );

		// Replace all values
		for (int i = 0; i < oList.size(); i += 2 )
		{
			String str = (String) oList.get(i);

			str = str.replaceAll(TIME_MARKER, timeStr);

			parsedList.set( i, str );
		}

		return parsedList;
	}

	void exportXmlTemplate(XmlWriter xml)
	{
		xml.startTag("time");
		xml.writeTag( "format", text );
		if ( dateFormat instanceof SimpleDateFormat )
			xml.writeTag( "pattern", ((SimpleDateFormat) dateFormat).toPattern());
		else
			xml.writeTag( "pattern", "" );			// A custom DateFormat can't be exported
		if ( textDate != null )
			xml.writeTag( "value", Util.getTimestamp(textDate) );
		xml.closeTag();
	}
}
