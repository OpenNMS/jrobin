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

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.jrobin.core.Util;

import java.io.IOException;

class RrdUpdateCmd extends RrdToolCmd {
	String getCmdType() {
		return "update";
	}

	Object execute() throws RrdException, IOException {
		String template = getOptionValue("t", "template");
		String[] dsNames = (template != null) ? new ColonSplitter(template).split() : null;
		String[] words = getRemainingWords();
		if (words.length < 3) {
			throw new RrdException("Insufficent number of parameters for rrdupdate command");
		}
		String path = words[1];
		RrdDb rrdDb = getRrdDbReference(path);
		try {
			if (dsNames != null) {
				// template specified, check datasource names
				for (String dsName : dsNames) {
					if (!rrdDb.containsDs(dsName)) {
						throw new RrdException("Invalid datasource name: " + dsName);
					}
				}
			}
			// parse update strings
			long timestamp = -1;
			for (int i = 2; i < words.length; i++) {
				String[] tokens = new ColonSplitter(words[i]).split();
				if (dsNames != null && dsNames.length + 1 != tokens.length) {
					throw new RrdException("Template requires " + dsNames.length + " values, " +
							(tokens.length - 1) + " value(s) found in: " + words[i]);
				}
				int dsCount = rrdDb.getHeader().getDsCount();
				if (dsNames == null && dsCount + 1 != tokens.length) {
					throw new RrdException("Expected " + dsCount + " values, " +
							(tokens.length - 1) + " value(s) found in: " + words[i]);
				}
				timestamp = Util.getTimestamp(tokens[0]);
				Sample sample = rrdDb.createSample(timestamp);
				for (int j = 1; j < tokens.length; j++) {
					if (dsNames == null) {
						sample.setValue(j - 1, parseDouble(tokens[j]));
					}
					else {
						sample.setValue(dsNames[j - 1], parseDouble(tokens[j]));
					}
				}
				sample.update();
			}
			return timestamp;
		}
		finally {
			releaseRrdDbReference(rrdDb);
		}
	}
}

