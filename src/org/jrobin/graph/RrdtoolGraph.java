package org.jrobin.graph;

import org.jrobin.core.RrdException;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: cbld
 * Date: 25-mei-2004
 * Time: 21:02:56
 * To change this template use File | Settings | File Templates.
 */
public class RrdtoolGraph
{
	// ================================================================
	// -- Constants
	// ================================================================
	private static final int SAVE_PNG		= 0;
	private static final int SAVE_GIF		= 1;

	private static final int DS_DEF			= 0;
	private static final int DS_CDEF		= 1;

	private static final int TXT_COMMENT	= 0;
	private static final int TXT_GPRINT		= 1;

	private static final int GRAPH_LINE		= 0;
	private static final int GRAPH_AREA		= 1;
	private static final int GRAPH_STACK	= 2;

	private static final int TKN_IGNORE		= -2;
	private static final int TKN_UNKNOWN	= -1;
	private static final int TKN_RRDTOOL	= 0;
	private static final int TKN_GRAPH		= 1;
	private static final int TKN_START		= 2;
	private static final int TKN_END		= 3;
	private static final int TKN_COMMENT	= 4;
	private static final int TKN_LINE		= 5;
	private static final int TKN_AREA		= 6;
	private static final int TKN_STACK		= 7;
	private static final int TKN_CDEF		= 8;
	private static final int TKN_DEF		= 9;
	private static final int TKN_GPRINT		= 11;
	private static final int TKN_HRULE		= 12;
	private static final int TKN_VRULE		= 13;
	private static final int TKN_STEP		= 14;
	private static final int TKN_TITLE		= 15;
	private static final int TKN_NOLEGEND	= 16;
	private static final int TKN_COLOR		= 17;
	private static final int TKN_RIGID		= 18;
	private static final int TKN_LOWERLIMIT	= 19;
	private static final int TKN_UPPERLIMIT	= 20;
	private static final int TKN_LAZY		= 21;
	private static final int TKN_OVERLAY	= 23;
	private static final int TKN_BACKGROUND	= 24;
	private static final int TKN_IMGFORMAT	= 25;
	private static final int TKN_WIDTH		= 26;
	private static final int TKN_HEIGHT		= 27;
	private static final int TKN_VERT_LABEL	= 28;
	private static final int TKN_UNITS_EXP	= 29;
	private static final int TKN_NOMINOR	= 30;
	private static final int TKN_XGRID		= 31;
	private static final int TKN_YGRID		= 32;
	private static final int TKN_BASE		= 33;


	// ================================================================
	// -- Members
	// ================================================================
	private String token			= "";
	private String script 			= null;
	private RrdGraphDef graphDef	= null;

	private int tokenPos			= 0;
	private char[] parseCmd			= new char[0];

	private boolean gridRigid		= false;
	private double gridLower		= Double.MAX_VALUE;
	private double gridUpper		= Double.MIN_VALUE;

	private int width				= 0;
	private int height				= 0;
	private int fileType			= SAVE_PNG;
	private String fileName			= "";

	// ================================================================
	// -- Constructors
	// ================================================================
	public RrdtoolGraph( String script )
	{
		this.script		= script;
	}


	// ================================================================
	// -- Public methods
	// ================================================================
	public RrdGraphDef getRrdGraphDef() throws RrdException
	{
		parseRrdtoolScript();

		return graphDef;
	}


