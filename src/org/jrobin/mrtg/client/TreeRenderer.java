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
package org.jrobin.mrtg.client;

import org.jrobin.mrtg.MrtgException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

class TreeRenderer extends DefaultTreeCellRenderer {
	private static ImageIcon MRTG_ICON;
	private static ImageIcon ROUTER_ICON;
	private static ImageIcon LINK_ICON;
	private static ImageIcon INACTIVE_ROUTER_ICON;
	private static ImageIcon INACTIVE_LINK_ICON;

	static {
		try {
			MRTG_ICON = Resources.getImageIcon("res/mrtg.png");
			ROUTER_ICON = Resources.getImageIcon("res/router.png");
			LINK_ICON = Resources.getImageIcon("res/link.png");
			INACTIVE_ROUTER_ICON = Resources.getImageIcon("res/router_inactive.png");
			INACTIVE_LINK_ICON = Resources.getImageIcon("res/link_inactive.png");
		} catch (MrtgException e) {
			e.printStackTrace();
		}
	}

	TreeRenderer() {
    	setLeafIcon(null);
		setClosedIcon(null);
		setOpenIcon(null);
	}

    public Component getTreeCellRendererComponent(
		JTree tree, Object value, boolean sel, boolean expanded,
		boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		Object nodeObj = node.getUserObject();
		if(nodeObj instanceof ServerInfo) {
			setFont(getFont().deriveFont(Font.BOLD));
			setIcon(MRTG_ICON);
		}
		else if (nodeObj instanceof RouterInfo) {
			setFont(getFont().deriveFont(Font.BOLD));
			RouterInfo routerInfo = (RouterInfo) nodeObj;
			//setForeground(routerInfo.isActive()? Color.BLACK: Color.RED);
			setIcon(routerInfo.isActive()? ROUTER_ICON: INACTIVE_ROUTER_ICON);
		}
		else if (nodeObj instanceof LinkInfo) {
			setFont(getFont().deriveFont(Font.PLAIN));
			LinkInfo linkInfo = (LinkInfo) nodeObj;
			//setForeground(linkInfo.isActive()? Color.BLACK: Color.RED);
			setIcon(linkInfo.isActive()? LINK_ICON: INACTIVE_LINK_ICON);
		}
		else {
			setFont(getFont().deriveFont(Font.PLAIN));
			//setForeground(Color.BLACK);
		}
		return this;
	}
}
