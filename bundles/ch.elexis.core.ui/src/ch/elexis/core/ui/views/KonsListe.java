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

package ch.elexis.core.ui.views;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.core.ui.dialogs.KonsFilterDialog;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Konsultation;


public class KonsListe extends ViewPart implements IRefreshable, ISaveablePart2 {
	public static final String ID = "ch.elexis.HistoryView"; //$NON-NLS-1$
	HistoryDisplay liste;
	IPatient actPatient;
	ViewMenus menus;
	private Action filterAction;
	private KonsFilter filter;
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	
	@Optional
	@Inject
	void compatitbility(
		@UIEventTopic(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY + "*") Object object){
		if (object instanceof IEncounter
			|| (object instanceof Class && object.equals(IEncounter.class))) {
			// refresh from database if modified by po
			if (object instanceof IEncounter) {
				IEncounter encounter = (IEncounter) object;
				CoreModelServiceHolder.get().refresh(encounter, true);
				CoreModelServiceHolder.get().refresh(encounter.getCoverage(), true);
			}
			restart(true);
		}
	}
	
	@Inject
	void activePatient(@Optional IPatient patient){
		if ((actPatient == null)
			|| (patient != null && !actPatient.getId().equals(patient.getId()))) {
			actPatient = patient;
			restart(true);
		}
	}
	
	@Inject
	void activeCoverage(@Optional ICoverage iCoverage){
		if (iCoverage != null) {
			actPatient = iCoverage.getPatient();
		}
		restart(false);
	}
	
	@Optional
	@Inject
	void changedCoverage(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") ICoverage iCoverage){
		if (iCoverage != null) {
			actPatient = iCoverage.getPatient();
			restart(false);
		}
	}
	
	@Optional
	@Inject
	void changedEncounter(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") IEncounter iEncounter){
		restart(false);
	}
	
	@Override
	public void createPartControl(final Composite parent){
		parent.setLayout(new GridLayout());
		liste = new HistoryDisplay(parent, getViewSite());
		liste.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		menus = new ViewMenus(getViewSite());
		menus.createToolbar(filterAction);
	//	ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_pat, eeli_kons);
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
	//	ElexisEventDispatcher.getInstance().removeListeners(eeli_fall, eeli_kons, eeli_pat);
		liste.stop();
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		liste.setFocus();
		refresh();
	}
	
	@Override
	public void refresh(){
		//eeli_pat.catchElexisEvent(ElexisEvent.createPatientEvent());		
	}
	
	private void restart(Boolean isPatientEvent){
		if (liste != null) {
			liste.stop();
			liste.load(actPatient, isPatientEvent == null || isPatientEvent.booleanValue());
			liste.start(filter);
		}
	}
	
	private void makeActions(){
		filterAction = new Action(Messages.KonsListe_FilterListAction, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
					setToolTipText(Messages.KonsListe_FilterListToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					if (!isChecked()) {
						filter = null;
					} else {
						KonsFilterDialog kfd = new KonsFilterDialog(actPatient, filter);
						if (kfd.open() == Dialog.OK) {
							filter = kfd.getResult();
						} else {
							kfd = null;
							setChecked(false);
						}
					}
				restart(null);
				}
			};
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	/**
	 * Not used
	 * 
	 * @deprecated @since 3.2
	 * @param clazz
	 */
	@Deprecated
	public void reloadContents(final Class clazz){
		if (clazz.equals(Konsultation.class)) {
			restart(null);
		}
		
	}
	
}
