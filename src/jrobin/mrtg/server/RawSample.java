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
package jrobin.mrtg.server;

import jrobin.core.Util;

class RawSample {
	private String host;
	private String ifDescr = "";
	private boolean valid = true;
	private long timestamp = Util.getTime();
	private long ifInOctets;
	private long ifOutOctets;
	private long sysUpTime;
	private int ifOperStatus;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getIfDescr() {
		return ifDescr;
	}

	public void setIfDescr(String ifDescr) {
		this.ifDescr = ifDescr;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getIfInOctets() {
		return ifInOctets;
	}

	public void setIfInOctets(long ifInOctets) {
		this.ifInOctets = ifInOctets;
	}

	public long getIfOutOctets() {
		return ifOutOctets;
	}

	public void setIfOutOctets(long ifOutOctets) {
		this.ifOutOctets = ifOutOctets;
	}

	public long getSysUpTime() {
		return sysUpTime;
	}

	public void setSysUpTime(long sysUpTime) {
		this.sysUpTime = sysUpTime;
	}

	public int getIfOperStatus() {
		return ifOperStatus;
	}

	public void setIfOperStatus(int ifOperStatus) {
		this.ifOperStatus = ifOperStatus;
	}

	public String toString() {
		return ifDescr + "@" + host + ": timestamp=" + timestamp + " valid=" + valid +
			" ifInOctets=" + ifInOctets + " ifOutOctets=" + ifOutOctets +
			" sysUpTime=" + sysUpTime;
	}
}
