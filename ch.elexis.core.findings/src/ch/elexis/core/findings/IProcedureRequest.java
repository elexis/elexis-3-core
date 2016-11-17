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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IProcedureRequest extends IFinding {
	/**
	 * Get the {@link IEncounter} referenced.
	 * 
	 * @return
	 */
	public Optional<IEncounter> getEncounter();
	
	/**
	 * Update the {@link IEncounter} referenced. Also updates the encounterId
	 * with the value of the {@link IEncounter}.
	 * 
	 * @param encounter
	 */
	public void setEncounter(IEncounter encounter);

	/**
	 * Get the coding of the {@link ICondition}.
	 * 
	 * @return
	 */
	public List<ICoding> getCoding();

	/**
	 * Set the coding of the {@link ICondition}.
	 * 
	 * @return
	 */
	public void setCoding(List<ICoding> coding);

	/**
	 * Get the start date and time of the encounter.
	 * 
	 * @return
	 */
	public Optional<LocalDateTime> getScheduledTime();

	/**
	 * Set the start date and time of the encounter.
	 * 
	 * @param time
	 */
	public void setScheduledTime(LocalDateTime time);

}
