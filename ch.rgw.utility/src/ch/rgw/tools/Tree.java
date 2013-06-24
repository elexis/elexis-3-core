/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 * Eine Baumförmige rekursive Datenstruktur. Ein Tree ist gleicheitig ein node. Ein Tree hat
 * children (die allerdings auch null sein können) und Geschwister, (die ebenfalls null sein
 * können), sowie ein Parent, welches ebenfalls null sein kann (dann ist dieses Tree-Objekt die
 * Wurzel des Baums) Jeder Tree trägt ein beliebiges Datenobjekt (contents).
 */
public class Tree<T> {
	public IFilter filter;
	protected Tree<T> parent;
	protected Tree<T> first;
	protected Tree<T> next;
	// protected Tree<T> last;
	public T contents;
	
	/**
	 * Eine neues Tree-Objekt erstellen
	 * 
	 * @param p
	 *            der Parent, oder null, wenn dies die Wurzel werden soll.
	 * @param elem
	 *            das zugeordnete Datenobjekt
	 */
	public Tree(Tree<T> p, T elem){
		contents = elem;
		parent = p;
		first = null;
		// last=null;
		filter = null;
		if (parent != null) {
			next = parent.first;
			parent.first = this;
		}
	}
	
	/**
	 * Ein neues Tree-Objekt innerhalb der Geschwisterliste sortiert einfügen
	 * 
	 * @param parent
	 *            Parent
	 * @param elem
	 *            Datenobjekt
	 * @param comp
	 *            Ein Comparator für das Fatenobjekt
	 */
	public Tree(Tree<T> parent, T elem, Comparator<T> comp){
		this.parent = parent;
		contents = elem;
		if (parent != null) {
			next = parent.first;
			Tree<T> prev = null;
			while ((next != null) && (comp.compare(next.contents, elem) < 0)) {
				prev = next;
				next = next.next;
			}
			if (prev == null) {
				parent.first = this;
			} else {
				prev.next = this;
			}
		}
	}
	
	/**
	 * Ein neues Tree-Objekt mit einem Filter erstellen. Wenn ein Filter gesetzt wird, dann werden
	 * von getChildren() nur die geliefert, die dem Filter entsprechen
	 * 
	 * @param p
	 *            Parent-Element
	 * @param elem
	 *            Datenobjekt
	 * @param f
	 *            Filter
	 */
	public Tree(Tree<T> p, T elem, IFilter f){
		this(p, elem);
		filter = f;
	}
	
	/**
	 * Filter nachträglich setzen. Der Filter wird für dieses und alle Children gesetzt.
	 * 
	 * @param f
	 *            der Filter
	 */
	public void setFilter(IFilter f){
		filter = f;
		Tree<T> cursor = first;
		while (cursor != null) {
			cursor.setFilter(f);
			cursor = cursor.next;
		}
	}
	
	/**
	 * Ein Datenobjekt als Kind-element zufügen. Dies (Das Datenobjekt wird implizit in ein
	 * Tree-Objekt gepackt. obj.add(t) ist dasselbe wie new Tree(obj,t))
	 * 
	 * @param elem
	 *            Das Datenobjekt
	 * @return das erzeugte Tree-Objekt
	 */
	public Tree<T> add(T elem){
		Tree<T> ret = new Tree<T>(this, elem, filter);
		return ret;
	}
	
	/**
	 * Ein Kind-Element samt dessen Unterelementen entfernen
	 * 
	 * @param subtree
	 *            das Kindelement
	 */
	public void remove(Tree<T> subtree){
		if (first == null) {
			return;
		}
		if (first.equals(subtree)) {
			first = subtree.next;
			return;
		}
		
		Tree<T> runner = first;
		
		while (!runner.next.equals(subtree)) {
			runner = runner.next;
			if (runner == null) {
				return;
			}
		}
		runner.next = subtree.next;
	}
	
