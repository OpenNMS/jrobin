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

import com.jrefinery.data.RegularTimePeriod;

import java.awt.*;

/**
 *
 */
class PlotDef {
	public static final float DEFAULT_LINE_WIDTH = 1.0F;

	protected Source source;
    protected Color color;
	protected String legend;
	protected PlotDef parent;
	private RrdTimeSeries totalSeries;
	private float lineWidth = DEFAULT_LINE_WIDTH;

	PlotDef(Source source, Color color, String legend) {
		this.source = source;
		this.color = color;
		this.legend = legend;
	}

	void stack(PlotDef parent) {
		this.parent = parent;
	}

	public Color getColor() {
		return color;
	}

	public String getLegend() {
		return legend;
	}

	public Source getSource() {
		return source;
	}

	public String getSourceName() {
		return source.getName();
	}

	public PlotDef getParent() {
        return parent;
	}

	RrdTimeSeries getSeries() {
		if(totalSeries == null) {
			RrdTimeSeries parentSeries = null;
			if(parent != null) {
				parentSeries = parent.getSeries();
			}
			totalSeries = new RrdTimeSeries(legend);
			RrdTimeSeries realSeries = source.getSeries();
			for(int i = 0; i < realSeries.getItemCount(); i++) {
				RegularTimePeriod t = realSeries.getTimePeriod(i);
				double realValue = realSeries.getValue(i).doubleValue();
				double parentValue = 0;
				if(parentSeries != null) {
					parentValue = parentSeries.getValue(i).doubleValue();
				}
				totalSeries.add(t, realValue + parentValue);
			}
		}
		return totalSeries;
	}

	void setLineWidth(float lineWidth) {
		this.lineWidth = lineWidth;
	}

	float getLineWidth() {
		return lineWidth;
	}

}
