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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.DelegatingSelectionProvider;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.views.FavoritenCTabItem;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import jakarta.inject.Inject;

/**
 * Bereitstellung der Auswahlliste für Codes aller Art: Oben häufigste des
 * Anwenders, in der Mitte häufigste des Patienten, unten ganze Systenatik
 *
 * @author Gerry
 *
 */
public abstract class CodeSelectorFactory implements IExecutableExtension {
	private static final String CAPTION_ERROR = Messages.Core_Error; // $NON-NLS-1$
	/** Anzahl der in den oberen zwei Listen zu haltenden Elemente */
	public static int ITEMS_TO_SHOW_IN_MFU_LIST = 15;

	private static Logger log = LoggerFactory.getLogger(CodeSelectorFactory.class);

	public CodeSelectorFactory() {
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

	}

	public abstract ViewerConfigurer createViewerConfigurer(CommonViewer cv);

	public abstract Class<?> getElementClass();

	public abstract void dispose();

	public abstract String getCodeSystemName();

	public String getCodeSystemCode() {
		return "999"; //$NON-NLS-1$
	}

	/**
	 * This method queries the <i>org.eclipse.ui.menus</i> extensions, and looks for
	 * menu contributions with a locationURI <i>popup:classname</i>. Found
	 * contributions are added to the {@link IMenuManager}.
	 *
	 * @param manager
	 * @param objects
	 */
	protected void addPopupCommandContributions(IMenuManager manager, Object[] selection) {
		java.util.List<IConfigurationElement> contributions = Extensions.getExtensions("org.eclipse.ui.menus"); //$NON-NLS-1$
		for (IConfigurationElement contributionElement : contributions) {
			String locationUri = contributionElement.getAttribute("locationURI"); //$NON-NLS-1$
			if (locationUri == null) {
				continue;
			}
			String[] parts = locationUri.split(":"); //$NON-NLS-1$
			if (parts.length == 2) {
				if (parts[0].equals("popup") && parts[1].equals(getClass().getName())) { //$NON-NLS-1$
					IConfigurationElement[] command = contributionElement.getChildren("command"); //$NON-NLS-1$
					if (command.length > 0) {
						addMenuContribution(command[0], manager, selection);
					}
				}
			}
		}
	}

	protected void addMenuContribution(IConfigurationElement commandElement, IMenuManager manager, Object[] selection) {
		ContributionAction action = new ContributionAction(commandElement);
		if (action.isValid()) {
			action.setSelection(selection);
			manager.add(action);
		}
	}

	public PersistentObject findElement(String code) {
		String s = getElementClass().getName() + StringConstants.DOUBLECOLON + code;
		return CoreHub.poFactory.createFromString(s);
	}

	public SelectionDialog getSelectionDialog(Shell parent, Object data) {
		throw new UnsupportedOperationException(
				"SelectionDialog for code system " + getCodeSystemName() + " not implemented"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static SelectionDialog getSelectionDialog(String codeSystemName, Shell parent, Object data) {
		java.util.List<IConfigurationElement> list = Extensions.getExtensions(ExtensionPointConstantsUi.GENERICCODE); // $NON-NLS-1$
		list.addAll(Extensions.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE)); // $NON-NLS-1$
		list.addAll(Extensions.getExtensions(ExtensionPointConstantsUi.DIAGNOSECODE)); // $NON-NLS-1$

		if (list != null) {
			for (IConfigurationElement ic : list) {
				java.util.Optional<CodeSystemDescription> systemDescription = CodeSystemDescription.of(ic);
				if (systemDescription.isPresent()) {
					if (codeSystemName.equals(systemDescription.get().getCodeSystemName())) {
						return systemDescription.get().getSelectionDialog(parent, data);
					}
				}
				systemDescription.ifPresent(description -> {
				});
			}
		}
		throw new IllegalStateException("Could not find code system " + codeSystemName); //$NON-NLS-1$
	}

	public static void makeTabs(CTabFolder ctab, IViewSite site, String point) {
		String settings = null;
		if (point.equals(ExtensionPointConstantsUi.VERRECHNUNGSCODE)) {
			settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_SRV, null);
		} else if (point.equals(ExtensionPointConstantsUi.DIAGNOSECODE)) {
			settings = ConfigServiceHolder.getUser(Preferences.USR_SERVICES_DIAGNOSES_DIAGNOSE, null);
		}

		java.util.List<IConfigurationElement> list = Extensions.getExtensions(point);
		if (settings == null) {
			addAllTabs(list, ctab, point);
		} else {
			addUserSpecifiedTabs(list, settings, ctab, point);
		}

		if (ctab.getItemCount() > 0) {
			ctab.setSelection(0);
		}
	}

