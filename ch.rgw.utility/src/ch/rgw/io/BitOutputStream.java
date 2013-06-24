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
import java.io.OutputStream;

/**
 * An OutputStream for bitwise writing
 * 
 * @author Gerry
 */
public class BitOutputStream extends OutputStream {
	public static final String Version(){
		return "0.6.5";
	}
	
	private int buffer;
	private int pos;
	private OutputStream stream;
	
	public BitOutputStream(OutputStream out){
		stream = out;
		pos = 128;
	}
	
	/**
	 * writes a byte into the OutputStream, starting at the position of the last written bit (not at
	 * a byte boundary)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int c) throws IOException{
		pushbits(c, 8);
	}
	
	/**
	 * Writes up to 32 bit
	 * 
	 * @param c
	 *            an integer containing the bits to be written
	 * @param bitnum
	 *            the number of bits (right aligned inside c) to write.
	 * @throws IOException
	 */
	public void pushbits(int c, int bitnum) throws IOException{
		int mask = 1 << (bitnum - 1);
		while (mask > 0) {
			if ((c & mask) == 0)
				write(false);
			else
				write(true);
			mask >>= 1;
		}
	}
	
	/**
	 * write a single bit
	 * 
	 * @param bit
	 *            true for a 1-Bit, false for a 0-Bit
	 * @throws IOException
	 */
	public void write(boolean bit) throws IOException{
		if (bit == true) {
			buffer |= pos;
		}
		pos >>= 1;
		if (pos == 0) {
			flush();
		}
	}
	
	/**
	 * Empty the buffer, flush the remaining bits with zeroes.
	 */
	public void flush() throws IOException{
		stream.write(buffer);
		buffer = 0;
		pos = 128;
	}
	
	public void close() throws IOException{
		flush();
		stream.write(0);
		stream.close();
	}
}
