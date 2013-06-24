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

import java.io.IOException;
import java.io.OutputStream;

import ch.rgw.io.BitOutputStream;
import ch.rgw.tools.IntTool;

/**
 * A Stream that compresses its output on the fly with the Huffmann algorithm<br>
 * A Huffman tree can be provided by the caller. The tree can be declared dynamic to be recomputed
 * regularly (for not in advance analyzable Stream with very variable data).
 * 
 * @author Gerry
 */
public class HuffmanOutputStream extends OutputStream {
	public static String Version(){
		return "0.1.4";
	}
	
	static final byte[] signature = new byte[] {
		'H', 'O', 'S', '0', '4'
	};
	BitOutputStream bos;
	HuffmanTree tree;
	int dyn;
	int counter;
	int[] tbl;
	
	/**
	 * The only Constructor
	 * 
	 * @param sup
	 *            a Stream to receive the output ultimately
	 * @param tree
	 *            tree a precomputed Huffman tree or null. If null, a standard tree for textual
	 *            files will be used.
	 * @param dynamic
	 *            if !=0: The tree will be recomputed every <dynamic> bytes
	 */
	public HuffmanOutputStream(OutputStream sup, HuffmanTree tr, int dynamic) throws IOException{
		if (tr == null) {
			tree = new HuffmanTree();
			tbl = HuffmanTree.useTable(HuffmanTree.TextDeutsch);
		} else {
			tree = tr;
			tbl = tree.getTable();
		}
		tree.build(tbl);
		sup.write(signature);
		IntTool.writeInt(dynamic, sup);
		tree.saveTable(sup);
		bos = new BitOutputStream(sup);
		dyn = dynamic;
		if (dyn != 0) {
			tbl = new int[HuffmanTree.TABLESIZE];
		}
		
	}
	
	public void write(int c) throws IOException{
		if (Huff.writeByte(tree.getRootNode(), bos, c) == false) { // System.out.println("Escaped");
		}
		
		if (dyn > 0) {
			tbl[c]++;
			if (++counter == dyn) {
				tree.build(tbl);
				tbl = new int[HuffmanTree.TABLESIZE];
				counter = 0;
			}
		}
	}
	
	public void flush() throws IOException{
		bos.flush();
	}
	
	/**
	 * terminate the stream: An EOF marker is written and he Stream is closed. If you don't store
	 * Informations about the length of the original data, you should always end with close(),
	 * because HuffmanInputStream can not determine the end of the compressed data.
	 */
	public void close() throws IOException{ // Huff.writeByte(tree.getRootNode(),bos,Huff.escape);
		// write(Huff.eof);
		Huff.writeEOF(tree.getRootNode(), bos);
		bos.close();
	}
}
