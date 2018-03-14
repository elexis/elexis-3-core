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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.ObjectFilterRegistry;
import ch.elexis.core.ui.actions.ObjectFilterRegistry.IObjectFilterProvider;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.FallComparator;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.provider.FaelleLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

public class CoverageSelectorPart {
	
	public static final String ID = CoverageSelectorPart.class.getName();
	
	@Inject
	MPart mPart;
	@Inject
	EPartService partService;
	@Inject
	UISynchronize uisync;
	@Inject
	EMenuService menuService;
	
	private TableViewer tableViewer;
	private IAction konsFilterAction;
	private IAction filterClosedAction;
	private final FallKonsFilter filter = new FallKonsFilter();
	
	public CoverageSelectorPart(){
		makeActions();
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, EModelService modelService){
		parent.setLayout(new GridLayout());
		
		tableViewer = new TableViewer(parent);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new FaelleLabelProvider());
		tableViewer
			.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				MPart findPart = partService.findPart(FallDetailView.ID);
				partService.activate(findPart);
			}
		});
		
		PartMenus partMenus = new PartMenus();
		partMenus.createViewerContextMenu(menuService, ID, table, GlobalActions.delFallAction,
			GlobalActions.openFallaction, GlobalActions.reopenFallAction,
			GlobalActions.makeBillAction);
		partMenus.createToolbar(modelService, mPart, GlobalActions.neuerFallAction,
			konsFilterAction, filterClosedAction);
	}
	
	@Inject
	private void context(@Optional @Active Patient currentPatient,
		@Optional @Active Fall currentCoverage){
		if (partService.isPartVisible(mPart)) {
			List<Fall> cases = new ArrayList<Fall>();
			if (currentPatient != null) {
				Fall[] casesA = currentPatient.getFaelle();
				Arrays.sort(casesA, new FallComparator());
				cases.addAll(Arrays.asList(casesA));
			}
			
			if (currentCoverage != null && konsFilterAction.isChecked()) {
				filter.setFall(currentCoverage);
			}
			
			uisync.asyncExec(() -> {
				if (!tableViewer.getTable().isDisposed()) {
					tableViewer.setInput(cases);
					if (currentCoverage != null) {
						StructuredSelection ss = (StructuredSelection) tableViewer.getSelection();
						if (!ss.isEmpty() && !ss.getFirstElement().equals(currentCoverage)) {
							tableViewer.setSelection(new StructuredSelection(currentCoverage));
						}
					}
				}
			});
		}
	}
	
	private void makeActions(){
		konsFilterAction = new Action(Messages.FaelleView_FilterConsultations, //$NON-NLS-1$
			Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.FaelleView_ShowOnlyConsOfThisCase); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					ObjectFilterRegistry.getInstance().unregisterObjectFilter(Konsultation.class,
						filter);
				} else {
					ObjectFilterRegistry.getInstance().registerObjectFilter(Konsultation.class,
						filter);
					filter.setFall((Fall) ElexisEventDispatcher.getSelected(Fall.class));
				}
			}
			
		};
		filterClosedAction = new Action("", Action.AS_CHECK_BOX) {
			private ViewerFilter closedFilter;
			{
				setToolTipText(Messages.FaelleView_ShowOnlyOpenCase); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_DOCUMENT_WRITE.getImageDescriptor());
				closedFilter = new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element){
						if (element instanceof Fall) {
							Fall fall = (Fall) element;
							return fall.isOpen();
						}
						return false;
					}
				};
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					tableViewer.removeFilter(closedFilter);
				} else {
					tableViewer.addFilter(closedFilter);
				}
			}
		};
	}
	
	class FallKonsFilter implements IObjectFilterProvider, IFilter {
		
		Fall mine;
		boolean bDaempfung;
		
		void setFall(final Fall fall){
			mine = fall;
			ElexisEventDispatcher.reload(Konsultation.class);
		}
		
		public void activate(){
			bDaempfung = true;
			konsFilterAction.setChecked(true);
			bDaempfung = false;
		}
		
		public void changed(){
			// don't mind
		}
		
		public void deactivate(){
			bDaempfung = true;
			konsFilterAction.setChecked(false);
			bDaempfung = false;
		}
		
		public IFilter getFilter(){
			return this;
		}
		
		public String getId(){
			return "ch.elexis.FallFilter"; //$NON-NLS-1$
		}
		
		public boolean select(final Object toTest){
			if (mine == null) {
				return true;
			}
			if (toTest instanceof Konsultation) {
				Konsultation k = (Konsultation) toTest;
				if (k.getFall().equals(mine)) {
					return true;
				}
			}
			return false;
		}
		
	}
	
}