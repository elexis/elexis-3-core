/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.lock.ILocalLockService;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Patient;

public class PatientDetailView2 extends ViewPart implements ISaveablePart2, IActivationListener {
	public static final String ID = "ch.elexis.PatDetail_v2"; //$NON-NLS-1$
	Patientenblatt2 pb;
	
	@Override
	public void createPartControl(Composite parent){
		setTitleImage(Images.IMG_VIEW_PATIENT_DETAIL.getImage());
		
		setPartName(Messages.PatientDetailView2_patientDetailViewName); //$NON-NLS-1$
		parent.setLayout(new FillLayout());
		pb = new Patientenblatt2(parent, getViewSite());
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	public void refresh(){
		pb.setPatient((Patient) ElexisEventDispatcher.getSelected(Patient.class));
		pb.refresh();
	}
	
	@Override
	public void setFocus(){
		if (pb != null && !pb.isDisposed()) {
			Patient selectedPatient = ElexisEventDispatcher.getSelectedPatient();
			if (selectedPatient != null && !selectedPatient.equals(pb.actPatient)) {
				pb.setPatient((Patient) ElexisEventDispatcher.getSelected(Patient.class));
				pb.refresh();
			}
		}
	}
	
	@Override
	public void dispose(){
		if (pb != null) {
			pb.dispose();
		}
		super.dispose();
	}
	
	/*
	 * ****** Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	@Override
	public void activation(boolean mode){
		if (!mode) {
			// save does not happen via locking in standalone mode
			if (CoreHub.getLocalLockService().getStatus() == ILocalLockService.Status.STANDALONE) {
				pb.save();
			}
		} else {
			pb.refreshUi();
		}
	}
	
	@Override
	public void visible(boolean mode){
	}
}
