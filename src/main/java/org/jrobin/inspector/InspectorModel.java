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

import java.io.File;
import java.text.DecimalFormat;

class InspectorModel {
	private MainTreeModel mainTreeModel = new MainTreeModel();
	private HeaderTableModel generalTableModel = new HeaderTableModel();
	private DatasourceTableModel datasourceTableModel = new DatasourceTableModel();
	private ArchiveTableModel archiveTableModel = new ArchiveTableModel();
	private DataTableModel dataTableModel = new DataTableModel();
	private File file;
	private boolean ok = false;

	MainTreeModel getMainTreeModel() {
		return mainTreeModel;
	}

	HeaderTableModel getGeneralTableModel() {
		return generalTableModel;
	}

	DatasourceTableModel getDatasourceTableModel() {
		return datasourceTableModel;
	}

	DataTableModel getDataTableModel() {
		return dataTableModel;
	}

	ArchiveTableModel getArchiveTableModel() {
		return archiveTableModel;
	}

	void setFile(File file) {
		this.file = file;
		this.ok = mainTreeModel.setFile(file);
		generalTableModel.setFile(file);
		datasourceTableModel.setFile(file);
		archiveTableModel.setFile(file);
		dataTableModel.setFile(file);
	}

	void refresh() {
		setFile(file);
	}

	void selectModel(int dsIndex, int arcIndex) {
		datasourceTableModel.setIndex(dsIndex);
		archiveTableModel.setIndex(dsIndex, arcIndex);
		dataTableModel.setIndex(dsIndex, arcIndex);
	}

	File getFile() {
		return file;
	}

	boolean isOk() {
		return ok;
	}

	private static String DOUBLE_FORMAT = "0.0000000000E00";
	private static final DecimalFormat df = new DecimalFormat(DOUBLE_FORMAT);

	static String formatDouble(double x, String nanString) {
		if (Double.isNaN(x)) {
			return nanString;
		}
		return df.format(x);
	}

	static String formatDouble(double x) {
		return formatDouble(x, "" + Double.NaN);
	}
}
