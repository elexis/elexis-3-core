/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.text.model;

public abstract class MimePart {
	private String mimetype;
	protected Object data;
	
	public MimePart(String type, Object data){
		mimetype = type;
		this.data = data;
	}
	
	public String getMimeType(){
		return mimetype;
	}
	
	public abstract byte[] getData();	
}
