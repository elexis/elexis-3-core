/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A class to keep track of the usage of certain objects
 * 
 * @author Gerry
 * 
 * @param <T>
 *            must implement Serializable
 */

public class MFUList<T> implements Iterable<T>, Serializable {
	private static final long serialVersionUID = 3966224865760348882L;
	private ArrayList<Entry<T>> list;
	int maxNum;
	
	public MFUList(int objectsToStart, int objectsToKeep){
		list = new ArrayList<Entry<T>>(objectsToStart);
		maxNum = objectsToKeep;
	}
	
	public void count(T obj){
		Iterator<Entry<T>> it = list.iterator();
		while (it.hasNext()) {
			Entry<T> e = it.next();
			if (e.o == null) {
				it.remove();
				continue;
			}
			if (e.o.equals(obj)) {
				if (e.count++ > 20000) {
					for (Entry<T> x : list) {
						if (x.count > 0) {
							x.count -= 20000;
						}
					}
				}
				Collections.sort(list);
				return;
			}
		}
		while (list.size() > maxNum) {
			list.remove(list.size() - 1);
		}
		
		list.add(new Entry<T>(obj));
		Collections.sort(list);
	}
	
	public List<T> getAll(){
		ArrayList<T> ret = new ArrayList<T>();
		for (Entry<T> e : list) {
			ret.add(e.o);
		}
		return ret;
	}
	
	public int getIndex(T obj){
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).o.equals(obj)) {
				return i;
			}
		}
		return -1;
	}
	
	class Entry<X> implements Comparable<Entry<X>>, Serializable {
		private static final long serialVersionUID = 5090900795191382845L;
		int count;
		X o;
		
		public Entry(X obj){
			o = obj;
			count = 0;
		}
		
		public int compareTo(Entry<X> obj){
			return obj.count - count;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj){
			if (obj instanceof Entry) {
				Entry<X> e = (Entry<X>) obj;
				return o.equals(e.o);
			}
			return false;
		}
		
	}
	
	public Iterator<T> iterator(){
		return new It();
	}
	
	class It implements Iterator<T> {
		Iterator<Entry<T>> li;
		
		It(){
			li = list.iterator();
		}
		
		public boolean hasNext(){
			return li.hasNext();
		}
		
		public T next(){
			return li.next().o;
		}
		
		public void remove(){
			li.remove();
		}
		
	}
}
