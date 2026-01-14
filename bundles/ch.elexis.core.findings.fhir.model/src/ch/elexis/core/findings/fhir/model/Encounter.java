package ch.elexis.core.findings.fhir.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.fhir.mapper.r4.findings.EncounterAccessor;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.fhir.model.service.FindingsModelServiceHolder;
import ch.elexis.core.model.IXid;

public class Encounter extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.Encounter>
		implements IEncounter {

	private EncounterAccessor accessor = new EncounterAccessor();

	public Encounter(ch.elexis.core.jpa.entities.Encounter entity) {
		super(entity);
	}

	@Override
	public String getPatientId() {
		return getEntity().getPatientId();
	}

	@Override
	public void setPatientId(String patientId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			org.hl7.fhir.r4.model.Encounter fhirEncounter = (org.hl7.fhir.r4.model.Encounter) resource.get();
			fhirEncounter.setSubject(new Reference(new IdDt("Patient", patientId)));
			saveResource(resource.get());
		}

		getEntity().setPatientId(patientId);
	}

	@Override
	public Optional<LocalDateTime> getStartTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStartTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setStartTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStartTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<LocalDateTime> getEndTime() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEndTime((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setEndTime(LocalDateTime time) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEndTime((DomainResource) resource.get(), time);
			saveResource(resource.get());
		}
	}

	@Override
	public String getConsultationId() {
		return getEntity().getConsultationId();
	}

	@Override
	public void setConsultationId(String consultationId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setConsultationId((DomainResource) resource.get(), consultationId);
			saveResource(resource.get());
		}
		getEntity().setConsultationId(consultationId);
	}

	@Override
	public String getMandatorId() {
		return getEntity().getMandatorId();
	}

	@Override
	public void setMandatorId(String mandatorId) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setMandatorId((DomainResource) resource.get(), mandatorId);
			saveResource(resource.get());
		}

		getEntity().setMandatorId(mandatorId);
	}

	@Override
	public void setType(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setType((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	@Override
	public List<ICoding> getType() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getType((DomainResource) resource.get());
		}
		return new ArrayList<>();
	}

	@Override
	public List<ICondition> getIndication() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getIndication(FindingsModelServiceHolder.get(), (DomainResource) resource.get());
		}
		return new ArrayList<>();
	}

	@Override
	public void setIndication(List<ICondition> indication) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setIndication((DomainResource) resource.get(), indication);
			saveResource(resource.get());
		}
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public String getRawContent() {
		return getEntity().getContent();
	}

	@Override
	public void setRawContent(String content) {
		getEntity().setContent(content);
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// TODO Auto-generated method stub
		return null;
	}
}
