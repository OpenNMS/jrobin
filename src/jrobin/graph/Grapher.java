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

import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.Spacer;
import com.jrefinery.chart.StandardLegend;
import com.jrefinery.chart.TextTitle;
import com.jrefinery.chart.axis.*;
import com.jrefinery.chart.plot.OverlaidXYPlot;
import com.jrefinery.data.Range;
import jrobin.core.RrdException;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

class Grapher {
	static final int GRAPH_RESOLUTION = 1000;
	static final String SPACER = "   ";
	static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 12);
	static final Font SUBTITLE_FONT = new Font("SansSerif", Font.PLAIN, 10);
	static final Spacer SUBTITLE_SPACER =
		new Spacer(Spacer.ABSOLUTE, 5, 0, 5, 2);
	static final Color BACK_COLOR = new Color(240, 240, 240);
	static final int DEFAULT_ALIGN = TextTitle.LEFT;
	// make it harder to find :)
	static final String GRAPH_SIGNATURE = "c@trahCeerFJ & niboRJ htiw detaerC";
	private RrdGraphDef graphDef;
	private int numPoints = GRAPH_RESOLUTION;

	Grapher(RrdGraphDef graphDef) throws RrdException {
		this.graphDef = graphDef;
		StringBuffer buff = new StringBuffer(GRAPH_SIGNATURE);
		graphDef.comment(buff.reverse().toString());
	}

	JFreeChart createJFreeChart() throws RrdException, IOException {
		PlotDef[] plotDefs = graphDef.getPlotDefs();
		OverlayGraph[] graphs = graphDef.getGraphs();
		if(plotDefs.length == 0) {
			throw new RrdException("Nothing to plot");
		}
        calculateSeries();
		OverlaidXYPlot plot = new OverlaidXYPlot(createTimeAxis(), createValueAxis());
		for(int i = 0; i < graphs.length; i++) {
			plot.add(graphs[i].getXYPlot());
		}
		JFreeChart chart = new JFreeChart("", plot);
		chart.setTitle(new TextTitle(graphDef.getTitle(), TITLE_FONT));
		Color backColor = graphDef.getBackColor();
		if(backColor == null) {
			backColor = BACK_COLOR;
		}
		chart.setBackgroundPaint(backColor);
		StandardLegend legend = (StandardLegend) chart.getLegend();
		legend.setOutlinePaint(backColor);
		legend.setBackgroundPaint(backColor);
		addSubtitles(chart);
		return chart;
	}

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

	private ValueAxis createTimeAxis() {
		HorizontalDateAxis axis = new HorizontalDateAxis(graphDef.getTimeAxisLabel());
		axis.setLowerMargin(0.0);
		axis.setUpperMargin(0.0);
		axis.setTickUnit(calculateDateTickUnit());
		return axis;
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

	private void addSubtitles(JFreeChart chart) throws RrdException {
		String currentLine = "";
		ArrayList subtitles = new ArrayList();
		Comment[] comments = graphDef.getComments();
		int lastScaleIndex = ValueScaler.NO_SCALE;
		for(int i = 0; i < comments.length; i++) {
			if(currentLine.length() > 0) {
				currentLine += SPACER;
			}
			Comment comment = comments[i];
			// uniform scaling is now supported
			comment.setScaleIndex(lastScaleIndex);
			currentLine += comment.getMessage();
			lastScaleIndex = comment.getScaleIndex();
			if(comment.isAlignSet()) {
				// should finish current line
				int align = comment.getAlign();
				TextTitle subtitle = new TextTitle(currentLine, SUBTITLE_FONT, Color.BLACK,
					TextTitle.BOTTOM, align, TextTitle.DEFAULT_VERTICAL_ALIGNMENT, SUBTITLE_SPACER);
				subtitles.add(subtitle);
				currentLine = "";
			}
		}
		if(currentLine.length() > 0) {
			TextTitle subtitle = new TextTitle(currentLine, SUBTITLE_FONT,
				Color.BLACK, TextTitle.BOTTOM, DEFAULT_ALIGN,
				TextTitle.DEFAULT_VERTICAL_ALIGNMENT, SUBTITLE_SPACER);
			subtitles.add(subtitle);
		}
		for(int i = subtitles.size() - 1; i >= 0; i--) {
			TextTitle subtitle = (TextTitle) subtitles.get(i);
			chart.addSubtitle(subtitle);
		}
	}
}
