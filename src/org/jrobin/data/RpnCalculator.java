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

package org.jrobin.data;

import org.jrobin.core.Util;
import org.jrobin.core.RrdException;

import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;

/**
 * Class which implements simple RPN calculator (RRDTool-like). <p>
 * To calculate the value of expression:
 * <pre>
 * square_root[(x + y) * (x-y)]
 * </pre>
 * for <code>x=5, y=4</code>, than for <code>x=6, y=4</code>, use the following code:
 * <pre>
 * RpnCalculator c = new RpnCalculator("x,y,+,x,y,-,*,SQRT");
 * c.setValue("x", 5);
 * c.setValue("y", 4);
 * System.out.println(c.calculate());
 * // change the value of "x", and leave "y" as before
 * c.setValue("x", 6);
 * System.out.println(c.calculate());
 * </pre>
 * Notes:<p>
 * <ul>
 * <li>If you call the {@link #setValue(double)} method with just one double argument,
 * it will set the value of variable named "value" by default.
 * <li>The method {@link #setTimestamp(long)} will set the value of variable "timestamp".
 * This special variable can be referenced in the RPN expression by using the token TIME.
 * <li>Once set, variable values remain preserved between consecutive {@link #calculate()} calls. You can overwrite
 * this values by calling the {@link #setValue(String, double)} method again. To get rid of all variable values,
 * use method {@link #clearValues()}.
 * </ul>
 *
 */
public class RpnCalculator {
	/** Default variable name for the {@link #setValue(double)} method ("value") */
	public static final String VALUE_PLACEHOLDER = "value";
	/** Default variable name for the {@link #setTimestamp(long)} method ("timestamp") */
	public static final String TIMESTAMP_PLACEHOLDER = "timestamp";

	private static final byte TKN_VAR 		= 0;
	private static final byte TKN_NUM 		= 1;
	private static final byte TKN_PLUS 		= 2;
	private static final byte TKN_MINUS 	= 3;
	private static final byte TKN_MULT 		= 4;
	private static final byte TKN_DIV 		= 5;
	private static final byte TKN_MOD 		= 6;
	private static final byte TKN_SIN 		= 7;
	private static final byte TKN_COS 		= 8;
	private static final byte TKN_LOG 		= 9;
	private static final byte TKN_EXP 		= 10;
    private static final byte TKN_FLOOR 	= 11;
	private static final byte TKN_CEIL 		= 12;
	private static final byte TKN_ROUND 	= 13;
	private static final byte TKN_POW 		= 14;
	private static final byte TKN_ABS 		= 15;
	private static final byte TKN_SQRT 		= 16;
	private static final byte TKN_RANDOM 	= 17;
	private static final byte TKN_LT 		= 18;
	private static final byte TKN_LE 		= 19;
	private static final byte TKN_GT 		= 20;
	private static final byte TKN_GE 		= 21;
	private static final byte TKN_EQ 		= 22;
	private static final byte TKN_IF 		= 23;
	private static final byte TKN_MIN 		= 24;
	private static final byte TKN_MAX 		= 25;
	private static final byte TKN_LIMIT 	= 26;
	private static final byte TKN_DUP 		= 27;
	private static final byte TKN_EXC 		= 28;
	private static final byte TKN_POP 		= 29;
	private static final byte TKN_UN 		= 30;
	private static final byte TKN_UNKN 		= 31;
	private static final byte TKN_NOW 		= 32;
	private static final byte TKN_TIME 		= 33;
	private static final byte TKN_PI 		= 34;
	private static final byte TKN_E 		= 35;
	private static final byte TKN_AND 		= 36;
    private static final byte TKN_OR 		= 37;
	private static final byte TKN_XOR 		= 38;

	private Map values = new HashMap();
	private Token[] tokens;
	private RpnStack stack = new RpnStack();

	private String rpnExpression;

	/**
	 * Creates new RpnCalculator. RpnCalculator objects may be safely reused to calculate as many
	 * expression values (for different variable values) as needed.
	 * @param rpnExpression RPN expression to be used. RPN tokens should be comma (",")
	 * or space (" ") delimited.
	 */
	public RpnCalculator(String rpnExpression) {
		this.rpnExpression = rpnExpression;
		createTokens();
	}

	private void createTokens() {
		StringTokenizer st = new StringTokenizer(rpnExpression, ", ");
		tokens = new Token[st.countTokens()];
		for(int i = 0; st.hasMoreTokens(); i++) {
			tokens[i] = createToken(st.nextToken());
		}
	}

