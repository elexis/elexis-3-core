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
package ch.elexis.core.ui.views.textsystem;

import java.util.Comparator;
import java.util.List;

import ch.elexis.core.data.util.SortedList;

public class PlatzhalterTreeData {
	private PlatzhalterTreeData parent;
	final private SortedList<PlatzhalterTreeData> childrenList =
		new SortedList<PlatzhalterTreeData>(new TreeComparator());
	
	final private String name;
	final private String key;
	final private String description;
	
	class TreeComparator implements Comparator<PlatzhalterTreeData> {
		public int compare(PlatzhalterTreeData o1, PlatzhalterTreeData o2){
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	public PlatzhalterTreeData(final PlatzhalterTreeData _parent, final String _name,
		final String _key, final String _description){
		super();
		if (parent != null) {
			if (_name.startsWith(parent.getName())) {
				this.name = _name.substring(parent.getName().length());
			} else {
				this.name = _name;
			}
			
		} else {
			this.name = _name;
		}
		this.key = _key;
		this.description = _description;
		setParent(_parent);
	}
	
	public PlatzhalterTreeData(final String _name, final String _key, final String _description){
		this(null, _name, _key, _description);
	}
	
	public PlatzhalterTreeData getChild(final String name){
		for (PlatzhalterTreeData ptd : childrenList) {
			if (name.equals(ptd.getName())) {
				return ptd;
			}
		}
		return null;
	}
	
	public void addChild(final PlatzhalterTreeData child){
		if (!childrenList.contains(child)) {
			childrenList.add(child);
		}
	}
	
	public void addChildren(final List<PlatzhalterTreeData> children){
		for (PlatzhalterTreeData ptd : children) {
			addChild(ptd);
		}
	}
	
	private void setParent(final PlatzhalterTreeData _parent){
		if (_parent != null) {
			this.parent = _parent;
			this.parent.addChild(this);
		}
	}
	
	public SortedList<PlatzhalterTreeData> getChildren(){
		return childrenList;
	}
	
	public PlatzhalterTreeData getParent(){
		return this.parent;
	}
	
	public String getDescription(){
		return this.description;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getKey(){
		return this.key;
	}
}
