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

import org.jrobin.core.*;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.Date;

class DataTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String[] COLUMN_NAMES = {"timestamp", "date", "value"};

	private File file;
	private Object[][] values;
	private int dsIndex = -1, arcIndex = -1;

	public int getRowCount() {
		if (values == null) {
			return 0;
		}
		else {
			return values.length;
		}
	}

	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (values == null) {
			return "--";
		}
		return values[rowIndex][columnIndex];
	}

	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		assert columnIndex == 2: "Column " + columnIndex + " is not editable!";
		double value;
		try {
			value = Double.parseDouble(aValue.toString());
		}
		catch (NumberFormatException nfe) {
			value = Double.NaN;
		}
		if (dsIndex >= 0 && arcIndex >= 0 && file != null) {
			try {
				RrdDb rrd = new RrdDb(file.getAbsolutePath());
				try {
					Robin robin = rrd.getArchive(arcIndex).getRobin(dsIndex);
					robin.setValue(rowIndex, value);
					values[rowIndex][2] = InspectorModel.formatDouble(robin.getValue(rowIndex));
				}
				finally {
					rrd.close();
				}
			}
			catch (Exception e) {
				Util.error(null, e);
			}
		}
	}

	void setFile(File newFile) {
		file = newFile;
		setIndex(-1, -1);
	}

	void setIndex(int newDsIndex, int newArcIndex) {
		if (dsIndex != newDsIndex || arcIndex != newArcIndex) {
			dsIndex = newDsIndex;
			arcIndex = newArcIndex;
			values = null;
			if (dsIndex >= 0 && arcIndex >= 0) {
				try {
					RrdDb rrd = new RrdDb(file.getAbsolutePath(), true);
					try {
						Archive arc = rrd.getArchive(arcIndex);
						Robin robin = arc.getRobin(dsIndex);
						long start = arc.getStartTime();
						long step = arc.getArcStep();
						double robinValues[] = robin.getValues();
						values = new Object[robinValues.length][];
						for (int i = 0; i < robinValues.length; i++) {
							long timestamp = start + i * step;
							String date = new Date(timestamp * 1000L).toString();
							String value = InspectorModel.formatDouble(robinValues[i]);
							values[i] = new Object[] {
									"" + timestamp, date, value
							};
						}
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
