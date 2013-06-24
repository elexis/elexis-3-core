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

/**
 * Huffmann tree with several creation and persistency options.
 * @author Gerry
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.prefs.Preferences;

import ch.rgw.tools.BinConverter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.IntTool;
import ch.rgw.tools.StringTool;

public class HuffmanTree {
	public static String Version(){
		return "1.0.2";
	}
	
	static final int TABLESIZE = 256;
	public int[] freq;
	Node root;
	
	public HuffmanTree(){
		root = null;
	}
	
	/**
	 * create a Huffman Tree from a given source. Counts the frequency of each byte on the source
	 * and constructs the tree accordingly
	 * 
	 * @param source
	 *            an arbitrary sequence of bytes.
	 */
	public HuffmanTree(byte[] source){
		build(constructTable(source));
	}
	
	/**
	 * create a Huffman Tree from a frequency table (which must be an Integer Array of exactly 255
	 * elements)
	 * 
	 * @param frequencyTable
	 */
	public HuffmanTree(int[] frequencyTable){
		build(frequencyTable);
	}
	
	/**
	 * Build a tree from a frequency table. Ensures that escape and eof are represented in the tree.
	 * 
	 * @param table
	 *            a field of TABLESIZE ints indicating the frequency od each byte
	 * @return the root node of the newly created tree
	 */
	@SuppressWarnings("unchecked")
	public Node build(int[] table){
		root = null;
		if ((table == null) || (table.length != TABLESIZE)) {
			return null;
		}
		freq = table;
		if (freq[Huff.escape] == 0)
			freq[Huff.escape] = 1;
		if (freq[Huff.eof] == 0)
			freq[Huff.eof] = 1;
		
		ArrayList nodes = new ArrayList(freq.length);
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] == 0)
				continue;
			nodes.add(new Node((byte) i, freq[i]));
		}
		Collections.sort(nodes);
		while (nodes.size() > 1) {
			Node a = (Node) nodes.remove(0);
			Node b = (Node) nodes.remove(0);
			int al = a.ch.length;
			byte[] bn = new byte[al + b.ch.length];
			for (int i = 0; i < al; i++) {
				bn[i] = a.ch[i];
			}
			for (int i = 0; i < b.ch.length; i++) {
				bn[i + al] = b.ch[i];
			}
			Arrays.sort(bn);
			Node n = new Node(bn, a.lfreq + b.lfreq);
			n.left = a;
			n.right = b;
			nodes.add(n);
			Collections.sort(nodes);
		}
		root = (Node) nodes.get(0);
		return root;
	}
	
	/**
	 * constructs a frequency table from an array of bytes
	 * 
	 * @param source
	 *            the array to construct the table from
	 */
	public static int[] constructTable(byte[] source){
		int[] ret = new int[TABLESIZE];
		for (int i = 0; i < source.length; i++) {
			ret[IntTool.ByteToInt(source[i])]++;
		}
		return ret;
	}
	
	/** constructs a frequency table from a file */
	public static int[] constructTable(RandomAccessFile file){
		int[] ret = new int[TABLESIZE];
		try {
			file.seek(0L);
			long l = file.length();
			for (long i = 0; i < l; i++) {
				int c = file.read();
				ret[c]++;
			}
			return ret;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Construct a frequency table from an InputStream. This will create a temporary file to copy
	 * the InputStream in.
	 * 
	 * @param source
	 *            the Input Stream
	 * @return an InputStream which is a copy of the source Stream, provided to re-Read the same
	 *         Bytes for the actual compression process.
	 */
	public InputStream constructTable(InputStream source, boolean copy){
		freq = new int[TABLESIZE];
		try {
			File file = null;
			FileOutputStream fos = null;
			if (copy == true) {
				file = File.createTempFile("huf", "tmp");
				file.deleteOnExit();
				fos = new FileOutputStream(file);
			}
			while (source.available() != 0) {
				int c = source.read();
				freq[c]++;
				if (copy)
					fos.write(c);
			}
			source.close();
			if (copy) {
				fos.close();
				return new FileInputStream(file);
			}
			return null;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	/**
	 * Creates a compacted form of the actual frequency table and saves it into an OutputStream.
	 * 
	 * @return true on success
	 */
	public boolean saveTable(OutputStream out){
		byte[] tbl = compactTable(freq);
		try {
			short l = (short) tbl.length;
			out.write(l & 0xff);
			out.write(l >> 8);
			for (int i = 0; i < tbl.length; i++) {
				out.write(tbl[i]);
			}
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return false;
		}
	}
	
	/**
	 * Reloads a frequency table as saved by saveTable
	 * 
	 * @return the table
	 */
	public static int[] loadTable(InputStream in){
		try {
			int l = in.read();
			l |= (in.read() << 8);
			byte[] tbl = new byte[l];
			for (int i = 0; i < tbl.length; i++) {
				tbl[i] = (byte) in.read();
			}
			return expandTable(tbl);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	/**
	 * imports a compacted predefined Table
	 * 
	 * @param in
	 * @return
	 */
	public static int[] useTable(byte[] in){
		return expandTable(in);
	}
	
	/**
	 * Import a compaced frequency table from the system preferences (In windows from the registry
	 * HKLM/JavaSoft/Prefs/ch/rgw/tools/Compress/StandardTables, in Linux from ~/.prefs)
	 * 
	 * @param name
	 *            Name of the table in the registry. If no table with this name is found, the
	 *            default table (TextDeutsch) is returned.
	 * @return the table
	 */
	public static int[] useStandardTable(String name){
		Preferences pr = Preferences.userNodeForPackage(Huff.class);
		Preferences node = pr.node("StandardTables");
		byte[] res = node.getByteArray(name, TextDeutsch);
		return HuffmanTree.expandTable(res);
	}
	
	/*
	 * public static byte[] makeProportional(int[] table) { int max=0; for(int
	 * i=0;i<table.length;i++) { if(table[i]==0) continue; if(table[i]>max) max=table[i]; } //double
	 * min=max*0.1; //max-=min; double prop=127.00/max; byte[] res=new byte[255]; for(int
	 * i=0;i<256;i++) { if(table[i]==0) continue; double proc=table[i]*prop; long
	 * red=Math.round(proc); if(red==0) red=1; if(red>255) red=255; res[i]=(byte)(red); } return
	 * res;
	 * 
	 * }
	 */
	/**
	 * compute a frequency table from an InputStream and save a compacted representation of this
	 * table in the system preferences. (To be used later by @see #useStandardTable(String) )
	 * 
	 * @param name
	 *            name to give the table.
	 * @param in
	 *            InputStream
	 * @return true on success
	 * @throws Exception
	 */
	public static boolean CreateStandardTableFromStream(String name, InputStream in)
		throws Exception{
		int[] tbl = new int[TABLESIZE];
		while (in.available() != 0) {
			int c = in.read();
			tbl[c]++;
		}
		byte[] dest = HuffmanTree.compactTable(tbl);
		Preferences pref = Preferences.userNodeForPackage(Huff.class);
		Preferences node = pref.node("StandardTables");
		node.putByteArray(name, dest);
		pref.flush();
		return true;
	}
	
	public Node getRootNode(){
		return root;
	}
	
	public int[] getTable(){
		return freq;
	}
	
	public static void dumpTable(int[] table){
		for (int i = 0; i < table.length; i++) {
			if (table[i] == 0)
				continue;
			System.out.println(i + " : " + table[i]);
		}
		
	}
	
	public static boolean checkCompacter(){
		int[] t1 =
			new int[] {
				1, 2, 3, 4, 5, 6, 7, 8, 9, 12345678, 9876, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1,
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 267
			};
		byte[] t2 = compactTable(t1);
		int[] t3 = expandTable(t2);
		if (t1.length != t3.length)
			return false;
		for (int i = 0; i < t1.length; i++) {
			if (t1[i] != t3[i])
				return false;
			
		}
		return true;
	}
	
	private static byte[] compactTable(final int[] table){
		byte[] ret = new byte[4 * table.length];
		for (int i = 0, off = 0; i < table.length; i++, off += 4) {
			BinConverter.intToByteArray(table[i], ret, off);
		}
		return RLL.compress((byte) 0, ret);
		
	}
	
	private static int[] expandTable(final byte[] source){
		byte[] t = RLL.expand(source);
		int[] ret = new int[t.length >> 2];
		for (int i = 0, off = 0; i < ret.length; i++, off += 4) {
			ret[i] = BinConverter.byteArrayToInt(t, off);
		}
		return ret;
	}
	
	class Node implements Comparable {
		Node left, right;
		byte[] ch;
		int lfreq;
		
		Node(byte x, int f){
			ch = new byte[] {
				x
			};
			lfreq = f;
		}
		
		Node(byte[] x, int f){
			ch = x;
			lfreq = f;
		}
		
		public int compareTo(Object arg0){
			Node other = (Node) arg0;
			if (lfreq > other.lfreq)
				return 1;
			else if (lfreq > other.lfreq)
				return -1;
			
			return 0;
		}
		
		@Override
		public boolean equals(Object o){
			if (o instanceof Node) {
				Node other = (Node) o;
				if (StringTool.compare(ch, other.ch) == true) {
					if (lfreq == other.lfreq) {
						return true;
					}
				}
			}
			return false;
		}
	}
	
	/** Standard table for German text */
	public static byte[] TextDeutsch = new byte[] {
		7, 7, 36, 0, 61, 7, 3, 0, 9, 3, 7, 10, 0, -12, 2, 7, 50, 0, 1, 7, 23, 0, -23, 36, 0, 0, 5,
		7, 3, 0, -18, 7, 3, 0, 1, 7, 7, 0, 35, 7, 3, 0, 43, 7, 3, 0, 12, 7, 3, 0, -123, 7, 3, 0,
		-119, 7, 11, 0, 51, 2, 0, 0, -2, 7, 3, 0, 71, 3, 0, 0, -8, 7, 3, 0, 48, 1, 0, 0, -82, 7, 3,
		0, -87, 7, 3, 0, 79, 7, 3, 0, 58, 7, 3, 0, 73, 7, 3, 0, 59, 7, 3, 0, 56, 7, 3, 0, 45, 7, 3,
		0, 36, 7, 3, 0, -5, 1, 0, 0, 35, 7, 3, 0, 38, 2, 0, 0, 117, 7, 3, 0, 38, 2, 0, 0, 13, 7, 3,
		0, 25, 7, 3, 0, -64, 1, 0, 0, -42, 7, 3, 0, -66, 7, 3, 0, 9, 1, 0, 0, 62, 1, 0, 0, -36, 7,
		3, 0, -24, 7, 3, 0, -61, 7, 3, 0, 41, 1, 0, 0, 36, 7, 3, 0, -57, 7, 3, 0, -109, 7, 3, 0,
		126, 1, 0, 0, -110, 7, 3, 0, 59, 7, 3, 0, 65, 1, 0, 0, 25, 7, 3, 0, -77, 7, 3, 0, 85, 2, 0,
		0, -61, 7, 3, 0, 84, 7, 3, 0, -112, 7, 3, 0, -84, 7, 3, 0, 40, 7, 3, 0, 15, 7, 3, 0, 88, 7,
		19, 0, 23, 1, 7, 6, 0, -61, 12, 0, 0, 21, 4, 0, 0, 70, 6, 0, 0, 33, 10, 0, 0, -41, 37, 0,
		0, -39, 3, 0, 0, -16, 6, 0, 0, -87, 9, 0, 0, 116, 19, 0, 0, 81, 7, 3, 0, 23, 3, 0, 0, 117,
		9, 0, 0, -55, 5, 0, 0, 45, 24, 0, 0, -53, 5, 0, 0, 72, 2, 0, 0, 28, 7, 3, 0, 49, 17, 0, 0,
		85, 14, 0, 0, 17, 15, 0, 0, 84, 8, 0, 0, -81, 1, 0, 0, 49, 3, 0, 0, -81, 7, 3, 0, 94, 7, 3,
		0, -87, 2, 0, 0, 1, 7, 7, 0, 1, 7, 27, 0, 8, 7, 51, 0, 1, 7, 3, 0, 4, 7, 3, 0, 8, 7, 11, 0,
		1, 7, 91, 0, 4, 7, 91, 0, 11, 7, 71, 0, 8, 7, 23, 0, 28, 7, 31, 0, 54, 1, 7, 70, 0, -37, 7,
		23, 0, 110, 1, 7, 14, 0
	};
}
