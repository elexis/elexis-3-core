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

import java.util.List;

import org.eclipse.swt.widgets.Display;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.dialogs.ReminderListSelectionDialog;
import ch.elexis.data.Reminder;
import ch.elexis.data.User;

public class UiUserEventListener extends ElexisUiEventListenerImpl {
	
	public UiUserEventListener(){
		super(User.class);
	}
	
	@Override
	public void runInUi(final ElexisEvent ev){
		if (ev.getType() == ElexisEvent.EVENT_SELECTED) {
			final List<Reminder> reminderList = Reminder.findToShowOnStartup(CoreHub.actUser);
			
			if (reminderList.size() > 0) {
				// must be called inside display thread
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run(){
						new ReminderListSelectionDialog(reminderList,
							Messages.ReminderView_importantRemindersOnLogin).open();
					}
				});
			}
		}
	}
	
}
