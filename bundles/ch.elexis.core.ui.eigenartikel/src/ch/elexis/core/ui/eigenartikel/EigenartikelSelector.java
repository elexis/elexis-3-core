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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
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

import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.eigenartikel.EigenartikelUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.localarticle.Constants;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.actions.ToggleVerrechenbarFavoriteAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.DefaultButtonProvider;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.PersistentObject;
import jakarta.inject.Inject;

public class EigenartikelSelector extends CodeSelectorFactory {

	private CommonViewer commonViewer;

	private ToggleVerrechenbarFavoriteAction tvfa = new ToggleVerrechenbarFavoriteAction();
	private ISelectionChangedListener selChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			TreeViewer tv = (TreeViewer) event.getSource();
			StructuredSelection ss = (StructuredSelection) tv.getSelection();
			tvfa.updateSelection(ss.isEmpty() ? null : ss.getFirstElement());

			if (!ss.isEmpty()) {
				IArticle ea = (IArticle) ss.getFirstElement();
				ContextServiceHolder.get().getRootContext().setNamed("ch.elexis.core.ui.eigenartikel.selection", ea); //$NON-NLS-1$
			} else {
				ContextServiceHolder.get().getRootContext().setNamed("ch.elexis.core.ui.eigenartikel.selection", null); //$NON-NLS-1$
			}
		}
	};

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer commonViewer) {
		this.commonViewer = commonViewer;

		MenuManager menu = new MenuManager();
		menu.add(tvfa);
		menu.add(rearrangePackagesAction);

		commonViewer.setNamedSelection("ch.elexis.core.ui.eigenartikel.selection"); //$NON-NLS-1$
		commonViewer.setContextMenu(menu);
		commonViewer.setSelectionChangedListener(selChangeListener);

		EigenartikelTreeContentProvider eal = new EigenartikelTreeContentProvider(commonViewer);

		ShowEigenartikelProductsAction seaoa = new ShowEigenartikelProductsAction(eal, this);
		rearrangePackagesAction
				.setEnabled(ConfigServiceHolder.getUser(ShowEigenartikelProductsAction.FILTER_CFG, false));

		FieldDescriptor<?>[] lbName = new FieldDescriptor<?>[] {
				new FieldDescriptor<IArticle>(EigenartikelTreeContentProvider.FILTER_KEY) };

		SelectorPanelProvider slp = new SelectorPanelProvider(lbName, true);
		slp.addActions(seaoa);

		DefaultButtonProvider dbp = new ViewerConfigurer.DefaultButtonProvider();
		SimpleWidgetProvider swp = new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null);

		EigenartikelTreeLabelProvider alp = new EigenartikelTreeLabelProvider();

		return new ViewerConfigurer(eal, alp, slp, dbp, swp).setContentType(ContentType.GENERICOBJECT);
	}

	@Override
	public Class<?> getElementClass() {
		return IArticle.class;
	}

	@Override
	public String getCodeSystemName() {
		return Constants.TYPE_NAME;
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (!structuredSelection.isEmpty()) {
						ICodeSelectorTarget target = CodeSelectorHandler.getInstance().getCodeSelectorTarget();
						if (target != null) {
							Object obj = structuredSelection.getFirstElement();
							if (obj instanceof IArticle) {
								IArticle article = (IArticle) obj;
								// translate to first package if product selected
								if (article.isProduct()) {
									@SuppressWarnings("unchecked")
									List<IArticle> packages = (List<IArticle>) (List<?>) article.getPackages();
									if (!packages.isEmpty()) {
										article = packages.get(0);
									}
								}
								target.codeSelected(article);
							}
						}
					}
				}
			}
		};
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IArticle.class.equals(clazz)) {
			if (commonViewer != null && !commonViewer.isDisposed()) {
				commonViewer.getViewerWidget().refresh();
			}
		}
	}

	@Optional
	@Inject
	public void update(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IArticle object) {
		if (commonViewer != null && object != null) {
			commonViewer.getViewerWidget().update(object, null);
		}
	}

	private RestrictedAction rearrangePackagesAction = new RestrictedAction(
			new ObjectEvaluatableACE(IArticle.class, Right.UPDATE)) {

		boolean initialized = false;

		{
			setText("Umgruppierung aktivieren");
		}

		@Override
		public void doRun() {
			if (initialized) {
				return;
			}

			commonViewer.getViewerWidget().addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
					new Transfer[] { TextTransfer.getInstance() },
					new ViewerDropAdapter(commonViewer.getViewerWidget()) {

						@Override
						public void dragEnter(final DropTargetEvent event) {
							event.detail = DND.DROP_COPY;
						}

						@Override
						public void drop(final DropTargetEvent event) {
							IArticle target = (IArticle) determineTarget(event);
							String drp = (String) event.data;
							String[] dl = drp.split(","); //$NON-NLS-1$
							for (String obj : dl) {
								PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
								if (dropped instanceof IArticle) {
									IArticle ea = (IArticle) dropped;
									if (ea.isProduct()) {
										continue;
									}
									LockResponse lr = LocalLockServiceHolder.get().acquireLock(target);
									if (lr.isOk()) {
										EigenartikelUtil.copyProductAttributesToArticleSetAsChild(target, ea);
										LocalLockServiceHolder.get().releaseLock(target);
										ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
												IArticle.class);
									} else {
										LockResponseHelper.showInfo(lr, target, log);
									}
								}
							}
						}

						@Override
						public boolean performDrop(Object data) {
							return true;
						}

						@Override
						public boolean validateDrop(Object target, int operation, TransferData transferType) {
							IArticle ea = (IArticle) getSelectedObject();
							IArticle eaTarget = (IArticle) target;
							return (eaTarget != null && eaTarget.isProduct() && ea != null && !ea.isProduct());
						}
					});
			initialized = true;
			setEnabled(!initialized);
		}
	};

	public void allowArticleRearrangement(boolean checked) {
		rearrangePackagesAction.setEnabled(checked);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
