/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.admin;

public class InsufficientRightsException extends Exception {
	private static final long serialVersionUID = 7507842875772322981L;
	
	public InsufficientRightsException(String message){
		super(message);
	}
}
