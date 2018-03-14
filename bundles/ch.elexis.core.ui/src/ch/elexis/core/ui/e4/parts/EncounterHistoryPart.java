/*******************************************************************************
 * Copyright (c) 2018, MEDEVIT and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    MEDEVIT <office@medevit.at> - initial implementation
 *******************************************************************************/
package ch.elexis.core.ui.e4.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.core.ui.dialogs.KonsFilterDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.HistoryDisplay;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

public class EncounterHistoryPart {
	
	public static final String ID = EncounterHistoryPart.class.getName();
	
	@Inject
	MPart mPart;
	@Inject
	EPartService partService;
	@Inject
	UISynchronize uisync;
	@Inject
	EMenuService menuService;
	
	private HistoryDisplay liste;
	private Action filterAction;
	private KonsFilter filter;
	
	@PostConstruct
	public void postConstruct(Composite parent, EModelService modelService){
		parent.setLayout(new GridLayout());
		
		liste = new HistoryDisplay(parent);
		liste.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		makeActions();
		
		PartMenus partMenus = new PartMenus();
		partMenus.createToolbar(modelService, mPart, GlobalActions.neueKonsAction, filterAction);
	}
	
	@Inject
	private void contextChange(@Optional @Active Patient currentPatient,
		@Optional @Active Fall currentCoverage, @Optional @Active Konsultation currentEncounter){
		if (partService.isPartVisible(mPart)) {
			if (currentCoverage != null) {
				currentPatient = currentCoverage.getPatient();
			}
			if (currentEncounter != null) {
				currentPatient = currentEncounter.getFall().getPatient();
			}
			
			restart(currentPatient);
		}
	}
	
	private void restart(Patient patient){
		if (liste != null) {
			liste.stop();
			// next is null for convenience - refactor load method
			liste.load(patient, null);
			liste.start(filter);
		}
	}
	
	private void makeActions(){
		filterAction = new Action(Messages.KonsListe_FilterListAction, Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
				setToolTipText(Messages.KonsListe_FilterListToolTip);
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					filter = null;
				} else {
					KonsFilterDialog kfd =
						new KonsFilterDialog(ElexisEventDispatcher.getSelectedPatient(), filter);
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
}