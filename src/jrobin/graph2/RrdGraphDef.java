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

import java.io.File;
import java.io.Serializable;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;
import java.util.GregorianCalendar;

import jrobin.core.Util;
import jrobin.core.RrdException;

/**
 * <p>Class used to collect information for the new JRobin graph. JRobin graphs have many
 * options and this class has methods and properties to set them.</p>
 *
 * <p>At this moment, JRobin graphs are quite good looking, but RRDTool is still better.
 * However, JRobin graphs have almost the same potential as RRDTool's graph command. To learn
 * more about RRDTool's graphs see RRDTool's
 * <a href="../../../man/rrdgraph.html" target="man">rrdgraph man page</a>.</p> This man page
 * is important: JRobin uses the same concept of graph sources definition (DEF directives)
 * and supports RPN extensions in complex datasource definitions (RRDTool's CDEF directives).</p>
 * 
 * <p><code>RrdGraphDef</code> class does not actually create any graph. It just collects necessary information.
 * Graph will be created when you pass <code>RrdGraphDef</code> object to the constructor
 * of {@link jrobin.graph.RrdGraph RrdGraph} object.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
public class RrdGraphDef implements Serializable
{
	// ================================================================
	// -- Members
	// ================================================================
	long endTime				= Util.getTime();					// default time spam of the last 24 hours
	long startTime				= Util.getTime() - 86400L;
	
	Title title					= null;								// no title
	String valueAxisLabel		= null;								// no vertical label
	
	boolean gridX				= true;								// hide entire X axis grid (default: no)
	boolean gridY				= true;								// hide entire Y axis grid (default: no)
	boolean minorGridX			= true;								// hide minor X axis grid (default: no)
	boolean minorGridY			= true;								// hide minor Y axis grid (default: no)
	boolean majorGridX			= true;								// hide major X axis grid with labels (default: no)
	boolean majorGridY			= true;								// hide major Y axis grid with labels (default: no)
	boolean rigidGrid			= false;							// disable auto scaling of grid range (default: no)
	boolean frontGrid			= true;								// show grid in front of the chart (default: yes)
	boolean antiAliasing		= true;								// use anti-aliasing for the chart (default: yes)
	boolean showLegend			= true;								// show legend and comments (default: yes)
		
	Color backColor				= new Color( 245, 245, 245 );		// variation of light gray
	Color canvasColor			= Color.WHITE;						// white
	Color borderColor			= Color.LIGHT_GRAY;					// light gray, only applicable with a borderStroke
	Color normalFontColor		= Color.BLACK;						// black
	Color titleFontColor		= Color.BLACK;						// black
	Color majorGridColor		= new Color(130,30,30);				// variation of dark red
	Color minorGridColor		= new Color(140,140,140);			// variation of gray
	Color axisColor				= new Color(130,30,30);				// variation of dark red
	Color arrowColor			= Color.RED;						// red
	Color frameColor			= Color.LIGHT_GRAY;					// light gray
	
	Font titleFont 				= null;								// use default 'grapher' font
	Font normalFont 			= null;								// use default 'grapher' font
	
	File background				= null;								// no background image by default
	File overlay				= null;								// no overlay image by default
	
	int chart_lpadding			= Grapher.CHART_LPADDING;
	
	BasicStroke borderStroke	= null;								// defaults to standard beveled border
	
	double baseValue			= 1000;
	int scaleIndex				= -1;								// NO_SCALE
	GridRange gridRange			= null;
	
	int numDefs					= 0;
	int commentLines			= 0;
	int commentLineShift		= 0;
	HashMap fetchSources		= new HashMap();
	Vector cdefList				= new Vector();
	Vector plotDefs				= new Vector();
	Vector comments				= new Vector();
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new JRobin graph object. 
	 */
	public RrdGraphDef() 
	{
		// Default constructor
	}

	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.  
	 * Using timestamps defined as number of seconds since the epoch.
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in secons.
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public RrdGraphDef( long startTime, long endTime ) throws RrdException 
	{
		setTimePeriod( startTime, endTime );
	}
	
	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.
	 * Time spam defined using <code>java.util.Date</code> objects.
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public RrdGraphDef( Date start, Date end) throws RrdException
	{
		setTimePeriod( start, end );
	}
	
	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.
	 * Time spam defined using <code>java.util.GregorianCalendar</code> objects.
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public RrdGraphDef( GregorianCalendar start, GregorianCalendar end ) throws RrdException
	{
		setTimePeriod( start, end );
	}

	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Sets time span to be presented on the graph using timestamps.
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in secons.
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod( long startTime, long endTime ) throws RrdException 
	{
		if ( startTime < 0 || endTime <= startTime )
			throw new RrdException( "Invalid graph start/end time: " + startTime + "/" + endTime );
		
		this.startTime 	= startTime;
		this.endTime 	= endTime;
	}
	
	/**
	 * Sets time span to be presented on the graph using <code>java.util.Date</code> objects.
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public void setTimePeriod( Date start, Date end ) throws RrdException 
	{
		setTimePeriod( start.getTime() / 1000L, end.getTime() / 1000L );
	}

	/**
	 * Sets time span to be presented on the graph using <code>java.util.GregorianCalendar</code> objects.
	 * @param start Starting time.
	 * @param end Ending time
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod( GregorianCalendar start, GregorianCalendar end ) throws RrdException 
	{
		setTimePeriod( start.getTime(), end.getTime() );
	}
	
	/**
	 * Sets graph title.
	 * @param title Graph title.
	 */
	public void setTitle( String title ) throws RrdException
	{
		this.title = new Title( title );
	}

	/**
	 * Sets vertical (value) axis label.
	 * @param valueAxisLabel Axis label.
	 */
	public void setVerticalLabel( String valueAxisLabel) 
	{
		this.valueAxisLabel = valueAxisLabel;
	}
	
	/**
	 * Sets image background color. If not set, back color defaults to a very light gray.
	 * @param backColor Graph background color.
	 */
	public void setBackColor( Color backColor ) 
	{
		this.backColor = backColor;
	}

	/**
	 * Sets chart area background color. If not set, back color defaults to white.
	 * @param backColor Chart area background color.
	 */
	public void setCanvasColor( Color canvasColor ) 
	{
		this.canvasColor = canvasColor;		
	}
	
	/**
	 * Specifies the settings of the image border.
	 * Default is sort of beveled border around the image.
	 * @param c Bordercolor of the image.
	 * @param w Pixel width of the image border.
	 */
	public void setImageBorder( Color c, int w ) 
	{
		this.borderStroke		= new BasicStroke( w );
		if ( c != null )
			this.borderColor	= c;
	}
	
	/**
	 * Sets the color of the title font used in the graph.
	 * @param c The color to be used.
	 */
	public void setTitleFontColor( Color c ) 
	{
		this.titleFontColor = c;
	}
	
	/**
	 * Sets the color of the default font used in the graph.
	 * @param c The color to be used.
	 */
	public void setDefaultFontColor( Color c ) 
	{
		this.normalFontColor = c;
	}
	
	/**
	 * Sets the font to be used for the graph title.
	 * @param f The Font to be used.
	 */
	public void setTitleFont( Font f )
	{
		this.titleFont = f;
	}
	
	/**
	 * Sets the default font to be used in the graph.
	 * @param f The Font to be used.
	 */
	public void setDefaultFont( Font f )
	{
		this.normalFont = f;
	}
	
	/**
	 * Determines the color of the major grid.
	 * @param c Color to use.
	 */
	public void setMajorGridColor( Color c ) 
	{
		this.majorGridColor = c;	
	}

	/**
	 * Determines the color of the minor grid.
	 * @param c Color to use.
	 */
	public void setMinorGridColor( Color c ) 
	{
		this.minorGridColor = c;
	}

	/**
	 * Determines the color of chart area frame.
	 * @param c Color to use.
	 */
	public void setFrameColor( Color c ) 
	{
		this.frameColor = c;
	}

	/**
	 * Determines the color of X axis.
	 * @param c Color to use.
	 */
	public void setAxisColor( Color c ) 
	{
		this.axisColor = c;
	}

	/**
	 * Determines the color of the small axis arrow on the X axis.
	 * @param c Color to use.
	 */
	public void setArrowColor( Color c ) 
	{
		this.arrowColor = c;
	}
	
	/**
	 * Determines if the minor grid for the X axis needs to be drawn.
	 * @param visible True if minor grid needs to be drawn, false if not.
	 */
	public void setMinorGridX( boolean visible ) 
	{
		this.minorGridX = visible;
	}

	/**
	 * Determines if the minor grid for the Y axis needs to be drawn.
	 * @param visible True if minor grid needs to be drawn, false if not.
	 */
	public void setMinorGridY( boolean visible ) 
	{
		this.minorGridY = visible;
	}

	/**
	 * Determines if the major grid with labels for the X axis needs to be drawn.
	 * @param visible True if major grid needs to be drawn, false if not.
	 */
	public void setMajorGridX( boolean visible ) 
	{
		this.majorGridX = visible;
	}

	/**
	 * Determines if the major grid width labels for the Y axis needs to be drawn.
	 * @param visible True if major grid needs to be drawn, false if not.
	 */
	public void setMajorGridY( boolean visible ) 
	{
		this.majorGridY = visible;
	}

	/**
	 * Determines if the X axis grid should be drawn.
	 * This will not change the left padding of the drawing area.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridX( boolean visible ) 
	{
		this.gridX		= visible;
	}

	/**
	 * Determines if the Y axis grid should be drawn.
	 * This will not change the bottom padding of the drawing area.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridY( boolean visible ) 
	{
		this.gridY		= visible;
	}
	
	/**
	 * Determines if the grid should have rigid upper and lower limits.
	 * If so the upper and lower limit will not autoscale depending on the
	 * graph values.  Default uses grid autoscaling.
	 * @param rigid True if the grid should have rigid limits.
	 */
	public void setRigidGrid( boolean rigid ) 
	{
		this.rigidGrid = rigid;
	}

	/**
	 * Determine if the graph grid is in front of the graphs itself, or behind it.
	 * Default is in front of the graph itself.
	 * @param frontGrid True if the grid is in front of the graphs.
	 */
	public void setFrontGrid( boolean frontGrid ) 
	{
		this.frontGrid = frontGrid;
	}

	/**
	 * Determine if the legend should be visible or not, default: visible.
	 * @param showLegend True if the legend is visible.
	 */
	public void setShowLegend( boolean showLegend ) 
	{
		this.showLegend	= showLegend;
	}
	
	/**
	 * Set the anti-aliasing option for the drawing area of the graph.
	 * Default uses anti-aliasing.
	 * @param aa True if anti-aliasing is on, false if off
	 */
	public void setAntiAliasing( boolean aa ) 
	{
		this.antiAliasing = aa;
	}
	
	/**
	 * Set the number of pixels on the left of the canvas area ( value marker space ).
	 * @param lp Number of pixels used, defaults to 50.
	 */
	public void setChartLeftPadding( int lp ) 
	{
		this.chart_lpadding = lp;
	}

	/**
	 * <p>Adds simple graph source to graph definition. Graph source <code>name</code>
	 * can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.
	 * <li>To define complex graph sources
	 * (see {@link #datasource(java.lang.String, java.lang.String) complex graph
	 * source definition}).
	 * <li>To specify graph data source for the
	 * {@link #gprint(java.lang.String, java.lang.String, java.lang.String) gprint()} method.
	 * @param name Graph source name.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFun Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST").
	 */
	public void datasource( String name, String file, String dsName, String consolFunc ) throws RrdException
	{
		if ( fetchSources.containsKey(file) ) {
			FetchSource rf = (FetchSource) fetchSources.get(file);
			rf.addSource( consolFunc, dsName, name );	
		}
		else
			fetchSources.put( file, new FetchSource(file, consolFunc, dsName, name) );
		
		numDefs++;
	}
	
	/**
	 * <p>Adds complex graph source with the given name to the graph definition.
	 * Complex graph sources are evaluated using the supplied <code>rpn</code> expression.
	 *
	 * <p>Complex graph source <code>name</code> can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.
	 * <li>To define other complex graph sources.
	 * <li>To specify graph data source for the
	 * {@link #gprint(java.lang.String, java.lang.String, java.lang.String) gprint()} method.
	 *
	 * <p>JRobin supports the following RPN functions, operators and constants: +, -, *, /,
	 * %, SIN, COS, LOG, EXP, FLOOR, CEIL, ROUND, POW, ABS, SQRT, RANDOM, LT, LE, GT, GE, EQ,
	 * IF, MIN, MAX, LIMIT, DUP, EXC, POP, UN, UNKN, NOW, TIME, PI and E. JRobin does not
	 * force you to specify at least one simple graph source name as RRDTool.</p>
	 *
	 * <p>For more details on RPN see RRDTool's
	 * <a href="../../../man/rrdgraph.html" target="man">rrdgraph man page</a>.</p>
	 * @param name Graph source name.
	 * @param rpn RPN expression containig comma delmited simple and complex graph
	 * source names, RPN constants, functions and operators.
	 */
	public void datasource( String name, String rpn ) 
	{
		cdefList.add( new Cdef(name, rpn) );
	}
	
	/**
	 * Adds line plot to the graph definition, using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's LINE1 directive (line width
	 * is set to 1). There is only
	 * one limitation: so far, legends in JRobin graphs are always centered (don't
	 * try to specify alignment in the legend string).
	 *
	 * @param sourceName Graph source name.
	 * @param color Line collor to be used.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void line( String sourceName, Color color, String legend ) throws RrdException 
	{
		plotDefs.add( new PlotDef(sourceName, color) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds line plot to the graph definition, using the specified color, legend and line width.
	 * This method takes exactly the same parameters as RRDTool's LINE directive. There is only
	 * one limitation: so far, legends in JRobin graphs are always centered (don't
	 * try to specify alignment in the legend string).
	 *
	 * @param sourceName Graph source name.
	 * @param color Line collor to be used.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void line( String sourceName, Color color, String legend, int lineWidth ) throws RrdException 
	{
		plotDefs.add( new PlotDef(sourceName, color, lineWidth) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds area plot to the graph definition,
	 * using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's AREA directive. There is only
	 * one limitation: so far, legends in JRobin graphs are always centered (don't
	 * try to specify alignment in the legend string).
	 *
	 * @param sourceName Graph source name.
	 * @param color Filling collor to be used for area plot.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void area( String sourceName, Color color, String legend ) throws RrdException 
	{
		plotDefs.add( new Area(sourceName, color) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds stacked plot to the graph definition,
	 * using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's STACK directive. There is only
	 * one limitation: so far, legends in JRobin graphs are always centered (don't
	 * try to specify alignment in the legend string).
	 *
	 * @param sourceName Graph source name.
	 * @param color Collor to be used.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void stack( String sourceName, Color color, String legend ) throws RrdException 
	{
		plotDefs.add( new Stack(sourceName, color) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds comment to the graph definition. Comments will be left, center or right aligned
	 * if the comment ends with <code>@l</code>, <code>@c</code> or <code>@r</code>,
	 * respectively.
	 * @param text Comment
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void comment(String text) throws RrdException {
		addComment( new Comment(text) );
	}
	
	/**
	 * <p>Calculate the chosen consolidation function <code>consolFun</code> over
	 * the graph <code>sourceName</code> and prints the result
	 * on the graph using the specified <code>format</code> string.</p>
	 *
	 * <p>In the format string there should be a
	 * <code>@n</code> marker (replace <code>n</code> with the desired number of decimals)
	 * in the place where the number should be printed. If an additional <code>@s</code> is
	 * found in the format, the value will be scaled and an appropriate SI magnitude
	 * unit will be printed in place of the <code>@s</code> marker. If you specify
	 * <code>@S</code> instead of <code>@s</code>, the value will be scaled with the scale
	 * factor used in the last gprint directive (uniform value scaling).</p>
	 *
	 * <p>The text printed on the graph will be left, center or right aligned
	 * if the format string ends with <code>@l</code>, <code>@c</code> or <code>@r</code>,
	 * respectively.</p>
	 *
	 * @param sourceName Graph source name
	 * @param consolFun Consolidation function to be used for calculation ("AVERAGE",
	 * "MIN", "MAX" or "LAST")
	 * @param format Format string. For example: "speed is @2 @sbits/sec@c",
	 * "temperature = @0 degrees"
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void gprint(String sourceName, String consolFun, String format) throws RrdException 
	{
		addComment( new Gprint(sourceName, consolFun, format) );
	}
	
	/**
	 * Sets a background image to use for the graph.
	 * The image can be any of the supported imageio formats,
	 * default <i>.gif, .jpg or .png</i>.
	 * @param fileName Filename of the image to use
	 */
	public void setBackground( String fileName ) 
	{
		File bgFile	= new File( fileName );
		if ( bgFile.exists() )
			this.background = bgFile;
	}

	/**
	 * Sets a overlay image to use for the graph.
	 * The image can be any of the supported imageio formats,
	 * default <i>.gif, .jpg or .png</i>.  All pixels with the color white
	 * RGB (255, 255, 255) will be treated as transparent.
	 * @param fileName Filename of the image to use
	 */
	public void setOverlay( String fileName ) 
	{
		File ovFile	= new File( fileName );
		if ( ovFile.exists() )
			this.overlay = ovFile;
	}
	
	/**
	 * 
	 * @param base
	 */
	public void setBaseValue( double base ) {
		this.baseValue = base;
	}

	/**
	 * 
	 * @param e
	 */
	public void setUnitsExponent( int e ) {
		this.scaleIndex = (6 - e / 3);	// Index in the scale table
	}
	
	/**
	 * Sets value range that will be presented in the graph. If not set, graph will be
	 * autoscaled.
	 * @param lower Lower limit.
	 * @param upper Upper limit.
	 * @param rigid Rigid grid, won't autoscale limits.
	 */

	public void setGridRange(double lower, double upper, boolean rigid) 
	{
		gridRange = new GridRange( lower, upper, rigid );
	}
	
	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	PlotDef[] getPlotDefs()
	{
		return (PlotDef[]) plotDefs.toArray( new PlotDef[] {} );
	}
	
	Comment[] getComments()
	{
		return (Comment[]) comments.toArray( new Comment[] {} );
	}
	
	int getCommentLineCount()
	{
		return ( comments.size() > 0 ? commentLines + commentLineShift : 0 ); 
		//return ( comments.size() > 0 ? (commentLines > 0 ? commentLines : 1) : 0 );
	}
	
	private void addComment( Comment cmt )
	{
		commentLines 		+= cmt.getLineCount();
		commentLineShift	= (cmt.isCompleteLine() ? 0 : 1); 
		comments.add( cmt );
	}
	
	private void addLegend( String legend, Color color ) throws RrdException
	{
		if ( legend != null )
			addComment( new Legend(legend, color) );
	}
}
