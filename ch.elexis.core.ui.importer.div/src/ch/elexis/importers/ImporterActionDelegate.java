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
package ch.elexis.importers;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ImporterActionDelegate implements IWorkbenchWindowActionDelegate {
	
	IWorkbenchWindow myWindow;
	
	public void dispose(){
		// TODO Auto-generated method stub
		
	}
	
	public void init(final IWorkbenchWindow window){
		myWindow = window;
	}
	
	public void run(final IAction action){
		// KontaktImporterDialog kib=new KontaktImporterDialog(myWindow.getShell());
		// kib.open();
		
	}
	
	public void selectionChanged(final IAction action, final ISelection selection){
		// TODO Auto-generated method stub
		
	}
	
}
