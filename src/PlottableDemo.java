
import org.jrobin.graph.*;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

class PlottableDemo {
	private PlottableDemo() throws RrdException, IOException {
		createGraph1();
		createGraph2();
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

	public static void main(String[] args) throws RrdException, IOException {
		new PlottableDemo();
	}
}
