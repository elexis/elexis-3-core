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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFinding {
	
	public enum RawContentFormat {
		FHIR_JSON, FHIR_XML
	}

	/**
	 * Get a unique ID for the finding. Usually the ID from the database.
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Get the patient this finding is referring to.
	 * 
	 * @return
	 */
	public String getPatientId();
	
	/**
	 * Set the patient this finding is referring to.
	 * 
	 */
	public void setPatientId(String patientId);
	
	public List<ICoding> getCoding();
	
	public void setCoding(List<ICoding> coding);
	
	public Optional<String> getText();
	
	public void setText(String text);
	
	public void addStringExtension(String theUrl, String theValue);
	
	public Map<String, String> getStringExtensions();
	
	public RawContentFormat getRawContentFormat();

	public String getRawContent();

	public void setRawContent(String content);

	/**
	 * Default method to convert form {@link LocalDateTime} to {@link Date}
	 * 
	 * @param localDateTime
	 * @return
	 */
	default Date getDate(LocalDateTime localDateTime){
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	/**
	 * Default method to convert form {@link LocalDateTime} to {@link Date}
	 * 
	 * @param localDateTime
	 * @return
	 */
	default Date getDate(LocalDate localDate){
		ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	/**
	 * Default method to convert form {@link Date} to {@link LocalDateTime}
	 * 
	 * @param localDateTime
	 * @return
	 */
	default LocalDateTime getLocalDateTime(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	/**
	 * Default method to convert form {@link Date} to {@link LocalDateTime}
	 * 
	 * @param localDateTime
	 * @return
	 */
	default LocalDate getLocalDate(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}
}
