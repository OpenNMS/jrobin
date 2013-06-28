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
package org.jrobin.inspector;

import org.jrobin.core.RrdDb;
import org.jrobin.core.Datasource;
import org.jrobin.core.RrdException;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.io.File;

class DatasourceTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final Object[] DESCRIPTIONS = {
			"name", "type", "heartbeat", "min value",
			"max value", "last value", "accum. value", "NaN seconds"
	};
	private static final String[] COLUMN_NAMES = {
			"description", "value"
	};

	private File file;
	private Object[] values;
	private int dsIndex = -1;

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

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	void setFile(File newFile) {
		file = newFile;
		setIndex(-1);
	}

	void setIndex(int newDsIndex) {
		if (dsIndex != newDsIndex) {
			dsIndex = newDsIndex;
			values = null;
			if (dsIndex >= 0) {
				try {
					RrdDb rrd = new RrdDb(file.getAbsolutePath(), true);
					try {
						Datasource ds = rrd.getDatasource(dsIndex);
						values = new Object[] {
								ds.getDsName(),
								ds.getDsType(),
								"" + ds.getHeartbeat(),
								InspectorModel.formatDouble(ds.getMinValue()),
								InspectorModel.formatDouble(ds.getMaxValue()),
								InspectorModel.formatDouble(ds.getLastValue()),
								InspectorModel.formatDouble(ds.getAccumValue()),
								"" + ds.getNanSeconds()
						};
					}
					finally {
						rrd.close();
					}
				}
				catch (IOException e) {
					Util.error(null, e);
				}
				catch (RrdException e) {
					Util.error(null, e);
				}
			}
			fireTableDataChanged();
		}
	}
}