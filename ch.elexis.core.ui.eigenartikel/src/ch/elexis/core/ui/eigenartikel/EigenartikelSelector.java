/*******************************************************************************
 * Copyright (c) 2006-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted from elexis main and adapted for usage
 *******************************************************************************/
package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.eigenartikel.acl.ACLContributor;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.actions.ToggleVerrechenbarFavoriteAction;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.DefaultButtonProvider;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.PersistentObject;

public class EigenartikelSelector extends CodeSelectorFactory {

	private CommonViewer cv;
	
	private UpdateEventListener updateEventListener;
	
	private ToggleVerrechenbarFavoriteAction tvfa = new ToggleVerrechenbarFavoriteAction();
	private ISelectionChangedListener selChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			TreeViewer tv = (TreeViewer) event.getSource();
			StructuredSelection ss = (StructuredSelection) tv.getSelection();
			tvfa.updateSelection(ss.isEmpty() ? null : ss.getFirstElement());
			
			if (!ss.isEmpty()) {
				Eigenartikel ea = (Eigenartikel) ss.getFirstElement();
				ElexisEventDispatcher.fireSelectionEvent(ea);
			} else {
				ElexisEventDispatcher.clearSelection(Eigenartikel.class);
			}
		}
	};
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		this.cv = cv;
		
		MenuManager menu = new MenuManager();
		menu.add(tvfa);
		menu.add(rearrangePackagesAction);
		
		cv.setContextMenu(menu);
		cv.setSelectionChangedListener(selChangeListener);
		
		EigenartikelTreeContentProvider eal = new EigenartikelTreeContentProvider(cv);
		
		DefaultControlFieldProvider dcfp = new DefaultControlFieldProvider(cv, new String[] {
			EigenartikelTreeContentProvider.FILTER_KEY
		});
		
		DefaultButtonProvider dbp = new ViewerConfigurer.DefaultButtonProvider();
		SimpleWidgetProvider swp =
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null);
		
		EigenartikelTreeLabelProvider alp = new EigenartikelTreeLabelProvider();
		
		updateEventListener = new UpdateEventListener(cv, getElementClass(),
			ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_UPDATE | ElexisEvent.EVENT_SELECTED);
		ElexisEventDispatcher.getInstance().addListeners(updateEventListener);
		
		return new ViewerConfigurer(eal, alp, dcfp, dbp, swp);
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(updateEventListener);
	}
	
	@Override
	public String getCodeSystemName(){
		return Eigenartikel.TYPNAME;
	}
	
	private class UpdateEventListener extends ElexisUiEventListenerImpl {
		
		CommonViewer viewer;
		
		UpdateEventListener(CommonViewer viewer, final Class<?> clazz, int mode){
			super(clazz, mode);
			this.viewer = viewer;
		}
		
		@Override
		public void runInUi(ElexisEvent ev){
			if (!viewer.getViewerWidget().getControl().isDisposed()) {
				if (ElexisEvent.EVENT_RELOAD == ev.getType()) {
					viewer.getViewerWidget().refresh(true);
				} else if (ElexisEvent.EVENT_UPDATE == ev.getType() && ev.getObject() != null) {
					viewer.getViewerWidget().refresh(ev.getObject(), true);
				}
			}
			if (ElexisEvent.EVENT_SELECTED == ev.getType()) {
				viewer.setSelection(ev.getObject(), false);
			}
		}
	}
	
	private RestrictedAction rearrangePackagesAction =
		new RestrictedAction(ACLContributor.EIGENARTIKEL_MODIFY) {
			
			boolean initialized = false;
			
			{
				setText("Umgruppierung aktivieren");
			}
			
			@Override
			public void doRun(){
				if (initialized) {
					return;
				}
				
				cv.getViewerWidget().addDropSupport(DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] {
					TextTransfer.getInstance()
				}, new ViewerDropAdapter(cv.getViewerWidget()) {
					
					@Override
					public void dragEnter(final DropTargetEvent event){
						event.detail = DND.DROP_COPY;
					}
					
					@Override
					public void drop(final DropTargetEvent event){
						Eigenartikel target = (Eigenartikel) determineTarget(event);
						String drp = (String) event.data;
						String[] dl = drp.split(","); //$NON-NLS-1$
						for (String obj : dl) {
							PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
							if (dropped instanceof Eigenartikel) {
								Eigenartikel ea = (Eigenartikel) dropped;
								if (ea.isProduct()) {
									continue;
								}
								LockResponse lr = CoreHub.getLocalLockService().acquireLock(target);
								if (lr.isOk()) {
									Eigenartikel.copyProductAttributesToArticleSetAsChild(target,
										ea);
									CoreHub.getLocalLockService().releaseLock(target);
									ElexisEventDispatcher.reload(Eigenartikel.class);
								} else {
									LockResponseHelper.showInfo(lr, target, log);
								}
							}
						}
					}
					
					@Override
					public boolean performDrop(Object data){
						return true;
					}
					
					@Override
					public boolean validateDrop(Object target, int operation,
						TransferData transferType){
						Eigenartikel ea = (Eigenartikel) getSelectedObject();
						Eigenartikel eaTarget = (Eigenartikel) target;
						return (eaTarget != null && eaTarget.isProduct() && ea != null
							&& !ea.isProduct());
					}
				});
				initialized = true;
				setEnabled(!initialized);
			}
		};
}
