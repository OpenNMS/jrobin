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
 * <p>A <code>ValueFormatter</code> object is used to convert double values to a formatted string.  
 * The value can be scaled according to a specific base value (default: 1000) and an appropriate
 * SI unit can be selected (k, M, G...).</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
class ValueFormatter 
{
	// ================================================================
	// -- Members
	// ================================================================	
	protected static final int NO_SCALE 		= -1;
	protected static final double DEFAULT_BASE	= 1000.0;
	
	private double base							= DEFAULT_BASE;
	private double[] scaleValues				= new double[] {
													1e18, 1e15, 1e12, 1e9, 1e6, 1e3, 1e0, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15
													};
	protected static String[] PREFIXES 			= new String[] {
													"E",  "P",  "T",  "G", "M", "k", " ",  "m", "µ", "n", "p",  "f"
													};
											
	private double value;
	private DecimalFormat decFormat;
	private int formattedStrLen;
	
	private double scaledValue;
	private int scaleIndex						= NO_SCALE;		// Last used scale index
	private int fixedIndex						= NO_SCALE;
	private String prefix;
	
	private boolean scale						= false;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	ValueFormatter() {
	}
	
	/**
	 * Constructs a ValueFormatter object with a specific scaling base value, and a specific SI unit to use.
	 * @param base Double value of the scaling base.
	 * @param scaleIndex Index of the SI unit in the SI unit table called <code>PREFIXES</code>.
	 */
	ValueFormatter( double base, int scaleIndex ) 
	{
		setBase( base );
		this.fixedIndex	= scaleIndex;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Sets the value to format, the number of decimals the formatted string should have, and
	 * the entire string len of the formatted value.  The formatted string will have whitespace
	 * prepended if the formatted string is shorter than the given length.
	 * @param value Double value that needs to be formatted.
	 * @param numDec Number of decimals that should be allowed in the formatted value.
	 * @param strLen Length of the complete formatted string.
	 */
	void setFormat( double value, int numDec, int strLen )
	{
		this.value 				= value;
		this.decFormat			= getDecimalFormat( numDec );
		this.formattedStrLen 	= strLen;	
	}
	
	/**
	 * Defines if the given value should be scaled using the set of SI units for a specific base value.
	 * @param normalScale True if normal scaling should be used, each value will be scaled with its own most
	 * appropriate SI unit.
	 * @param uniformScale True if uniform scaling should be used, each value will be scaled using the SI unit of the
	 * previously scaled value. Uniform scaling takes precendence over normal scaling.
	 */
	void setScaling( boolean normalScale, boolean uniformScale )
	{
		if ( fixedIndex >= 0 ) {
			scale 		= true;
			scaleIndex	= fixedIndex;
		}
		else {
			scale = (normalScale || uniformScale);
			if ( !uniformScale ) 
				scaleIndex = NO_SCALE;
		}
	}
	
	/**
	 * Formats the value with the given options and returns the result as a text string.
	 * @return String containing the formatted value.
	 */
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
	
	/**
	 * Returns a more 'rounded' formatted value for use with grid steps.
	 * @return The scaled, rounded value as a <code>java.lang.String</code>.
	 */
	String getScaledValue()
	{
		scaleValue( scaleIndex );
		long intVal = new Double( scaledValue ).longValue();
		
		if ( intVal == scaledValue )
			return "" + intVal;
		else
			return "" + scaledValue;
	}
	
	/**
	 * Sets the value of the scaling base, this base is used to determine when a scaled SI unit should be used.
	 * @param baseValue Double value of the scaling base.
	 */
	void setBase( double baseValue ) 
	{
		if ( baseValue == this.base )
			return;
		
		this.base			= baseValue;
		double tmp 			= 1;
		for (int i = 1; i < 7; i++) {
			tmp 				*= baseValue;
			scaleValues[6 - i] 	= tmp;
		}
		tmp = 1;
		for (int i = 7; i < scaleValues.length; i++) {
			tmp					*= baseValue;
			scaleValues[i]	 	= ( 1 / tmp );
		}
	}
	
	double getBase() {
		return base;
	}
	
	String getPrefix() { 
		return prefix;
	}	
	
	
	// ================================================================
	// -- Private methods
	// ================================================================
	/**
	 * Scales the given value based on the given options.
	 * @param scaleIndex Forced index of the SI unit in the <code>PREFIXES</code> table.  <code>NO_SCALE</code> if not forced.
	 */
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
	
	/**
	 * Retrieves a <code>DecimalFormat</code> string to format the value, based on a given number of decimals that should
	 * be used.
	 * @param numDec Number of decimals to use in the formatted value.
	 * @return DecimalFormat to use for formatting.
	 */
	private DecimalFormat getDecimalFormat( int numDec ) 
	{
		StringBuffer formatStr = new StringBuffer("###0");		// "#,##0", removed the 'grouping' separator
		for(int i = 0; i < numDec; i++) {
			if(i == 0) {
				formatStr.append('.');
			}
			formatStr.append('0');
		}

		NumberFormat nf 	= NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat df 	= (DecimalFormat) nf;
		df.applyPattern( formatStr.toString() );
		
		return df;
	}
}
