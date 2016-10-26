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
import java.time.LocalDateTime;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

public interface ICondition extends IFinding {
	public enum ConditionCategory {
			UNKNOWN, DIAGNOSIS, COMPLAINT;
		
		public String getLocalized(){
			try {
				String localized = ResourceBundle.getBundle("ch.elexis.core.findings.messages")
					.getString(this.getClass().getSimpleName() + "_" + this.name());
				return localized;
			} catch (MissingResourceException e) {
				return this.toString();
			}
		}
	}

	public enum ConditionStatus {
			UNKNOWN, ACTIVE, RELAPSE, REMISSION, RESOLVED;
		
		public String getLocalized(){
			try {
				String localized = ResourceBundle.getBundle("ch.elexis.core.findings.messages")
					.getString(this.getClass().getSimpleName() + "_" + this.name());
				return localized;
			} catch (MissingResourceException e) {
				return this.toString();
			}
		}
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
	 * Set date when condition was documented.
	 * 
	 * @param date
	 */
	public void setDateRecorded(LocalDate date);
	
	/**
	 * Get the date the {@link ICondition} was documented.
	 * 
	 * @return
	 */
	public Optional<LocalDate> getDateRecorded();
	
	/**
	 * Set the time when the {@link ICondition} began.
	 * 
	 * @param startTime
	 */
	public void setStartTime(LocalDateTime startTime);
	
	/**
	 * Get the time when the {@link ICondition} began.
	 * 
	 * @return
	 */
	public Optional<LocalDateTime> getStartTime();
	
	/**
	 * Set the time when the {@link ICondition} abated.
	 * 
	 * @param endTime
	 */
	public void setEndTime(LocalDateTime endTime);
	
	/**
	 * Get the time when the {@link ICondition} abated.
	 * 
	 * @return
	 */
	public Optional<LocalDateTime> getEndTime();
	
	/**
	 * Add additional information about the {@link ICondition}.
	 * 
	 * @param text
	 */
	public void addNote(String text);
	
	/**
	 * Remove an additional information about the {@link ICondition}.
	 * 
	 * @param text
	 */
	public void removeNote(String text);
	
	/**
	 * Get additional information about the {@link ICondition}.
	 * 
	 * @return
	 */
	public List<String> getNotes();
}
