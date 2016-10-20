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

import java.util.List;
import java.util.Optional;

/**
 * Main Interface to load, find and manipulate {@link IFinding} instances.
 * 
 * @author thomas
 *
 */
public interface IFindingsService {

	/**
	 * Find {@link IFinding} instances referring to the patientId. With the filter parameter the
	 * type of the {@link IFinding} instances that are looked up can be limited.
	 * 
	 * @param patientId
	 * @param filter
	 * @return
	 */
	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter);

	/**
	 * Find {@link IFinding} instances referring to the consultationId. With the filter parameter
	 * the type of the {@link IFinding} instances that are looked up can be limited.
	 * 
	 * @param patientId
	 * @param filter
	 * @return
	 */
	public List<IFinding> getConsultationsFindings(String consultationId, Class<? extends IFinding> filter);

	/**
	 * Save the {@link IFinding} instance to a persistent state.
	 * 
	 * @param finding
	 */
	public void saveFinding(IFinding finding);

	/**
	 * Delete the {@link IFinding} instance from persistent state.
	 * 
	 * @param finding
	 */
	public void deleteFinding(IFinding finding);

	/**
	 * Get a {@link IFindingsFactory} that can be used to create implementations of
	 * {@link IFinding}.
	 * 
	 * @return
	 */
	public IFindingsFactory getFindingsFactory();

	/**
	 * Try to load an {@link IFinding} instance by its id.
	 * 
	 * @param idPart
	 * @return
	 */
	public Optional<IFinding> findById(String idPart);

	/**
	 * Set if the service should try to create or update Findings from existing
	 * information. Existing information are Finding specific e.g.
	 * {@link IEncounter} will search for {@link Behandung}.<br />
	 * 
	 * If set to true, find operations will run the IFinding and Service
	 * specific create or update code.<br />
	 * 
	 * Default createOrUpdate is false.
	 * 
	 * @param value
	 */
	public void setCreateOrUpdate(boolean value);

	/**
	 * Get the current createOrUpdate state.
	 * 
	 * @return
	 */
	public boolean getCreateOrUpdate();
}
