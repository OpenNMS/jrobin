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

package org.jrobin.inspector;

import org.jrobin.core.*;

import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.io.IOException;
import java.util.Date;

class DataTableModel extends AbstractTableModel {
	private static final String[] COLUMN_NAMES = {"timestamp", "date", "value"};

	private File file;
	private Object[][] values;
	private int dsIndex = -1, arcIndex = -1;

	public int getRowCount() {
		if(values == null) {
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
		if(values == null) {
			return "--";
		}
		return values[rowIndex][columnIndex];
	}

	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	void setFile(File newFile) {
		if (file == null || !file.getAbsolutePath().equals(newFile.getAbsolutePath())) {
			file = newFile;
			setIndex(-1, -1);
		}
	}

	void setIndex(int newDsIndex, int newArcIndex) {
		if (dsIndex != newDsIndex || arcIndex != newArcIndex) {
			dsIndex = newDsIndex;
			arcIndex = newArcIndex;
			values = null;
			if(dsIndex >= 0 && arcIndex >= 0) {
				try {
					RrdDb rrd = new RrdDb(file.getAbsolutePath());
					Archive arc = rrd.getArchive(arcIndex);
					Robin robin = arc.getRobin(dsIndex);
					long start = arc.getStartTime();
					long step = arc.getArcStep();
					double robinValues[] = robin.getValues();
					values = new Object[robinValues.length][];
					for(int i = 0; i < robinValues.length; i++) {
						long timestamp = start + i * step;
						String date = new Date(timestamp * 1000L).toString();
						String value = InspectorModel.formatDouble(robinValues[i]);
						values[i] = new Object[] {
							"" + timestamp,	date, value
						};
					}
					rrd.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (RrdException e) {
					e.printStackTrace();
				}
			}
			fireTableDataChanged();
		}
	}
}
