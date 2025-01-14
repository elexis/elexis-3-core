/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted Eigenartikel to ch.elexis.eigenartikel
 *
 *******************************************************************************/

package ch.elexis.core.ui.views.codesystems;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.DelegatingSelectionProvider;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FavoritenCTabItem;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory.cPage;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class LeistungenView extends ViewPart implements IActivationListener {

	private static final String CAPTION_ERROR = Messages.Core_Error; // $NON-NLS-1$
	public final static String ID = "ch.elexis.LeistungenView"; //$NON-NLS-1$
	public CTabFolder ctab;
	CTabItem selected;
	private String defaultRGB;

	private DelegatingSelectionProvider delegatingSelectionProvider;

	public LeistungenView() {
		defaultRGB = UiDesk.createColor(new RGB(255, 255, 255));
	}

	@Override
	public void createPartControl(final Composite parent) {
		delegatingSelectionProvider = new DelegatingSelectionProvider();

		parent.setLayout(new GridLayout());
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		ctab.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ctab.setSimple(false);
		ctab.setMRUVisible(true);
		ctab.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selected = ctab.getSelection();

				if (selected instanceof FavoritenCTabItem)
					return;

				if (selected != null) {
					CodeSystemDescription description = (CodeSystemDescription) selected.getData();
					cPage page = (cPage) selected.getControl();
					if (page == null) {
						// SWTHelper.alert(CAPTION_ERROR, "cPage=null"); //$NON-NLS-1$
						page = new cPage(ctab, description);
						selected.setControl(page);
						// parent.redraw();
					}
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
					if (description.getCodeSelectorFactory() != null
							&& description.getCodeSelectorFactory().hasContextMenu()) {
						description.getCodeSelectorFactory().activateContextMenu(getSite(), delegatingSelectionProvider,
								ID);
					}
				}
				((cPage) selected.getControl()).refresh();
				setFocus();
			}

		});

		// menu to select & define color
		Menu tabFolderMenu = new Menu(ctab);
		MenuItem miColor = new MenuItem(tabFolderMenu, SWT.POP_UP);
		miColor.setText(Messages.LeistungenView_defineColor);
		miColor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem item = ctab.getSelection();
				ColorDialog cd = new ColorDialog(UiDesk.getTopShell());
				RGB selected = cd.open();
				if (selected != null) {
					ConfigServiceHolder.setGlobal(Preferences.LEISTUNGSCODES_COLOR + item.getText(),
							UiDesk.createColor(selected));
					setCTabItemColor(ctab.getSelection().getText());
				}
			}
		});
		ctab.setMenu(tabFolderMenu);

		CodeSelectorFactory.makeTabs(ctab, getViewSite(), ExtensionPointConstantsUi.VERRECHNUNGSCODE); // $NON-NLS-1$
		GlobalEventDispatcher.addActivationListener(this, this);
		getSite().setSelectionProvider(delegatingSelectionProvider);
	}

	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (selected == null) {
			if (ctab.getItems().length > 0) {
				selected = ctab.getSelection();
			}
		}
		if (selected instanceof FavoritenCTabItem) {
			((FavoritenCTabItem) selected).update();
			return;
		}
		if (selected != null) {
			cPage page = (cPage) selected.getControl();
			if (page == null) {
				page = new cPage(ctab, (CodeSystemDescription) selected.getData());
				selected.setControl(page);
				// parent.redraw();
			}
			page.cv.getConfigurer().getControlFieldProvider().setFocus();
		}
		setCTabItemColor(selected.getText());
	}

	private void setCTabItemColor(String id) {
		String rgbColor = ConfigServiceHolder.getGlobal(Preferences.LEISTUNGSCODES_COLOR + id, defaultRGB);
		Color color = UiDesk.getColorFromRGB(rgbColor);
		ctab.setSelectionBackground(new Color[] { UiDesk.getDisplay().getSystemColor(SWT.COLOR_WHITE), color },
				new int[] { 100 }, true);
	}

	void swapTabs(int iLeft, int iRight) {
		CTabItem ctLeft = ctab.getItem(iLeft);
		CTabItem ctRight = ctab.getItem(iRight);
		String t = ctLeft.getText();
		Control c = ctLeft.getControl();
		ctLeft.setText(ctRight.getText());
		ctLeft.setControl(ctRight.getControl());
		ctRight.setText(t);
		ctRight.setControl(c);
	}

	public void activation(boolean mode) {
		if (selected instanceof FavoritenCTabItem)
			return;
		if (mode == false) {
			if (selected != null) {
				cPage page = (cPage) selected.getControl();
				if (page != null && !page.isDisposed()) {
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
				}
			}
			// remove any ICodeSelectiorTarget, since it's no more needed
			CodeSelectorHandler.getInstance().removeCodeSelectorTarget();
		} else {
			if (selected != null) {
				if (selected.getControl() == null) {
					initCTabItemControl(selected);
				}
				cPage page = (cPage) selected.getControl();
				page.refresh();
			}

		}

	}

	private void initCTabItemControl(CTabItem tabItem) {
		CodeSystemDescription systemDescription = (CodeSystemDescription) tabItem.getData();
		if (systemDescription != null) {
			cPage page = new cPage(tabItem, systemDescription);
			selected.setControl(page);
		}
	}

	public void visible(boolean mode) {
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	public void setSelected(CTabItem ctab) {
		selected = ctab;
	}
}
