import org.jrobin.graph.*;
import org.jrobin.core.RrdException;
import java.awt.*;
import java.io.IOException;
import java.util.GregorianCalendar;

class PlottableDemo {
	public static void main(String[] args) throws RrdException, IOException {
		// create plottable datasource
		GregorianCalendar[] gc = {
			new GregorianCalendar(2004, 0, 1),
			new GregorianCalendar(2004, 0, 4),
			new GregorianCalendar(2004, 0, 6),
			new GregorianCalendar(2004, 0, 12),
			new GregorianCalendar(2004, 0, 18),
			new GregorianCalendar(2004, 0, 19),
			new GregorianCalendar(2004, 0, 21)
		};
		double values[] = {
			1.2, 3.4, 2.7, 3.0, 1.1, 1.2, 1.6
		};
		// four different imterpolation methods
		LinearInterpolator i1 = new LinearInterpolator(gc, values); // defaults to INTERPOLATE_LINEAR
		LinearInterpolator i2 = new LinearInterpolator(gc, values);
		i2.setInterpolationMethod(LinearInterpolator.INTERPOLATE_LEFT);
		LinearInterpolator i3 = new LinearInterpolator(gc, values);
		i3.setInterpolationMethod(LinearInterpolator.INTERPOLATE_RIGHT);
		CubicSplineInterpolator i4 = new CubicSplineInterpolator(gc, values);
		// graph definition
		RrdGraphDef gdef = new RrdGraphDef(gc[0], gc[gc.length - 1]);
		gdef.setTitle("Plottable demonstration");
		gdef.setTimeAxisLabel("days of our lives");
		gdef.setVerticalLabel("inspiration");
		gdef.datasource("linear", i1);
		gdef.datasource("left", i2);
		gdef.datasource("right", i3);
		gdef.datasource("spline", i4);
		gdef.area("linear", Color.RED, "Linear");
		gdef.line("left", Color.BLUE, "Left", 2);
		gdef.line("right", Color.GREEN, "Right@L", 2);
		gdef.line("spline", Color.MAGENTA, "Spline@R", 2);
		gdef.setTimeAxis(TimeAxisUnit.DAY, 1, TimeAxisUnit.DAY, 1, "dd", true);
		RrdGraph g = new RrdGraph(gdef);
		g.saveAsPNG("plottable.png", 500, 250);
	}
}
