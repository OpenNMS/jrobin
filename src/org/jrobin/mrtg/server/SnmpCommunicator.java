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
package org.jrobin.mrtg.server;

import snmp.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

class SnmpCommunicator {
	static final int SNMP_TIMEOUT = 10; // seconds

	static final String[][] OIDS = {
		{"sysDescr",      "1.3.6.1.2.1.1.1.0"       },
		{"sysName",       "1.3.6.1.2.1.1.5.0"       },
		{"ifDescr",       "1.3.6.1.2.1.2.2.1.2"     },
		{"ifType",        "1.3.6.1.2.1.2.2.1.3"     },
		{"ifSpeed",       "1.3.6.1.2.1.2.2.1.5"     },
		{"sysUpTime",     "1.3.6.1.2.1.1.3.0"       },
		{"ifOperStatus",  "1.3.6.1.2.1.2.2.1.8"     },
		{"ifInOctets",    "1.3.6.1.2.1.2.2.1.10"    },
		{"ifOutOctets",   "1.3.6.1.2.1.2.2.1.16"    },
		{"ifInErrors",    "1.3.6.1.2.1.2.2.1.14"    },
		{"ifOutErrors",   "1.3.6.1.2.1.2.2.1.20"    },
		{"ifInDiscards",  "1.3.6.1.2.1.2.2.1.13"    },
		{"ifOutDiscards", "1.3.6.1.2.1.2.2.1.19"    },
		{"ifAlias",       "1.3.6.1.2.1.31.1.1.1.18" }
	};

	// state variables
	private SNMPv1CommunicationInterface comm;

    public SnmpCommunicator(String host, String community)
		throws IOException {
		InetAddress hostAddress = InetAddress.getByName(host);
		comm = new SNMPv1CommunicationInterface(0, hostAddress, community);
		comm.setSocketTimeout(SNMP_TIMEOUT * 1000);
    }

    public String getNumericOid(String oid) {
    	int n = OIDS.length;
    	for(int i = 0; i < n; i++) {
    		String name = OIDS[i][0], value = OIDS[i][1];
    		if(oid.startsWith(name)) {
    			return oid.replaceFirst(name, value);
    		}
    	}
    	// probably numerical
    	return oid;
    }

	public String get(String oid) throws IOException {
		String numericOid = getNumericOid(oid);
		try {
	    	SNMPVarBindList newVars = comm.getMIBEntry(numericOid);
		    SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
			SNMPObject snmpObject = pair.getSNMPObjectAt(1);
			return snmpObject.toString().trim();
		}
		catch(SNMPBadValueException bve) {
			return null;
		}
		catch(SNMPGetException ge) {
			return null;
		}
	}

	public String get(String oid, int index) throws IOException {
		return get(oid + "." + index);
	}

	public String[] get(String[] oids) throws IOException {
		int count = oids.length;
		String[] result = new String[count];
		for(int i = 0; i < count; i++) {
			result[i] = get(oids[i]);
		}
		return result;
	}

	public Map walk(String base) throws IOException {
		TreeMap map = new TreeMap();
		String baseOid = getNumericOid(base);
		String currentOid = baseOid;
		try {
			while(true) { // ugly, but it works
				SNMPVarBindList newVars = comm.getNextMIBEntry(currentOid);
				SNMPSequence pair = (SNMPSequence)(newVars.getSNMPObjectAt(0));
				currentOid = pair.getSNMPObjectAt(0).toString();
 	 	  		String value = pair.getSNMPObjectAt(1).toString().trim();
	 	 	  	if(currentOid.startsWith(baseOid)) {
		   		 	// extract interface number from oid
			    	int lastDot = currentOid.lastIndexOf(".");
					String indexStr = currentOid.substring(lastDot + 1);
					int index = Integer.parseInt(indexStr);
					// store interface description
					map.put(new Integer(index), value);
		    	}
				else {
					break;
				}
			}
		}
		catch(SNMPBadValueException bve) { }
		catch(SNMPGetException ge) { }
		return map;
	}

	public int getIfIndexByIfDescr(String ifDescr) throws IOException {
		Map map = walk("ifDescr");
		Iterator it = map.keySet().iterator();
		while(it.hasNext()) {
			Integer ix = (Integer) it.next();
			String value = (String) map.get(ix);
		    if(value.equalsIgnoreCase(ifDescr)) {
				return ix.intValue();
		    }
		}
		return -1;
	}

	public void close() {
		if(comm != null) {
			try {
				comm.closeConnection();
				comm = null;
			}
			catch (SocketException se) {}
		}
	}

	protected void finalize() {
		close();
	}

}

