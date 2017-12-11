/*******************************************************************************
 * Copyright (c) 2006, G. Weirich and Elexis
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

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.JobPool;
import ch.elexis.core.ui.util.Messages;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

/**
 * ContentProvider, der einen Tree füllen kann. Datenquelle muss ein TreeLoader sein.
 * 
 * @author Gerry
 * 
 */
public class TreeContentProvider implements ITreeContentProvider, BackgroundJobListener,
		ICommonViewerContentProvider {
	BackgroundJob job;
	CommonViewer viewer;
	
	public TreeContentProvider(CommonViewer v, BackgroundJob loader){
		job = loader;
		viewer = v;
		if (JobPool.getJobPool().getJob(job.getJobname()) == null) {
			JobPool.getJobPool().addJob(job);
		}
		job.addListener(this);
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Object[] getChildren(Object element){
		if (element instanceof ch.rgw.tools.Tree) {
			ch.rgw.tools.Tree tr = (ch.rgw.tools.Tree) element;
			return tr.getChildren().toArray();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Object getParent(Object element){
		if (element instanceof ch.rgw.tools.Tree) {
			ch.rgw.tools.Tree tr = (ch.rgw.tools.Tree) element;
			return tr.getParent();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public boolean hasChildren(Object element){
		if (element instanceof ch.rgw.tools.Tree) {
			ch.rgw.tools.Tree tr = (ch.rgw.tools.Tree) element;
			return tr.hasChildren();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")//$NON-NLS-1$
	public Object[] getElements(Object inputElement){
		ch.rgw.tools.Tree result = (ch.rgw.tools.Tree) job.getData();
		if (result == null) {
			JobPool.getJobPool().activate(job.getJobname(), Job.SHORT);
			return new String[] {
				Messages.TreeContentProvider_loadData
			}; //$NON-NLS-1$
		} else {
			if (viewer.getConfigurer().getControlFieldProvider().isEmpty()) {
				result.setFilter(null);
			} else {
				result.setFilter(((DefaultControlFieldProvider) viewer.getConfigurer()
					.getControlFieldProvider()).createFilter());
			}
			Collection c = result.getChildren();
			return c.toArray();
		}
		
	}
	
	public void startListening(){
		viewer.getConfigurer().controlFieldProvider.addChangeListener(this);
	}
	
	public void stopListening(){
		viewer.getConfigurer().controlFieldProvider.removeChangeListener(this);
	}
	
	public void dispose(){
		job.removeListener(this);
	}
	
	public void inputChanged(Viewer pViewer, Object oldInput, Object newInput){
		// TODO Auto-generated method stub
		
	}
	
	public void jobFinished(BackgroundJob j){
		// int size=((Object[])j.getData()).length;
		viewer.notify(CommonViewer.Message.update);
		
	}
	
	public void changed(HashMap<String, String> vals){
		if (viewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			viewer.notify(CommonViewer.Message.empty);
		} else {
			viewer.notify(CommonViewer.Message.notempty);
			// viewer.getViewerWidget().addFilter(viewer.getConfigurer().getControlFieldProvider().createFilter());
		}
		job.invalidate();
		viewer.notify(CommonViewer.Message.update);
	}
	
	public void reorder(String field){
		job.invalidate();
		
	}
	
	public void selected(){
		// nothing to do
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
}