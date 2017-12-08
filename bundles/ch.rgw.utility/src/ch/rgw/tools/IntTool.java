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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Einige Utilities für Zahlen, Integer IO und Konversionen
 */

public class IntTool {
	public static String Version(){
		return "1.1.1";
	}
	
	/**
	 * Schreibt ein Long als Array von Bytes
	 * 
	 * @param arr
	 *            Zielarray
	 * @param offset
	 *            Offset im Array, ab dem geschrieben werden soll
	 * @param value
	 *            Wert, der geschrieben werden soll
	 * @return Neuer Offset zum Schreiben des nächsten Werts
	 */
	public static int toBytes(byte[] arr, int offset, long value){
		int low = (int) (value & 0xffffffff);
		offset = toBytes(arr, offset, low);
		int hi = (int) ((value >> 32) & 0xffffffff);
		offset = toBytes(arr, offset, hi);
		return offset;
	}
	
	/**
	 * Schreibt einen Integer als Array von Bytes
	 * 
	 * @param arr
	 *            Zielarray
	 * @param offset
	 *            Offset im Array, ab dem geschrieben werden soll
	 * @param value
	 *            Wert, der geschrieben werden soll
	 * @return Neuer Offset zum Schreiben des nächsten Werts
	 */
	public static int toBytes(byte[] arr, int offset, int value){
		int b1 = value & 0xff;
		int b2 = (value >> 8) & 0xff;
		int b3 = (value >> 16) & 0xff;
		int b4 = (value >> 24) & 0xff;
		arr[offset++] = (byte) (b1); // 8
		arr[offset++] = (byte) (b2); // 16
		arr[offset++] = (byte) (b3); // 24
		arr[offset++] = (byte) (b4); // 32
		return offset;
	}
	
	/** Schreibt einen Integer als Bytefolge in einen OutputStream */
	public static void writeInt(int i, OutputStream out) throws IOException{
		byte[] arr = new byte[4];
		toBytes(arr, 0, i);
		out.write(arr);
	}
	
	/** Liest einen als Bytefolge abgelegten Integer aus einem InputStream */
	public static int readInt(InputStream in) throws IOException{
		byte[] arr = new byte[4];
		in.read(arr);
		return fromBytesInt(arr, 0);
	}
	
	/**
	 * Holt einen als Bytefolge in einem Array abgelegten Long-Wert
	 * 
	 * @param arr
	 *            Das Quellarray
	 * @param offset
	 *            Offset innerhalb des Arrays, ab dem gelesen werden soll
	 * @return Den gelesenen Long Wert
	 */
	public static long fromBytesLong(byte[] arr, int offset){
		int low = fromBytesInt(arr, offset);
		long ret = ((long) fromBytesInt(arr, offset + 4)) << 32;
		ret |= low;
		return ret;
	}
	
	/**
	 * Holt einen als Bytefolge in einem Array abgelegten Integer-Wert
	 * 
	 * @param arr
	 *            Das Quellarray
	 * @param offset
	 *            Offset innerhalb des Arrays, ab dem gelesen werden soll
	 * @return Den gelesenen Integer Wert
	 */
	public static int fromBytesInt(byte[] arr, int offset){
		int b1 = ByteToInt((arr[offset++]));
		int b2 = ByteToInt((arr[offset++]));
		int b3 = ByteToInt((arr[offset++]));
		int b4 = ByteToInt(arr[offset++]);
		return b1 + (b2 << 8) + (b3 << 16) + (b4 << 24);
	}
	
	/** Wandelt ein Long in einen druckbaren String um */
	public static String to_prt(long val){
		long num = val;
		int flag = 65;
		int off = 28;
		int siz = 9;
		while ((num >> off) == 0) {
			off -= 4;
			siz--;
		}
		byte[] res = new byte[siz];
		for (int i = 1; off >= 0; off -= 4, i++) {
			int nibble = (int) ((num >> off) & 15);
			res[i] = (byte) (65 + nibble);
			flag++;
		}
		res[0] = (byte) (flag - 1);
		try {
			return new String(res, "UTF-8");
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	/* Holt einen mit to_prt geschriebenen Long zur�ck */
	public static long from_prt(String val){
		byte[] bytes = null;
		try {
			bytes = val.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return 0;
		}
		int flag = bytes[0] - 65;
		int off = 4 * flag;
		long ret = 0;
		int i = 1;
		for (; off >= 0; off -= 4, i++) {
			int nibble = bytes[i] - 65;
			ret += nibble << off;
		}
		return ret;
	}
	
	/**
	 * Ergänzt ein Long um eine Prüfsumme und wandelt das ganze in einen leicht abtippbaren String,
	 * bestehend aus kurzen Zeichenfolgen um
	 * 
	 * @param num
	 *            die Zahl
	 * @return der codierte und �berpr�fbare String
	 */
	public static String envelope(long num){
		String s0 = to_prt(num);
		byte[] s1;
		try {
			s1 = s0.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			ExHandler.handle(e);
			return null;
		}
		int l = s1.length;
		int chk = 0;
		for (int i = 0; i < l; i++) {
			chk += s1[i];
			if (chk > 255)
				chk -= 255;
		}
		String s2 = to_prt(chk);
		String cmb = s0 + s2.substring(1);
		cmb = cmb.replaceAll("(-?[^-]{4})", "$1-");
		cmb = cmb.replaceFirst("-$", "");
		cmb = cmb.toLowerCase();
		
		return cmb;
	}
	
	/**
	 * Holt einen Long-Wert aus einem envelope. Prüft, ob die Prüfsumme korrekt ist.
	 * 
	 * @param env
	 * @return
	 */
	public static long disenvelope(String env){
		String in = env.toUpperCase().replaceAll("-", "");
		long res = IntTool.from_prt(in);
		String cmp = envelope(res);
		if (cmp.equals(env)) {
			return res;
		}
		return -1;
	}
	
	/** Betrachtet ein byte als unsigned und liefert diesen Wert als Integer zurück */
	public static int ByteToInt(byte s){
		if (s < 0) {
			byte s1 = (byte) (s & (byte) 127);
			int s2 = s1;
			int s3 = s2 | 128;
			return s3;
		}
		return s;
	}
	
	/*
	 * public static String toHexString(int x) { int hi=x>>16; int lo=x&0x0000ffff; return
	 * toHexString(hi)+toHexString(lo); }
	 */
	public static String toHexString(byte x){
		int hi = x / 16;
		int lo = x - hi;
		StringBuffer s = new StringBuffer(2);
		s.append(Character.forDigit(hi, 16));
		s.append(Character.forDigit(lo, 16));
		return s.toString();
	}
	
	/** rundet eine Zahl auf definierte Stellen */
	
	public static float round(double val, int stel){
		double mult = Math.pow(10, stel);
		long zwi = Math.round(val * mult);
		return (float) (zwi / 100.0);
	}
	
	/** Log10 */
	public static double lg(double x){
		return Math.log(x) / Math.log(10);
	}
	
}