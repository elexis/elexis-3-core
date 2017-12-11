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

/**
 * Exception thrown if an exception was thrown from jdbc framework.
 */
@SuppressWarnings("serial")
public class JdbcLinkException extends RuntimeException {
	
	public JdbcLinkException(String string, Exception cause){
		super(string, cause);
	}
	
	public JdbcLinkException(String string){
		super(string);
	}
	
	public JdbcLinkException(Exception cause){
		super(cause);
	}
}
