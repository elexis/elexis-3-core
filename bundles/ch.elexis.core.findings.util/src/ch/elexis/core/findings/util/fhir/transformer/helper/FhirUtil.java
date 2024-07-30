package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Money;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;

public class FhirUtil {

	public static void setVersionedIdPartLastUpdatedMeta(Class<?> resourceClass, DomainResource domainResource,
			Identifiable localObject) {
		domainResource.setId(new IdDt(resourceClass.getSimpleName(), localObject.getId(),
				Long.toString(localObject.getLastupdate())));
		domainResource.getMeta().setLastUpdated(getLastUpdateAsDate(localObject.getLastupdate()).orElse(null));
		domainResource.getMeta().setVersionId(Long.toString(localObject.getLastupdate()));
	}

	public static Optional<Date> getLastUpdateAsDate(Long lastUpdate) {
		if (lastUpdate != null) {
			Date lastUpdateDate = Date.from(getLastUpdateAsZonedDateTime(lastUpdate).get().toInstant());
			return Optional.of(lastUpdateDate);
		}
		return Optional.empty();
	}

	public static Optional<ZonedDateTime> getLastUpdateAsZonedDateTime(Long lastUpdate) {
		if (lastUpdate != null) {
			ZonedDateTime zonedDateTime = Instant.ofEpochMilli(lastUpdate).atZone(ZoneId.systemDefault());
			return Optional.of(zonedDateTime);

		}
		return Optional.empty();
	}

	public static Reference getReference(Identifiable identifiable) {
		if (identifiable == null) {
			return null;
		}

		String resourceType = null;

		if (identifiable instanceof IPatient) {
			resourceType = "Patient";
		} else if (identifiable instanceof IEncounter) {
			resourceType = "Encounter";
		} else if (identifiable instanceof IBilled) {
			resourceType = "ChargeItem";
		} else if (identifiable instanceof IMandator) {
			resourceType = "Practitioner";
		}

		if (resourceType != null) {
			return new Reference(new IdDt(resourceType, identifiable.getId()));
		}

		throw new IllegalArgumentException(identifiable.getClass().getCanonicalName());
	}

	public static Optional<String> getId(Reference reference) {
		if (reference != null) {
			if (StringUtils.isNotBlank(reference.getReference())) {
				return Optional.of(reference.getReferenceElement().getIdPart());
			} else if (StringUtils.isNotBlank(reference.getId())) {
				if (reference.getId().startsWith("/")) {
					return Optional.of(reference.getId().substring(1));
				}
				return Optional.of(reference.getId());
			}
		}
		return Optional.empty();
	}

	public static Money toFhir(ch.rgw.tools.Money total) {
		Money money = new Money();
		money.setValue(total.doubleValue());
		money.setCurrency("CHF");
		return money;
	}

	public static Optional<String> getLocalId(String id) {
		if (StringUtils.isNotBlank(id)) {
			if (id.endsWith("/")) {
				id = id.substring(0, id.length() - 1);
			}
			if (id.contains("/")) {
				if (id.contains("/_history")) {
					id = id.substring(0, id.indexOf("/_history"));
				}
				return Optional.of(id.substring(id.lastIndexOf('/') + 1));
			}
			return Optional.of(id);
		}
		return Optional.empty();
	}

	public static boolean isReferenceType(Reference reference, String type) {
		if (reference.getReferenceElement().hasResourceType()) {
			return reference.getReferenceElement().getResourceType().equals(type);
		} else if (reference.hasType()) {
			return reference.getType().equals(type);
		}
		return false;
	}

	/**
	 * Get the code String of the first {@link Coding} in the
	 * {@link CodeableConcept} list with matching system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static Optional<String> getCodeFromConceptList(String system, List<CodeableConcept> list) {
		if (list != null && !list.isEmpty()) {
			for (CodeableConcept concept : list) {
				Optional<String> found = getCodeFromCodingList(system, concept.getCoding());
				if (found.isPresent()) {
					return found;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the codes of all {@link Coding}s in the {@link CodeableConcept} matching
	 * the system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static List<String> getCodesFromConceptList(String system, List<CodeableConcept> list) {
		if (list != null && !list.isEmpty()) {
			List<String> ret = new ArrayList<String>();
			for (CodeableConcept concept : list) {
				ret.addAll(getCodesFromCodingList(system, concept.getCoding()));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	/**
	 * Get the code String of the first {@link Coding} in the list with matching
	 * system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static Optional<String> getCodeFromCodingList(String system, List<Coding> list) {
		if (list != null && !list.isEmpty()) {
			for (Coding coding : list) {
				if (coding.getSystem().equals(system) && coding.getCode() != null) {
					return Optional.of(coding.getCode());
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the codes of all {@link Coding}s matching the system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static List<String> getCodesFromCodingList(String system, List<Coding> list) {
		if (list != null && !list.isEmpty()) {
			List<String> ret = new ArrayList<String>();
			for (Coding coding : list) {
				if (coding.getSystem().equals(system) && coding.getCode() != null) {
					ret.add(coding.getCode());
				}
			}
			return ret;
		}
		return Collections.emptyList();
	}
}
