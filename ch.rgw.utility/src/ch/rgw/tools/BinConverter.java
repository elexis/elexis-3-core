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

/**
 * Some helper routines for data conversion, all data is treated in network byte order.
 */
public class BinConverter {
	/**
	 * Gets bytes from an array into an integer.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param nOfs
	 *            index from where to read the data
	 * @return the 32bit integer
	 */
	public final static int byteArrayToInt(byte[] buf, int nOfs){
		return (buf[nOfs] << 24) | ((buf[nOfs + 1] & 0x0ff) << 16) | ((buf[nOfs + 2] & 0x0ff) << 8)
			| (buf[nOfs + 3] & 0x0ff);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts an integer to bytes, which are put into an array.
	 * 
	 * @param nValue
	 *            the 32bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param nOfs
	 *            where to place the bytes in the buf
	 */
	public final static void intToByteArray(int nValue, byte[] buf, int nOfs){
		buf[nOfs] = (byte) ((nValue >>> 24) & 0x0ff);
		buf[nOfs + 1] = (byte) ((nValue >>> 16) & 0x0ff);
		buf[nOfs + 2] = (byte) ((nValue >>> 8) & 0x0ff);
		buf[nOfs + 3] = (byte) nValue;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets bytes from an array into a long.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param nOfs
	 *            index from where to read the data
	 * @return the 64bit integer
	 */
	public final static long byteArrayToLong(byte[] buf, int nOfs){
		// Looks more complex - but it is faster (at least on 32bit platforms).
		
		return ((long) ((buf[nOfs] << 24) | ((buf[nOfs + 1] & 0x0ff) << 16)
			| ((buf[nOfs + 2] & 0x0ff) << 8) | (buf[nOfs + 3] & 0x0ff)) << 32)
			| (((buf[nOfs + 4] << 24) | ((buf[nOfs + 5] & 0x0ff) << 16)
				| ((buf[nOfs + 6] & 0x0ff) << 8) | (buf[nOfs + 7] & 0x0ff)) & 0x0ffffffffL);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts a long to bytes, which are put into an array.
	 * 
	 * @param lValue
	 *            the 64bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param nOfs
	 *            where to place the bytes in the buf
	 */
	public final static void longToByteArray(long lValue, byte[] buf, int nOfs){
		int nTmp = (int) (lValue >>> 32);
		
		buf[nOfs] = (byte) (nTmp >>> 24);
		buf[nOfs + 1] = (byte) ((nTmp >>> 16) & 0x0ff);
		buf[nOfs + 2] = (byte) ((nTmp >>> 8) & 0x0ff);
		buf[nOfs + 3] = (byte) nTmp;
		
		nTmp = (int) lValue;
		
		buf[nOfs + 4] = (byte) (nTmp >>> 24);
		buf[nOfs + 5] = (byte) ((nTmp >>> 16) & 0x0ff);
		buf[nOfs + 6] = (byte) ((nTmp >>> 8) & 0x0ff);
		buf[nOfs + 7] = (byte) nTmp;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts values from an integer array to a long.
	 * 
	 * @param buf
	 *            where to get the bytes
	 * @param nOfs
	 *            index from where to read the data
	 * @return the 64bit integer
	 */
	public final static long intArrayToLong(int[] buf, int nOfs){
		return (((long) buf[nOfs]) << 32) | (buf[nOfs + 1] & 0x0ffffffffL);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts a long to integers which are put into an array.
	 * 
	 * @param lValue
	 *            the 64bit integer to convert
	 * @param buf
	 *            the target buf
	 * @param nOfs
	 *            where to place the bytes in the buf
	 */
	public final static void longToIntArray(long lValue, int[] buf, int nOfs){
		buf[nOfs] = (int) (lValue >>> 32);
		buf[nOfs + 1] = (int) lValue;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Makes a long from two integers (treated unsigned).
	 * 
	 * @param nLo
	 *            lower 32bits
	 * @param nHi
	 *            higher 32bits
	 * @return the built long
	 */
	public final static long makeLong(int nLo, int nHi){
		return (((long) nHi << 32) | (nLo & 0x00000000ffffffffL));
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the lower 32 bits of a long.
	 * 
	 * @param lVal
	 *            the long integer
	 * @return lower 32 bits
	 */
	public final static int longLo32(long lVal){
		return (int) lVal;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Gets the higher 32 bits of a long.
	 * 
	 * @param lVal
	 *            the long integer
	 * @return higher 32 bits
	 */
	public final static int longHi32(long lVal){
		return (int) (lVal >>> 32);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	// our table for hex conversion
	final static char[] HEXTAB = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	/**
	 * Converts a byte array to a hex string.
	 * 
	 * @param data
	 *            the byte array
	 * @return the hex string
	 */
	public final static String bytesToHexStr(byte[] data){
		return bytesToHexStr(data, 0, data.length);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts a byte array to a hex string.
	 * 
	 * @param data
	 *            the byte array
	 * @param nOfs
	 *            start index where to get the bytes
	 * @param nLen
	 *            number of bytes to convert
	 * @return the hex string
	 */
	public final static String bytesToHexStr(byte[] data, int nOfs, int nLen){
		StringBuffer sbuf;
		
		sbuf = new StringBuffer();
		sbuf.setLength(nLen << 1);
		
		int nPos = 0;
		
		int nC = nOfs + nLen;
		
		while (nOfs < nC) {
			sbuf.setCharAt(nPos++, HEXTAB[(data[nOfs] >> 4) & 0x0f]);
			sbuf.setCharAt(nPos++, HEXTAB[data[nOfs++] & 0x0f]);
		}
		return sbuf.toString();
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts a hex string back into a byte array (invalid codes will be skipped).
	 * 
	 * @param sHex
	 *            hex string
	 * @param data
	 *            the target array
	 * @param nSrcOfs
	 *            from which character in the string the conversion should begin, remember that
	 *            (nSrcPos modulo 2) should equals 0 normally
	 * @param nDstOfs
	 *            to store the bytes from which position in the array
	 * @param nLen
	 *            number of bytes to extract
	 * @return number of extracted bytes
	 */
	public final static int hexStrToBytes(String sHex, byte[] data, int nSrcOfs, int nDstOfs,
		int nLen){
		int nI, nJ, nStrLen, nAvailBytes, nDstOfsBak;
		byte bActByte;
		boolean blConvertOK;
		
		// check for correct ranges
		nStrLen = sHex.length();
		
		nAvailBytes = (nStrLen - nSrcOfs) >> 1;
		if (nAvailBytes < nLen) {
			nLen = nAvailBytes;
		}
		
		int nOutputCapacity = data.length - nDstOfs;
		if (nLen > nOutputCapacity) {
			nLen = nOutputCapacity;
		}
		
		// convert now
		
		nDstOfsBak = nDstOfs;
		
		for (nI = 0; nI < nLen; nI++) {
			bActByte = 0;
			blConvertOK = true;
			
			for (nJ = 0; nJ < 2; nJ++) {
				bActByte <<= 4;
				char cActChar = sHex.charAt(nSrcOfs++);
				
				if ((cActChar >= 'a') && (cActChar <= 'f')) {
					bActByte |= (byte) (cActChar - 'a') + 10;
				} else {
					if ((cActChar >= '0') && (cActChar <= '9')) {
						bActByte |= (byte) (cActChar - '0');
					} else {
						blConvertOK = false;
					}
				}
			}
			if (blConvertOK) {
				data[nDstOfs++] = bActByte;
			}
		}
		
		return (nDstOfs - nDstOfsBak);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * Converts a byte array into a Unicode string.
	 * 
	 * @param data
	 *            the byte array
	 * @param nOfs
	 *            where to begin the conversion
	 * @param nLen
	 *            number of bytes to handle
	 * @return the string
	 */
	public final static String byteArrayToStr(byte[] data, int nOfs, int nLen){
		int nAvailCapacity, nSBufPos;
		StringBuffer sbuf;
		
		// we need two bytes for every character
		nLen &= ~1;
		
		// enough bytes in the buf?
		nAvailCapacity = data.length - nOfs;
		
		if (nAvailCapacity < nLen) {
			nLen = nAvailCapacity;
		}
		
		sbuf = new StringBuffer();
		sbuf.setLength(nLen >> 1);
		
		nSBufPos = 0;
		
		while (0 < nLen) {
			sbuf.setCharAt(nSBufPos++, (char) ((data[nOfs] << 8) | (data[nOfs + 1] & 0x0ff)));
			nOfs += 2;
			nLen -= 2;
		}
		
		return sbuf.toString();
	}
}
