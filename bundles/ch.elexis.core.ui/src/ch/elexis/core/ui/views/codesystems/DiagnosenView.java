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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory.cPage;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class DiagnosenView extends ViewPart implements IActivationListener {
	public final static String ID = "ch.elexis.DiagnosenView"; //$NON-NLS-1$
	CTabFolder ctab;
	CTabItem selected;

	public DiagnosenView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		ctab = new CTabFolder(parent, SWT.BOTTOM);
		ctab.setSimple(false);
		ctab.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selected = ctab.getSelection();
				if (selected != null) {
					cPage page = (cPage) selected.getControl();
					if (page == null) {

						page = new cPage(ctab, (CodeSystemDescription) selected.getData());
						selected.setControl(page);
						// parent.redraw();
					}
					page.cv.getConfigurer().getControlFieldProvider().clearValues();
				}
				((cPage) selected.getControl()).refresh();
				setFocus();
			}

		});

		CodeSelectorFactory.makeTabs(ctab, getViewSite(), ExtensionPointConstantsUi.DIAGNOSECODE);

		GlobalEventDispatcher.addActivationListener(this, this);
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
		if (selected != null) {
			cPage page = (cPage) selected.getControl();
			if (page == null) {
				// SWTHelper.alert(CAPTION_ERROR, "cPage=null"); //$NON-NLS-1$
				page = new cPage(ctab, (CodeSystemDescription) selected.getData());
				selected.setControl(page);
				// parent.redraw();
			}
			page.cv.getConfigurer().getControlFieldProvider().setFocus();
		}
	}

	public void activation(boolean mode) {
		if (mode == false) {
			if (selected != null) {
				cPage page = (cPage) selected.getControl();
				page.cv.getConfigurer().getControlFieldProvider().clearValues();
			}

			// remove any ICodeSelectiorTarget, since it's no more needed
			CodeSelectorHandler.getInstance().removeCodeSelectorTarget();
		} else {
			if (selected != null) {
				cPage page = (cPage) selected.getControl();
				page.refresh();
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
