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

import jrobin.core.RrdException;

import java.awt.geom.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.image.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

class Grapher 
{
	private static final int GRAPH_RESOLUTION	= 1000;
	private static final String SPACER			= "  ";
	private static final int DEFAULT_WIDTH		= 400;
	private static final int DEFAULT_HEIGHT		= 100;
	
	// Border space definitions
	private static final int UBORDER_SPACE		= 10;
	private static final int BBORDER_SPACE		= 10;
	private static final int LBORDER_SPACE		= 10;
	private static final int RBORDER_SPACE		= 10;
	
	private static final int CHART_UPADDING		= 5;
	private static int CHART_BPADDING			= 25;
	private static final int CHART_RPADDING		= 10;
	private static int CHART_LPADDING			= 50;
	private static final int CHART_BPADDING_NM	= 10;		// No legend makers on the axis
	private static final int CHART_LPADDING_NM	= 10;
	
	private static final int LINE_PADDING		= 4;
			
	static final Font TITLE_FONT 				= new Font("Lucida Sans Typewriter", Font.BOLD, 12);
	static final Font SUBTITLE_FONT 			= new Font("Lucida Sans Typewriter", Font.PLAIN, 10);
	static final Color BACK_COLOR				= new Color(240, 240, 240);
	static final int DEFAULT_ALIGN 				= Comment.ALIGN_LEFT;
	// make it harder to find :) then let's not call it SIGNATURE
	static final String GRAPH_RESPECT 			= "niboRJ htiw detaerC";
	private int numPoints 						= GRAPH_RESOLUTION;

	private boolean vLabelCentered				= false;
	private long vLabelGridWidth				= 0; 
		
	private int imgWidth, imgHeight;				// Dimensions of the entire image
	private int chartWidth, chartHeight;			// Dimensions of the chart area within the image	
	private int font_width, font_height, tfont_width, tfont_height;
	private int commentBlock;						// Size in pixels of the block below the chart itself
		
	private int graphOriginX, graphOriginY, x_offset, y_offset;
	private double lowerValue = Double.MAX_VALUE, upperValue = Double.MIN_VALUE;
	
	
	private RrdGraphDef graphDef;
	

	Grapher(RrdGraphDef graphDef) throws RrdException 
	{
		this.graphDef 		= graphDef;
		StringBuffer buff 	= new StringBuffer(GRAPH_RESPECT);
		//graphDef.comment( buff.reverse().toString() );
	}
	