	// ================================================================
	// -- Private methods
	// ================================================================
	/**
	 *
	 * @return
	 * @throws RrdException
	 */
	private boolean parseRrdtoolScript() throws RrdException
	{
		long startTime	= 0, stopTime = 0;

		graphDef	= new RrdGraphDef();

		// First replace all special whitespace chars by a space
		parseCmd 	= script.replace( '\n', ' ' ).replace( '\r', ' ' ).replace( '\t', ' ' ).toCharArray();

		while ( nextToken() > 0 )
		{
			System.err.println( token );
			switch ( parseToken( token ) )
			{
				case TKN_RRDTOOL:					// We don't care about this token
					break;

				case TKN_GRAPH:						// Next token is the filename
					nextToken();
					break;

				case TKN_START:						// Next token is the start time
					nextToken();
					startTime	= Long.parseLong( token );
					break;

				case TKN_END:						// Next token is the end time
					nextToken();
					stopTime	= Long.parseLong( token );
					break;

				case TKN_NOMINOR:					// Hide the entire minor grid
					graphDef.setMinorGridX( false );
					graphDef.setMinorGridY( false );
					break;

				case TKN_WIDTH:						// Next token is graph pixel width
					nextToken();
					width		= Integer.parseInt( token );
					break;

				case TKN_HEIGHT:					// Next token is graph pixel height
					nextToken();
					height		= Integer.parseInt( token );
					break;

				case TKN_UNITS_EXP:					// Next token is the units exponent value
					nextToken();
					graphDef.setUnitsExponent( Integer.parseInt( token ) );
					break;

				case TKN_VERT_LABEL:				// Next token is the actual vertical label text
					nextToken();
					graphDef.setVerticalLabel( unescape(token) );
					break;

				case TKN_TITLE:						// Next token is the actual title text
					nextToken();
					graphDef.setTitle( unescape(token) );
					break;

				case TKN_IMGFORMAT:					// Next token is the file type
					nextToken();
					if ( token.equalsIgnoreCase("gif") )
						fileType = SAVE_GIF;
					break;

				case TKN_BACKGROUND:				// Next token is the filename of background image
					nextToken();
					graphDef.setBackground( unescape(token) );
					break;

				case TKN_OVERLAY:					// Next token is the filename of background image
					nextToken();
					graphDef.setOverlay( unescape(token) );
					break;

				case TKN_NOLEGEND:					// Hide the legend
					graphDef.setShowLegend( false );
					break;

				case TKN_LOWERLIMIT:				// Next token is the lower limit value
					nextToken();
					gridLower	= Double.parseDouble(token);
					break;

				case TKN_UPPERLIMIT:				// Next token is the upper limit value
					nextToken();
					gridUpper	= Double.parseDouble(token);
					break;

				case TKN_RIGID:						// Set rigid grid
					gridRigid 	= true;
					break;

				case TKN_BASE:						// Set base value
					nextToken();
					graphDef.setBaseValue( Double.parseDouble(token) );
					break;

				case TKN_COMMENT:
					parseTextCommand( TXT_COMMENT );
					break;

				case TKN_GPRINT:
					parseTextCommand( TXT_GPRINT );
					break;

				case TKN_LINE:
					parseGraphCommand( GRAPH_LINE );
					break;

				case TKN_AREA:
					parseGraphCommand( GRAPH_AREA );
					break;

				case TKN_STACK:
					parseGraphCommand( GRAPH_STACK );
					break;

				case TKN_DEF:
					parseDatasource( DS_DEF );
					break;

				case TKN_CDEF:
					parseDatasource( DS_CDEF );
					break;

				case TKN_IGNORE:					// Do nothing
					break;

				case TKN_UNKNOWN:
					throw new RrdException( "Unknown token: " + token );
			}
		}

		// Set grid range if necessary
		if ( gridRigid || ( gridLower == Double.MAX_VALUE ) || ( gridUpper == Double.MIN_VALUE ) )
			graphDef.setGridRange( gridLower, gridUpper, gridRigid );

		return true;
	}

	/**
	 *
	 * @param type
	 * @throws RrdException
	 */
	private void parseGraphCommand( int type ) throws RrdException
	{
		if ( type == GRAPH_LINE )
		{
			int w	= Integer.parseInt( "" + token.charAt( 4 ) );

			// Get the datasource
			int pos		= token.indexOf( '#', 6 );
			int npos	= token.indexOf( ':', 6 );
			if ( pos < 0 ) pos = npos;

			String ds		= ( pos > 0 ? token.substring( 6, pos ) : token.substring( 6 ) );
			Color color		= null;
			String legend	= null;

			// Get the color
			if ( pos > 0 && token.charAt(pos) == '#' )
				color	= Color.decode(  npos > 0 ? token.substring( pos, npos ) : token.substring( pos ) );

			// Get the legend (if there is one)
			if ( npos > 0 )
				legend	= unescape( token.substring( npos + 1 ) );

			graphDef.line( ds, color, legend, w );
		}
		else
		{
			if ( type == GRAPH_STACK )
				token	= token.substring( 6 );
			else
				token	= token.substring( 5 );

			int pos		= token.indexOf( '#' );
			int npos	= token.indexOf( ':' );

			String ds		= ( pos > 0 ? token.substring( 0, pos ) : token.substring( 0 ) );
			Color color		= null;
			String legend	= null;

			// Get the color
			if ( pos > 0 && token.charAt(pos) == '#' )
				color	= Color.decode(  npos > 0 ? token.substring( pos, npos ) : token.substring( pos ) );

			// Get the legend (if there is one)
			if ( npos > 0 )
				legend	= unescape( token.substring( npos + 1 ) );

			if ( type == GRAPH_AREA )
				graphDef.area( ds, color, legend );
			else if ( type == GRAPH_STACK )
				graphDef.stack( ds, color, legend );
		}
	}

