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

package ch.rgw.io;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Log;

/**
 * �berschrift: Toolbox Beschreibung: Copyright: Copyright (c) 2002 Organisation: rgw
 * 
 * @author G. Weirich
 * @version 1.0
 */

public class Resource {
	
	Class clazz;
	String resbase;
	static Log log;
	
	// String basedir;
	
	public Resource(String packagename){
		clazz = getClass();
		resbase = "/" + packagename.replace('.', '/') + "/";
	}
	
	public InputStream getInputStream(String name){
		String resname = resbase + name;
		InputStream is = clazz.getResourceAsStream(resname);
		if (is == null) {
			return null;
		}
		return is;
	}
	
	public String getText(String name){
		InputStream is = getInputStream(name);
		StringBuffer sb = new StringBuffer();
		int c;
		try {
			while ((c = is.read()) != -1) {
				sb.append((char) c);
			}
			is.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		return sb.toString();
	}
	
	public byte[] getBytes(String name){
		InputStream is = getInputStream(name);
		byte[] buffer = new byte[0];
		byte[] tmpbuf = new byte[1024];
		int len;
		try {
			while ((len = is.read(tmpbuf)) > 0) {
				byte[] newbuf = new byte[buffer.length + len];
				System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
				System.arraycopy(tmpbuf, 0, newbuf, buffer.length, len);
				buffer = newbuf;
			}
			is.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		return buffer;
	}
	
	public Image getImage(String name){
		byte[] buffer = getBytes(name);
		Image ret = Toolkit.getDefaultToolkit().createImage(buffer);
		return ret;
	}
	
	public java.net.URL getBaseDir(String rsc){
		String p = "/" + rsc.replace('.', '/'); //$NON-NLS-1$
		return clazz.getResource(p);
	}
}