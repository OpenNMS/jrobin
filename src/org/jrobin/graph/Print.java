package org.jrobin.graph;

import org.jrobin.core.RrdException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This is a simplified version of the Gprint class, used for simple value formatting
 * with scaling.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Print
{
	// ================================================================
	// -- Members
	// ================================================================
	private static final String SCALE_MARKER 			= "@s";
	private static final String UNIFORM_SCALE_MARKER 	= "@S";
	private static final String VALUE_MARKER 			= "@([0-9]*\\.[0-9]{1}|[0-9]{1}|\\.[0-9]{1})";
	private static final Pattern VALUE_PATTERN 			= Pattern.compile(VALUE_MARKER);

	private int numDec									= 3;		// Show 3 decimal values by default
	private int strLen									= -1;
	private boolean normalScale							= false;
	private boolean uniformScale						= false;

	private ValueFormatter vFormat;


	// ================================================================
	// -- Constructor
	// ================================================================
	Print( double defaultBase, int scaleIndex )
	{
		vFormat = new ValueFormatter( defaultBase, scaleIndex );
	}


	// ================================================================
	// -- Protected methods
	// ================================================================
	String getFormattedString( double value, String format, double baseValue ) throws RrdException
	{
		// -- Parse the format
		checkValuePlacement( format );

		// -- Generate the formatted string
		double oldBase	= vFormat.getBase();
		vFormat.setBase( baseValue );

		vFormat.setFormat( value, numDec, strLen );
		vFormat.setScaling( normalScale, uniformScale );

		String valueStr = vFormat.getFormattedValue();
		String prefix	= vFormat.getPrefix();

		vFormat.setBase( oldBase );

		String str = format;

		str = str.replaceAll(VALUE_MARKER, valueStr);
		if ( normalScale ) str = str.replaceAll(SCALE_MARKER, prefix);
		if ( uniformScale ) str = str.replaceAll(UNIFORM_SCALE_MARKER, prefix);

		return str;
	}

	private void checkValuePlacement( String text ) throws RrdException
	{
		Matcher m = VALUE_PATTERN.matcher(text);

		if ( m.find() )
		{
			normalScale 	= (text.indexOf(SCALE_MARKER) >= 0);
			uniformScale	= (text.indexOf(UNIFORM_SCALE_MARKER) >= 0);

			if ( normalScale && uniformScale )
				throw new RrdException( "Can't specify normal scaling and uniform scaling at the same time." );

			String[] group 	= m.group(1).split("\\.");
			strLen 			= -1;
			numDec 			= 0;

			if ( group.length > 1 )
			{
				if ( group[0].length() > 0 ) {
					strLen 	= Integer.parseInt(group[0]);
					numDec 	= Integer.parseInt(group[1]);
				}
				else
					numDec 	= Integer.parseInt(group[1]);
			}
			else
				numDec = Integer.parseInt(group[0]);
		}
		else
			throw new RrdException( "Could not find where to place value. No @ placeholder found." );
	}
}
