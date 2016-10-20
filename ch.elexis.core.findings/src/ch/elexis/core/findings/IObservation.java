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

public interface IObservation extends IFinding {
	public List<IObservation> getSourceObservations();
	
	public void addSourceObservation(IObservation source);
	
	public List<IObservation> getTargetObseravtions();
	
	public void addTargetObservation(IObservation source);
	
	/**
	 * Get the {@link IEncounter} referenced.
	 * 
	 * @return
	 */
	public Optional<IEncounter> getEncounter();
	
	/**
	 * Update the {@link IEncounter} referenced. Also updates the patientId with the value of the
	 * {@link IEncounter}.
	 * 
	 * @param encounter
	 */
	public void setEncounter(IEncounter encounter);
}
