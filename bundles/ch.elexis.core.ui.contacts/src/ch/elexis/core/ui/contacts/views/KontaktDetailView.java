/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.contacts.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.ViewMenus;

public class KontaktDetailView extends ViewPart {
	public static final String ID = "ch.elexis.KontaktDetailView"; //$NON-NLS-1$
	KontaktBlatt kb;

	public KontaktDetailView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		kb = new KontaktBlatt(parent, SWT.NONE, getViewSite());
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(GlobalActions.printKontaktEtikette);
	}

	@Override
	public void setFocus() {
		kb.setFocus();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
