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

package ch.rgw.crypt;

public class BadParameterException extends CryptologistException {
	
	private static final long serialVersionUID = -5502719232422683351L;
	
	public BadParameterException(String message, int code){
		super(message, code);
	}
}
