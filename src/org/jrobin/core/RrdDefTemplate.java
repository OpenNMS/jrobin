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

import org.xml.sax.InputSource;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.File;

/**
 * Class to represent RrdDef XML template. Use this class to produce similar RRD definitions
 * (RrdDef objects) from the same XML template. Use <code>${placeholder_name}</code> placeholders
 * in the XML source to mark XML code which will be modified (replaced) at run time.<p>
 *
 * Here is a self-explaining example of a valid XML definition:<p>
 *
 * <pre><code>
 * &lt;rrd_def&gt;
 *     &lt;path&gt;${path}&lt;/path&gt;
 *     &lt;!-- not mandatory --&gt;
 *     &lt;start&gt;1000123456&lt;/start&gt;
 *     &lt;!-- not mandatory --&gt;
 *     &lt;step&gt;${step}&lt;/step&gt;
 *     &lt;!-- at least one datasource must be supplied --&gt;
 *     &lt;datasource&gt;
 *         &lt;name&gt;input&lt;/name&gt;
 *         &lt;type&gt;COUNTER&lt;/type&gt;
 *         &lt;heartbeat&gt;300&lt;/heartbeat&gt;
 *         &lt;min&gt;0&lt;/min&gt;
 *         &lt;max&gt;U&lt;/max&gt;
 *     &lt;/datasource&gt;
 *     &lt;datasource&gt;
 *         &lt;name&gt;temperature&lt;/name&gt;
 *         &lt;type&gt;GAUGE&lt;/type&gt;
 *         &lt;heartbeat&gt;400&lt;/heartbeat&gt;
 *         &lt;min&gt;U&lt;/min&gt;
 *         &lt;max&gt;1000&lt;/max&gt;
 *     &lt;/datasource&gt;
 *     &lt;!-- at least one archive must be supplied --&gt;
 *     &lt;archive&gt;
 *         &lt;cf&gt;AVERAGE&lt;/cf&gt;
 *         &lt;xff&gt;0.5&lt;/xff&gt;
 *         &lt;steps&gt;1&lt;/steps&gt;
 *         &lt;rows&gt;${rows}&lt;/rows&gt;
 *     &lt;/archive&gt;
 *     &lt;archive&gt;
 *         &lt;cf&gt;MAX&lt;/cf&gt;
 *         &lt;xff&gt;0.6&lt;/xff&gt;
 *         &lt;steps&gt;6&lt;/steps&gt;
 *         &lt;rows&gt;7000&lt;/rows&gt;
 *     &lt;/archive&gt;
 * &lt;/rrd_def&gt;
 * </code></pre>
 * XML template can be embedded in a String or a file - the class provides constructors in both
 * cases. <p>
 *
 * Note that the above XML definition contains three placeholders (<code>${path}</code>,
 * <code>${step}</code> and <code>${rows}</code>) - these placeholders must be replaced with
 * real values before the RrdDef object is requested by calling {@link #getRrdDef getRrdDef()}
 * method. To replace placeholders with real values at run time use inhereted
 * (and overloaded) public {@link XmlTemplate#setMapping(String, String) setMapping()}
 * methods.<p>
 *
 * You are free to use the same template object to create as many RrdDef objects as needed
 * (probably with different placeholder-value mappings).<p>
 *
 * Here is an example how to create two different RRD files using the template given above:<p>
 *
 * <pre>
 * // 'template.xml' file contains XML template already specified
 * File file = new File("template.xml");
 * RrdDefTemplate t = new RrdDefTemplate(file);
 *
 * // replace ${path} placeholder with the real value (test1.rrd)
 * t.setMapping("path", "test1.rrd");
 *
 * // replace ${step} placeholder with the real value (600)
 * t.setMapping("step", 600);
 *
 * // replace ${rows} placeholder with the real value (800)
 * t.setMapping("rows", 800);
 *
 * // get RrdDef from the template object...
 * RrdDef def = t.getRrdDef();
 *
 * // ...and use it to construct the first RRD file
 * RrdDb rrd = new RrdDb(def); rrd.close();
 *
 * // note that all mappings are still active
 * // change the value for some (or all) placeholders
 * // to construct the second database
 * // with different parameters
 * t.setMapping("path", "test2.rrd");
 * def = t.getRrdDef();
 *
 * // the second RRD file will be also created with step=600, rows=800
 * rrd = new RrdDb(def); rrd.close();
 * </pre>
 */
