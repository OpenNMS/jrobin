package jrobin.demo;

import jrobin.graph.RrdGraphDef;
import jrobin.graph.RrdGraph;
import jrobin.core.RrdException;

import java.util.GregorianCalendar;
import java.awt.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Oct 16, 2003
 * Time: 9:17:24 AM
 * To change this template use Options | File Templates.
 */
public class JRobinGallery 
{
	public static void graph7() throws RrdException, IOException {
		RrdGraphDef def = new RrdGraphDef();
        GregorianCalendar start = new GregorianCalendar(2003, 3, 25);
		GregorianCalendar end = new GregorianCalendar(2003, 5, 5);
		//GregorianCalendar start = new GregorianCalendar(2003, 4, 1, 13, 00);
		//GregorianCalendar end = new GregorianCalendar(2003, 4, 1, 13, 50);
		def.setTimePeriod(start, end);
		long t0 = start.getTime().getTime() / 1000L;
		long t1 = end.getTime().getTime() / 1000L;
		def.datasource("sine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,7,PI,*,*,SIN");
		def.datasource("v2", "/gallery.rrd", "shade", "AVERAGE");
		def.datasource("cosine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,3,PI,*,*,COS");
		def.datasource("line", "TIME," + t0 + ",-," + (t1 - t0) + ",/,1000,*");
		def.datasource("v1", "sine,line,*,ABS");
		int n = 40;
		for(int i = 0; i < n; i++) {
			long t = t0 + (t1 - t0) * i / n;
			def.datasource("c" + i, "TIME," + t + ",GT,v2,UNKN,IF");
		}
		def.area( new GregorianCalendar( 2003, 4, 1 ), Double.MIN_VALUE, new GregorianCalendar( 2003, 5, 1 ), Double.MAX_VALUE, Color.ORANGE, "april 2003\n" );
		for(int i = 0; i < n; i++) {
            if(i==0) {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), "Output by night");
			}
			else if(i==n/2) {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), "Output by day");
			}
			else {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), null);
			}
		}
		def.line("v2", Color.YELLOW, null);
		def.line("v1", Color.BLUE, "Input voltage@L", 3);
		def.line("v1", Color.YELLOW, null, 1);
		def.setTitle("Voltage measurement");
		def.setVerticalLabel("[Volts]");
		//def.setValueAxisLabel("[Volts]");
		def.comment("fancy looking graphs@r");
		//def.setValueStep(100);
		RrdGraph graph = new RrdGraph(def);
		graph.saveAsPNG("/demo7.png", 400, 250);
		//graph.saveAsPNG("/demo7.png");
	}

	public static void graph6() throws RrdException, IOException {
		RrdGraphDef def = new RrdGraphDef();
        GregorianCalendar start = new GregorianCalendar(2003, 4, 1);
		GregorianCalendar end = new GregorianCalendar(2003, 4, 2);
		def.setTimePeriod(start, end);
		long t0 = start.getTime().getTime() / 1000L;
		long t1 = end.getTime().getTime() / 1000L;
		def.datasource("sine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,7,PI,*,*,SIN");
		def.datasource("cosine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,3,PI,*,*,COS");
		def.datasource("line", "TIME," + t0 + ",-," + (t1 - t0) + ",/,1000,*");
		def.datasource("v1", "sine,line,*");
		def.datasource("v2", "cosine,800,*");
		def.datasource("diff", "v2,v1,-");
		def.datasource("absdiff", "diff,ABS");
		def.datasource("blank1", "v1,0,GT,v2,0,GT,AND,v1,v2,MIN,0,IF");
		def.datasource("blank2", "v1,0,LT,v2,0,LT,AND,v1,v2,MAX,0,IF");
		def.datasource("median", "v1,v2,+,2,/");
		def.line("median", Color.YELLOW, null);
		def.area("v1", Color.WHITE, null);
		def.stack("diff", Color.YELLOW, "safe zone\n");
		def.area("blank1", Color.WHITE, null);
		def.area("blank2", Color.WHITE, null);
		def.line("v1", Color.BLUE, null, 1);
		def.line("v2", Color.BLUE, null, 1);
		def.setTitle("Voltage measurement");
		def.setVerticalLabel("[Volts]");
		//def.setValueAxisLabel("[Volts]");
		def.gprint("absdiff", "MAX", "max safe: @2V");
		def.gprint("absdiff", "AVERAGE", "avg safe: @2V@L");
		def.comment("fancy looking graph@r");
		RrdGraph graph = new RrdGraph(def);
		//graph.saveAsPNG("/demo6.png");
		graph.saveAsPNG("/demo6.png", 400, 250);
	}

	public static void main(String[] args) throws RrdException, IOException {
		graph6();
		graph7();
	}
}
