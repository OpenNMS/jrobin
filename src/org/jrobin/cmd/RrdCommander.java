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

package org.jrobin.cmd;

import org.jrobin.core.RrdException;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Class to be used to execute various RRDTool commands (original syntax of RRDTool must be used).
 */
public class RrdCommander {
	/**
	 * Checks if the output from any RRDTool command will be visible on the standard output device
	 * (console). Default setting is <code>true</code>.
	 *
	 * @return true, if the output will be visible on the standard output device; false, otherwise.
	 */
	public static synchronized boolean isStandardOutUsed() {
		return RrdToolCmd.isStandardOutUsed();
	}

	/**
	 * Method used to control access to stdout (System.out, console) for all RRDTool commands. By default,
	 * all RRDTool commands are allowed to print results to stdout, in a form used by RRDTool.
	 *
	 * @param standardOutUsed <code>true</code> if the output should be visible on the
	 * standard output device, <code>false</code> otherwise.
	 */
	public static synchronized void setStandardOutUsed(boolean standardOutUsed) {
		RrdToolCmd.setStandardOutUsed(standardOutUsed);
	}
	/**
	 * Checks if the class uses {@link org.jrobin.core.RrdDbPool} internally while executing
	 * RRDTool commands.
	 * @return true if the pool is used, false otherwise
	 */
	public static synchronized boolean isRrdDbPoolUsed() {
		return RrdToolCmd.isRrdDbPoolUsed();
	}

	/**
	 * Forces or prohibits {@link org.jrobin.core.RrdDbPool} usage internally while executing
	 * RRDTool commands
	 * @param rrdDbPoolUsed true, to force pool usage, false otherwise.
	 */
	public static synchronized void setRrdDbPoolUsed(boolean rrdDbPoolUsed) {
		RrdToolCmd.setRrdDbPoolUsed(rrdDbPoolUsed);
	}

	/**
	 * Executes single RRDTool command. The command string should start with some
	 * well known RRDTool command word (create, update, fetch, graph...)<p>
	 * @param command RRDTool command like: <p>
	 * <pre>
	 * create test.rrd --start "noon yesterday" --step 300 DS:x:GAUGE:600:U:U RRA:0.5:5:1000
	 * update test.rrd N:1000
	 * last test.rrd
	 * ...
	 * </pre>
	 * @return Result of specific RRDTool command. It is guaranteed that the result of any
	 * successfully executed command will be always different from null.
	 * Unsuccessfully executed commands will always throw
	 * an exception, so you need not check for null results.<p>
	 * Exact type of the result depends from the
	 * type of executed RRDTool command:<p>
	 * <ul>
	 * <li><b>create</b>: returns java.lang.String containing path to the newly created RRD file.
	 * <li><b>last</b>: returns java.lang.Long representing timestamp of the last update.
	 * <li><b>update</b>: returns java.lang.Long representing timestamp of the last update.
	 *<li><b>dump</b>: returns (very long) java.lang.String representing the content of a RRD file
	 * in XML format.
	 * </ul>
	 * @throws IOException thrown in case of I/O error
	 * @throws RrdException thrown for all other errors (parsing errors,
	 * unknown RRDTool syntax/command/option, internal RRD errors...)
	 */
	public static synchronized Object execute(String command) throws IOException, RrdException {
		RrdCmdScanner cmdScanner = new RrdCmdScanner(command);
		RrdToolCmd[] commanders = {
			new RrdCreateCmd(cmdScanner),
			new RrdLastCmd(cmdScanner),
			new RrdUpdateCommand(cmdScanner),
			new RrdDumpCmd(cmdScanner),
			new RrdFetchCmd(cmdScanner)
		};
		for(int i = 0; i < commanders.length; i++) {
			Object result = commanders[i].go();
			if(result != null) {
				return result;
			}
		}
		throw new RrdException("Unknown RRDTool command: " + command);
	}

	public static void main(String[] args) {
		System.out.println("== JRobin's RRDTool commander ==");
		System.out.println("Type a RRDTool command after the dollar sign and press Enter.");
		System.out.println("Start your RRDTool command with 'create', 'update', 'fetch' etc.");
		System.out.println("Use any word starting with a dot '.' to bail out");
		System.out.println("================================");
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			try {
				System.out.print("$ ");
				String s = r.readLine();
				if(s.startsWith(".")) {
					System.exit(0);
				}
				execute(s);
			} catch (IOException e) {
				System.err.println(e);
			} catch (RrdException e) {
				System.err.println(e);
			}
		}
	}
}
