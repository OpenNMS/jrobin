/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by saxon
 * User: stalker
 * Date: Oct 5, 2003
 * Time: 11:26:05 AM
 */
class InspectorModel {
	private MainTreeModel mainTreeModel = new MainTreeModel();
	private HeaderTableModel generalTableModel = new HeaderTableModel();
	private DatasourceTableModel datasourceTableModel = new DatasourceTableModel();
	private ArchiveTableModel archiveTableModel = new ArchiveTableModel();
	private DataTableModel dataTableModel = new DataTableModel();

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
		mainTreeModel.setFile(file);
		generalTableModel.setFile(file);
		datasourceTableModel.setFile(file);
		archiveTableModel.setFile(file);
		dataTableModel.setFile(file);
	}

	void selectModel(int dsIndex, int arcIndex) {
		datasourceTableModel.setIndex(dsIndex);
		archiveTableModel.setIndex(dsIndex, arcIndex);
		dataTableModel.setIndex(dsIndex, arcIndex);
	}

	private static String DOUBLE_FORMAT = "0.0000000000E00";
	private static final DecimalFormat df = new DecimalFormat(DOUBLE_FORMAT);

	static String formatDouble(double x, String nanString) {
		if(Double.isNaN(x)) {
			return nanString;
		}
		return df.format(x);
	}

	static String formatDouble(double x) {
		return formatDouble(x, "" + Double.NaN);
	}
}
