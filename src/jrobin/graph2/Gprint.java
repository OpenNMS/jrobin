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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jrobin.core.RrdException;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class Gprint extends Comment 
{
	private static final String SCALE_MARKER 			= "@s";
	private static final String UNIFORM_SCALE_MARKER 	= "@S";
	private static final String VALUE_MARKER 			= "@([0-9]*\\.[0-9]{1}|[0-9]{1}|\\.[0-9]{1})";
	private static final Pattern VALUE_PATTERN 			= Pattern.compile(VALUE_MARKER);
	
	private String sourceName;
	private int aggregate; 
	private int numDec									= 3;		// Show 3 decimal values by default
	private int strLen									= -1;
	private boolean normalScale							= false;
	private boolean uniformScale						= false;
	
	Gprint( String sourceName, String consolFunc, String text ) throws RrdException
	{
		this.text = text;
		checkValuePlacement();		// First see if this GPRINT is valid
		super.parseComment();
		
		this.commentType = Comment.CMT_GPRINT;
		this.sourceName = sourceName;
		
		if ( consolFunc.equalsIgnoreCase("AVERAGE") || consolFunc.equalsIgnoreCase("AVG") )
			aggregate = Source.AGG_AVERAGE;
		else if ( consolFunc.equalsIgnoreCase("MAX") || consolFunc.equalsIgnoreCase("MAXIMUM") )
			aggregate = Source.AGG_MAXIMUM;
		else if ( consolFunc.equalsIgnoreCase("MIN") || consolFunc.equalsIgnoreCase("MINIMUM") )
			aggregate = Source.AGG_MINIMUM;
		else if ( consolFunc.equalsIgnoreCase("LAST") )
			aggregate = Source.AGG_LAST;
		else if ( consolFunc.equalsIgnoreCase("FIRST") )
			aggregate = Source.AGG_FIRST;
		else
			throw new RrdException( "Invalid consolidation function specified." );
	}
	
	void setValue( Source[] sources, HashMap sourceIndex, ValueFormatter vFormat ) throws RrdException
	{
		try
		{
			double value 	= sources[ ((Integer) sourceIndex.get(sourceName)).intValue() ].getAggregate( aggregate );
						
			vFormat.setFormat( value, numDec, strLen );
			vFormat.setScaling( normalScale, uniformScale );
			
			String valueStr = vFormat.getFormattedValue();
			String prefix	= vFormat.getPrefix();
			
			// Replace all values
			for (int i = 0; i < oList.size(); i += 2 )
			{
				String str = (String) oList.elementAt(i);
				
				str = str.replaceAll(VALUE_MARKER, valueStr);
				if ( normalScale ) str = str.replaceAll(SCALE_MARKER, prefix);
				if ( uniformScale ) str = str.replaceAll(UNIFORM_SCALE_MARKER, prefix);
				
				oList.set( i, str );
			}
		}
		catch (Exception e) {
			throw new RrdException( "Could not find datasource: " + sourceName );
		}
	}
	
	/**
	 * Check value placement by finding placeholder.
	 * Check for uniform or regular scaling.
	 * Check for the number of decimals and the complete value string length.
	 * @throws RrdException
	 */
	protected void checkValuePlacement() throws RrdException
	{
		Matcher m = VALUE_PATTERN.matcher(text);
		
		if ( m.find() )
		{	
			normalScale 	= (text.indexOf(SCALE_MARKER) >= 0);
			uniformScale	= (text.indexOf(UNIFORM_SCALE_MARKER) >= 0);
			
			if ( normalScale && uniformScale )
				throw new RrdException( "Can't specify normal scaling and uniform scaling at the same time." );
			
			String[] group = m.group(1).split("\\.");
			strLen = -1;
			numDec = 0;
	
			if ( group.length > 1 ) 
			{
				if ( group[0].length() > 0 ) {
					strLen 	= Integer.parseInt(group[0]);
					numDec 	= Integer.parseInt(group[1]);
				}
				else
					numDec = Integer.parseInt(group[1]);
			}
			else
				numDec 	= Integer.parseInt(group[0]);
		}
		else
			throw new RrdException( "Could not find where to place value. No @ placeholder found." );
	}
	
}
