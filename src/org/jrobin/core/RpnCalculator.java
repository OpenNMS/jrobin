/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package org.jrobin.core;

import java.util.StringTokenizer;

class RpnCalculator {
	static final String VAR_PLACEHOLDER = "value";

	private static final byte TOK_VAR = 0;
	private static final byte TOK_NUM = 1;
	private static final byte TOK_PLUS = 2;
	private static final byte TOK_MINUS = 3;
	private static final byte TOK_MULT = 4;
	private static final byte TOK_DIV = 5;
	private static final byte TOK_MOD = 6;
	private static final byte TOK_SIN = 7;
	private static final byte TOK_COS = 8;
	private static final byte TOK_LOG = 9;
	private static final byte TOK_EXP = 10;
    private static final byte TOK_FLOOR = 11;
	private static final byte TOK_CEIL = 12;
	private static final byte TOK_ROUND = 13;
	private static final byte TOK_POW = 14;
	private static final byte TOK_ABS = 15;
	private static final byte TOK_SQRT = 16;
	private static final byte TOK_RANDOM = 17;
	private static final byte TOK_LT = 18;
	private static final byte TOK_LE = 19;
	private static final byte TOK_GT = 20;
	private static final byte TOK_GE = 21;
	private static final byte TOK_EQ = 22;
	private static final byte TOK_IF = 23;
	private static final byte TOK_MIN = 24;
	private static final byte TOK_MAX = 25;
	private static final byte TOK_LIMIT = 26;
	private static final byte TOK_DUP = 27;
	private static final byte TOK_EXC = 28;
	private static final byte TOK_POP = 29;
	private static final byte TOK_UN = 30;
	private static final byte TOK_UNKN = 31;
	// private static final byte TOK_NOW = 32;
	// private static final byte TOK_TIME = 33;
	private static final byte TOK_PI = 34;
	private static final byte TOK_E = 35;
	private static final byte TOK_AND = 36;
    private static final byte TOK_OR = 37;
	private static final byte TOK_XOR = 38;

	private String[] tokens;
	private byte[] tokenCodes;
	private double[] parsedDoubles;
	private RpnStack stack = new RpnStack();

	private String rpnExpression;
	private double value;
	// private long timestamp;

	RpnCalculator(String rpnExpression) throws RrdException {
		this.rpnExpression = rpnExpression;
		createTokens();
	}

	void setValue(double value) {
		this.value = value;
	}

	/* not supported yet
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	*/

	private void createTokens() throws RrdException {
		StringTokenizer st = new StringTokenizer(rpnExpression, ",");
		int count = st.countTokens();
		tokens = new String[count];
		tokenCodes = new byte[count];
		parsedDoubles = new double[count];
		for(int i = 0; st.hasMoreTokens(); i++) {
			String token = st.nextToken();
			tokens[i] = token;
			byte tokenCode = findTokenCode(token);
			tokenCodes[i] = tokenCode;
			if(tokenCode == TOK_NUM) {
				parsedDoubles[i] = Double.parseDouble(token);
			}
		}
	}

	private byte findTokenCode(String token) throws RrdException {
		if(isVariable(token)) {
			return TOK_VAR;
		}
		else if(isNumber(token)) {
			return TOK_NUM;
		}
		else if(token.equals("+")) {
			return TOK_PLUS;
		}
		else if(token.equals("-")) {
			return TOK_MINUS;
		}
		else if(token.equals("*")) {
			return TOK_MULT;
		}
		else if(token.equals("/")) {
			return TOK_DIV;
		}
		else if(token.equals("%")) {
			return TOK_MOD;
		}
		else if(token.equals("SIN")) {
			return TOK_SIN;
		}
		else if(token.equals("COS")) {
			return TOK_COS;
		}
		else if(token.equals("LOG")) {
			return TOK_LOG;
		}
		else if(token.equals("EXP")) {
			return TOK_EXP;
		}
		else if(token.equals("FLOOR")) {
			return TOK_FLOOR;
		}
		else if(token.equals("CEIL")) {
			return TOK_CEIL;
		}
		else if(token.equals("ROUND")) {
			return TOK_ROUND;
		}
		else if(token.equals("POW")) {
			return TOK_POW;
		}
		else if(token.equals("ABS")) {
			return TOK_ABS;
		}
		else if(token.equals("SQRT")) {
			return TOK_SQRT;
		}
		else if(token.equals("RANDOM")) {
			return TOK_RANDOM;
		}
		else if(token.equals("LT")) {
			return TOK_LT;
		}
		else if(token.equals("LE")) {
			return TOK_LE;
		}
		else if(token.equals("GT")) {
			return TOK_GT;
		}
		else if(token.equals("GE")) {
			return TOK_GE;
		}
		else if(token.equals("EQ")) {
			return TOK_EQ;
		}
		else if(token.equals("IF")) {
			return TOK_IF;
		}
		else if(token.equals("MIN")) {
			return TOK_MIN;
		}
		else if(token.equals("MAX")) {
			return TOK_MAX;
		}
		else if(token.equals("LIMIT")) {
			return TOK_LIMIT;
		}
		else if(token.equals("DUP")) {
			return TOK_DUP;
		}
		else if(token.equals("EXC")) {
			return TOK_EXC;
		}
		else if(token.equals("POP")) {
			return TOK_POP;
		}
		else if(token.equals("UN")) {
			return TOK_UN;
		}
		else if(token.equals("UNKN")) {
			return TOK_UNKN;
		}

		/* not supported yet
		else if(token.equals("NOW")) {
			return TOK_NOW;
		}
		else if(token.equals("TIME")) {
			return TOK_TIME;
		}
		*/
		else if(token.equals("PI")) {
			return TOK_PI;
		}
		else if(token.equals("E")) {
			return TOK_E;
		}
		else if(token.equals("AND")) {
			return TOK_AND;
		}
		else if(token.equals("OR")) {
			return TOK_OR;
		}
		else if(token.equals("XOR")) {
			return TOK_XOR;
		}
		else {
			throw new RrdException("Unknown RPN token encountered: " + token);
		}
	}

