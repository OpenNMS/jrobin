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

/**
 * Class used as a base class for various XML template related classes. Class provides
 * methods for XML source parsing and XML tree traversing. XML source may have unlimited
 * number of placeholders in the format <code>${placeholder_name}</code>. Methods are provided
 * to specify values of placeholders at runtime. Note that this class has limited functionality:
 * XML source gets parsed, and placeholder values are collected. You have to extend this class
 * to do anything more useful.<p>
 */
public abstract class XmlTemplate {
	/**
	 * root element of the DOM hierarchy representing XML source
	 */
	protected Element root;
	private HashMap valueMap = new HashMap();

	/**
	 * Creates XmlTemplate object from any parsable XML input source
	 * @param xmlSource Any parsable XML input source
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException thrown (in most cases) when the source has invalid XML syntax
	 * (cannot be parsed at all)
	 */
	protected XmlTemplate(InputSource xmlSource) throws IOException, RrdException {
		root = Util.Xml.getRootElement(xmlSource);
	}

    /**
	 * Creates XmlTemplate object from a string containing properly formatted XML
	 * @param xmlString parsable XML string
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException thrown (in most cases) when the source has invalid XML syntax
	 * (cannot be parsed at all)
	 */
	protected XmlTemplate(String xmlString) throws IOException, RrdException {
		root = Util.Xml.getRootElement(xmlString);
	}

	/**
	 * Creates XmlTemplate object from a file containing properly formatted XML
	 * @param xmlFile File object representing file containing parsable XML source
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException thrown (in most cases) when the source has invalid XML syntax
	 * (cannot be parsed at all)
	 */
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
	 * Sets value for the given placeholder. Placeholder should be supplied by its name.
	 * For example, for a placeholder <code>${param}</code> specify just <code>param</code>
	 * for its name.
	 * @param name Name of the placeholder (placeholder without leading '${' and ending '}').
	 * @param value Value to replace placeholder with.
	 */
	public void setMapping(String name, String value) {
		valueMap.put(name, value);
	}

	/**
	 * Sets value for the given placeholder. Placeholder should be supplied by its name.
	 * For example, for a placeholder <code>${param}</code> specify just <code>param</code>
	 * for its name.
	 * @param name Name of the placeholder (placeholder without leading '${' and ending '}').
	 * @param value Value to replace placeholder with.
	 */
	public void setMapping(String name, int value) {
		valueMap.put(name, new Integer(value));
	}

	/**
	 * Sets value for the given placeholder. Placeholder should be supplied by its name.
	 * For example, for a placeholder <code>${param}</code> specify just <code>param</code>
	 * for its name.
	 * @param name Name of the placeholder (placeholder without leading '${' and ending '}').
	 * @param value Value to replace placeholder with.
	 */
	public void setMapping(String name, long value) {
		valueMap.put(name, new Long(value));
	}

	/**
	 * Sets value for the given placeholder. Placeholder should be supplied by its name.
	 * For example, for a placeholder <code>${param}</code> specify just <code>param</code>
	 * for its name.
	 * @param name Name of the placeholder (placeholder without leading '${' and ending '}').
	 * @param value Value to replace placeholder with.
	 */
	public void setMapping(String name, double value) {
		valueMap.put(name, new Double(value));
	}

	/**
	 * Returns all child nodes with the given tag
	 * belonging to the specified parent Node
	 * @param parentNode Parent node
	 * @param childName Child node tag
	 * @return Array of child nodes with the specified parent and with the given tag name
	 */
	protected static Node[] getChildNodes(Node parentNode, String childName) {
		return Util.Xml.getChildNodes(parentNode, childName);
	}

	/**
	 * Returns the first child node with the given parent and the given tag.
	 * @param parentNode Parent node
	 * @param childName Child node tag
	 * @return First child node with the given parent node and the given tag.
	 * @throws RrdException Thrown if no such child can be found
	 */
	protected static Node getFirstChildNode(Node parentNode, String childName) throws RrdException {
		return Util.Xml.getFirstChildNode(parentNode, childName);
	}

