
import org.jrobin.graph.*;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;

class PlottableDemo {
	static double[] SF_DOWNLOAD_COUNT = {
		0, 0, 13, 34, 76, 72, 255, 144, 135, 194, 358, 304, 247
	};
	static double[] SF_PAGE_HITS = {
		0, 1072, 517, 979, 2132, 2532, 5515, 3519, 3500, 4942, 7858, 7797, 5509
	};
	static GregorianCalendar[] SF_TIMESTAMPS = new GregorianCalendar[SF_DOWNLOAD_COUNT.length];
	static Date SF_START_DATE = new GregorianCalendar(2003, 4, 1).getTime(); // May 1st 2004.

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
		RrdGraphDef gdef = new RrdGraphDef(t0, t1);
		gdef.setTitle("Plottable demonstration");
		gdef.setTimeAxisLabel("days of our lives");
		gdef.setVerticalLabel("inspiration");
		gdef.datasource("real", p);
		gdef.datasource("linear", i1);
		gdef.datasource("spline", i2);
		gdef.line("real", Color.BLUE, "Real values", 1);
		gdef.line("linear", Color.RED, "Linear interpolation", 1);
		gdef.line("spline", Color.MAGENTA, "Spline interpolation@r", 1);
		gdef.setTimeAxis(TimeAxisUnit.DAY, 1, TimeAxisUnit.DAY, 1, "dd", true);
		RrdGraph g = new RrdGraph(gdef);
		String filename = Util.getJRobinDemoPath("plottable1.png");
		g.saveAsPNG(filename, 400, 200);
		System.out.println("Graph1 saved to " + filename);
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
		RrdGraph graph = new RrdGraph(gDef);
		String filename = Util.getJRobinDemoPath("plottable2.png");
		graph.saveAsPNG(filename, 300, 100);
		System.out.println("Graph2 saved to " + filename);
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
		RrdGraph graph = new RrdGraph(gDef);
		String filename = Util.getJRobinDemoPath("plottable3.png");
		graph.saveAsPNG(filename, 400, 200);
		System.out.println("Graph3 saved to " + filename);
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
		RrdGraph graph = new RrdGraph(gDef);
		String filename = Util.getJRobinDemoPath("plottable4.png");
		graph.saveAsPNG(filename, 400, 200);
		System.out.println("Graph4 saved to " + filename);
	}

	private void createGraph5() throws RrdException, IOException {
		LinearInterpolator hitsInterpolator =
				new LinearInterpolator(SF_TIMESTAMPS, SF_PAGE_HITS);
		LinearInterpolator downloadsInterpolator =
				new LinearInterpolator(SF_TIMESTAMPS, SF_DOWNLOAD_COUNT);
		RrdGraphDef gDef = new RrdGraphDef(SF_TIMESTAMPS[0], SF_TIMESTAMPS[SF_TIMESTAMPS.length - 1]);
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
		RrdGraph graph = new RrdGraph(gDef);
		String filename = Util.getJRobinDemoPath("plottable5.png");
		graph.saveAsPNG(filename, 400, 200);
		System.out.println("Graph5 saved to " + filename);
	}

	public static void main(String[] args) throws RrdException, IOException {
		new PlottableDemo();
	}
}
