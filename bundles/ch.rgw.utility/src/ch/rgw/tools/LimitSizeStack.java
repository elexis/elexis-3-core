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

import java.util.LinkedList;

/**
 * A Stack that has a limited capacity. If more elements are pushed in on top, oldest elements are
 * "pushed out" at the bottom. In all other respects, this class behaves like any other stack
 * implementation
 * 
 * @author gerry
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class LimitSizeStack<T> extends LinkedList<T> {
	private int max;
	
	public LimitSizeStack(int limit){
		max = limit;
	}
	
	public void push(T elem){
		if (size() >= max) {
			remove(size() - 1);
		}
		add(0, elem);
	}
	
	public T pop(){
		if (size() == 0) {
			return null;
		}
		return remove(0);
	}
}
