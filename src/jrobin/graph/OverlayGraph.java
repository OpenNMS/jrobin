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
import java.util.ArrayList;

/**
 *
 */
class OverlayGraph {
	// last plot defines grpah type
	private ArrayList plotDefs = new ArrayList();

	OverlayGraph() {
	}

	OverlayGraph(PlotDef plotDef) {
		plotDefs.add(plotDef);
	}

	void addPlotDef(PlotDef plotDef) throws RrdException {
		PlotDef parent = getLastPlotDef();
		plotDefs.add(plotDef);
		plotDef.stack(parent);
	}

	private PlotDef getLastPlotDef() {
		int count = plotDefs.size();
		if(count == 0) {
			return null;
		}
		return (PlotDef) plotDefs.get(count - 1);
	}

	/*
	XYPlot getXYPlot() throws RrdException {
		if(plotDefs.size() == 0) {
			throw new RrdException("Nothing to plot");
		}
		// create renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(AreaXYRenderer.LINES);
		if(shouldFill()) {
			renderer = new AreaXYRenderer(AreaXYRenderer.AREA);
		}
		// create TimeSeriesCollection
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		int count = plotDefs.size();
		for(int i = count - 1; i >= 0; i--) {
			PlotDef plotDef = (PlotDef) plotDefs.get(i);
			RrdTimeSeries series = plotDef.getSeries();
			series.fixNaNs(shouldFill()? new Double(0): null);
			dataset.addSeries(series);
			renderer.setSeriesPaint(count - 1 - i, plotDef.getColor());
			renderer.setSeriesStroke(count - 1 - i, new BasicStroke(plotDef.getLineWidth()));
		}
		XYPlot plot = new XYPlot(dataset, null, null, renderer);
		return plot;
	}
	*/
	
	private boolean shouldFill() {
		return plotDefs.get(0) instanceof Area;
	}
}