	/**
	 * add all available tabs as they occur (independent from any user settings)
	 *
	 * @param list list of tabs to add
	 * @param ctab parent
	 */
	private static void addAllTabs(java.util.List<IConfigurationElement> list, CTabFolder ctab, String point) {
		ITEMS_TO_SHOW_IN_MFU_LIST = ConfigServiceHolder.getUser(Preferences.USR_MFU_LIST_SIZE, 15);
		ctab.setSimple(false);

		// add favorites tab first
		if (point.equals(ExtensionPointConstantsUi.VERRECHNUNGSCODE)) {
			new FavoritenCTabItem(ctab, SWT.None);
		}

		if (list != null) {
			for (IConfigurationElement ic : list) {
				java.util.Optional<CodeSystemDescription> systemDescription = CodeSystemDescription.of(ic);
				if (systemDescription.isPresent()) {
					CTabItem tabItem = new CTabItem(ctab, SWT.NONE);
					tabItem.setText(systemDescription.get().getCodeSystemName());
					tabItem.setData(systemDescription.get());
				}
			}
		}
	}

	/**
	 * add tabs dependent on user settings (user defines which tabs are displayed
	 * and in which position)
	 *
	 * @param list
	 * @param settings user defined tabs and order
	 * @param ctab     parent
	 * @param point    in case its a VERRECHNUNGSCODE add favorite tab
	 */
	private static void addUserSpecifiedTabs(java.util.List<IConfigurationElement> list, String settings,
			CTabFolder ctab, String point) {

		Map<String, IConfigurationElement> allIcMap = new HashMap<>();
		for (IConfigurationElement ic : list) {
			try {
				IDetailDisplay d = (IDetailDisplay) ic
						.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CDD);
				allIcMap.put(d.getTitle(), ic);
			} catch (Exception e) {
				ExHandler.handle(e);
			}
		}

