package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;
import org.jrobin.core.Datasource;

import java.io.IOException;

class RrdRestoreCmd extends RrdToolCmd {

	String getCmdType() {
		return "restore";
	}

	Object execute() throws RrdException, IOException {
    	boolean check = getBooleanOption("r", "range-check");
		String[] words = getRemainingWords();
		if(words.length != 3) {
			throw new RrdException("Invalid rrdrestore syntax");
		}
		String xmlPath = words[1];
		String rrdPath = words[2];
		RrdDb rrdDb = getRrdDbReference(rrdPath, xmlPath);
		try {
			if(check) {
				int dsCount = rrdDb.getHeader().getDsCount();
				for(int i = 0; i < dsCount; i++) {
					Datasource ds = rrdDb.getDatasource(i);
					double minValue = ds.getMinValue();
					double maxValue = ds.getMaxValue();
					// this will perform range check
					ds.setMinMaxValue(minValue, maxValue, true);
				}
			}
			return rrdPath;
		}
		finally {
			releaseRrdDbReference(rrdDb);
		}
	}


}
