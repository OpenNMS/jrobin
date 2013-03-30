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

import org.jrobin.core.Datasource;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

import java.io.IOException;

class RrdRestoreCmd extends RrdToolCmd {
	String getCmdType() {
		return "restore";
	}

	Object execute() throws RrdException, IOException, RrdException {
		boolean check = getBooleanOption("r", "range-check");
		String[] words = getRemainingWords();
		if (words.length != 3) {
			throw new RrdException("Invalid rrdrestore syntax");
		}
		String xmlPath = words[1];
		String rrdPath = words[2];
		RrdDb rrdDb = getRrdDbReference(rrdPath, xmlPath);
		try {
			if (check) {
				int dsCount = rrdDb.getHeader().getDsCount();
				for (int i = 0; i < dsCount; i++) {
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
