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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple und nicht beonders effiziente Implementation eines OutputStreams, der GLZ komprimiert.
 * Schreibt die Daten in einen Puffer, der bei close() komprimiert wird.
 * 
 * @author Gerry
 * 
 */
public class GLZOutputStream extends OutputStream {
	
	private ByteArrayOutputStream intermediate;
	private OutputStream dest;
	
	public GLZOutputStream(OutputStream oo, int sizeHint){
		intermediate = new ByteArrayOutputStream(sizeHint);
		dest = oo;
	}
	
	public GLZOutputStream(OutputStream oo){
		intermediate = new ByteArrayOutputStream();
		dest = oo;
	}
	
	@Override
	public void write(byte[] arg0) throws IOException{
		intermediate.write(arg0);
	}
	
	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException{
		intermediate.write(arg0, arg1, arg2);
	}
	
	@Override
	public void write(int arg0) throws IOException{
		intermediate.write(arg0);
	}
	
	public void close() throws IOException{
		intermediate.close();
		byte[] d = intermediate.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(d);
		GLZ glz = new GLZ();
		glz.compress(bais, dest);
		dest.close();
	}
	
}
