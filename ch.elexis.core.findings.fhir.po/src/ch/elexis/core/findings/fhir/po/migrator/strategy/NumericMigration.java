package ch.elexis.core.findings.fhir.po.migrator.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.fhir.po.migrator.messwert.MesswertFieldMapping;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.data.Patient;

public class NumericMigration extends AbstractMigrationStrategy implements IMigrationStrategy {
	
	private static Logger logger = LoggerFactory.getLogger(NumericMigration.class);
	
	private MesswertFieldMapping mapping;
	private Messwert messwert;
	
	public NumericMigration(MesswertFieldMapping mapping, Messwert messwert){
		this.mapping = mapping;
		this.messwert = messwert;
	}
	
	@Override
	public Optional<IObservation> migrate(){
		try {
			IObservation observation = (IObservation) templateService
				.createFinding(Patient.load(messwert.get(Messwert.FLD_PATIENT_ID)), template);
			
			String result = messwert.getResult(mapping.getLocalBefundField());
			observation.setNumericValue(getValue(result),
				((InputDataNumeric) template.getInputData()).getUnit());
			
			String comment = getComment(result);
			if (comment != null && !comment.isEmpty()) {
				observation.setComment(comment);
			}
			return Optional.of(observation);
		} catch (ElexisException e) {
			logger.error("Error creating observation", e);
		}
		return Optional.empty();
	}
	
	/**
	 * Get the first numeric value.
	 * 
	 * @param result
	 * @return
	 */
	public static BigDecimal getValue(String result){
		StringBuilder sb = new StringBuilder();
		for (char c : result.toCharArray()) {
			if (Character.isDigit(c) || c == '.' || c == ',') {
				sb.append(c);
			} else {
				break;
			}
		}
		if (sb.length() > 0) {
			String value = sb.toString().replaceAll(",", ".");
			if (value.startsWith(".")) {
				value = "0" + value;
			}
			if (value.endsWith(".")) {
				value = value + "0";
			}
			try {
				return new BigDecimal(value);
			} catch (NumberFormatException ne) {
				logger
					.error("Could not parse numeric result [" + result + "] value [" + value + "]");
			}
		}
		return null;
	}
	
	/**
	 * Get non numeric text at the end of the String.
	 * 
	 * @param result
	 * @return
	 */
	public static String getComment(String result){
		StringBuilder sb = new StringBuilder();
		char[] charArray = result.toCharArray();
		for (int i = charArray.length - 1; i > -1; i--) {
			char c = charArray[i];
			if (!Character.isDigit(c)) {
				sb.append(c);
			} else {
				break;
			}
		}
		if (sb.length() > 0) {
			sb = sb.reverse();
			return sb.toString();
		}
		return null;
	}
	
	/**
	 * Get a list of numeric values.
	 * 
	 * @param result
	 * @return
	 */
	public static List<BigDecimal> getValues(String result){
		List<BigDecimal> ret = new ArrayList<>();
		
		List<String> parts = new ArrayList<>();
		String[] spacesSplits = result.split(" ");
		for (String spacesSplit : spacesSplits) {
			String[] slashSplits = spacesSplit.split("\\/");
			for (String slashSplit : slashSplits) {
				parts.add(slashSplit);
			}
		}
		
		for (String string : parts) {
			BigDecimal value = getValue(string);
			if (value != null) {
				ret.add(value);
			}
		}
		return ret;
	}
}
