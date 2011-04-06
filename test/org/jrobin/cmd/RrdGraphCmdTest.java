package org.jrobin.cmd;

import java.io.IOException;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.junit.Before;
import org.junit.Test;

public class RrdGraphCmdTest {

	RrdGraphCmd graphCmd;
	String jrbFileName;

	@Before
	public void setUp() throws RrdException, IOException {
		// Don't use stdout; this silences output if we're outputting to "-"
		// If debugging, you may wish to turn this to true, in case there's
		// output that is helpful in figuring
		// But, note that then you'll get the GIF file printed to stdout as well
		RrdToolCmd.setStandardOutUsed(false);

		// Create a simple JRB file called test-graph-cmd with an RRA called
		// "testvalue" (AVERAGE). Enough to generate graphs using RrdGraphCmd.
		// Note we're not testing the graphing itself, just the parsing
		this.jrbFileName = "target/test-graph-cmd.jrb";
		RrdDef def = new RrdDef(this.jrbFileName);
		def.setStartTime(1000);
		def.setStep(1);
		def.addDatasource("testvalue", "GAUGE", 2, Double.NaN, Double.NaN);
		def.addArchive("RRA:AVERAGE:0.5:1:100");
		RrdDb rrd = new RrdDb(def);
		rrd.close();

		this.graphCmd = new RrdGraphCmd();
		
	}

	@Test
	/*1
	 * There was a null pointer exception lurking in the code that parses the
	 * AREA statement, if there is no legend.
	 */
	public void testBasicGraph() throws RrdException, IOException, RrdException {
		this.graphCmd.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE AREA:testvalue#FF0000:TestValue");
	}

	@Test
	/*
	 * There was a null pointer exception lurking in the code that parses the
	 * AREA statement, if there was no legend.
	 */
	public void testAreaWithoutLegend() throws RrdException, IOException,
			RrdException {
		// The command we execute is necessarily quite short, but should still
		// be generally parseable
		// "-" means in memory (not a file). And the AREA just specifies a
		// source value and colour, but no label
		this.graphCmd
				.executeCommand("graph - DEF:testvalue=" + this.jrbFileName
						+ ":testvalue:AVERAGE AREA:testvalue#FF0000");
	}

	@Test
	public void testLine1() throws RrdException, IOException, RrdException {
		this.graphCmd.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");
	}
	
	@Test
	public void testLine2() throws RrdException, IOException, RrdException {
	
		this.graphCmd.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE2:testvalue#FF0000");
	}
	
	@Test
	public void testLine31() throws RrdException, IOException, RrdException {
		this.graphCmd.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE3:testvalue#FF0000");
	}
	
	@Test
	public void testLine3WithLegend() throws RrdException, IOException, RrdException {
	this.graphCmd.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE3:testvalue#FF0000:Legend");

	}

	@Test
	public void testStack() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE LINE1:testvalue#FF0000 STACK:testvalue#FF0000");
	}
	
	@Test
	public void testStackWithLegend() throws RrdException, IOException, RrdException {
	
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE LINE1:testvalue#FF0000 STACK:testvalue#FF0000:Legend");

	}

	@Test
	public void testCDEF() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE CDEF:testvalue8=testvalue,8,* LINE1:testvalue8#FF0000");
	}

	@Test
	public void testPrint() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE PRINT:testvalue:MAX:%f LINE1:testvalue#FF0000");
	}

	@Test
	public void testGPrint() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE GPRINT:testvalue:MAX:%f LINE1:testvalue#FF0000");
	}

	@Test
	public void testComment() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE COMMENT:FOOOOOOOOD LINE1:testvalue#FF0000");
	}

	@Test
	public void testHRule() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE HRULE:1#FF0000 LINE1:testvalue#FF0000");
	}
	public void testHRuleWithLegent() throws RrdException, IOException, RrdException {
		this.graphCmd
		.executeCommand("graph - DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE HRULE:1#FF0000:Legend LINE1:testvalue#FF0000");

	}

	@Test
	public void testVRule() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE VRULE:1#FF0000 LINE1:testvalue#FF0000");
	}

	@Test
	public void testColorDefinition() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - --color grid#00FF00 DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");
	}
	
	@Test
	public void testYGrid() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - --y-grid 5:2 DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");
		
	}
	
	@Test
	public void testYGridNone() throws RrdException, IOException, RrdException {
		this.graphCmd
		.executeCommand("graph - --y-grid none DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");

	}

	@Test
	public void testXGrid() throws RrdException, IOException, RrdException {
		this.graphCmd
				.executeCommand("graph - --x-grid MINUTE:10:HOUR:1:HOUR:4:0:%A DEF:testvalue="
						+ this.jrbFileName
						+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");
		
	}

	@Test
	public void testXGridNone() throws RrdException, IOException, RrdException {
		this.graphCmd
		.executeCommand("graph - --x-grid none DEF:testvalue="
				+ this.jrbFileName
				+ ":testvalue:AVERAGE LINE1:testvalue#FF0000");

	}
	
	

}
