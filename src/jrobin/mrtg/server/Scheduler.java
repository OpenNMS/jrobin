/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
 *
 * (C) Copyright 2003, by Sasa Markovic.
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

import jrobin.mrtg.MrtgException;

import java.util.Vector;

class Scheduler extends Thread {
	private static final int RESOLUTION = 5; // seconds

	public Scheduler() {

	}

	public void run() {
		Hardware hardware;
		try {
			hardware = Server.getInstance().getHardware();
		} catch (MrtgException e) {
			e.printStackTrace();
			return;
		}
		while(true) {
			Vector routers = hardware.getRouters();
			for(int i = 0; i < routers.size(); i++) {
				Router router = (Router) routers.get(i);
				Vector links = router.getLinks();
				for (int j = 0; j < links.size(); j++) {
					Link link = (Link) links.get(j);
                    if(router.isActive() && link.isActive() && link.isDue() && !link.isSampling()) {
						new Collector(router, link).start();
					}
				}
			}
			// sleep for a while
			synchronized(this) {
				try {
					wait(RESOLUTION * 1000L);
				} catch (InterruptedException e) { }
			}
		}
	}
}
