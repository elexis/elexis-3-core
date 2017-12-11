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
package ch.elexis.core.ui.util.viewers;

import java.util.HashMap;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

public class CommonContentProviderAdapter implements ICommonViewerContentProvider {
	
	public Object[] getElements(Object inputElement){
		return null;
	}
	
	public void dispose(){
		
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		
	}
	
	public void startListening(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void stopListening(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void changed(HashMap<String, String> values){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void reorder(String field){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void selected(){
		// TODO Automatisch erstellter Methoden-Stub
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
}
