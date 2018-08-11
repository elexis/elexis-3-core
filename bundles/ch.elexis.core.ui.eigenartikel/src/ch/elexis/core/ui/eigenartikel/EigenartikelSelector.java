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

import java.util.List;

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

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.eigenartikel.EigenartikelUtil;
import ch.elexis.core.eigenartikel.acl.ACLContributor;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.ITypedArticle;
import ch.elexis.core.model.eigenartikel.Constants;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.actions.ToggleVerrechenbarFavoriteAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.services.ContextServiceHolder;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.DoubleClickListener;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.DefaultButtonProvider;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.PersistentObject;

public class EigenartikelSelector extends CodeSelectorFactory {

	private CommonViewer commonViewer;
	
	private ToggleVerrechenbarFavoriteAction tvfa = new ToggleVerrechenbarFavoriteAction();
	private ISelectionChangedListener selChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event){
			TreeViewer tv = (TreeViewer) event.getSource();
			StructuredSelection ss = (StructuredSelection) tv.getSelection();
			tvfa.updateSelection(ss.isEmpty() ? null : ss.getFirstElement());
			
			if (!ss.isEmpty()) {
				ITypedArticle ea = (ITypedArticle) ss.getFirstElement();
				ContextServiceHolder.get().getRootContext()
					.setNamed("ch.elexis.core.ui.eigenartikel.selection", ea);
			} else {
				ContextServiceHolder.get().getRootContext()
					.setNamed("ch.elexis.core.ui.eigenartikel.selection", null);
			}
		}
	};
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer commonViewer){
		this.commonViewer = commonViewer;
		
		MenuManager menu = new MenuManager();
		menu.add(tvfa);
		menu.add(rearrangePackagesAction);
		
		commonViewer.setNamedSelection("ch.elexis.core.ui.eigenartikel.selection");
		commonViewer.setContextMenu(menu);
		commonViewer.setSelectionChangedListener(selChangeListener);
		
		EigenartikelTreeContentProvider eal = new EigenartikelTreeContentProvider(commonViewer);
		
		ShowEigenartikelProductsAction seaoa = new ShowEigenartikelProductsAction(eal, this);
		rearrangePackagesAction.setEnabled(CoreHub.userCfg.get(ShowEigenartikelProductsAction.FILTER_CFG, false));
		
		FieldDescriptor<?>[] lbName = new FieldDescriptor<?>[] {
			new FieldDescriptor<ITypedArticle>(EigenartikelTreeContentProvider.FILTER_KEY)
		};
		
		SelectorPanelProvider slp = new SelectorPanelProvider(lbName, true);
		slp.addActions(seaoa);
		
		DefaultButtonProvider dbp = new ViewerConfigurer.DefaultButtonProvider();
		SimpleWidgetProvider swp =
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null);
		
		EigenartikelTreeLabelProvider alp = new EigenartikelTreeLabelProvider();
		
		return new ViewerConfigurer(eal, alp, slp, dbp, swp);
	}
	
	@Override
	public Class<?> getElementClass(){
		return ITypedArticle.class;
	}
	
	@Override
	public String getCodeSystemName(){
		return Constants.TYPE_NAME;
	}
	
	@Override
	protected DoubleClickListener getDoubleClickListener(){
		return new DoubleClickListener() {
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				ICodeSelectorTarget target =
					CodeSelectorHandler.getInstance().getCodeSelectorTarget();
				if (target != null) {
					if (obj instanceof ITypedArticle) {
						ITypedArticle article = (ITypedArticle) obj;
						// translate to first package if product selected
						if (article.isProduct()) {
							@SuppressWarnings("unchecked")
							List<ITypedArticle> packages =
								(List<ITypedArticle>) (List<?>) article.getPackages();
							if (!packages.isEmpty()) {
								article = packages.get(0);
							}
						}
						target.codeSelected(article);
					}
				}
			}
		};
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
				
				commonViewer.getViewerWidget().addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
					new Transfer[] {
					TextTransfer.getInstance()
				}, new ViewerDropAdapter(commonViewer.getViewerWidget()) {
					
					@Override
					public void dragEnter(final DropTargetEvent event){
						event.detail = DND.DROP_COPY;
					}
					
					@Override
					public void drop(final DropTargetEvent event){
						ITypedArticle target = (ITypedArticle) determineTarget(event);
						String drp = (String) event.data;
						String[] dl = drp.split(","); //$NON-NLS-1$
						for (String obj : dl) {
							PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
							if (dropped instanceof ITypedArticle) {
								ITypedArticle ea = (ITypedArticle) dropped;
								if (ea.isProduct()) {
									continue;
								}
								LockResponse lr = CoreHub.getLocalLockService().acquireLock(target);
								if (lr.isOk()) {
									EigenartikelUtil
										.copyProductAttributesToArticleSetAsChild(target,
										ea);
									CoreHub.getLocalLockService().releaseLock(target);
									ContextServiceHolder.get().postEvent(
										ElexisEventTopics.EVENT_RELOAD, ITypedArticle.class);
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
						ITypedArticle ea = (ITypedArticle) getSelectedObject();
						ITypedArticle eaTarget = (ITypedArticle) target;
						return (eaTarget != null && eaTarget.isProduct() && ea != null
							&& !ea.isProduct());
					}
				});
				initialized = true;
				setEnabled(!initialized);
			}
		};

	public void allowArticleRearrangement(boolean checked){
		rearrangePackagesAction.setEnabled(checked);
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
}
