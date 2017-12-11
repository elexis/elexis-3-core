/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util.viewers;

import java.util.HashMap;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;
import ch.rgw.tools.Tree;

public class BasicTreeContentProvider implements ITreeContentProvider, ICommonViewerContentProvider {
	
	public Object[] getChildren(Object parentElement){
		return ((Tree) parentElement).getChildren().toArray();
	}
	
	public Object getParent(Object element){
		return ((Tree) element).getParent();
	}
	
	public boolean hasChildren(Object element){
		return ((Tree) element).hasChildren();
	}
	
	public void dispose(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public Object[] getElements(Object inputElement){
		Tree root = (Tree) inputElement;
		return root.getChildren().toArray();
	}
	
	public void startListening(){
		// TODO Auto-generated method stub
		
	}
	
	public void stopListening(){
		// TODO Auto-generated method stub
		
	}
	
	public void changed(HashMap<String, String> values){
		// TODO Auto-generated method stub
		
	}
	
	public void reorder(String field){
		// TODO Auto-generated method stub
		
	}
	
	public void selected(){
		// nothing to do
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
}
