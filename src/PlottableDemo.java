import org.jrobin.graph.*;
import org.jrobin.core.RrdException;
import java.awt.*;
import java.io.IOException;
import java.util.Date;

class PlottableDemo {
	public static void main(String[] args) throws RrdException, IOException {
		final long t0 = new Date().getTime() / 1000L, dt = 86400L;
		final int n = 10;
		final long t1 = t0 + (n - 1) * dt;
		Plottable p = new Plottable() {
			public double getValue(long t) {
				double x = (t - t0) / (double)(t1 - t0);
				return Math.exp(-x * 2) * Math.cos(x * 7 * Math.PI);
			}
		};

		long t[] = new long[n];
		double x[] = new double[n];
		for(int i = 0; i < n; i++) {
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
		g.saveAsPNG("plottable2.png", 400, 200);
	}
}
