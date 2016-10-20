/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings;

import java.time.LocalDate;
import java.util.Optional;

public interface ICondition extends IFinding {
	public enum ConditionCategory {
		UNKNOWN, DIAGNOSIS, COMPLAINT
	}

	public enum ConditionStatus {
		UNKNOWN, ACTIVE, RELAPSE, REMISSION, RESOLVED
	}

	/**
	 * Get the condition category.
	 */
	public ConditionCategory getCategory();

	/**
	 * Set the condition category.
	 * 
	 * @param category
	 */
	public void setCategory(ConditionCategory category);

	/**
	 * Get the condition status.
	 * 
	 * @return
	 */
	public ConditionStatus getStatus();

	/**
	 * Set the condition status.
	 * 
	 * @param status
	 */
	public void setStatus(ConditionStatus status);
	
	/**
	 * Set date when first entered.
	 * 
	 * @param date
	 */
	public void setDateRecorded(LocalDate date);
	
	/**
	 * Get the date the {@link ICondition} was first entered.
	 * 
	 * @return
	 */
	public Optional<LocalDate> getDateRecorded();
}
