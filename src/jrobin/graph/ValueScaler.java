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

class ValueScaler 
{
	// Change so you don't need to recalculate the base
	// VALUES all the time (actually, we're using a static)
	// Really weird stuff in here, let's change it at some point
	static final int NO_SCALE 			= -1;
	static final double DEFAULT_BASE	= 1000.0;
	
	private double base					= DEFAULT_BASE;
	private double[] VALUES 			= new double[] {
		1e18, 1e15, 1e12, 1e9, 1e6, 1e3, 1e0, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15
	};
	private static String[] PREFIXES 	= new String[] {
		"E",  "P",  "T",  "G", "M", "k", " ",  "m", "µ", "n", "p",  "f"
	};
	private String prefix;
	private double scaledValue;
	private int scaleIndex;

	ValueScaler(double value) {
		this(value, NO_SCALE, 1000.0);
	}
	
	ValueScaler(double value, double base) {
		setBase(base);
		scaleValue(value, NO_SCALE);
	}

	ValueScaler(double value, int scaleIndex, double base) {
		setBase(base);
		scaleValue(value, scaleIndex);
	}
	
	ValueScaler(double value, int scaleIndex) {
		setBase(1000.0);
		scaleValue(value, scaleIndex);
	}

	private void scaleValue( double value, int scaleIndex)
	{
		double absValue = Math.abs(value);
		if (scaleIndex == NO_SCALE) 
		{
			this.prefix 		= " ";
			this.scaledValue 	= value;
			
			for (int i = 0; i < VALUES.length; i++) 
			{
				if (absValue >= VALUES[i] && absValue < VALUES[i] * base) 
				{
					if ( VALUES[i] != 1e-3 )	// Special case, is treated in the GPRINT section
					{
						this.prefix 		= PREFIXES[i];
						this.scaledValue 	= value / VALUES[i];
						this.scaleIndex 	= i;
						return;
					}
				}
			}
		}
		else {
			this.prefix 		= PREFIXES[scaleIndex];
			this.scaledValue 	= value / VALUES[scaleIndex];
			this.scaleIndex 	= scaleIndex;
		}
	}
	
	String getPrefix() {
		return prefix;
	}

	double getScaledValue() {
		return scaledValue;
	}

	public int getScaleIndex() {
		return scaleIndex;
	}
	
	public void setBase( double baseValue ) 
	{
		this.base			= baseValue;
		double tmp 			= 1;
		for (int i = 1; i < 7; i++) {
			tmp 			*= baseValue;
			VALUES[6 - i] 	= tmp;
		}
		tmp = 1;
		for (int i = 7; i < VALUES.length; i++) {
			tmp				*= baseValue;
			VALUES[i]		 = ( 1 / tmp );
		}
	}
	
	public double getBase() {
		return this.base;
	}
}
