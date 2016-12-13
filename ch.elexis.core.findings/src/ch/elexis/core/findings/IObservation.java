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
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

public interface IObservation extends IFinding {
	public enum ObservationCategory {
		SOCIALHISTORY, VITALSIGNS, IMAGING, LABORATORY, PROCEDURE, SURVEY, EXAM, THERAPY;

		public String getLocalized() {
			try {
				String localized = ResourceBundle.getBundle("ch.elexis.core.findings.messages")
						.getString(this.getClass().getSimpleName() + "_" + this.name());
				return localized;
			} catch (MissingResourceException e) {
				return this.toString();
			}
		}
	}

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
	 * Update the {@link IEncounter} referenced. Also updates the encounterId
	 * with the value of the {@link IEncounter}.
	 * 
	 * @param encounter
	 */
	public void setEncounter(IEncounter encounter);

	/**
	 * Get the effective date and time of the observation.
	 * 
	 * @return
	 */
	public Optional<LocalDateTime> getEffectiveTime();

	/**
	 * Set the effective date and time of the observation.
	 * 
	 * @param time
	 */
	public void setEffectiveTime(LocalDateTime time);

	/**
	 * Get the category of the observation.
	 * 
	 * @return
	 */
	public ObservationCategory getCategory();

	/**
	 * Set the category of the observation.
	 * 
	 * @param category
	 */
	public void setCategory(ObservationCategory category);

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
}
