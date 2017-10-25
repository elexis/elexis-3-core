package ch.elexis.core.findings.fhir.po.migrator.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.fhir.po.migrator.messwert.MesswertFieldMapping;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.data.Patient;

public class ComponentMigration extends AbstractMigrationStrategy implements IMigrationStrategy {
	
	private static Logger logger = LoggerFactory.getLogger(ComponentMigration.class);
	
	private MesswertFieldMapping mapping;
	private Messwert messwert;
	private List<IObservation> createdObservations;
	
	private String componentGrpCode;
	private String componentCode;
	
	public ComponentMigration(MesswertFieldMapping mapping, Messwert messwert,
		List<IObservation> createdObservations){
		this.mapping = mapping;
		this.messwert = messwert;
		this.createdObservations = createdObservations;
		
		String code = mapping.getFindingsCode();
		String[] parts = code.split("\\.");
		if (parts.length == 2) {
			componentGrpCode = parts[0];
			componentCode = parts[1];
		}
	}
	
	@Override
	public Optional<IObservation> migrate(){
		IObservation observation = getOrCreateObservation();
		if (observation != null) {
			boolean valueSet = false;
			List<ObservationComponent> components = observation.getComponents();
			for (ObservationComponent observationComponent : components) {
				if (ModelUtil.isCodeInList(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(),
					componentCode, observationComponent.getCoding())) {
					valueSet = setValue(observationComponent);
					observation.updateComponent(observationComponent);
					break;
				}
			}
			if (valueSet) {
				return Optional.of(observation);
			}
		}
		return Optional.empty();
	}
	
	private boolean setValue(ObservationComponent observationComponent){
		ObservationType type = observationComponent.getTypeFromExtension(ObservationType.class);
		if (type == ObservationType.NUMERIC) {
			BigDecimal value =
				NumericMigration.getValue(messwert.getResult(mapping.getLocalBefundField()));
			observationComponent.setNumericValue(Optional.ofNullable(value));
			return true;
		} else if (type == ObservationType.TEXT) {
			String value =
				TextMigration.getValue(messwert.getResult(mapping.getLocalBefundField()));
			observationComponent.setStringValue(Optional.ofNullable(value));
			return true;
		}
		return false;
	}
	
	private IObservation getOrCreateObservation(){
		// lookup already created group observation
		for (IObservation iObservation : createdObservations) {
			if (ModelUtil.isCodeInList(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(),
				componentGrpCode, iObservation.getCoding())) {
				return iObservation;
			}
		}
		try {
			return (IObservation) templateService
				.createFinding(Patient.load(messwert.get(Messwert.FLD_PATIENT_ID)), template);
		} catch (ElexisException e) {
			logger.error("Error creating observation", e);
		}
		return null;
	}
}
