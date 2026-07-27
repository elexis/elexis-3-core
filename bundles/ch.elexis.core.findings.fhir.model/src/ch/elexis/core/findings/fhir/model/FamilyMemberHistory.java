package ch.elexis.core.findings.fhir.model;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.DomainResource;

import ch.elexis.core.fhir.mapper.r4.findings.FamilyMemberHistoryAccessor;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.model.IXid;

public class FamilyMemberHistory extends AbstractFindingModelAdapter<ch.elexis.core.jpa.entities.FamilyMemberHistory>
		implements IFamilyMemberHistory {

	private FamilyMemberHistoryAccessor accessor = new FamilyMemberHistoryAccessor();

	public FamilyMemberHistory(ch.elexis.core.jpa.entities.FamilyMemberHistory entity) {
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
