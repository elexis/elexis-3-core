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

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream for bitwise reading.
 * 
 * @author Gerry
 */
public class BitInputStream extends InputStream {
	public static final String Version(){
		return "0.3.3";
	}
	
	private int buffer;
	private int pos;
	InputStream stream;
	
	public BitInputStream(InputStream in){
		stream = in;
		pos = 0;
	}
	
	/**
	 * reads a byte, starting not at byte boundaries, but at the last read bit.
	 * 
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException{
		return pullBits(8);
	}
	
	/**
	 * reads up to 32 bits and returns them as int.
	 * 
	 * @param bitnum
	 *            number of bits to read
	 * @return the integer containing the requested bits (left-padded with zero)
	 * @throws IOException
	 */
	public int pullBits(int bitnum) throws IOException{
		int mask = 1 << (bitnum - 1);
		int ret = 0;
		while (mask != 0) {
			if (readBit() == true) {
				ret |= mask;
			}
			mask >>= 1;
		}
		return ret;
	}
	
	/**
	 * reads a single bit
	 * 
	 * @return true for an 1-Bit, false for a 0-Bit
	 * @throws IOException
	 */
	public boolean readBit() throws IOException{
		if (pos == 0) {
			buffer = stream.read();
			pos = 128;
		}
		boolean res = (buffer & pos) == pos;
		pos >>= 1;
		return res;
	}
	
	/**
	 * Tells whether one or more bits can be read without blocking. CAUTION: The returned number
	 * does <b>not</b> accurately indicate the number of waiting bits (Only zero or non-zero is
	 * guaranteed)
	 */
	public int available() throws IOException{
		if (stream.available() > 0)
			return 1;
		if (pos > 0)
			return 1;
		return 0;
	}
	
	public void close() throws IOException{
		stream.close();
	}
	
}
