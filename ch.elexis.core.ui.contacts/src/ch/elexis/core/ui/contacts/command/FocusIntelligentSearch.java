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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.ui.contacts.Activator;
import ch.elexis.core.ui.contacts.views.ContactSelectorView;


/**
 * Focus the intelligent search text field. This command exists for the purpose
 * of getting called via a keybdinding.
 */
public class FocusIntelligentSearch extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(ContactSelectorView.ID);
		} catch (PartInitException e) {
			Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Error activating intelligent search view");
			StatusManager.getManager().handle(status, StatusManager.SHOW);
		}
		return null;
	}

}
