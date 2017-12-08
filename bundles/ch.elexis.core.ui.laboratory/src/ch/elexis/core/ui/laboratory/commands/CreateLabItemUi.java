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
package ch.elexis.core.ui.laboratory.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.laboratory.dialogs.EditLabItem;

public class CreateLabItemUi extends AbstractHandler {
	public static final String COMMANDID = "ch.elexis.labitem.create"; //$NON-NLS-1$
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			// create and open the dialog
			Shell parent = HandlerUtil.getActiveShell(event);
			EditLabItem dialog = new EditLabItem(parent, null);
			dialog.open();
		} catch (Exception ex) {
			throw new RuntimeException(COMMANDID, ex);
		}
		return null;
	}
}