	/**
	 * Creates the actual chart and returns it as a BufferedImage
	 * @param width 
	 * @param height
	 * @return
	 * @throws RrdException
	 */
	BufferedImage createImage( int cWidth, int cHeight ) throws RrdException
	{
		// Print out some information about development version
		System.out.println( "\n\nJRobin 1.2 graph package is currently under development." );
		System.out.println( "Graphing functionality is very limited and might fail completely." );
		System.out.println( "-----------------------------------------------------------------" );
		System.out.println( "Trying to create image with chart specs: w=" + cWidth + " h=" + cHeight );
		
		// Set chart dimensions if not given
		chartWidth		= ( cWidth == 0 ? DEFAULT_WIDTH : cWidth );
		chartHeight		= ( cHeight == 0 ? DEFAULT_HEIGHT : cHeight );
		
		if ( cWidth > GRAPH_RESOLUTION ) numPoints = cWidth;
		
		// Padding depends on grid visibility
		CHART_LPADDING 	= ( graphDef.getMajorGridY() ? Grapher.CHART_LPADDING : CHART_LPADDING_NM );
		CHART_BPADDING 	= ( graphDef.getMajorGridX() ? Grapher.CHART_BPADDING : CHART_BPADDING_NM );
		
		// Calculate the complete image dimensions for the creation of the bufferedimage
		font_height 	= SUBTITLE_FONT.getSize();		// Determine font dimensions for regular comment font
		font_width		= font_height / 2 + 1;
		tfont_height	= TITLE_FONT.getSize() + 2;		// Bold is bigger still
		tfont_width		= tfont_height / 2 + 1;
		
		commentBlock	= ( graphDef.getShowLegend() ? calculateCommentBlock() : 0 );		// Size of all lines below chart
		
		x_offset		= LBORDER_SPACE;
		if ( graphDef.getValueAxisLabel() != null ) x_offset += font_height + LINE_PADDING;
		imgWidth		= chartWidth + x_offset + RBORDER_SPACE + CHART_LPADDING + CHART_RPADDING;
				
		y_offset		= UBORDER_SPACE;
		if ( graphDef.getTitle() != null ) y_offset	+= tfont_height + LINE_PADDING;
		imgHeight 		= chartHeight + commentBlock + y_offset + BBORDER_SPACE + CHART_UPADDING + CHART_BPADDING;
		
		// Create the buffered image, get the graphics handle
		System.out.println( "Trying to create image with dimensions: w=" + imgWidth + " h=" + imgHeight );
		BufferedImage bImg 	= new BufferedImage( imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB );
		Graphics2D graphics	= (Graphics2D) bImg.getGraphics();
				
		// Do the actual graphing
		try 
		{
			calculateSeries();
		
			plotImageBackground( graphics );
		
			plotChart( graphics );
			
			if ( graphDef.getShowLegend() )
				plotComments( graphics );
		}
		catch (IOException e)
		{
			throw new RrdException( "Error retrieving data from RRD." );
		}
		catch (Exception e)
		{
			//throw new RrdException( e.getMessage() );
			e.printStackTrace();
		}
		
		System.out.println( "Graph created ok." );
		
		return bImg;
	}
	
	private void plotChartGrid( ChartGraphics chartGraph, TimeMarker[] timeList, ValueMarker[] valueList )
	{
		Graphics2D g = chartGraph.g;
		g.setFont( SUBTITLE_FONT );
		
		int lux = x_offset + CHART_LPADDING;
		int luy = y_offset + CHART_UPADDING;
		
		boolean gridX	= graphDef.getGridX();
		boolean gridY	= graphDef.getGridY();
		boolean minorX	= graphDef.getMinorGridX();
		boolean minorY	= graphDef.getMinorGridY();
		boolean majorX	= graphDef.getMajorGridX();
		boolean majorY	= graphDef.getMajorGridY();
			
		long start = graphDef.getStartTime();
		long secTime;
		float[] dashPattern = { 1, 1 };
		BasicStroke dStroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
								BasicStroke.JOIN_MITER, 10,
								dashPattern, 0);
		
		// First draw basic axis
		int tmpx = lux + chartWidth;
		int tmpy = luy + chartHeight;
		
		g.setColor( new Color(130,30,30) );
		g.drawLine( lux - 4, tmpy, tmpx + 4, tmpy );
		g.setColor( Color.RED );
		g.drawLine( tmpx + 4, tmpy - 3, tmpx + 4, tmpy + 3);
		g.drawLine( tmpx + 4, tmpy - 3, tmpx + 9, tmpy);
		g.drawLine( tmpx + 4, tmpy + 3, tmpx + 9, tmpy);
		
