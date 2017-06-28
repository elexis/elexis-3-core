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
 * Main Interface to load, find and save {@link IFinding} instances.
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
	public <T extends IFinding> List<T> getPatientsFindings(String patientId,
		Class<T> filter);

	/**
	 * Find {@link IFinding} instances referring to the consultationId. With the filter parameter
	 * the type of the {@link IFinding} instances that are looked up can be limited.
	 * 
	 * @param patientId
	 * @param filter
	 * @return
	 */
	public <T extends IFinding> List<T> getConsultationsFindings(String consultationId,
		Class<T> filter);

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
	 * Factory method to create new {@link IFinding} instances.
	 * 
	 * @param type
	 * @return
	 */
	public <T extends IFinding> T create(Class<T> type);
	
	/**
	 * Try to load an {@link IFinding} instance by its id, using a specific IFinding class for
	 * better performance than {@link IFindingsService#findById(String)}.
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	public <T extends IFinding> Optional<T> findById(String id, Class<T> clazz);
}
