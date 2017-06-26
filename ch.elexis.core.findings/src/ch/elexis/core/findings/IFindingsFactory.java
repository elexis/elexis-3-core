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

/**
 * Main Interface to create new {@link IFinding} instances.
 * 
 * @author thomas
 *
 */
public interface IFindingsFactory {
	
	/**
	 * Create a new persistent {@link IEncounter} instance.
	 * 
	 * @return
	 */
	public IEncounter createEncounter();
	
	/**
	 * Create a new persistent {@link IObservation} instance.
	 * 
	 * @return
	 */
	public IObservation createObservation();
	
	/**
	 * Create a new persistent {@link ICondition} instance.
	 * 
	 * @return
	 */
	public ICondition createCondition();
	
	/**
	 * Create a new persistent {@link IClinicalImpression} instance.
	 * 
	 * @return
	 */
	public IClinicalImpression createClinicalImpression();
	
	/**
	 * Create a new persistent {@link IProcedureRequest} instance.
	 * 
	 * @return
	 */
	public IProcedureRequest createProcedureRequest();
	
	/**
	 * Create a new persistent {@link IFamilyMemberHistory} instance.
	 * 
	 * @return
	 */
	public IFamilyMemberHistory createFamilyMemberHistory();
}
