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
package org.jrobin.graph;

import org.jrobin.core.XmlTemplate;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.File;
import java.awt.*;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Class used to create an arbitrary number of RrdGraphDefs (graph definition) objects
 * from a single XML template. XML template can be supplied as an XML InputSource,
 * XML file or XML formatted string.<p>
 *
 * Here is an example of a properly formatted XML template with all available options in it
 * (unwanted options can be removed):<p>
 * <pre>
 * &lt;rrd_graph_def&gt;
 *     &lt;span&gt;
 *         &lt;!-- ISO FORMAT: yyyy-MM-dd HH:mm:ss --&gt;
 *         &lt;start&gt;2004-02-27 13:35:00&lt;/start&gt;
 *         &lt;!-- timestamp in seconds is also allowed --&gt;
 *         &lt;end&gt;1234567890&lt;/end&gt;
 *     &lt;/span&gt;
 *     &lt;options&gt;
 *         &lt;anti_aliasing&gt;true&lt;/anti_aliasing&gt;
 *         &lt;arrow_color&gt;#FF0000&lt;/arrow_color&gt;
 *         &lt;axis_color&gt;#00FF00&lt;/axis_color&gt;
 *         &lt;back_color&gt;#00FF00&lt;/back_color&gt;
 *         &lt;background&gt;#FFFFFF&lt;/background&gt;
 *         &lt;base_value&gt;1024&lt;/base_value&gt;
 *         &lt;canvas&gt;#112211&lt;/canvas&gt;
 *         &lt;left_padding&gt;55&lt;/left_padding&gt;
 *         &lt;default_font&gt;
 *             &lt;name&gt;Times&lt;/name&gt;
 *             &lt;style&gt;BOLD ITALIC&lt;/style&gt;
 *             &lt;size&gt;15&lt;/size&gt;
 *         &lt;/default_font&gt;
 *         &lt;default_font_color&gt;#000000&lt;/default_font_color&gt;
 *         &lt;frame_color&gt;#0000FF&lt;/frame_color&gt;
 *         &lt;front_grid&gt;true&lt;/front_grid&gt;
 *         &lt;grid_range&gt;
 *             &lt;lower&gt;100&lt;/lower&gt;
 *             &lt;upper&gt;200&lt;/upper&gt;
 *             &lt;rigid&gt;false&lt;/rigid&gt;
 *         &lt;/grid_range&gt;
 *         &lt;grid_x&gt;true&lt;/grid_x&gt;
 *         &lt;grid_y&gt;false&lt;/grid_y&gt;
 *         &lt;border&gt;
 *             &lt;color&gt;#00FFFF&lt;/color&gt;
 *             &lt;width&gt;2&lt;/width&gt;
 *         &lt;/border&gt;
 *         &lt;major_grid_color&gt;#00FF00&lt;/major_grid_color&gt;
 *         &lt;major_grid_x&gt;true&lt;/major_grid_x&gt;
 *         &lt;major_grid_y&gt;false&lt;/major_grid_y&gt;
 *         &lt;minor_grid_color&gt;#00FFFF&lt;/minor_grid_color&gt;
 *         &lt;minor_grid_x&gt;true&lt;/minor_grid_x&gt;
 *         &lt;minor_grid_y&gt;false&lt;/minor_grid_y&gt;
 *         &lt;overlay&gt;overlay_image.png&lt;/overlay&gt;
 *         &lt;show_legend&gt;true&lt;/show_legend&gt;
 *         &lt;show_signature&gt;false&lt;/show_signature&gt;
 *         &lt;time_axis&gt;
 *             &lt;!-- ALLOWED TIME UNITS: SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR --&gt;
 *             &lt;min_grid_time_unit&gt;HOUR&lt;/min_grid_time_unit&gt;
 *             &lt;min_grid_unit_steps&gt;4&lt;/min_grid_unit_steps&gt;
 *             &lt;maj_grid_time_unit&gt;DAY&lt;/maj_grid_time_unit&gt;
 *             &lt;maj_grid_unit_steps&gt;2&lt;/maj_grid_unit_steps&gt;
 *             &lt;date_format&gt;HH:mm&lt;/date_format&gt;
 *             &lt;center_labels&gt;true&lt;/center_labels&gt;
 *         &lt;/time_axis&gt;
 *         &lt;time_axis_label&gt;time&lt;/time_axis_label&gt;
 *         &lt;title&gt;Graph title&lt;/title&gt;
 *         &lt;title_font&gt;
 *             &lt;name&gt;Verdana&lt;/name&gt;
 *             &lt;style&gt;BOLD&lt;/style&gt;
 *             &lt;size&gt;17&lt;/size&gt;
 *         &lt;/title_font&gt;
 *         &lt;title_font_color&gt;#FF0000&lt;/title_font_color&gt;
 *         &lt;units_exponent&gt;6&lt;/units_exponent&gt;
 *         &lt;value_axis&gt;
 *             &lt;grid_step&gt;100&lt;/grid_step&gt;
 *             &lt;label_step&gt;200&lt;/label_step&gt;
 *         &lt;/value_axis&gt;
 *         &lt;vertical_label&gt;voltage [V]&lt;/vertical_label&gt;
 *     &lt;/options&gt;
 *     &lt;datasources&gt;
 *         &lt;def&gt;
 *             &lt;name&gt;input&lt;/name&gt;
 *             &lt;rrd&gt;test1.rrd&lt;/rrd&gt;
 *             &lt;source&gt;inOctets&lt;/source&gt;
 *             &lt;cf&gt;AVERAGE&lt;/cf&gt;
 *         &lt;/def&gt;
 *         &lt;def&gt;
 *             &lt;name&gt;output&lt;/name&gt;
 *             &lt;rrd&gt;test2.rrd&lt;/rrd&gt;
 *             &lt;source&gt;outOctets&lt;/source&gt;
 *             &lt;cf&gt;MAX&lt;/cf&gt;
 *         &lt;/def&gt;
 *         &lt;def&gt;
 *             &lt;name&gt;input8&lt;/name&gt;
 *             &lt;rpn&gt;input,8,*&lt;/rpn&gt;
 *         &lt;/def&gt;
 *         &lt;def&gt;
 *             &lt;name&gt;output8&lt;/name&gt;
 *             &lt;rpn&gt;output,8,*,-1,*&lt;/rpn&gt;
 *         &lt;/def&gt;
 *     &lt;/datasources&gt;
 *     &lt;graph&gt;
 *         &lt;area&gt;
 *             &lt;datasource&gt;input&lt;/datasource&gt;
 *             &lt;color&gt;#FF0000&lt;/color&gt;
 *             &lt;legend&gt;Input traffic&lt;/legend&gt;
 *         &lt;/area&gt;
 *         &lt;area&gt;
 *             &lt;datasource&gt;output&lt;/datasource&gt;
 *             &lt;color&gt;#00FF00&lt;/color&gt;
 *             &lt;legend&gt;Output traffic&lt;/legend&gt;
 *         &lt;/area&gt;
 *         &lt;stack&gt;
 *             &lt;datasource&gt;input8&lt;/datasource&gt;
 *             &lt;color&gt;#AA00AA&lt;/color&gt;
 *             &lt;legend&gt;Stacked input@r&lt;/legend&gt;
 *         &lt;/stack&gt;
 *         &lt;line&gt;
 *             &lt;datasource&gt;input&lt;/datasource&gt;
 *             &lt;color&gt;#AB7777&lt;/color&gt;
 *             &lt;legend&gt;Input traffic@l&lt;/legend&gt;
 *         &lt;/line&gt;
 *         &lt;line&gt;
 *             &lt;datasource&gt;output&lt;/datasource&gt;
 *             &lt;color&gt;#AA00AA&lt;/color&gt;
 *             &lt;legend&gt;Output traffic@r&lt;/legend&gt;
 *             &lt;width&gt;2&lt;/width&gt;
 *         &lt;/line&gt;
 *         &lt;area&gt;
 *             &lt;time1&gt;2004-02-25 12:00:01&lt;/time1&gt;
 *             &lt;time2&gt;1000222333&lt;/time2&gt;
 *             &lt;value1&gt;1001.23&lt;/value1&gt;
 *             &lt;value2&gt;2765.45&lt;/value2&gt;
 *             &lt;color&gt;#AABBCC&lt;/color&gt;
 *             &lt;legend&gt;simeple two point area&lt;/legend&gt;
 *         &lt;/area&gt;
 *         &lt;line&gt;
 *             &lt;time1&gt;1000111444&lt;/time1&gt;
 *             &lt;time2&gt;2004-02-25 12:00:01&lt;/time2&gt;
 *             &lt;value1&gt;1009.23&lt;/value1&gt;
 *             &lt;value2&gt;9002.45&lt;/value2&gt;
 *             &lt;color&gt;#AABB33&lt;/color&gt;
 *             &lt;legend&gt;simple two point line&lt;/legend&gt;
 *             &lt;width&gt;5&lt;/width&gt;
 *         &lt;/line&gt;
 *         &lt;gprint&gt;
 *             &lt;datasource&gt;input&lt;/datasource&gt;
 *             &lt;cf&gt;AVERAGE&lt;/cf&gt;
 *             &lt;format&gt;Average input: @2@c&lt;/format&gt;
 *         &lt;/gprint&gt;
 *         &lt;gprint&gt;
 *             &lt;datasource&gt;output&lt;/datasource&gt;
 *             &lt;cf&gt;MAX&lt;/cf&gt;
 *             &lt;format&gt;Average output: @2@r&lt;/format&gt;
 *         &lt;/gprint&gt;
 *         &lt;hrule&gt;
 *             &lt;value&gt;1234.5678&lt;/value&gt;
 *             &lt;color&gt;#112233&lt;/color&gt;
 *             &lt;legend&gt;horizontal rule&lt;/legend&gt;
 *             &lt;width&gt;3&lt;/width&gt;
 *         &lt;/hrule&gt;
 *         &lt;vrule&gt;
 *             &lt;time&gt;2004-02-22 17:43:57&lt;/time&gt;
 *             &lt;color&gt;#112299&lt;/color&gt;
 *             &lt;legend&gt;vertical rule&lt;/legend&gt;
 *             &lt;width&gt;6&lt;/width&gt;
 *         &lt;/vrule&gt;
 *         &lt;comment&gt;Created with JRobin&lt;/comment&gt;
 *     &lt;/graph&gt;
 * &lt;/rrd_graph_def&gt;
 * </pre>
 * Notes on the template syntax:<p>
 * <ul>
 * <li>There is a strong relation between the XML template syntax and the syntax of
 * {@link RrdGraphDef} class methods. If you are not sure what some XML tag means, check javadoc
 * for the corresponding class.
 * <li>hard-coded timestamps in templates should be long integeres
 * (like: 1000243567) or ISO formatted strings (like: 2004-02-21 12:25:45)
 * <li>all leading and trailing whitespaces are removed
 * <li>use <code>true</code>, <code>on</code>, <code>yes</code>, <code>y</code>,
 * or <code>1</code> to specify boolean <code>true</code> value (anything else will
 * be treated as <code>false</code>).
 * <li>floating point values: anything that cannot be parsed will be treated as Double.NaN
 * (like: U, unknown, 12r.23)
 * <li>use #RRGGBB format to specify colors.
 * <li>comments are allowed.
 * </ul>
 * Any template value (text between <code>&lt;some_tag&gt;</code> and
 * <code>&lt;/some_tag&gt;</code>) can be replaced with
 * a variable of the following form: <code>${variable_name}</code>. Use
 * {@link XmlTemplate#setMapping(String, String) setMapping()} methods from the base class to replace
 * template variables with real values at runtime.<p>
 *
 * Typical usage scenario:<p>
 * <ul>
 * <li>Create your XML template and save it to a file (template.xml, for example)
 * <li>Replace template values with variables if you want to change them during runtime.
 * For example, time span should not be hard-coded in the template - you probably want to create
 * many different graphs with different time spans, but starting from the same XML template.
 * For example, your XML template could start with:
 * <pre>
 * &lt;rrd_graph_def&gt;
 *     &lt;span&gt;
 *         &lt;start&gt;${start}&lt;/start&gt;
 *         &lt;end&gt;${end}&lt;/end&gt;
 *     &lt;/span&gt;
 *     ...
 * </pre>
 * <li>In your Java code, create RrdGraphDefTemplate object using your XML template file:
 * <pre>
 * RrdGraphDefTemplate t = new RrdGraphDefTemplate(new File(template.xml));
 * </pre>
 * <li>Then, specify real values for template variables:
 * <pre>
 * t.setMapping("start", new GregorianCalendar(2004, 2, 25));
 * t.setMapping("end", new GregorianCalendar(2004, 2, 26));
 * </pre>
 * <li>Once all template variables are set, just use the template object to create RrdGraphDef
 * object. This object is actually used to create JRobin grahps:
 * <pre>
 * RrdGraphDef gdef = t.getRrdGraphDef();
 * RrdGraph g = new RrdGraph(gdef);
 * g.saveAsPNG("graph.png");
 * </pre>
 * </ul>
 * You should create new RrdGraphDefTemplate object only once for each XML template. Single template
 * object can be reused to create as many RrdGraphDef objects as needed, with different values
 * specified for template variables. XML synatax check is performed only once - the first graph
 * definition object gets created relatively slowly, but it will be created much faster next time.
 */
public class RrdGraphDefTemplate extends XmlTemplate {
	static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";   // ISO format

	private RrdGraphDef rrdGraphDef;

	/**
	 * Creates template object from any parsable XML source
	 * @param inputSource XML source
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException usually thrown in case of XML related error
	 */
	public RrdGraphDefTemplate(InputSource inputSource) throws IOException, RrdException {
		super(inputSource);
	}

	/**
	 * Creates template object from the file containing XML template code
	 * @param xmlFile file containing XML template
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException usually thrown in case of XML related error
	 */
	public RrdGraphDefTemplate(File xmlFile) throws IOException, RrdException {
		super(xmlFile);
	}

	/**
	 * Creates template object from the string containing XML template code
	 * @param xmlString string containing XML template
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException usually thrown in case of XML related error
	 */
	public RrdGraphDefTemplate(String xmlString) throws IOException, RrdException {
		super(xmlString);
	}

	/**
	 * Creates RrdGraphDef object which can be used to create RrdGraph
	 * object (actual JRobin graphs). Before this method is called, all template variables (if any)
	 * must be resolved (replaced with real values).
	 * See {@link XmlTemplate#setMapping(String, String) setMapping()} method information to
	 * understand how to supply values for template variables.
	 * @return Graph definition which can be used to create RrdGraph object (actual JRobin graphs)
	 * @throws RrdException Thrown if parsed XML template contains invalid (unrecognized) tags
	 */
	public RrdGraphDef getRrdGraphDef() throws RrdException {
		// basic check
		if(!root.getTagName().equals("rrd_graph_def")) {
			throw new RrdException("XML definition must start with <rrd_graph_def>");
		}
		validateOnce(root, new String[] {"span", "options", "datasources", "graph"});
		rrdGraphDef = new RrdGraphDef();
        // traverse all nodes
		Node[] childs = getChildNodes(root);
		for(int i = 0; i < childs.length; i++) {
			// SPAN
			String nodeName = childs[i].getNodeName();
            if(nodeName.equals("span")) {
				resolveSpan(childs[i]);
			}
			// OPTIONS
			else if(nodeName.equals("options")) {
				resolveOptions(childs[i]);
			}
			// DATASOURCES
			else if(nodeName.equals("datasources")) {
				resolveDatasources(childs[i]);
			}
			// GRAPH ELEMENTS
			else if(nodeName.equals("graph")) {
				resolveGraphElements(childs[i]);
			}
		}
		return rrdGraphDef;
	}

	private void resolveGraphElements(Node graphNode) throws RrdException {
		validateOnce(graphNode, new String[] {
			"area", "line", "stack", "gprint", "hrule", "vrule", "comment"
		});
		Node[] childs = getChildNodes(graphNode);
		for(int i = 0; i < childs.length; i++) {
			String nodeName = childs[i].getNodeName();
			if(nodeName.equals("area")) {
				resolveArea(childs[i]);
			}
			else if(nodeName.equals("line")) {
				resolveLine(childs[i]);
			}
			else if(nodeName.equals("stack")) {
				validateOnce(childs[i], new String[] { "datasource", "color", "legend" });
				String datasource = getChildValue(childs[i], "datasource");
				String colorStr = getChildValue(childs[i], "color");
				Color color = Color.decode(colorStr);
				String legend = getChildValue(childs[i], "legend");
				rrdGraphDef.stack(datasource, color, legend);
			}
			else if(nodeName.equals("comment")) {
				String comment = getValue(childs[i]);
				rrdGraphDef.comment(comment);
			}
			else if(nodeName.equals("gprint")) {
				validateOnce(childs[i], new String[] { "datasource", "cf", "format" });
				String datasource = getChildValue(childs[i], "datasource");
				String consolFun = getChildValue(childs[i], "cf");
				String format = getChildValue(childs[i], "format");
				rrdGraphDef.gprint(datasource, consolFun, format);
			}
			else if(nodeName.equals("hrule")) {
				validateOnce(childs[i], new String[] { "value", "color", "legend", "width" });
				double value = getChildValueAsDouble(childs[i], "value");
				String colorStr = getChildValue(childs[i], "color");
				Color color = Color.decode(colorStr);
				String legend = getChildValue(childs[i], "legend");
				int width = 1;
				try {
					width = getChildValueAsInt(childs[i], "width");
				} catch(RrdException e) { }
				rrdGraphDef.hrule(value, color, legend, width);
			}
			else if(nodeName.equals("vrule")) {
				validateOnce(childs[i], new String[] { "time", "color", "legend", "width" });
				String timeStr = getChildValue(childs[i], "time");
				GregorianCalendar gc = resolveTime(timeStr);
				String colorStr = getChildValue(childs[i], "color");
				Color color = Color.decode(colorStr);
				String legend = getChildValue(childs[i], "legend");
				int width = 1;
				try {
					width = getChildValueAsInt(childs[i], "width");
				} catch(RrdException e) { }
				rrdGraphDef.vrule(gc, color, legend, width);
			}
		}
	}

	private void resolveLine(Node lineNode) throws RrdException {
        if(hasChildNode(lineNode, "datasource")) {
			// ordinary line definition
			validateOnce(lineNode, new String[] { "datasource", "color", "legend", "width" });
			String datasource = getChildValue(lineNode, "datasource");
			String colorStr = getChildValue(lineNode, "color");
			Color color = Color.decode(colorStr);
			String legend = getChildValue(lineNode, "legend");
			// line width is not mandatory
			int width = 1;
			try {
				width = getChildValueAsInt(lineNode, "width");
			} catch(RrdException e) { }
			rrdGraphDef.line(datasource, color, legend, width);
		}
		else if(hasChildNode(lineNode, "time1")) {
			// two point definition
			validateOnce(lineNode, new String[] {
				"time1", "time2", "value1", "value2", "color", "legend", "width"
			});
			String t1str = getChildValue(lineNode, "time1");
			GregorianCalendar gc1 = resolveTime(t1str);
			String t2str = getChildValue(lineNode, "time2");
			GregorianCalendar gc2 = resolveTime(t2str);
			double v1 = getChildValueAsDouble(lineNode, "value1");
			double v2 = getChildValueAsDouble(lineNode, "value2");
            String colorStr = getChildValue(lineNode, "color");
			Color color = Color.decode(colorStr);
			String legend = getChildValue(lineNode, "legend");
			int width = 1;
			try {
				width = getChildValueAsInt(lineNode, "width");
			} catch(RrdException e) { }
			rrdGraphDef.line(gc1, v1, gc2, v2, color, legend, width);
		}
		else {
			throw new RrdException("Unrecognized <line> format");
		}
	}

	private void resolveArea(Node areaNode) throws RrdException {
        if(hasChildNode(areaNode, "datasource")) {
			validateOnce(areaNode, new String[] { "datasource", "color", "legend" });
			// ordinary area definition
			String datasource = getChildValue(areaNode, "datasource");
			String colorStr = getChildValue(areaNode, "color");
			Color color = Color.decode(colorStr);
			String legend = getChildValue(areaNode, "legend");
			rrdGraphDef.area(datasource, color, legend);
		}
		else if(hasChildNode(areaNode, "time1")) {
			// two point definition
			validateOnce(areaNode, new String[] {
				"time1", "time2", "value1", "value2", "color", "legend", "width"
			});
			String t1str = getChildValue(areaNode, "time1");
			GregorianCalendar gc1 = resolveTime(t1str);
			String t2str = getChildValue(areaNode, "time2");
			GregorianCalendar gc2 = resolveTime(t2str);
			double v1 = getChildValueAsDouble(areaNode, "value1");
			double v2 = getChildValueAsDouble(areaNode, "value2");
            String colorStr = getChildValue(areaNode, "color");
			Color color = Color.decode(colorStr);
			String legend = getChildValue(areaNode, "legend");
			rrdGraphDef.area(gc1, v1, gc2, v2, color, legend);
		}
		else {
			throw new RrdException("Unrecognized <area> format");
		}
	}

	private void resolveDatasources(Node dsNode) throws RrdException {
		validateOnce(dsNode, new String[] { "def" });
		Node[] nodes = getChildNodes(dsNode, "def");
		for(int i = 0; i < nodes.length; i++) {
			if(hasChildNode(nodes[i], "rrd")) {
				// RRD datasource
				validateOnce(nodes[i], new String[] {"name", "rrd", "source", "cf"});
				String name = getChildValue(nodes[i], "name");
            	String rrd = getChildValue(nodes[i], "rrd");
				String dsName = getChildValue(nodes[i], "source");
				String consolFun = getChildValue(nodes[i], "cf");
				rrdGraphDef.datasource(name, rrd, dsName, consolFun);
			}
			else if(hasChildNode(nodes[i], "rpn")) {
				// RPN datasource
				validateOnce(nodes[i], new String[] {"name", "rpn"});
				String name = getChildValue(nodes[i], "name");
				String rpn = getChildValue(nodes[i], "rpn");
				rrdGraphDef.datasource(name, rpn);
			}
			else {
				throw new RrdException("Unrecognized <def> format");
			}
		}
	}

	private void resolveOptions(Node rootOptionNode) throws RrdException {
		validateOnce(rootOptionNode, new String[] {
			"anti_aliasing", "arrow_color", "axis_color", "back_color", "background",
			"base_value", "canvas", "left_padding", "default_font",	"default_font_color",
			"frame_color", "front_grid", "grid_range", "grid_x", "grid_y", "border",
			"major_grid_color", "major_grid_x", "major_grid_y",
			"minor_grid_color", "minor_grid_x", "minor_grid_y",
			"overlay", "show_legend", "show_signature", "time_axis", "time_axis_label",
			"title", "title_font", "title_font_color", "units_exponent", "value_axis",
			"vertical_label"
		});
		Node[] optionNodes = getChildNodes(rootOptionNode);
		for(int i = 0; i < optionNodes.length; i++) {
			String option = optionNodes[i].getNodeName();
			Node optionNode = optionNodes[i];
			// ANTI ALIASING
			if(option.equals("anti_aliasing")) {
				boolean antiAliasing = getValueAsBoolean(optionNode);
				rrdGraphDef.setAntiAliasing(antiAliasing);
			}
			// ARROW COLOR
			else if(option.equals("arrow_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setArrowColor(Color.decode(colorStr));
			}
			// AXIS COLOR
			else if(option.equals("axis_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setAxisColor(Color.decode(colorStr));
			}
			// BACK COLOR
			else if(option.equals("back_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setBackColor(Color.decode(colorStr));
			}
			// BACKGROUND
			else if(option.equals("background")) {
				String backgroundFile = getValue(optionNode);
				rrdGraphDef.setBackground(backgroundFile);
			}
			// BASE VALUE
			else if(option.equals("base_value")) {
				double baseValue = getValueAsDouble(optionNode);
				rrdGraphDef.setBaseValue(baseValue);
			}
			// CANVAS
			else if(option.equals("canvas")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setCanvasColor(Color.decode(colorStr));
			}
			// LEFT PADDING
			else if(option.equals("left_padding")) {
				int padding = getValueAsInt(optionNode);
				rrdGraphDef.setChartLeftPadding(padding);
			}
			// DEFAULT FONT
			else if(option.equals("default_font")) {
				Font f = resolveFont(optionNode);
				rrdGraphDef.setTitleFont(f);
			}
			// DEFAULT FONT COLOR
			else if(option.equals("default_font_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setDefaultFontColor(Color.decode(colorStr));
			}
			// FRAME COLOR
			else if(option.equals("frame_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setFrameColor(Color.decode(colorStr));
			}
			// FRONT GRID
			else if(option.equals("front_grid")) {
				boolean frontGrid = getValueAsBoolean(optionNode);
				rrdGraphDef.setFrontGrid(frontGrid);
			}
			// GRID RANGE
			else if(option.equals("grid_range")) {
				validateOnce(optionNode, new String[] { "lower", "upper", "rigid" });
				double lower = getChildValueAsDouble(optionNode, "lower");
				double upper = getChildValueAsDouble(optionNode, "upper");
				boolean rigid = getChildValueAsBoolean(optionNode, "rigid");
				rrdGraphDef.setGridRange(lower, upper, rigid);
			}
			// GRID X?
			else if(option.equals("grid_x")) {
				boolean gx = getValueAsBoolean(optionNode);
				rrdGraphDef.setGridX(gx);
			}
			// GRID Y?
			else if(option.equals("grid_y")) {
				boolean gy = getValueAsBoolean(optionNode);
				rrdGraphDef.setGridY(gy);
			}
			// BORDER
			else if(option.equals("border")) {
				validateOnce(optionNode, new String[] {"color", "width"});
				String colorStr = getChildValue(optionNode, "color");
				int width = getChildValueAsInt(optionNode, "width");
				rrdGraphDef.setImageBorder(Color.decode(colorStr), width);
			}
			// MAJOR GRID COLOR
			else if(option.equals("major_grid_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setMajorGridColor(Color.decode(colorStr));
			}
			// MAJOR GRID X?
			else if(option.equals("major_grid_x")) {
				boolean gx = getValueAsBoolean(optionNode);
				rrdGraphDef.setMajorGridX(gx);
			}
			// MAJOR GRID Y?
			else if(option.equals("major_grid_y")) {
				boolean gy = getValueAsBoolean(optionNode);
				rrdGraphDef.setMajorGridY(gy);
			}
			// MINOR GRID COLOR
			else if(option.equals("minor_grid_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setMinorGridColor(Color.decode(colorStr));
			}
			// MINOR GRID X?
			else if(option.equals("minor_grid_x")) {
				boolean gx = getValueAsBoolean(optionNode);
				rrdGraphDef.setMinorGridX(gx);
			}
			// MINOR GRID Y?
			else if(option.equals("minor_grid_y")) {
				boolean gy = getValueAsBoolean(optionNode);
				rrdGraphDef.setMinorGridY(gy);
			}
			// OVERLAY
			else if(option.equals("overlay")) {
				String overlay = getValue(optionNode);
				rrdGraphDef.setOverlay(overlay);
			}
			// SHOW LEGEND?
			else if(option.equals("show_legend")) {
				boolean show = getValueAsBoolean(optionNode);
				rrdGraphDef.setShowLegend(show);
			}
			// SHOW SIGNATURE?
			else if(option.equals("show_signature")) {
				boolean show = getValueAsBoolean(optionNode);
				rrdGraphDef.setShowSignature(show);
			}
			// TIME AXIS
			else if(option.equals("time_axis")) {
				validateOnce(optionNode, new String[] {
					"min_grid_time_unit", "min_grid_unit_steps", "maj_grid_time_unit",
					"maj_grid_unit_steps", "date_format", "center_labels"
				});
				int unit1 = resolveUnit(getChildValue(optionNode, "min_grid_time_unit"));
				int step1 = getChildValueAsInt(optionNode, "min_grid_unit_steps");
				int unit2 = resolveUnit(getChildValue(optionNode, "maj_grid_time_unit"));
				int step2 = getChildValueAsInt(optionNode, "maj_grid_unit_steps");
				String format = getChildValue(optionNode, "date_format");
				boolean center = getChildValueAsBoolean(optionNode, "center_labels");
				rrdGraphDef.setTimeAxis(unit1, step1, unit2, step2, format, center);
			}
			// TIME AXIS LABEL
			else if(option.equals("time_axis_label")) {
				String label = getValue(optionNode);
				rrdGraphDef.setTimeAxisLabel(label);
			}
			// TITLE
			else if(option.equals("title")) {
				String title = getValue(optionNode);
				rrdGraphDef.setTitle(title);
			}
			// TITLE FONT
			else if(option.equals("title_font")) {
				Font f = resolveFont(optionNode);
				rrdGraphDef.setTitleFont(f);
			}
			// TITLE FONT COLOR
			else if(option.equals("title_font_color")) {
				String colorStr = getValue(optionNode);
				rrdGraphDef.setTitleFontColor(Color.decode(colorStr));
			}
			// UNITS EXPONENT
			else if(option.equals("units_exponent")) {
				int exp = getValueAsInt(optionNode);
				rrdGraphDef.setUnitsExponent(exp);
			}
			// VALUE AXIS
			else if(option.equals("value_axis")) {
				validateOnce(optionNode, new String[] {"grid_step", "label_step"});
				double gridStep = getChildValueAsDouble(optionNode, "grid_step");
				double labelStep = getChildValueAsDouble(optionNode, "label_step");
				rrdGraphDef.setValueAxis(gridStep, labelStep);
			}
			// VERTICAL LABEL
			else if(option.equals("vertical_label")) {
				String label = getValue(optionNode);
				rrdGraphDef.setVerticalLabel(label);
			}
		}
	}

	private int resolveUnit(String unit) {
		if(unit.equalsIgnoreCase("second")) {
			return TimeAxisUnit.SECOND;
		}
		else if(unit.equalsIgnoreCase("minute")) {
			return TimeAxisUnit.MINUTE;
		}
		else if(unit.equalsIgnoreCase("hour")) {
			return TimeAxisUnit.HOUR;
		}
		else if(unit.equalsIgnoreCase("day")) {
			return TimeAxisUnit.DAY;
		}
		else if(unit.equalsIgnoreCase("week")) {
			return TimeAxisUnit.WEEK;
		}
		else if(unit.equalsIgnoreCase("month")) {
			return TimeAxisUnit.MONTH;
		}
		else if(unit.equalsIgnoreCase("year")) {
			return TimeAxisUnit.YEAR;
		}
		else {
			throw new IllegalArgumentException("Invalid unit specified: " + unit);
		}
	}

	private void resolveSpan(Node spanNode) throws RrdException {
		validateOnce(spanNode, new String[] {"start", "end"});
		String startStr = getChildValue(spanNode, "start");
		String endStr = getChildValue(spanNode, "end");
		GregorianCalendar gc1 = resolveTime(startStr);
		GregorianCalendar gc2 = resolveTime(endStr);
		rrdGraphDef.setTimePeriod(gc1, gc2);
	}

	private Font resolveFont(Node fontNode) throws RrdException {
		validateOnce(fontNode, new String[] {"name", "style", "size"});
        String name = getChildValue(fontNode, "name");
		String style = getChildValue(fontNode, "style");
		int size = getChildValueAsInt(fontNode, "size");
		int stl = Font.PLAIN;
		if(style.equalsIgnoreCase("BOLD")) {
			stl = Font.BOLD;
		}
		else if(style.equalsIgnoreCase("ITALIC")) {
			stl = Font.ITALIC;
		}
		else if(style.equalsIgnoreCase("BOLDITALIC") ||
			style.equalsIgnoreCase("ITALICBOLD") ||
			style.equalsIgnoreCase("BOLD ITALIC") ||
			style.equalsIgnoreCase("ITALIC BOLD")) {
			stl = Font.ITALIC + Font.BOLD;
		}
		return new Font(name, stl, size);
	}

	private GregorianCalendar resolveTime(String timeStr) {
		// try to parse it as long
		try {
			long timestamp = Long.parseLong(timeStr);
			return Util.getGregorianCalendar(timestamp);
		} catch (NumberFormatException e) { }
		// not a long timestamp, try to parse it as data
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
		df.setLenient(false);
		try {
			Date date = df.parse(timeStr);
            return Util.getGregorianCalendar(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Time/date not in " + DATE_FORMAT +
				" format: " + timeStr);
		}
	}

	public static void main(String[] args) throws IOException, RrdException {
		File f = new File("work/test2.xml");
		RrdGraphDefTemplate t = new RrdGraphDefTemplate(f);
		t.setMapping("date", new Date());
		t.getRrdGraphDef();
	}
}
