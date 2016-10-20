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

public interface IEncounter extends IFinding {
	public String getConsultationId();
	
	public void setConsultationId(String consultationId);
	
	public String getServiceProviderId();
	
	public void setServiceProviderId(String serviceProviderId);
	
	/**
	 * Get all {@link ICondition} entries linked to this encounter. The reason the encounter takes
	 * place.
	 * 
	 * @return the entries, or an empty list
	 */
	public List<ICondition> getIndication();
	
	/**
	 * Set all {@link ICondition} entries linked to this encounter. The reason the encounter takes
	 * place.
	 * 
	 * @param indication
	 */
	public void setIndication(List<ICondition> indication);
	
	Optional<LocalDateTime> getStartTime();
	
	void setStartTime(LocalDateTime time);
}
