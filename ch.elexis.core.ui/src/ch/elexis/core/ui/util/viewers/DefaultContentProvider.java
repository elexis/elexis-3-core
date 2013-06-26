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
import java.util.List;

import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.ui.util.Messages;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

/**
 * Default-Implementation des ContentProviders. Zeigt eine Liste der in der Datenbank vorhandenen
 * Elemente einer bestimmten Klasse an.
 * 
 * @author Gerry
 * 
 */
public class DefaultContentProvider implements ICommonViewerContentProvider {
	protected Class source;
	protected CommonViewer cv;
	protected String[] order = null;
	protected boolean reverse = false;
	
	public DefaultContentProvider(CommonViewer c, Class clazz){
		source = clazz;
		cv = c;
		
	}
	
	public DefaultContentProvider(CommonViewer c, Class clazz, String[] o, boolean rev){
		source = clazz;
		cv = c;
		order = o;
		reverse = rev;
	}
	
	public void startListening(){
		ViewerConfigurer.ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			cfp.addChangeListener(this);
		}
	}
	
	public void stopListening(){
		ViewerConfigurer.ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			cfp.removeChangeListener(this);
		}
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Object[] getElements(Object inputElement){
		Query qbe = new Query(source);
		if (order != null) {
			qbe.orderBy(reverse, order);
		}
		ViewerConfigurer.ControlFieldProvider cfp = cv.getConfigurer().getControlFieldProvider();
		if (cfp != null) {
			cfp.setQuery(qbe);
		}
		List<PersistentObject> list = qbe.execute();
		if (list == null) {
			return new String[] {
				Messages.getString("NoDataAvailable")}; //$NON-NLS-1$
		} else {
			return list.toArray();
		}
	}
	
	public void dispose(){
		stopListening();
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		
	}
	
	public void changed(HashMap<String, String> values){
		cv.notify(CommonViewer.Message.update);
		if (cv.getConfigurer().getControlFieldProvider().isEmpty()) {
			cv.notify(CommonViewer.Message.empty);
		} else {
			cv.notify(CommonViewer.Message.notempty);
		}
		
	}
	
	public void reorder(String field){
		cv.notify(CommonViewer.Message.update);
		
	}
	
	public void selected(){
		// nothing to do
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
}