public class RrdDefTemplate extends XmlTemplate {
	/**
	 * Creates RrdDefTemplate object from any parsable XML input source. Read general information
	 * for this class to find an example of a properly formatted RrdDef XML source.
	 * @param xmlInputSource Xml input source
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of XML related error (parsing error, for example)
	 */
	public RrdDefTemplate(InputSource xmlInputSource) throws IOException, RrdException {
		super(xmlInputSource);
	}

	/**
	 * Creates RrdDefTemplate object from the string containing XML template.
	 * Read general information for this class to see an example of a properly formatted XML source.
	 * @param xmlString String containing XML template
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of XML related error (parsing error, for example)
	 */
	public RrdDefTemplate(String xmlString) throws IOException, RrdException {
		super(xmlString);
	}

    /**
	 * Creates RrdDefTemplate object from the file containing XML template.
	 * Read general information for this class to see an example of a properly formatted XML source.
	 * @param xmlFile File object representing file with XML template
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown in case of XML related error (parsing error, for example)
	 */
	public RrdDefTemplate(File xmlFile) throws IOException, RrdException {
		super(xmlFile);
	}

    /**
	 * Returns RrdDef object constructed from the underlying XML template. Before this method
	 * is called, values for all non-optional placeholders must be supplied. To specify
	 * placeholder values at runtime, use some of the overloaded
	 * {@link XmlTemplate#setMapping(String, String) setMapping()} methods. Once this method
	 * returns, all placeholder values are preserved. To remove them all, call inhereted
	 * {@link XmlTemplate#clearValues() clearValues()} method explicitly.<p>
	 *
	 * @return RrdDef object constructed from the underlying XML template,
	 * with all placeholders replaced with real values. This object can be passed to the constructor
	 * of the new RrdDb object.
	 * @throws RrdException Thrown (in most cases) if the value for some placeholder
	 * was not supplied through {@link XmlTemplate#setMapping(String, String) setMapping()}
	 * method call
	 */
	public RrdDef getRrdDef() throws RrdException {
		if (!root.getTagName().equals("rrd_def")) {
			throw new RrdException("XML definition must start with <rrd_def>");
		}
		// PATH must be supplied or exception is thrown
		String path = getChildValue(root, "path");
		RrdDef rrdDef = new RrdDef(path);
		try {
			long start = getChildValueAsLong(root, "start");
			rrdDef.setStartTime(start);
		} catch (RrdException e) {
			// START is not mandatory
		}
		try {
			long step = getChildValueAsLong(root, "step");
			rrdDef.setStep(step);
		} catch (RrdException e) {
			// STEP is not mandatory
		}
		// datsources
		Node[] dsNodes = getChildNodes(root, "datasource");
		for (int i = 0; i < dsNodes.length; i++) {
			String name = getChildValue(dsNodes[i], "name");
			String type = getChildValue(dsNodes[i], "type");
			long heartbeat = getChildValueAsLong(dsNodes[i], "heartbeat");
			double min = getChildValueAsDouble(dsNodes[i], "min");
			double max = getChildValueAsDouble(dsNodes[i], "max");
			rrdDef.addDatasource(name, type, heartbeat, min, max);
		}
		// archives
		Node[] arcNodes = getChildNodes(root, "archive");
		for (int i = 0; i < arcNodes.length; i++) {
			String consolFun = getChildValue(arcNodes[i], "cf");
			double xff = getChildValueAsDouble(arcNodes[i], "xff");
			int steps = getChildValueAsInt(arcNodes[i], "steps");
			int rows = getChildValueAsInt(arcNodes[i], "rows");
			rrdDef.addArchive(consolFun, xff, steps, rows);
		}
		return rrdDef;
	}


	public static void main(String[] args) throws RrdException, IOException {
		File f = new File("work/test.xml");
		RrdDefTemplate t = new RrdDefTemplate(f);

		t.setMapping("path", "test1.rrd");
		t.setMapping("step", 310);
		t.setMapping("hb", 123);

		RrdDef def = t.getRrdDef();
		System.out.println(def.dump());
	}

}
