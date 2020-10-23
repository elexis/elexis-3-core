/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, D. Lutz, P. Schönbucher and Elexis
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

import static ch.elexis.core.ui.actions.GlobalActions.closeFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.delFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.makeBillAction;
import static ch.elexis.core.ui.actions.GlobalActions.neuerFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.openFallaction;
import static ch.elexis.core.ui.actions.GlobalActions.reopenFallAction;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.ObjectFilterRegistry;
import ch.elexis.core.ui.actions.ObjectFilterRegistry.IObjectFilterProvider;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.provider.FaelleContentProvider;
import ch.elexis.core.ui.views.provider.FaelleLabelProvider;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.ExHandler;

/**
 * Eine alternative, platzsparendere Fälle-View
 */
public class FaelleView extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.schoebufaelle"; //$NON-NLS-1$
	TableViewer tv;
	ViewMenus menus;
	private IAction konsFilterAction;
	private IAction filterClosedAction;
	private final FallKonsFilter filter = new FallKonsFilter();
	
	private IPatient actPatient;
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	
	@Inject
	void activePatient(@Optional IPatient patient){
		Display.getDefault().asyncExec(() -> {
			handleEventPatient(patient);
		});
	}
	
	@Optional
	@Inject
	void lockedPatient(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) IPatient patient){
		handleEventPatient(patient);
	}
	
	@Optional
	@Inject
	void unlockedPatient(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) IPatient patient){
		handleEventPatient(patient);
	}
	
	private void handleEventPatient(IPatient patient){
		if (patient != null && CoreUiUtil.isActiveControl(tv.getControl())) {
			if (actPatient != patient) {
				actPatient = patient;
				tv.refresh(true);
				ICoverage currentFall = ContextServiceHolder.get().getRootContext()
					.getTyped(ICoverage.class).orElse(null);
				if (currentFall != null) {
					tv.setSelection(new StructuredSelection(currentFall));
				}
			}
		}
	}
	
	@Optional
	@Inject
	void compatitbility(
		@UIEventTopic(ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY + "*") Object object){
		if (object instanceof ICoverage
			|| (object instanceof Class && object.equals(ICoverage.class))) {
			// refresh from database if modified by po
			if (actPatient != null) {
				if (object instanceof ICoverage) {
					CoreModelServiceHolder.get().refresh((ICoverage) object, true);
				}
				CoreModelServiceHolder.get().refresh(actPatient, true);
			}
			refreshTableViewer();
		}
	}
	
	@Optional
	@Inject
	void createCoverage(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) ICoverage iCoverage){
		refreshTableViewer();
	}
	
	@Optional
	@Inject
	void updateCoverage(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) ICoverage iCoverage){
		CoreModelServiceHolder.get().refresh(actPatient);
		refreshTableViewer();
	}
	
	@Optional
	@Inject
	void deleteCoverage(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) ICoverage iCoverage){
		refreshTableViewer();
	}
	
	@Optional
	@Inject
	void reloadCoverage(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> iCoverage){
		if (ICoverage.class.equals(iCoverage)) {
			refreshTableViewer();
		}
	}

	@Inject
	void activeCoverage(@Optional ICoverage iCoverage){
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(tv.getControl())) {
				tv.refresh(true);
				ICoverage currentFall = iCoverage;
				if (currentFall != null) {
					tv.setSelection(new StructuredSelection(currentFall));
				}
				if (konsFilterAction.isChecked()) {
					filter.setFall(iCoverage);
				}
			}
		});
		
	}
	
	private void refreshTableViewer(){
		if (CoreUiUtil.isActiveControl(tv.getControl())) {
			tv.refresh(true);
		}
	}
	
	
	public FaelleView(){
		makeActions();
	}
	
	@Override
	public void createPartControl(final Composite parent){
		setPartName(Messages.FaelleView_partName); //$NON-NLS-1$
		parent.setLayout(new GridLayout());
		tv = new TableViewer(parent);
		tv.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.setContentProvider(new FaelleContentProvider());
		tv.setLabelProvider(new FaelleLabelProvider());
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					if (!selection.isEmpty()) {
						ICoverage selectedCoverage = (ICoverage) ((StructuredSelection) selection).getFirstElement();
						ContextServiceHolder.get().setActiveCoverage(selectedCoverage);
					}
				}
			}
		});
		menus = new ViewMenus(getViewSite());
		menus.createToolbar(neuerFallAction, konsFilterAction, filterClosedAction);
		menus.createViewerContextMenu(tv, openFallaction, closeFallAction, null, delFallAction, reopenFallAction,
			makeBillAction);
		tv.setInput(getViewSite());
		tv.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				try {
					FallDetailView pdv =
						(FallDetailView) getSite().getPage().showView(FallDetailView.ID);
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
			}
		});
	//	ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_pat);
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
	//	ElexisEventDispatcher.getInstance().removeListeners(eeli_fall, eeli_pat);
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		tv.getControl().setFocus();
		refresh();
	}
	
	@Override
	public void refresh(){
		handleEventPatient(ContextServiceHolder.get().getActivePatient().orElse(null));
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
				//@TODO nopo for Konsultation
				if (!isChecked()) {
					ObjectFilterRegistry.getInstance().unregisterObjectFilter(Konsultation.class,
						filter);
				} else {
					ObjectFilterRegistry.getInstance().registerObjectFilter(Konsultation.class,
						filter);
					filter.setFall(ContextServiceHolder.get().getRootContext()
						.getTyped(ICoverage.class).orElse(null));
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
						if (element instanceof ICoverage) {
							ICoverage fall = (ICoverage) element;
							return fall.isOpen();
						}
						return false;
					}
				};
			}
			
			@Override
			public void run(){
				if (!isChecked()) {
					tv.removeFilter(closedFilter);
				} else {
					tv.addFilter(closedFilter);
				}
			}
		};
	}
	
	class FallKonsFilter implements IObjectFilterProvider, IFilter {
		
		ICoverage mine;
		boolean bDaempfung;
		
		void setFall(final ICoverage fall){
			mine = fall;
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
				IEncounter.class);
		}
		
		@Override
		public void activate(){
			bDaempfung = true;
			konsFilterAction.setChecked(true);
			bDaempfung = false;
		}
		
		@Override
		public void changed(){
			// don't mind
		}
		
		@Override
		public void deactivate(){
			bDaempfung = true;
			konsFilterAction.setChecked(false);
			bDaempfung = false;
		}
		
		@Override
		public IFilter getFilter(){
			return this;
		}
		
		@Override
		public String getId(){
			return "ch.elexis.FallFilter"; //$NON-NLS-1$
		}
		
		@Override
		public boolean select(final Object toTest){
			if (mine == null) {
				return true;
			}
			if (toTest instanceof IEncounter) {
				IEncounter k = (IEncounter) toTest;
				if (k.getCoverage().equals(mine)) {
					return true;
				}
			}
			return false;
		}
		
	}
}
