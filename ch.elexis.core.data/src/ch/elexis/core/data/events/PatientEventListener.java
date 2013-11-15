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
package ch.elexis.core.data.events;

import java.util.List;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Reminder;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.events.MessageEvent;

/**
 * Listener for patient events, registered within
 * {@link Hub#start(org.osgi.framework.BundleContext)}, de-registered within
 * {@link Hub#stop(org.osgi.framework.BundleContext)}
 */
public class PatientEventListener extends ElexisEventListenerImpl {
	
	public PatientEventListener(){
		super(Patient.class);
	}
	
	@Override
	public void run(final ElexisEvent ev){
		if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
			if (CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, false)) {
				List<Reminder> list =
					Reminder.findRemindersDueFor((Patient) ev.getObject(), CoreHub.actUser, true);
				if (list.size() != 0) {
					StringBuilder sb = new StringBuilder();
					for (Reminder r : list) {
						sb.append(r.getMessage()).append("\n\n"); //$NON-NLS-1$
					}
					MessageEvent.fireInformation("important reminders for this patient",
						sb.toString());
				}
			}
		}
	}
	
}
