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

package jrobin.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

class XmlReader {

	private Element root;
	private Node[] dsNodes, arcNodes;

    XmlReader(String xmlFilePath) throws IOException, RrdException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(xmlFilePath));
			root = doc.getDocumentElement();
			dsNodes = getChildNodes(root, "ds");
			arcNodes = getChildNodes(root, "rra");
		} catch (FactoryConfigurationError e) {
			throw new RrdException("XML error: " + e);
		} catch (ParserConfigurationException e) {
			throw new RrdException("XML error: " + e);
		} catch (SAXException e) {
			throw new RrdException("XML error: " + e);
		}
	}

	String getVersion() throws RrdException {
		return getChildValue(root, "version");
	}

	long getLastUpdateTime() throws RrdException {
		return getChildValueAsLong(root, "lastupdate");
	}

	long getStep() throws RrdException {
		return getChildValueAsLong(root, "step");
	}

	int getDsCount() {
		return dsNodes.length;
	}

	int getArcCount() {
		return arcNodes.length;
	}

    String getDsName(int dsIndex) throws RrdException {
		return getChildValue(dsNodes[dsIndex], "name");
	}

	String getDsType(int dsIndex) throws RrdException {
		return getChildValue(dsNodes[dsIndex], "type");
	}

	long getHeartbeat(int dsIndex) throws RrdException {
		return getChildValueAsLong(dsNodes[dsIndex], "minimal_heartbeat");
	}

	double getMinValue(int dsIndex) throws RrdException {
		return getChildValueAsDouble(dsNodes[dsIndex], "min");
	}

	double getMaxValue(int dsIndex) throws RrdException {
		return getChildValueAsDouble(dsNodes[dsIndex], "max");
	}

	double getLastValue(int dsIndex) throws RrdException {
		return getChildValueAsDouble(dsNodes[dsIndex], "last_ds");
	}

	double getAccumValue(int dsIndex) throws RrdException {
		return getChildValueAsDouble(dsNodes[dsIndex], "value");
	}

	long getNanSeconds(int dsIndex) throws RrdException {
		return getChildValueAsLong(dsNodes[dsIndex], "unknown_sec");
	}

    String getConsolFun(int arcIndex) throws RrdException {
		return getChildValue(arcNodes[arcIndex], "cf");
	}

	double getXff(int arcIndex) throws RrdException {
		return getChildValueAsDouble(arcNodes[arcIndex], "xff");
	}

	int getSteps(int arcIndex) throws RrdException {
		return getChildValueAsInt(arcNodes[arcIndex], "pdp_per_row");
	}

	double getStateAccumValue(int arcIndex, int dsIndex) throws RrdException {
        Node cdpNode = getFirstChildNode(arcNodes[arcIndex], "cdp_prep");
        Node[] dsNodes = getChildNodes(cdpNode, "ds");
		return getChildValueAsDouble(dsNodes[dsIndex], "value");
	}

	int getStateNanSteps(int arcIndex, int dsIndex) throws RrdException {
        Node cdpNode = getFirstChildNode(arcNodes[arcIndex], "cdp_prep");
        Node[] dsNodes = getChildNodes(cdpNode, "ds");
		return getChildValueAsInt(dsNodes[dsIndex], "unknown_datapoints");
	}

	int getRows(int arcIndex) throws RrdException {
		Node dbNode = getFirstChildNode(arcNodes[arcIndex], "database");
        Node[] rows = getChildNodes(dbNode, "row");
		return rows.length;
	}

	double[] getValues(int arcIndex, int dsIndex) throws RrdException {
		Node dbNode = getFirstChildNode(arcNodes[arcIndex], "database");
        Node[] rows = getChildNodes(dbNode, "row");
		double[] values = new double[rows.length];
		for(int i = 0; i < rows.length; i++) {
            Node[] vNodes = getChildNodes(rows[i], "v");
			Node vNode = vNodes[dsIndex];
			values[i] = Util.parseDouble(vNode.getFirstChild().getNodeValue().trim());
		}
		return values;
	}

	// utility functions for DOM tree traversing

	static Node[] getChildNodes(Node parentNode, String childName) {
		ArrayList nodes = new ArrayList();
		NodeList nodeList = parentNode.getChildNodes();
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if(node.getNodeName().equals(childName)) {
				nodes.add(node);
			}
		}
		return (Node[]) nodes.toArray(new Node[0]);
	}

	static Node getFirstChildNode(Node parentNode, String childName) throws RrdException {
		Node[] childs = getChildNodes(parentNode, childName);
		if(childs.length > 0) {
			return childs[0];
		}
		throw new RrdException("XML Error, no such child: " + childName);
	}

	static String getChildValue(Node parentNode, String childName) throws RrdException {
		NodeList children = parentNode.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if(child.getNodeName().equals(childName)) {
				return child.getFirstChild().getNodeValue().trim();
			}
		}
		throw new RrdException("XML Error, no such child: " + childName);
	}

	static int getChildValueAsInt(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Integer.parseInt(valueStr);
	}

	static long getChildValueAsLong(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Long.parseLong(valueStr);
	}

	static double getChildValueAsDouble(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Util.parseDouble(valueStr);
	}
}