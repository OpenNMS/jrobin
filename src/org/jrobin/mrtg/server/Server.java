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
package org.jrobin.mrtg.server;

import org.jrobin.core.RrdDb;
import org.jrobin.mrtg.MrtgException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

class Server {
	private static Server instance;

	private Hardware hardware = new Hardware();
	private Scheduler scheduler = new Scheduler();
	private Archiver archiver = new Archiver();
	private RpcServer webServer = new RpcServer();
	private Date startDate = new Date();

	public synchronized static Server getInstance() throws MrtgException {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	private Server() throws MrtgException {
		// set RrdDb locking mode
		RrdDb.setLockMode(RrdDb.NO_LOCKS);
		String hwFile = Config.getHardwareFile();
		if(new File(hwFile).exists()) {
			loadHardware();
		}
		else {
			saveHardware();
		}
		// start scheduler and archiver
		archiver.start();
		scheduler.start();
	}

	void saveHardware() throws MrtgException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Element root = doc.createElement("mrtg");
			doc.appendChild(root);
			Vector routers = hardware.getRouters();
			for(int i = 0; i < routers.size(); i++) {
				Router router = (Router) routers.get(i);
				router.appendXml(root);
			}
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			DOMSource source = new DOMSource(root);
			FileOutputStream destination = new FileOutputStream(Config.getHardwareFile());
			StreamResult result = new StreamResult(destination);
			transformer.transform(source, result);
			destination.close();
		} catch (Exception e) {
			throw new MrtgException(e);
		}
	}

	private void loadHardware() throws MrtgException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(Config.getHardwareFile()));
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("router");
			hardware = new Hardware();
			Vector routers = hardware.getRouters();
			for(int i = 0; i < nodes.getLength(); i++) {
				routers.add(new Router(nodes.item(i)));
			}
		} catch (Exception e) {
			throw new MrtgException(e);
		}
	}

	public String toString() {
		return hardware.toString();
	}

	synchronized int addRouter(String host, String community, String descr, boolean active)
		throws MrtgException {
		int retCode = hardware.addRouter(host, community, descr, active);
		saveHardware();
		return retCode;
	}

	synchronized int updateRouter(String host, String community, String descr, boolean active)
		throws MrtgException {
		int retCode = hardware.updateRouter(host, community, descr, active);
		saveHardware();
		return retCode;
	}

	synchronized int removeRouter(String host) throws MrtgException {
		int retCode = hardware.removeRouter(host);
		saveHardware();
		return retCode;
	}

	synchronized int addLink(String host, String ifDescr, String descr, int samplingInterval,
							 boolean active)
		throws MrtgException {
		int retCode = hardware.addLink(host, ifDescr, descr, samplingInterval, active);
		saveHardware();
		return retCode;
	}

	synchronized int updateLink(String host, String ifDescr, String descr,
								int samplingInterval, boolean active)
		throws MrtgException {
		int retCode = hardware.updateLink(host, ifDescr, descr, samplingInterval, active);
		saveHardware();
		return retCode;
	}

	synchronized int removeLink(String host, String ifDescr) throws MrtgException {
		int retCode = hardware.removeLink(host, ifDescr);
		saveHardware();
		return retCode;
	}

	synchronized byte[] getPngGraph(String host, String ifDescr, long start, long stop)
		throws MrtgException {
		Grapher grapher = new Grapher(host, ifDescr);
		return grapher.getPngGraphBytes(start, stop);
	}

	synchronized Router[] getRouters() {
		return (Router[]) hardware.getRouters().toArray(new Router[0]);
	}

	String[] getAvailableLinks(String host) throws MrtgException {
		Router router = hardware.getRouterByHost(host);
		try {
			if(router != null) {
				return router.getAvailableLinks();
			}
			else {
				return null;
			}
		} catch (IOException e) {
			throw new MrtgException(e);
		}
	}

    Hardware getHardware() {
		return hardware;
	}

	Archiver getArchiver() {
		return archiver;
	}

	public Date getStartDate() {
		return startDate;
	}

	Hashtable getServerInfo() {
		Hashtable hash = new Hashtable();
		hash.put("sampleCount", new Integer(archiver.getSampleCount()));
		hash.put("savesCount", new Integer(archiver.getSavesCount()));
		hash.put("goodSavesCount", new Integer(archiver.getGoodSavesCount()));
		hash.put("badSavesCount", new Integer(archiver.getBadSavesCount()));
		hash.put("startDate", startDate);
		return hash;
	}

	public static void main(String[] args) throws Exception {
        Server s = Server.getInstance();
	}
}