	/**
	 *
	 * @param text
	 * @return
	 */
	private String unescape( String text )
	{
		if ( text.startsWith( "'" ) || text.startsWith( "\"" ) )
			return text.substring( 1, text.length() - 1 );

		return text;
	}

	/**
	 *
	 * @param type
	 * @throws RrdException
	 */
	private void parseTextCommand( int type ) throws RrdException
	{
		int pos	= token.indexOf( ':' );

		if ( type == TXT_COMMENT )
		{
			String text	= unescape( token.substring( pos + 1 ) );

			graphDef.comment( text );
		}
		else if ( type == TXT_GPRINT )
		{
			// GPRINT:vname:CF:format
			int npos	= token.indexOf( ':', ++pos );

			String ds	= token.substring( pos, npos );
			pos			= token.indexOf( ':', ++npos );

			String cf	= token.substring( npos, pos );
			String text	= unescape( token.substring( pos + 1 ) );

			// Change the placeholder to JRobin
			//graphDef.gprint( ds, cf, text );
		}
	}

	/**
	 *
	 * @param type
	 * @throws RrdException
	 */
	private void parseDatasource( int type ) throws RrdException
	{
		// Fetch the name of the datasource
		int pos		= token.indexOf( ':' );
		int npos	= token.indexOf( '=', ++pos );

		String name	= token.substring( pos, npos );

		if ( type == DS_DEF )
		{
			// DEF:vname=rrd:ds-name:CF
			token			= token.substring( npos + 1 );

			// Fetch reverse
			pos				= token.lastIndexOf( ':' );
			String cf		= token.substring( pos + 1 );

			npos			= token.lastIndexOf( ':', pos - 1 );
			String dsName	= token.substring( npos + 1, pos );
			String rrdFile	= token.substring( 0, npos );

			graphDef.datasource( name, rrdFile, dsName, cf );
		}
		else
		{
			// CDEF:vname=rpn-expression
			graphDef.datasource( name, token.substring( npos + 1 ) );
		}
	}

	/**
	 * Reads the next token in the rrdtool script.
	 *
	 * @return
	 */
	private int nextToken()
	{
		char[] tknChars 		= new char[512];
		int charPos 			= 0;
		int cmdPos				= tokenPos;
		boolean found			= false;

		boolean stringComplete 	= true;
		char findChar			= ' ';

		/*
		 * This will read from the current position, till the next whitespace.
		 * However, if it encounters a " or a ' that is not escaped, it will read until the next matching character.
		 */
		while ( charPos < 512 && (cmdPos < parseCmd.length) && !found )
		{
			if ( parseCmd[cmdPos] == '"' )
			{
				if ( stringComplete ) {
					stringComplete 	= false;
					findChar		= '"';
				}
				else if ( findChar == '"' && !(parseCmd[cmdPos - 1] == '\\') )
					stringComplete	= true;
			}
			else if ( parseCmd[cmdPos] == '\'' )
			{
				if ( stringComplete ) {
					stringComplete 	= false;
					findChar		= '\'';
				}
				else if ( findChar == '\'' && !(parseCmd[cmdPos - 1] == '\\') )
					stringComplete	= true;
			}
			if ( stringComplete && parseCmd[cmdPos] == ' ' )
				found = true;
			else
				tknChars[charPos++] = parseCmd[cmdPos++];
		}

		token 	= new String( tknChars, 0, charPos ).trim();

		tokenPos = cmdPos + 1;

		return charPos;
	}

