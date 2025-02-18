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
package ch.elexis.core.ui.views.codesystems;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.DelegatingSelectionProvider;
import ch.elexis.core.ui.util.ImporterPage;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.views.FavoritenCTabItem;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.core.ui.views.MakrosCTabItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class CodeDetailView extends ViewPart implements IActivationListener {
	public final static String ID = "ch.elexis.codedetailview"; //$NON-NLS-1$
	private CTabFolder ctab;
	private IAction importAction;
	private ViewMenus viewmenus;
	private Hashtable<String, ImporterPage> importers;

	private DelegatingSelectionProvider delegatingSelectionProvider;

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.NONE);
		importers = new Hashtable<>();

		addAllPages();
		if (ctab.getItemCount() > 0) {
			ctab.setSelection(0);
		}
		ctab.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem selected = ctab.getSelection();

				if (selected != null) {
					if (selected.getControl() instanceof MasterDetailsPage) {
						MasterDetailsPage page = (MasterDetailsPage) selected.getControl();
						if (page.getCodeSelectorFactory().hasContextMenu()) {
							page.getCodeSelectorFactory().activateContextMenu(getSite(), delegatingSelectionProvider,
									ID);
						}
					}
				}

				if (selected instanceof FavoritenCTabItem || selected instanceof MakrosCTabItem) {
					return;
				}

				if (selected != null) {
					String t = selected.getText();
					importAction.setEnabled(importers.get(t) != null);
					MasterDetailsPage page = (MasterDetailsPage) selected.getControl();
					ViewerConfigurer vc = page.cv.getConfigurer();
					vc.getControlFieldProvider().setFocus();
				}
			}
		});
		makeActions();
		viewmenus = new ViewMenus(getViewSite());
		viewmenus.createMenu(importAction /* ,deleteAction */);
		GlobalEventDispatcher.addActivationListener(this, this);
		delegatingSelectionProvider = new DelegatingSelectionProvider();
		getSite().setSelectionProvider(delegatingSelectionProvider);
	}

	private void makeActions() {
		importAction = new Action(Messages.Core_Import_Action) { // $NON-NLS-1$
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
						dlg.getShell().setText(Messages.Core_Import_Data); // $NON-NLS-1$
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

	}

	private void addAllPages() {
		String settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_CODES, null);
		if (settings == null) {
			new MakrosCTabItem(ctab, SWT.NONE);
			new FavoritenCTabItem(ctab, SWT.None);
			addPagesFor(ExtensionPointConstantsUi.DIAGNOSECODE);
			addPagesFor(ExtensionPointConstantsUi.VERRECHNUNGSCODE);
			addPagesFor(ExtensionPointConstantsUi.GENERICCODE);
		} else {
			new MakrosCTabItem(ctab, SWT.NONE);
			addUserSpecifiedPages(settings);
		}

	}

	private void addUserSpecifiedPages(String settings) {
		String[] userSettings = settings.split(","); //$NON-NLS-1$
		Map<Integer, IConfigurationElement> iceMap = new TreeMap<>();

		iceMap = collectNeededPages(ExtensionPointConstantsUi.DIAGNOSECODE, userSettings, iceMap);
		iceMap = collectNeededPages(ExtensionPointConstantsUi.VERRECHNUNGSCODE, userSettings, iceMap);
		iceMap = collectNeededPages(ExtensionPointConstantsUi.GENERICCODE, userSettings, iceMap);

		// add favorites tab if settings desire it
		for (int i = 0; i < userSettings.length; i++) {
			if (userSettings[i].equals("Favoriten")) { //$NON-NLS-1$
				iceMap.put(i, null);
			}
		}

		for (Integer key : iceMap.keySet()) {
			IConfigurationElement ce = iceMap.get(key);
			if (ce == null) {
				new FavoritenCTabItem(ctab, SWT.None);
				continue;
			}

			try {
				IDetailDisplay detailDisplay = (IDetailDisplay) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
				CodeSelectorFactory codeSelector = (CodeSelectorFactory) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
				String a = ce.getAttribute(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
				ImporterPage ip = null;
				if (a != null) {
					ip = (ImporterPage) ce.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
					if (ip != null) {
						importers.put(detailDisplay.getTitle(), ip);
					}
				}

				MasterDetailsPage page = new MasterDetailsPage(ctab, codeSelector, detailDisplay);
				CTabItem ct = new CTabItem(ctab, SWT.NONE);
				ct.setText(detailDisplay.getTitle());
				ct.setControl(page);
				ct.setData(detailDisplay);

				CoreUiUtil.injectServices(codeSelector);
				CoreUiUtil.injectServices(detailDisplay);
			} catch (Exception ex) {
				LoggerFactory.getLogger(getClass()).error("Error creating pages", ex); //$NON-NLS-1$
				ElexisStatus status = new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Initialisieren von " + ce.getName(), ex, ElexisStatus.LOG_WARNINGS);
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		}
	}

	private Map<Integer, IConfigurationElement> collectNeededPages(String point, String[] userSettings,
			Map<Integer, IConfigurationElement> iceMap) {
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		for (IConfigurationElement ce : list) {
			try {
				if ("Artikel".equals(ce.getName())) { //$NON-NLS-1$
					continue;
				}
				IDetailDisplay d = (IDetailDisplay) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
				for (int i = 0; i < userSettings.length; i++) {
					if (userSettings[i].equals(d.getTitle().trim())) {
						iceMap.put(i, ce);
					}
				}
			} catch (Exception ex) {
				ElexisStatus status = new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Initialisieren von " + ce.getName(), ex, ElexisStatus.LOG_WARNINGS);
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		}
		return iceMap;
	}

	private void addPagesFor(String point) {
		List<IConfigurationElement> list = Extensions.getExtensions(point);
		for (IConfigurationElement ce : list) {
			try {
				if ("Artikel".equals(ce.getName())) { //$NON-NLS-1$
					continue;
				}
				IDetailDisplay detailDisplay = (IDetailDisplay) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
				CodeSelectorFactory codeSelector = (CodeSelectorFactory) ce
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
				String a = ce.getAttribute(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
				ImporterPage ip = null;
				if (a != null) {
					ip = (ImporterPage) ce.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_IMPC);
					if (ip != null) {
						importers.put(detailDisplay.getTitle(), ip);
					}
				}
				MasterDetailsPage page = new MasterDetailsPage(ctab, codeSelector, detailDisplay);
				CTabItem ct = new CTabItem(ctab, SWT.NONE);
				ct.setText(detailDisplay.getTitle());
				ct.setControl(page);
				ct.setData(detailDisplay);

				CoreUiUtil.injectServicesWithContext(codeSelector);
				CoreUiUtil.injectServicesWithContext(detailDisplay);
			} catch (Exception ex) {
				LoggerFactory.getLogger(getClass()).error("Error creating pages", ex); //$NON-NLS-1$
				ElexisStatus status = new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
						"Fehler beim Initialisieren von " + ce.getName(), ex, ElexisStatus.LOG_WARNINGS);
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		}
	}

	@Override
	public void setFocus() {
		if (ctab.getItemCount() > 0) {
			ctab.setFocus();
		}
	}

	private class MasterDetailsPage extends Composite {
		private SashForm sash;
		private CommonViewer cv;
		private CodeSelectorFactory master;
		private IDetailDisplay detail;

		public MasterDetailsPage(Composite parent, CodeSelectorFactory codeSelectorFactory,
				IDetailDisplay displayDetail) {
			super(parent, SWT.NONE);

			this.detail = displayDetail;
			this.master = codeSelectorFactory;
			cv = new CommonViewer();
			setLayout(new FillLayout());
			sash = new SashForm(this, SWT.NONE);
			cv.setViewName(master.getCodeSystemName());
			cv.create(master.createViewerConfigurer(cv), sash, SWT.NONE, getViewSite());
			detail.createDisplay(sash, getViewSite());
			cv.getConfigurer().getContentProvider().startListening();
		}

		public CodeSelectorFactory getCodeSelectorFactory() {
			return master;
		}
	}

	@Override
	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, this);
		if ((ctab != null) && (!ctab.isDisposed())) {
			for (CTabItem ct : ctab.getItems()) {
				MasterDetailsPage page = (MasterDetailsPage) ct.getControl();
				// page.cv.getViewerWidget().removeSelectionChangedListener(
				// GlobalEventDispatcher.getInstance().getDefaultListener());
				page.cv.getConfigurer().getContentProvider().stopListening();
				page.dispose();
			}
		}

	}

	/** Vom ActivationListener */
	public void activation(boolean mode) {
		CTabItem selected = ctab.getSelection();
		if (selected instanceof FavoritenCTabItem || selected instanceof MakrosCTabItem)
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

	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
