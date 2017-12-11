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
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.data.Patient;
import ch.elexis.data.Reminder;

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
			/**
			 * ch.elexis.core.ui.views.ReminderView#eeli_pat will be called on opposite
			 * Preferences.USR_SHOWPATCHGREMINDER condition.
			 */
			if (CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, false)) {
				List<Reminder> list = Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser,
					false, (Patient) ev.getObject(), true);
				if (list.size() != 0) {
					StringBuilder sb = new StringBuilder();
					for (Reminder r : list) {
						sb.append(r.getSubject()+"\n");
						sb.append(r.getMessage()+"\n\n");
					}
					MessageEvent.fireInformation(Messages.PatientEventListener_0,
						sb.toString());
				}
			}
		}
	}
	
}
