/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
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

package org.jrobin.graph;

import org.jrobin.data.DataProcessor;

class Def extends Source {
	private final String rrdPath, dsName, consolFun, backend;

	Def(String name, String rrdPath, String dsName, String consolFun) {
		this(name, rrdPath, dsName, consolFun, null);
	}

	Def(String name, String rrdPath, String dsName, String consolFun, String backend) {
		super(name);
		this.rrdPath = rrdPath;
		this.dsName = dsName;
		this.consolFun = consolFun;
		this.backend = backend;
	}

	void requestData(DataProcessor dproc) {
		if (backend == null) {
			dproc.addDatasource(name, rrdPath, dsName, consolFun);
		}
		else {
			dproc.addDatasource(name, rrdPath, dsName, consolFun, backend);
		}
	}
}
