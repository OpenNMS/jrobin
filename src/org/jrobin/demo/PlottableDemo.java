package org.jrobin.demo;

/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
 
import org.jrobin.graph.*;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

class PlottableDemo {
	static final double[] SF_DOWNLOAD_COUNT = {
		0, 0, 13, 34, 76, 72, 255, 144, 135, 194, 358, 304, 293
	};
	static final double[] SF_PAGE_HITS = {
		0, 1072, 517, 979, 2132, 2532, 5515, 3519, 3500, 4942, 7858, 7797, 6570
	};
	static final GregorianCalendar[] SF_TIMESTAMPS = 
		new GregorianCalendar[SF_DOWNLOAD_COUNT.length];
	static final Date SF_START_DATE = 
		new GregorianCalendar(2003, 4, 1).getTime(); // May 1st 2004.

	static {
		for(int i = 0; i < SF_TIMESTAMPS.length; i++) {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(SF_START_DATE);
			gc.add(Calendar.MONTH, i);
			SF_TIMESTAMPS[i] = gc;
		}
	}

	private PlottableDemo() throws RrdException, IOException {
		createGraph1();
		createGraph2();
		createGraph3();
		createGraph4();
		createGraph5();
		createGraph6();
		createGraph7();
		createGraph8();
		createGraph9();
		createGraph10();
		createGraph11();
		createGraph12();
		createGraph13();
		createGraph14();
		createGraph15();
		createGraph16();
		createGraph17();
	}

	private void createGraph1() throws RrdException, IOException {
		final long t0 = new Date().getTime() / 1000L, dt = 86400L;
		final int n = 10;
		final long t1 = t0 + (n - 1) * dt;
		Plottable p = new Plottable() {
			public double getValue(long t) {
				double x = (t - t0) / (double) (t1 - t0);
				return Math.exp(-x * 2) * Math.cos(x * 7 * Math.PI);
			}
		};

		long t[] = new long[n];
		double x[] = new double[n];
		for (int i = 0; i < n; i++) {
			t[i] = t0 + i * dt;
			x[i] = p.getValue(t[i]);
		}
		LinearInterpolator i1 = new LinearInterpolator(t, x); // defaults to INTERPOLATE_LINEAR
		CubicSplineInterpolator i2 = new CubicSplineInterpolator(t, x);
		// graph definition
		RrdGraphDef gDef = new RrdGraphDef(t0, t1);
		gDef.setTitle("Plottable demonstration");
		gDef.setTimeAxisLabel("days of our lives");
		gDef.setVerticalLabel("inspiration");
		gDef.datasource("real", p);
		gDef.datasource("linear", i1);
		gDef.datasource("spline", i2);
		gDef.line("real", Color.BLUE, "Real values", 1);
		gDef.line("linear", Color.RED, "Linear interpolation", 1);
		gDef.line("spline", Color.MAGENTA, "Spline interpolation@r", 1);
		gDef.setTimeAxis(TimeAxisUnit.DAY, 1, TimeAxisUnit.DAY, 1, "dd", true);
		createGraph(gDef);
	}

	private void createGraph2() throws RrdException, IOException {
		GregorianCalendar[] timestamps = {
			new GregorianCalendar(2004, 2, 1, 0, 0, 0),
			new GregorianCalendar(2004, 2, 1, 2, 0, 0),
			new GregorianCalendar(2004, 2, 1, 7, 0, 0),
			new GregorianCalendar(2004, 2, 1, 14, 0, 0),
			new GregorianCalendar(2004, 2, 1, 17, 0, 0),
			new GregorianCalendar(2004, 2, 1, 19, 0, 0),
			new GregorianCalendar(2004, 2, 1, 23, 0, 0),
			new GregorianCalendar(2004, 2, 1, 24, 0, 0)
		};
		double[] values = {100, 250, 230, 370, 350, 300, 340, 350};
		LinearInterpolator linear = new LinearInterpolator(timestamps, values);
		linear.setInterpolationMethod(LinearInterpolator.INTERPOLATE_LEFT);
		CubicSplineInterpolator spline = new CubicSplineInterpolator(timestamps, values);
		RrdGraphDef gDef = new RrdGraphDef(timestamps[0], timestamps[timestamps.length - 1]);
		gDef.setTitle("Plottable demonstration");
		gDef.setTimeAxisLabel("time");
		gDef.setVerticalLabel("water level [inches]");
		gDef.datasource("linear", linear);
		gDef.datasource("spline", spline);
		gDef.area("spline", Color.ORANGE, "Spline interpolation");
		gDef.line("linear", Color.RED, "Linear inteprolation@r", 2);
		gDef.gprint("spline", "AVERAGE", "Average spline value: @0 inches@r");
		gDef.gprint("linear", "AVERAGE", "Average linear value: @0 inches@r");
		createGraph(gDef);
	}

