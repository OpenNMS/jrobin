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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class RrdCmdScanner {
	private static final Pattern PATTERN = Pattern.compile("([^\"\\s]*\"[^\"]*\")|([^\"\\s]+)");
	private LinkedList words = new LinkedList();
	private String cmdType;

	RrdCmdScanner(String[] cmdWords) {
		for(int i = 0; i < cmdWords.length; i++) {
			words.add(cmdWords[i]);
			if(words.size() == 1) {
				cmdType = cmdWords[i];
			}
		}
	}

	RrdCmdScanner(String command) {
		parseWords(command);
	}

	private void parseWords(String command) {
		Matcher m = PATTERN.matcher(command);
		while(m.find()) {
			String word = m.group();
			word = word.replaceAll("\"", "");
			// System.out.println("Adding: [" + word + "]");
			words.add(word);
			if(words.size() == 1) {
				cmdType = word;
			}
		}
	}

	String getCmdType() {
		return cmdType;
	}

	String getOptionValue(String shortForm, String longForm) throws RrdException {
		for(int i = 0; i < words.size(); i++) {
			String word = (String) words.get(i);
			if((shortForm != null && word.equals("-" + shortForm)) ||
				(longForm != null && word.equals("--" + longForm))) {
				// match found
				if(i < words.size() - 1) {
					// value available
					String value = (String) words.get(i + 1);
					words.remove(i + 1);
					words.remove(i);
					return value;
				}
				else {
					throw new RrdException("Option found but value is not available");
				}
			}
		}
		return null;
	}

	String getOptionValue(String shortForm, String longForm, String defaultValue) throws RrdException {
		String value = getOptionValue(shortForm, longForm);
		return value != null? value: defaultValue;
	}

	boolean getBooleanOption(String shortForm, String longForm) throws RrdException {
		for(int i = 0; i < words.size(); i++) {
			String word = (String) words.get(i);
			if((shortForm != null && word.equals("-" + shortForm)) ||
				(longForm != null && word.equals("--" + longForm))) {
				// match found
				words.remove(i);
				return true;
			}
		}
		return false;
	}

	String[] getRemainingWords() {
		return (String[]) words.toArray(new String[0]);
	}
}
