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

import javax.xml.parsers.*;

import java.io.*;
import java.awt.Color;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.jrobin.core.RrdException;

/**
 * <p>JRobin</p>
 * 
 * @author Arne Vandamme <cobralord@cherrymon.org>
 */
class XmlGraphDefConvertor extends DefaultHandler
{
	// ----------------------------------------
	private final static Byte BLOCK_ROOT		= new Byte((byte)0);
	private final static Byte BLOCK_GENERAL		= new Byte((byte)1);
	private final static Byte BLOCK_DATASOURCES	= new Byte((byte)2);
	private final static Byte BLOCK_GRAPHING	= new Byte((byte)3);
	
	private final static Byte EL_NONE			= new Byte((byte)0);
	private final static Byte EL_DEF			= new Byte((byte)1);
	private final static Byte EL_CDEF			= new Byte((byte)2);
	private final static Byte EL_GENERAL		= new Byte((byte)3);
	private final static Byte EL_DATASOURCES	= new Byte((byte)4);
	private final static Byte EL_GRAPHING		= new Byte((byte)5);
	private final static Byte EL_DEF_FILE		= new Byte((byte)6);
	private final static Byte EL_DEF_DSNAME		= new Byte((byte)7);
	private final static Byte EL_DEF_CF			= new Byte((byte)8);
	private final static Byte EL_COMMENT		= new Byte((byte)9);
	private final static Byte EL_LINE			= new Byte((byte)10);
	private final static Byte EL_LINE_DS		= new Byte((byte)11);
	private final static Byte EL_PERIOD			= new Byte((byte)12);
	private final static Byte EL_LINE_LEGEND	= new Byte((byte)13);
	private final static Byte EL_LINE_WIDTH		= new Byte((byte)14);
	private final static Byte EL_PERIOD_START	= new Byte((byte)15);
	private final static Byte EL_PERIOD_STOP	= new Byte((byte)16);
	private final static Byte EL_LINE_COLOR		= new Byte((byte)17);
	private final static Byte EL_AREA			= new Byte((byte)18);
	private final static Byte EL_TITLE			= new Byte((byte)19);
	private final static Byte EL_IMGBORDER		= new Byte((byte)20);
	private final static Byte EL_BORDERWIDTH	= new Byte((byte)21);
	private final static Byte EL_COLORS			= new Byte((byte)22);
	private final static Byte EL_CLR_BACKGROUND	= new Byte((byte)23);
	private final static Byte EL_CLR_CANVAS		= new Byte((byte)24);
	private final static Byte EL_SIGNATURE		= new Byte((byte)25);
	
	private RrdGraphDef def;
	private String[] params						= new String[6];
	private Color colorParam					= null;
	private int intParam						= 0;
	private int numParams						= 1;
	
	private java.util.Stack tags				= new java.util.Stack();
	
	private int level							= 0;
	private Byte block							= BLOCK_ROOT;
	private Byte element						= EL_NONE;
	
	XmlGraphDefConvertor( String xml, RrdGraphDef def ) throws RrdException
	{
		this.def 	= def;
		tags.push( EL_NONE );
		
		try
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			
			// Parse the input
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse( new ByteArrayInputStream( xml.getBytes() ), this );
			
		}
		catch ( Exception e ) {
			//throw new RrdException( "Could not parse XML string." );
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)	throws SAXException
	{
		level++;
		Byte parent = (Byte) tags.peek();
		
		if ( qName.equals("color") || ( parent == EL_COLORS && ( qName.equals("background") || qName.equals("canvas") ) ) )
		{
			colorParam = new Color( Integer.parseInt(attrs.getValue("r")), 
									Integer.parseInt(attrs.getValue("g")), 
									Integer.parseInt(attrs.getValue("b")) 
								);
			numParams |= 2;
		}
		
		if ( parent == EL_COLORS )
		{
			if ( qName.equals("background") )
				tags.push( EL_CLR_BACKGROUND );
			else if ( qName.equals("canvas") )
				tags.push( EL_CLR_CANVAS );
		}
		if ( parent == EL_DEF )
		{
			if ( qName.equals("file") )
				tags.push( EL_DEF_FILE );
			else if ( qName.equals("ds-name") )
				tags.push( EL_DEF_DSNAME );
			else if ( qName.equals("cf") )
				tags.push( EL_DEF_CF );
		}
		else if ( parent == EL_LINE || parent == EL_AREA )
		{
			if ( qName.equals("datasource") )
				tags.push( EL_LINE_DS );
			else if ( qName.equals("legend") )
				tags.push( EL_LINE_LEGEND );
			else if ( qName.equals("width") )
				tags.push( EL_LINE_WIDTH );
			else if ( qName.equals("color") )
				tags.push( EL_LINE_COLOR );
		}
		else if ( parent == EL_IMGBORDER ) 
		{
			if ( qName.equals("width") )
				tags.push( EL_BORDERWIDTH );
		}
		else
		{	
			if ( qName.equals("general") )
				tags.push( EL_GENERAL );
			else if ( qName.equals("datasources") )
				tags.push( EL_DATASOURCES );
			else if ( qName.equals("graphing") )
				tags.push( EL_GRAPHING );
			else if ( qName.equals("title") )
				tags.push( EL_TITLE );
			
			else if ( qName.equals("period") )
				tags.push( EL_PERIOD );
			else if ( qName.equals("start") )
				tags.push( EL_PERIOD_START );
			else if ( qName.equals("end") )
				tags.push( EL_PERIOD_STOP );
			else if ( qName.equals("def") )				// DEF 
			{
				params[0] = "def";
				params[1] = attrs.getValue("name");
				
				tags.push( EL_DEF );
			}
			else if ( qName.equals("cdef") ) 			// CDEF
			{
				params[0] = "cdef";
				params[1] = attrs.getValue("name");
				
				tags.push( EL_CDEF );
			}
			else if ( qName.equals("comment") ) 
				tags.push( EL_COMMENT );
			else if ( qName.equals("line") ) 
				tags.push( EL_LINE );
			else if ( qName.equals("area") ) 
				tags.push( EL_AREA );
			else if ( qName.equals("colors") ) 
				tags.push( EL_COLORS );
			else if ( qName.equals("signature") ) 
				tags.push( EL_SIGNATURE );
			else if ( qName.equals("image-border") ) {
				tags.push( EL_IMGBORDER );
				intParam = 1;				// Default border of 1
			}
			else
				tags.push( EL_NONE );
		}
	}
	
