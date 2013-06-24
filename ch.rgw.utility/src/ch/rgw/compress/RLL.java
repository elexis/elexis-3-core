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

import ch.rgw.tools.IntTool;

/**
 * RLL Commpressor/Decompressor
 * 
 * @author Gerry
 */
public class RLL {
	public static String Version(){
		return "0.2.1";
	}
	
	/**
	 * compress the source array. use rllchar as character to indicate repetition. if rllchar is
	 * 0x0, the least frequent character from source will be used.
	 * 
	 * @return a newly created array with the rll compressed data.
	 */
	public static byte[] compress(byte rllchar, byte[] source){
		if (rllchar == (byte) 0) {
			int[] freq = HuffmanTree.constructTable(source);
			int min = 100000;
			for (int i = 0; i < freq.length; i++) {
				if (freq[i] == 0) {
					rllchar = (byte) i;
					break;
				}
				if (freq[i] < min) {
					min = freq[i];
					rllchar = (byte) i;
				}
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(source.length);
		baos.write(rllchar);
		int i;
		for (i = 0; i < source.length - 3; i++) {
			if ((source[i] == source[i + 1]) && (source[i + 1] == source[i + 2])) {
				int rep = 3;
				while (((i + rep) < source.length) && source[i + rep] == source[i]) {
					if (rep == 254)
						break;
					rep++;
				}
				baos.write(rllchar);
				baos.write(rep);
				baos.write(source[i]);
				i += rep - 1;
			} else if (source[i] == rllchar) {
				baos.write(rllchar);
				baos.write(1);
			} else {
				baos.write(source[i]);
			}
		}
		while (i < source.length) {
			baos.write(source[i++]);
		}
		return baos.toByteArray();
		
	}
	
	/**
	 * Expand a rll-compressed array
	 * 
	 * @param source
	 *            the compressed array. First byte must be the rllchar.
	 * @return a newly created array with the expanded data.
	 */
	public static byte[] expand(byte[] source){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(source.length);
		byte rllchar = source[0];
		int i;
		for (i = 1; i < source.length - 2; i++) {
			if (source[i] == rllchar) {
				if (source[i + 1] == 1) {
					baos.write(rllchar);
					i++;
				} else {
					int k = IntTool.ByteToInt(source[i + 1]);
					while (k-- > 0) {
						baos.write(source[i + 2]);
					}
					i += 2;
				}
			} else {
				baos.write(source[i]);
			}
			
		}
		while (i < source.length) {
			baos.write(source[i++]);
		}
		
		return baos.toByteArray();
	}
	
}
