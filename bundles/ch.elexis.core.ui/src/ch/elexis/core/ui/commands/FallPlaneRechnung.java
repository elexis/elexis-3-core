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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

import ch.elexis.core.ui.UiDesk;

public class FallPlaneRechnung extends AbstractHandler {
	
	public Object execute(ExecutionEvent arg0) throws ExecutionException{
		InputDialog dlg =
			new InputDialog(UiDesk.getTopShell(), Messages.FallPlaneRechnung_PlanBillingHeading,
				Messages.FallPlaneRechnung_PlanBillingAfterDays, "30", new IInputValidator() { //$NON-NLS-1$
				
					public String isValid(String newText){
						if (newText.matches("[0-9]*")) { //$NON-NLS-1$
							return null;
						}
						return Messages.FallPlaneRechnung_PlanBillingPleaseEnterPositiveInteger;
					}
				});
		if (dlg.open() == Dialog.OK) {
			return dlg.getValue();
		}
		return null;
	}
	
}