	private Token createToken(String str) {
		Token token = new Token(str);
		if(Util.isDouble(str)) {
			token.id = TKN_NUM;
			token.number = Util.parseDouble(str);
		}
		else if(str.equals("+")) {
			token.id = TKN_PLUS;
		}
		else if(str.equals("-")) {
			token.id = TKN_MINUS;
		}
		else if(str.equals("*")) {
			token.id = TKN_MULT;
		}
		else if(str.equals("/")) {
			token.id = TKN_DIV;
		}
		else if(str.equals("%")) {
			token.id = TKN_MOD;
		}
		else if(str.equals("SIN")) {
			token.id = TKN_SIN;
		}
		else if(str.equals("COS")) {
			token.id = TKN_COS;
		}
		else if(str.equals("LOG")) {
			token.id = TKN_LOG;
		}
		else if(str.equals("EXP")) {
			token.id = TKN_EXP;
		}
		else if(str.equals("FLOOR")) {
			token.id = TKN_FLOOR;
		}
		else if(str.equals("CEIL")) {
			token.id = TKN_CEIL;
		}
		else if(str.equals("ROUND")) {
			token.id = TKN_ROUND;
		}
		else if(str.equals("POW")) {
			token.id = TKN_POW;
		}
		else if(str.equals("ABS")) {
			token.id = TKN_ABS;
		}
		else if(str.equals("SQRT")) {
			token.id = TKN_SQRT;
		}
		else if(str.equals("RANDOM")) {
			token.id = TKN_RANDOM;
		}
		else if(str.equals("LT")) {
			token.id = TKN_LT;
		}
		else if(str.equals("LE")) {
			token.id = TKN_LE;
		}
		else if(str.equals("GT")) {
			token.id = TKN_GT;
		}
		else if(str.equals("GE")) {
			token.id = TKN_GE;
		}
		else if(str.equals("EQ")) {
			token.id = TKN_EQ;
		}
		else if(str.equals("IF")) {
			token.id = TKN_IF;
		}
		else if(str.equals("MIN")) {
			token.id = TKN_MIN;
		}
		else if(str.equals("MAX")) {
			token.id = TKN_MAX;
		}
		else if(str.equals("LIMIT")) {
			token.id = TKN_LIMIT;
		}
		else if(str.equals("DUP")) {
			token.id = TKN_DUP;
		}
		else if(str.equals("EXC")) {
			token.id = TKN_EXC;
		}
		else if(str.equals("POP")) {
			token.id = TKN_POP;
		}
		else if(str.equals("UN")) {
			token.id = TKN_UN;
		}
		else if(str.equals("UNKN")) {
			token.id = TKN_UNKN;
		}
		else if(str.equals("NOW")) {
			token.id = TKN_NOW;
		}
		else if(str.equals("TIME")) {
			token.id = TKN_TIME;
		}
		else if(str.equals("PI")) {
			token.id = TKN_PI;
		}
		else if(str.equals("E")) {
			token.id = TKN_E;
		}
		else if(str.equals("AND")) {
			token.id = TKN_AND;
		}
		else if(str.equals("OR")) {
			token.id = TKN_OR;
		}
		else if(str.equals("XOR")) {
			token.id = TKN_XOR;
		}
		else {
			token.id = TKN_VAR;
		}
		return token;
	}

	/**
	 * Sets the value for the default variable if RPN expression ("value").
	 * @param value Value to be used in calculation
	 */
	public void setValue(double value) {
		setValue(VALUE_PLACEHOLDER, value);
	}

	/**
	 * Sets the timestamp to be used in evaluation of the RPN expression. To use this
	 * value in the RPN expression, use token TIME.
	 * @param timestamp The value which will be used if token TIME is found in the RPN expression
	 */
	public void setTimestamp(long timestamp) {
		setValue(TIMESTAMP_PLACEHOLDER, timestamp);
	}

	/**
	 * Sets new value for a variable in the RPN expression.
	 * @param name Variable name
	 * @param value Variable value
	 */
	public void setValue(String name, double value) {
		values.put(name, new Double(value));
	}

	/**
	 * Clears all values specified for variables in the RPN expression
	 */
	public void clearValues() {
		values.clear();
	}

