package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Date;
import java.util.Set;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.Identifiable;

public interface IdentifiableDomainResourceAttributeMapper<T extends Identifiable, U extends DomainResource> {

	default void mapMetaData(Identifiable source, DomainResource target) {
		Meta meta = new Meta();
		meta.setLastUpdated(new Date(source.getLastupdate()));
		target.setMeta(meta);
	}

	default void mapNarrative(Identifiable source, DomainResource target) {
		Narrative narrative = new Narrative();
		narrative.setStatus(NarrativeStatus.GENERATED);
		try {
			narrative.setDivAsString(source.getLabel());
		} catch (Exception ex) {
			LoggerFactory.getLogger(getClass()).warn("Error setting narrative {} {}: {}", source.getClass(),
					source.getId(), ex.getMessage());
		}
		target.setText(narrative);
	}

	default Identifier getElexisObjectIdentifier(Identifiable dbObject) {
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_OBJID.getSystem());
		identifier.setValue(dbObject.getId());
		return identifier;
	}

	abstract void elexisToFhir(T source, U target, SummaryEnum summaryEnum, Set<Include> includes);

	abstract void fhirToElexis(U source, T target);

}
