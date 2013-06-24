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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.rgw.io.BitInputStream;
import ch.rgw.io.BitOutputStream;
import ch.rgw.tools.ExHandler;

/**
 * GLZ is a simple implementation of a variation of the lempel-ziv algorithm. It compresses much
 * less than zip, but is fast enough to be implemented in pure Java. (I did program it in assembler
 * originally in the early 90's though; this alorithm programmed in assembler and running on a 80386
 * of those days was even slower than running on a computer of today in this java programm, which is
 * essentially a transscript of the original Assembler source)
 * 
 * @author Gerry
 */
public class GLZ {
	public static String Version(){
		return "1.0.0";
	}
	
	private static final int LZBUFFSIZE = 0x8cf2;
	private static final short ofb = 4;
	private int[] buff1, buff2, buff3;
	int LZCode, MaxCode, bitcount;
	
	public GLZ(){
		buff1 = new int[LZBUFFSIZE];
		buff2 = new int[LZBUFFSIZE];
		buff3 = new int[LZBUFFSIZE / 2];
	}
	
	public int compress(InputStream in, OutputStream o) throws IOException{
		BitOutputStream bos = new BitOutputStream(o);
		int size = 0;
		fillbuffer();
		int Prev = in.read();
		if (Prev == -1) {
			Prev = 0x100;
		}
		
		while (in.available() > 0) {
			int Act = in.read();
			size++;
			int hash = (Act << 6) ^ Prev;
			int cnt1;
			if (hash != 0) {
				cnt1 = (LZBUFFSIZE / 2) - hash;
			} else {
				cnt1 = 1;
			}
			
			hash = findHash(hash, Prev, Act, cnt1); // find code or empty place in hashtable
			if (buff1[hash] != -1) // code found
			{
				Prev = buff1[hash]; // use it
				continue;
			}
			buff1[hash] = LZCode++; // empty place found
			buff2[hash] = Prev;
			buff3[hash] = (byte) Act;
			bos.pushbits(Prev, bitcount); // write new code
			Prev = Act;
			if (LZCode > 0x3ffe) // dictionary exhausted
			{
				bos.pushbits(0x102, bitcount); // new dictionary
				fillbuffer();
				continue;
			}
			if (LZCode > MaxCode) // codespace exhausted
			{
				bos.pushbits(0x101, bitcount++); // extend code length
				MaxCode <<= 1;
				MaxCode |= 1;
			}
		} /* WHILE */
		bos.pushbits(Prev, bitcount); // last code
		bos.pushbits(0x100, bitcount); // eof
		bos.pushbits(0, bitcount); // fill
		bos.flush(); // write rest
		return size + 1;
	}
	
	private int findHash(int hash, int Prev, int Act, int cnt1){
		while (buff1[hash] != -1) {
			if ((buff2[hash] == Prev) && (buff3[hash] == (byte) Act)) {
				break; // found
			}
			hash -= cnt1; // rehash
			if (hash < 0)
				hash += (LZBUFFSIZE / 2);
		}
		return hash;
	}
	
	private void fillbuffer(){
		for (int i = 0; i < buff1.length; i++) {
			buff1[i] = -1;
			
		}
		LZCode = 0x103;
		bitcount = 9;
		MaxCode = 0x1ff;
	}
	
	public void expand(InputStream i, OutputStream o) throws IOException{
		int Prev, Prev1, Act, dx;
		BitInputStream bis = new BitInputStream(i);
		
		newdic: while (true) {
			LZCode = 0x103;
			bitcount = 9;
			MaxCode = 0x1ff;
			Prev1 = bis.pullBits(bitcount);
			if (Prev1 == 0x100) {
				o.flush();
				return;
			}
			Act = Prev = Prev1;
			o.write(Act);
			
			while (Prev1 != 0x100) {
				dx = Prev1 = (short) bis.pullBits(bitcount);
				switch (Prev1) {
				case 0x101:
					bitcount++; // Extend codesize and fall thru
				case 0x100:
					continue; // EOF; will break at "while"
				case 0x102:
					continue newdic; // Begin new dictionary
				default:
					int si = ofb;
					if (Prev1 >= LZCode) {
						buff1[si++] = Act;
						dx = Prev;
					}
					while (dx > 0xff) {
						buff1[si++] = buff3[dx];
						dx = buff2[dx];
					}
					
					buff1[si] = Act = dx;
					
					do {
						o.write(buff1[si--]);
					} while (si >= ofb);
					
					buff2[LZCode] = Prev;
					buff3[LZCode++] = (byte) Act;
					Prev = Prev1;
					
				} // case
			} // while;
			break; // EOF (0x100) received
		} // while(true)
		o.flush();
	}
	
	public byte[] encodeString(String input){
		byte[] b = input.getBytes();
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			compress(in, out);
			return out.toByteArray();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
		
	}
	
	public String decodeString(byte[] i){
		ByteArrayInputStream in = new ByteArrayInputStream(i);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			expand(in, out);
			byte[] cp = out.toByteArray();
			return new String(cp);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
}
