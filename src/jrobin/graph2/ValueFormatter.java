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

import java.util.Locale;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
class ValueFormatter 
{
	static final int NO_SCALE 			= -1;
	static final double DEFAULT_BASE	= 1000.0;
	
	private double base					= DEFAULT_BASE;
	private double[] scaleValues		= new double[] {
											1e18, 1e15, 1e12, 1e9, 1e6, 1e3, 1e0, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15
											};
	private static String[] PREFIXES 	= new String[] {
											"E",  "P",  "T",  "G", "M", "k", " ",  "m", "µ", "n", "p",  "f"
											};
											
	private double value;
	private DecimalFormat decFormat;
	private int formattedStrLen;
	
	private double scaledValue;
	private int scaleIndex				= NO_SCALE;		// Last used scale index
	private String prefix;
	
	private boolean scale		= false;
	
	ValueFormatter() {
		
	}
	
	ValueFormatter( double base ) 
	{
		setBase( base );
	}
	
	void setFormat( double value, int numDec, int strLen )
	{
		this.value 				= value;
		this.decFormat			= getDecimalFormat( numDec );
		this.formattedStrLen 	= strLen;	
	}
	
	void setScaling( boolean normalScale, boolean uniformScale )
	{
		scale = (normalScale || uniformScale);
		if ( !uniformScale ) 
			this.scaleIndex = NO_SCALE;
	}
	
	String getFormattedValue()
	{
		String valueStr = "" + value;
		
		if ( scale ) {
			scaleValue( scaleIndex );
			valueStr = decFormat.format(scaledValue);
		}
		else
			valueStr = decFormat.format(value);
		
		// Fix the formatted string to the correct length
		int diff = formattedStrLen - valueStr.length();
		
		StringBuffer preSpace = new StringBuffer("");
		for (int i = 0; i < diff; i++)
			preSpace.append(' ');
			
		valueStr = preSpace.append(valueStr).toString();
		
		return valueStr;
	}
	
	private void scaleValue( int scaleIndex)
	{
		double absValue = Math.abs(value);
		if (scaleIndex == NO_SCALE) 
		{
			this.prefix 		= " ";
			this.scaledValue 	= value;
		
			for (int i = 0; i < scaleValues.length; i++) 
			{
				if (absValue >= scaleValues[i] && absValue < scaleValues[i] * base) 
				{
					if ( scaleValues[i] != 1e-3 )	// Special case
					{
						this.prefix 		= PREFIXES[i];
						this.scaledValue 	= value / scaleValues[i];
						this.scaleIndex 	= i;
						return;
					}
				}
			}
		}
		else {
			this.prefix 		= PREFIXES[scaleIndex];
			this.scaledValue 	= value / scaleValues[scaleIndex];
			this.scaleIndex 	= scaleIndex;
		}
	}
	
	String getPrefix()
	{
		return prefix;
	}
			
	void setBase( double baseValue ) 
	{
		this.base			= baseValue;
		double tmp 			= 1;
		for (int i = 1; i < 7; i++) {
			tmp 				*= baseValue;
			scaleValues[6 - i] 	= tmp;
		}
		tmp = 1;
		for (int i = 7; i < scaleValues.length; i++) {
			tmp				*= baseValue;
			scaleValues[i]	 = ( 1 / tmp );
		}
	}
	
	double getBase() {
		return base;
	}
	
	private DecimalFormat getDecimalFormat(int numDec) 
	{
		String formatStr = "###0";		// "#,##0", removed the 'grouping' separator
		for(int i = 0; i < numDec; i++) {
			if(i == 0) {
				formatStr += ".";
			}
			formatStr += "0";
		}

		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern( formatStr );
		
		return df;
	}
}
