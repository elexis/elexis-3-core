/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import ch.elexis.core.data.Query;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.ACE;
import ch.elexis.core.ui.actions.AbstractDataLoaderJob;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.JobPool;
import ch.elexis.core.ui.util.Messages;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;

/**
 * Ein Content-Provider, der benötigte Daten aus einem BackgroundJob bezieht und einem TableViewer
 * nur gerade die jeweils benötigten Datne liefern kann. Registriert sich beim Dataloader selbst als
 * listener und startet diesen auch, wenn Daten benötigt werden.
 * 
 * @author Gerry
 */
public class LazyContentProvider implements ICommonViewerContentProvider, ILazyContentProvider,
		BackgroundJobListener, AbstractDataLoaderJob.FilterProvider {
	AbstractDataLoaderJob dataloader;
	CommonViewer tableviewer;
	ACE required;
	
	public LazyContentProvider(CommonViewer viewer, AbstractDataLoaderJob job, ACE rights){
		dataloader = job;
		job.addListener(this);
		job.addFilterProvider(this);
		tableviewer = viewer;
		required = rights;
	}
	
	public void dispose(){
		dataloader.removeListener(this);
		dataloader.removeFilterProvider(this);
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	
	public void updateElement(int index){
		if (CoreHub.acl.request(required) == false) {
			((TableViewer) tableviewer.getViewerWidget()).replace(" --- ", index); //$NON-NLS-1$
			return;
		}
		if (dataloader.isValid()) {
			Object[] res = (Object[]) dataloader.getData();
			Object nval = Messages.getString("LazyContentProvider.noData"); //$NON-NLS-1$
			if (index < res.length) {
				nval = res[index];
			}
			((TableViewer) tableviewer.getViewerWidget()).replace(nval, index);
		} else {
			JobPool pool = JobPool.getJobPool();
			if (pool.getJob(dataloader.getJobname()) == null) {
				pool.addJob(dataloader);
				
			}
			pool.activate(dataloader.getJobname(), Job.SHORT);
		}
		
	}
	
	public void jobFinished(BackgroundJob j){
		int size = 0;
		if ((j != null) && (j.getData() != null)) {
			size = ((Object[]) j.getData()).length;
		}
		((TableViewer) tableviewer.getViewerWidget()).getTable().setItemCount(size == 0 ? 1 : size);
		tableviewer.notify(CommonViewer.Message.update);
		
	}
	
	public void startListening(){
		tableviewer.getConfigurer().controlFieldProvider.addChangeListener(this);
	}
	
	public void stopListening(){
		tableviewer.getConfigurer().controlFieldProvider.removeChangeListener(this);
	}
	
	public void applyFilter(){
		Query qbe = dataloader.getQuery();
		if (qbe != null) {
			ViewerConfigurer vc = tableviewer.getConfigurer();
			if (vc != null) {
				ControlFieldProvider cfp = vc.getControlFieldProvider();
				cfp.setQuery(qbe);
			}
		}
	}
	
	public void changed(HashMap<String, String> vals){
		dataloader.invalidate();
		if (tableviewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			tableviewer.notify(CommonViewer.Message.empty);
		} else {
			tableviewer.notify(CommonViewer.Message.notempty);
		}
		JobPool.getJobPool().activate(dataloader.getJobname(), Job.SHORT);
	}
	
	public void reorder(String field){
		dataloader.setOrder(field);
		changed(null);
		
	}
	
	public void selected(){
		// nothing to do
	}
	
	public Object[] getElements(Object inputElement){
		return (Object[]) dataloader.getData();
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
	
}