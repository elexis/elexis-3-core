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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.IRefreshable;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class KontaktDetailView extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.KontaktDetailView"; //$NON-NLS-1$
	KontaktBlatt kb;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	public KontaktDetailView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		kb = new KontaktBlatt(parent, SWT.NONE, getViewSite());
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(GlobalActions.printKontaktEtikette);

		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
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

	@Override
	public void refresh() {
		kb.refresh();
	}
}