		if ( gridX )
		{
			int pixWidth = 0;
			if (vLabelCentered)
				pixWidth = (chartGraph.getX( vLabelGridWidth ) - chartGraph.getX( 0 ));
			
			for (int i = 0; i < timeList.length; i++)
			{
				secTime 	= timeList[i].timestamp / 1000;
				int posRel 	= chartGraph.getX(secTime);
				int pos 	= lux + posRel;
			
				if ( posRel >= 0 ) {
					if ( majorX && timeList[i].isLabel() )
					{
						g.setColor( new Color(130,30,30) );
						g.setStroke( dStroke );
						g.drawLine( pos, luy, pos, luy + chartHeight);
						g.setStroke( new BasicStroke() );
						g.drawLine( pos, luy - 2, pos, luy + 2);
						g.drawLine( pos, luy + chartHeight - 2, pos, luy + chartHeight + 2);
						// Only draw label itself if we are far enough from the side axis
						// Use extra 2 pixel padding (3 pixels from border total at least)
						int txtDistance = (timeList[i].text.length() * font_width) / 2;
						
						if ( vLabelCentered )
						{
							if ( pos + pixWidth <= lux + chartWidth ) {
								g.setColor( Color.BLACK );
								g.drawString( timeList[i].text, pos + 2 + pixWidth/2 - txtDistance, luy + chartHeight + font_height + LINE_PADDING );
							}
						}
						else if ( (pos - lux > txtDistance + 2) && (pos + txtDistance + 2 < lux + chartWidth) )	
						{ 
							g.setColor( Color.BLACK );
							g.drawString( timeList[i].text, pos - txtDistance, luy + chartHeight + font_height + LINE_PADDING );
						}
					}
					else if ( minorX )
					{	
						g.setColor( new Color(140,140,140) );
						g.setStroke( dStroke );
						g.drawLine( pos, luy, pos, luy + chartHeight);
						g.setStroke( new BasicStroke() );
						g.drawLine( pos, luy - 1, pos, luy + 1);
						g.drawLine( pos, luy + chartHeight - 1, pos, luy + chartHeight + 1);
					
					}
				}
			}
		}
		