	private void createGraph3() throws RrdException, IOException {
		LinearInterpolator linear = new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("JRobin page hits per month");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("page hits");
		gDef.datasource("linear", linear);
		gDef.area("linear", Color.GREEN, null);
		gDef.line("linear", Color.RED, "page hits@L", 2);
		gDef.vrule(new GregorianCalendar(2004, 0, 1), Color.BLUE, null, 3);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		gDef.comment("Data provided by SourceForge.net@r");
		createGraph(gDef);
	}

	private void createGraph4() throws RrdException, IOException {
		LinearInterpolator linear = new LinearInterpolator(SF_TIMESTAMPS, SF_DOWNLOAD_COUNT);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("JRobin download count per month");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("download count");
		gDef.datasource("linear", linear);
		gDef.area("linear", Color.GREEN, null);
		gDef.line("linear", Color.RED, "download count@L", 2);
		gDef.vrule(new GregorianCalendar(2004, 0, 1), Color.BLUE, null, 3);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		gDef.comment("Data provided by SourceForge.net@r");
		createGraph(gDef);
	}

	private void createGraph5() throws RrdException, IOException {
		LinearInterpolator hitsInterpolator =
				new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		LinearInterpolator downloadsInterpolator =
				new LinearInterpolator(SF_TIMESTAMPS, SF_DOWNLOAD_COUNT);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], 
			SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("JRobin statistics at SourceForge");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits/downloads");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("downloads", downloadsInterpolator);
		gDef.datasource("ratio", "downloads,0,EQ,UNKN,hits,downloads,/,IF");
		gDef.area("hits", Color.GREEN, null);
		gDef.line("hits", Color.RED, "page hits", 2);
		gDef.area("downloads", Color.MAGENTA, "downloads@L");
		gDef.vrule(new GregorianCalendar(2004, 0, 1), Color.BLUE, null, 3);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		gDef.gprint("ratio", "AVERAGE", "Average number of page hits per download: @0@r");
		gDef.comment("Data provided by SourceForge.net@r");
		createGraph(gDef);
	}

	private void createGraph6() throws RrdException, IOException {
		LinearInterpolator hitsInterpolator =
			new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		LinearInterpolator trendInterpolator = new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		trendInterpolator.setInterpolationMethod(LinearInterpolator.INTERPOLATE_REGRESSION);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trend report");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("trend", trendInterpolator);
		gDef.datasource("diff", "hits,trend,-");
		gDef.datasource("absdiff", "diff,ABS");
		gDef.area("trend", null, null);
		gDef.stack("diff", Color.YELLOW, "difference");
		gDef.line("hits", Color.RED, "real page hits");
		gDef.line("trend", Color.BLUE, "trend@L");
		gDef.gprint("absdiff", "AVERAGE", "Average difference: @0@r");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph7() throws RrdException, IOException {
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0],
			SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("hits2", "hits,1000,-");
		gDef.datasource("invisible", "hits2,0,GE,hits2,0,IF");
		gDef.datasource("margin", "hits,invisible,-");
		gDef.area("invisible", null, null);
		gDef.stack("margin", Color.YELLOW, "yellow margin");
		gDef.line("hits", Color.RED, "page hits", 3);
		gDef.line("hits", Color.WHITE, null, 1);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph8() throws RrdException, IOException {
		CubicSplineInterpolator hitsInterpolator =
			new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0],
			SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("avg", "hits", "AVERAGE");
		gDef.datasource("diff", "avg,hits,-");
		gDef.datasource("diffpos", "diff,0,GE,diff,0,IF");
		gDef.datasource("diffneg", "diff,0,LT,diff,0,IF");
		gDef.area("hits", null, null);
		gDef.stack("diffpos", Color.RED, "bad");
		gDef.stack("diffneg", Color.GREEN,  "good");
		gDef.line("hits", Color.BLUE, "hits", 3);
		gDef.line("hits", Color.WHITE, null, 1);
		gDef.line("avg", Color.MAGENTA, "average@L", 3);
		gDef.line("avg", Color.WHITE, null, 1);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		gDef.gprint("hits", "AVERAGE", "Average: @0@r");
		createGraph(gDef);
	}

	private void createGraph9() throws RrdException, IOException {
		GregorianCalendar[] times = { SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1] };
		double[] values = { SF_PAGE_HITS[0], SF_PAGE_HITS[SF_PAGE_HITS.length - 1] };
		LinearInterpolator trendLine = new LinearInterpolator(times, values);
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0],
			SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("trend", trendLine);
		gDef.datasource("diff", "trend,hits,-");
		gDef.area("hits", null, null);
		gDef.stack("diff", Color.YELLOW, "difference");
		gDef.line("hits", Color.BLUE, "hits");
		gDef.line("trend", Color.RED, "trend@L");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph10() throws RrdException, IOException {
		final int GRADIENT_STEPS = 30;
		final Color color1 = Color.RED, color2 = Color.YELLOW;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		for(int i = 0; i <= GRADIENT_STEPS; i++) {
			gDef.datasource("hits" + i, "hits," + i + ",*," + GRADIENT_STEPS + ",/");
		}
		for(int i = GRADIENT_STEPS; i >=0 ; i--) {
			Color c = interpolateColor(color1, color2, i / (double) GRADIENT_STEPS);
			gDef.area("hits" + i, c, null);
		}
		gDef.line("hits", Color.BLACK, "Number of page hits");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph11() throws RrdException, IOException {
		final int GRADIENT_STEPS = 30;
		final Color color1 = Color.RED, color2 = Color.YELLOW;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		for(int i = 0; i <= GRADIENT_STEPS; i++) {
			gDef.datasource("hits" + i, "hits," + i + ",*," + GRADIENT_STEPS + ",/");
		}
		for(int i = GRADIENT_STEPS; i >= 0 ; i--) {
			Color c = interpolateColor(color1, color2, i / (double) GRADIENT_STEPS);
			gDef.area("hits" + i, c, null);
		}
		gDef.line("hits", color2, "Estimated number of page hits");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		gDef.setCanvasColor(color1);
		createGraph(gDef);
	}

	private void createGraph12() throws RrdException, IOException {
		final int GRADIENT_STEPS = 30;
		final Color color1 = Color.YELLOW, color2 = Color.RED;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("top", "hits", "MAX");
		for(int i = 1; i <= GRADIENT_STEPS; i++) {
			gDef.datasource("hits" + i, "hits,top," + i + ",*," + GRADIENT_STEPS + ",/,MIN");
		}
		for(int i = GRADIENT_STEPS; i >= 1 ; i--) {
			Color c = i % 2 == 0? color1: color2;
			gDef.area("hits" + i, c, null);
		}
		gDef.line("hits", color2, "Estimated number of page hits");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph13() throws RrdException, IOException {
		final int GRADIENT_STEPS = 15;
		final Color color1 = Color.LIGHT_GRAY, color2 = Color.WHITE;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		for(int i = GRADIENT_STEPS; i >= 1 ; i--) {
			Color c = interpolateColor(color1, color2, i / (double) GRADIENT_STEPS);
			gDef.line("hits", c, null, i);
		}
		gDef.line("hits", color1, "Estimated number of page hits");
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph14() throws RrdException, IOException {
		final int GRADIENT_STEPS = 20;
		final double GRADIENT_WIDTH = 2000.0;
		final Color color1 = Color.RED, color2 = Color.WHITE;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		for(int i = 0; i <= GRADIENT_STEPS; i++) {
			gDef.datasource("hits" + i,
				"hits," + GRADIENT_WIDTH + "," + i + ",*," + GRADIENT_STEPS + ",/,-,0,MAX");
		}
		for(int i = 0; i <= GRADIENT_STEPS; i++) {
			gDef.area("hits" + i, interpolateColor(color1, color2, i / (double) GRADIENT_STEPS), null);
		}
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph15() throws RrdException, IOException {
		final int STEPS = 20;
		final Color color1 = Color.BLACK, color2 = Color.RED;
		CubicSplineInterpolator hitsInterpolator =
				new CubicSplineInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("maxhits", "hits", "MAX");
		for(int i = 1; i <= STEPS; i++) {
			gDef.datasource("hits" + i, "maxhits," + i + ",*," + STEPS + ",/,hits,GE,hits,0,IF");
		}
		for(int i = STEPS; i >= 1; i--) {
			gDef.area("hits" + i, interpolateColor(color1, color2, i / (double) STEPS), null);
		}
		gDef.line("hits", Color.BLUE, "page hits", 2);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph16() throws RrdException, IOException {
		final int STEPS = 20;
		final Color color1 = Color.BLACK, color2 = Color.RED;
		LinearInterpolator hitsInterpolator =
			new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		hitsInterpolator.setInterpolationMethod(LinearInterpolator.INTERPOLATE_LEFT);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("hits");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("maxhits", "hits", "MAX");
		for(int i = 1; i <= STEPS; i++) {
			gDef.datasource("hits" + i, "maxhits," + i + ",*," + STEPS + ",/,hits,GE,hits,0,IF");
		}
		for(int i = STEPS; i >= 1; i--) {
			gDef.area("hits" + i, interpolateColor(color1, color2, i / (double) STEPS), null);
		}
		gDef.line("hits", Color.BLUE, "page hits", 2);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

	private void createGraph17() throws RrdException, IOException {
		final int STEPS = 20;
		final Color color1 = Color.YELLOW, color2 = Color.RED;
		LinearInterpolator hitsInterpolator =
			new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		LinearInterpolator trendInterpolator = new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		trendInterpolator.setInterpolationMethod(LinearInterpolator.INTERPOLATE_REGRESSION);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
		gDef.setTitle("Trick graph");
		gDef.setTimeAxisLabel("month");
		gDef.setVerticalLabel("difference");
		gDef.datasource("hits", hitsInterpolator);
		gDef.datasource("trend", trendInterpolator);
		gDef.datasource("diff", "hits,trend,-");
		for(int i = 1; i <= STEPS; i++) {
			gDef.datasource("diff" + i, "diff," + i + ",*," + STEPS + ",/");
		}
		for(int i = STEPS; i >= 1; i--) {
			String ds = "diff" + i;
			Color c = interpolateColor(color1, color2, i / (double) STEPS);
			String legend = (i == 1)? "dissipation": null;
			gDef.area(ds, c, legend);
		}
		gDef.setCanvasColor(color2);
		gDef.setTimeAxis(TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, "MMM", false);
		createGraph(gDef);
	}

    private Color interpolateColor(Color c1, Color c2, double factor) {
		int r = c1.getRed() + (int)((c2.getRed() - c1.getRed()) * factor);
		int g = c1.getGreen() + (int)((c2.getGreen() - c1.getGreen()) * factor);
		int b = c1.getBlue() + (int)((c2.getBlue() - c1.getBlue()) * factor);
		return new Color(r, g, b);
	}

	private static int count;

	private static void createGraph(RrdGraphDef gDef) throws IOException, RrdException {
		RrdGraph graph = new RrdGraph(gDef);
		String filename = Util.getJRobinDemoPath("plottable" + (++count) + ".png");
		graph.saveAsPNG(filename, 800, 400);
		//graph.getExportData().exportXml( System.out );
		System.out.println("Saved to: " + filename);
	}

	public static void main(String[] args) throws RrdException, IOException {
		new PlottableDemo();
	}
}
