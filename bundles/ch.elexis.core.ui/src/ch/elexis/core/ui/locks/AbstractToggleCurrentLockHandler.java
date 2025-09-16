package ch.elexis.core.ui.locks;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import jakarta.inject.Inject;

public abstract class AbstractToggleCurrentLockHandler extends AbstractHandler implements IElementUpdater {

	protected ICommandService commandService;

	public abstract String getCommandId();

	public abstract Class<?> getTemplateClass();

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (LockInfo.class.equals(clazz)) {
			if (commandService == null) {
				commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
			}
			commandService.refreshElements(getCommandId(), null);
		}
	}

	@Inject
	@Optional
	public void acquireLock(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) Object object) {
		if (commandService == null) {
			commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		}
		commandService.refreshElements(getCommandId(), null);
	}

	@Inject
	@Optional
	public void releaseLock(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) Object object) {
		if (commandService == null) {
			commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		}
		commandService.refreshElements(getCommandId(), null);
	}

	public AbstractToggleCurrentLockHandler() {
		CoreUiUtil.injectServices(this);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (commandService == null) {
			commandService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(ICommandService.class);
		}

		Object selected = ContextServiceHolder.get().getTyped(getTemplateClass()).orElse(null);
		if (selected == null) {
			commandService.refreshElements(getCommandId(), null);
			return null;
		}

		if (LocalLockServiceHolder.get().isLockedLocal(selected)) {
			LocalLockServiceHolder.get().releaseLock(selected);
		} else {
			LockResponse lr = LocalLockServiceHolder.get().acquireLock(selected);
			if (!lr.isOk()) {
				LockResponseHelper.showInfo(lr, selected, null);
			}
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void updateElement(UIElement element, Map parameters) {
		Object selected = ContextServiceHolder.get().getTyped(getTemplateClass()).orElse(null);
		if (selected == null) {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
			return;
		}

		if (LocalLockServiceHolder.get().isLockedLocal(selected)) {
			element.setIcon(Images.IMG_LOCK_OPEN.getImageDescriptor());
			element.setChecked(true);
		} else {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
		}
	}

}
