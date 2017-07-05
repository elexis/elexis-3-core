/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.text;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.core.ui.views.TextView;
import ch.elexis.data.Brief;
import ch.rgw.tools.ExHandler;

public class XrefExtension implements IKonsExtension {
	IRichTextDisplay tx;
	
	public String connect(IRichTextDisplay tf){
		tx = tf;
		return XRefExtensionConstants.providerID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		
		n.background = UiDesk.getColor(UiDesk.COL_LIGHTBLUE);
		n.foreground = UiDesk.getColor(UiDesk.COL_GREY20);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		try {
			if (CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false)) {
				startLocalEdit(Brief.load(refID));
			} else {
				TextView tv = (TextView) Hub.plugin.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(TextView.ID);
				tv.openDocument(Brief.load(refID));
			}
			return true;
		} catch (PartInitException e) {
			ExHandler.handle(e);
		}
		return false;
	}
	
	private void startLocalEdit(Brief brief){
		if (brief != null) {
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			Command command =
				commandService.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"); //$NON-NLS-1$
			IEclipseContext iEclipseContext =
				PlatformUI.getWorkbench().getService(IEclipseContext.class);
			iEclipseContext.set(command.getId(), new StructuredSelection(brief));
			ExecutionEvent event =
				new ExecutionEvent(command, Collections.EMPTY_MAP, this, iEclipseContext);
			try {
				command.executeWithChecks(event);
			} catch (ExecutionException | NotDefinedException | NotEnabledException
					| NotHandledException e) {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
					Messages.BriefAuswahl_errorttile,
					Messages.BriefAuswahl_erroreditmessage);
			}
		}
	}
	
	public IAction[] getActions(){
		// TODO Auto-generated method stub
		return null;
	}
	
	public void removeXRef(String refProvider, String refID){
		// TODO Auto-generated method stub
		
	}
	
	public void setInitializationData(IConfigurationElement config, String propertyName,
		Object data) throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	public void insert(Object o, int pos){
		
	}
	
}
