/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.core;

import java.io.IOException;

class RrdString extends RrdPrimitive {
	private String cache;

	RrdString(final RrdUpdater updater, final boolean isConstant) throws IOException {
		super(updater, RrdPrimitive.RRD_STRING, isConstant);
	}

	RrdString(final RrdUpdater updater) throws IOException {
		this(updater, false);
	}

	void set(final String value) throws IOException {
		if (!isCachingAllowed()) {
			writeString(value);
		}
		// caching allowed
		else if (cache == null || !cache.equals(value)) {
			// update cache
			writeString(cache = value);
		}
	}

	String get() throws IOException {
		return (cache != null) ? cache : readString();
	}
}
