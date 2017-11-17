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

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.bzip2.CBZip2InputStream;
import org.apache.commons.compress.bzip2.CBZip2OutputStream;

import ch.rgw.compress.CompEx;
import ch.rgw.compress.GLZInputStream;
import ch.rgw.compress.GLZOutputStream;
import ch.rgw.compress.HuffmanInputStream;
import ch.rgw.compress.HuffmanOutputStream;
import ch.rgw.compress.HuffmanTree;
import ch.rgw.tools.net.NetTool;

/**
 * Einige Hilfsfunktionen mit und an Strings und String-Collections
 * 
 * @author Gerry Weirich
 */

public class StringTool {
	
	public static final String Version(){
		return "2.0.4";
	}
	
	private static String default_charset = "utf-8";
	public static final String leer = "";
	public static final String space = " ";
	public static final String equals = "=";
	public static final String crlf = "\r\n";
	public static final String lf = "\n";
	public static final String slash = "/";
	public static final String backslash = "\\";
	
	public static final String numbers = "[0-9]+";
	public static final String wordSeparatorChars = "\n\r\t.,;:!? ";
	public static final String wordSeparators = "[\\t ,\\.:\\?!\\n\\r]";
	public static final String lineSeparators = "[\\n\\r\\.\\?!;]";
	public static final String ipv4address = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
	public static final String ipv6address =
		"((([0-9a-f]{1,4}+:){7}+[0-9a-f]{1,4}+)|(:(:[0-9a-f]{1,4}+){1,6}+)|(([0-9a-f]{1,4}+:){1,6}+:)|(::)|(([0-9a-f]{1,4}+:)(:[0-9a-f]{1,4}+){1,5}+)|(([0-9a-f]{1,4}+:){1,2}+(:[0-9a-f]{1,4}+){1,4}+)|(([0-9a-f]{1,4}+:){1,3}+(:[0-9a-f]{1,4}+){1,3}+)|(([0-9a-f]{1,4}+:){1,4}+(:[0-9a-f]{1,4}+){1,2}+)|(([0-9a-f]{1,4}+:){1,5}+(:[0-9a-f]{1,4}+))|(((([0-9a-f]{1,4}+:)?([0-9a-f]{1,4}+:)?([0-9a-f]{1,4}+:)?([0-9a-f]{1,4}+:)?)|:)(:(([0-9]{1,3}+\\.){3}+[0-9]{1,3}+)))|(:(:[0-9a-f]{1,4}+)*:([0-9]{1,3}+\\.){3}+[0-9]{1,3}+))(/[0-9]+)?";
	
	// public static final String wordChars="a-zA-ZäöüÄÖÜéèàâê\'";
	public static final String wordChars = "\\p{L}\'";
	private static int ipHash;
	private static long sequence;
	public static final int LEFT = 1;
	public static final int RIGHTS = 2;
	
	/**
	 * Set the charset to use in all charset-dependent String operations
	 * 
	 * @param charset_name
	 *            the name of the charset (that must be valid)
	 */
	public static void setDefaultCharset(String charset_name){
		default_charset = charset_name;
	}
	
	/**
	 * get the configured default charset
	 * 
	 * @return the charset name (defaults to utf-8)
	 */
	public static String getDefaultCharset(){
		return default_charset;
	}
	
	/**
	 * create a String from a byte array, using the configured charset (defaults to utf-8)
	 * 
	 * @param bytes
	 *            an array of bytes rthat constitute a String in the indicated charset.
	 * @return the created String.
	 */
	public static String createString(byte[] bytes){
		try {
			return new String(bytes, default_charset);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			ExHandler.handle(e);
		}
		return null;
	}
	
	/**
	 * Create a byte arra from a String using the configured charset (defaults to utf-8)
	 * 
	 * @param string
	 * @return
	 */
	public static byte[] getBytes(String string){
		try {
			return string.getBytes(default_charset);
		} catch (UnsupportedEncodingException e) {
			// should not happen
			ExHandler.handle(e);
		}
		return null;
	}
	
	/**
	 * return the bounds of a Rectangle around a String
	 * 
	 * @deprecated this ist a dependency to Swing
	 */
	@Deprecated
	public static Rectangle2D getStringBounds(final String s, final Graphics g){
		if (isNothing(s)) {
			return new Rectangle(0, 0);
		}
		FontRenderContext frc = ((Graphics2D) g).getFontRenderContext();
		Font fnt = g.getFont();
		Rectangle2D r = fnt.getStringBounds(s, frc);
		return r;
	}
	
