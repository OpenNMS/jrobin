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
package org.jrobin.core;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Date;
import java.util.GregorianCalendar;
import java.awt.*;

/**
 * Class used as a base class for various XML template related classes. Class provides
 * methods for XML source parsing and XML tree traversing. XML source may have unlimited
 * number of placeholders (variables) in the format <code>${variable_name}</code>.
 * Methods are provided to specify variable values at runtime.
 * Note that this class has limited functionality: XML source gets parsed, and variable
 * values are collected. You have to extend this class to do something more useful.<p>
 */
public abstract class XmlTemplate {
	protected Element root;
	private HashMap valueMap = new HashMap();
	private HashSet validatedNodes = new HashSet();

	protected XmlTemplate(InputSource xmlSource) throws IOException, RrdException {
		root = Util.Xml.getRootElement(xmlSource);
	}

	protected XmlTemplate(String xmlString) throws IOException, RrdException {
		root = Util.Xml.getRootElement(xmlString);
	}

	protected XmlTemplate(File xmlFile) throws IOException, RrdException {
		root = Util.Xml.getRootElement(xmlFile);
	}

	/**
	 * Removes all placeholder-value mappings.
	 */
	public void clearValues() {
		valueMap.clear();
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, String value) {
		valueMap.put(name, value);
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, int value) {
		valueMap.put(name, new Integer(value));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, long value) {
		valueMap.put(name, new Long(value));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, double value) {
		valueMap.put(name, new Double(value));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, Color value) {
		valueMap.put(name, "#" + Integer.toHexString(value.getRGB() & 0xFFFFFF));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, Date value) {
        setMapping(name, Util.getTimestamp(value));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, GregorianCalendar value) {
        setMapping(name, Util.getTimestamp(value));
	}

	/**
	 * Sets value for a single XML template variable. Variable name should be specified
	 * without leading '${' and ending '}' placeholder markers. For example, for a placeholder
	 * <code>${start}</code>, specify <code>start</start> for the <code>name</code> parameter.
	 * @param name variable name
	 * @param value value to be set in the XML template
	 */
	public void setMapping(String name, boolean value) {
		valueMap.put(name, "" + value);
	}

	protected static Node[] getChildNodes(Node parentNode, String childName) {
		return Util.Xml.getChildNodes(parentNode, childName);
	}

	protected static Node[] getChildNodes(Node parentNode) {
		return Util.Xml.getChildNodes(parentNode, null);
	}

	protected static Node getFirstChildNode(Node parentNode, String childName) throws RrdException {
		return Util.Xml.getFirstChildNode(parentNode, childName);
	}

	protected boolean hasChildNode(Node parentNode, String childName) {
		return Util.Xml.hasChildNode(parentNode, childName);
	}

	protected String getChildValue(Node parentNode, String childName) throws RrdException {
		String value = Util.Xml.getChildValue(parentNode, childName);
		return resolveMappings(value);
	}

	protected String getValue(Node parentNode) {
		String value = Util.Xml.getValue(parentNode);
		return resolveMappings(value);
	}

	private String resolveMappings(String value) {
		if(value.startsWith("${") && value.endsWith("}")) {
			// template variable found, remove leading "${" and trailing "}"
			String var = value.substring(2, value.length() - 1);
			if(valueMap.containsKey(var)) {
				// mapping found
				value = valueMap.get(var).toString();
			}
			else {
				// no mapping found - this is illegal
				throw new IllegalArgumentException("No mapping found for template variable " + value);
			}
		}
		return value;
	}

	protected int getChildValueAsInt(Node parentNode, String childName)	throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Integer.parseInt(valueStr);
	}

	protected int getValueAsInt(Node parentNode) {
		String valueStr = getValue(parentNode);
		return Integer.parseInt(valueStr);
	}

	protected long getChildValueAsLong(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Long.parseLong(valueStr);
	}

	protected long getValueAsLong(Node parentNode) {
		String valueStr = getValue(parentNode);
		return Long.parseLong(valueStr);
	}

	protected double getChildValueAsDouble(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Util.parseDouble(valueStr);
	}

	protected double getValueAsDouble(Node parentNode) {
		String valueStr = getValue(parentNode);
		return Util.parseDouble(valueStr);
	}

	protected boolean getChildValueAsBoolean(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Util.parseBoolean(valueStr);
	}

	protected boolean getValueAsBoolean(Node parentNode) {
		String valueStr = getValue(parentNode);
		return Util.parseBoolean(valueStr);
	}

	protected boolean isEmptyNode(Node node) {
		// comment node or empty text node
		return node.getNodeName().equals("#comment") ||
			(node.getNodeName().equals("#text") && node.getNodeValue().trim().length() == 0);
	}

	protected void validateOnce(Node parentNode, String[] allowedChildNames) throws RrdException {
		// validate node only once
		if(validatedNodes.contains(parentNode)) {
			//System.out.println("Already validated");
			return;
		}
		Node[] childs = getChildNodes(parentNode);
		main:
		for(int i = 0; i < childs.length; i++) {
			String childName = childs[i].getNodeName();
			for(int j = 0; j < allowedChildNames.length; j++) {
				if(childName.equals(allowedChildNames[j])) {
					continue main;
				}
			}
			if(!isEmptyNode(childs[i])) {
				throw new RrdException("Unexpected tag encountered: <" + childName + ">");
			}
		}
		validatedNodes.add(parentNode);
	}
}