	/**
	 * 
	 */
	public void endElement(String namespaceURI, String sName, String qName) throws SAXException
	{
		Byte tag = (Byte) tags.pop();	// Remove tag from the stack
		
		
		try
		{
			if ( qName.equals("general") || qName.equals("datasources") || qName.equals("graphing") )
				block = BLOCK_ROOT;
		
			else if ( tag == EL_DEF || tag == EL_CDEF )
				addDatasource();
			
			else if ( tag == EL_LINE || tag == EL_AREA )
				addPlotDef( tag );
			
			else if ( tag == EL_PERIOD )
				def.setTimePeriod( Long.parseLong(params[0]), Long.parseLong(params[1]) );
					
			else if ( tag == EL_IMGBORDER )
				def.setImageBorder( colorParam, intParam );
			
			else if ( tag == EL_CLR_BACKGROUND )
				def.setBackColor( colorParam );
			
			else if ( tag == EL_CLR_CANVAS )
				def.setCanvasColor( colorParam );
		}
		catch ( RrdException e ) {
			throw new SAXException( e );
		}
		
		level--;
		
	}
	
	/**
	 * 
	 */
	public void characters(char buf[], int offset, int len) throws SAXException
	{
		String value 	= new String(buf, offset, len);		// remove whitespace
		Byte tag		= (Byte) tags.peek();
		
		if ( level < 3 ) return;
		
		try
		{
			if ( tag == EL_LINE_DS || tag == EL_PERIOD_START )
				params[0] = value;
			else if ( tag == EL_LINE_LEGEND || tag == EL_PERIOD_STOP )
				params[1] = value;
			else if ( tag == EL_CDEF || tag == EL_DEF_FILE )			// RPN expressions are param 2
				params[2] = value;
			else if ( tag == EL_DEF_DSNAME )
				params[3] = value;
			else if ( tag == EL_DEF_CF )
				params[4] = value;
			else if ( tag == EL_COMMENT )						// Add comment
				def.comment( value );
			else if ( tag == EL_TITLE )							// Set title
				def.setTitle( value );
			else if ( tag == EL_SIGNATURE )						// Set signature visibility
				def.setShowSignature( !value.equalsIgnoreCase("no") );
			else if ( tag == EL_LINE_WIDTH || tag == EL_BORDERWIDTH ) {
				intParam 	= Integer.parseInt( value );
				numParams 	|= 4;
			}
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @throws RrdException
	 */
	private void addDatasource() throws RrdException
	{
		String dsType = params[0];
		
		if ( dsType.equals("def") )			// name file dsname consolfunc
			def.datasource( params[1], params[2], params[3], params[4] );
		else if ( dsType.equals("cdef") )	// name rpn
			def.datasource( params[1], params[2] );
	}

	/**
	 * 
	 * @throws RrdException
	 */
	private void addPlotDef( Byte graphType ) throws RrdException
	{
		int width 	= 1;
		Color color = null;
		
		if ( graphType == EL_LINE ) 
		{
			if ( (numParams & 4) == 4 ) 
				width = intParam;
			if ( (numParams & 2) == 2 )
				color = colorParam;
			
			def.line( params[0], color, params[1], width );
		}
		else if ( graphType == EL_AREA )
		{
			if ( (numParams & 2) == 2 )
				color = colorParam;
			
			def.area( params[0], color, params[1] );
		}
		
		numParams = 0;
	}
}