	/**
	 * Split a String into a String Arry
	 * 
	 * @deprecated obsoleted by java 1.4x 's {@link String#split(String) String.split} method.
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static String[] split(final String m, final String delim){
		Vector v = splitV(m, delim);
		if (v == null) {
			return null;
		}
		String[] ret = (String[]) v.toArray(new String[1]);
		return ret;
	}
	
	/**
	 * Spaltet einen String in einen Vektor
	 * 
	 * @param m
	 *            der zu splittende String
	 * @param delim
	 *            Trennzeichen, an dem zu splitten ist.
	 */
	
	@SuppressWarnings("unchecked")
	public static Vector splitV(final String m, final String delim){
		String mi = m;
		if (mi.equals("")) {
			return null;
		}
		Vector v = new Vector(30, 30);
		
		int i = 0, j = 0;
		while (true) {
			j = mi.indexOf(delim, i);
			if (j == -1) {
				v.add(mi.substring(i));
				break;
			}
			String l = mi.substring(i, j).trim();
			if (!l.equals("")) {
				v.add(l);
			}
			i = j + 1;
		}
		
		return v;
	}
	
	/**
	 * Split a String into an ArrayList
	 * 
	 * @param m
	 *            the String to msplit
	 * @param delim
	 *            the delimiter to split at
	 * @return an ArrayList containing at least one element without the delimiter
	 */
	@SuppressWarnings("unchecked")
	public static List<String> splitAL(final String m, final String delim){
		ArrayList al = new ArrayList();
		String mi = m;
		int i = 0, j = 0;
		while (true) {
			j = mi.indexOf(delim, i);
			if (j == -1) {
				al.add(mi.substring(i));
				break;
			}
			String l = mi.substring(i, j).trim();
			if (!l.equals("")) {
				al.add(l);
			}
			i = j + 1;
		}
		return al;
	}
	
	public static final String flattenSeparator = "~#<";
	
	/**
	 * Wandelt eine Hashtable in einen String aus Kommagetrennten a=b-Paaren um.
	 */
	
	@SuppressWarnings("unchecked")
	public static String flattenStrings(final Hashtable h){
		return flattenStrings(h, null);
	}
	
	public static String flattenStrings(final Hashtable<Object, Object> h, final flattenFilter fil){
		if (h == null) {
			return null;
		}
		Enumeration<Object> keys = h.keys();
		StringBuffer res = new StringBuffer(1000);
		res.append("FS1").append(flattenSeparator);
		while (keys.hasMoreElements()) {
			Object ko = (keys.nextElement());
			if (fil != null) {
				if (fil.accept(ko) == false) {
					continue;
				}
			}
			String v = ObjectToString(h.get(ko));
			String k = ObjectToString(ko);
			if ((k == null) || (v == null) || k.matches(".*=.*")) { // log.log("attempt
				// to
				// flatten
				// unsupported
				// object
				// type",Log.FATALS);
				return null;
			}
			res.append(k).append("=").append(v).append(flattenSeparator);
		}
		String r = res.toString();
		return r.replaceFirst(flattenSeparator + "$", "");
	}
	
	public static final int NONE = 0;
	public static final int HUFF = 1;
	public static final int BZIP = 2;
	public static final int GLZ = 3;
	public static final int ZIP = 4;
	public static final int GUESS = 99;
	
	/**
	 * Eine String-Collection comprimieren
	 * 
	 * @param strings
	 * @param compressMode
	 * @return ein byte array mit dem komprimierten Inhalt der String-Collection
	 */
	public static byte[] pack(final Collection<String> strings){
		String res = join(strings, "\n");
		return CompEx.Compress(res, CompEx.ZIP);
	}
	
	/**
	 * compress an array of single-lined Strings into a byte array
	 * 
	 * @param strings
	 *            an array of String that must not contain newline (\n) characters
	 * @return a byte array with the ZIP-compressed contents of the String array
	 */
	public static byte[] pack(final String[] strings){
		String res = join(strings, "\n");
		return CompEx.Compress(res, CompEx.ZIP);
	}
	
	/**
	 * Unpack a Zip-compressed byte-Array in a List of Strings.
	 * 
	 * @param pack
	 *            a packed byte array as created by pack()
	 * @return an ArrayList of Strings
	 * @see pack(String[])
	 * @see pack(Collection<String>)
	 */
	public static List<String> unpack(final byte[] pack){
		try {
			String raw = new String(CompEx.expand(pack), default_charset);
			return splitAL(raw, "\n");
		} catch (Exception ex) {
			return null; // Sollte sowieso nie vorkommen
		}
		
	}
	
