/*******************************************************************************
 * Copyright (c) 2005-2008, G. Weirich and Elexis
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

import java.util.Collection;
import java.util.Comparator;

/**
 * Ein Tree, der seine Children erst bei Bedarf lädt. Dazu muss ein LazyTreeListener übergeben
 * werden, der die Children liefern muss.
 * 
 * @author gerry
 * 
 */
public class LazyTree<T> extends Tree<T> {
	LazyTreeListener listen;
	
	public LazyTree(Tree<T> p, T elem, LazyTreeListener l, Comparator<T> comp){
		super(p, elem, comp);
		listen = l;
	}
	
	public LazyTree(Tree<T> p, T elem, LazyTreeListener l){
		super(p, elem);
		listen = l;
	}
	
	public LazyTree(Tree<T> p, T elem, IFilter f, LazyTreeListener l){
		super(p, elem, f);
		listen = l;
	}
	
	public Collection<Tree<T>> getChildren(){
		loadChildren();
		return super.getChildren();
	}
	
	public boolean hasChildren(){
		if (first == null) {
			return (listen == null ? false : listen.hasChildren(this));
		}
		return true;
	}
	
	public LazyTree<T> add(T elem, LazyTreeListener l){
		LazyTree<T> ret = new LazyTree<T>(this, elem, filter, l);
		return ret;
	}
	
	// Stack Overflow?? //TODO
	private void loadChildren(){
		if ((first == null) && (listen != null)) {
			listen.fetchChildren(this);
		}
	}
	
	public Tree<T> getFirstChild(){
		loadChildren();
		return first;
	}
	
	public interface LazyTreeListener {
		/**
		 * fetch children of this node.
		 * 
		 * @param l
		 * @return true if children were added
		 */
		public boolean fetchChildren(LazyTree<?> l);
		
		/**
		 * return true if this node has children
		 * 
		 * @param l
		 * @return
		 */
		public boolean hasChildren(LazyTree<?> l);
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	@Override
	public synchronized Tree move(Tree newParent){
		if (!(newParent instanceof LazyTree)) {
			preload();
			
		}
		return super.move(newParent);
	}
	
	public Tree preload(){
		loadChildren();
		for (Tree child = first; child != null; child = child.next) {
			if (child instanceof LazyTree) {
				((LazyTree) child).preload();
			}
		}
		return this;
	}
	
}
