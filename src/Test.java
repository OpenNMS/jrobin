

import jrobin.graph.RrdGraphDef;
import jrobin.graph.RrdGraph;
import jrobin.core.Util;
import jrobin.core.RrdException;

import java.io.IOException;
import java.awt.*;

class Test {
	static final String RRD_FILE = "S:/traffic_jrobin.rrd";
	static final String PNG = "S:/traffic_jrobin.png";
	public static void main(String[] args) throws RrdException, IOException {
		RrdGraphDef def = new RrdGraphDef();
		def.setTimePeriod(Util.getTimestamp(2003, 9, 28), Util.getTimestamp(2003, 9, 29));
        def.setVerticalLabel("link speed [bits/sec]");
        def.setTitle("Leased Line Daily Traffic");
        def.datasource("in", RRD_FILE, "input", "AVERAGE");
		def.datasource("out", RRD_FILE, "output", "AVERAGE");
        def.datasource("in8", "in,8,*");
		def.datasource("out8", "out,8,*");
		def.datasource("total", "in8,out8,+");
        def.datasource("totalneg", "total,-1,*");
        def.area("out8", Color.GREEN, "output traffic");
		def.line("in8", Color.BLUE, "input traffic");
		def.line("totalneg", Color.RED, "total traffic@r");
        def.gprint("in8", "AVERAGE", "avgIn=@2 @sbits/sec");
		def.gprint("in8", "MAX", "maxIn=@2 @sbits/sec@r");
		def.gprint("out8", "AVERAGE", "avgOut=@2 @sbits/sec");
		def.gprint("out8", "MAX", "maxOut=@2 @sbits/sec@r");
		def.gprint("total", "AVERAGE", "avgTotal=@2 @sbits/sec");
		def.gprint("total", "MAX", "maxTotal=@2 @sbits/sec@r");
		def.setAntiAliasing(false);
		RrdGraph g = new RrdGraph(def);
		g.saveAsPNG(PNG, 400, 200);
	}
}
