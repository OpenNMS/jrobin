/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran@codeloop.com>
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 * $Id$
 */
package org.jrobin.core.jrrd;

import java.io.IOException;

/**
 * Instances of this class model the primary data point status from an RRD file.
 *
 * @author <a href="mailto:ciaran@codeloop.com">Ciaran Treanor</a>
 * @version $Revision$
 */
public class PDPStatusBlock {

	long offset;
	long size;
	String lastReading;
	int unknownSeconds;
	double value;

	PDPStatusBlock(RRDFile file) throws IOException {

		offset = file.getFilePointer();
		lastReading = file.readString(Constants.LAST_DS_LEN);

		file.align(4);

		unknownSeconds = file.readInt();

		file.skipBytes(4);

		value = file.readDouble();

		// Skip rest of pdp_prep_t.par[]
		file.skipBytes(64);

		size = file.getFilePointer() - offset;
	}

	/**
	 * Returns the last reading from the data source.
	 *
	 * @return the last reading from the data source.
	 */
	public String getLastReading() {
		return lastReading;
	}

	/**
	 * Returns the current value of the primary data point.
	 *
	 * @return the current value of the primary data point.
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Returns the number of seconds of the current primary data point is
	 * unknown data.
	 *
	 * @return the number of seconds of the current primary data point is unknown data.
	 */
	public int getUnknownSeconds() {
		return unknownSeconds;
	}

	/**
	 * Returns a summary the contents of this PDP status block.
	 *
	 * @return a summary of the information contained in this PDP status block.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer("[PDPStatus: OFFSET=0x");

		sb.append(Long.toHexString(offset));
		sb.append(", SIZE=0x");
		sb.append(Long.toHexString(size));
		sb.append(", lastReading=");
		sb.append(lastReading);
		sb.append(", unknownSeconds=");
		sb.append(unknownSeconds);
		sb.append(", value=");
		sb.append(value);
		sb.append("]");

		return sb.toString();
	}
}
