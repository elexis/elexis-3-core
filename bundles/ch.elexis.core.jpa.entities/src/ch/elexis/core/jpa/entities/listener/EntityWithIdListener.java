/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.jpa.entities.listener;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import ch.elexis.core.jpa.entities.EntityWithId;

public class EntityWithIdListener {

	/**
	 * This method is called just before an update on the data set happens. We use
	 * it to always set the lastUpdate field to the correct value.
	 *
	 * @param k
	 */
	@PreUpdate
	public void preUpdate(EntityWithId o) {
		o.setLastupdate(System.currentTimeMillis());
	}

	@PrePersist
	public void prePersist(EntityWithId o) {
		preUpdate(o);
	}
}
