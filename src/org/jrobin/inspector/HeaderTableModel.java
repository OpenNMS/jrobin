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
 *
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

package org.jrobin.inspector;

import org.jrobin.core.RrdDb;
import org.jrobin.core.Header;
import org.jrobin.core.RrdException;

import javax.swing.table.AbstractTableModel;
import java.util.Date;
import java.io.IOException;
import java.io.File;

class HeaderTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final Object[] DESCRIPTIONS = {
			"path", "signature", "step", "last timestamp",
			"datasources", "archives", "size"
	};
	private static final String[] COLUMN_NAMES = {"description", "value"};

	private Object[] values;

	public int getRowCount() {
		return DESCRIPTIONS.length;
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return DESCRIPTIONS[rowIndex];
		}
		else if (columnIndex == 1) {
			if (values != null) {
				return values[rowIndex];
			}
			else {
				return "--";
			}
		}
		return null;
	}

	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}


	void setFile(File file) {
		try {
			values = null;
			String path = file.getAbsolutePath();
			RrdDb rrd = new RrdDb(path, true);
			try {
				Header header = rrd.getHeader();
				String signature = header.getSignature();
				String step = "" + header.getStep();
				String lastTimestamp = header.getLastUpdateTime() + " [" +
						new Date(header.getLastUpdateTime() * 1000L) + "]";
				String datasources = "" + header.getDsCount();
				String archives = "" + header.getArcCount();
				String size = rrd.getRrdBackend().getLength() + " bytes";
				values = new Object[] {
						path, signature, step, lastTimestamp, datasources, archives, size
				};
			}
			finally {
				rrd.close();
			}
			fireTableDataChanged();
		}
		catch (IOException e) {
			Util.error(null, e);
		}
		catch (RrdException e) {
			Util.error(null, e);
		}
	}
}