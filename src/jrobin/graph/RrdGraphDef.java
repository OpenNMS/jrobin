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

import jrobin.core.Util;
import jrobin.core.RrdException;

import java.io.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;

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
 * <p>If you are familiar with RRDTool's graphing options, you will be able to create
 * JRobin graphs in no time. It is quite simple to translate complex RRDTool graph
 * definitions into JRobin java code. For example: </p>
 *
 * <pre>
 * rrdtool graph traffic.png
 * --start "05/21/2003 00:00" --end "05/22/2003 00:00"
 * -w 550 -h 325 -v "link speed [bytes/sec]"
 * -a PNG -t "Leased Line Traffic"
 * DEF:in=traffic.rrd:input:AVERAGE
 * DEF:out=traffic.rrd:output:AVERAGE
 * CDEF:in8=in,8,*
 * CDEF:out8=out,8,*
 * CDEF:total=in8,out8,+
 * CDEF:totalNeg=total,-1,*
 * AREA:out8#00FF00:"output traffic"
 * STACK:in8#0000FF:"input traffic (stacked)"
 * AREA:totalNeg#FF0000:"total traffic\r"
 * GPRINT:in8:AVERAGE:"avgIn=%.2lf %sbits/sec"
 * GPRINT:in8:MAX:"maxIn=%.2lf %sbits/sec\r"
 * GPRINT:out8:AVERAGE:"avgOut=%.2lf %sbits/sec"
 * GPRINT:out8:MAX:"maxOut=%.2lf %sbits/sec\r"
 * GPRINT:total:AVERAGE:"avgTotal=%.2lf %sbits/sec"
 * GPRINT:total:MAX:"maxTotal=%.2lf %sbits/sec\r" </pre>
 *
 * <p>...would look like this in your java code:</p>
 *
 * <pre>
 * GregorianCalendar start = new GregorianCalendar(2003, 4, 21);
 * GregorianCalendar end = new GregorianCalendar(2003, 4, 22);
 * RrdGraphDef gDef = new RrdGraphDef();
 * gDef.setTimePeriod(start, end);
 * gDef.setTitle("Leased Line Traffic");
 * gDef.setTimeAxisLabel("time");
 * gDef.setValueAxisLabel("link speed [bytes/sec]");
 * gDef.datasource("in", "traffic.rrd", "input", "AVERAGE");
 * gDef.datasource("out", "traffic.rrd", "output", "AVERAGE");
 * gDef.datasource("in8", "in,8,*");
 * gDef.datasource("out8", "out,8,*");
 * gDef.datasource("total", "in8,out8,+");
 * gDef.datasource("totalneg", "total,-1,*");
 * gDef.area("out8", Color.GREEN, "output traffic");
 * gDef.stack("in8", Color.BLUE, "input traffic (stacked)");
 * gDef.area("totalneg", Color.RED, "total traffic");
 * gDef.gprint("in8", "AVERAGE", "avgin=@2 @sbits/sec");
 * gDef.gprint("in8", "MAX", "maxin=@2 @sbits/sec@r");
 * gDef.gprint("out8", "AVERAGE", "avgout=@2 @sbits/sec");
 * gDef.gprint("out8", "MAX", "maxout=@2 @sbits/sec@r");
 * gDef.gprint("total", "AVERAGE", "avgtotal=@2 @sbits/sec");
 * gDef.gprint("total", "MAX", "maxtotal=@2 @sbits/sec@r");
 * RrdGraph graph = new RrdGraph(gDef);
 * graph.saveAsPNG(new File("traffic.png"), 645, 440); </pre>
 *
 * <p>Compare the graphs:</p>
 * <p align="center"><img src="../../../images/rrd.png" border="1"><br><b>RRDTool's graph</b></p>
 * <p align="center"><img src="../../../images/jrobin.png" border="1"><br><b>JRobin's graph</b></p>
 *
 * <p><code>RrdGraphDef</code> class does not actually create any graph. It just collects necessary information.
 * Graph will be created when you pass <code>RrdGraphDef</code> object to the constructor
 * of {@link jrobin.graph.RrdGraph RrdGraph} object.</p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class RrdGraphDef 
{
	private ArrayList sources = new ArrayList();
	private ArrayList plotDefs = new ArrayList();
	private ArrayList graphs = new ArrayList();
	private ArrayList comments = new ArrayList();

	// graph parameters
	private int timeUnit, timeUnitCount;
	private SimpleDateFormat timeFormat;
	private long endTime 			= Util.getTime();
	private long startTime 			= endTime - 86400L;
	private String title 			= null;				// Default to null, to check
	private String timeAxisLabel 	= "";
	private String valueAxisLabel	= null;
	
	// Visibility of grid settings
	private boolean gridX			= true;
	private boolean gridY			= true;
	private boolean minorGridX		= true;
	private boolean minorGridY		= true;
	private boolean majorGridX		= true;
	private boolean majorGridY		= true;
	private boolean rigidGrid		= false;
	private boolean frontGrid		= true;
	private boolean antiAliasing	= true;
	private boolean showLegend		= true;
	
	private File background			= null;
	private File overlay			= null;
	
	private BasicStroke borderStroke;
		
	private Range valueRange;
	private boolean logarithmic = false;
	private double valueStep = 0;

	private Color borderColor;		// null value is special here
	private Color backColor			= new Color( 245, 245, 245 );
	private Color canvasColor		= Color.WHITE;
	private Color fontColor			= Color.BLACK;
	private Color majorGridColor	= new Color(130,30,30);
	private Color minorGridColor	= new Color(140,140,140);
	private Color axisColor			= new Color(130,30,30);
	private Color arrowColor		= Color.RED;
	private Color frameColor		= Color.LIGHT_GRAY;
	
	private TimeAxisUnit tAxis		= null;
	private boolean tAxisCentered	= false;
	private double valueGridStep	= Double.NaN;
	private double valueLabelStep	= Double.NaN;
	
	private double baseValue		= 1000;
	private int scaleIndex			= -1;			// NO_SCALE
	
	private int chart_lpadding		= Grapher.CHART_LPADDING;

	public HashMap rrdFiles			= new HashMap();
	/**
	 * Creates new RRD graph definition.
	 */
	public RrdGraphDef() {
	}

	/**
	 * Sets time span to be presented on the graph using timestamps.
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in secons.
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod(long startTime, long endTime) throws RrdException {
		if(startTime < 0 || endTime <= startTime) {
			throw new RrdException("Invalid graph start/end time: " + startTime + "/" + endTime);
		}
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Sets time span to be presented on the graph using <code>java.util.Date</code> objects.
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public void setTimePeriod(Date start, Date end) throws RrdException {
		setTimePeriod(start.getTime() / 1000L, end.getTime() / 1000L);
	}

	/**
	 * Sets time span to be presented on the graph using
	 * <code>java.util.GregorianCalendar</code> objects.
	 * @param start Starting time.
	 * @param end Ending time
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod(GregorianCalendar start, GregorianCalendar end) throws RrdException {
		setTimePeriod(start.getTime(), end.getTime());
	}

	/**
	 * Sets graph title.
	 * @param title Graph title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets horizontal (time) axis label.
	 * @param timeAxisLabel Axis label.
	 */
	public void setTimeAxisLabel(String timeAxisLabel) {
		this.timeAxisLabel = timeAxisLabel;
	}

	/**
	 * Sets vertical (value) axis label
	 * @param valueAxisLabel Axis label.
	 */
	public void setValueAxisLabel(String valueAxisLabel) {
		this.valueAxisLabel = valueAxisLabel;
	}

	void addSource(Source def) {
		sources.add(def);
	}

	void addPlot(PlotDef plotDef) throws RrdException {
		plotDefs.add(plotDef);
		graphs.add(new OverlayGraph(plotDef));
		// Add comment line for the legend
		if ( plotDef.getLegend() != null )
			addComment( new Legend(plotDef.getColor(), plotDef.getLegend()) );
	}

	void addPlot(Stack plotDef) throws RrdException {
		plotDefs.add(plotDef);
		// Add comment line for the legend
		if ( plotDef.getLegend() != null )
			addComment( new Legend(plotDef.getColor(), plotDef.getLegend()) );
		OverlayGraph lastGraph = getLastGraph();
		if(lastGraph != null) {
			lastGraph.addPlotDef(plotDef);
			return;
		}
		throw new RrdException("You have to STACK graph onto something...");
	}
	
	void addPlot(Vrule vruleDef) throws RrdException {
		plotDefs.add(vruleDef);
		sources.add( vruleDef.getSource() );
		if ( vruleDef.getLegend() != null )
			addComment( new Legend(vruleDef.getColor(), vruleDef.getLegend()) );
	}

	void addPlot(Hrule hruleDef) throws RrdException {
		plotDefs.add(hruleDef);
		graphs.add(new OverlayGraph(hruleDef));
		sources.add(hruleDef.getSource());
		if ( hruleDef.getLegend() != null )
			addComment( new Legend(hruleDef.getColor(), hruleDef.getLegend()) );
	}

	private OverlayGraph getLastGraph() {
		int count = graphs.size();
		if(count == 0) {
			return null;
		}
		return (OverlayGraph) graphs.get(count - 1);
	}

	void addComment(Comment comment) {
        comments.add(comment);
	}

	Source[] getSources() {
		return (Source[]) sources.toArray(new Source[0]);
	}

	PlotDef[] getPlotDefs() {
		return (PlotDef[]) plotDefs.toArray(new PlotDef[0]);
	}

	OverlayGraph[] getGraphs() {
		return (OverlayGraph[]) graphs.toArray(new OverlayGraph[0]);
	}

	Comment[] getComments() {
		return (Comment[]) comments.toArray(new Comment[0]);
	}

	long getEndTime() {
		return endTime;
	}

	long getStartTime() {
		return startTime;
	}

	String getTitle() {
		return title;
	}

	String getTimeAxisLabel() {
		return timeAxisLabel;
	}

	String getValueAxisLabel() {
		return valueAxisLabel;
	}

	private Source findSourceByName(String sourceName) throws RrdException {
		for(int i = 0; i < sources.size(); i++)	{
			Source source = (Source) sources.get(i);
			if(source.getName().equals(sourceName)) {
				return source;
			}
		}
		throw new RrdException("Datasource not found: " + sourceName);
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
	public void area(String sourceName, Color color, String legend) throws RrdException {
		Source source = findSourceByName(sourceName);
		addPlot(new Area(source, color, legend));
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
	public void line(String sourceName, Color color, String legend) throws RrdException {
		Source source = findSourceByName(sourceName);
		addPlot(new Line(source, color, legend));
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
	public void line(String sourceName, Color color, String legend, float lineWidth)
		throws RrdException {
		Source source = findSourceByName(sourceName);
		addPlot(new Line(source, color, legend, lineWidth));
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
	public void stack(String sourceName, Color color, String legend) throws RrdException {
		Source source = findSourceByName(sourceName);
		addPlot( new Stack(source, color, legend) );
	}

	/**
	 * Adds horizontal rule to the graph definition.
	 * @param value Rule posiotion.
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void hrule(double value, Color color, String legend) throws RrdException {
		addPlot( new Hrule(value, color, legend) );
	}
	
	/**
	 * Adds horizontal rule to the graph definition.
	 * @param value Rule posiotion.
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @param lineWidth Width of the hrule line in pixels.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void hrule(double value, Color color, String legend, float lineWidth) throws RrdException {
		addPlot( new Hrule(value, color, legend, lineWidth) );
	}
	
	/**
	 * Adds a vertical rule to the graph definition.
	 * @param timestamp Rule position (specific moment in time)
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 */
	public void vrule( GregorianCalendar timestamp, Color color, String legend ) throws RrdException {
		addPlot( new Vrule(timestamp.getTimeInMillis() / 1000, color, legend) );
	}
	
	/**
	 * Adds a vertical rule to the graph definition.
	 * @param timestamp Rule position (specific moment in time)
	 * @param color Rule color.
	 * @param legend Legend to be added to the graph.
	 * @param lineWidth Width of the vrule in pixels.
	 */
	public void vrule( GregorianCalendar timestamp, Color color, String legend, float lineWidth ) throws RrdException {
		addPlot( new Vrule(timestamp.getTimeInMillis() / 1000, color, legend, lineWidth) );
	}

	/**
	 * Adds comment to the graph definition. Comments will be left, center or right aligned
	 * if the comment ends with <code>@l</code>, <code>@c</code> or <code>@r</code>,
	 * respectively.
	 * @param text Comment
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	public void comment(String text) throws RrdException {
		addComment(new Comment(text));
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
	public void gprint(String sourceName, String consolFun, String format)
		throws RrdException {
		Source source = findSourceByName(sourceName);
		addComment(new Gprint(source, consolFun, format));
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
	public void datasource(String name, String file, String dsName, String consolFun) {
		// Experimental fetch code
		if ( rrdFiles.containsKey(file) ) {
			RrdFile rf = (RrdFile) rrdFiles.get(file);
			rf.addSource( consolFun, dsName, name );	
		}
		else
			rrdFiles.put( file, new RrdFile(consolFun, dsName, name) );
		
		addSource(new Def(name, file, dsName, consolFun));
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
	public void datasource(String name, String rpn) {
		addSource(new Cdef(name, rpn));
	}

	/**
	 * Sets horizontal space between time ticks. If not specified, JRobin will try to
	 * calculate it.
	 * @param unit Time unit for tick. Use supplied constants: YEAR, MONTH, DAY,
	 * HOUR, MINUTE, SECOND.
	 * @param unitCount Number of time units between time ticks.
	 * @param format Format to be used for tick label.
	 */
	/**
	 * @deprecated  As of JDK 1.1, replaced by {@link #setBounds(int,int,int,int)}
	 */
	public void setTimeUnit(int unit, int unitCount, SimpleDateFormat format) {
		this.timeUnit = unit;
		this.timeUnitCount = unitCount;
		this.timeFormat = format;
	}

	int getTimeUnit() {
		return timeUnit;
	}

	int getTimeUnitCount() {
		return timeUnitCount;
	}

	SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}

	/**
	 * Sets value range that will be presented in the graph. If not set, graph will be
	 * autoscaled.
	 * @param lower Lower limit.
	 * @param upper Upper limit.
	 */
	
	public void setValueRange(double lower, double upper) {
		valueRange = new Range( lower, upper);
	}

	Range getValueRange() {
		return valueRange;
	}
	

	boolean isLogarithmic() {
		return logarithmic;
	}

	/**
	 * Sets normal or logarithmic graph type. If not set, defaults to normal graph.
	 * @param logarithmic
	 */
	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
	}

	double getValueStep() {
		return valueStep;
	}

	/**
	 * Sets vertical space between value ticks. If not specified, JRobin will try to guess it.
	 * @param valueStep Value step between value ticks.
	 */
	public void setValueStep(double valueStep) {
		this.valueStep = valueStep;
	}

	Color getBackColor() {
		return backColor;
	}

	Color getCanvasColor() {
		return canvasColor;	
	}
	
	/**
	 * Sets image background color. If not set, back color defaults to a very light gray.
	 * @param backColor Graph background color.
	 */
	public void setBackColor( Color backColor ) {
		this.backColor = backColor;
	}
	
	/**
	 * Sets graph area background color. If not set, back color defaults to white.
	 * @param backColor Graph area background color.
	 */
	public void setCanvasColor( Color canvasColor ) {
		this.canvasColor = canvasColor;		
	}
	
	/**
	 * Determines if the minor grid for the X axis needs to be drawn.
	 * @param visible True if minor grid needs to be drawn, false if not.
	 */
	public void setMinorGridX( boolean visible ) {
		this.minorGridX = visible;
	}
	
	/**
	 * Determines if the minor grid for the X axis needs to be drawn.
	 * @param visible True if minor grid needs to be drawn, false if not.
	 */
	public void setMinorGridY( boolean visible ) {
		this.minorGridY = visible;
	}
	
	boolean getMinorGridX() {
		return minorGridX;
	}
	
	boolean getMinorGridY() {
		return minorGridY;
	}
	
	/**
	 * Determines if the major grid with labels for the X axis needs to be drawn.
	 * @param visible True if major grid needs to be drawn, false if not.
	 */
	public void setMajorGridX( boolean visible ) {
		this.majorGridX = visible;
	}

	/**
	 * Determines if the major grid for the X axis needs to be drawn.
	 * @param visible True if major grid needs to be drawn, false if not.
	 */
	public void setMajorGridY( boolean visible ) {
		this.majorGridY = visible;
	}

	boolean getMajorGridX() {
		return majorGridX;
	}

	boolean getMajorGridY() {
		return majorGridY;
	}

	/**
	 * Determines if the X axis grid should be drawn.
	 * This will not change the left padding of the drawing area.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridX( boolean visible ) {
		this.gridX		= visible;
	}

	/**
	 * Determines if the Y axis grid should be drawn.
	 * This will not change the bottom padding of the drawing area.
	 * @param visible True if grid needs to be drawn, false if not.
	 */
	public void setGridY( boolean visible ) {
		this.gridY		= visible;
	}

	boolean getGridX() {
		return gridX;
	}

	boolean getGridY() {
		return gridY;
	}
	
	/**
	 * Specifies the settings of the image border 
	 * @param c Bordercolor of the image
	 * @param w Pixel width of the image border
	 */
	public void setImageBorder( Color c, int w ) {
		this.borderStroke	= new BasicStroke( w );
		if ( c != null )
			this.borderColor	= c;
	}
	
	Color getImageBorderColor() {
		return borderColor;
	}
	
	BasicStroke getImageBorderStroke() {
		return borderStroke;
	}
	
	/**
	 * Determines if the grid should have rigid upper and lower limits.
	 * If so the upper and lower limit will not autoscale depending on the
	 * graph values.
	 * @param rigid True if the grid should have rigid limits
	 */
	public void setRigidGrid( boolean rigid ) {
		this.rigidGrid = rigid;
	}
	
	boolean getRigidGrid() {
		return this.rigidGrid;
	}
	
	/**
	 * Determine if the graph grid is in front of the graphs itself, or behind it.
	 * Default is in front of the graph itself.
	 * @param frontGrid True if the grid is in front of the graphs
	 */
	public void setFrontGrid( boolean frontGrid ) {
		this.frontGrid = frontGrid;
	}
	
	boolean getFrontGrid() {
		return this.frontGrid;
	}
	
	/**
	 * Determine if the legend should be visible or not, default: visible.
	 * @param showLegend True if the legend is visible
	 */
	public void setShowLegend( boolean showLegend ) {
		this.showLegend	= showLegend;
	}
	
	boolean getShowLegend() {
		return this.showLegend;
	}
	
	/**
	 * Sets the color of the font on the graph.
	 * @param c The color to be used.
	 */
	public void setFontColor( Color c ) {
		this.fontColor = c;
	}
	
	Color getFontColor() {
		return this.fontColor;
	}
	
	/**
	 * Determines the color of the major grid.
	 * @param c Color to use
	 */
	public void setMajorGridColor( Color c ) {
		this.majorGridColor = c;	
	}
	
	Color getMajorGridColor() {
		return majorGridColor;
	}
	
	/**
	 * Determines the color of the minor grid.
	 * @param c Color to use
	 */
	public void setMinorGridColor( Color c ) {
		this.minorGridColor = c;
	}
	
	Color getMinorGridColor() {
		return minorGridColor;
	}
	
	/**
	 * Determines the color of canvas frame.
	 * @param c Color to use
	 */
	public void setFrameColor( Color c ) {
		this.frameColor = c;
	}

	Color getFrameColor() {
		return frameColor;
	}
	
	/**
	 * Determines the color of X axis.
	 * @param c Color to use
	 */
	public void setAxisColor( Color c ) {
		this.axisColor = c;
	}

	Color getAxisColor() {
		return axisColor;
	}
	
	/**
	 * Determines the color of the small axis arrow.
	 * @param c Color to use
	 */
	public void setArrowColor( Color c ) {
		this.arrowColor = c;
	}

	Color getArrowColor() {
		return arrowColor;
	}
	
	/**
	 * Set the number of pixels on the left of the canvas area ( value marker space )
	 * @param lp Number of pixels used, defaults to 50
	 */
	public void setChartLeftPadding( int lp ) {
		this.chart_lpadding = lp;
	}
	
	int getChartLeftPadding() {
		return this.chart_lpadding;
	}
	
	/**
	 * Set the anti-aliasing option for the drawing area of the graph.
	 * Default uses anti-aliasing.
	 * @param aa True if anti-aliasing is on, false if off
	 */
	public void setAntiAliasing( boolean aa ) {
		this.antiAliasing = aa;
	}
	
	boolean getAntiAliasing() {
		return this.antiAliasing;
	}
	
	/**
	 * Sets a background image to use for the graph.
	 * The image can be any of the supported imageio formats,
	 * default <i>.gif, .jpg or .png</i>.
	 * @param fileName Filename of the image to use
	 */
	public void setBackground( String fileName ) {
		File bgFile	= new File( fileName );
		if ( bgFile.exists() )
			this.background = bgFile;
	}
	
	File getBackground() {
		return this.background;
	}
	
	/**
	 * Sets a overlay image to use for the graph.
	 * The image can be any of the supported imageio formats,
	 * default <i>.gif, .jpg or .png</i>.  All pixels with the color white
	 * RGB (255, 255, 255) will be treated as transparent.
	 * @param fileName Filename of the image to use
	 */
	public void setOverlay( String fileName ) {
		File ovFile	= new File( fileName );
		if ( ovFile.exists() )
			this.overlay = ovFile;
	}

	File getOverlay() {
		return this.overlay;
	}
	
	/**
	 * Should write explanation of custom grid specs here.
	 * @param minGridTime
	 * @param minGridUnits
	 * @param majGridTime
	 * @param majGridUnits
	 * @param df
	 * @param centered
	 */
	public void setTimeAxis( int minGridTime, 
								int minGridUnits, 
								int majGridTime, 
								int majGridUnits, 
								String df,
								boolean centered ) 
	{
		this.tAxis 			= new TimeAxisUnit( minGridTime, 
												minGridUnits, 
												majGridTime, 
												majGridUnits, 
												new SimpleDateFormat( df ) 
											);
		this.tAxisCentered	= centered;		
	}
	
	TimeAxisUnit getTimeAxis() {
		return this.tAxis;
	}
	
	boolean getTimeAxisCentered() {
		return this.tAxisCentered;
	}
	
	/**
	 * Should write explanation of custom grid specs here
	 * @param gridStep
	 * @param labelStep
	 */
	public void setValueAxis( double gridStep, double labelStep ) {
		this.valueGridStep 	= gridStep;
		this.valueLabelStep = labelStep;
	}
	
	double getValueGridStep() {
		return this.valueGridStep;
	}
	
	double getValueLabelStep() {
		return this.valueLabelStep;
	}
	
	/**
	 * 
	 * @param base
	 */
	public void setBaseValue( double base ) {
		this.baseValue = base;
	}
	
	double getBaseValue() {
		return this.baseValue;
	}
	
	/**
	 * 
	 * @param e
	 */
	public void setUnitsExponent( int e ) {
		this.scaleIndex = (6 - e / 3);	// Index in the scale table
	}
	
	int getScaleIndex() {
		return this.scaleIndex;
	}
}