	private static boolean isNumber(String token) {
		try {
			Double.parseDouble(token);
			return true;
		}
		catch(NumberFormatException nfe) {
			return false;
		}
	}

	private static boolean isVariable(String token) {
		return token.equals(VAR_PLACEHOLDER);
	}

	double calculate() throws RrdException {
		resetCalculator();
		for(int i = 0; i < tokenCodes.length; i++) {
			byte tokenCode = tokenCodes[i];
			double x1, x2, x3;
			switch(tokenCode) {
				case TOK_NUM:
					push(parsedDoubles[i]); break;
				case TOK_VAR:
					push(value); break;
				case TOK_PLUS:
					push(pop() + pop()); break;
				case TOK_MINUS:
					x2 = pop(); x1 = pop(); push(x1 - x2); break;
				case TOK_MULT:
					push(pop() * pop()); break;
				case TOK_DIV:
					x2 = pop(); x1 = pop();	push(x1 / x2); break;
				case TOK_MOD:
					x2 = pop(); x1 = pop(); push(x1 % x2); break;
				case TOK_SIN:
					push(Math.sin(pop())); break;
				case TOK_COS:
					push(Math.cos(pop())); break;
				case TOK_LOG:
					push(Math.log(pop())); break;
				case TOK_EXP:
					push(Math.exp(pop())); break;
				case TOK_FLOOR:
					push(Math.floor(pop())); break;
				case TOK_CEIL:
					push(Math.ceil(pop())); break;
				case TOK_ROUND:
					push(Math.round(pop())); break;
				case TOK_POW:
					x2 = pop(); x1 = pop();	push(Math.pow(x1, x2)); break;
				case TOK_ABS:
					push(Math.abs(pop())); break;
				case TOK_SQRT:
					push(Math.sqrt(pop())); break;
				case TOK_RANDOM:
					push(Math.random()); break;
				case TOK_LT:
					x2 = pop(); x1 = pop(); push(x1 < x2? 1: 0); break;
				case TOK_LE:
					x2 = pop(); x1 = pop(); push(x1 <= x2? 1: 0); break;
				case TOK_GT:
					x2 = pop(); x1 = pop(); push(x1 > x2? 1: 0); break;
				case TOK_GE:
					x2 = pop(); x1 = pop(); push(x1 >= x2? 1: 0); break;
				case TOK_EQ:
					x2 = pop(); x1 = pop();	push(x1 == x2? 1: 0); break;
				case TOK_IF:
					x3 = pop(); x2 = pop(); x1 = pop();	push(x1 != 0? x2: x3); break;
				case TOK_MIN:
					push(Math.min(pop(), pop())); break;
				case TOK_MAX:
					push(Math.max(pop(), pop())); break;
				case TOK_LIMIT:
					x3 = pop(); x2 = pop(); x1 = pop();
					push(x1 < x2 || x1 > x3? Double.NaN: x1); break;
				case TOK_DUP:
					x1 = pop(); push(x1); push(x1); break;
				case TOK_EXC:
					x2 = pop(); x1 = pop();	push(x2); push(x1); break;
				case TOK_POP:
					pop(); break;
				case TOK_UN:
					push(Double.isNaN(pop())? 1: 0); break;
				case TOK_UNKN:
					push(Double.NaN); break;
				/* not supported yet
				case TOK_NOW:
					push(Util.getTime()); break;
				case TOK_TIME:
					push(timestamp); break;
				*/
				case TOK_PI:
					push(Math.PI); break;
				case TOK_E:
					push(Math.E); break;
				case TOK_AND:
					x2 = pop(); x1 = pop();	push((x1 != 0 && x2 != 0)? 1: 0); break;
				case TOK_OR:
					x2 = pop(); x1 = pop();	push((x1 != 0 || x2 != 0)? 1: 0); break;
				case TOK_XOR:
					x2 = pop(); x1 = pop();
					push(((x1 != 0 && x2 == 0) || (x1 == 0 && x2 != 0))? 1: 0); break;
				default:
					throw new RrdException("Unexpected RPN token encountered [" +
						tokenCode + "]");
			}
		}
        double retVal = pop();
		if(!isStackEmpty()) {
			throw new RrdException("Stack not empty at the end of calculation. " +
				"Probably bad RPN expression");
		}
		return retVal;
	}

	void push(double x) throws RrdException {
		stack.push(x);
	}

	double pop() throws RrdException {
		return stack.pop();
	}

	void resetCalculator() {
		stack.reset();
	}

	boolean isStackEmpty() {
		return stack.isEmpty();
	}

	class RpnStack {
		static final int MAX_STACK_SIZE = 1000;
		private double[] stack = new double[MAX_STACK_SIZE];
		private int pos = 0;

		void push(double x) throws RrdException {
			if(pos >= MAX_STACK_SIZE) {
				throw new RrdException(
					"PUSH failed, RPN stack full [" + MAX_STACK_SIZE + "]");
			}
			stack[pos++] = x;
		}

		double pop() throws RrdException {
			if(pos <= 0) {
				throw new RrdException("POP failed, RPN stack is empty ");
			}
			return stack[--pos];
		}

		void reset() {
			pos = 0;
		}

		boolean isEmpty() {
			return pos == 0;
		}
	}

/*
	public static void main(String[] args) throws RrdException {
		RpnCalculator c = new RpnCalculator("2,3,/,value,+");
		c.setValue(5);
		System.out.println(c.calculate());
	}
*/
}
