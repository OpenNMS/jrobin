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
import java.util.StringTokenizer;

import jrobin.core.RrdException;

/**
 * <p>Represents a 'calculated' datasource for a graph.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
public class Cdef extends Source
{
	private String[] strTokens;
		
	double[] constants;
	int[] dsIndices;
	byte[] tokens;
	
	Cdef( String name, String rpn )
	{
		super(name);
		
		StringTokenizer st 	= new StringTokenizer(rpn, ",");
		int count 			= st.countTokens();
		strTokens 			= new String[count];
		
		for( int i = 0; st.hasMoreTokens(); i++ )
			strTokens[i] = st.nextToken().trim();	
	}
	
	void prepare( HashMap sourceIndex, int numPoints ) throws RrdException
	{
		// Create values table of correct size
		values = new double[numPoints];
		
		// Parse rpn expression for better performance
		String tkn;
		
		constants 	= new double[ strTokens.length ];
		dsIndices	= new int[ strTokens.length ];
		tokens		= new byte[ strTokens.length ];
		
		for (int i = 0; i < strTokens.length; i++)
		{
			tkn = strTokens[i];
			
			if ( isNumber(tkn) ) {
				tokens[i]		= RpnCalculator.TKN_CONSTANT;
				constants[i]	= Double.parseDouble(tkn);
			}
			else if ( sourceIndex.containsKey(tkn) ) {
				tokens[i]		= RpnCalculator.TKN_DATASOURCE;
				dsIndices[i]	= ( (Integer) sourceIndex.get(tkn) ).intValue();
			}
			else if ( tkn.equals("+") )
				tokens[i]		= RpnCalculator.TKN_PLUS;
			else if ( tkn.equals("-") )
				tokens[i]		= RpnCalculator.TKN_MINUS;
			else if ( tkn.equals("*") )
				tokens[i]		= RpnCalculator.TKN_MULTIPLY;
			else if ( tkn.equals("/") )
				tokens[i]		= RpnCalculator.TKN_DIVIDE;
			else if ( tkn.equals("%") )
				tokens[i]		= RpnCalculator.TKN_MOD;
			else if ( tkn.equals("SIN") )
				tokens[i]		= RpnCalculator.TKN_SIN;
			else if ( tkn.equals("COS") )
				tokens[i]		= RpnCalculator.TKN_COS;
			else if ( tkn.equals("LOG") )
				tokens[i]		= RpnCalculator.TKN_LOG;
			else if ( tkn.equals("EXP") )
				tokens[i]		= RpnCalculator.TKN_EXP;
			else if ( tkn.equals("FLOOR") )
				tokens[i]		= RpnCalculator.TKN_FLOOR;
			else if ( tkn.equals("CEIL") )
				tokens[i]		= RpnCalculator.TKN_CEIL;
			else if ( tkn.equals("ROUND") )
				tokens[i]		= RpnCalculator.TKN_ROUND;
			else if ( tkn.equals("POW") )
				tokens[i]		= RpnCalculator.TKN_POW;
			else if ( tkn.equals("ABS") )
				tokens[i]		= RpnCalculator.TKN_ABS;
			else if ( tkn.equals("SQRT") )
				tokens[i]		= RpnCalculator.TKN_SQRT;
			else if ( tkn.equals("RANDOM") )
				tokens[i]		= RpnCalculator.TKN_RANDOM;
			else if ( tkn.equals("LT") )
				tokens[i]		= RpnCalculator.TKN_LT;
			else if ( tkn.equals("LE") )
				tokens[i]		= RpnCalculator.TKN_LE;
			else if ( tkn.equals("GT") )
				tokens[i]		= RpnCalculator.TKN_GT;
			else if ( tkn.equals("GE") )
				tokens[i]		= RpnCalculator.TKN_GE;
			else if ( tkn.equals("EQ") )
				tokens[i]		= RpnCalculator.TKN_EQ;
			else if ( tkn.equals("IF") )
				tokens[i]		= RpnCalculator.TKN_IF;
			else if ( tkn.equals("MIN") )
				tokens[i]		= RpnCalculator.TKN_MIN;
			else if ( tkn.equals("MAX") )
				tokens[i]		= RpnCalculator.TKN_MAX;
			else if ( tkn.equals("LIMIT") )
				tokens[i]		= RpnCalculator.TKN_LIMIT;
			else if ( tkn.equals("DUP") )
				tokens[i]		= RpnCalculator.TKN_DUP;
			else if ( tkn.equals("EXC") )
				tokens[i]		= RpnCalculator.TKN_EXC;
			else if ( tkn.equals("POP") )
				tokens[i]		= RpnCalculator.TKN_POP;
			else if ( tkn.equals("UN") )
				tokens[i]		= RpnCalculator.TKN_UN;
			else if ( tkn.equals("UNKN") )
				tokens[i]		= RpnCalculator.TKN_UNKN;
			else if ( tkn.equals("NOW") )
				tokens[i]		= RpnCalculator.TKN_NOW;
			else if ( tkn.equals("TIME") )
				tokens[i]		= RpnCalculator.TKN_TIME;
			else if ( tkn.equals("PI") )
				tokens[i]		= RpnCalculator.TKN_PI;
			else if ( tkn.equals("E") )
				tokens[i]		= RpnCalculator.TKN_E;
			else if ( tkn.equals("AND") )
				tokens[i]		= RpnCalculator.TKN_AND;
			else if ( tkn.equals("OR") )
				tokens[i]		= RpnCalculator.TKN_OR;
			else if ( tkn.equals("XOR") )
				tokens[i]		= RpnCalculator.TKN_XOR;
			else
				throw new RrdException("Unknown token enocuntered: " + tkn);	
			
		}
	}
		
	private boolean isNumber(String token) 
	{
		try 
		{
			Double.parseDouble(token);
			return true;
		}
		catch (NumberFormatException nfe) {
				return false;
		}
	}
	
	void set( int pos, long timestamp, double val )
	{
		super.set( pos, timestamp, val );
		values[pos] = val;
	}
	
}
