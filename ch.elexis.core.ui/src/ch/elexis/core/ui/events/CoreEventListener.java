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
import org.eclipse.ui.services.ISourceProviderService;

import ch.elexis.core.ui.commands.sourceprovider.PatientSelectionStatus;

/**
 * @since 3.0.0
 */
public class CoreEventListener {
	
	private static ISourceProviderService sps = null;
	
	public CoreEventListener(){
		// Listen to patient selection and de-selection event, set status
		// accordingly!!
		
		if (sps == null)
			sps =
				(ISourceProviderService) PlatformUI.getWorkbench().getService(
					ISourceProviderService.class);
		
		((PatientSelectionStatus) sps.getSourceProvider(PatientSelectionStatus.PATIENTACTIVE))
			.setState(true);
		
		((PatientSelectionStatus) sps.getSourceProvider(PatientSelectionStatus.PATIENTACTIVE))
			.setState(false);
		
		// / Handle ElexisStatusEvents!!!
		
	}
	
}