	/**
	 * Evaluates RPN expression, by replacing variable placeholders with specified values. You are free
	 * to call this method as many times as needed, with the same or modified variable values.
	 * @return The value of the RPN expression
	 * @throws org.jrobin.core.RrdException Thrown if some variable values are not specified before this method is called, or if the
	 * RPN expression is not valid.
	 */
	public double calculate() throws RrdException {
		resetStack();
		for(int i = 0; i < tokens.length; i++) {
			Token token = tokens[i];
			double x1, x2, x3;
			switch(token.id) {
				case TKN_NUM:
					push(token.number);
					break;
				case TKN_VAR:
					push(getValue(token.str));
					break;
				case TKN_PLUS:
					push(pop() + pop());
					break;
				case TKN_MINUS:
					x2 = pop(); x1 = pop();
					push(x1 - x2);
					break;
				case TKN_MULT:
					push(pop() * pop());
					break;
				case TKN_DIV:
					x2 = pop(); x1 = pop();
					push(x1 / x2);
					break;
				case TKN_MOD:
					x2 = pop(); x1 = pop();
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
					x2 = pop(); x1 = pop();
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
					x2 = pop(); x1 = pop();
					push(x1 < x2? 1: 0);
					break;
				case TKN_LE:
					x2 = pop(); x1 = pop();
					push(x1 <= x2? 1: 0);
					break;
				case TKN_GT:
					x2 = pop(); x1 = pop();
					push(x1 > x2? 1: 0);
					break;
				case TKN_GE:
					x2 = pop(); x1 = pop();
					push(x1 >= x2? 1: 0);
					break;
				case TKN_EQ:
					x2 = pop(); x1 = pop();
					push(x1 == x2? 1: 0);
					break;
				case TKN_IF:
					x3 = pop(); x2 = pop(); x1 = pop();
					push(x1 != 0? x2: x3);
					break;
				case TKN_MIN:
					push(Math.min(pop(), pop()));
					break;
				case TKN_MAX:
					push(Math.max(pop(), pop()));
					break;
				case TKN_LIMIT:
					x3 = pop(); x2 = pop(); x1 = pop();
					push(x1 < x2 || x1 > x3? Double.NaN: x1);
					break;
				case TKN_DUP:
					push(peek());
					break;
				case TKN_EXC:
					x2 = pop(); x1 = pop();
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
					push(getValue(TIMESTAMP_PLACEHOLDER));
					break;
				case TKN_PI:
					push(Math.PI);
					break;
				case TKN_E:
					push(Math.E);
					break;
				case TKN_AND:
					x2 = pop(); x1 = pop();
					push((x1 != 0 && x2 != 0)? 1: 0);
					break;
				case TKN_OR:
					x2 = pop(); x1 = pop();
					push((x1 != 0 || x2 != 0)? 1: 0);
					break;
				case TKN_XOR:
					x2 = pop(); x1 = pop();
					push(((x1 != 0 && x2 == 0) || (x1 == 0 && x2 != 0))? 1: 0);
					break;
				default:
					throw new RrdException("Unexpected RPN token encountered [" +
						token.id + "," + token.str + "]");
			}
		}
        double retVal = pop();
		if(!isStackEmpty()) {
			throw new RrdException("Stack not empty at the end of calculation. " +
					"Probably bad RPN expression [" + rpnExpression + "]");
		}
		return retVal;
	}

	private double getValue(String varName) throws RrdException {
		if(values.containsKey(varName)) {
			return ((Double) values.get(varName)).doubleValue();
		}
		throw new RrdException("Value of variable [" + varName + "] not specified");
	}

	private void push(double x) throws RrdException {
		stack.push(x);
	}

	private double pop() throws RrdException {
		return stack.pop();
	}

	private double peek() throws RrdException {
		return stack.peek();
	}

	private void resetStack() {
		stack.reset();
	}

	private boolean isStackEmpty() {
		return stack.isEmpty();
	}

	private class RpnStack {
		private static final int MAX_STACK_SIZE = 1000;
		private double[] stack = new double[MAX_STACK_SIZE];
		private int pos = 0;

		void push(double x) throws RrdException {
			if(pos >= MAX_STACK_SIZE) {
				throw new RrdException("PUSH failed, RPN stack full [" + MAX_STACK_SIZE + "]");
			}
			stack[pos++] = x;
		}

		double pop() throws RrdException {
			if(pos <= 0) {
				throw new RrdException("POP failed, RPN stack is empty ");
			}
			return stack[--pos];
		}

		double peek() throws RrdException {
			if(pos <= 0) {
				throw new RrdException("PEEK failed, RPN stack is empty ");
			}
			return stack[pos - 1];
		}

		void reset() {
			pos = 0;
		}

		boolean isEmpty() {
			return pos <= 0;
		}
	}

	private class Token {
		byte id;
		String str;
		double number;

		Token(String str) {
			this.str = str;
		}
	}

	/*
	public static void main(String[] args) throws RrdException {
		RpnCalculator c = new RpnCalculator("x,y,+,x,y,-,*,SQRT");
		c.setValue("x", 5);
		c.setValue("y", 4);
		System.out.println(c.calculate());
		c.setValue("x", 6);
		System.out.println(c.calculate());
	}
	*/
}
