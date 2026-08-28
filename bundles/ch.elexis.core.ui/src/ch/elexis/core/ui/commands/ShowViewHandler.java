package ch.elexis.core.ui.commands;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.views.DeprecatedViews;
import jakarta.inject.Inject;

public class ShowViewHandler extends AbstractHandler {

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	@Inject
	private MApplication mApplication;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		CoreUiUtil.injectServices(this);

		updateDescriptors();
		callE4();
		return null;
	}

	private void callE4() {
		ParameterizedCommand command = commandService.createCommand("ch.elexis.core.ui.e4.command.showview", //$NON-NLS-1$
				null);
		if (command != null) {
			handlerService.executeHandler(command);
		} else {
			LoggerFactory.getLogger(getClass()).error("Command not found"); //$NON-NLS-1$
		}
	}

	private void updateDescriptors() {
		List<String> removeDescriptors = DeprecatedViews.getHiddenViewIds();
		ArrayList<MPartDescriptor> copy = new ArrayList<>(mApplication.getDescriptors());
		for (MPartDescriptor descriptor : copy) {
			if (removeDescriptors.contains(DeprecatedViews.normalizeId(descriptor.getElementId()))) {
				mApplication.getDescriptors().remove(descriptor);
				LoggerFactory.getLogger(getClass()).info("model part descriptor: " + descriptor + " removed"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
