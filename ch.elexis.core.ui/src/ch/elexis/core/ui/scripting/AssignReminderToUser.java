/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.scripting;

import java.util.Collection;

import ch.elexis.data.Anwender;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;

public class AssignReminderToUser {
	
	public String assignAll(String toUsername){
		return run(new Query<Reminder>(Reminder.class).execute(), toUsername);
	}
	
	private String run(Collection<Reminder> workset, String username){
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		Anwender toUser = Anwender.load(qbe.findSingle(Anwender.FLD_LABEL, Query.EQUALS, username));
		if (!toUser.exists()) {
			return username + " kann nicht gefunden werden";
		}
		
		int i = 0;
		if (workset != null && workset.size() > 0) {
			for (Reminder r : workset) {
				r.addResponsible(toUser);
				i++;
			}
		}
		return i + " reminders wurden angepasst.";
		
	}
	
	public String assign(String fromUsername, String toUsername){
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		Anwender user = Anwender.load(qbe.findSingle(Anwender.FLD_LABEL, Query.EQUALS, fromUsername));
		if (user.exists()) {
			return run(user.getReminders(null), toUsername);
		} else {
			return fromUsername + " kann nicht gefunden werden";
		}
	}
	
}
