/*
 * Copyright (C) 2001 Ciaran Treanor <ciaran@codeloop.com>
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 *
 * $Id$
 */
package org.jrobin.core.jrrd;

/**
 * This exception may be throw if an error occurs while operating
 * on an RRD object.
 *
 * @author <a href="mailto:ciaran@codeloop.com">Ciaran Treanor</a>
 * @version $Revision$
 */
public class RRDException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an RRDException with no detail message.
	 */
	public RRDException() {
		super();
	}

	/**
	 * Constructs an RRDException with the specified detail message.
	 */
	public RRDException(String message) {
		super(message);
	}
}
