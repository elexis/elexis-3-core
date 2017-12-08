/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.events;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.ISourceProviderService;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.commands.sourceprovider.PatientSelectionStatus;
import ch.elexis.core.ui.locks.ToggleCurrentPatientLockHandler;
import ch.elexis.data.Patient;

/**
 * Listener for patient events, registered within
 * {@link Hub#start(org.osgi.framework.BundleContext)}, de-registered within
 * {@link Hub#stop(org.osgi.framework.BundleContext)}
 */
public class UiPatientEventListener extends ElexisUiEventListenerImpl {
	
	private static ISourceProviderService sps = null;
	
	private ICommandService commandService;
	
	public UiPatientEventListener(){
		super(Patient.class);
	}
	
	@Override
	public void runInUi(final ElexisEvent ev){
		Patient pat = (Patient) ev.getObject();
		Hub.setWindowText(pat);
		
		if (sps == null) {
			sps =
				(ISourceProviderService) PlatformUI.getWorkbench().getService(
					ISourceProviderService.class);
		}
		if(commandService==null) {
			commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(ICommandService.class);
		}	
		
		commandService.refreshElements(ToggleCurrentPatientLockHandler.COMMAND_ID, null);
		
		PatientSelectionStatus provider =
			(PatientSelectionStatus) sps.getSourceProvider(PatientSelectionStatus.PATIENTACTIVE);
		if (provider == null) {
			return;
		}
		
		provider.setState(pat != null);
	}
	
}
