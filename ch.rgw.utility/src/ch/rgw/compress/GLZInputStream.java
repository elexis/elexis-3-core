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

package ch.rgw.compress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple und nicht besonders effiziente Implementation eines GLZ-InputStreams Macht einfach eine
 * Zwischenspeicherung der decodierten Daten.
 * 
 * @author Gerry
 * 
 */
public class GLZInputStream extends InputStream {
	byte[] decomp;
	int pointer;
	
	public GLZInputStream(InputStream in) throws Exception{
		ByteArrayOutputStream dcs = new ByteArrayOutputStream();
		GLZ glz = new GLZ();
		glz.expand(in, dcs);
		dcs.flush();
		decomp = dcs.toByteArray();
		pointer = 0;
	}
	
	@Override
	public int read() throws IOException{
		if (pointer == decomp.length) {
			return -1;
		}
		return decomp[pointer++];
	}
	
}
