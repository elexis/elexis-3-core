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

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.ui.contacts.dialogs.AdvancedFilterDialog;
import ch.elexis.core.ui.icons.Images;

public class AdvancedFilterCommand extends AbstractHandler implements
		IElementUpdater {

	public static final String ID = "at.medevit.elexis.contacts.core.command.AdvancedFilterCommand";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		AdvancedFilterDialog afd = new AdvancedFilterDialog(PlatformUI
				.getWorkbench().getDisplay().getActiveShell());
		int retVal = afd.open();
		//System.out.println(retVal);
		return null;
	}

	@Override
	public void updateElement(UIElement element,
			@SuppressWarnings("rawtypes") Map parameters) {
		element.setIcon(Images.IMG_FILTER.getImageDescriptor());
	}

}