	/**
	 * Returns the 'value' of the child node with the given parent and the given tag name.
	 * For example, in a DOM-tree created from the following XML source:<p>
	 * <pre>
	 * ...&lt;root&gt;&lt;branch&gt;abc&lt;/branch&gt;&lt;/root&gt;...
	 * </pre>
	 * and assuming that <code>parentNode</code> points to the <code>&lt;root&gt;</code> element,
	 * the following call:<p>
	 * <pre>
	 * getChildValue(parentNode, "branch");
	 * </pre>
	 * returns:<p>
	 * <code>abc</code><p>
	 * @param parentNode Parent DOM node
	 * @param childName Child node tag
	 * @return XML 'value' of the child node (trimmed content between <childName> and </childName>
	 * tags)
	 * @throws RrdException Thrown if no such child node exists.
	 */
	protected String getChildValue(Node parentNode, String childName) throws RrdException {
		String value = Util.Xml.getChildValue(parentNode, childName);
		if(value.startsWith("${") && value.endsWith("}")) {
			// template variable found, remove leading "${" and trailing "}"
			String var = value.substring(2, value.length() - 1);
			if(valueMap.containsKey(var)) {
				// mapping found
				value = valueMap.get(var).toString();
			}
			else {
				// no mapping found - this is illegal
				throw new RrdException("No mapping found for template variable " + value);
			}
		}
		return value;
	}

	/**
	 * Returns the 'value' of the child node with the given parent and the given tag name.
	 * For example, in a DOM-tree created from the following XML source:<p>
	 * <pre>
	 * ...&lt;root&gt;&lt;branch&gt;123&lt;/branch&gt;&lt;/root&gt;...
	 * </pre>
	 * and assuming that <code>parentNode</code> points to the <code>&lt;root&gt;</code> element,
	 * the following call:<p>
	 * <pre>
	 * getChildValue(parentNode, "branch");
	 * </pre>
	 * returns:<p>
	 * <code>123</code><p>
	 * @param parentNode Parent DOM node
	 * @param childName Child node tag
	 * @return XML 'value' of the child node (trimmed content between <childName> and </childName>
	 * tags)
	 * @throws RrdException Thrown if no such child node exists.
	 */
	protected int getChildValueAsInt(Node parentNode, String childName)
		throws RrdException, NumberFormatException {
		String valueStr = getChildValue(parentNode, childName);
		return Integer.parseInt(valueStr);
	}

	/**
	 * Returns the 'value' of the child node with the given parent and the given tag name.
	 * For example, in a DOM-tree created from the following XML source:<p>
	 * <pre>
	 * ...&lt;root&gt;&lt;branch&gt;123&lt;/branch&gt;&lt;/root&gt;...
	 * </pre>
	 * and assuming that <code>parentNode</code> points to the <code>&lt;root&gt;</code> element,
	 * the following call:<p>
	 * <pre>
	 * getChildValue(parentNode, "branch");
	 * </pre>
	 * returns:<p>
	 * <code>123</code><p>
	 * @param parentNode Parent DOM node
	 * @param childName Child node tag
	 * @return XML 'value' of the child node (trimmed content between <childName> and </childName>
	 * tags)
	 * @throws RrdException Thrown if no such child node exists.
	 */
	protected long getChildValueAsLong(Node parentNode, String childName)
		throws RrdException, NumberFormatException {
		String valueStr = getChildValue(parentNode, childName);
		return Long.parseLong(valueStr);
	}

	/**
	 * Returns the 'value' of the child node with the given parent and the given tag name.
	 * For example, in a DOM-tree created from the following XML source:<p>
	 * <pre>
	 * ...&lt;root&gt;&lt;branch&gt;123.45&lt;/branch&gt;&lt;/root&gt;...
	 * </pre>
	 * and assuming that <code>parentNode</code> points to the <code>&lt;root&gt;</code> element,
	 * the following call:<p>
	 * <pre>
	 * getChildValue(parentNode, "branch");
	 * </pre>
	 * returns:<p>
	 * <code>123.45</code><p>
	 * @param parentNode Parent DOM node
	 * @param childName Child node tag
	 * @return XML 'value' of the child node (trimmed content between <childName> and </childName>
	 * tags)
	 * @throws RrdException Thrown if no such child node exists.
	 */
	protected double getChildValueAsDouble(Node parentNode, String childName) throws RrdException {
		String valueStr = getChildValue(parentNode, childName);
		return Util.parseDouble(valueStr);
	}
}
