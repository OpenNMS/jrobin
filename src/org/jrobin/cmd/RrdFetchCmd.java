/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;
import org.jrobin.core.FetchRequest;
import org.jrobin.core.FetchData;

import java.io.IOException;

class RrdFetchCmd extends RrdToolCmd {
	static final String DEFAULT_START = "end-1day";
	static final String DEFAULT_END = "now";

	String getCmdType() {
		return "fetch";
	}

	Object execute() throws RrdException, IOException {
		// --start
		String startStr = getOptionValue("s", "start", DEFAULT_START);
		TimeSpec spec1 = new TimeParser(startStr).parse();
		// --end
		String endStr = getOptionValue("e", "end", DEFAULT_END);
		TimeSpec spec2 = new TimeParser(endStr).parse();
		long[] timestamps = TimeSpec.getTimestamps(spec1, spec2);
		// --resolution
		String resolutionStr = getOptionValue("r", "resolution", "1");
		long resolution = parseLong(resolutionStr);
		// other words
    	String[] words = getRemainingWords();
		if(words.length != 3) {
			throw new RrdException("Invalid rrdfetch syntax");
		}
		String path = words[1];
		String consolFun = words[2];
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