		String[] userSettings = settings.split(","); //$NON-NLS-1$
		for (String tab : userSettings) {
			if ("Favoriten".equals(tab)) { //$NON-NLS-1$
				if (point.equals(ExtensionPointConstantsUi.VERRECHNUNGSCODE)) {
					new FavoritenCTabItem(ctab, SWT.NONE);
				}
			} else {
				IConfigurationElement ic = allIcMap.get(tab);
				if (ic == null) {
					continue;
				}
				java.util.Optional<CodeSystemDescription> systemDescription = CodeSystemDescription.of(ic);
				if (systemDescription.isPresent()) {
					CTabItem tabItem = new CTabItem(ctab, SWT.NONE);
					tabItem.setText(systemDescription.get().getCodeSystemName());
					tabItem.setData(systemDescription.get());
				}
			}
		}
	}

	/**
	 * If the user resizes the parts of the selector, we'll remember the new size
	 *
	 * @author gerry
	 *
	 */
	private static class ResizeListener extends ControlAdapter {
		private final String k;
		private final SashForm mine;

		ResizeListener(SashForm form, String key) {
			k = key;
			mine = form;
		}

		@Override
		public void controlResized(ControlEvent e) {
			int[] weights = mine.getWeights();
			StringBuilder v = new StringBuilder();
			v.append(Integer.toString(weights[0])).append(",").append(Integer.toString(weights[1])) //$NON-NLS-1$
					.append(",").append(Integer.toString(weights[2])); //$NON-NLS-1$
			ConfigServiceHolder.setUser(k, v.toString());
		}

	}

	/**
	 * Display page for one codesystem. Upper part: MFU user, middle part: MFU
	 * patient, lower part: All codes
	 *
	 * @author gerry
	 *
	 */
	public static class cPage extends Composite {
		private CodeElementStatisticsComposite userStatistics, patientStatistics;
		private CodeSystemDescription description;
		CommonViewer cv;
		ViewerConfigurer vc;
		int[] sashWeights = null;
		ResizeListener resizeListener;

		@Inject
		void activeUser(@Optional IUser user) {
			Display.getDefault().asyncExec(() -> {
				adaptForUser(user);
			});
		}

		private void adaptForUser(IUser user) {
			if (patientStatistics != null && (!patientStatistics.isDisposed())) {
				patientStatistics.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
			}
			if (userStatistics != null && (!userStatistics.isDisposed())) {
				userStatistics.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
			}
			if (cv != null && cv.getViewerWidget() != null && (!cv.getViewerWidget().getControl().isDisposed())) {
				cv.getViewerWidget().getControl().setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
			}
			userStatistics.setContact(user.getAssignedContact());
		}

		cPage(final CTabItem ctab, final CodeSystemDescription description) {
			super(ctab.getParent(), SWT.NONE);
			initContent(description);
		}

		cPage(final CTabFolder ctab, final CodeSystemDescription description) {
			super(ctab, SWT.NONE);
			initContent(description);
		}

		private void initContent(CodeSystemDescription description) {
			this.description = description;
			setLayout(new FillLayout());
			SashForm sash = new SashForm(this, SWT.VERTICAL | SWT.SMOOTH);
			String cfgKey = "ansicht/codesystem/" + description.getCodeSystemName(); //$NON-NLS-1$
			resizeListener = new ResizeListener(sash, cfgKey);
			String sashW = ConfigServiceHolder.getUser(cfgKey, "20,20,60"); //$NON-NLS-1$
			sashWeights = new int[3];
			int i = 0;
			for (String sw : sashW.split(",")) { //$NON-NLS-1$
				sashWeights[i++] = Integer.parseInt(sw);
			}

			userStatistics = new CodeElementStatisticsComposite(description.getElexisClassName(), sash, SWT.NONE);
			userStatistics.setTitle(Messages.CodeSelectorFactory_yourMostFrequent);
			userStatistics.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

			patientStatistics = new CodeElementStatisticsComposite(description.getElexisClassName(), sash, SWT.NONE);
			patientStatistics.setTitle(Messages.CodeSelectorFactory_patientsMostFrequent);
			patientStatistics.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));

			Group gAll = new Group(sash, SWT.NONE);
			gAll.setText(Messages.Core_All); // $NON-NLS-1$
			gAll.setLayout(new GridLayout());
			cv = new CommonViewer();
			vc = description.getCodeSelectorFactory().createViewerConfigurer(cv);
			// add double click listener for generic CodeSelectorTarget, added before create
			// of CommonViewer
			if (vc.getContentType() == ContentType.GENERICOBJECT) {
				vc.setDoubleClickListener(description.getCodeSelectorFactory().getDoubleClickListener());
			}

			Composite cvc = new Composite(gAll, SWT.NONE);
			cvc.setLayout(new GridLayout());
			cvc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cv.create(vc, cvc, SWT.NONE, this);
			cv.getViewerWidget().getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			vc.getContentProvider().startListening();

			try {
				sash.setWeights(sashWeights);
			} catch (Throwable t) {
				ExHandler.handle(t);
				sash.setWeights(new int[] { 20, 20, 60 });
			}

			CoreUiUtil.injectServicesWithContext(this);
			refresh();
		}

		/*
		 * (Kein Javadoc)
		 *
		 * @see org.eclipse.swt.widgets.Widget#dispose()
		 */
		@Override
		public void dispose() {
			vc.getContentProvider().stopListening();
			super.dispose();
		}

		public void refresh() {
			patientStatistics.setContact(ContextServiceHolder.get().getActivePatient().orElse(null));
			userStatistics.setContact(ContextServiceHolder.get().getActiveUserContact().orElse(null));
		}
	}

	/**
	 * Test if the {@link CodeSelectorFactory} implementation has a context menu,
	 * that should be registered with the workbench.
	 *
	 * @return
	 */
	public boolean hasContextMenu() {
		return getSelectionProvider() != null && getMenuManager() != null;
	}

	/**
	 * Get the {@link ISelectionProvider} of this {@link CodeSelectorFactory}.
	 * Override this method and {@link #getMenuManager()}, so a {@link IViewPart}
	 * can register the context menu with the workbench using
	 * {@link #activateContextMenu(IWorkbenchPartSite, DelegatingSelectionProvider, String)}.
	 *
	 * @return
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}

	/**
	 * Get the {@link MenuManager} of this {@link MenuManager}. Override this method
	 * and {@link #getSelectionProvider()}, so a {@link IViewPart} can register the
	 * context menu with the workbench using
	 * {@link #activateContextMenu(IWorkbenchPartSite, DelegatingSelectionProvider, String)}.
	 *
	 * @return
	 */
	public MenuManager getMenuManager() {
		return null;
	}

	/**
	 * Registers the context menu, if {@link #hasContextMenu()} returns true, with
	 * the site. The id of the context menu is viewId plus . and
	 * {@link #getCodeSystemName()}. <br />
	 * example ids: <i>ch.elexis.codedetailview.Block</i> or
	 * <i>ch.elexis.LeistungenView.Block</i>
	 *
	 * @param site
	 */
	public void activateContextMenu(IWorkbenchPartSite site, DelegatingSelectionProvider selectionProvider,
			String viewId) {
		if (hasContextMenu() && site.getPart() != null) {
			selectionProvider.setSelectionProviderDelegate(getSelectionProvider());
			site.registerContextMenu(viewId + "." + getCodeSystemName(), getMenuManager(), selectionProvider); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the {@link IDoubleClickListener} used on the Viewer of this
	 * {@link CodeSelectorFactory}. Default implementation passes the selected
	 * {@link Object} directly to the code selector target (manage via
	 * {@link CodeSelectorHandler}). If a {@link Leistungsblock} is selected it will
	 * pass its contained elements to the code selector target. </br>
	 * </br>
	 * Should be overridden by subclasses for special behaviour.
	 *
	 * @return
	 */
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
							// TODO implement for block
							target.codeSelected(obj);
						}
					}
				}
			}
		};
	}
}