	/**
	 *
	 * @param token
	 * @return
	 */
	private int parseToken( String token )
	{
		if ( token.equalsIgnoreCase("rrdtool") )
			return TKN_RRDTOOL;

		if ( token.equalsIgnoreCase("graph") )
			return TKN_GRAPH;

		if ( token.equalsIgnoreCase("--start") || token.equals("-s") )
			return TKN_START;

		if ( token.equalsIgnoreCase("--end") || token.equals("-e") )
			return TKN_END;

		if ( token.equalsIgnoreCase("--width") || token.equals("-w") )
			return TKN_WIDTH;

		if ( token.equalsIgnoreCase("--height") || token.equals("-h") )
			return TKN_HEIGHT;

		if ( token.equalsIgnoreCase("--no-minor") )
			return TKN_NOMINOR;

		if ( token.equalsIgnoreCase("--units-exponent") || token.equals("-X") )
			return TKN_UNITS_EXP;

		if ( token.equalsIgnoreCase("--vertical-label") || token.equals("-v") )
			return TKN_VERT_LABEL;

		if ( token.equalsIgnoreCase("--imgformat") || token.equals("-a") )
			return TKN_IMGFORMAT;

		if ( token.equalsIgnoreCase("--background") || token.equals("-B") )
			return TKN_BACKGROUND;

		if ( token.equalsIgnoreCase("--overlay") || token.equals("-O") )
			return TKN_OVERLAY;

		if ( token.equalsIgnoreCase("--title") || token.equals("-t") )
			return TKN_TITLE;

		if ( token.equalsIgnoreCase("--step") || token.equals("-S") )
			return TKN_STEP;

		if ( token.equalsIgnoreCase("--no-legend") || token.equals("-g") )
			return TKN_NOLEGEND;

		if ( token.equalsIgnoreCase("--base") || token.equals("-b") )
			return TKN_BASE;

		if ( token.equalsIgnoreCase("--lower-limit") || token.equals("-l") )
			return TKN_LOWERLIMIT;

		if ( token.equalsIgnoreCase("--upper-limit") || token.equals("-u") )
			return TKN_UPPERLIMIT;

		if ( token.equalsIgnoreCase("--rigid") || token.equals("-r") )
			return TKN_RIGID;

		if ( token.startsWith("COMMENT") )
			return TKN_COMMENT;

		if ( token.startsWith("GPRINT") )
			return TKN_GPRINT;

		if ( token.startsWith("LINE") )
			return TKN_LINE;

		if ( token.startsWith("AREA") )
			return TKN_AREA;

		if ( token.startsWith("STACK") )
			return TKN_STACK;

		if ( token.startsWith("HRULE") )
			return TKN_HRULE;

		if ( token.startsWith("VRULE") )
			return TKN_VRULE;

		if ( token.startsWith("CDEF") )
			return TKN_CDEF;

		if ( token.startsWith("DEF") )
			return TKN_DEF;

		if ( token.equals("-Y") || token.equals("--alt-y-grid")	|| token.equals("-R") || token.equals("--alt-y-mrtg")
								|| token.equals("-A") || token.equals("--alt-autoscale") || token.equals("-M")
								|| token.equals("--alt-autoscale-max") || token.equals("-L")
								|| token.equals("--units-length") || token.equals("-i") || token.equals("--interlaced")
								|| token.equals("-f") || token.equals("--imginfo") || token.equals("-o")
								|| token.equals("--logarithmic") || token.equals("-j") || token.equals("--only-graph")
								|| token.equals("-F") || token.equals("--force-rules-legend") || token.startsWith("PRINT")
								|| token.equals("-U") || token.startsWith("--unit") )
			return TKN_IGNORE;

		return TKN_UNKNOWN;
	}


	public static void main( String[] args ) throws Exception
	{
		String str = "rrdtool graph FNAME --start 100 --end 700\n"
					+ "DEF:inOctets=c:/file.rrd:test-run:AVERAGE "
					+ "CDEF:bitIn=inOctets,8,* "
					+ "COMMENT:'commentaar \"nr\" 1'\n"
					+ "AREA:test#ffa9b3:'this is the legend'\n"
					+ "COMMENT:\"commentaar 'nr' 2\"\n"
					+ "LINE2:test2:'this is the legend two' "
					+ "GPRINT:bitIn:AVG:'Average %2.5'";

		RrdtoolGraph rg = new RrdtoolGraph( str );

		rg.getRrdGraphDef();
	}
}
