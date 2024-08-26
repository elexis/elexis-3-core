package ch.elexis.core.ui.laboratory.commands;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.laboratory.dialogs.EditLabItem;

public class EditLabItemUi extends AbstractHandler {

	public static final String COMMANDID = "ch.elexis.labitem.edit"; //$NON-NLS-1$
	public static final String PARAMETERID = "ch.elexis.labitem.edit.selected"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// get the parameter
			String param = event.getParameter(PARAMETERID);
			Optional<Identifiable> labitem = StoreToStringServiceHolder.get().loadFromString(param);
			if (labitem.isPresent() && labitem.get() instanceof ILabItem) {
				// create and open the dialog with the parameter
				Shell parent = HandlerUtil.getActiveShell(event);
				EditLabItem dialog = new EditLabItem(parent, (ILabItem) labitem.get());
				dialog.open();
			}
		} catch (Exception ex) {
			throw new RuntimeException(COMMANDID, ex);
		}
		return null;
	}

	public static void executeWithParams(Identifiable identifiable) {
		try {
			// get the command
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ICommandService cmdService = window.getService(ICommandService.class);
			Command cmd = cmdService.getCommand(COMMANDID);
			// create the parameter
			HashMap<String, Object> param = new HashMap<>();
			param.put(PARAMETERID, StoreToStringServiceHolder.get().storeToString(identifiable).orElse(null));
			// build the parameterized command
			ParameterizedCommand pc = ParameterizedCommand.generateCommand(cmd, param);
			// execute the command
			IHandlerService handlerService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(IHandlerService.class);
			handlerService.executeCommand(pc, null);
		} catch (Exception ex) {
			throw new RuntimeException(COMMANDID, ex);
		}
	}

}
