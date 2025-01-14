/*******************************************************************************
 * Copyright (c) 2006-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - several modifications
 *******************************************************************************/

package ch.elexis.core.ui.views.artikel;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.views.FavoritenCTabItem;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class ArtikelView extends ViewPart implements IActivationListener {
	private static final String KEY_CE = "ce"; //$NON-NLS-1$
	private static final String KEY_DETAIL = "detail"; //$NON-NLS-1$
	public static final String ID = "ch.elexis.artikelview"; //$NON-NLS-1$
	private CTabFolder ctab;
	private IAction importAction /* ,deleteAction */;
	private ViewMenus viewmenus;
	private Hashtable<String, ImporterPage> importers;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		importers = new Hashtable<>();

		new FavoritenCTabItem(ctab, SWT.None);
		addPagesFor(ExtensionPointConstantsUi.VERRECHNUNGSCODE);

		if (ctab.getItemCount() > 0) {
			ctab.setSelection(0);
		}
		ctab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selected = ctab.getSelection();

				if (selected instanceof FavoritenCTabItem)
					return;

				if (selected != null) {
					String t = selected.getText();

					MasterDetailsPage page = (MasterDetailsPage) selected.getControl();
					if (page == null) {
						try {
							IDetailDisplay det = (IDetailDisplay) selected.getData(KEY_DETAIL);
							IConfigurationElement ce = (IConfigurationElement) selected.getData(KEY_CE);
							CodeSelectorFactory codeSelectorFactory = (CodeSelectorFactory) ce
									.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
							if (codeSelectorFactory != null) {
								CoreUiUtil.injectServices(codeSelectorFactory);
							}
							String a = ce.getAttribute(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
							ImporterPage ip = null;
							if (a != null) {
								ip = (ImporterPage) ce
										.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
								if (ip != null) {
									importers.put(det.getTitle(), ip);
								}
							}

							page = new MasterDetailsPage(ctab, codeSelectorFactory, det);
							selected.setControl(page);
							selected.setData(det);
						} catch (Exception ex) {
							LoggerFactory.getLogger(getClass()).error("Error creating pages", ex); //$NON-NLS-1$
							return;
						}
					}
					importAction.setEnabled(importers.get(t) != null);
					ViewerConfigurer vc = page.cv.getConfigurer();
					vc.getControlFieldProvider().setFocus();
				}
			}

		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createMenu(importAction /* ,deleteAction */);
		GlobalEventDispatcher.addActivationListener(this, this);

	}

	private void makeActions() {
		importAction = new Action(Messages.Core_Import_Action) {
			@Override
			public void run() {
				CTabItem it = ctab.getSelection();
				if (it != null) {
					ImporterPage top = importers.get(it.getText());
					if (top != null) {
						ImportDialog dlg = new ImportDialog(getViewSite().getShell(), top);
						dlg.create();
						dlg.setTitle(top.getTitle());
						dlg.setMessage(top.getDescription());
						dlg.getShell().setText(Messages.Core_Import_Data);
						if (dlg.open() == Dialog.OK) {
							top.run(false);
						}
					}
				}

			}

		};
	}

	private class ImportDialog extends TitleAreaDialog {
		ImporterPage importer;

		public ImportDialog(Shell parentShell, ImporterPage i) {
			super(parentShell);
			importer = i;
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			return importer.createPage(parent);
		}

		@Override
		protected void okPressed() {
			importer.collect();
			super.okPressed();
		}

	}

	private void addPagesFor(String point) {
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		IDetailDisplay detailDisplay = null;
		CodeSelectorFactory codeSelector = null;
		boolean headerDone = false;
		for (int i = 0; i < list.size(); i++) {
			IConfigurationElement ce = list.get(i);
			try {
				if (!"Artikel".equals(ce.getName())) //$NON-NLS-1$
					continue;
				// The first page initializes the screen
				if (!headerDone) {
					detailDisplay = (IDetailDisplay) ce
							.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
					String a = ce.getAttribute(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
					ImporterPage ip = null;
					if (a != null) {
						ip = (ImporterPage) ce
								.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
						if (ip != null) {
							importers.put(detailDisplay.getTitle(), ip);
						}
					}
					codeSelector = (CodeSelectorFactory) ce
							.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
					MasterDetailsPage page = new MasterDetailsPage(ctab, codeSelector, detailDisplay);
					CTabItem ct = new CTabItem(ctab, SWT.None);
					ct.setText(detailDisplay.getTitle());
					ct.setControl(page);
					ct.setData(detailDisplay);
					page.sash.setWeights(new int[] { 30, 70 });
					headerDone = true;

					if (codeSelector != null) {
						CoreUiUtil.injectServicesWithContext(codeSelector);
					}
					if (detailDisplay != null) {
						CoreUiUtil.injectServicesWithContext(detailDisplay);
					}
					continue;
				}
				detailDisplay = (IDetailDisplay) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
				CTabItem ct = new CTabItem(ctab, SWT.NONE);
				ct.setText(detailDisplay.getTitle());
				ct.setData(KEY_CE, ce);
				ct.setData(KEY_DETAIL, detailDisplay);
				if (detailDisplay != null) {
					CoreUiUtil.injectServicesWithContext(detailDisplay);
				}
			} catch (Exception ex) {
				LoggerFactory.getLogger(getClass()).error("Error creating pages", ex); //$NON-NLS-1$
				MessageBox mb = new MessageBox(getViewSite().getShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setText(Messages.Core_Error);
				mb.setMessage(Messages.Core_Error_Initialising_code_system + ce.getName() + ":\n" //$NON-NLS-1$
						+ ex.getLocalizedMessage());
				mb.open();
			}

		}
	}

	@Override
	public void setFocus() {
		if (ctab.getItemCount() > 0) {
			ctab.setFocus();
		}
	}

	/*
	 * public void selectionEvent(PersistentObject obj){ CTabItem top =
	 * ctab.getSelection(); if (top != null) { IDetailDisplay ids = (IDetailDisplay)
	 * top.getData(); Class cl = ids.getElementClass(); String o1 =
	 * obj.getClass().getName(); String o2 = cl.getName(); if (o1.equals(o2)) {
	 * ids.display(obj); } }
	 *
	 * }
	 */

	class MasterDetailsPage extends Composite {
		SashForm sash;
		CommonViewer cv;
		IDetailDisplay detailDisplay;

		MasterDetailsPage(Composite parent, CodeSelectorFactory master, IDetailDisplay detail) {
			super(parent, SWT.NONE);
			setLayout(new FillLayout());
			sash = new SashForm(this, SWT.NONE);
			cv = new CommonViewer();
			ViewerConfigurer vc = master.createViewerConfigurer(cv);
			if (vc.getContentType() == ContentType.GENERICOBJECT) {
				vc.setDoubleClickListener(master.getDoubleClickListener());
			}
			cv.create(vc, sash, SWT.NONE, getViewSite());
			// cv.getViewerWidget().addSelectionChangedListener(
			// GlobalEventDispatcher.getInstance().getDefaultListener());
			/* Composite page= */detail.createDisplay(sash, getViewSite());
			cv.getConfigurer().getContentProvider().startListening();
			detailDisplay = detail;
		}
	}

	@Override
	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, this);
		if ((ctab != null) && (!ctab.isDisposed())) {
			for (CTabItem ct : ctab.getItems()) {
				MasterDetailsPage page = (MasterDetailsPage) ct.getControl();
				// ((MasterDetailsPage) ct.getControl()).cv.getViewerWidget()
				// .removeSelectionChangedListener(
				// GlobalEventDispatcher.getInstance()
				// .getDefaultListener());
				page.cv.getConfigurer().getContentProvider().stopListening();
				page.dispose();
			}
		}

	}

	/** Vom ActivationListener */
	public void activation(boolean mode) {
		CTabItem selected = ctab.getSelection();
		if (selected instanceof FavoritenCTabItem)
			return;
		if (selected != null) {
			MasterDetailsPage page = (MasterDetailsPage) selected.getControl();
			ViewerConfigurer vc = page.cv.getConfigurer();
			if (mode == true) {
				vc.getControlFieldProvider().setFocus();
			} else {
				vc.getControlFieldProvider().clearValues();
			}
		}
	}

	public void visible(boolean mode) {
		System.out.println(this.getClass().getName() + " visible " + mode); //$NON-NLS-1$
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
