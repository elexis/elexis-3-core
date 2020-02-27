package ch.elexis.core.findings.util.fhir.transformer.mapper;

import java.util.Date;
import java.util.Set;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
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
		narrative.setDivAsString(source.getLabel());
		target.setText(narrative);
	}
	
	abstract void elexisToFhir(T source, U target, SummaryEnum summaryEnum, Set<Include> includes);
	
	abstract void fhirToElexis(U source, T target);
	
}
