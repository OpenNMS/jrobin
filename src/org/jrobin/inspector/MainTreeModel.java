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

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.io.File;

class MainTreeModel extends DefaultTreeModel {
	private static final DefaultMutableTreeNode INVALID_NODE =
		new DefaultMutableTreeNode("No valid RRD file specified");

	private File file;

	MainTreeModel() {
		super(INVALID_NODE);
	}

	boolean setFile(File newFile) {
		try {
			file = newFile;
			RrdDb rrd = new RrdDb(file.getAbsolutePath());
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new RrdNode(rrd));
			int dsCount = rrd.getRrdDef().getDsCount();
			int arcCount = rrd.getRrdDef().getArcCount();
			for (int dsIndex = 0; dsIndex < dsCount; dsIndex++) {
				DefaultMutableTreeNode dsNode =
					new DefaultMutableTreeNode(new RrdNode(rrd, dsIndex));
				for (int arcIndex = 0; arcIndex < arcCount; arcIndex++) {
					DefaultMutableTreeNode arcNode =
						new DefaultMutableTreeNode(new RrdNode(rrd, dsIndex, arcIndex));
					dsNode.add(arcNode);
				}
				root.add(dsNode);
			}
			rrd.close();
			setRoot(root);
			return true;
		} catch (IOException e) {
			setRoot(INVALID_NODE);
		} catch (RrdException e) {
			setRoot(INVALID_NODE);
		}
		return false;
	}
}
