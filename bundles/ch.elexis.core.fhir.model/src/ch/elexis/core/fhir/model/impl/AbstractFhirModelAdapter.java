package ch.elexis.core.fhir.model.impl;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.BaseResource;

import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public abstract class AbstractFhirModelAdapter<T extends BaseResource> implements Identifiable {

	private T fhirResource;

	public AbstractFhirModelAdapter(T fhirResource) {
		this.fhirResource = fhirResource;
	}

	public T getFhirResource() {
		return fhirResource;
	}

	@Override
	public String getId() {
		return fhirResource.getIdElement().getIdPart();
	}

	@Override
	public String getLabel() {
		return fhirResource.getId();
	}

	@Override
	public Long getLastupdate() {
		if (fhirResource.getMeta() != null && StringUtils.isNotBlank(fhirResource.getMeta().getVersionId())) {
			return Long.valueOf(fhirResource.getMeta().getVersionId());
		}
		return 0L;
	}

	@Override
	public IXid getXid(String domain) {
		throw new UnsupportedOperationException();
	}

	public abstract Class<T> getFhirType();

	public abstract Class<?> getModelType();

	public Identifiable toEntityModelAdapter() {
		return (Identifiable) CoreModelServiceHolder.get().load(getId(), getModelType()).orElse(null);
	}

	public Class<?> getModelServiceClass() {
		return IFhirModelService.class;
	}
}
