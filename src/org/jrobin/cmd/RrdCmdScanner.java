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
	//private static final Pattern PATTERN = Pattern.compile("([^\"\\s]*\"[^\"]*\")|([^\"\\s]+)");

	private String cmdType;
	private String command;

	private LinkedList options	= new LinkedList();
	private LinkedList words 	= new LinkedList();

	RrdCmdScanner(String[] cmdWords) {
		for(int i = 0; i < cmdWords.length; i++) {
			words.add(cmdWords[i]);
			if(words.size() == 1) {
				cmdType = cmdWords[i];
			}
		}
	}

	RrdCmdScanner( String command )
	{
		// Set the command type only
		String cmd 		= command.trim();
		int typePos 	= command.indexOf( ' ' );
		cmdType			= cmd.substring( 0, typePos );

		this.command	= cmd.substring( typePos ).trim();

		//parseWords(command);
	}

	protected void parse( String[] keywords )
	{
		StringBuffer sbuf = new StringBuffer( "( -)");

		for ( int i = 0; i < keywords.length; i++ )
			sbuf.append( "|( " + keywords[i] + ":)" );

		Pattern pattern = Pattern.compile( sbuf.toString() );

		parseWords( command, pattern );
	}

	private void parseWords( String command, Pattern pattern )
	{
		int start = 0, stop = 0;

		Matcher m 		= pattern.matcher(command);

		while ( m.find() )
		{
			if ( start == 0 )
				start 	= m.start();
			else
			{
				stop 	= m.start();

				// Put this 'word' away
				storeWord( command.substring( start, stop ).trim() );

				// This is a new start
				start	= stop;
				stop	= 0;
			}
		}

		// Ok, see if we have to put the last word away
		if ( start > 0 && stop == 0 )
			storeWord( command.substring( start ).trim() );
	}

	private void storeWord( String word )
	{
		if ( word.charAt(0) == '-' )		// This is an option
			options.add( word );
		else								// This is a general 'word'
		{
			// TODO: Remove \ characters or other 'in between' characters that are used in scripting
			// TODO: Remove leading single and double quotes in text, make sure all special characters are detected and treated okay
			// TODO: Best way to try this probably to put the code from: http://www.jrobin.org/phpBB2/viewtopic.php?t=39 in a file
			// TODO: read that file in, and then see if the resulting string parses correctly
			words.add( word );
		}
	}

/*
	private void parseWords2(String command) {
		// Make this a bit more complex, read until ' -', or keyword
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
*/

	String getCmdType() {
		return cmdType;
	}

	String getOptionValue( String shortFormWord, String longFormWord ) throws RrdException
	{
		String shortForm 	= "-" + shortFormWord;
		String longForm		= "--" + longFormWord;

		for ( int i = 0; i < options.size(); i++ )
		{
			String value	= null;
			String option 	= (String) options.get( i );

			if ( shortForm != null && option.startsWith( shortForm ) )
				value = option.substring( shortForm.length() ).trim();
			else if ( longForm != null && option.startsWith( longForm ) )
			{
				// Next character might be =
				value = option.substring( longForm.length() ).trim();
				if ( value.length() > 1 && value.charAt(0) == '=' )
					value = value.substring( 1 );
			}

			if ( value != null )
			{
				options.remove( i );

				return value;
			}
		}

		// Option not found
		return null;
	}
/*
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
*/

	String getOptionValue(String shortForm, String longForm, String defaultValue) throws RrdException {
		String value = getOptionValue(shortForm, longForm);
		return value != null? value: defaultValue;
	}

	boolean getBooleanOption(String shortForm, String longForm) throws RrdException {
		return (getOptionValue( shortForm, longForm ) != null);
	}

/*
	boolean getBooleanOption2(String shortForm, String longForm) throws RrdException {
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
*/

	String[] getRemainingWords() {
		return (String[]) words.toArray(new String[0]);
	}
}
