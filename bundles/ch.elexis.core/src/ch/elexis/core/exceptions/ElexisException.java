/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.exceptions;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

public class ElexisException extends Exception {
	private static final long serialVersionUID = -4535064606049686854L;
	private static Logger log = Logger.getLogger(ElexisException.class.getName());

	public static final int EE_DUPLICATE_DISPATCHER = 1;
	public static final int EE_BAD_DISPATCHER = 2;
	public static final int EE_UNEXPECTED_RESPONSE = 3;
	public static final int EE_FILE_ERROR = 4;
	public static final int EE_NOT_SUPPORTED = 5;
	public static final int EE_NOT_FOUND = 6;

	Class<?> clazz;
	int errcode;

	public ElexisException(Class<?> clazz, String errmsg, int errcode) {
		this(clazz, errmsg, errcode, false);
	}

	public ElexisException(Class<?> clazz, String errmsg, int errcode, boolean doLog) {
		super(errmsg);
		this.clazz = clazz;
		this.errcode = errcode;
		log.severe(clazz.getName() + ": " + errmsg + StringUtils.SPACE + Integer.toString(errcode));
	}

	public ElexisException(String errmsg, Throwable throwable) {
		super(errmsg, throwable);
	}

	public ElexisException(String errmsg) {
		super(errmsg);
	}

	public Class<?> getThrowingClass() {
		return clazz;
	}

	public int getErrCode() {
		return errcode;
	}
}
