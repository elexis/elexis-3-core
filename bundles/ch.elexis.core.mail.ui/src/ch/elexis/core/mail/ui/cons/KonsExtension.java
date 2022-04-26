package ch.elexis.core.mail.ui.cons;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.ui.handlers.EncounterUtil;
import ch.elexis.core.mail.ui.handlers.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.util.IKonsExtension;

public class KonsExtension implements IKonsExtension {
	IRichTextDisplay mine;

	public static final String EXTENSION_ID = "ch.elexis.core.mail.ui.cons";

	@Override
	public String connect(IRichTextDisplay tf) {
		mine = tf;
		return EXTENSION_ID;
	}

	@Override
	public boolean doLayout(StyleRange styleRange, String provider, String id) {
		styleRange.background = CoreUiUtil.getColorForString("a6ffaa");
		return true;
	}

	@Override
	public boolean doXRef(String refProvider, String refID) {
		Optional<ITaskDescriptor> taskDescriptor = TaskServiceHolder.get().findTaskDescriptorByIdOrReferenceId(refID);
		taskDescriptor.ifPresent(descriptor -> {
			// open mail dialog
			// now try to call the send mail task command
			try {
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
				Command sendMailTaskCommand = commandService.getCommand("ch.elexis.core.mail.ui.sendMailTask");

				HashMap<String, String> params = new HashMap<String, String>();
				params.put("ch.elexis.core.mail.ui.sendMailTaskDescriptorId", refID);
				ParameterizedCommand parametrizedCommmand = ParameterizedCommand.generateCommand(sendMailTaskCommand,
						params);
				PlatformUI.getWorkbench().getService(IHandlerService.class).executeCommand(parametrizedCommmand, null);
			} catch (Exception ex) {
				LoggerFactory.getLogger(getClass()).warn("Send mail Task command not available", ex);
			}
		});
		return true;
	}

	@Override
	public String updateXRef(String provider, String refID) {
		Optional<ITaskDescriptor> taskDescriptor = TaskServiceHolder.get().findTaskDescriptorByIdOrReferenceId(refID);
		if (taskDescriptor.isPresent()) {
			return EncounterUtil.getTaskDescriptorText(taskDescriptor.get());
		}
		return null;
	}

	@Override
	public void insert(Object o, int pos) {
		// no d&d ...
	}

	@Override
	public IAction[] getActions() {
		return null;
	}

	@Override
	public void removeXRef(String refProvider, String refID) {
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
	}
}
