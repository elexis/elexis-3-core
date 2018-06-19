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

import static ch.elexis.core.ui.actions.GlobalActions.delFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.makeBillAction;
import static ch.elexis.core.ui.actions.GlobalActions.neuerFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.openFallaction;
import static ch.elexis.core.ui.actions.GlobalActions.reopenFallAction;
import static ch.elexis.core.ui.actions.GlobalActions.closeFallAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.ObjectFilterRegistry;
import ch.elexis.core.ui.actions.ObjectFilterRegistry.IObjectFilterProvider;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.provider.FaelleContentProvider;
import ch.elexis.core.ui.views.provider.FaelleLabelProvider;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
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
	
	private Patient actPatient;
	
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	
	private final ElexisUiEventListenerImpl eeli_pat =
		new ElexisUiEventListenerImpl(Patient.class) {
			public void runInUi(ElexisEvent ev){
				if(isActiveControl(tv.getControl())) {
					if(actPatient != ev.getObject()) {
						actPatient = (Patient) ev.getObject();
						tv.refresh();
						Fall currentFall = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
						if (currentFall != null) {
							tv.setSelection(new StructuredSelection(currentFall));
						}
					}
				}
			}
		};
	
	private final ElexisUiEventListenerImpl eeli_fall = new ElexisUiEventListenerImpl(Fall.class,
		ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_DELETE | ElexisEvent.EVENT_RELOAD
			| ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_UPDATE) {
		
		public void runInUi(final ElexisEvent ev){
			if (isActiveControl(tv.getControl())) {
				if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
					tv.refresh(true);
					Fall currentFall = (Fall) ev.getObject();
					if (currentFall != null) {
						tv.setSelection(new StructuredSelection(currentFall));
					}
					if (konsFilterAction.isChecked()) {
						filter.setFall((Fall) ev.getObject());
					}
				} else {
					tv.refresh(true);
				}
			}
		}
	};
	
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
		tv.addSelectionChangedListener(GlobalEventDispatcher.getInstance().getDefaultListener());
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
		ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_pat);
		getSite().getPage().addPartListener(udpateOnVisible);
	}
	
	@Override
	public void dispose(){
		getSite().getPage().removePartListener(udpateOnVisible);
		ElexisEventDispatcher.getInstance().removeListeners(eeli_fall, eeli_pat);
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		tv.getControl().setFocus();
		refresh();
	}
	
	@Override
	public void refresh(){
		eeli_pat.catchElexisEvent(ElexisEvent.createPatientEvent());
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
					tv.removeFilter(closedFilter);
				} else {
					tv.addFilter(closedFilter);
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
