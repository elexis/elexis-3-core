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

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.ui.dialogs.ArtikelDetailDialog;

public class EditEigenartikelUi extends AbstractHandler {
	
	public static final String COMMANDID = "ch.elexis.eigenartikel.edit"; //$NON-NLS-1$
	public static final String PARAMETERID = "ch.elexis.eigenartikel.edit.selected"; //$NON-NLS-1$
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			// get the parameter
			String param = event.getParameter(PARAMETERID);
			PersistentObject artikel =
				(PersistentObject) event.getCommand().getParameterType(PARAMETERID)
					.getValueConverter().convertToObject(param);
			// create and open the dialog with the parameter
			Shell parent = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
			ArtikelDetailDialog dialog = new ArtikelDetailDialog(parent, artikel);
			dialog.open();
		} catch (Exception ex) {
			throw new RuntimeException(COMMANDID, ex);
		}
		return null;
	}
	
	public static void executeWithParams(PersistentObject parameter){
		try {
			// get the command
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ICommandService cmdService = (ICommandService) window.getService(ICommandService.class);
			Command cmd = cmdService.getCommand(EditEigenartikelUi.COMMANDID);
			// create the parameter
			HashMap<String, Object> param = new HashMap<String, Object>();
			param.put(EditEigenartikelUi.PARAMETERID, parameter);
			// build the parameterized command
			ParameterizedCommand pc = ParameterizedCommand.generateCommand(cmd, param);
			// execute the command
			IHandlerService handlerService =
				(IHandlerService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(IHandlerService.class);
			handlerService.executeCommand(pc, null);
		} catch (Exception ex) {
			throw new RuntimeException(EditEigenleistungUi.COMMANDID, ex);
		}
	}
}
