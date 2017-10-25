package ch.elexis.core.findings.fhir.po.migrator.strategy;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.fhir.po.migrator.messwert.MesswertFieldMapping;
import ch.elexis.data.Patient;

public class TextMigration extends AbstractMigrationStrategy implements IMigrationStrategy {
	
	private static Logger logger = LoggerFactory.getLogger(NumericMigration.class);
	
	private MesswertFieldMapping mapping;
	private Messwert messwert;
	
	public TextMigration(MesswertFieldMapping mapping, Messwert messwert){
		this.mapping = mapping;
		this.messwert = messwert;
	}
	
	@Override
	public Optional<IObservation> migrate(){
		try {
			IObservation observation = (IObservation) templateService
				.createFinding(Patient.load(messwert.get(Messwert.FLD_PATIENT_ID)), template);
			
			observation.setStringValue(getValue(messwert.getResult(mapping.getLocalBefundField())));
			
			return Optional.of(observation);
		} catch (ElexisException e) {
			logger.error("Error creating observation", e);
		}
		return Optional.empty();
	}
	
	public static String getValue(String result){
		return result;
	}
}
