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
package jrobin.mrtg.client;

import jrobin.mrtg.MrtgConstants;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

class RpcClient implements MrtgConstants {

	private XmlRpcClient webClient;

	RpcClient(String host) throws IOException {
		webClient = new XmlRpcClient(host, SERVER_PORT);
	}

    Hashtable getMrtgInfo() throws IOException, XmlRpcException {
		Vector params = new Vector();
		return (Hashtable) webClient.execute("mrtg.getMrtgInfo", params);
	}

	byte[] getPngGraph(RouterInfo routerInfo, LinkInfo linkInfo, Date start, Date stop)
		throws IOException, XmlRpcException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(linkInfo.getIfDescr());
		params.add(start);
		params.add(stop);
		return (byte[]) webClient.execute("mrtg.getPngGraph", params);
	}

	int addRouter(RouterInfo routerInfo)
		throws IOException, XmlRpcException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(routerInfo.getCommunity());
		params.add(routerInfo.getDescr());
		params.add(new Boolean(routerInfo.isActive()));
		return ((Integer) webClient.execute("mrtg.addRouter", params)).intValue();
	}

	int updateRouter(RouterInfo routerInfo)
		throws IOException, XmlRpcException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(routerInfo.getCommunity());
		params.add(routerInfo.getDescr());
		params.add(new Boolean(routerInfo.isActive()));
		return ((Integer) webClient.execute("mrtg.updateRouter", params)).intValue();
	}

	int deleteRouter(RouterInfo routerInfo) throws IOException, XmlRpcException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		return ((Integer) webClient.execute("mrtg.removeRouter", params)).intValue();
	}

	Vector getAvailableLinks(RouterInfo routerInfo)
		throws IOException, XmlRpcException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		Vector result = (Vector) webClient.execute("mrtg.getAvailableLinks", params);
		return result;
	}

	int addLink(RouterInfo routerInfo, LinkInfo linkInfo) throws XmlRpcException, IOException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(linkInfo.getIfDescr());
		params.add(linkInfo.getDescr());
		params.add(new Integer(linkInfo.getSamplingInterval()));
		params.add(new Boolean(linkInfo.isActive()));
		return ((Integer) webClient.execute("mrtg.addLink", params)).intValue();
	}

	int updateLink(RouterInfo routerInfo, LinkInfo linkInfo)
		throws XmlRpcException, IOException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(linkInfo.getIfDescr());
		params.add(linkInfo.getDescr());
		params.add(new Integer(linkInfo.getSamplingInterval()));
		params.add(new Boolean(linkInfo.isActive()));
		return ((Integer) webClient.execute("mrtg.updateLink", params)).intValue();
	}

	int removeLink(RouterInfo routerInfo, LinkInfo linkInfo)
		throws XmlRpcException, IOException {
		Vector params = new Vector();
		params.add(routerInfo.getHost());
		params.add(linkInfo.getIfDescr());
		return ((Integer) webClient.execute("mrtg.removeLink", params)).intValue();
	}
}
