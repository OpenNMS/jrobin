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
package jrobin.mrtg.server;

import java.util.Vector;

class Hardware {
	private Vector routers = new Vector();

	Hardware() {	}

	Vector getRouters() {
		return routers;
	}

	void setRouters(Vector routers) {
		this.routers = routers;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		for(int i = 0; i < routers.size(); i++) {
			buff.append(routers.get(i));
		}
		return buff.toString();
	}

	Router getRouterByHost(String host) {
        for(int i = 0; i < routers.size(); i++) {
			Router router = (Router) routers.get(i);
			if(router.getHost().equalsIgnoreCase(host)) {
				return router;
			}
		}
		return null;
	}

	int addRouter(String host, String community, String descr, boolean active) {
		Router router = getRouterByHost(host);
		if(router == null) {
			// not found
			Router newRouter = new Router();
            newRouter.setHost(host);
			newRouter.setCommunity(community);
			newRouter.setDescr(descr);
			newRouter.setActive(active);
			routers.add(newRouter);
			// added
			return 0;
		}
		// error, already exists
		return -1;
	}

    int updateRouter(String host, String community, String descr, boolean active) {
        Router router = getRouterByHost(host);
		if(router != null) {
			router.setCommunity(community);
			router.setDescr(descr);
			router.setActive(active);
			return 0;
		}
		// not found, not updated
		return -1;
	}

	int removeRouter(String host) {
		Router router = getRouterByHost(host);
		if(router == null) {
			// not found, cannot remove
			return -1;
		}
		// remove router only if no links are attached
		if(router.getLinkCount() > 0) {
			return -2;
		}
		routers.remove(router);
		return 0;
	}

	int addLink(String host, String ifDescr, String descr, int samplingInterval, boolean active) {
        Router router = getRouterByHost(host);
		if(router == null) {
			// router not found, link cannot be added
            return -1;
		}
        Link link = router.getLinkByIfDescr(ifDescr);
		if(link != null) {
			// such link already exists, link cannot be added
			return -2;
		}
        Link newLink = new Link();
		newLink.setDescr(descr);
		newLink.setIfDescr(ifDescr);
		newLink.setSamplingInterval(samplingInterval);
		newLink.setActive(active);
		router.addLink(newLink);
		return 0;
	}

	int updateLink(String host, String ifDescr, String descr, int samplingInterval, boolean active) {
		Router router = getRouterByHost(host);
		if(router == null) {
			// router not found, link cannot be updated
            return -1;
		}
        Link link = router.getLinkByIfDescr(ifDescr);
		if(link == null) {
			// such link cannot be found and updated
			return -2;
		}
		link.setDescr(descr);
		link.setSamplingInterval(samplingInterval);
		link.setActive(active);
		return 0;
	}

	int removeLink(String host, String ifDescr) {
		Router router = getRouterByHost(host);
		if(router == null) {
			// router not found, link cannot be removed
            return -1;
		}
        Link link = router.getLinkByIfDescr(ifDescr);
		if(link == null) {
			// such link cannot be found and removed
			return -2;
		}
		router.removeLink(link);
		return 0;
	}
}

