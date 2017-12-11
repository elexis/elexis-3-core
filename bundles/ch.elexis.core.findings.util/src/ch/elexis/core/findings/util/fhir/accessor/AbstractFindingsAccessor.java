package ch.elexis.core.findings.util.fhir.accessor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.StringType;

import ch.elexis.core.findings.util.ModelUtil;

public abstract class AbstractFindingsAccessor {
	
	public Optional<String> getText(DomainResource resource){
		Narrative narrative = ((DomainResource) resource).getText();
		if (narrative != null && narrative.getDivAsString() != null) {
			return ModelUtil.getNarrativeAsString(narrative);
		}
		return Optional.empty();
	}
	
	public void setText(DomainResource resource, String text){
		Narrative narrative = resource.getText();
		if (narrative == null) {
			narrative = new Narrative();
		}
		ModelUtil.setNarrativeFromString(narrative, text);
		resource.setText(narrative);
	}
	
	public void addStringExtension(DomainResource resource, String theUrl, String theValue){
		Extension extension = new Extension(theUrl);
		extension.setValue(new StringType().setValue(theValue));
		resource.addExtension(extension);
	}
	
	public Map<String, String> getStringExtensions(DomainResource resource){
		List<Extension> extensions = resource.getExtension();
		return extensions.stream().filter(extension -> extension.getValue() instanceof StringType)
			.collect(Collectors.toMap(extension -> extension.getUrl(),
				extension -> ((StringType) extension.getValue()).getValueAsString()));
	}
	
	/**
	 * Method to convert form {@link LocalDateTime} to {@link Date}
	 * 
	 * @param localDateTime
	 * @return
	 */
	protected Date getDate(LocalDateTime localDateTime){
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	/**
	 * Method to convert form {@link LocalDateTime} to {@link Date}
	 * 
	 * @param localDateTime
	 * @return
	 */
	protected Date getDate(LocalDate localDate){
		ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	/**
	 * Method to convert form {@link Date} to {@link LocalDateTime}
	 * 
	 * @param localDateTime
	 * @return
	 */
	protected LocalDateTime getLocalDateTime(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	/**
	 * Method to convert form {@link Date} to {@link LocalDateTime}
	 * 
	 * @param localDateTime
	 * @return
	 */
	protected LocalDate getLocalDate(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalDate();
	}
}
