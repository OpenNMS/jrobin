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
package jrobin.graph;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import jrobin.core.Util;
import jrobin.core.RrdDb;
import jrobin.core.RrdException;

/**
 * <p>Creates a BufferedImage of a graph, based on data from a GraphDef.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Grapher 
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final String SPACER			= "  ";					// default comment spacer (two blank spaces)
	protected static final int GRAPH_RESOLUTION		= 400;					// default graph resolution
	protected static final int DEFAULT_WIDTH		= 400;					// default width in pixels of the chart area
	protected static final int DEFAULT_HEIGHT		= 100;					// default height in pixels of the chart area
	
	// Border space definitions
	protected static final int UBORDER_SPACE		= 10;					// padding from graph upper border
	protected static final int BBORDER_SPACE		= 10;					// padding from graph lower border
	protected static final int LBORDER_SPACE		= 10;					// padding from graph left border
	protected static final int RBORDER_SPACE		= 13;					// padding from graph right border
	
	protected static final int CHART_UPADDING		= 5;					// padding space above the chart area
	protected static final int CHART_BPADDING		= 25;					// default padding space below the chart area			
	protected static final int CHART_RPADDING		= 10;					// padding space on the right of the chart area
	protected static final int CHART_LPADDING		= 50;					// default padding space on the left of the chart area
	protected static final int CHART_BPADDING_NM	= 10;					// default padding below chart if no legend markers
	protected static final int CHART_LPADDING_NM	= 10;					// default padding left of chart if no legend markers
	
	protected static final int LINE_PADDING			= 4;					// default padding between two consecutive text lines
	
	// Default fonts
	protected static final Font TITLE_FONT			= new Font("Lucida Sans Typewriter", Font.BOLD, 12);
	protected static final Font NORMAL_FONT			= new Font("Lucida Sans Typewriter", Font.PLAIN, 10);
	
	private Font title_font 						= TITLE_FONT;			// font used for the title 
	private Font normal_font	 					= NORMAL_FONT;			// font used for all default text
	private Color normalFontColor					= null;
	private int numPoints 							= GRAPH_RESOLUTION;		// number of points used to calculate the graph
	
	private int chart_lpadding, chart_bpadding;								// calculated padding on the left and below the chart area
	private int imgWidth, imgHeight;										// dimensions of the entire image
	private int chartWidth, chartHeight;									// dimensions of the chart area within the image	
	private int nfont_width, nfont_height, tfont_width, tfont_height;		// font dimennsion specs (approximated)
	private int commentBlock;												// size in pixels of the block below the chart itself
	private int graphOriginX, graphOriginY, x_offset, y_offset;
	
	private RrdGraphDef graphDef;
	private RrdGraph rrdGraph;
	
	private Source[] sources;
	private HashMap sourceIndex;
	private long[] timestamps;

	private ValueFormatter valueFormat;
	private BasicStroke	defaultStroke;
	private ValueGrid vGrid;
	private TimeGrid tGrid;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a grapher object, used for creating a graph image based on a <code>RrdGraphDef</code> object.
	 * A reference to a <code>RrdGraph</code> object is kept for <code>RrdDb</code> pooling.
	 * @param graphDef Graph definition for the graph to be created.
	 * @param rrdGraph RrdGraph object that takes care of saving the images.
	 */
	Grapher( RrdGraphDef graphDef, RrdGraph rrdGraph )
	{
		this.graphDef = graphDef;
		this.rrdGraph = rrdGraph;
		
		// Set font dimension specifics
		if ( graphDef.getDefaultFont() != null )
			normal_font = graphDef.getDefaultFont();
		if ( graphDef.getTitleFont() != null )
			title_font	= graphDef.getTitleFont();
		normalFontColor	= graphDef.getDefaultFontColor();
		
		nfont_height 	= normal_font.getSize();		// Determine font dimensions for regular comment font
		nfont_width		= nfont_height / 2 + 1;
		
		// Bold font is higher
		tfont_height	= ( title_font.isBold() ? title_font.getSize() + 2 : title_font.getSize() );
		tfont_width		= ( title_font.isBold() ? tfont_height / 2 : tfont_height / 2 + 1 );
		
		// Create the shared valueformatter
		valueFormat 	= new ValueFormatter( graphDef.getBaseValue(), graphDef.getScaleIndex() );
		
		// Set default graph stroke
		defaultStroke	= new BasicStroke();
	}
	
	
	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	/**
	 * Creates the actual graph based on the GraphDef definition.
	 * The graph is created as a <code>java.awt.image.BufferedImage</code>.
	 * @param cWidth Width of the chart area in pixels.
	 * @param cHeight Height of the chart area in pixels.
	 * @return The created graph as a BufferedImage.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 * @throws IOException Thrown in case of a I/O related error.
	 */
	protected BufferedImage createImage( int cWidth, int cHeight, int colorType ) throws RrdException, IOException
	{
		// Calculate chart dimensions
		chartWidth			= ( cWidth == 0 ? DEFAULT_WIDTH : cWidth );
		chartHeight			= ( cHeight == 0 ? DEFAULT_HEIGHT : cHeight );

		if ( cWidth > 0 ) numPoints = cWidth;

		// Padding depends on grid visibility
		chart_lpadding 		= ( graphDef.showMajorGridY() ? graphDef.getChartLeftPadding() : CHART_LPADDING_NM );
		chart_bpadding 		= ( graphDef.showMajorGridX() ? CHART_BPADDING : CHART_BPADDING_NM );
		
		// Size of all lines below chart
		commentBlock		= 0;
		if ( graphDef.showLegend() )
			commentBlock 	= graphDef.getCommentLineCount() * (nfont_height + LINE_PADDING) - LINE_PADDING;		
	
		// x_offset and y_offset define the starting corner of the actual graph 
		x_offset			= LBORDER_SPACE;
		if ( graphDef.getVerticalLabel() != null ) 
			x_offset 		+= nfont_height + LINE_PADDING;
		imgWidth			= chartWidth + x_offset + RBORDER_SPACE + chart_lpadding + CHART_RPADDING;
		
		y_offset			= UBORDER_SPACE;
		if ( graphDef.getTitle() != null )			// Title *always* gets a extra LF automatically 
			y_offset		+= ((tfont_height + LINE_PADDING) * graphDef.getTitle().getLineCount() + tfont_height) + LINE_PADDING;
		imgHeight 			= chartHeight + commentBlock + y_offset + BBORDER_SPACE + CHART_UPADDING + CHART_BPADDING;
		
		// Create graphics object
		BufferedImage bImg 	= new BufferedImage( imgWidth, imgHeight, colorType );
		Graphics2D graphics	= (Graphics2D) bImg.getGraphics();
		
		// Do the actual graphing
		calculateSeries();							// calculate all datasources
						
		plotImageBackground( graphics );			// draw the image background
			
		plotChart( graphics );						// draw the actual chart
			
		plotComments( graphics );					// draw all comment lines
			
		plotOverlay( graphics );					// draw a possible image overlay
			
		plotSignature( graphics );					// draw the JRobin signature

		
		// Dispose graphics context
		graphics.dispose();
		
		return bImg;
	}
	
	
	// ================================================================
	// -- Private methods
	// ================================================================
	/**
	 * Fetches and calculates all datasources used in the graph.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 * @throws IOException Thrown in case of a I/O related error.
	 */
	private void calculateSeries() throws RrdException, IOException
	{
		ValueExtractor ve;
		FetchSource src;
		RrdDb rrd;
		String[] varList;
		
		long startTime 			= graphDef.getStartTime();
		long endTime			= graphDef.getEndTime();
	
		int numDefs				= graphDef.getNumDefs();
		
		Cdef[] cdefList			= graphDef.getCdefs();
		int numCdefs			= cdefList.length;
	
		// Set up the array with all datasources (both Def and Cdef)
		sources 				= new Source[ numDefs + numCdefs ];
		sourceIndex 			= new HashMap( numDefs + numCdefs );
		int tblPos				= 0;
		int vePos				= 0;
	
		ValueExtractor[] veList	= new ValueExtractor[ graphDef.getFetchSources().size() ];
		Iterator fetchSources 	= graphDef.getFetchSources().values().iterator();
		
		while ( fetchSources.hasNext() )
		{
			// Get the rrdDb
			src 	= (FetchSource) fetchSources.next();
			rrd		= rrdGraph.getRrd( src.getRrdFile() ); 
		
			// Fetch all required datasources
			ve 		= src.fetch( rrd, startTime,  endTime );
			varList = ve.getNames();
		
			for (int i= 0; i < varList.length; i++) {
				sources[tblPos]	= new Def(varList[i], numPoints);
				sourceIndex.put( varList[i], new Integer(tblPos++) );
			}
			
			veList[ vePos++ ] = ve;
		}
	
		// Add all Cdefs to the source table		
		// Reparse all RPN datasources to use indices of the correct variables
		for ( int i = 0; i < cdefList.length; i++ )
		{
			cdefList[i].prepare( sourceIndex, numPoints );
		
			sources[tblPos]	= cdefList[i];
			sourceIndex.put( cdefList[i].getName(), new Integer(tblPos++) );	
		}
	
		// RPN calculator for the Cdefs
		RpnCalculator rpnCalc 	= new RpnCalculator( sources );
	
		// Fill the array for all datasources
		timestamps 				= new long[numPoints];
	
		for (int i = 0; i < numPoints; i++) 
		{
			long t 	= (long) (startTime + i * ((endTime - startTime) / (double)(numPoints - 1)));
			int pos = 0;
		
			// Get all fetched datasources
			for (int j = 0; j < veList.length; j++)
				pos = veList[j].extract( t, sources, i, pos );
		
			// Get all combined datasources
			for (int j = pos; j < sources.length; j++)
				sources[j].set(i, t, rpnCalc.evaluate( (Cdef) sources[j], i, t ) );

			timestamps[i] = t;
		}
	
		// Clean up the fetched datasources forcibly
		veList = null;
	}
	
	/**
	 * Draws the image background, title and value axis label.
	 * @param g Handle of a Graphics2D context to draw on.
	 */
	private void plotImageBackground( Graphics2D g )
	{
		// Draw general background color
		g.setColor( graphDef.getBackColor() );
		g.fillRect(0, 0, imgWidth, imgHeight );
	
		// Draw a background image, if background image fails, just continue
		try {
			File bgImage = graphDef.getBackground();
			if ( bgImage != null ) {
				RenderedImage img = ImageIO.read(bgImage);
				g.drawRenderedImage( img, null );
			}
		} catch (IOException e) {}
	
		// Set the image border
		Color bc 		= graphDef.getBorderColor();
		BasicStroke bs	= graphDef.getBorderStroke();

		if ( bs != null && bc != null )				// custom single line border
		{
			g.setColor( bc );
			g.setStroke( bs );
			
			// Check for 'visible' line width
			int w = new Float(bs.getLineWidth()).intValue();
			if ( w > 0 ) 
				g.drawRect( w / 2, w / 2, imgWidth - w, imgHeight - w);
			
			g.setStroke( defaultStroke );
		}
		else										// default slightly beveled border
		{
			g.setColor( new Color( 0xdc, 0xdc, 0xdc ) );
			g.fillRect( 0, 0, 2, imgHeight - 1 );
			g.fillRect( 0, 0, imgWidth - 1, 2 );
			g.setColor( Color.GRAY );
			g.drawLine( 0, imgHeight - 1, imgWidth, imgHeight - 1 );
			g.drawLine( imgWidth - 1, 0, imgWidth - 1, imgHeight );
			g.drawLine( 1, imgHeight - 2, imgWidth, imgHeight - 2 );
			g.drawLine( imgWidth - 2, 1, imgWidth - 2, imgHeight );
		}
	
		plotImageTitle( g );
		
		plotVerticalLabel( g );
	}
	
	/**
	 * Plots all datasources on the graph, uses all values gathered in {@link #CalculateSeries() }.
	 * @param graphics Handle of a Graphics2D context to draw on.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	private void plotChart( Graphics2D graphics ) throws RrdException
	{
		int lux		= x_offset + chart_lpadding;
		int luy		= y_offset + CHART_UPADDING;

		// Canvas color should only be drawn if no background image is set
		// If there's a background image, canvas should be transparent
		if ( graphDef.getBackground() == null ) {
			graphics.setColor( graphDef.getCanvasColor() );
			graphics.fillRect( lux, luy, chartWidth, chartHeight );
		}
	
		// Draw the chart area frame
		graphics.setColor( graphDef.getFrameColor() );
		graphics.drawRect( lux, luy, chartWidth, chartHeight );
			
		double val;
		double[] tmpSeries 	= new double[numPoints];
		
		GridRange range		= graphDef.getGridRange();
		boolean rigid		= ( range != null ? range.isRigid() : false );
		double lowerValue	= ( range != null ? range.getLowerValue() : Double.MAX_VALUE );
		double upperValue	= ( range != null ? range.getUpperValue() : Double.MIN_VALUE );
		
		// For autoscale, detect lower and upper limit of values
		PlotDef[] plotDefs 	= graphDef.getPlotDefs();
		for ( int i = 0; i < plotDefs.length; i++ )
		{
			plotDefs[i].setSource( sources, sourceIndex );
			Source src = plotDefs[i].getSource();
		
			// Only try autoscale when we do not have a rigid grid
			if ( !rigid && src != null )
			{
				double min = src.getAggregate( Source.AGG_MINIMUM );
				double max = src.getAggregate( Source.AGG_MAXIMUM );
			
				// If the plotdef is a stack, evaluate ALL previous values to find a possible max
				if ( plotDefs[i].plotType == PlotDef.PLOT_STACK && i >= 1 ) 
				{
					if ( plotDefs[i - 1].plotType == PlotDef.PLOT_STACK ) {		// Use this source plus stack of previous ones
					
						for (int j = 0; j < tmpSeries.length; j++)
						{
							val = tmpSeries[j] + plotDefs[i].getValue(j, timestamps);
	
							if ( val < lowerValue ) lowerValue = val;
							if ( val > upperValue ) upperValue = val;
	
							tmpSeries[j] = val;
						}
					}
					else {														// Use this source plus the previous one
					
						for (int j = 0; j < tmpSeries.length; j++)
						{
							val = plotDefs[i - 1].getValue(j, timestamps) + plotDefs[i].getValue(j, timestamps);
						
							if ( val < lowerValue ) lowerValue = val;
							if ( val > upperValue ) upperValue = val;
						
							tmpSeries[j] = val;
						}
	
					}
				}
				else		// Only use min/max of a single datasource
				{
					if ( min < lowerValue ) lowerValue 	= min;
					if ( max > upperValue ) upperValue	= max;
				}
			}
		
		}
		
		vGrid 			= new ValueGrid( range, lowerValue, upperValue, graphDef.getValueAxis() );
		tGrid			= new TimeGrid( graphDef.getStartTime(), graphDef.getEndTime(), graphDef.getTimeAxis() );
		
		lowerValue		= vGrid.getLowerValue();
		upperValue		= vGrid.getUpperValue();
						
		// Use a special graph 'object' that takes care of resizing and reversing y coordinates
		ChartGraphics g 	= new ChartGraphics( graphics );
		g.setDimensions( chartWidth, chartHeight );
		g.setXRange( tGrid.getStartTime(), tGrid.getEndTime() );
		g.setYRange( lowerValue, upperValue );
		
		// Set the chart origin point
		double diff = 1.0d;
		if ( lowerValue < 0 )
			diff = 1.0d - ( lowerValue / ( -upperValue + lowerValue ));
		graphOriginX = lux;
		graphOriginY = new Double(luy + chartHeight * diff).intValue();

		// If the grid is behind the plots, draw it first
		if ( !graphDef.isFrontGrid() ) plotChartGrid( g );

		// Use AA if necessary
		if ( graphDef.useAntiAliasing() )
			graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		// Prepare clipping area and origin
		graphics.setClip( lux, luy, chartWidth, chartHeight);
		graphics.translate( graphOriginX, graphOriginY );
 
		int lastPlotType 	= PlotDef.PLOT_LINE;
		int[] parentSeries 	= new int[numPoints];

		// Pre calculate x positions of the corresponding timestamps
		int[] xValues		= new int[timestamps.length];
		for (int i = 0; i < timestamps.length; i++)
			xValues[i]		= g.getX(timestamps[i]);
	
		// Draw all graphed values
		for (int i = 0; i < plotDefs.length; i++) 
		{
			plotDefs[i].draw( g, xValues, parentSeries, lastPlotType );
			lastPlotType = plotDefs[i].plotType;
		}

		// Reset clipping area, origin and AA settings
		graphics.translate( -graphOriginX, -graphOriginY );
		graphics.setClip( 0, 0, imgWidth, imgHeight);
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );

		// If the grid is in front of the plots, draw it now
		if ( graphDef.isFrontGrid() ) plotChartGrid( g );
	}
	
	/**
	 * Plots the chart grid on the graph, both value and time axis minor and major grid lines.
	 * Accompanied by approriate labels at defined intervals.
	 * @param chartGraph ChartGraphics object containing a Graphics2D handle to draw on.
	 */
	private void plotChartGrid( ChartGraphics chartGraph )
	{
		Graphics2D g = chartGraph.getGraphics();
		g.setFont( normal_font );

		int lux = x_offset + chart_lpadding;
		int luy = y_offset + CHART_UPADDING;

		boolean minorX	= graphDef.showMinorGridX();
		boolean minorY	= graphDef.showMinorGridY();
		boolean majorX	= graphDef.showMajorGridX();
		boolean majorY	= graphDef.showMajorGridY();
		
		Color minColor	= graphDef.getMinorGridColor();
		Color majColor	= graphDef.getMajorGridColor();
		
		// Dashed line
		float[] dashPattern = { 1, 1 };
		BasicStroke dStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
								BasicStroke.JOIN_MITER, 10,
								dashPattern, 0);

		// Draw basic axis
		int tmpx = lux + chartWidth;
		int tmpy = luy + chartHeight;

		// Draw X axis with arrow
		g.setColor( graphDef.getAxisColor() );
		g.drawLine( lux - 4, tmpy, tmpx + 4, tmpy );
		g.setColor( graphDef.getArrowColor() );
		g.drawLine( tmpx + 4, tmpy - 3, tmpx + 4, tmpy + 3 );
		g.drawLine( tmpx + 4, tmpy - 3, tmpx + 9, tmpy );
		g.drawLine( tmpx + 4, tmpy + 3, tmpx + 9, tmpy );

		// Draw X axis time grid and labels
		if ( graphDef.showGridX() )
		{
			TimeMarker[] timeList	= tGrid.getTimeMarkers();
			boolean labelCentered	= tGrid.centerLabels();
			long labelGridWidth		= tGrid.getMajorGridWidth();
			
			int pixWidth 			= 0;
			if ( labelCentered )
				pixWidth = ( chartGraph.getX( labelGridWidth ) - chartGraph.getX( 0 ) );
			
			for (int i = 0; i < timeList.length; i++)
			{
				long secTime 	= timeList[i].getTimestamp();
				int posRel 		= chartGraph.getX(secTime);
				int pos 		= lux + posRel;
				String label	= timeList[i].getLabel();
				
				if ( posRel >= 0 ) {
					if ( majorX && timeList[i].isLabel() )
					{
						g.setColor( majColor );
						g.setStroke( dStroke );
						g.drawLine( pos, luy, pos, luy + chartHeight );
						g.setStroke( defaultStroke );
						g.drawLine( pos, luy - 2, pos, luy + 2 );
						g.drawLine( pos, luy + chartHeight - 2, pos, luy + chartHeight + 2 );
						// Only draw label itself if we are far enough from the side axis
						// Use extra 2 pixel padding (3 pixels from border total at least)
						int txtDistance = (label.length() * nfont_width) / 2;
				
						if ( labelCentered )
						{
							if ( pos + pixWidth <= lux + chartWidth )
								graphString( g, label, pos + 2 + pixWidth/2 - txtDistance, luy + chartHeight + nfont_height + LINE_PADDING );
						}
						else if ( (pos - lux > txtDistance + 2) && (pos + txtDistance + 2 < lux + chartWidth) )	
							graphString( g, label, pos - txtDistance, luy + chartHeight + nfont_height + LINE_PADDING );
					}
					else if ( minorX )
					{	
						g.setColor( minColor );
						g.setStroke( dStroke );
						g.drawLine( pos, luy, pos, luy + chartHeight );
						g.setStroke( defaultStroke );
						g.drawLine( pos, luy - 1, pos, luy + 1 );
						g.drawLine( pos, luy + chartHeight - 1, pos, luy + chartHeight + 1 );
			
					}
				}
			}
		}
		
		// Draw Y axis value grid and labels
		valueFormat.setScaling( true, false );			// always scale the label values
		if ( graphDef.showGridY() )
		{
			ValueMarker[] valueList = vGrid.getValueMarkers();
			
			for (int i = 0; i < valueList.length; i++)
			{
				int valRel 		= chartGraph.getY( valueList[i].getValue() );
				
				valueFormat.setFormat( valueList[i].getValue(), 2, 0 );
				String label	= (valueFormat.getScaledValue() + " " + valueFormat.getPrefix()).trim();
	
				if ( majorY && valueList[i].isMajor() )
				{
					g.setColor( majColor );
					g.setStroke( dStroke );
					g.drawLine( graphOriginX, graphOriginY - valRel, graphOriginX + chartWidth, graphOriginY - valRel );
					g.setStroke( defaultStroke );
					g.drawLine( graphOriginX - 2, graphOriginY - valRel, graphOriginX + 2, graphOriginY - valRel );
					g.drawLine( graphOriginX + chartWidth - 2, graphOriginY - valRel, graphOriginX + chartWidth + 2, graphOriginY - valRel );
					graphString( g, label, graphOriginX - (label.length() * nfont_width) - 7, graphOriginY - valRel + nfont_height/2 - 1 );
				}
				else if ( minorY )
				{
					g.setColor( minColor );
					g.setStroke( dStroke );
					g.drawLine( graphOriginX, graphOriginY - valRel, graphOriginX + chartWidth, graphOriginY - valRel );
					g.setStroke( defaultStroke );
					g.drawLine( graphOriginX - 1, graphOriginY - valRel, graphOriginX + 1, graphOriginY - valRel );
					g.drawLine( graphOriginX + chartWidth - 1, graphOriginY - valRel, graphOriginX + chartWidth + 1, graphOriginY - valRel );
				}

			}
		}
		
	}
	
	/**
	 * Plots all comments and legends on graph.
	 * @param g Handle of a Graphics2D context to draw on.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	private void plotComments( Graphics2D g ) throws RrdException
	{
		if ( !graphDef.showLegend() ) return;
		
		LinkedList markerList = new LinkedList();
		
		// Position the cursor just below the chart area
		int posy			= y_offset + chartHeight + CHART_UPADDING + CHART_BPADDING + nfont_height;
		int posx			= LBORDER_SPACE;

		g.setColor( normalFontColor );
		g.setFont( normal_font );
		
		Comment[] clist		= graphDef.getComments();
		StringBuffer tmpStr	= new StringBuffer("");

		boolean newLine		= false;
		boolean drawText	= false;
		
		for (int i = 0; i < clist.length; i++)
		{
			if ( clist[i].commentType == Comment.CMT_LEGEND ) 
			{
				markerList.addLast( new LegendMarker( tmpStr.length() * nfont_width, ((Legend) clist[i]).getColor() ) );
				tmpStr.append( "   " );		// Add 3 spaces where the mark will be
			} 
			else if ( clist[i].commentType == Comment.CMT_GPRINT )
				((Gprint) clist[i]).setValue( sources, sourceIndex, valueFormat );
			
			Vector tknpairs = clist[i].getTokens();
			
			for (int j = 0; j < tknpairs.size(); j++)
			{
				String str 	= (String) tknpairs.elementAt(j++);
				Byte tkn	= (Byte) tknpairs.elementAt(j);
				
				if ( clist[i].trimString() )
					tmpStr.append( str.trim() );
				else
					tmpStr.append( str );
					
				if ( tkn != Comment.TKN_NULL )
				{
					drawText = true;
					if ( tkn == Comment.TKN_ALF ) {
						newLine	= true;
						posx	= LBORDER_SPACE;					
					} 
					else if ( tkn == Comment.TKN_ARF ) {
						newLine	= true;
						posx 	= imgWidth - RBORDER_SPACE - (tmpStr.length() * nfont_width);
					}
					else if ( tkn == Comment.TKN_ACF ) {
						newLine	= true;
						posx 	= imgWidth / 2 - (tmpStr.length() * nfont_width) / 2;
					}
					else if ( tkn == Comment.TKN_AL )
						posx	= LBORDER_SPACE;
					else if ( tkn == Comment.TKN_AR )
						posx 	= imgWidth - RBORDER_SPACE - (tmpStr.length() * nfont_width);
					else if ( tkn == Comment.TKN_AC )
						posx 	= imgWidth / 2 - (tmpStr.length() * nfont_width) / 2;
				}
				
				if ( !newLine && clist[i].addSpacer() )
					tmpStr.append( SPACER );
								
				// Plot the string
				if ( drawText ) {
					graphString( g, tmpStr.toString(), posx, posy );
					tmpStr		= new StringBuffer(""); 
					drawText	= false;

					// Plot the markers	
					while ( !markerList.isEmpty() ) {
						LegendMarker lm = (LegendMarker) markerList.removeFirst();
						g.setColor( lm.getColor() );
						g.fillRect( posx + lm.getXPosition(), posy - 9, 10, 10 );
						g.setColor( normalFontColor );
						g.drawRect( posx + lm.getXPosition(), posy - 9, 10, 10 );
					}
				}
				
				if ( newLine ) {
					posy 	+= nfont_height + LINE_PADDING;
					newLine	= false;
				}
				
			}
		}
		
		if ( tmpStr.length() > 0)
		{
			posx		= LBORDER_SPACE;
			graphString( g, tmpStr.toString(), posx, posy );
			tmpStr		= new StringBuffer(""); 
			drawText	= false;

			// Plot the markers	
			while ( !markerList.isEmpty() ) {
				LegendMarker lm = (LegendMarker) markerList.removeFirst();
				g.setColor( lm.getColor() );
				g.fillRect( posx + lm.getXPosition(), posy - 9, 10, 10 );
				g.setColor( normalFontColor );
				g.drawRect( posx + lm.getXPosition(), posy - 9, 10, 10 );
			}			
		}
	}
	
	/**
	 * Plots a possible overlay image over the current graph.  All white pixels
	 * are ignored and treated as 100% transparent.
	 * @param g Handle of a Graphics2D context to draw on.
	 */
	private void plotOverlay( Graphics2D g )
	{
		// If overlay drawing fails, just ignore it
		try 
		{
			File overlayImg = graphDef.getOverlay();
			if ( overlayImg != null )
			{
				BufferedImage img 	= ImageIO.read(overlayImg);
			
				int w 				= img.getWidth();
				int h 				= img.getHeight();
				int rgbWhite 		= Color.WHITE.getRGB(); 
				int pcolor, red, green, blue;

				// For better performance we might want to load all color
				// ints of the overlay in one go
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						pcolor = img.getRGB(i, j);
						if ( pcolor != rgbWhite ) 
						{
							red 	= (pcolor >> 16) & 0xff;
							green 	= (pcolor >> 8) & 0xff;
							blue 	= pcolor & 0xff;

							g.setColor( new Color(red, green, blue) );
							g.drawLine( i, j, i, j );
						}
					}
				}
			}
		} catch (IOException e) {}	
	}
	
	/**
	 * Plots the graph title in the corresponding title font.
	 * @param g Handle of a Graphics2D context to draw on.
	 */
	private void plotImageTitle( Graphics2D g )
	{
		Title graphTitle	= graphDef.getTitle();
		
		// No title to draw
		if ( graphTitle == null )
			return;
		
		// Position the cursor just above the chart area
		int posy			= tfont_height - 1 + UBORDER_SPACE;
		int posx			= LBORDER_SPACE;

		// Set drawing specifics
		g.setColor( graphDef.getTitleFontColor() );
		g.setFont( title_font );

		// Parse and align the title text
		StringBuffer tmpStr	= new StringBuffer("");
		boolean newLine		= false;

		Vector tknpairs = graphTitle.getTokens();
		for (int j = 0; j < tknpairs.size(); j++)
		{
			String str 	= (String) tknpairs.elementAt(j++);
			Byte tkn	= (Byte) tknpairs.elementAt(j);

			tmpStr.append( str );
			if ( tkn != Comment.TKN_NULL )
			{
				if ( tkn == Comment.TKN_ALF ) {
					newLine	= true;
					posx	= LBORDER_SPACE;					
				} 
				else if ( tkn == Comment.TKN_ARF ) {
					newLine	= true;
					posx 	= imgWidth - RBORDER_SPACE - (tmpStr.length() * tfont_width);
				}
				else if ( tkn == Comment.TKN_ACF ) {
					newLine	= true;
					posx 	= imgWidth / 2 - (tmpStr.length() * tfont_width) / 2;
				}
				else if ( tkn == Comment.TKN_AL )
					posx	= LBORDER_SPACE;
				else if ( tkn == Comment.TKN_AR )
					posx 	= imgWidth - RBORDER_SPACE - (tmpStr.length() * tfont_width);
				else if ( tkn == Comment.TKN_AC )
					posx 	= imgWidth / 2 - (tmpStr.length() * tfont_width) / 2;
			}
			else {		// default is a center alignment for title
				posx 	= imgWidth / 2 - (tmpStr.length() * tfont_width) / 2;
			}

			// Plot the string
			g.drawString( tmpStr.toString(), posx, posy );
			tmpStr		= new StringBuffer(""); 

			// Go to next line
			if ( newLine )
			{
				posy += tfont_height + LINE_PADDING;
				newLine	= false;
			}
		}
		
	}
	
	/**
	 * Plots the vertical label on the left hand side of the chart area.
	 * @param g Handle of a Graphics2D context to draw on.
	 */
	private void plotVerticalLabel( Graphics2D g )
	{
		String valueAxisLabel 	= graphDef.getVerticalLabel();
		
		if ( valueAxisLabel == null )
			return;
		
		g.setColor( normalFontColor );
		int labelWidth			= valueAxisLabel.length() * nfont_width;

		// draw a rotated label text as vertical label
		g.setFont( normal_font );
		g.rotate( -Math.PI/2.0 );
		graphString( g, valueAxisLabel, - y_offset - CHART_UPADDING
										- chartHeight / 2 
										- labelWidth / 2,
										LBORDER_SPACE + nfont_height
										);
		g.rotate( Math.PI/2.0 );
	}

	/**
	 * Draws the standard JRobin signature on the image.
	 * @param g Handle of a Graphics2D context to draw on.
	 */
	private void plotSignature( Graphics2D g )
	{
		if ( !graphDef.showSignature() )
			return;
		
		String sig = "www.jrobin.org"; 
		g.setColor( Color.GRAY );
		g.setFont( new Font("Courier", Font.PLAIN, 10) );
	
		g.rotate( Math.PI/2.0 );
		g.drawString( sig, 5, - imgWidth + 9 );	
		g.rotate( -Math.PI/2.0 );
	}	

	/**
	 * Graphs a text string onto a graphics2d context, using the specified default font color.
	 * @param g Handle of a Graphics2D context to draw on.
	 * @param str String to draw.
	 * @param x X start position of the string.
	 * @param y Y start position of the string.
	 */
	private void graphString( Graphics2D g, String str, int x, int y )
	{
		Color oc = g.getColor();
		
		g.setColor( normalFontColor );
		g.drawString( str, x, y );
		
		g.setColor( oc );
	}	
}
