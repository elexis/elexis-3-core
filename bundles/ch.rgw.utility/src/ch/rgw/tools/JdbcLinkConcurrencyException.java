/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.rgw.tools;

@SuppressWarnings("serial")
public class JdbcLinkConcurrencyException extends JdbcLinkException {

	public JdbcLinkConcurrencyException(Exception cause) {
		super(cause);
	}

	public JdbcLinkConcurrencyException(String message, Exception cause) {
		super(message, cause);
	}
}