	/**
	 * An einen anderen Parenet-Node oder Tree zügeln (Mitsamt allen Kindern)
	 * 
	 * @param newParent
	 *            der neue Elter
	 */
	public synchronized Tree<T> move(Tree<T> newParent){
		Tree<T> oldParent = parent;
		if (oldParent != null) {
			oldParent.remove(this);
		}
		parent = newParent;
		next = newParent.first;
		newParent.first = this;
		return this;
	}
	
	/**
	 * Ähnlich wie add, aber wenn das übergebene Child schon existiert, werden nur dessen Kinder mit
	 * den Kindern des existenten childs ge'merged' (Also im Prinzip ein add mit Vermeidung von
	 * Dubletten
	 */
	public synchronized void merge(Tree<T> newChild){
		Tree<T> tExist = find(newChild.contents, false);
		if (tExist != null) {
			for (Tree<T> ts = newChild.first; ts != null; ts = ts.next) {
				tExist.merge(ts);
			}
			if (newChild.first == null) {
				newChild.getParent().remove(newChild);
			}
		} else {
			newChild.move(this);
		}
		
	}
	
	/**
	 * Alle Kind-Elemente entfernen
	 * 
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public synchronized void clear(){
		for (Tree t : getChildren()) {
			remove(t);
		}
	}
	
	/**
	 * Alle Kind-Elemente liefern
	 * 
	 * @return eine Collection mit den Kind-Trees
	 */
	public Collection<Tree<T>> getChildren(){
		ArrayList<Tree<T>> al = new ArrayList<Tree<T>>();
		Tree<T> cursor = first;
		while (cursor != null) {
			if (filter == null) {
				al.add(cursor);
			} else {
				if (filter.select(cursor.contents) || cursor.hasChildren()) {
					al.add(cursor);
				}
			}
			cursor = cursor.next;
		}
		return al;
	}
	
	/**
	 * Das Elternobjekt liefern
	 * 
	 * @return das parent
	 */
	public Tree<T> getParent(){
		return parent;
	}
	
	/**
	 * Erstes Kind-element liefern. Null, wenn keine Kinder. Dies macht im Gegensatz zu
	 * hasChildren() keine synchronisation!
	 * 
	 * @return
	 */
	public Tree<T> getFirstChild(){
		return first;
	}
	
	/**
	 * Nächstes Geschwister liefern oder null wenn keine mehr da sind. getParent().getFirstChild()
	 * liefert den Start der Geschwisterliste.
	 * 
	 * @return
	 */
	public Tree<T> getNextSibling(){
		return next;
	}
	
	/**
	 * Fragen, ob Kinder vorhanden sind
	 * 
	 * @return true wenn dieses Objekt Children hat.
	 */
	public boolean hasChildren(){
		if (filter == null) {
			return (first != null);
		}
		Tree<T> cursor = first;
		while (cursor != null) {
			if (filter.select(cursor.contents) || cursor.hasChildren()) {
				return true;
			}
			cursor = cursor.next;
		}
		return false;
	}
	
	/**
	 * Ein Array mit allen Elementen des Baums liefern
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Tree<T>[] toArray(){
		return (Tree<T>[]) getAll().toArray();
	}
	
	/**
	 * Eine Liste mit allen Elementen des Baums liefern
	 * 
	 * @return
	 */
	public Collection<Tree<T>> getAll(){
		ArrayList<Tree<T>> al = new ArrayList<Tree<T>>();
		Tree<T> child = first;
		while (child != null) {
			al.add(child);
			al.addAll(child.getAll());
			child = child.next;
		}
		return al;
	}
	
	public Tree<T> find(Object o, boolean deep){
		for (Tree<T> t : getChildren()) {
			if (t.contents.equals(o)) {
				return t;
			}
			if (deep) {
				Tree<T> ct = t.find(o, true);
				if (ct != null) {
					return ct;
				}
			}
		}
		return null;
	}
	
}
