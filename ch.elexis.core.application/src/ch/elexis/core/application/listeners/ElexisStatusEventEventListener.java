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
package ch.elexis.core.application.listeners;

import org.eclipse.jface.dialogs.MessageDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.UiDesk;

/**
 * Listens to Elexis status events of highest priority, such as exception or
 * operational error messages. Depending on the situation these may or may not
 * be presented to the user.
 */
public class ElexisStatusEventEventListener extends ElexisEventListenerImpl {

	private Logger log = LoggerFactory
			.getLogger(ElexisStatusEventEventListener.class.getName());

	public ElexisStatusEventEventListener() {
		super(null, ElexisStatus.class, ElexisEvent.EVENT_ELEXIS_STATUS,
				ElexisEvent.PRIORITY_SYNC);
	}

	@Override
	public void run(ElexisEvent ev) {
		ElexisStatus es = (ElexisStatus) ev.getGenericObject();
		log.info("StatusEvent [PLUGIN] " + es.getPlugin() + " [MESSAGE] "
				+ es.getMessage() + " [EXCEPTION] " + es.getException());
		MessageDialog.openError(UiDesk.getTopShell(), "Elexis Status",
				es.getMessage() + " " + es.getException());
	}
}
