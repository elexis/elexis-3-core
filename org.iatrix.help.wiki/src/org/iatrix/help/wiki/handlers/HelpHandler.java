/*******************************************************************************
 * Copyright (c) 2008, D. Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    D. Lutz - initial implementation
 *    
 *******************************************************************************/

package org.iatrix.help.wiki.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.iatrix.help.wiki.views.WikiView;

import ch.elexis.Hub;
import ch.rgw.tools.ExHandler;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class HelpHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public HelpHandler(){}
	
	/**
	 * the command has been executed, so extract extract the needed information from the application
	 * context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		// we use the view's class name as context id
		String contextId = null;
		IWorkbenchPage activePage = window.getActivePage();
		if (activePage != null) {
			IWorkbenchPart activePart = activePage.getActivePart();
			if (activePart != null) {
				contextId = activePart.getClass().getName();
			}
		}
		
		// TODO DEBUG
		System.out.println("DEBUG: " + contextId);
		
		if (contextId != null) {
			// activate view
			try {
				IViewPart view =
					Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(WikiView.ID);
				if (view != null && view instanceof WikiView) {
					WikiView wikiView = (WikiView) view;
					wikiView.setPage(contextId);
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
		
		return null;
	}
}
