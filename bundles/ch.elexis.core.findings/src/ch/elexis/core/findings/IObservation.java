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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

import ch.elexis.core.findings.IObservationLink.ObservationLinkType;

public interface IObservation extends IFinding {
	public enum ObservationCategory {
			SOCIALHISTORY("social-history"), VITALSIGNS("vital-signs"), IMAGING("imaging"),
			LABORATORY("laboratory"), PROCEDURE("procedure"), SURVEY("survey"), EXAM("exam"),
			THERAPY("therapy"), SOAP_SUBJECTIVE("subjective"), SOAP_OBJECTIVE("objective"),
			UNKNOWN("unknown");
		
		private String code;
		
		private ObservationCategory(String code){
			this.code = code;
		}
		
		public String getCode(){
			return code;
		}
		
		public String getLocalized(){
			try {
				String localized = ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(this.getClass().getSimpleName() + "_" + this.name());
				return localized;
			} catch (MissingResourceException e) {
				return this.toString();
			}
		}
	}
	
	public enum ObservationType {
			COMP, REF, NUMERIC, TEXT
	}
	
	public enum ObservationCode {
			ANAM_PERSONAL(IdentifierSystem.ELEXIS_ANAMNESE, "personal"),
			ANAM_RISK(IdentifierSystem.ELEXIS_ANAMNESE, "risk");
		
		private String code;
		private IdentifierSystem identifierSystem;
		
		private ObservationCode(IdentifierSystem identifierSystem, String code){
			this.code = code;
			this.identifierSystem = identifierSystem;
		}
		
		public String getCode(){
			return code;
		}
		
		public IdentifierSystem getIdentifierSystem(){
			return identifierSystem;
		}
		
		public boolean isSame(ICoding iCoding){
			return code != null && code.equals(iCoding.getCode()) && identifierSystem != null
				&& identifierSystem.getSystem() != null
				&& identifierSystem.getSystem().equals(iCoding.getSystem());
		}
	}
	
	/**
	 * Get all {@link IObservation}s which have this {@link IObservation} as target.
	 * 
	 * @param type
	 * @return
	 */
	public List<IObservation> getSourceObservations(ObservationLinkType type);
	
	/**
	 * Add a {@link IObservationLink} to the source {@link IObservation}.
	 * 
	 * @param source
	 * @param type
	 */
	public void addSourceObservation(IObservation source, ObservationLinkType type);
	
	/**
	 * Remove the {@link IObservationLink} to the source {@link IObservation}.
	 * 
	 * @param source
	 * @param type
	 */
	public void removeSourceObservation(IObservation source, ObservationLinkType type);
	
	/**
	 * Get all {@link IObservation}s which have this {@link IObservation} as source.
	 * 
	 * @param type
	 * @return
	 */
	public List<IObservation> getTargetObseravtions(ObservationLinkType type);
	
	/**
	 * Add a {@link IObservationLink} to the target {@link IObservation}.
	 * 
	 * @param target
	 * @param type
	 */
	public void addTargetObservation(IObservation target, ObservationLinkType type);
	
	/**
	 * Remove the {@link IObservationLink} to the target {@link IObservation}.
	 * 
	 * @param target
	 * @param type
	 */
	public void removeTargetObservation(IObservation target, ObservationLinkType type);
	
	/**
	 * Adds a component to the fhir object
	 * 
	 * @param component
	 */
	public void addComponent(ObservationComponent component);
	
	/**
	 * Updates a component of the fhir object
	 * 
	 * @param component
	 */
	public void updateComponent(ObservationComponent component);
	
	/**
	 * Returns all components from the fhir object
	 * 
	 * @return
	 */
	public List<ObservationComponent> getComponents();
	
	/**
	 * Get the {@link IEncounter} referenced.
	 * 
	 * @return
	 */
	public Optional<IEncounter> getEncounter();
	
	/**
	 * Update the {@link IEncounter} referenced. Also updates the encounterId with the value of the
	 * {@link IEncounter}.
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
	
	/**
	 * Set the quantity of the observation.
	 * 
	 * @return
	 */
	public void setNumericValue(BigDecimal bigDecimal, String unit);
	
	/**
	 * Get the value as numeric
	 * 
	 * @return
	 */
	public Optional<BigDecimal> getNumericValue();
	
	/**
	 * Set the stringValue of the observation.
	 * 
	 * @return
	 */
	public void setStringValue(String value);
	
	/**
	 * Get the stringValue
	 * 
	 * @return
	 */
	public Optional<String> getStringValue();
	
	/**
	 * Get the Unit
	 * 
	 * @return
	 */
	public Optional<String> getNumericValueUnit();
	
	/**
	 * Sets a custom type for a observation
	 * 
	 * @param observationType
	 */
	public void setObservationType(ObservationType observationType);
	
	/**
	 * Returns the type for this observation
	 * 
	 * @return
	 */
	public ObservationType getObservationType();
	
	/**
	 * Checks if this observation is inside a reference
	 * 
	 * @return
	 */
	public boolean isReferenced();
	
	/**
	 * Marks this observation with a referenced flag
	 * 
	 * @param referenced
	 */
	public void setReferenced(boolean referenced);
	
	/**
	 * Sets comments about the result
	 * 
	 * @param comment
	 */
	public void setComment(String comment);
	
	/**
	 * Returns the comment of this observation
	 * 
	 * @return
	 */
	public Optional<String> getComment();
	
	/**
	 * Adds or updates a format by key
	 * 
	 * @param key
	 * @param value
	 */
	public void addFormat(String key, String value);
	
	/**
	 * Returns the format by key
	 * 
	 * @param key
	 * @return
	 */
	public String getFormat(String key);
	
	/**
	 * Get the script string. The script can be evaluated to a new value.
	 * 
	 * @return
	 */
	public Optional<String> getScript();
	
	/**
	 * Set the script string. The script will be evaluated to a new value.
	 * 
	 */
	public void setScript(String script);
	
	/**
	 * Get the decimal place that should be used to display the numeric value.
	 * 
	 * @return -1 if not set
	 */
	public int getDecimalPlace();
	
	/**
	 * Set the decimal place that should be used to display the numeric value.
	 * 
	 * @param value
	 */
	public void setDecimalPlace(int value);
	
	/**
	 * Get an URI String describing the origin of the {@link IObservation}. Use {@link UriType} to
	 * determine the type.
	 * 
	 * @return
	 */
	public Optional<String> getOriginUri();
	
	/**
	 * Set an URI String describing the origin of the {@link IObservation}. Use {@link UriType} to
	 * determine the type. Max length are 255 characters.
	 * 
	 * @param uri
	 */
	public void setOriginUri(String uri);
}
