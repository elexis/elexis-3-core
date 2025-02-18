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
package ch.elexis.core.ui.medication.views;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.ListDisplaySelectionProvider;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Eine platzsparende View zur Anzeige der Dauermedikation
 *
 * @author gerry
 *
 */
public class DauerMediView extends ViewPart implements IRefreshable {
	public final static String ID = "ch.elexis.dauermedikationview"; //$NON-NLS-1$
	private IAction toClipBoardAction;
	FixMediDisplay dmd;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	public DauerMediView() {

	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		dmd = new FixMediDisplay(parent, getViewSite());
		dmd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		ListDisplaySelectionProvider selDisplay = new ListDisplaySelectionProvider(dmd);
		getSite().registerContextMenu(FixMediDisplay.ID, dmd.getMenuManager(), selDisplay);
		getSite().setSelectionProvider(selDisplay);

		makeActions();
		getViewSite().getActionBars().getToolBarManager().add(toClipBoardAction);

		getSite().getPage().addPartListener(udpateOnVisible);
	}

	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);

		dmd.dispose();
	}

	@Override
	public void setFocus() {
		dmd.setFocus();
	}

	private void makeActions() {
		toClipBoardAction = new Action(Messages.Core_Copy) { // $NON-NLS-1$
			{
				setToolTipText(Messages.DauerMediView_copyToClipboard); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_CLIPBOARD.getImageDescriptor());
			}

			@Override
			public void run() {
				dmd.toClipBoard(true);
			}

		};
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void refresh() {
		if (dmd != null && !dmd.isDisposed()) {
			dmd.reload();
		}
	}
}
