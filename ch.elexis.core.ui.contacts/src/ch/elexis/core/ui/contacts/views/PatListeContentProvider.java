/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.contacts.views;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ICommonViewerContentProvider;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class PatListeContentProvider implements ICommonViewerContentProvider, ILazyContentProvider {
	CommonViewer viewer;
	Query<Patient> qbe;
	Object[] pats;
	boolean bValid = false;
	boolean bUpdating = false;
	String[] orderLabels;
	String[] orderFields;
	String firstOrder;
	PatListFilterBox pfilter;
	ViewPart site;
	
	public PatListeContentProvider(CommonViewer cv, String[] fieldsToOrder, ViewPart s){
		viewer = cv;
		site = s;
		orderLabels = new String[fieldsToOrder.length];
		orderFields = new String[fieldsToOrder.length];
		for (int i = 0; i < fieldsToOrder.length; i++) {
			String[] def = fieldsToOrder[i].split(Query.EQUALS);
			orderFields[i] = def[0];
			orderLabels[i] = def.length > 1 ? def[1] : def[0];
		}
		firstOrder = orderFields[0];
	}
	
	public void startListening(){
		viewer.getConfigurer().getControlFieldProvider().addChangeListener(this);
		qbe = new Query<Patient>(Patient.class);
	}
	
	public void stopListening(){
		if (viewer != null) {
			viewer.getConfigurer().getControlFieldProvider().removeChangeListener(this);
		}
	}
	
	public void setFilter(PatListFilterBox f){
		qbe.addPostQueryFilter(f);
		pfilter = f;
		bValid = false;
	}
	
	public void removeFilter(PatListFilterBox f){
		qbe.removePostQueryFilter(f);
		pfilter = null;
		bValid = false;
	}
	
	public Object[] getElements(Object inputElement){
		if (bValid || bUpdating) {
			return pats;
		}
		if (pfilter != null) {
			pats = new String[] {
				Messages.PatListeContentProvider_LoadingData
			};
			((TableViewer) viewer.getViewerWidget()).setItemCount(1);
		}
		
		// viewer.getViewerWidget().refresh(true);
		if (!CoreHub.acl.request(AccessControlDefaults.PATIENT_DISPLAY)) {
			return new Object[0];
		}
		
		Job job = new Job(Messages.PatListeContentProvider_LoadingPatients) { //$NON-NLS-1$
			
				@Override
				protected IStatus run(IProgressMonitor monitor){
					monitor.beginTask(Messages.PatListeContentProvider_LoadPatients,
						IProgressMonitor.UNKNOWN); //$NON-NLS-1$
					
					qbe.clear();
					if (pfilter != null) {
						if (pfilter.aboutToStart() == false) {
							return Status.CANCEL_STATUS;
						}
					}
					viewer.getConfigurer().getControlFieldProvider().setQuery(qbe);
					String[] actualOrder;
					int idx = StringTool.getIndex(orderFields, firstOrder);
					if ((idx == -1) || (idx == 0)) {
						actualOrder = orderFields;
					} else {
						actualOrder = new String[orderFields.length];
						int n = 0;
						int begin = idx;
						do {
							actualOrder[n++] = orderFields[idx++];
							if (idx >= orderFields.length) {
								idx = 0;
							}
						} while (idx != begin);
					}
					qbe.orderBy(false, actualOrder);
					List<Patient> lPats = qbe.execute();
					if (lPats == null) {
						pats = new Patient[0];
					} else {
						pats = lPats.toArray(new Patient[0]);
					}
					UiDesk.getDisplay().syncExec(new Runnable() {
						
						public void run(){
							((TableViewer) viewer.getViewerWidget()).setItemCount(pats.length);
							bValid = true;
							if (pfilter != null) {
								pfilter.finished();
							}
							viewer.getViewerWidget().refresh();
							bUpdating = false;
							
						}
						
					});
					monitor.done();
					return Status.OK_STATUS;
				}
				
			};
		job.setPriority(Job.SHORT);
		job.setUser(false);
		// job.setSystem(true);
		bUpdating = true;
		IWorkbenchSiteProgressService siteService =
			(IWorkbenchSiteProgressService) site.getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		siteService.schedule(job, 0 /* now */, true /*
														 * use the half-busy cursor in the part
														 */);
		
		job.setProperty(IProgressConstants.ICON_PROPERTY, Images.IMG_AUSRUFEZ_ROT.getImage());
		
		// job.schedule();
		return pats;
	}
	
	public void dispose(){
		stopListening();
	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	
	public void changed(HashMap<String, String> vals){
		bValid = false;
		getElements(viewer);
		if (viewer.getConfigurer().getControlFieldProvider().isEmpty()) {
			viewer.notify(CommonViewer.Message.empty);
		} else {
			viewer.notify(CommonViewer.Message.notempty);
		}
		// viewer.notify(CommonViewer.Message.update);
	}
	
	public void reorder(String field){
		int idx = StringTool.getIndex(orderLabels, field);
		if (idx > -1) {
			firstOrder = orderFields[idx];
			changed(null);
		}
		
	}
	
	public void selected(){
		// TODO Auto-generated method stub
		
	}
	
	public void updateElement(int index){
		if (!bValid) {
			getElements(viewer);
		}
		if (pats.length > index) {
			((TableViewer) viewer.getViewerWidget()).replace(pats[index], index);
		} else {
			((TableViewer) viewer.getViewerWidget()).replace("-", index); //$NON-NLS-1$
		}
	}
	
	public void invalidate(){
		bValid = false;
	}
	
	@Override
	public void init(){
		// TODO Auto-generated method stub
		
	}
	
}
