/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, Daniel Lutz and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel Lutz - initial implementation, based on RechnungsDrucker
 * 
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.views.TemplatePrintView;
import ch.elexis.data.Patient;
import ch.rgw.tools.StringTool;

public class TemplateDrucker {
	TemplatePrintView tpw;
	IWorkbenchPage page;
	// IProgressMonitor monitor;
	Patient patient;
	String template;
	String printer;
	String tray;
	
	public TemplateDrucker(String template, String printer, String tray){
		this.template = template;
		this.printer = null;
		this.tray = null;
		
		if (!StringTool.isNothing(printer)) {
			this.printer = printer;
		}
		if (!StringTool.isNothing(tray)) {
			this.tray = tray;
		}
	}
	
	public void doPrint(Patient pat){
		this.patient = pat;
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		
		try {
			tpw = (TemplatePrintView) page.showView(TemplatePrintView.ID);
			progressService.runInUI(PlatformUI.getWorkbench().getProgressService(),
				new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor){
						monitor.beginTask(Messages.TemplateDrucker_printing + template + "...", 1); //$NON-NLS-1$
						
						Patient actPatient =
							(Patient) ElexisEventDispatcher.getSelected(Patient.class);
						if (tpw.doPrint(actPatient, template, printer, tray, monitor) == false) {
							Status status =
								new Status(Status.ERROR, "ch.elexis", Status.ERROR,
									Messages.TemplateDrucker_errorPrinting, null);
							ErrorDialog.openError(null, Messages.TemplateDrucker_errorPrinting,
								Messages.TemplateDrucker_docname + template
									+ Messages.TemplateDrucker_couldntPrint, status); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							
						}
						
						monitor.done();
					}
				}, null);
			
			page.hideView(tpw);
			
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					Messages.TemplateDrucker_errorPrinting + ": "
						+ Messages.TemplateDrucker_couldntOpen, ex);
			StatusManager.getManager().handle(status);
		}
	}
}
