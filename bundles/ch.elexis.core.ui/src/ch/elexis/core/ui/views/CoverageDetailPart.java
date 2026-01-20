
package ch.elexis.core.ui.views;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.e4.SWTHelper;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * @since 3.13 Can not be moved to ch.elexis.core.ui.e4 due to
 *        {@link CoverageDetailComposite}
 */
public class CoverageDetailPart {

	private CoverageDetailComposite coverageDetailComposite;

	@Inject
	public void activeCoverage(@Optional ICoverage coverage) {
		CoreUiUtil.runAsyncIfActive(() -> {
//			ICoverage deselectedCoverage = fdb.getFall();
			if (coverage != null) {
				coverageDetailComposite.setFall(coverage);
//				if (deselectedCoverage != null) {
//					releaseAndRefreshLock(deselectedCoverage, ToggleCurrentCaseLockHandler.COMMAND_ID);
//				}
			} else {
				coverageDetailComposite.setFall(null);
//				if (deselectedCoverage != null) {
//					releaseAndRefreshLock(deselectedCoverage, ToggleCurrentCaseLockHandler.COMMAND_ID);
//				}
			}
		}, coverageDetailComposite);
	}

	@Inject
	void activeUser(@Optional IUser user) {
		Display.getDefault().asyncExec(() -> {
			if (coverageDetailComposite != null && !coverageDetailComposite.isDisposed()) {
				adaptForUser(user);
			}
		});
	}

	private void adaptForUser(IUser user) {
		coverageDetailComposite.setUser(user);
		coverageDetailComposite.reloadBillingSystemsMenu();
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout());
		coverageDetailComposite = new CoverageDetailComposite(parent);
		coverageDetailComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		coverageDetailComposite.setUnlocked(false);
	}

	@Focus
	public void setFocus() {
		coverageDetailComposite.setFocus();
	}

	private void releaseAndRefreshLock(ICoverage object, String commandId) {
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
//		commandService.refreshElements(commandId, null);
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

}