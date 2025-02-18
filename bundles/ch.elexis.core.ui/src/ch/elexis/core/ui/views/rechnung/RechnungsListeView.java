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
package ch.elexis.core.ui.views.rechnung;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewer.PoDoubleClickListener;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListBottomComposite;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Tree;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Display a listing of all bills selected after several user selectable
 * criteria. The selected bills can be modified or exported.
 *
 * @author gerry
 *
 */
public class RechnungsListeView extends ViewPart implements IRefreshable {

	public final static String ID = "ch.elexis.RechnungsListeView"; //$NON-NLS-1$

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	CommonViewer cv;
	ViewerConfigurer vc;
	RnActions actions;
	RnContentProvider cntp;
	RnControlFieldProvider cfp;

	private InvoiceListBottomComposite invoiceListeBottomComposite;

	@Optional
	@Inject
	public void activeMandator(IMandator mandator) {
		if (invoiceListeBottomComposite != null) {
			invoiceListeBottomComposite.updateMahnAutomatic();
		}
	}

	public RechnungsListeView() {
	}

	@Override
	public void createPartControl(final Composite p) {
		p.setLayout(new GridLayout());
		// SashForm sash=new SashForm(p,SWT.VERTICAL);
		Composite comp = new Composite(p, SWT.NONE);
		comp.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		comp.setLayout(new GridLayout());
		cv = new CommonViewer();
		cntp = new RnContentProvider(this, cv);
		cfp = new RnControlFieldProvider();
		vc = new ViewerConfigurer(cntp, new ViewerConfigurer.TreeLabelProvider(), cfp,
				new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.V_SCROLL | SWT.MULTI, cv));
		// rnFilter=FilterFactory.createFilter(Rechnung.class,"Rn
		// Nummer","Name","Vorname","Betrag");
		cv.create(vc, comp, SWT.NONE, getViewSite());

		cv.addDoubleClickListener(new PoDoubleClickListener() {
			@Override
			public void doubleClicked(PersistentObject obj, CommonViewer cv) {
				if (obj instanceof Patient) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Patient) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.showView(UiResourceConstants.PatientDetailView2_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Fall) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Fall) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.showView(FallDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else if (obj instanceof Rechnung) {
					try {
						ElexisEventDispatcher.fireSelectionEvent((Rechnung) obj);
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RnDetailView.ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});

		invoiceListeBottomComposite = new InvoiceListBottomComposite(comp, SWT.NONE);

		cv.getViewerWidget().getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ViewMenus menu = new ViewMenus(getViewSite());
		actions = new RnActions(this);
		menu.createToolbar(actions.reloadAction, actions.mahnWizardAction, actions.rnFilterAction, null,
				actions.rnExportAction);
		menu.createMenu(actions.expandAllAction, actions.collapseAllAction, actions.printListeAction,
				actions.exportListAction, actions.addAccountExcessAction);
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new RnMenuListener(this));
		cv.setContextMenu(mgr);
		cntp.startListening();
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	public InvoiceListBottomComposite getInvoiceListeBottomComposite() {
		return invoiceListeBottomComposite;
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		cntp.stopListening();
		super.dispose();
	}

	private boolean isOldShown = false;

	@Override
	public void setFocus() {
		if (!isOldShown) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Ansicht veraltet", "Die Ansicht "
					+ getTitle()
					+ " ist veraltet, und wird nicht mehr unterstützt. Bitte verwenden Sie die Rechnungsliste Ansicht.");
			isOldShown = true;
		}
	}

	@SuppressWarnings("unchecked")
	List<Rechnung> createList() {
		IStructuredSelection sel = (IStructuredSelection) cv.getViewerWidget().getSelection();
		List<Tree> at = sel.toList();
		List<Rechnung> ret = new LinkedList<>();
		for (Tree<PersistentObject> t : at) {
			if (t.contents instanceof Patient) {
				for (Tree<PersistentObject> tp : t.getChildren()) {
					for (Tree<PersistentObject> tf : tp.getChildren()) {
						Rechnung rn = (Rechnung) tf.contents;
						if (!ret.contains(rn)) {
							ret.add(rn);
						}
					}
				}
			} else if (t.contents instanceof Fall) {
				for (Tree<PersistentObject> tr : t.getChildren()) {
					Rechnung rn = (Rechnung) tr.contents;
					if (!ret.contains(rn)) {
						ret.add(rn);
					}
				}
			} else if (t.contents instanceof Rechnung) {
				Rechnung rn = (Rechnung) t.contents;
				if (!ret.contains(rn)) {
					ret.add(rn);
				}
			}
		}
		return ret;
	}

	@Override
	public void refresh() {
		reload(IInvoice.class);
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IInvoice.class.equals(clazz)) {
			CoreUiUtil.runAsyncIfActive(() -> {
				cv.notify(CommonViewer.Message.update);
			}, cv);
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
