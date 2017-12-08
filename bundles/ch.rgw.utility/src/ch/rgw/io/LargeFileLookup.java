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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Lookup Strings in an arbitrarly large sorted file This is a quite trivial implemetation with
 * binary sort in the file.
 * 
 * @author Gerry Weirich
 * 
 */
public class LargeFileLookup {
	RandomAccessFile raf;
	long len;
	
	public LargeFileLookup(File file) throws FileNotFoundException{
		len = file.length();
		raf = new RandomAccessFile(file, "r");
	}
	
	public List<String> binarySearch(String search) throws IOException{
		String string = search.toLowerCase();
		List<String> result = new ArrayList<String>();
		long low = 0;
		long high = len;
		
		long p = -1;
		while (low < high) {
			long mid = (low + high) / 2;
			p = mid;
			while (p >= 0) {
				raf.seek(p);
				char c = (char) raf.readByte();
				if (c == '\n')
					break;
				p--;
			}
			if (p < 0)
				raf.seek(0);
			String line = raf.readLine();
			if (line.toLowerCase().compareTo(string) < 0)
				low = mid + 1;
			else
				high = mid;
		}
		
		p = low;
		while (p >= 0) {
			raf.seek(p);
			if (((char) raf.readByte()) == '\n')
				break;
			p--;
		}
		
		if (p < 0)
			raf.seek(0);
		
		while (true) {
			String line = raf.readLine();
			if (line == null || !line.toLowerCase().startsWith(string))
				break;
			result.add(line);
		}
		
		return result;
	}
	
}
