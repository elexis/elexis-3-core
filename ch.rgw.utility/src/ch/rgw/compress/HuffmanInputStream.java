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
import java.io.InputStream;

import ch.rgw.io.BitInputStream;
import ch.rgw.tools.IntTool;

/**
 * A Stream that decompresses an earlier created HuffmanOutputStream Tree and dynamic feature are
 * read from the Stream header.
 * 
 * @author Gerry
 */
public class HuffmanInputStream extends InputStream {
	public static String Version(){
		return "0.5.4";
	}
	
	InputStream in;
	BitInputStream bis;
	HuffmanTree tree = new HuffmanTree();
	int dyn;
	int[] tbl;
	int counter;
	
	public HuffmanInputStream(InputStream in) throws IOException{
		this.in = in;
		byte[] sig = new byte[HuffmanOutputStream.signature.length];
		in.read(sig);
		for (int i = 0; i < sig.length; i++) {
			if (sig[i] != HuffmanOutputStream.signature[i]) {
				throw new IOException("Bad Stream Header");
			}
		}
		dyn = IntTool.readInt(in);
		tree.build(HuffmanTree.loadTable(in));
		bis = new BitInputStream(in);
		if (dyn != 0) {
			tbl = new int[HuffmanTree.TABLESIZE];
			counter = 0;
		}
	}
	
	/*
	 * Read a bit sequence long enough to make up a byte an return that byte
	 */
	public int read() throws IOException{
		int ret = Huff.readByte(tree.getRootNode(), bis);
		if ((ret != -1) && (dyn > 0)) {
			tbl[ret]++;
			if (++counter == dyn) {
				tree.build(tbl);
				tbl = new int[HuffmanTree.TABLESIZE];
				counter = 0;
			}
		}
		
		return ret;
	}
	
	public int available() throws IOException{
		return bis.available();
	}
}
