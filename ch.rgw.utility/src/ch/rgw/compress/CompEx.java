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
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 *******************************************************************************/

package ch.rgw.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.compress.bzip2.CBZip2OutputStream;

import ch.rgw.tools.BinConverter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Compressor/Expander
 */
public class CompEx {
	public static final int NONE = 0;
	public static final int GLZ = 1 << 29;
	public static final int RLL = 2 << 29;
	public static final int HUFF = 3 << 29;
	public static final int BZIP2 = 4 << 29;
	public static final int ZIP = 5 << 29;
	
	public static final byte[] Compress(String in, int mode){
		if (StringTool.isNothing(in)) {
			return null;
		}
		try {
			return Compress(in.getBytes(StringTool.getDefaultCharset()), mode);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	public static final byte[] Compress(byte[] in, int mode){
		if (in == null) {
			return null;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		return Compress(bais, mode);
	}
	
	public static final byte[] Compress(InputStream in, int mode){
		try {
			switch (mode) {
			case GLZ:
				return CompressGLZ(in);
			case BZIP2:
				return CompressBZ2(in);
			case ZIP:
				return CompressZIP(in);
				// case HUFF: return CompressHuff(in);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return null;
	}
	
	public static byte[] CompressGLZ(InputStream in) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[4];
		// BinConverter.intToByteArray(0,buf,0);
		baos.write(buf);
		GLZ glz = new GLZ();
		long total = glz.compress(in, baos);
		byte[] ret = baos.toByteArray();
		total &= 0x1fffffff;
		total |= GLZ;
		BinConverter.intToByteArray((int) total, ret, 0);
		return ret;
		
	}
	
	public static byte[] CompressBZ2(InputStream in) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		baos.write(buf, 0, 4); // Länge des Originalstroms
		CBZip2OutputStream bzo = new CBZip2OutputStream(baos);
		int l;
		int total = 0;
		;
		while ((l = in.read(buf, 0, buf.length)) != -1) {
			bzo.write(buf, 0, l);
			total += l;
		}
		bzo.close();
		byte[] ret = baos.toByteArray();
		// Die höchstwertigen 3 Bit als Typmarker setzen
		total &= 0x1fffffff;
		total |= BZIP2;
		BinConverter.intToByteArray(total, ret, 0);
		return ret;
	}
	
	public static byte[] CompressZIP(InputStream in) throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[8192];
		baos.write(buf, 0, 4); // Länge des Originalstroms
		ZipOutputStream zo = new ZipOutputStream(baos);
		zo.putNextEntry(new ZipEntry("Data"));
		int l;
		long total = 0;
		;
		while ((l = in.read(buf, 0, buf.length)) != -1) {
			zo.write(buf, 0, l);
			total += l;
		}
		zo.close();
		byte[] ret = baos.toByteArray();
		// Die höchstwertigen 3 Bit als Typmarker setzen
		total &= 0x1fffffff;
		total |= ZIP;
		BinConverter.intToByteArray((int) total, ret, 0);
		return ret;
	}
	
	public static byte[] expand(byte[] in){
		if (in == null) {
			return null;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		return expand(bais);
	}
	
	public static byte[] expand(InputStream in){
		ByteArrayOutputStream baos;
		byte[] siz = new byte[4];
		try {
			in.read(siz);
			long size = BinConverter.byteArrayToInt(siz, 0);
			long typ = size & ~0x1fffffff;
			size &= 0x1fffffff;
			byte[] ret = new byte[(int) size];
			
			switch ((int) typ) {
			case BZIP2:
				CBZip2InputStream bzi = new CBZip2InputStream(in);
				int off = 0;
				int l = 0;
				while ((l = bzi.read(ret, off, ret.length - off)) > 0) {
					off += l;
				}
				
				bzi.close();
				in.close();
				return ret;
			case GLZ:
				GLZ glz = new GLZ();
				baos = new ByteArrayOutputStream();
				glz.expand(in, baos);
				return baos.toByteArray();
			case HUFF:
				HuffmanInputStream hin = new HuffmanInputStream(in);
				off = 0;
				l = 0;
				while ((l = hin.read(ret, off, ret.length - off)) > 0) {
					off += l;
				}
				hin.close();
				return ret;
			case ZIP:
				ZipInputStream zi = new ZipInputStream(in);
				zi.getNextEntry();
				off = 0;
				l = 0;
				while ((l = zi.read(ret, off, ret.length - off)) > 0) {
					off += l;
				}
				
				zi.close();
				return ret;
			default:
				throw new Exception("Invalid compress format");
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
}
