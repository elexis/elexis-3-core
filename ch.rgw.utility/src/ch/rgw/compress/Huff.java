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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.prefs.Preferences;

import ch.rgw.compress.HuffmanTree.Node;
import ch.rgw.io.BitInputStream;
import ch.rgw.io.BitOutputStream;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.IntTool;

/**
 * Simple implementation of the Huffman compression algorithm. Consists solely of static functions.<br>
 * To compress data:
 * <ol>
 * <li>Load, create or use a frequency table for the data to compress: (Tree.constructTable(),
 * Tree.loadTable(), Tree.useTable())</li>
 * <li>Create a Huffman tree and obtain its root Node: new Tree().build(table);</li>
 * <li>compress the data: encode()</li>
 * <li>expand the data: decode()</li>
 * <p>
 * This implementation uses an extension to the straightforward algorithm to deal with the situation
 * of a character being written, which was not in the table, and with end-of-file conditions. In
 * such cases, an escape character (0x07) will be written and specially interpreted
 * </p>
 * 
 * @author Gerry
 */
public class Huff {
	public static String Version(){
		return "0.6.4";
	}
	
	public static final byte escape = 7;
	public static final int eof = 255;
	
	/**
	 * Huffman encode the source array
	 * 
	 * @param tree
	 *            a Hufmann tree as created by new Tree() or null
	 * @param source
	 *            the source to compress. If tree is null, the Tree will be computed from the source
	 *            first and its frequency table is included in the output.
	 * @return The huffman compressed source
	 */
	public static byte[] encode(HuffmanTree tree, byte[] source){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BitOutputStream bos = new BitOutputStream(baos);
		baos.write(source.length & 0xff);
		baos.write((source.length >> 8) & 0xff);
		baos.write((source.length >> 16) & 0xff);
		baos.write((source.length >> 24) & 0xff);
		if (tree == null) {
			tree = new HuffmanTree();
			tree.build(HuffmanTree.constructTable(source));
			tree.saveTable(baos);
		}
		try {
			for (int i = 0; i < source.length; i++) {
				writeByte(tree.getRootNode(), bos, source[i]);
			}
			// writeByte(tree.getRootNode(),bos,escape);
			// bos.write(eof);
			bos.flush();
			bos.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		return baos.toByteArray();
	}
	
	/**
	 * Huffman decode the source array
	 * 
	 * @param tree
	 *            a Huffman tree as created by new Tree() or null
	 * @param source
	 *            A huffman compressed array as returned by encode(). If tree is null, the frequency
	 *            count is expected to be included to the source.
	 * @return the decoded data or null on error.
	 */
	public static byte[] decode(HuffmanTree tree, byte[] source){
		ByteArrayInputStream bais = new ByteArrayInputStream(source);
		int size = bais.read() | (bais.read() << 8) | (bais.read() << 16) | (bais.read() << 24);
		BitInputStream bis = new BitInputStream(bais);
		byte[] out = new byte[size];
		Node root;
		try {
			if (tree == null) {
				root = new HuffmanTree().build(HuffmanTree.loadTable(bais));
			} else {
				root = tree.getRootNode();
			}
			for (int i = 0; i < out.length; i++) {
				out[i] = (byte) readByte(root, bis);
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		return out;
	}
	
	/** Huffman encode a String */
	public static byte[] encodeString(HuffmanTree tree, String i){
		byte[] b = i.getBytes();
		return encode(tree, b);
	}
	
	/** Decode a previously with encodeString compressed String */
	public static String decodeString(HuffmanTree tree, byte[] in){
		byte[] out = decode(tree, in);
		String res = new String(out);
		return res;
	}
	
	/**
	 * Writes a Huffman encoded byte. If the byte is not found in the Huffman tree, an escape
	 * sequence is generated (leading to an increase of the total size of the compressed file)
	 * 
	 * @param act
	 *            the root node of the Huffman tree
	 * @param out
	 *            the BitOutputStream to write the byte
	 * @param c
	 *            the byte to write
	 * @return true if the byte was written normally, false if an escape sequence was written
	 * @throws IOException
	 *             on write errors
	 */
	static boolean writeByte(Node act, BitOutputStream out, int c) throws IOException{
		if (Arrays.binarySearch(act.ch, (byte) c) < 0) // The byte was not found
		{
			writeBits(act, out, escape); // write escape
			out.write(c); // write the byte as 8 Bit
			return false;
		} else if (c == escape) // The escape character by itself
		{
			writeBits(act, out, c); // must be followed by another
			out.write(c); // escape character
			return true;
		} else {
			return writeBits(act, out, c);
		}
	}
	
	public static void writeEOF(Node act, BitOutputStream out) throws IOException{
		writeBits(act, out, escape);
		out.write(eof);
	}
	
	private static boolean writeBits(Node act, BitOutputStream out, int c) throws IOException{
		while (true) // travel through all nodes starting with act
		{
			if (act.ch.length == 1) // found if this node is the char
			{
				return true;
			}
			if (act.left != null) // search left subtree
			{
				if (Arrays.binarySearch(act.left.ch, (byte) c) >= 0) {
					out.write(false);
					act = act.left;
					continue;
				}
			}
			if (act.right != null) // search right subtree
			{
				if (Arrays.binarySearch(act.right.ch, (byte) c) >= 0) {
					out.write(true);
					act = act.right;
					continue;
				}
			}
			break;
		}
		throw new IOException("Bad Huffman code"); // Character not contained in the tree
	}
	
	/**
	 * Read a huffman encoded byte: read as many bits as necessary out of the InputStream an return
	 * the resulting byte. manages escape codes.
	 * 
	 * @param act
	 *            The root node of the Huffman tree
	 * @param in
	 *            The BitInputStream to reas from
	 * @return a single byte
	 * @throws IOException
	 */
	static int readByte(Node act, BitInputStream in) throws IOException{
		while (true) {
			if (act == null) {
				throw new IOException("Bad Huffman code");
			}
			if (act.ch.length == 1) {
				int c = IntTool.ByteToInt(act.ch[0]);
				if (c == escape) {
					int f = in.read();
					if (f == eof) {
						return -1;
					}
					return f;
				}
				return c;
			}
			if (in.readBit() == true) {
				act = act.right;
			} else {
				act = act.left;
			}
		}
	}
	
	/**
	 * The main function can be used to accomplish a few tasks regarding persistent storage of
	 * Huffman tables.:
	 * <ul>
	 * <li>java -classpath . ch.rgw.tools.Compress.Huff list: shows all stored tables in the system
	 * Preferences</li>
	 * <li>java -classpath . ch.rgw.tools.Compress.Huff create <name> <file>: computes a frequency
	 * table of the <file> and stores ist as <name> in the preferences.</li>
	 * <li>java -classpath . ch.rgw.tools.Compress.Huff export <name> <file>: exports the table
	 * <name> from the preferences into two files:
	 * <ul>
	 * <li><file>.bin: A binary representation (sequence of bytes)</li>
	 * <li><file>.asc: A textual representation (comma separated list)</li>
	 * </ul>
	 * </li>
	 * </ul>
	 */
	public static void main(String[] argv){
		try {
			if (argv[0].equals("create")) {
				FileInputStream in = new FileInputStream(argv[2]);
				HuffmanTree.CreateStandardTableFromStream(argv[1], in);
			} else if (argv[0].equals("list")) {
				Preferences pr = Preferences.userNodeForPackage(Huff.class);
				Preferences node = pr.node("StandardTables");
				String[] tables = node.keys();
				for (int i = 0; i < tables.length; i++) {
					System.out.println(tables[i]);
				}
			}
			
			else if (argv[0].equals("export")) {
				Preferences pr = Preferences.userNodeForPackage(Huff.class);
				Preferences node = pr.node("StandardTables");
				FileOutputStream exbin = new FileOutputStream(argv[2] + ".bin");
				FileOutputStream exasc = new FileOutputStream(argv[2] + ".asc");
				byte[] cmpt = node.getByteArray(argv[1], null);
				if (cmpt == null) {
					System.out.println("Table " + argv[1] + " not found");
				} else {
					int i, k = 0;
					for (i = 0; i < cmpt.length; i++) {
						exbin.write(cmpt[i]);
						if (k++ == 20) {
							exasc.write('\n');
							k = 0;
						}
						exasc.write((Byte.toString(cmpt[i]) + ",").getBytes());
					}
				}
			} else {
				System.out.println("Usage: Huff create <name> <file>\n"
					+ "or Huff export <name> <file>n" + "or Huff list");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
