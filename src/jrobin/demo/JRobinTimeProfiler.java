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
package jrobin.demo;

import java.io.*;

/**
 * <p>This class is used for time/performance profiling of the 'old' and 'new' JRobin graphing packages.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
public class JRobinTimeProfiler 
{
	private static int NUM_DEMOS = 6;
	TimeProfile[] profiles;
	
	long oldGraphs, newGraphs;
	
	String[] steps = new String[] { "Prepare", 
									"CalculateSeries", 
									"PlotImageBackground", 
									"PlotChart", 
									"PlotComments", 
									"ImageSave", 
									"", 
									"" };
	
	JRobinTimeProfiler( PrintWriter out )
	{
		profiles = new TimeProfile[NUM_DEMOS];
		for (int i = 0; i < profiles.length; i++)
			profiles[i] = new TimeProfile();
		
		new ProfileGraphsOld( out, this );
		new ProfileGraphsNew( out, this );
		
		long proc;
		for (int i = 0; i < profiles.length; i++)
		{
			TimeProfile p = profiles[i];
			
			out.println("\n  DEMO " + (i + 1) + ":  " + p.name + "\n-----------------------------------------------------");
			// Graph def creation times
			proc = ( p.defCreation[0] > 0 ? ((p.defCreation[0] - p.defCreation[1]) * 100 / p.defCreation[0]) : 0); 
			out.println( setwr(25, " 0 - RrdGraphDef creation") + setw(10, p.defCreation[0]) + setw(15, p.defCreation[1])
						+ setw(10, proc + "%") + "\n" );
			// Step creation times
			for (int j = 0; j < p.creation[0].length; j++)
			{
				proc = ( p.creation[0][j] > 0 ? ((p.creation[0][j] - p.creation[1][j]) * 100 / p.creation[0][j]) : 0); 
				out.println( setw(2, "" + (j + 1)) + " - " + setwr(20, steps[j]) + setw(10, p.creation[0][j]) 
								+ setw(15, p.creation[1][j]) + setw(10, proc + "%") );
			}
			// Total creation time
			proc = ( p.totalCreation[0] > 0 ? ((p.totalCreation[0] - p.totalCreation[1]) * 100 / p.totalCreation[0]) : 0); 
			out.println( "\n" + setwr(25, "     Total time:") + setw(10, p.totalCreation[0]) + setw(15, p.totalCreation[1]) 
							+ setw(10, proc + "%") + "\n" );
		}	
		
		proc = ( oldGraphs > 0 ? ((oldGraphs - newGraphs) * 100 / oldGraphs) : 0); 
		out.println( setwr(25, "     Total batch time:") + setw(10, oldGraphs) + setw(15, newGraphs)
							+ setw(10, proc + "%") + "\n" );
		out.println("\n\n");
	}
	
	private String setwr( int num, String str )
	{
		int diff = num - str.length();
		StringBuffer prefix = new StringBuffer("");
		for (int i = 0; i < diff; i++)
			prefix.append(' ');
		return str + prefix.toString();
	}
	
	private String setw( int num, String str )
	{
		int diff = num - str.length();
		StringBuffer prefix = new StringBuffer("");
		for (int i = 0; i < diff; i++)
			prefix.append(' ');
		return prefix.append(str).toString();
	}
	
	private String setw( int num, long val )
	{
		return setw(num, "" + val);
	}
	
	public static void main( String[] args )
	{
		PrintWriter out = new PrintWriter( System.out );
		
		new JRobinTimeProfiler( out );
		
		out.close();
	}
}
