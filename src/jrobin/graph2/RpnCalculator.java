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

import java.util.ArrayList;

import jrobin.core.Util;
import jrobin.core.RrdException;

/**
 * <p>Used to calculate result of an RPN expression.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
public class RpnCalculator 
{
	// ================================================================
	// -- Members
	// ================================================================	
	// Token definitions
	public static final byte TKN_CONSTANT	= 0;
	public static final byte TKN_DATASOURCE	= 1;
	public static final byte TKN_PLUS		= 2;
	public static final byte TKN_MINUS		= 3;
	public static final byte TKN_MULTIPLY	= 4;
	public static final byte TKN_DIVIDE		= 5;
	public static final byte TKN_MOD		= 6;
	public static final byte TKN_SIN		= 7;
	public static final byte TKN_COS		= 8;
	public static final byte TKN_LOG		= 9;
	public static final byte TKN_EXP		= 10;
	public static final byte TKN_FLOOR		= 11;
	public static final byte TKN_CEIL		= 12;
	public static final byte TKN_ROUND		= 13;
	public static final byte TKN_POW		= 14;
	public static final byte TKN_ABS		= 15;
	public static final byte TKN_SQRT		= 16;
	public static final byte TKN_RANDOM		= 17;
	public static final byte TKN_LT			= 18;
	public static final byte TKN_LE			= 19;
	public static final byte TKN_GT			= 20;
	public static final byte TKN_GE			= 21;
	public static final byte TKN_EQ			= 22;
	public static final byte TKN_IF			= 23;
	public static final byte TKN_MIN		= 24;
	public static final byte TKN_MAX		= 25;
	public static final byte TKN_LIMIT		= 26;
	public static final byte TKN_DUP		= 27;
	public static final byte TKN_EXC		= 28;
	public static final byte TKN_POP		= 29;
	public static final byte TKN_UN			= 30;
	public static final byte TKN_UNKN		= 31;
	public static final byte TKN_NOW		= 32;
	public static final byte TKN_TIME		= 33;
	public static final byte TKN_PI			= 34;
	public static final byte TKN_E			= 35;
	public static final byte TKN_AND		= 36;
	public static final byte TKN_OR			= 37;
	public static final byte TKN_XOR		= 38;
	
	private Source[] sources;
	private ArrayList stack = new ArrayList();
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a RPN calculator object by providing the source array to use for value lookups.
	 * @param sources Table containing all retrieved datasources of the graph definition.
	 */
	RpnCalculator( Source[] sources )
	{
		this.sources = sources;
	}
	
	
	// ================================================================
	// -- Public methods
	// ================================================================	
	/**
	 * Evaluates a Cdef RPN expression into a single value.
	 * @param cdef Cdef object representing the parsed RPN expression.
	 * @param row Row index in the source table to retrieve all necessary values.
	 * @param timestamp Timestamp of the datapoint for which the value should be calculated.
	 * @return Calculated double value of the requested datapoint.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public double evaluate( Cdef cdef, int row, long timestamp ) throws RrdException
	{
		stack.clear();
		
		byte[] tokens 		= cdef.getTokens();
		int[] dsIndices		= cdef.getDsIndices();
		double[] constants	= cdef.getConstants();
		
		double x1, x2, x3;
		
		for ( int i = 0; i < tokens.length; i++ ) 
		{
			switch ( tokens[i] )
			{
				case TKN_CONSTANT:
					push( constants[i] );
					break;
					
				case TKN_DATASOURCE:
					push( sources[ dsIndices[i] ].get(row) );
					break;
					
				case TKN_PLUS:
					push(pop() + pop());
					break;
					
				case TKN_MINUS:
					x2 = pop();
					x1 = pop();
					push(x1 - x2);
					break;
					
				case TKN_MULTIPLY:
					push(pop() * pop());
					break;
					
				case TKN_DIVIDE:
					x2 = pop();
					x1 = pop();
					push(x1 / x2);
					break;
					
				case TKN_MOD:
					x2 = pop();
					x1 = pop();
					push(x1 % x2);
					break;
					
				case TKN_SIN:
					push(Math.sin(pop()));
					break;
					
				case TKN_COS:
					push(Math.cos(pop()));
					break;
					
				case TKN_LOG:
					push(Math.log(pop()));
					break;
					
				case TKN_EXP:
					push(Math.exp(pop()));
					break;
					
				case TKN_FLOOR:
					push(Math.floor(pop()));
					break;
					
				case TKN_CEIL:
					push(Math.ceil(pop()));
					break;
					
				case TKN_ROUND:
					push(Math.round(pop()));
					break;
					
				case TKN_POW:
					x2 = pop();
					x1 = pop();
					push(Math.pow(x1, x2));
					break;
					
				case TKN_ABS:
					push(Math.abs(pop()));
					break;
					
				case TKN_SQRT:
					push(Math.sqrt(pop()));
					break;
					
				case TKN_RANDOM:
					push(Math.random());
					break;
					
				case TKN_LT:
					x2 = pop();
					x1 = pop();
					push(x1 < x2? 1: 0);
					break;
					
				case TKN_LE:
					x2 = pop();
					x1 = pop();
					push(x1 <= x2? 1: 0);
					break;
					
				case TKN_GT:
					x2 = pop();
					x1 = pop();
					push(x1 > x2? 1: 0);
					break;
					
				case TKN_GE:
					x2 = pop();
					x1 = pop();
					push(x1 >= x2? 1: 0);
					break;
					
				case TKN_EQ:
					x2 = pop();
					x1 = pop();
					push(x1 == x2? 1: 0);
					break;
					
				case TKN_IF:
					x3 = pop();
					x2 = pop();
					x1 = pop();
					push(x1 != 0 ? x2: x3);
					break;
					
				case TKN_MIN:
					push(Math.min(pop(), pop()));
					break;
					
				case TKN_MAX:
					push(Math.max(pop(), pop()));
					break;
					
				case TKN_LIMIT:
					double high = pop(), low = pop(), value = pop();
					push(value < low || value > high? Double.NaN: value);
					break;
					
				case TKN_DUP:
					double x = pop();
					push(x);
					push(x);
					break;
					
				case TKN_EXC:
					x2 = pop();
					x1 = pop();
					push(x2);
					push(x1);
					break;
					
				case TKN_POP:
					pop();
					break;
					
				case TKN_UN:
					push(Double.isNaN(pop())? 1: 0);
					break;
					
				case TKN_UNKN:
					push(Double.NaN);
					break;
					
				case TKN_NOW:
					push(Util.getTime());
					break;
					
				case TKN_TIME:
					push(timestamp);
					break;
					
				case TKN_PI:
					push(Math.PI);
					break;
					
				case TKN_E:
					push(Math.E);
					break;
					
				case TKN_AND:
					x2 = pop();
					x1 = pop();
					push((x1 != 0 && x2 != 0)? 1: 0);
					break;
					
				case TKN_OR:
					x2 = pop();
					x1 = pop();
					push((x1 != 0 || x2 != 0)? 1: 0);
					break;
					
				case TKN_XOR:
					x2 = pop();
					x1 = pop();
					push(((x1 != 0 && x2 == 0) || (x1 == 0 && x2 != 0))? 1: 0);
					break;
			}
		}
		
		if (stack.size() != 1)
			throw new RrdException("RPN error, invalid stack length");
		
		return pop();
	}
	
	
	// ================================================================
	// -- Private methods
	// ================================================================	
	/**
	 * Pushes as a double value on the internal stack.
	 * @param value Value to push on the stack.
	 */
	private void push( double value ) 
	{
		stack.add( new Double(value) );
	}

	/**
	 * Pops a double value off the internal stack.
	 * @return Value popped off the stack.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	private double pop() throws RrdException 
	{
		int last = stack.size() - 1;
		if ( last < 0 )
			throw new RrdException("POP failed, stack empty");
		
		Double lastValue = (Double) stack.remove(last);
	
		return lastValue.doubleValue();
	}

}
