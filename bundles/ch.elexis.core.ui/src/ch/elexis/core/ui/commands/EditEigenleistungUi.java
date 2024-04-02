/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.commands;

import java.util.HashMap;

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

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.ui.dialogs.EigenLeistungDialog;
import ch.elexis.data.PersistentObject;

public class EditEigenleistungUi extends AbstractHandler {
	public static final String COMMANDID = "ch.elexis.eigenleistung.edit"; //$NON-NLS-1$
	public static final String PARAMETERID = "ch.elexis.eigenleistung.edit.selected"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			// get the parameter
			String param = event.getParameter(PARAMETERID);
			IVerrechenbar verrechenbar = (IVerrechenbar) event.getCommand().getParameterType(PARAMETERID)
					.getValueConverter().convertToObject(param);
			ICustomService customService = NoPoUtil.loadAsIdentifiable((PersistentObject) verrechenbar,
					ICustomService.class).orElse(null);
			// create and open the dialog with the parameter
			Shell parent = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
			EigenLeistungDialog dialog = new EigenLeistungDialog(parent, customService);
			dialog.open();
		} catch (Exception ex) {
			throw new RuntimeException(COMMANDID, ex);
		}
		return null;
	}

	public static void executeWithParams(PersistentObject parameter) {
		try {
			// get the command
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ICommandService cmdService = (ICommandService) window.getService(ICommandService.class);
			Command cmd = cmdService.getCommand(EditEigenleistungUi.COMMANDID);
			// create the parameter
			HashMap<String, Object> param = new HashMap<>();
			param.put(EditEigenleistungUi.PARAMETERID, parameter);
			// build the parameterized command
			ParameterizedCommand pc = ParameterizedCommand.generateCommand(cmd, param);
			// execute the command
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(IHandlerService.class);
			handlerService.executeCommand(pc, null);
		} catch (Exception ex) {
			throw new RuntimeException(EditEigenleistungUi.COMMANDID, ex);
		}
	}
}
