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
package ch.elexis.scripting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import ch.elexis.data.PersistentObject;

public class Counter {
	private ArrayList<Float> list = new ArrayList<Float>();
	private Hashtable<Object, Integer> hash = new Hashtable<Object, Integer>();
	
	public void add(float i){
		list.add(new Float(i));
	}
	
	public void clear(){
		list.clear();
	}
	
	public float getAverage(int digits){
		float sum = 0;
		for (Float in : list) {
			sum += in;
		}
		float multiplyer = (float) Math.pow(10.0, digits);
		return Math.round(multiplyer * sum / list.size()) / multiplyer;
	}
	
	public float getMedian(){
		Collections.sort(list);
		int size = list.size();
		int center = size >> 1;
		float f1 = list.get(center);
		if ((size & 1) == 0) {
			float f2 = list.get(center + 1);
			return (f1 + f2) / 2;
		}
		return f1;
	}
	
	public void add(Object o){
		System.out.println(((PersistentObject) o).getLabel());
		Integer c = hash.get(o);
		int cx = 0;
		if (c == null) {
			cx = 1;
		} else {
			cx = c + 1;
		}
		hash.put(o, new Integer(cx));
	}
	
	public ObjCounter[] getTopList(int num){
		ArrayList<ObjCounter> aoc = new ArrayList<ObjCounter>(hash.size());
		for (Object o : hash.keySet()) {
			aoc.add(new ObjCounter(o, hash.get(o)));
		}
		Collections.sort(aoc, new Comparator<ObjCounter>() {
			
			public int compare(ObjCounter arg0, ObjCounter arg1){
				return arg1.count - arg0.count;
			}
			
		});
		num = Math.min(num, aoc.size());
		ObjCounter[] ret = new ObjCounter[num];
		for (int i = 0; i < num; i++) {
			ret[i] = aoc.get(i);
		}
		return ret;
	}
	
	public String getTopListAsString(int num){
		ObjCounter[] cnt = getTopList(num);
		StringBuilder sb = new StringBuilder();
		for (ObjCounter oc : cnt) {
			sb.append(oc.count).append("\t\t"); //$NON-NLS-1$
			PersistentObject po = (PersistentObject) oc.obj;
			sb.append(po.getLabel()).append("\n"); //$NON-NLS-1$
		}
		return sb.toString();
	}
	
	class ObjCounter {
		ObjCounter(Object obj, int num){
			this.obj = obj;
			count = num;
		}
		
		Object obj;
		int count;
	}
}
