/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class SortedList<T> extends LinkedList<T> {
	private Comparator<T> cmp;
	
	public SortedList(Comparator<T> comp){
		cmp = comp;
	}
	
	public SortedList(Collection<T> source, Comparator<T> comp){
		super(source);
		cmp = comp;
		sort();
	}
	
	public void sort(){
		Collections.sort(this, cmp);
	}
	
	@Override
	public boolean add(T elem){
		if (super.add(elem)) {
			sort();
			return true;
		}
		return false;
	}
	
}
