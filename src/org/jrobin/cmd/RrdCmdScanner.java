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

package org.jrobin.cmd;

import org.jrobin.core.RrdException;

import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class RrdCmdScanner {
	private LinkedList words = new LinkedList();
	private StringBuffer buff;

	RrdCmdScanner(String command) throws RrdException {
		String cmd = command.trim();
		// parse words
		char activeQuote = 0;
		for(int i = 0; i < cmd.length(); i++) {
			char c = cmd.charAt(i);
			if((c == '"' || c == '\'') && activeQuote == 0) {
				// opening double or single quote
				initWord();
				activeQuote = c;
				continue;
			}
			if(c == activeQuote) {
				// closing quote
				activeQuote = 0;
				continue;
			}
			if(c == ' ' && activeQuote == 0) {
				// separator encountered
				finishWord();
				continue;
			}
			if(c == '\\' && activeQuote == '"' && i + 1 < cmd.length()) {
				// check for \" and \\ inside double quotes
				char c2 = cmd.charAt(i + 1);
				if(c2 == '\\' || c2 == '"') {
					appendWord(c2);
					i++;
					continue;
				}
			}
			// ordinary character
			appendWord(c);
		}
		if(activeQuote != 0) {
			throw new RrdException("End of command reached but " + activeQuote + " expected");
		}
		finishWord();
	}

	String getCmdType() {
		if(words.size() > 0) {
			return (String) words.get(0);
		}
		else {
			return null;
		}
	}

	private void appendWord(char c) {
		if(buff == null) {
			buff = new StringBuffer("");
		}
		buff.append(c);
	}

	private void finishWord() {
		if(buff != null) {
			words.add(buff.toString());
			buff = null;
		}
	}

	private void initWord() {
		if(buff == null) {
			buff = new StringBuffer("");
		}
	}

	void dump() {
		for(int i = 0; i < words.size(); i++) {
			System.out.println(words.get(i));
		}
	}

	String getOptionValue(String shortForm, String longForm, String defaultValue)
			throws RrdException {
		String value = getOptionValue("-" + shortForm);
		if(value == null) {
			value = getOptionValue("--" + longForm);
			if(value == null) {
				value = defaultValue;
			}
		}
		return value;
	}

	String getOptionValue(String shortForm, String longForm)
			throws RrdException {
		return getOptionValue(shortForm, longForm, null);
	}

	private String getOptionValue(String fullForm) throws RrdException {
		for(int i = 0; i < words.size(); i++) {
			String word = (String) words.get(i);
			if(word.equals(fullForm)) {
				// full match
				// the value is in the next word
				if(i + 1 < words.size()) {
					String value = (String) words.get(i + 1);
					words.remove(i + 1);
					words.remove(i);
					return value;
				}
				else {
					throw new RrdException("Value for option " + fullForm + " expected but not found");
				}
			}
			if(word.startsWith(fullForm)) {
				int pos = fullForm.length();
				if(word.charAt(pos) == '=') {
					// skip '=' if present
					pos++;
				}
				words.remove(i);
				return word.substring(pos);
			}
		}
		return null;
	}

	boolean getBooleanOption(String shortForm, String longForm) {
		for(int i = 0; i < words.size(); i++) {
			String word = (String) words.get(i);
			if(word.equals("-" + shortForm) || word.equals("--" + longForm)) {
				words.remove(i);
				return true;
			}
		}
		return false;
	}

	String[] getRemainingWords() {
		return (String[]) words.toArray(new String[0]);
	}

	public static void main(String[] args) {
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print("$ ");
				String s = r.readLine();
				RrdCmdScanner sc = new RrdCmdScanner(s);
				System.out.println("Value for option x is: [" + sc.getOptionValue("x", "xx") + "]");
			} catch (IOException e) {
				System.err.println(e);
			} catch (RrdException e) {
				System.err.println(e);
			}
		}
	}
}
