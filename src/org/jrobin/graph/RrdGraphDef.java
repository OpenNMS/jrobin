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
package org.jrobin.graph;

import java.io.*;
import java.awt.Font;
import java.awt.Color;
import java.awt.BasicStroke;
import java.util.Date;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import org.jrobin.core.Util;
import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Class used to collect information for a JRobin graph. JRobin graphs have many
 * options and this class has methods and properties to set them.</p>
 *
 * <p>The JRobin graph package was designed to create graphs that have the same look as the 
 * RRDTool counter parts.  Almost all the same graphing options are available, with some extra's
 * like more advanced text alignment and custom point-to-point lines and area's.</p>
 * 
 * <p>To learn more about RDTool's graphs see RRDTool's
 * <a href="../../../../man/rrdgraph.html" target="man">rrdgraph man page</a>.  This man page
 * is important: JRobin uses the same concept of graph sources definition (DEF directives)
 * and supports RPN extensions in complex datasource definitions (RRDTool's CDEF directives).</p>
 * 
 * <p><code>RrdGraphDef</code> class does not actually create any graph. It just collects necessary information.
 * Graph will be created when you pass <code>RrdGraphDef</code> object to a {@link org.jrobin.graph.RrdGraph RrdGraph}, either
 * by passing it to the constructor or using the <code>setGraphDef()</code> method.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
public class RrdGraphDef implements Serializable
{
	// ================================================================
	// -- Members
	// ================================================================
	private long endTime				= Util.getTime();					// default time span of the last 24 hours
	private long startTime				= Util.getTime() - 86400L;
	
	private Title title					= null;								// no title
	private String valueAxisLabel		= null;								// no vertical label
	private TimeAxisLabel timeAxisLabel = null;								// no horizontal label
	
	private boolean gridX				= true;								// hide entire X axis grid (default: no)
	private boolean gridY				= true;								// hide entire Y axis grid (default: no)
	private boolean minorGridX			= true;								// hide minor X axis grid (default: no)
	private boolean minorGridY			= true;								// hide minor Y axis grid (default: no)
	private boolean majorGridX			= true;								// hide major X axis grid with labels (default: no)
	private boolean majorGridY			= true;								// hide major Y axis grid with labels (default: no)
	private boolean frontGrid			= true;								// show grid in front of the chart (default: yes)
	private boolean antiAliasing		= true;								// use anti-aliasing for the chart (default: yes)
	private boolean showLegend			= true;								// show legend and comments (default: yes)
	private boolean drawSignature		= true;								// show JRobin url signature (default: yes)
		
	private Color backColor				= new Color( 245, 245, 245 );		// variation of light gray
	private Color canvasColor			= Color.WHITE;						// white
	private Color borderColor			= Color.LIGHT_GRAY;					// light gray, only applicable with a borderStroke
	private Color normalFontColor		= Color.BLACK;						// black
	private Color titleFontColor		= Color.BLACK;						// black
	private Color majorGridColor		= new Color(130,30,30);				// variation of dark red
	private Color minorGridColor		= new Color(140,140,140);			// variation of gray
	private Color axisColor				= new Color(130,30,30);				// variation of dark red
	private Color arrowColor			= Color.RED;						// red
	private Color frameColor			= Color.LIGHT_GRAY;					// light gray
	
	private Font titleFont 				= null;								// use default 'grapher' font
	private Font normalFont 			= null;								// use default 'grapher' font
	
	private File background				= null;								// no background image by default
	private File overlay				= null;								// no overlay image by default
	
	private int chart_lpadding			= Grapher.CHART_LPADDING;			// padding space on the left of the chart area
	
	private int firstDayOfWeek			= TimeAxisUnit.MONDAY;				// first day of a calendar week, default: monday
	
	private double baseValue			= ValueFormatter.DEFAULT_BASE;		// unit base value to use (default: 1000)
	private int scaleIndex				= ValueFormatter.NO_SCALE;			// fixed units exponent value to use
	
	private BasicStroke borderStroke	= null;								// defaults to standard beveled border
	private TimeAxisUnit tAxis			= null;								// custom time axis grid, defaults to no custom
	private ValueAxisUnit vAxis			= null;								// custom value axis grid, defaults to no custom
	private GridRange gridRange			= null;								// custom value range definition, defaults to auto-scale
	
	// -- Non-settable members
	private int numDefs					= 0;								// number of Def datasources added
	private int commentLines			= 0;								// number of complete lines in the list of comment items
	private int commentLineShift		= 0;								// modifier to add to get minimum one complete line of comments
	
	private HashMap fetchSources		= new HashMap();					// holds the list of FetchSources
	private Vector cdefList				= new Vector();						// holds the list of Cdef datasources
	private Vector pdefList				= new Vector();						// holds the list of Plottable datasources
	private Vector plotDefs				= new Vector();						// holds the list of PlotDefs
	private Vector comments				= new Vector();						// holds the list of comment items
	
		
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new default JRobin graph object. 
	 */
	public RrdGraphDef() {
	}
	
	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.  
	 * Using timestamps defined as number of seconds since the epoch.
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in seconds.
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
	 * Sets time span to be presented on the graph using timestamps in number of seconds.
	 * An end time of 0 means JRobin will try to use the last available update time.
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in secons.
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod( long startTime, long endTime ) throws RrdException 
	{
		if ( startTime < 0 || ( endTime != 0 && endTime <= startTime ) )
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
	 * @param label Vertical axis label.
	 */
	public void setVerticalLabel( String label) 
	{
		this.valueAxisLabel = label;
	}
	
	/**
	 * Sets horizontal (time) axis label.
	 * <p>
	 * A horizontal axis label is always center aligned by default, with an extra linefeed to add
	 * some space before the regular comment lines start.  If you wish to remove the extra line of whitespace
	 * you should specify the alignment in the label using @c, @l or @r.  Using the @C, @L or @R markers will
	 * align the text appropriately, and leave the extra line of whitespace intact.
	 * </p>
	 * <p>
	 * It is possible to use multiple lines and multiple alignment markers for the axis label, in that case
	 * you should specify alignment for every part of the label to get it to display correctly.  When using multiple
	 * lines, no markers will be added to the end of the last line by default.
	 * </p>
	 * @param label Horizontal axis label.
	 */
	public void setTimeAxisLabel( String label ) throws RrdException
	{
		if ( label != null )
		{	
			timeAxisLabel		= new TimeAxisLabel( label );
			commentLines 		+= timeAxisLabel.getLineCount();
			commentLineShift	= (timeAxisLabel.isCompleteLine() ? 0 : 1); 
			
			comments.add( 0, timeAxisLabel );
		}
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
	 * Sets chart area background color. If not set, canvas color defaults to white.
	 * @param canvasColor Chart area background color.
	 */
	public void setCanvasColor( Color canvasColor ) 
	{
		this.canvasColor = canvasColor;		
	}
	
	/**
	 * Specifies the settings of the image border.
	 * Default is sort of beveled border around the image.
	 * To disable the image border, just specify a pixel width of 0.
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
	 * Sets the color of the title font used in the graph as a <code>java.awt.Color</code> object.
	 * Default title font color is black.
	 * @param c The color to be used.
	 */
	public void setTitleFontColor( Color c ) 
	{
		this.titleFontColor = c;
	}
	
	/**
	 * Sets the color of the default font used in the graph as a <code>java.awt.Color</code> object.
	 * Default font color is black.
	 * @param c The color to be used.
	 */
	public void setDefaultFontColor( Color c ) 
	{
		this.normalFontColor = c;
	}
	
	/**
	 * Sets the font to be used for the graph title as a <code>java.awt.Font</code> object.
	 * Default title font is "Lucida Sans Typewriter", with BOLD attributes and a size of 12 points.
	 * @param f The Font to be used.
	 */
	public void setTitleFont( Font f )
	{
		this.titleFont = f;
	}
	
	/**
	 * Sets the default font to be used in the graph as a <code>java.awt.Font</code> object.
	 * Default font is "Lucida Sans Typewriter", with PLAIN attributes and a size of 10 points.
	 * @param f The Font to be used.
	 */
	public void setDefaultFont( Font f )
	{
		this.normalFont = f;
	}
	
	/**
	 * Sets the color of the chart's major grid.
	 * Grid labels have the same color as the default font.
	 * @param c Color to use.
	 */
	public void setMajorGridColor( Color c ) 
	{
		this.majorGridColor = c;	
	}

	/**
	 * Determines the color of chart's the minor grid.
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
	 * Determines the color of chart X axis.
	 * @param c Color to use.
	 */
	public void setAxisColor( Color c ) 
	{
		this.axisColor = c;
	}

	/**
	 * Determines the color of the small axis arrow on the chart X axis.
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
	 * Determines if the major grid with labels for the Y axis needs to be drawn.
	 * @param visible True if major grid needs to be drawn, false if not.
	 */
	public void setMajorGridY( boolean visible ) 
	{
		this.majorGridY = visible;
	}

	/**
	 * Determines if the X axis grid should be drawn.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridX( boolean visible ) 
	{
		this.gridX		= visible;
	}

	/**
	 * Determines if the Y axis grid should be drawn.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridY( boolean visible ) 
	{
		this.gridY		= visible;
	}

	/**
	 * Determine if the graph grid is in front of the chart itself, or behind it.
	 * Default is in front of the chart.
	 * @param frontGrid True if the grid is in front of the chart.
	 */
	public void setFrontGrid( boolean frontGrid ) 
	{
		this.frontGrid = frontGrid;
	}

	/**
	 * Determine if the legend should be visible or not, default: visible.
	 * Invisible legend area means no comments will be plotted, and the graph will be smaller
	 * in height.
	 * @param showLegend True if the legend is visible.
	 */
	public void setShowLegend( boolean showLegend ) 
	{
		this.showLegend	= showLegend;
	}
	
	/**
	 * Determine if the default JRobin signature should be visible, default: yes.
	 * The signature text is "www.jrobin.org" and the signature is centered at the bottom of the graph.
	 * Unless you have a good reason not to draw the signature, please be so kind as to leave the 
	 * signature visible.  Disabling the signature can give a minor performance boost.
	 * @param showSignature True if the signature is visible.
	 */
	public void setShowSignature( boolean showSignature )
	{
		this.drawSignature = showSignature;
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
	 * Set the number of pixels on the left of the chart area ( value marker space ).
	 * @param lp Number of pixels used, defaults to 50.
	 */
	public void setChartLeftPadding( int lp ) 
	{
		this.chart_lpadding = lp;
	}
	
	/**
	 * Sets a background image to use for the graph.
	 * The image can be any of the supported imageio formats,
	 * default <i>.gif, .jpg or .png</i>.
	 * 
	 * Please note: if the provided file does not exit at graph creation time, the
	 * corresponding graph will be created without the background image, and without
	 * any exception being thrown.
	 * 
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
	 *
	 * Please note: if the provided file does not exit at graph creation time, the
	 * corresponding graph will be created without the overlay image, and without
	 * any exception being thrown.
	 * 
	 * @param fileName Filename of the image to use
	 */
	public void setOverlay( String fileName ) 
	{
		File ovFile	= new File( fileName );
		if ( ovFile.exists() )
			this.overlay = ovFile;
	}

	/** 
	 * Sets the base for value scaling. 
	 * If you are graphing memory this should be set to 1024 so that one Kb is 1024 bytes.
	 * As a default the base value is set to 1000, under the assumption you will be measuring
	 * network traffic, in wich case 1 kb/s equals 1000 b/s. 
	 * @param base Value to set as base for scaling.
	 */
	public void setBaseValue( double base ) 
	{
		this.baseValue = base;
	}

	/**
	 * This sets the 10** exponent scaling of the Y-axis values. 
	 * Normally values will be scaled to the appropriate units (k, M, etc.). 
	 * However you may wish to display units always in k (Kilo, 10e3) even if the data is in the 
	 * M (Mega, 10e6) range for instance. Value should be an integer which is a multiple of 3 
	 * between -18 and 18 inclusive. It is the exponent on the units you which to use. 
	 * For example, use 3 to display the y-axis values in k (Kilo, 10e3, thousands), 
	 * use -6 to display the y-axis values in u (Micro, 10e-6, millionths). Use a value of 0 to 
	 * prevent any scaling of the y-axis values.
	 * @param e Exponent value to use
	 */
	public void setUnitsExponent( int e ) 
	{
		this.scaleIndex = (6 - e / 3);	// Index in the scale table
	}

	int getUnitsExponent() {
		return (6 - scaleIndex) * 3;
	}

	/**
	 * Sets value range that will be presented in the graph. If not set, graph limits will be autoscaled.
	 * @param lower Lower limit.
	 * @param upper Upper limit.
	 * @param rigid Rigid grid, won't autoscale limits.
	 */

	public void setGridRange(double lower, double upper, boolean rigid) 
	{
		gridRange = new GridRange( lower, upper, rigid );
	}

	/**
	 * This sets the grid and labels on the Y axis.
	 * Minor grid lines appear at <code>gridStep</code>, major grid lines accompanied by a label
	 * will appear every <code>labelStep</code> value.   
	 * @param gridStep Value step on which a minor grid line will appear.
	 * @param labelStep Value step on which a major grid line with value label will appear.
	 */
	public void setValueAxis( double gridStep, double labelStep ) 
	{
		vAxis = new ValueAxisUnit( gridStep, labelStep );
	}

	/**
	 * This sets the grid and labels on the X axis.
	 * There are both minor and major grid lines, the major lines are accompanied by a time label.
	 * 
	 * To define a grid line you must define a specific time unit, and a number of time steps.
	 * A grid line will appear everey steps*unit.  Possible units are defined in the 
	 * {@link org.jrobin.graph.TimeAxisUnit TimeAxisUnit} class, and are <i>SECOND, MINUTE, HOUR, DAY,
	 * WEEK, MONTH</i> and <i>YEAR</i>.
	 * 
	 * @param minGridTimeUnit Time unit for the minor grid lines.
	 * @param minGridUnitSteps Time unit steps for the minor grid lines.
	 * @param majGridTimeUnit Time unit for the major grid lines.
	 * @param majGridUnitSteps Time unit steps for the major grid lines.
	 * @param dateFormat Format string of the time labels, according to <code>java.text.SimpleDateFormat</code> specifications.
	 * @param centerLabels True if the time label should be centered in the area between two major grid lines.
	 */
	public void setTimeAxis( int minGridTimeUnit, 
								int minGridUnitSteps, 
								int majGridTimeUnit, 
								int majGridUnitSteps, 
								String dateFormat,
								boolean centerLabels ) 
	{
		this.tAxis 			= new TimeAxisUnit( minGridTimeUnit, 
												minGridUnitSteps, 
												majGridTimeUnit, 
												majGridUnitSteps, 
												new SimpleDateFormat( dateFormat ),
												centerLabels ,
												firstDayOfWeek
											);
	}
	
	/**
	 * Sets the first day of a calendar week, defaults to monday if not set.
	 * 
	 * @param day Weekday, 0 for sunday, 6 for saturday.
	 */
	public void setFirstDayOfWeek( int day )
	{
		firstDayOfWeek = day;
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
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
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
	 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrdgraph.html" target="man">rrdgraph man page</a>.</p>
	 * @param name Graph source name.
	 * @param rpn RPN expression containig comma delmited simple and complex graph
	 * source names, RPN constants, functions and operators.
	 */
	public void datasource( String name, String rpn ) 
	{
		cdefList.add( new Cdef(name, rpn) );
	}
	
	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 * 
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 */
	public void datasource( String name, Plottable plottable )
	{
		pdefList.add( new Pdef(name, plottable) );
	}
	
	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 * 
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param index Integer referring to the datasource in the Plottable class.
	 */
	public void datasource( String name, Plottable plottable, int index )
	{
		pdefList.add( new Pdef(name, plottable, index) );
	}
	
	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 * 
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param sourceName String name referring to the datasource in the Plottable class.
	 */
	public void datasource( String name, Plottable plottable, String sourceName )
	{
		pdefList.add( new Pdef(name, plottable, sourceName) );
	}
	
	/**
	 * Adds line plot to the graph definition, using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's LINE1 directive (line width
	 * is set to 1).  The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 *
	 * @param sourceName Graph source name.
	 * @param color Line collor to be used.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void line( String sourceName, Color color, String legend ) throws RrdException 
	{
		plotDefs.add( new Line(sourceName, color) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds line plot to the graph definition, using the specified color, legend and line width.
	 * This method takes exactly the same parameters as RRDTool's LINE directive. The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 *
	 * @param sourceName Graph source name.
	 * @param color Line color to be used.
	 * @param legend Legend to be printed on the graph.
	 * @param lineWidth Width of the line in pixels.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void line( String sourceName, Color color, String legend, int lineWidth ) throws RrdException 
	{
		plotDefs.add( new Line(sourceName, color, lineWidth) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds line plot to the graph definition, based on two points.
	 * Start and end point of the line are specified. The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param t1 Timestamp (X axis) of the start point of the line.
	 * @param v1 Value (Y axis) of the start point of the line.
	 * @param t2 Timestamp (X axis) of the end point of the line.
	 * @param v2 Value (Y axis) of the end point of the line.
	 * @param color Line color to be used.
	 * @param legend Legend to be printed on the graph.
	 * @param lineWidth Width of the line in pixels.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void line( GregorianCalendar t1, double v1, GregorianCalendar t2, double v2, Color color, String legend, int lineWidth ) throws RrdException
	{
		plotDefs.add( new CustomLine( t1.getTimeInMillis() / 1000, v1, t2.getTimeInMillis() / 1000, v2, color, lineWidth ) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds area plot to the graph definition,
	 * using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's AREA directive. The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
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
	 * Adds area plot to the graph definition, based on two points.
	 * Points specified are the bottom-left corner and the upper-right corner.
	 * When stacked onto such an area, a stack is always placed on top of the "upper border" of the
	 * rectangle (value of the second point). The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param t1 Timestamp (X axis) of the bottom-left corner of the area.
	 * @param v1 Value (Y axis) of the bottom-left corner of the area.
	 * @param t2 Timestamp (X axis) of the upper-right corner of the area.
	 * @param v2 Value (Y axis) of the upper-right corner of the area.
	 * @param color Filling collor to be used for area plot.
	 * @param legend Legend to be printed on the graph.
	 * @throws RrdException Thrown if invalid graph source name is supplied.
	 */
	public void area( GregorianCalendar t1, double v1, GregorianCalendar t2, double v2, Color color, String legend ) throws RrdException
	{
		plotDefs.add( new CustomArea( t1.getTimeInMillis() / 1000, v1, t2.getTimeInMillis() / 1000, v2, color ) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds stacked plot to the graph definition,
	 * using the specified color and legend. This method
	 * takes exactly the same parameters as RRDTool's STACK directive. The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>. 
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
	 * Adds horizontal rule to the graph definition.  The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param value Rule posiotion.
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void hrule(double value, Color color, String legend) throws RrdException {
		plotDefs.add( new CustomLine( Long.MIN_VALUE, value, Long.MAX_VALUE, value, color ) );
		addLegend( legend, color );
	}

	/**
	 * Adds horizontal rule to the graph definition.  The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param value Rule posiotion.
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @param lineWidth Width of the hrule line in pixels.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void hrule(double value, Color color, String legend, int lineWidth) throws RrdException {
		plotDefs.add( new CustomLine( Long.MIN_VALUE, value, Long.MAX_VALUE, value, color, lineWidth ) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds a vertical rule to the graph definition.  The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param timestamp Rule position (specific moment in time)
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 */
	public void vrule( GregorianCalendar timestamp, Color color, String legend ) throws RrdException {
		long timeSecs = timestamp.getTimeInMillis() / 1000;
		plotDefs.add( new CustomLine( timeSecs, Double.MIN_VALUE, timeSecs, Double.MAX_VALUE, color ) );
		addLegend( legend, color );
	}

	/**
	 * Adds a vertical rule to the graph definition.  The legend allows for the same
	 * alignment options as <code>gprint</code> or <code>comment</code>.
	 * @param timestamp Rule position (specific moment in time)
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @param lineWidth Width of the vrule in pixels.
	 */
	public void vrule( GregorianCalendar timestamp, Color color, String legend, int lineWidth ) throws RrdException {
		long timeSecs = timestamp.getTimeInMillis() / 1000;
		plotDefs.add( new CustomLine( timeSecs, Double.MIN_VALUE, timeSecs, Double.MAX_VALUE, color, lineWidth ) );
		addLegend( legend, color );
	}
	
	/**
	 * Adds comment to the graph definition. A comment on the graph will be left, center or right aligned
	 * if the format string ends with <code>@l</code>, <code>@c</code> or <code>@r</code>,
	 * respectively. It is also possible to align text without adding a linefeed by using
	 * <code>@L</code>, <code>@R</code> and <code>@C</code> as markers.  After a GPRINT some
	 * whitespace is appended by default.  To suppress this whitespace put a <code>@G</code>
	 * marker at the very end of the string.  By putting a <code>@g</code> marker instead all
	 * whitespace inside the string at very beginning or end will be removed also.
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
	 * respectively. It is also possible to align text without adding a linefeed by using
	 * <code>@L</code>, <code>@R</code> and <code>@C</code> as markers.  After a GPRINT some
	 * whitespace is appended by default.  To suppress this whitespace put a <code>@G</code>
	 * marker at the very end of the string.  By putting a <code>@g</code> marker instead all
	 * whitespace inside the string at very beginning or end will be removed also.</p>
	 *
	 * @param sourceName Graph source name
	 * @param consolFun Consolidation function to be used for calculation ("AVERAGE",
	 * "MIN", "MAX" or "LAST")
	 * @param format Format string. For example: "speed is @5.2 @sbits/sec@c",
	 * "temperature = @0 degrees"
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public void gprint(String sourceName, String consolFun, String format) throws RrdException 
	{
		addComment( new Gprint(sourceName, consolFun, format) );
	}

	/**
	 * Exports RrdGraphDef (graph definition) object in XML format to output stream.
	 * Generated code can be parsed with {@link RrdGraphDefTemplate} class.
	 * @param stream Output stream to send XML code to
	 */
	public void exportXmlTemplate(OutputStream stream) {
		XmlWriter xml = new XmlWriter(stream);
		xml.startTag("rrd_graph_def");
        // SPAN
		xml.startTag("span");
		xml.writeTag("start", startTime);
		xml.writeTag("end", endTime);
		xml.closeTag(); // span
		// OPTIONS
		xml.startTag("options");
		xml.writeTag("anti_aliasing", antiAliasing);
		xml.writeTag("arrow_color", arrowColor);
		xml.writeTag("axis_color", axisColor);
		xml.writeTag("back_color", backColor);
		if(background != null) {
			xml.writeTag("background", background);
		}
		xml.writeTag("base_value", baseValue);
        xml.writeTag("canvas", canvasColor);
		xml.writeTag("left_padding", chart_lpadding);
		if(normalFont != null) {
			xml.writeTag("default_font", normalFont);
		}
		xml.writeTag("default_font_color", normalFontColor);
		xml.writeTag("frame_color", frameColor);
		xml.writeTag("front_grid", frontGrid);
		if(gridRange != null) {
			gridRange.exportXmlTemplate(xml);
		}
		xml.writeTag("grid_x", gridX);
		xml.writeTag("grid_y", gridY);
		if(borderStroke != null) {
			xml.startTag("border");
			xml.writeTag("color", borderColor);
			xml.writeTag("width", (int)borderStroke.getLineWidth());
			xml.closeTag(); // border
		}
		xml.writeTag("major_grid_color", majorGridColor);
		xml.writeTag("major_grid_x", majorGridX);
		xml.writeTag("major_grid_y", majorGridY);
		xml.writeTag("minor_grid_color", minorGridColor);
		xml.writeTag("minor_grid_x", minorGridX);
		xml.writeTag("minor_grid_y", minorGridY);
		if(overlay != null) {
			xml.writeTag("overlay", overlay);
		}
        xml.writeTag("show_legend", showLegend);
		xml.writeTag("show_signature", drawSignature);
		if(tAxis != null) {
			tAxis.exportXmlTemplate(xml);
		}
		if(timeAxisLabel != null) {
			timeAxisLabel.exportXmlTemplate(xml);
		}
		if(title != null) {
			title.exportXmlTemplate(xml);
		}
		if(titleFont != null) {
			xml.writeTag("title_font", titleFont);
		}
        xml.writeTag("title_font_color", titleFontColor);
		if(scaleIndex != ValueFormatter.NO_SCALE) {
			xml.writeTag("units_exponent", getUnitsExponent());
		}
		if(vAxis != null) {
			vAxis.exportXmlTemplate(xml);
		}
		if(valueAxisLabel != null) {
            xml.writeTag("vertical_label", valueAxisLabel);
		}
		xml.closeTag(); // options
		// DATASOURCES
		xml.startTag("datasources");
		// defs
		Iterator fsIterator = fetchSources.values().iterator();
		while (fsIterator.hasNext()) {
			FetchSource fs = (FetchSource) fsIterator.next();
			fs.exportXml(xml);
		}
		// cdefs
		for (int i = 0; i < cdefList.size(); i++ ) {
			Cdef cdef = (Cdef) cdefList.elementAt(i);
			cdef.exportXml(xml);
		}
		xml.closeTag(); // datasources
		xml.startTag("graph");
		for ( int i = 0; i < comments.size(); i++ )
		{
			Comment cmt = (Comment) comments.elementAt(i);
			if ( cmt.commentType == Comment.CMT_LEGEND || cmt.commentType == Comment.CMT_NOLEGEND)
			{
				PlotDef pDef = (PlotDef) plotDefs.elementAt( ((Legend) cmt).getPlofDefIndex() );
				pDef.exportXmlTemplate(xml, cmt.text);
			}
			else if(cmt instanceof TimeAxisLabel) {
				// NOP: already exported in the options section
			}
			else {
				cmt.exportXmlTemplate(xml);
			}
		}
		xml.closeTag(); // graph
		xml.closeTag(); // rrd_graph_def
		xml.flush();
	}

	/**
	 * Exports RrdGraphDef (graph definition) object in XML format to string.
	 * Generated code can be parsed with {@link RrdGraphDefTemplate} class.
	 * @return String representing graph definition in XML format
	 */
	public String exportXmlTemplate() {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		exportXmlTemplate(outputStream);
		return outputStream.toString();
	}

	/**
	 * Exports RrdGraphDef (graph definition) object in XML format to file.
	 * Generated code can be parsed with {@link RrdGraphDefTemplate} class.
	 * @param filePath destination file
	 */
	public void exportXmlTemplate(String filePath) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(filePath, false);
		exportXmlTemplate(outputStream);
		outputStream.close();
	}

	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	protected long getStartTime() {
		return startTime;
	}
	
	protected long getEndTime() {
		return endTime;
	}
	
	protected Title getTitle() {
		return title;
	}
	
	protected String getVerticalLabel() {
		return valueAxisLabel;
	}
	
	protected Color getBackColor() {
		return backColor;
	}
	
	protected Color getCanvasColor() {
		return canvasColor;
	}
	
	protected Color getImageBorderColor() {
		return borderColor;
	}
	
	protected BasicStroke getImageBorderStroke() {
		return borderStroke;
	}
	
	protected Color getTitleFontColor() {
		return titleFontColor;
	}

	protected Color getDefaultFontColor() {
		return normalFontColor;
	}
	
	protected Font getTitleFont() {
		return titleFont;
	}
	
	protected Font getDefaultFont() {
		return normalFont;
	}
	
	protected Color getMajorGridColor() {
		return majorGridColor;
	}
	
	protected Color getMinorGridColor() {
		return minorGridColor;
	}
	
	protected Color getFrameColor() {
		return frameColor;
	}
	
	protected Color getAxisColor() {
		return axisColor;
	}

	protected Color getArrowColor() {
		return arrowColor;
	}
	
	protected Color getBorderColor() {
		return borderColor;
	}
	
	protected BasicStroke getBorderStroke() {
		return borderStroke;	
	}
	
	protected boolean showMinorGridX() {
		return minorGridX;
	}
	
	protected boolean showMinorGridY() {
		return minorGridY;
	}
	
	protected boolean showMajorGridX() {
		return majorGridX;
	} 
	
	protected boolean showMajorGridY() {
		return majorGridY;
	}

	protected boolean showGridX() {
		return gridX;
	}
	
	protected boolean showGridY() {
		return gridY;
	}
	
	protected boolean drawFrontGrid() {
		return frontGrid;
	}
	
	protected boolean showLegend() {
		return showLegend;
	}
	
	protected boolean showSignature() {
		return drawSignature;
	}
	
	protected boolean isFrontGrid() {
		return frontGrid;
	}
	
	protected boolean useAntiAliasing() {
		return antiAliasing;
	}
	
	protected int getChartLeftPadding() {
		return chart_lpadding;
	}
	
	protected File getBackground() {
		return background;
	}
	
	protected File getOverlay() {
		return overlay;
	}
	
	protected double getBaseValue() {
		return baseValue;
	}

	protected int getScaleIndex() {
		return scaleIndex;
	}

	protected GridRange getGridRange() {
		return gridRange;
	}
	
	protected ValueAxisUnit getValueAxis() {
		return vAxis;
	}
	
	protected TimeAxisUnit getTimeAxis() {
		return tAxis;
	}
	
	protected int getFirstDayOfWeek() {
		return firstDayOfWeek;
	}
	
	protected PlotDef[] getPlotDefs()
	{
		return (PlotDef[]) plotDefs.toArray( new PlotDef[] {} );
	}
	
	protected Comment[] getComments()
	{
		return (Comment[]) comments.toArray( new Comment[] {} );
	}
	
	protected int getCommentLineCount()
	{
		return ( comments.size() > 0 ? commentLines + commentLineShift : 0 ); 
	}
	
	protected int getNumDefs()
	{
		return numDefs;
	}
	
	protected Cdef[] getCdefs()
	{
		return (Cdef[]) cdefList.toArray( new Cdef[] {} );		
	}
	
	protected Pdef[] getPdefs()
	{
		return (Pdef[]) pdefList.toArray( new Pdef[] {} );
	}
	
	protected HashMap getFetchSources()
	{
		return fetchSources;
	}
	
	
	// ================================================================
	// -- Private methods
	// ================================================================
	private void addComment( Comment cmt )
	{
		commentLines 		+= cmt.getLineCount();
		commentLineShift	= (cmt.isCompleteLine() ? 0 : 1); 
		comments.add( cmt );
	}
	
	private void addLegend( String legend, Color color ) throws RrdException
	{
		// Always add the item, even if it's empty, always add the index 
		// of the graph this legend is for
		addComment( new Legend(legend, color, plotDefs.size() - 1 ) );
	}
}