		if ( gridY )
		{
			for (int i = 0; i < valueList.length; i++)
			{
				int valRel = chartGraph.getY( valueList[i].value );
			
				if ( majorY && valueList[i].isLabel() )
				{
					g.setColor( new Color(130,30,30) );
					g.setStroke( dStroke );
					g.drawLine( graphOriginX, graphOriginY - valRel, graphOriginX + chartWidth, graphOriginY - valRel );
					g.setStroke( new BasicStroke() );
					g.drawLine( graphOriginX - 2, graphOriginY - valRel, graphOriginX + 2, graphOriginY - valRel);
					g.drawLine( graphOriginX + chartWidth - 2, graphOriginY - valRel, graphOriginX + chartWidth + 2, graphOriginY - valRel );
					g.setColor( Color.BLACK );
					g.drawString( valueList[i].text, graphOriginX - (valueList[i].text.length() * font_width) - 7, graphOriginY - valRel + font_height/2 - 1 );
				}
				else if ( minorY )
				{
					g.setColor( new Color(140,140,140) );
					g.setStroke( dStroke );
					g.drawLine( graphOriginX, graphOriginY - valRel, graphOriginX + chartWidth, graphOriginY - valRel );
					g.setStroke( new BasicStroke() );
					g.drawLine( graphOriginX - 1, graphOriginY - valRel, graphOriginX + 1, graphOriginY - valRel);
					g.drawLine( graphOriginX + chartWidth - 1, graphOriginY - valRel, graphOriginX + chartWidth + 1, graphOriginY - valRel );
				}
	
			}
		}
	}
	
	/**
	 * Plots the comments on the image
	 * @param g
	 */
	private void plotComments( Graphics2D g ) throws RrdException
	{
		int posy		= y_offset + chartHeight + CHART_UPADDING + CHART_BPADDING + font_height;
		int posx		= LBORDER_SPACE;
			
		Comment[] cl	= graphDef.getComments();

		g.setColor( Color.BLACK );
		g.setFont( SUBTITLE_FONT );
	
		for (int i = 0; i < cl.length; i++)
		{
			String comment 	= cl[i].getMessage();
			String str;
		
			// If legend, draw color rectangle
			if ( cl[i].isLegend() )
			{
				g.setColor( ((Legend) cl[i]).getColor() );
				g.fillRect( posx, posy - 9, 10, 10 );
				g.setColor( Color.BLACK );
				g.drawRect( posx, posy - 9, 10, 10 );
				g.setColor( Color.BLACK );
				posx += 10 + (3*font_width - 10);		
			}

			int lf = comment.indexOf("\n");
			while ( lf >= 0 )
			{
				str 	= comment.substring(0, lf).replaceAll("\n", "");
				comment = comment.substring(lf + 1);
			
				g.drawString(str, posx, posy);
			
				posy 	+= font_height + LINE_PADDING;
				posx 	= LBORDER_SPACE;
				lf 		= comment.indexOf("\n");
			}
		
			if ( comment.length() > 0 )
			{
				switch ( cl[i].getAlign() )
				{
					case Comment.ALIGN_CENTER:
						posx = imgWidth / 2 - (comment.length()*font_width)/2; 
						g.drawString(comment, posx, posy);
						posy += font_height + LINE_PADDING;
						posx = LBORDER_SPACE;
						break;
					
					case Comment.ALIGN_LEFT:
						posx = LBORDER_SPACE; 
						g.drawString(comment, posx, posy);
						posy += font_height + LINE_PADDING;
						posx = LBORDER_SPACE;
						break;
				
					case Comment.ALIGN_RIGHT:
						posx = imgWidth - comment.length()*font_width - RBORDER_SPACE; 
						g.drawString(comment, posx, posy);
						posy += font_height + LINE_PADDING;
						posx = LBORDER_SPACE;
						break;
				
					default:
						comment = comment + SPACER;
						g.drawString(comment, posx, posy);
						posx += font_width * comment.length();
				}
			}
			
		}
	}
	
	/**
	 * Plots the graph itself
	 * @param d
	 */
	private void plotChart( Graphics2D graphics )
	{
		int lux		= x_offset + CHART_LPADDING;
		int luy		= y_offset + CHART_UPADDING;
		
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawRect( lux, luy, chartWidth, chartHeight);
								
		double val;
		double[] tmpSeries = new double[numPoints];
	
		PlotDef[] plotDefs 	= graphDef.getPlotDefs();
	
		for (int i = 0; i < plotDefs.length; i++)
		{
			Double[] values = (Double[]) plotDefs[i].getSource().getSeries().values.toArray( new Double[0] );
		
			for (int j = 0; j < values.length; j++)
			{
				val = values[j].doubleValue();
				if ( plotDefs[i].plotType == PlotDef.PLOT_STACK )
					val += tmpSeries[j];
							
				if ( val < lowerValue ) lowerValue = val;
				if ( val > upperValue ) upperValue = val;

				tmpSeries[j] = val;
			}
		}
	
		// Use a special graph 'object' that takes care of resizing and 
		// reversing y coordinates
		ChartGraphics g 	= new ChartGraphics( graphics );
		g.setMeasurements( chartWidth, chartHeight );
		g.setXRange( graphDef.getStartTime(), graphDef.getEndTime() );
		
		ValueMarker[] vlist = calculateValueMarkers();
		TimeMarker[] tlist	= calculateTimeMarkers();
		
		// Upper and lower were set in value markers
		double diff = 1.0d;
		if ( lowerValue < 0 )
			diff = 1.0d - ( lowerValue / ( -upperValue + lowerValue ));
		graphOriginX = lux;
		graphOriginY = new Double(luy + chartHeight*diff).intValue();
	
		g.setYRange( lowerValue, upperValue );

		if ( !graphDef.getFrontGrid() ) plotChartGrid( g, tlist, vlist );
	
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
	
		// Prepare clipping area and origin
		graphics.setClip( lux, luy, chartWidth, chartHeight);
		graphics.translate( graphOriginX, graphOriginY );
		 
		int lastPlotType 	= PlotDef.PLOT_LINE;
		int[] parentSeries 	= new int[numPoints];
	
		for (int i = 0; i < plotDefs.length; i++)
		{
			Source source = plotDefs[i].getSource();
					
			g.setColor( plotDefs[i].getColor() );
						
			switch ( plotDefs[i].getType() )
			{
				case PlotDef.PLOT_LINE:
					graphics.setStroke( new BasicStroke(plotDefs[i].getLineWidth()) );
					drawLine( g, parentSeries, source, false );
					lastPlotType = PlotDef.PLOT_LINE;
					graphics.setStroke( new BasicStroke() );
					break;
				case PlotDef.PLOT_AREA:
					drawArea( g, parentSeries, source, false );
					lastPlotType = PlotDef.PLOT_AREA;
					break;
				case PlotDef.PLOT_STACK:
					if ( lastPlotType == PlotDef.PLOT_AREA )
						drawArea( g, parentSeries, source, true );
					else
						drawLine( g, parentSeries, source, true );
					break;
			}
		}
	
		// Reset clipping area and origin
		graphics.translate( -graphOriginX, -graphOriginY );
		graphics.setClip( 0, 0, imgWidth, imgHeight);
		graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		
		if ( graphDef.getFrontGrid() ) plotChartGrid( g, tlist, vlist );
	}
	
	/**
	 * 
	 * @param g
	 * @param p
	 * @param s
	 */
	private void drawLine( ChartGraphics g, int[] p, Source s, boolean stack )
	{
		int ax = 0, ay = 0;
		int nx = 0, ny = 0, last = -1;
		
		RrdSecond[] times 	= (RrdSecond[]) s.getSeries().times.toArray( new RrdSecond[0] );
		Double[] values 	= (Double[]) s.getSeries().values.toArray( new Double[0] );
		
		for (int i = 0; i < times.length; i++)
		{
			nx = g.getX( times[i].timestamp );
			ny = g.getY( values[i].doubleValue() );
			if ( stack )
				ny += p[i];
		
			if ( ax != 0 && ay != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
				g.drawLine( ax, ay, nx, ny );
		
			p[i] 	= ny;
			ax 		= nx;
			ay 		= ny;
		}		
	}

	/**
	 * 
	 * @param g
	 * @param p
	 * @param s
	 */
	private void drawArea( ChartGraphics g, int[] p, Source s, boolean stack )
	{
		int ax = 0, ay = 0, py;
		int nx = 0, ny = 0, last = -1;

		RrdSecond[] times 	= (RrdSecond[]) s.getSeries().times.toArray( new RrdSecond[0] );
		Double[] values 	= (Double[]) s.getSeries().values.toArray( new Double[0] );
	
		for (int i = 0; i < times.length; i++)
		{
			py = 0;
		
			nx = g.getX( times[i].timestamp );
			ny = g.getY( values[i].doubleValue() );
			if ( stack )
			{
				py 	= p[i];
				ny += p[i];
			}
		
			if ( ax != 0 && py != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
				g.drawLine( nx, py, nx, ny );
		
			p[i]	= ny;
			ax 		= nx;
			ay 		= ny;
		}		
	}
		
	/**
	 * Gets all series of datapoints
	 */
	private void calculateSeries() throws RrdException, IOException 
	{
		Source[] sources 	= graphDef.getSources();
		long startTime 		= graphDef.getStartTime();
		long endTime 		= graphDef.getEndTime();
	
		if(endTime - startTime + 1 < numPoints)
			numPoints = (int)(endTime - startTime + 1);
	
		for(int i = 0; i < sources.length; i++)
			sources[i].setIntervalInternal(startTime, endTime);
	
		for (int i = 0; i < numPoints; i++) 
		{
			long t = (long)(startTime + i * ((endTime - startTime) / (double)(numPoints - 1)));
			ValueCollection valueCollection = new ValueCollection();
			for (int j = 0; j < sources.length; j++)
				sources[j].getValueInternal(t, valueCollection);
		}
	}
	
	/**
	 * Plots the labels for the vertical axis.
	 * @param g
	 */
	private void plotVerticalAxisLabels( Graphics2D g )
	{
		g.setColor( Color.BLACK );
		String valueAxisLabel 	= graphDef.getValueAxisLabel();
		if ( valueAxisLabel != null )
		{
			int labelWidth			= valueAxisLabel.length() * font_width;

			g.setFont( SUBTITLE_FONT );
			g.rotate( -Math.PI/2.0 );
			g.drawString( valueAxisLabel, - y_offset - CHART_UPADDING
											- chartHeight / 2 
											- labelWidth / 2,
											LBORDER_SPACE + font_height
											);
			g.rotate( Math.PI/2.0 );
		}
	}
	
	/**
	 * Plots the chart title in the correct font
	 * @param g
	 */
	private void plotImageTitle( Graphics2D g )
	{
		String title 	= graphDef.getTitle();
		if ( title != null )
		{
			int tf_height	= TITLE_FONT.getSize();
			int tf_width	= tf_height / 2 + 1;
			int titleWidth 	= title.length() * tf_width;
			
			// Title goes in the middle of the graph, in black
			g.setColor( Color.BLACK );
			g.setFont( TITLE_FONT );
			g.drawString( title, imgWidth / 2 - titleWidth / 2, tf_height + UBORDER_SPACE );
		}
	}
	
	/**
	 * Draws the image background, title and value axis label.
	 * @param g
	 */
	private void plotImageBackground( Graphics2D g )
	{
		// Background
		g.setColor( graphDef.getBackColor() );
		g.fillRect(0, 0, imgWidth, imgHeight );
	
		// Border
		Color bc 		= graphDef.getImageBorderColor();
		BasicStroke bs	= graphDef.getImageBorderStroke();
		
		if ( bc != null && bs != null )
		{
			g.setColor( bc );
			g.setStroke( bs );
			int w = new Float(bs.getLineWidth()).intValue();
			if ( w > 0 ) g.drawRect( w / 2, w / 2, imgWidth - w, imgHeight - w);
			g.setStroke( new BasicStroke() );
		}
		else
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
		plotVerticalAxisLabels( g );
	}
		
	/**
	 * Runs through all comments and determines number of lines all comments take.
	 * Current method is not that good in detecting number of lines, could be mucht better.
	 * Not more efficient, but producing far better results with complex graph scripts.
	 */
	private int calculateCommentBlock() throws RrdException
	{
		String line, newLine;
		Comment[] list 		= graphDef.getComments();
	
		int commentLines 	= (list.length > 0 ? 1 : 0);
	
		for (int i = 0; i < list.length; i++)
		{
			line 		= list[i].comment;
			int lfpos 	= line.indexOf("\n");
		
			while ( lfpos >= 0 )
			{
				commentLines++;
			
				line	= line.substring(lfpos + 1);
				lfpos 	= line.indexOf("\n");
			}
		
			// Check alignment, it adds an extra linefeed
			if ( list[i].isAlignSet() && i < list.length - 1 )
				commentLines++;
		}
		
		return commentLines * (font_height + LINE_PADDING) - LINE_PADDING;
	}
	
	/*
	private void calculateSeries() throws RrdException, IOException {
		Source[] sources = graphDef.getSources();
		long startTime = graphDef.getStartTime();
		long endTime = graphDef.getEndTime();
		if(endTime - startTime + 1 < numPoints) {
			numPoints = (int)(endTime - startTime + 1);
		}
		for(int i = 0; i < sources.length; i++) {
			sources[i].setIntervalInternal(startTime, endTime);
		}
		for(int i = 0; i < numPoints; i++) {
            long t = (long)(startTime + i * ((endTime - startTime) / (double)(numPoints - 1)));
			ValueCollection valueCollection = new ValueCollection();
			for(int j = 0; j < sources.length; j++) {
				sources[j].getValueInternal(t, valueCollection);
			}
		}
	}

	private DateTickUnit calculateDateTickUnit() {
		SimpleDateFormat simpleDateFormat = graphDef.getTimeFormat();
		if(simpleDateFormat != null) {
			// format specified
			int unit = graphDef.getTimeUnit();
			int unitCount = graphDef.getTimeUnitCount();
			return new DateTickUnit(unit, unitCount, simpleDateFormat);
		}
		// else
		long startTime = graphDef.getStartTime();
		long endTime = graphDef.getEndTime();
		double days = (endTime - startTime) / 86400.0;
		if(days <= 2.0 / 24.0) {
			// less than two hours
			return new DateTickUnit(DateTickUnit.MINUTE, 10, new SimpleDateFormat("HH:mm"));
		}
		else if(days <= 6.0 / 24.0) {
			return new DateTickUnit(DateTickUnit.MINUTE, 30, new SimpleDateFormat("HH:mm"));
		}
		else if(days <= 16.0 / 24.0) {
			return new DateTickUnit(DateTickUnit.HOUR, 1, new SimpleDateFormat("HH:mm"));
		}
		else if(days <= 1) {
			return new DateTickUnit(DateTickUnit.HOUR, 2, new SimpleDateFormat("HH:00"));
		}
		else if(days <= 2) {
			return new DateTickUnit(DateTickUnit.HOUR, 4, new SimpleDateFormat("HH:00"));
		}
		else if(days <= 3) {
			return new DateTickUnit(DateTickUnit.HOUR, 6, new SimpleDateFormat("HH:00"));
		}
		else if(days <= 5) {
			return new DateTickUnit(DateTickUnit.HOUR, 12, new SimpleDateFormat("EEE HH'h'"));
		}
		else if(days <= 8) {
			return new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("EEE dd"));
		}
		else if(days <= 32) {
			return new DateTickUnit(DateTickUnit.DAY, 1, new SimpleDateFormat("dd"));
		}
		else if(days <= 63) {
			return new DateTickUnit(DateTickUnit.DAY, 2, new SimpleDateFormat("dd"));
		}
		else if(days <= 120) {
			return new DateTickUnit(DateTickUnit.DAY, 4, new SimpleDateFormat("dd"));
		}
		else if(days <= 365 * 2) {
			return new DateTickUnit(DateTickUnit.MONTH, 1, new SimpleDateFormat("MMM"));
		}
		else if(days <= 365 * 4) {
			return new DateTickUnit(DateTickUnit.MONTH, 2, new SimpleDateFormat("MMM"));
		}
		else if(days <= 365 * 8) {
			return new DateTickUnit(DateTickUnit.MONTH, 4, new SimpleDateFormat("MMM"));
		}
		else {
			return new DateTickUnit(DateTickUnit.YEAR, 1, new SimpleDateFormat("YYYY"));
		}
	}

	private ValueAxis createValueAxis() {
		NumberAxis axis;
		if(graphDef.isLogarithmic()) {
			axis = new VerticalLogarithmicAxis(graphDef.getValueAxisLabel());
		}
		else {
			axis = new VerticalNumberAxis(graphDef.getValueAxisLabel());
		}
		Range valueRange = graphDef.getValueRange();
		if(valueRange != null) {
			axis.setRange(valueRange);
		}
		double valueStep = graphDef.getValueStep();
		if(valueStep > 0) {
			TickUnits units = new TickUnits();
			units.add(new NumberTickUnit(valueStep));
			axis.setStandardTickUnits(units);
		}
		axis.setNumberFormatOverride(new VerticalAxisFormat());
		return axis;
	}
	*/
	
	/**
	 * 
	 * @return List of timemarkers to plot
	 */
	private TimeMarker[] calculateTimeMarkers()
	{
		TimeAxisUnit t = null;
	
		SimpleDateFormat simpleDateFormat = graphDef.getTimeFormat();
		if(simpleDateFormat != null) {
			// format specified
			int unit = graphDef.getTimeUnit();
			int unitCount = graphDef.getTimeUnitCount();
			System.out.println( unit + ":" + unitCount + ":" + simpleDateFormat);
			//return new DateTickUnit(unit, unitCount, simpleDateFormat);
		}
		// else
		long startTime = graphDef.getStartTime();
		long endTime = graphDef.getEndTime();
		double days = (endTime - startTime) / 86400.0;
		
		vLabelCentered = false;
		if (days <= 2.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.MINUTE, 5, TimeAxisUnit.MINUTE, 10, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 3.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.MINUTE, 5, TimeAxisUnit.MINUTE, 20, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 5.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.MINUTE, 10, TimeAxisUnit.MINUTE, 30, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 10.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.MINUTE, 15, TimeAxisUnit.HOUR, 1, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 15.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.MINUTE, 30, TimeAxisUnit.HOUR, 2, new SimpleDateFormat("HH:mm"));
			//t = new TimeAxisUnit( TimeAxisUnit.HOUR, 2, TimeAxisUnit.HOUR, 6, new SimpleDateFormat("HH:mm"));
		}
		else if(days <= 20.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 1, TimeAxisUnit.HOUR, 1, new SimpleDateFormat("HH"));
			vLabelCentered = true;
		}
		else if(days <= 36.0 / 24.0) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 1, TimeAxisUnit.HOUR, 4, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 2) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 2, TimeAxisUnit.HOUR, 6, new SimpleDateFormat("HH:mm"));
		}
		else if (days <= 3) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 3, TimeAxisUnit.HOUR, 12, new SimpleDateFormat("HH:mm"));
		}
		else if(days <= 7) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 6, TimeAxisUnit.DAY, 1, new SimpleDateFormat("EEE dd"));
			vLabelCentered = true;
		}
		else if(days <= 14) {
			t = new TimeAxisUnit( TimeAxisUnit.HOUR, 12, TimeAxisUnit.DAY, 1, new SimpleDateFormat("dd"));
			vLabelCentered = true;
		}
		else if (days <= 43) {
			t = new TimeAxisUnit( TimeAxisUnit.DAY, 1, TimeAxisUnit.WEEK, 1, new SimpleDateFormat("'week' ww"));
			vLabelCentered = true;
		}
		else if(days <= 157) {
			t = new TimeAxisUnit( TimeAxisUnit.WEEK, 1, TimeAxisUnit.WEEK, 1, new SimpleDateFormat("ww"));
			vLabelCentered	= true;			
		}
		else {
			t = new TimeAxisUnit( TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, new SimpleDateFormat("MMM"));
			vLabelCentered 	= true;
		}
		
		vLabelGridWidth	= t.getMajorGridWidth();
			
		return t.getTimeMarkers( graphDef.getStartTime(), graphDef.getEndTime() );
	}
	
	/**
	 * 
	 * @return List of value markers to plot
	 */
	private ValueMarker[] calculateValueMarkers() 
	{
		ValueAxisUnit v 		= null;
		boolean lowerFromRange 	= false;
		boolean upperFromRange	= false;
		boolean rigid			= graphDef.getRigidGrid();	
		
		// Exceptional case
		if ( upperValue == 0 && upperValue == lowerValue )
			 upperValue	= 0.9;
				
		Range vr		= graphDef.getValueRange();
		if ( vr != null )
		{
			double rLower = vr.getLowerValue();
			if ( !Double.isNaN(rLower) && (rigid || rLower < lowerValue) ) {
				lowerValue 		= rLower;
				lowerFromRange	= true; 
			}
			double rUpper = vr.getUpperValue();
			if ( !Double.isNaN(rUpper) && (rigid || rUpper > upperValue) ) {
				upperValue 		= rUpper;
				upperFromRange	= true;
			}
		}
				
		double shifted 	= ( Math.abs(upperValue) > Math.abs(lowerValue) ? Math.abs(upperValue) : Math.abs(lowerValue) );
		double mod		= 1.0;
		while ( shifted > 10 ) {
			shifted /= 10;
			mod		*= 10;
		}
		while ( shifted < 1 ) {
			shifted *= 10;
			mod		/= 10;
		}
		
		if ( shifted <= 3 )
			v = new ValueAxisUnit( 1, 0.2*mod, 1, 1.0*mod );
		else if ( shifted <= 5 )
			v = new ValueAxisUnit( 1, 0.5*mod, 1, 1.0*mod );
		else if ( shifted <= 9 )
			v = new ValueAxisUnit( 1, 0.5*mod, 1, 2.0*mod );
		else
			v = new ValueAxisUnit( 1, 1.0*mod, 1, 5.0*mod );
		
		if ( !upperFromRange ) upperValue = v.getNiceHigher( upperValue );
		if ( !lowerFromRange ) lowerValue = v.getNiceLower( lowerValue );
				
		return v.getValueMarkers( lowerValue, upperValue );
	}
}
