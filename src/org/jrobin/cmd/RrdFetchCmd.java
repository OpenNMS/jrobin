package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;
import org.jrobin.core.FetchRequest;
import org.jrobin.core.FetchData;

import java.io.IOException;

class RrdFetchCmd extends RrdToolCmd {
	static final String DEFAULT_START = "end-1day";
	static final String DEFAULT_END = "now";

	public RrdFetchCmd(RrdCmdScanner cmdScanner) {
		super(cmdScanner);
	}

	String getCmdType() {
		return "fetch";
	}

	Object execute() throws RrdException, IOException {
		// --start
		String startStr = cmdScanner.getOptionValue("s", "start", DEFAULT_START);
		TimeSpec spec1 = new TimeParser(startStr).parse();
		// --end
		String endStr = cmdScanner.getOptionValue("e", "end", DEFAULT_END);
		TimeSpec spec2 = new TimeParser(endStr).parse();
		long[] timestamps = TimeSpec.getTimestamps(spec1, spec2);
		// --resolution
		String resolutionStr = cmdScanner.getOptionValue("r", "resolution");
		long resolution = 1;
		if(resolutionStr != null) {
			resolution = parseLong(resolutionStr);
		}
		// other words
    	String[] tokens = cmdScanner.getRemainingWords();
		if(tokens.length != 3) {
			throw new RrdException("Invalid rrdfetch syntax");
		}
		String path = tokens[1];
		String consolFun = tokens[2];
		RrdDb rrdDb = getRrdDbReference(path);
		try {
			FetchRequest fetchRequest = rrdDb.createFetchRequest(
					consolFun, timestamps[0], timestamps[1], resolution);
			System.out.println(fetchRequest.dump());
			FetchData fetchData = fetchRequest.fetchData();
			println(fetchData.toString());
			return fetchData;
		}
		finally {
			releaseRrdDbReference(rrdDb);
		}
	}
}
