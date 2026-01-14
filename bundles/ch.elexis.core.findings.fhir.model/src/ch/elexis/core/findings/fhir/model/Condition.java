package ch.elexis.core.findings.fhir.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;

import ch.elexis.core.fhir.mapper.r4.findings.ConditionAccessor;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.model.IXid;

public class Condition extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.Condition>
		implements ICondition {

	private ConditionAccessor accessor = new ConditionAccessor();

	public Condition(ch.elexis.core.jpa.entities.Condition entity) {
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
			accessor.setPatientId((DomainResource) resource.get(), patientId);
			saveResource(resource.get());
		}

		getEntity().setPatientId(patientId);
	}

	@Override
	public List<ICoding> getCoding() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCoding((DomainResource) resource.get());
		}
		return Collections.emptyList();
	}

	@Override
	public void setCoding(List<ICoding> coding) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCoding((DomainResource) resource.get(), coding);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<LocalDate> getDateRecorded() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getDateRecorded((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setDateRecorded(LocalDate date) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setDateRecorded((DomainResource) resource.get(), date);
			saveResource(resource.get());
		}
	}

	@Override
	public ConditionCategory getCategory() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getCategory((DomainResource) resource.get());
		}
		return ConditionCategory.UNKNOWN;
	}

	@Override
	public void setCategory(ConditionCategory category) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setCategory((DomainResource) resource.get(), category);
			saveResource(resource.get());
		}
	}

	@Override
	public ConditionStatus getStatus() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStatus((DomainResource) resource.get());
		}
		return ConditionStatus.UNKNOWN;
	}

	@Override
	public void setStatus(ConditionStatus status) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent() && status != ConditionStatus.UNKNOWN) {
			accessor.setStatus((DomainResource) resource.get(), status);
			saveResource(resource.get());
		}
	}

	@Override
	public void setStart(String start) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setStart((DomainResource) resource.get(), start);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getStart() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getStart((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void setEnd(String end) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.setEnd((DomainResource) resource.get(), end);
			saveResource(resource.get());
		}
	}

	@Override
	public Optional<String> getEnd() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getEnd((DomainResource) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public void addNote(String text) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.addNote((DomainResource) resource.get(), text);
			saveResource(resource.get());
		}
	}

	@Override
	public void removeNote(String text) {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			accessor.removeNote((DomainResource) resource.get(), text);
			saveResource(resource.get());
		}
	}

	@Override
	public List<String> getNotes() {
		Optional<IBaseResource> resource = loadResource();
		if (resource.isPresent()) {
			return accessor.getNotes((DomainResource) resource.get());
		}
		return Collections.emptyList();
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
