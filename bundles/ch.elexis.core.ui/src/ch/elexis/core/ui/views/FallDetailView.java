/*******************************************************************************
 * Copyright (c) 2005-2012, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - fix for wrong case being set if eeli_fall listener not active
 *
 *******************************************************************************/

package ch.elexis.core.ui.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.locks.ToggleCurrentCaseLockHandler;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;

public class FallDetailView extends ViewPart implements IRefreshable {
	public static final String ID = "ch.elexis.FallDetailView"; //$NON-NLS-1$
	FallDetailBlatt2 fdb;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	@Inject
	void activeUser(@Optional IUser user) {
		Display.getDefault().asyncExec(() -> {
			if (fdb != null && !fdb.isDisposed()) {
				adaptForUser(user);
			}
		});
	}

	private void adaptForUser(IUser user) {
		fdb.setUser(user);
		fdb.reloadBillingSystemsMenu();
	}

	@Inject
	public void activeCoverage(@Optional ICoverage coverage) {
		CoreUiUtil.runAsyncIfActive(() -> {
			Fall fall = (Fall) NoPoUtil.loadAsPersistentObject(coverage, Fall.class);
			Fall deselectedFall = fdb.getFall();
			if (fall != null) {
				fdb.setFall(fall);
				if (deselectedFall != null) {
					releaseAndRefreshLock(deselectedFall, ToggleCurrentCaseLockHandler.COMMAND_ID);
				}
			} else {
				fdb.setFall(null);
				if (deselectedFall != null) {
					releaseAndRefreshLock(deselectedFall, ToggleCurrentCaseLockHandler.COMMAND_ID);
				}
			}
		}, fdb);
	}

	@Optional
	@Inject
	void lockedCoverage(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) ICoverage coverage) {
		Fall fall = (Fall) NoPoUtil.loadAsPersistentObject(coverage, Fall.class);
		if (fdb != null && fall.equals(fdb.getFall())) {
			fdb.setUnlocked(true);
		}
	}

	@Optional
	@Inject
	void unlockedCoverage(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) ICoverage coverage) {
		Fall fall = (Fall) NoPoUtil.loadAsPersistentObject(coverage, Fall.class);
		if (fdb != null && fall.equals(fdb.getFall())) {
			fdb.setUnlocked(false);
		}
	}

	private void releaseAndRefreshLock(IPersistentObject object, String commandId) {
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout());
		fdb = new FallDetailBlatt2(parent);
		fdb.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		fdb.setUnlocked(false);

		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	@Override
	public void setFocus() {
		fdb.setFocus();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	public Fall getActiveFall() {
		return fdb.getFall();
	}

	@Override
	public void refresh() {
		fdb.setFall((IFall) NoPoUtil.loadAsPersistentObject(ContextServiceHolder.get().getActiveCoverage().orElse(null),
				Fall.class));
	}
}
