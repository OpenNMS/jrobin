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

import jrobin.core.RrdException;
import jrobin.core.Util;

import java.util.ArrayList;

/**
 *
 */
class RpnCalculator {
	long timestamp;
	ValueCollection values;
	String[] tokens;
	ArrayList stack = new ArrayList();

	public RpnCalculator(long timestamp, ValueCollection values, String[] tokens) {
		this.timestamp = timestamp;
		this.values = values;
		this.tokens = tokens;
	}

	double evaluate() throws RrdException {
		for(int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if(isNumber(token)) {
				push(Double.parseDouble(token));
			}
			else if(values.contains(token)) {
				push(values.getValue(token));
			}
			else if(token.equals("+")) {
				push(pop() + pop());
			}
			else if(token.equals("-")) {
				double x2 = pop(), x1 = pop();
				push(x1 - x2);
			}
			else if(token.equals("*")) {
				push(pop() * pop());
			}
			else if(token.equals("/")) {
				double x2 = pop(), x1 = pop();
				push(x1 / x2);
			}
			else if(token.equals("%")) {
				double x2 = pop(), x1 = pop();
				push(x1 % x2);
			}
			else if(token.equals("SIN")) {
				push(Math.sin(pop()));
			}
			else if(token.equals("COS")) {
				push(Math.cos(pop()));
			}
			else if(token.equals("LOG")) {
				push(Math.log(pop()));
			}
			else if(token.equals("EXP")) {
				push(Math.exp(pop()));
			}
			else if(token.equals("FLOOR")) {
				push(Math.floor(pop()));
			}
			else if(token.equals("CEIL")) {
				push(Math.ceil(pop()));
			}
			else if(token.equals("ROUND")) {
				push(Math.round(pop()));
			}
			else if(token.equals("POW")) {
				double x2 = pop(), x1 = pop();
				push(Math.pow(x1, x2));
			}
			else if(token.equals("ABS")) {
				push(Math.abs(pop()));
			}
			else if(token.equals("SQRT")) {
				push(Math.sqrt(pop()));
			}
			else if(token.equals("RANDOM")) {
				push(Math.random());
			}
			else if(token.equals("LT")) {
				double x2 = pop(), x1 = pop();
				push(x1 < x2? 1: 0);
			}
			else if(token.equals("LE")) {
				double x2 = pop(), x1 = pop();
				push(x1 <= x2? 1: 0);
			}
			else if(token.equals("GT")) {
				double x2 = pop(), x1 = pop();
				push(x1 > x2? 1: 0);
			}
			else if(token.equals("GE")) {
				double x2 = pop(), x1 = pop();
				push(x1 >= x2? 1: 0);
			}
            else if(token.equals("EQ")) {
				double x2 = pop(), x1 = pop();
				push(x1 == x2? 1: 0);
			}
			else if(token.equals("IF")) {
				double x3 = pop(), x2 = pop(), x1 = pop();
				push(x1 != 0? x2: x3);
			}
			else if(token.equals("MIN")) {
				push(Math.min(pop(), pop()));
			}
			else if(token.equals("MAX")) {
				push(Math.max(pop(), pop()));
			}
			else if(token.equals("LIMT")) {
				double high = pop(), low = pop(), value = pop();
				push(value < low || value > high? Double.NaN: value);
			}
			else if(token.equals("DUP")) {
				double x = pop();
				push(x);
				push(x);
			}
			else if(token.equals("EXC")) {
				double x2 = pop(), x1 = pop();
				push(x2);
				push(x1);
			}
			else if(token.equals("POP")) {
				pop();
			}
			else if(token.equals("UN")) {
				push(Double.isNaN(pop())? 1: 0);
			}
			else if(token.equals("UNKN")) {
				push(Double.NaN);
			}
			else if(token.equals("NOW")) {
				push(Util.getTime());
			}
			else if(token.equals("TIME")) {
				push(timestamp);
			}
			else if(token.equals("PI")) {
				push(Math.PI);
			}
			else if(token.equals("E")) {
				push(Math.E);
			}
			// logical operators
			else if(token.equals("AND")) {
				double x2 = pop(), x1 = pop();
				push((x1 != 0 && x2 != 0)? 1: 0);
			}
			else if(token.equals("OR")) {
				double x2 = pop(), x1 = pop();
				push((x1 != 0 || x2 != 0)? 1: 0);
			}
			else if(token.equals("XOR")) {
				double x2 = pop(), x1 = pop();
				push(((x1 != 0 && x2 == 0) || (x1 == 0 && x2 != 0))? 1: 0);
			}
			else {
				throw new RrdException("Unknown token enocuntered: " + token);
			}
		}
		if(stack.size() != 1) {
			throw new RrdException("RPN error, invalid stack length");
		}
		return pop();
	}

	private boolean isNumber(String token) {
		try {
			Double.parseDouble(token);
			return true;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
	}

	private void push(double value) {
		stack.add(new Double(value));
	}

	private double pop() throws RrdException {
		int last = stack.size() - 1;
		if(last < 0) {
			throw new RrdException("POP failed, stack empty");
		}
		Double lastValue = (Double) stack.get(last);
		stack.remove(last);
		return lastValue.doubleValue();
	}

}
