/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.command;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.ui.contacts.Activator;

public class OpenStickerPreferencePage extends AbstractHandler {
	
	public static final String ID =
		"at.medevit.elexis.contacts.core.command.OpenStickerPreferencePage";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		
		Command cmd = commandService.getCommand("org.eclipse.ui.window.preferences");
		try {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("preferencePageId", "ch.elexis.prefs.sticker");
			ExecutionEvent ev = new ExecutionEvent(cmd, hm, null, null);
			cmd.executeWithChecks(ev);
		} catch (Exception exception) {
			Status status =
				new Status(IStatus.WARNING, Activator.PLUGIN_ID,
					"Error opening sticker preference page");
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		}
		return null;
	}
	
}
