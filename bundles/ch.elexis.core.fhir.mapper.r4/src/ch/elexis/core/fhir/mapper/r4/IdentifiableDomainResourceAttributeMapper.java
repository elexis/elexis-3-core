package ch.elexis.core.fhir.mapper.r4;

import java.util.Date;

import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Narrative.NarrativeStatus;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.model.Identifiable;

/**
 * A type specific mapper between the Elexis model and FHIR R4. A single mapper
 * does not support {@link Include} as this would require to reload a third
 * party element.
 * 
 * @param <T>
 * @param <U>
 */
public class IdentifiableDomainResourceAttributeMapper<T extends Identifiable, U extends DomainResource> {

	final Class<? extends DomainResource> ressourceClazz;

	public IdentifiableDomainResourceAttributeMapper(Class<? extends DomainResource> ressourceClazz) {
		this.ressourceClazz = ressourceClazz;
	}

	public final void elexisToFhir(T source, U target, SummaryEnum summaryEnum) {
		mapIdAndMeta(ressourceClazz, target, source);

		if (SummaryEnum.DATA != summaryEnum) {
			mapNarrative(source, target);
		}
		if (SummaryEnum.TEXT == summaryEnum || SummaryEnum.COUNT == summaryEnum) {
			return;
		}
		fullElexisToFhir(source, target, summaryEnum);
	}

	public void fullElexisToFhir(T source, U target, SummaryEnum summaryEnum) {
	}

	public final void fhirToElexis(U source, T target) {
		fullFhirToElexis(source, target);
	}

	public void fullFhirToElexis(U source, T target) {
	}

	public void mapIdAndMeta(Class<?> resourceClass, DomainResource domainResource, Identifiable localObject) {
		IdDt idDt = new IdDt(resourceClass.getSimpleName(), localObject.getId(),
				Long.toString(localObject.getLastupdate()));
		domainResource.setId(idDt);

		Meta meta = new Meta();
		meta.setLastUpdated(new Date(localObject.getLastupdate()));
		meta.setVersionId(Long.toString(localObject.getLastupdate()));
		domainResource.setMeta(meta);
	}

	public void mapNarrative(T source, U target) {
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

}
