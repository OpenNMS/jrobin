/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.cmd;

import org.jrobin.core.*;
import org.jrobin.graph.RrdGraphConstants;

import java.io.IOException;

class RrdFetchCmd extends RrdToolCmd implements RrdGraphConstants {
	String getCmdType() {
		return "fetch";
	}

	Object execute() throws RrdException, IOException {
		String startStr = getOptionValue("s", "start", DEFAULT_START);
		String endStr = getOptionValue("e", "end", DEFAULT_END);
		long[] timestamps = Util.getTimestamps(startStr, endStr);
		String resolutionStr = getOptionValue("r", "resolution", "1");
		long resolution = parseLong(resolutionStr);
		// other words
		String[] words = getRemainingWords();
		if (words.length != 3) {
			throw new RrdException("Invalid rrdfetch syntax");
		}
		String path = words[1];
		String consolFun = words[2];
		RrdDb rrdDb = getRrdDbReference(path);
		try {
			FetchRequest fetchRequest = rrdDb.createFetchRequest(consolFun, timestamps[0], timestamps[1], resolution);
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