	/**
	 * Eine Hashtable in ein komprimiertes Byte-Array umwandeln
	 * 
	 * @param hash
	 *            die Hashtable
	 * @param compressMode
	 *            GLZ, HUFF, BZIP2
	 * @param ExtInfo
	 *            Je nach Kompressmode nötige zusatzinfo
	 * @return das byte-Array mit der komprimierten Hashtable
	 * @deprecated compressmode is always ZIP now.
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static byte[] flatten(final Hashtable hash, final int compressMode, final Object ExtInfo){
		ByteArrayOutputStream baos = null;
		OutputStream os = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream(hash.size() * 30);
			switch (compressMode) {
			case GUESS:
			case ZIP:
				os = new ZipOutputStream(baos);
				((ZipOutputStream) os).putNextEntry(new ZipEntry("hash"));
				break;
			case BZIP:
				os = new CBZip2OutputStream(baos);
				break;
			case HUFF:
				os = new HuffmanOutputStream(baos, (HuffmanTree) ExtInfo, 0);
				break;
			case GLZ:
				os = new GLZOutputStream(baos, hash.size() * 30);
				break;
			default:
				os = baos;
			}
			
			oos = new ObjectOutputStream(os);
			oos.writeObject(hash);
			if (os != null) {
				os.close();
			}
			baos.close();
			return baos.toByteArray();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Ein mit flatten() erzeugtes Byte-Array wieder in eine HAshtable zurückverwandeln
	 * 
	 * @param flat
	 *            Die komprimierte Hashtable
	 * @param compressMode
	 *            Expnad-Modus
	 * @param ExtInfo
	 * @return die Hastbale
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public static Hashtable fold(final byte[] flat, final int compressMode, final Object ExtInfo){
		ObjectInputStream ois = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(flat);
			switch (compressMode) {
			case BZIP:
				ois = new ObjectInputStream(new CBZip2InputStream(bais));
				break;
			case HUFF:
				ois = new ObjectInputStream(new HuffmanInputStream(bais));
				break;
			case GLZ:
				ois = new ObjectInputStream(new GLZInputStream(bais));
				break;
			case ZIP:
				ZipInputStream zis = new ZipInputStream(bais);
				zis.getNextEntry();
				ois = new ObjectInputStream(zis);
				break;
			case GUESS:
				Hashtable<Object, Object> res = fold(flat, ZIP, null);
				if (res == null) {
					res = fold(flat, GLZ, null);
					if (res == null) {
						res = fold(flat, BZIP, null);
						if (res == null) {
							res = fold(flat, HUFF, ExtInfo);
							if (res == null) {
								return null;
							}
						}
					}
				}
				return res;
			default:
				ois = new ObjectInputStream(bais);
				break;
			}
			
			Hashtable<Object, Object> res = (Hashtable<Object, Object>) ois.readObject();
			ois.close();
			bais.close();
			return res;
		} catch (Exception ex) {
			// ExHandler.handle(ex); deliberately don't mind
			return null;
		}
		
	}
	
	static private String ObjectToString(final Object o){
		if (o instanceof String) {
			return "A" + (String) o;
		}
		if (o instanceof Integer) {
			return "B" + ((Integer) o).toString();
		}
		if (o instanceof Serializable) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			try {
				oos = new ObjectOutputStream(baos);
				oos.writeObject(o);
				oos.close();
				byte[] ret = baos.toByteArray();
				return "Z" + enPrintable(ret);
				
			} catch (IOException e) {
				ExHandler.handle(e);
				return null;
			}
		}
		return null;
	}
	
	static private Object StringToObject(final String s){
		String sx = s.substring(1);
		char pref = s.charAt(0);
		switch (pref) {
		case 'A':
			return sx;
		case 'B':
			return (new Integer(Integer.parseInt(sx)));
		case 'Z':
			byte[] b = dePrintable(sx);
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(b);
				ObjectInputStream ois = new ObjectInputStream(bais);
				Object ret = ois.readObject();
				ois.close();
				bais.close();
				return ret;
			} catch (Exception ex) {
				ExHandler.handle(ex);
				return null;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Hashtable foldStrings(final String s){
		Hashtable h = new Hashtable();
		if (StringTool.isNothing(s)) {
			return h;
		}
		String[] elems = s.split(flattenSeparator);
		if (!elems[0].equals("FS1")) {
			return null;
		}
		for (int i = 1; i < elems.length; i++) {
			String[] elem = elems[i].split("=", 2);
			if (elem.length != 2) { // log.log("Fehler in
				// Hash-Repr�sentation",Log.ERRORS);
				return null;
			}
			Object k = StringToObject(elem[0].trim());
			Object v = StringToObject(elem[1].trim());
			if ((k == null) || (v == null)) {
				return null;
			}
			h.put(k, v);
		}
		return h;
	}
	
	/** gibt true zurück, wenn das Objekt kein String oder null oder "" ist */
	static public boolean isNothing(final Object n){
		if (n == null) {
			return true;
		}
		if (n instanceof String) { // if(((String)n).equals("")) return true;
			if (((String) n).trim().equals("")) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Gibt true zurück, wenn das Feld null ist, leer ist, oder nur Leerstrings enthält
	 */
	static public boolean isEmpty(final String[] f){
		if (f == null) {
			return true;
		}
		for (int i = 0; i < f.length; i++) {
			if (!isNothing(f[i])) {
				return false;
			}
		}
		return true;
	}
	
	/** Verleicht zwei byte-Arrays */
	static public boolean compare(final byte[] a, final byte[] b){
		if (a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Sucht einen String in einem String-Array und gibt dessen Index zurück. Die Suche erfolgt ohne
	 * Berücksichtigung von Gross/Kleinschreibung.
	 * 
	 * @return den index von val in arr oder -1 wenn nicht gefunden.
	 */
	static public int getIndex(final String[] arr, final String val){
		for (int i = 0; i < arr.length; i++) {
			if (val.equalsIgnoreCase(arr[i])) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Verlängert oder kürzt einen String.
	 * 
	 * @param where
	 *            LEFT vorne füllen, RIGHT hinten füllen
	 * @param chr
	 *            Zeichen zum Füllen
	 * @param src
	 *            Quellstring
	 * @param size
	 *            erwünschte Länge
	 * @return der neue String
	 */
	static public String pad(final int where, final char chr, final String src, final int size){
		int diff = size - src.length();
		if (diff > 0) {
			StringBuffer s = new StringBuffer(diff);
			for (int i = 0; i < diff; i++) {
				s.append(chr);
			}
			if (where == LEFT) {
				return s + src;
			}
			return src + s;
		}
		return src.substring(0, size);
	}
	
	/**
	 * Erstellt einen String aus mehreren nacheinander folgenden Strings
	 * 
	 * @param str
	 *            der zu multiplizierende string
	 * @param num
	 *            Zahl der Multiplikationen
	 */
	static public String filler(final String str, int num){
		StringBuilder s = new StringBuilder(num);
		while (num-- > 0) {
			s.append(str);
		}
		return s.toString();
	}
	
	/**
	 * Compares two numeric strings
	 * 
	 * @param first
	 * @param seconds
	 * @return
	 * @since 3.4
	 */
	public static int compareNumericStrings(String first, String seconds){
		int i1 = 0;
		int i2 = 0;
		boolean a1 = isNumeric(first);
		boolean a2 = isNumeric(seconds);
		
		if (a1 && a2) {
			i1 = Integer.parseInt(first);
			i2 = Integer.parseInt(seconds);
			return Integer.compare(i1, i2);
		}
		return Boolean.compare(a1, a2);
	}
	
	/**
	 * Checks if a string is numeric
	 * 
	 * @param str
	 * @return
	 * @since 3.4
	 */
	public static boolean isNumeric(String str){
		if (str != null) {
			return str.matches("-?\\d+");
		}
		return false;
	}
	
	public static String RectangleToString(int x, int y, int w, int h){
		StringBuilder sb = new StringBuilder();
		sb.append(x).append(",").append(y).append(",").append(w).append(",").append(h);
		return sb.toString();
	}
	
	/**
	 * Verknüpft die Elemente eines String-Arrays mittels tren zu einem String
	 * 
	 * @param arr
	 *            - String Array
	 * @param tren
	 *            - Verbindingsstring
	 * @return den verknüpften String
	 */
	static public String join(final String[] arr, final String tren){
		if ((arr == null) || (arr.length == 0)) {
			return "";
		}
		StringBuffer res = new StringBuffer(100);
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == null) {
				continue;
			}
			res.append(arr[i]).append(tren);
		}
		String r2 = res.toString();
		return r2.replaceFirst(tren + "$", "");
	}
	
	public static String join(final Iterable<String> i, final String tren){
		StringBuilder ret = new StringBuilder();
		Iterator<String> it = i.iterator();
		while (it.hasNext()) {
			ret.append(it.next());
			if (it.hasNext()) {
				ret.append(tren);
			}
		}
		return ret.toString();
	}
	
	/**
	 * Wandelt ein Byte-Array in einen druckbaren String um. (Alle Bytes werden in ihre Nibbles
	 * zerlegt, diese werden ähnlich wie mit base64 als Zeichen gespeichert
	 */
	public static String enPrintable(final byte[] src){
		return enPrintable(src, 70); // compatibility
	}
	
	static public String enPrintable(final byte[] src, final int offset){
		byte[] out = new byte[src.length * 2];
		for (int i = 0; i < src.length; i++) {
			out[2 * i] = (byte) ((src[i] >> 4) + offset);
			out[2 * i + 1] = (byte) ((src[i] & 0x0f) + offset);
		}
		try {
			return new String(out, default_charset);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Wandelt einen mit enPrintable erhaltenen String in ein byte-Array zurück.
	 */
	public static byte[] dePrintable(final String src){
		return dePrintable(src, 70); // compatibility
	}
	
	static public byte[] dePrintable(final String src, final int offset){
		byte[] input = null;
		try {
			input = src.getBytes(default_charset);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		byte[] out = new byte[input.length / 2];
		for (int i = 0; i < out.length; i++) {
			out[i] = (byte) ((input[2 * i] - offset) * 16 + (input[2 * i + 1] - offset));
		}
		return out;
	}
	
	/**
	 * Convert a byte array into a String that consists strictly only of numbers and capital
	 * Letters. This can be useful for transmission over 7-Bit-Channels (In fact, 4 bit channels
	 * would suffice) or Web Forms that would need URLConversion otherwise.
	 * 
	 * @param src
	 *            the source array
	 * @return a String that is 2 times the length of src + 3 Bytes and consists only of [0-9A-P]*
	 */
	public static String enPrintableStrict(byte[] src){
		if (src == null) {
			return null;
		}
		byte[] out = new byte[(src.length << 1) + 3];
		try {
			out[0] = 'E'; // header
			out[1] = 'P';
			out[2] = '1'; // Version
			for (int i = 0; i < src.length; i++) {
				int i1 = (src[i] & 0xff) >> 4;
				byte o1 = (byte) (i1 + 65);
				byte o2 = (byte) ((src[i] & 0x0f) + 65);
				out[2 * i + 3] = o1;
				out[2 * i + 4] = o2;
			}
			return new String(out, default_charset);
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Convert a String that was created with enPrintableStrict() back into a byte array
	 * 
	 * @param src
	 *            a String previously created by enPrintableStrict
	 * @return a byte array with the original data or null on errors
	 */
	public static byte[] dePrintableStrict(String src){
		byte[] input = null;
		try {
			input = src.getBytes(default_charset);
			if ((input[0] != 'E') || (input[1] != 'P')) {
				return null;
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		byte[] out = new byte[(input.length - 3) >> 1];
		for (int i = 0; i < out.length; i++) {
			int o1 = input[2 * i + 3] - 65;
			int o2 = input[2 * i + 4] - 65;
			out[i] = (byte) ((o1 << 4) + o2);
		}
		return out;
	}
	
	/**
	 * Gibt eine zufällige und eindeutige Zeichenfolge zurück
	 * 
	 * @param salt
	 *            Ein beliebiger String oder null
	 */
	public static String unique(final String salt){
		if (ipHash == 0) {
			Iterator<String> it = NetTool.IPs.iterator();
			while (it.hasNext()) {
				ipHash += (it.next()).hashCode();
			}
		}
		
		long t = System.currentTimeMillis();
		int t1 = System.getProperty("user.name").hashCode();
		long t2 = ((long) ipHash) << 32;
		long t3 = Math.round(Math.random() * Long.MAX_VALUE);
		long t4 = t + t1 + t2 + t3;
		if (salt != null) {
			long t0 = salt.hashCode();
			t4 ^= t0;
		}
		t4 += sequence++;
		if (sequence > 99999) {
			sequence = 0;
		}
		long idx = sequence % salties.length;
		char start = salties[(int) idx];
		return new StringBuilder().append(start).append(Long.toHexString(t4))
			.append(Long.toHexString((long) Math.random() * 1000)).append(sequence).toString();
	}
	
	/**
	 * make sure a String is never null
	 * 
	 * @param in
	 *            a String or null
	 * @return "" if in was null, in otherwise
	 */
	public static String unNull(final String in){
		return (in == null) ? "" : in;
	}
	
	/**
	 * Dem StreamTokenizer nachempfundene Klasse, die auf einem String arbeitet. Kann gequotete und
	 * geklammerte ausdrücke als token zusammenfassen. Wirft exceptions bei unmatched quotes oder
	 * klammern.
	 * 
	 * @author Gerry Weirich
	 * 
	 */
	static public class tokenizer {
		/** Betrachte in " eingeschlossene Phrasen als ein token */
		public static final int DOUBLE_QUOTED_TOKENS = 1;
		/** Betrachte in ' eingeschlossene Phrasen als ein token */
		public static final int SINGLE_QUOTED_TOKENS = 2;
		/**
		 * In () geklammerte phrasen als ein token betrachten. Verschachtelte Klammern werden
		 * unverändert übernommen
		 */
		public static final int ROUND_BRACKET_TOKENS = 4;
		/** In [] geklammerte Phrasen als ein token betrachten */
		public static final int EDGE_BRACKET_TOKENS = 8;
		/** in {} geklammerte Phrasen als ein token betrachten */
		public static final int CURLY_BRACKET_TOKENS = 16;
		/** Zeilenende bricht token ab */
		public static final int CRLF_MATTERS = 32;
		private final String delim;
		private final int mode;
		private int pos;
		private final String mine;
		
		/**
		 * Einziger Konstruktor
		 * 
		 * @param m
		 *            der Quellstring
		 * @param delim
		 *            Zeichen, die als Tokengrenze betrachtet werden
		 * @param mode
		 *            OR-Kombination der obigen Token-Konstanten
		 */
		public tokenizer(final String m, final String delim, final int mode){
			mine = m;
			this.delim = delim;
			this.mode = mode;
			pos = 0;
		}
		
		/** Splittet den String auf und liefert die tokens als List */
		@SuppressWarnings("unchecked")
		public List<String> tokenize() throws IOException{
			ArrayList ret = new ArrayList();
			StringBuffer token = new StringBuffer();
			while (pos < mine.length()) {
				char c = mine.charAt(pos++);
				if (delim.indexOf(c) != -1) {
					ret.add(token.toString());
					token.setLength(0);
					continue;
				}
				token.append(c);
				switch (c) {
				case '\"':
					if ((mode & DOUBLE_QUOTED_TOKENS) != 0) {
						token.append(readToMatching('\"', '\"'));
					}
					break;
				case '\'':
					if ((mode & SINGLE_QUOTED_TOKENS) != 0) {
						token.append(readToMatching('\'', '\''));
					}
					break;
				case '(':
					if ((mode & ROUND_BRACKET_TOKENS) != 0) {
						token.append(readToMatching('(', ')'));
					}
					break;
				case ')':
					if ((mode & ROUND_BRACKET_TOKENS) != 0) {
						throw new IOException("unmatched bracket");
					}
					break;
				case '[':
					if ((mode & EDGE_BRACKET_TOKENS) != 0) {
						token.append(readToMatching('[', ']'));
					}
					break;
				case ']':
					if ((mode & EDGE_BRACKET_TOKENS) != 0) {
						throw new IOException("unmatched bracket");
					}
					break;
				case '{':
					if ((mode & CURLY_BRACKET_TOKENS) != 0) {
						token.append(readToMatching('{', '}'));
					}
					break;
				case '}':
					if ((mode & CURLY_BRACKET_TOKENS) != 0) {
						throw new IOException("unmatched bracket");
					}
				}
			}
			ret.add(token.toString());
			return ret;
		}
		
		private StringBuffer readToMatching(final char open, final char close) throws IOException{
			StringBuffer ret = new StringBuffer();
			int level = 1;
			while (pos < mine.length()) {
				char c = mine.charAt(pos++);
				ret.append(c);
				if (c == close) {
					if (--level == 0) {
						return ret;
					}
				} else if (c == open) {
					level++;
				} else if (c == '\r') {
					if ((mode & CRLF_MATTERS) != 0) {
						throw new IOException("Unexpected end of line while looking for " + close);
					}
				}
			}
			throw new IOException("Unexpected end of line while looking for " + close);
		}
	}
	
	public interface flattenFilter {
		boolean accept(Object key);
	}
	
	/**
	 * Versucht herauszufinden, ob ein Name weiblich ist
	 * 
	 * @param name
	 *            der Name
	 * @return true wenn der Name vielleicht weiblich ist
	 */
	public static boolean isFemale(final String name){
		if (isNothing(name)) {
			return false;
		}
		final String[] suffices = {
			"a", "is", "e", "id", "ah", "eh", "th"
		};
		for (String s : suffices) {
			if (name.endsWith(s)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isMailAddress(final String in){
		if (StringTool.isNothing(in)) {
			return false;
		}
		// up to 7 characters in device for james@bond.invalid
		return in.matches("\\w[\\w|\\.\\-]+@\\w[\\w\\.\\-]+\\.[a-zA-Z]{2,7}");
		// oder \w[\w|\.\-]+@\w[\w\.\-]+\.[a-zA-Z]{2,4}
	}
	
	/**
	 * Test whether a String is an IPV4 or IPV6-Address
	 * 
	 * @param in
	 *            a String that is possibly an ipv4 or ipv6-Address
	 * @return true if ir seems to be an IP-Address
	 */
	public static boolean isIPAddress(final String in){
		if (in.matches(ipv4address)) {
			return true;
		}
		if (in.matches(ipv6address)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return the first word of the given String
	 */
	public static String getFirstWord(final String in){
		if (isNothing(in)) {
			return "";
		}
		String[] words = in.split(wordSeparators);
		return words[0];
	}
	
	/**
	 * Return the first line if the given String but at most maxChars
	 */
	public static String getFirstLine(final String in, final int maxChars){
		if (isNothing(in)) {
			return "";
		}
		String[] lines = in.split(lineSeparators);
		if (lines[0].length() > maxChars) {
			int ix = lines[0].lastIndexOf(' ', maxChars);
			return lines[0].substring(0, ix);
		}
		return lines[0];
	}
	
	/**
	 * Gibt das Wort des Inhalts zurück das an oder unmittelbar vor der angegebenen Position ist
	 * 
	 * @return Das mit an dieser Stelle befindliche Wort des Strings, <code>String.empty</code>
	 *         falls kein Wort dort ist idt oder der Index ausserhalb des Textbereichs ist
	 */
	public static String getWordAtIndex(String text, int index){
		char c;
		if (index < 0 || text == null || index > text.length()) {
			return "";
		}
		int start;
		int end;
		for (start = index - 1; start >= 0; start--) {
			c = text.charAt(start);
			if (wordSeparatorChars.indexOf(c) != -1) {
				start++;
				break;
			}
		}
		if (start < 0) {
			start = 0;
		}
		for (end = index; end < text.length(); end++) {
			c = text.charAt(end);
			if (wordSeparatorChars.indexOf(c) != -1) {
				break;
			}
		}
		if (end > text.length()) {
			end = text.length() + 1;
		}
		return text.substring(start, end);
	}
	
	@SuppressWarnings("unchecked")
	public static void dumpHashtable(final Log log, final Hashtable table){
		Set<String> keys = table.keySet();
		log.log("Dump Hashtable\n", Log.INFOS);
		for (String key : keys) {
			log.log(key + ": " + table.get(key).toString(), Log.INFOS);
		}
		log.log("End dump\n", Log.INFOS);
	}
	
	/**
	 * Change first lettere to uppercase, other letters to lowercase
	 * 
	 * @param orig
	 *            the word to change (at least 2 characters)
	 * @return the normalized word. Tis will return orig if orig is less than 2 characters
	 */
	public static String normalizeCase(final String orig){
		if (orig == null) {
			return "";
		}
		if (orig.length() < 2) {
			return orig;
		}
		return orig.substring(0, 1).toUpperCase() + orig.substring(1).toLowerCase();
	}
	
	/**
	 * Convert first Character to uppercase. leave rest unchanged
	 * 
	 * @param orig
	 *            the original String
	 * @return the original String with first Character uppercase
	 */
	public static String capitalize(final String orig){
		if (orig == null) {
			return "";
		}
		if (orig.length() < 2) {
			return orig;
		}
		return orig.substring(0, 1).toUpperCase() + orig.substring(1);
	}
	
	/**
	 * Zwei Strings verleichen. Berücksichtigen, dass einer oder beide auch Null sein könnten.
	 * 
	 * @param a
	 *            erster String
	 * @param b
	 *            zweiter String
	 * @return -1,0 oder 1
	 */
	public static int compareWithNull(String a, String b){
		if (a == null) {
			if (b == null) {
				return 0;
			} else {
				return -1;
			}
		} else if (b == null) {
			return 1;
		} else {
			return a.compareTo(b);
		}
	}
	
	/**
	 * String wenn nötig kürzen
	 * 
	 * @param orig
	 *            Originalstring
	 * @param len
	 *            maximal zulöässige Lenge
	 * @return den String, der maximal len Zeichen lang ist
	 */
	public static String limitLength(final String orig, final int len){
		if (orig == null) {
			return "";
		}
		if (orig.length() > len) {
			return orig.substring(0, len);
		}
		return orig;
	}
	
	/**
	 * String aus einem Array holen. Leerstring, wenn der angeforderte Index ausserhalb des Arrays
	 * liegt
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	public static String getSafe(final String[] array, final int index){
		if ((index > -1) && (array.length > index)) {
			return array[index];
		}
		return "";
	}
	
	/**
	 * Parse a String but don't throw expetion if not parsable. Return 0 instead
	 * 
	 * @param string
	 * @return
	 */
	public static int parseSafeInt(String string){
		if (string == null) {
			return 0;
		}
		try {
			return Integer.parseInt(string.trim());
		} catch (NumberFormatException ne) {
			return 0;
		}
	}
	
	/**
	 * Parse a Double from a string but don't throw an Exception if not parseable. Return 0.0
	 * instead.
	 * 
	 * @param string
	 *            a String containing probably a Double
	 * @return always a double. 0.0 if the origin was 0.0 or null or not a Double
	 */
	public static double parseSafeDouble(String string){
		if (string == null) {
			return 0.0;
		}
		try {
			return Double.parseDouble(string.trim());
		} catch (NumberFormatException ne) {
			return 0.0;
		}
	}
	
	/**
	 * String mit unterschiedlicher möglicher Schreibweise in einheitliche Schreibweise bringen
	 * 
	 * @param in
	 *            ein String
	 * @return derselbe String, aber alle möglicherweise kritische Zeichen durch _ ersetzt.
	 */
	public static String unambiguify(final String in){
		String ret = in.toLowerCase();
		ret = ret.replaceAll("([^a-z]|ue|oe|ae)", "_");
		ret = ret.replaceAll("__+", "_");
		return ret;
	}
	
	/**
	 * convert a String from a source encoding to this platform's default encoding
	 * 
	 * @param src
	 *            the source string
	 * @param srcEncoding
	 *            the name of the encoding of the source
	 * @return the transcoded String or the source String if the encoding is not supported
	 */
	public static String convertEncoding(String src, String srcEncoding){
		try {
			byte[] bytes = src.getBytes();
			return new String(bytes, srcEncoding);
		} catch (UnsupportedEncodingException e) {
			return src;
		}
	}
	
	/**
	 * convert a String Array from a source encoding to this platform's default encoding
	 * 
	 * @param src
	 *            the source Array
	 * @param srcEncoding
	 *            the name of the encoding of the source
	 * @return the transcoded Array or the source Array if the encoding is not supported
	 */
	public static String[] convertEncoding(String[] src, String srcEncoding){
		String[] ret = new String[src.length];
		for (int i = 0; i < src.length; i++) {
			ret[i] = convertEncoding(src[i], srcEncoding);
		}
		return ret;
	}
	
	/**
	 * Eine beliebige Ziffernfolge mit der Modulo-10 Prüfsumme verpacken
	 * 
	 * @param number
	 *            darf nur aus Ziffern bestehen
	 * @return die Eingabefolge, ergänzt um ihre Prüfziffer
	 */
	public static String addModulo10(final String number){
		int row = 0;
		String nr = number.replaceAll("[^0-9]", "");
		for (int i = 0; i < nr.length(); i++) {
			int col = Integer.parseInt(nr.substring(i, i + 1));
			row = mod10Checksum[row][col];
		}
		return number + Integer.toString(mod10Checksum[row][10]);
		
	}
	
	/**
	 * Die Modulo-10-Prüfsumme wieder entfernen
	 * 
	 * @param number
	 *            eine um eine prüfziffer ergänzte Zahl
	 * @return die Zahl ohne prüfziffer oder null, wenn die Prüfziffer falsch war.
	 */
	public static String checkModulo10(final String number){
		String check = number.substring(0, number.length() - 1);
		String should = addModulo10(check);
		if (should.equals(number)) {
			return check;
		}
		return null;
	}
	
	/** Array für den modulo-10-Prüfsummencode */
	private static final int[][] mod10Checksum = {
		{
			0, 9, 4, 6, 8, 2, 7, 1, 3, 5, 0
		}, {
			9, 4, 6, 8, 2, 7, 1, 3, 5, 0, 9
		}, {
			4, 6, 8, 2, 7, 1, 3, 5, 0, 9, 8
		}, {
			6, 8, 2, 7, 1, 3, 5, 0, 9, 4, 7
		}, {
			8, 2, 7, 1, 3, 5, 0, 9, 4, 6, 6
		}, {
			2, 7, 1, 3, 5, 0, 9, 4, 6, 8, 5
		}, {
			7, 1, 3, 5, 0, 9, 4, 6, 8, 2, 4
		}, {
			1, 3, 5, 0, 9, 4, 6, 8, 2, 7, 3
		}, {
			3, 5, 0, 9, 4, 6, 8, 2, 7, 1, 2
		}, {
			5, 0, 9, 4, 6, 8, 2, 7, 1, 3, 1
		}
	};
	
	private static final char[] salties = {
		'q', 'w', 'e', 'r', 't', 'z', 'u', 'o', 'i', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k',
		'l', 'y', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'A', 'Y', 'W', 'E', 'D', 'C', 'R', 'F', 'V',
		'T', 'G', 'B', 'Z', 'H', 'N', 'U', 'J', 'M', 'I', 'K', 'O', 'L', 'P'
	};
}