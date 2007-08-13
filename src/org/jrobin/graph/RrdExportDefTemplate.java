/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package org.jrobin.graph;

import org.xml.sax.InputSource;
import org.jrobin.core.RrdException;
import org.jrobin.core.XmlTemplate;
import org.jrobin.core.Util;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.File;
import java.util.GregorianCalendar;

/**
 * <p>Class used to create a RrdExportDef (export) object
 * from a single XML template. XML template can be supplied as an XML InputSource,
 * XML file or XML formatted string.<p>
 *
 * <p>Below is an exmple XML template, not all options are required:</p>
 *
 * <xmp>
 * <rrd_export_def>
 *	<span>
 *		<start>${start}</start>
 *		<end>${end}</end>
 *	</span>
 *  <options>
 * 		<resolution>300</resolution>
 * 		<strict_export>true</strict_export>
 *  </options>
 *	<datasources>
 *		<def>
 *			<name>bytesIn</name>
 *			<rrd>${rrd}</rrd>
 *			<source>ifInOctets</source>
 *			<cf>AVERAGE</cf>
 *		</def>
 *		<def>
 *			<name>bytesOut</name>
 *			<rrd>${rrd}</rrd>
 *			<source>ifOutOctets</source>
 *			<cf>AVERAGE</cf>
 *		</def>
 *		<def>
 *			<name>bitsIn</name>
 *			<rpn>bytesIn,8,*</rpn>
 *		</def>
 *		<def>
 *			<name>bitsOut</name>
 *			<rpn>bytesOut,8,*</rpn>
 *		</def>
 *	</datasources>
 *	<exports>
 *		<export>
 *			<datasource>bitsIn</datasource>
 *			<legend>Incoming traffic</legend>
 *		</export>
 *		<export>
 *			<datasource>bitsOut</datasource>
 *			<legend>Outgoing traffic</legend>
 *		</export>
 *	</exports>
 * </rrd_export_def>
 * </xmp>
 *
 * <p><b>Note:</b> for more information on JRobin XML templates in general, refer to the {@link RrdGraphDefTemplate}</p>
 * <p><b>Note:</b> the <code>RrdExportDefTemplate</code> <code>datasources</code> section can contain all the same options
 * as the corresponding section in {@link RrdGraphDefTemplate}
 * </p>
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
public class RrdExportDefTemplate extends XmlTemplate
{
	private RrdExportDef def;

	/**
	 * Creates template object from any parsable XML source
	 * @param inputSource XML source
	 * @throws java.io.IOException thrown in case of I/O error
	 * @throws org.jrobin.core.RrdException usually thrown in case of XML related error
	 */
	public RrdExportDefTemplate(InputSource inputSource) throws IOException, RrdException {
		super(inputSource);
	}

	/**
	 * Creates template object from the file containing XML template code
	 * @param xmlFile file containing XML template
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException usually thrown in case of XML related error
	 */
	public RrdExportDefTemplate(File xmlFile) throws IOException, RrdException {
		super(xmlFile);
	}

	/**
	 * Creates template object from the string containing XML template code
	 * @param xmlString string containing XML template
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException usually thrown in case of XML related error
	 */
	public RrdExportDefTemplate(String xmlString) throws IOException, RrdException {
		super(xmlString);
	}

	/**
	 * Creates RrdExportDef object which can be used to create RrdExport
	 * object (actual JRobin export). Before this method is called, all template variables (if any)
	 * must be resolved (replaced with real values).
	 * See {@link XmlTemplate#setVariable(String, String) setVariable()} method information to
	 * understand how to supply values for template variables.
	 *
	 * @return Export definition which can be used to create RrdExport object (actual JRobin export)
	 * @throws RrdException Thrown if parsed XML template contains invalid (unrecognized) tags
	 */
	public RrdExportDef getRrdExportDef() throws RrdException
	{
		// basic check
		if( !root.getTagName().equals("rrd_export_def") )
			throw new RrdException("XML definition must start with <rrd_export_def>");

		validateTagsOnlyOnce( root, new String[] {"span", "options", "datasources", "exports"} );
		def = new RrdExportDef();
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
			// EXPORTS
			else if(nodeName.equals("exports")) {
				resolveExports(childs[i]);
			}
		}
		return def;
	}

	private void resolveExports(Node datasourceNode) throws RrdException
	{
		validateTagsOnlyOnce(datasourceNode, new String[] { "export*" });
		Node[] nodes = getChildNodes(datasourceNode, "export");
		for( int i = 0; i < nodes.length; i++ )
		{
			validateTagsOnlyOnce( nodes[i], new String[] { "datasource", "legend" } );
			String ds 		= getChildValue( nodes[i], "datasource" );
			String legend 	= getChildValue( nodes[i], "legend" );

			def.export( ds, legend );
		}
	}

	private void resolveDatasources(Node datasourceNode) throws RrdException
	{
		validateTagsOnlyOnce(datasourceNode, new String[] { "def*", "export_data*" });
		Node[] nodes = getChildNodes(datasourceNode, "def");
		for(int i = 0; i < nodes.length; i++) {
			if(hasChildNode(nodes[i], "rrd"))
			{
				// RRD datasource
				validateTagsOnlyOnce(nodes[i], new String[] {"name", "rrd", "source", "cf", "backend"});
				String name 	= getChildValue(nodes[i], "name");
            	String rrd 		= getChildValue(nodes[i], "rrd");
				String dsName 	= getChildValue(nodes[i], "source");
				String consolFun = getChildValue(nodes[i], "cf");

				if ( Util.Xml.hasChildNode(nodes[i], "backend") )
				{
					String backend = getChildValue( nodes[i], "backend" );
					def.datasource( name, rrd, dsName, consolFun, backend );
				}
				else
					def.datasource(name, rrd, dsName, consolFun);
			}
			else if(hasChildNode(nodes[i], "rpn")) {
				// RPN datasource
				validateTagsOnlyOnce(nodes[i], new String[] {"name", "rpn"});
				String name = getChildValue(nodes[i], "name");
				String rpn 	= getChildValue(nodes[i], "rpn");
				def.datasource(name, rpn);
			}
			else if ( hasChildNode( nodes[i], "cf" ) || hasChildNode( nodes[i], "datasource" ) ) {
				// STATIC AGGREGATED DATASOURCE
				validateTagsOnlyOnce( nodes[i], new String[] {"name", "datasource", "cf"} );
				String name	= getChildValue(nodes[i], "name");
				String ds	= getChildValue(nodes[i], "datasource");
				String cf	= getChildValue(nodes[i], "cf");
				def.datasource( name, ds, cf );
			}
			else {
				throw new RrdException("Unrecognized <def> format");
			}
		}

		nodes = getChildNodes(datasourceNode, "export_data");
		for ( int i = 0; i < nodes.length; i++ )
		{
			validateTagsOnlyOnce( nodes[i], new String[] {"file", "ds_name_prefix", "use_legend_names"} );
			String file 			= getChildValue( nodes[i], "file" );
            String prefix			= "d";
			boolean use_legends		= false;

			if ( Util.Xml.hasChildNode( nodes[i], "ds_name_prefix" ) )
				prefix 			= getChildValue(nodes[i], "ds_name_prefix");

			if ( Util.Xml.hasChildNode( nodes[i], "use_legend_names" ) )
				use_legends 	= getChildValueAsBoolean(nodes[i], "use_legend_names");

			try
			{
				if ( !prefix.equals("d") )
					def.addExportData( new ExportData( new File(file), prefix ) );
				else
					def.addExportData( new ExportData( new File(file), use_legends ) );
			}
			catch ( IOException ioe ) {
				throw new RrdException( ioe );
			}
		}
	}

	private void resolveOptions(Node rootOptionNode) throws RrdException
	{
		validateTagsOnlyOnce( rootOptionNode, new String[] {
			"resolution", "strict_export"
		});

		Node[] optionNodes = getChildNodes(rootOptionNode);
		for( int i = 0; i < optionNodes.length; i++ )
		{
			String option 	= optionNodes[i].getNodeName();
			Node optionNode = optionNodes[i];

			if(option.equals("strict_export"))								// STRICT EXPORT
				def.setStrictExport( getValueAsBoolean(optionNode) );
			else if(option.equals("resolution"))							// RESOLUTION
				def.setResolution( getValueAsInt(optionNode) );
		}
	}

	private void resolveSpan(Node spanNode) throws RrdException
	{
		validateTagsOnlyOnce(spanNode, new String[] {"start", "end"});
		String startStr 		= getChildValue(spanNode, "start");
		String endStr 			= getChildValue(spanNode, "end");
		GregorianCalendar gc1 	= Util.getGregorianCalendar(startStr);
		GregorianCalendar gc2 	= Util.getGregorianCalendar(endStr);
		def.setTimePeriod(gc1, gc2);
	}
}
