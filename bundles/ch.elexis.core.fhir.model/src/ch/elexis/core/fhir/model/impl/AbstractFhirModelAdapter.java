package ch.elexis.core.fhir.model.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.codesystems.V3ObservationValue;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.slf4j.LoggerFactory;

import ch.elexis.core.fhir.model.FhirModelServiceHolder;
import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.fhir.model.interfaces.IFhirBased;
import ch.elexis.core.fhir.model.service.FhirAttributeMapperProvider;
import ch.elexis.core.fhir.model.service.FhirDtoProvider;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.WithExtInfo;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

// TODO add WithExtInfo -> via internal service
public abstract class AbstractFhirModelAdapter<T extends Identifiable, U extends DomainResource>
		implements Identifiable, WithExtInfo, IFhirBased {

	private boolean isDirty;

	private U fhirResource;
	private T localObject;
	private Map<Object, Object> extInfo;

	public AbstractFhirModelAdapter(U fhirResource) {
		this.fhirResource = fhirResource;
		this.extInfo = new HashMap<Object, Object>();
		this.isDirty = false;

		if (!isSubsetted()) {
			// we received a full FHIR object - load it
			localObject = FhirDtoProvider.createDto(getModelType());
			FhirAttributeMapperProvider.getMapper(getModelType(), getFhirType()).fhirToElexis(fhirResource,
					localObject);
		}
	}

	public U getFhirResource() {
		return fhirResource;
	}

	@Override
	public boolean isSubsetted() {
		return fhirResource.getMeta().getTag(V3ObservationValue.SUBSETTED.getSystem(),
				V3ObservationValue.SUBSETTED.toCode()) != null;
	}

	@Override
	public String getNarrativeLabel() {
		return fhirResource.getText().getDiv().allText();
	}

	@Override
	public Set<String> getNarrativeTags() {
		XhtmlNode div = fhirResource.getText().getDiv();
		String _class = div.getAttribute("class");
		if (_class == null) {
			return Collections.emptySet();
		}
		return Arrays.stream(_class.split("\\s+")).collect(Collectors.toSet());
	}

	// ok if subsetted, never delegate to localObject
	@Override
	public String getId() {
		return fhirResource.getIdElement().getIdPart();
	}

	// ok if subsetted, never delegate to localObject
	@Override
	public String getLabel() {
		return fhirResource.getText().getDiv().allText();
	}

	// ok if subsetted
	@Override
	public Long getLastupdate() {
		if (fhirResource.getMeta() != null && StringUtils.isNotBlank(fhirResource.getMeta().getVersionId())) {
			return Long.valueOf(fhirResource.getMeta().getVersionId());
		}
		return 0L;
	}

	public T getLoaded() {
		load();
		return localObject;
	}

	public T getLoadedMarkDirty() {
		isDirty = true;
		return getLoaded();
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getExtInfo(Object key) {
		// TODO direct access?
		return extInfo.get(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		// TODO direct access?
		extInfo.put(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		// TODO direct access?
		return extInfo;
	}

	@Override
	public void load() {
		if (localObject != null) {
			// localResource does not exist for subsetted instances
			return;
		}

		if (isSubsetted()) {
			Optional<U> loaded = FhirModelServiceHolder.get().load(getId(), getFhirType());
			if (loaded.isPresent()) {
				fhirResource = loaded.get();
			} else {
				LoggerFactory.getLogger(getClass()).warn("Error loading type %s with id %s", getFhirType(), getId());
				// FIXME what to do??
			}

			// TODO try to load exactly this version - what to do if this fails?
			// revert to a newer one? do we need to inform the user? I don't think that at
			// state isSubsetted this really matters!
			// if its reloaded with newer version send system event?

			// loadReplace fhirResource
			// use attributeMapper to populate local object
			// use localObject to delegate requests
		}
		// get AttributeMapper use it to populate
		localObject = FhirDtoProvider.createDto(getModelType());
		FhirAttributeMapperProvider.getMapper(getModelType(), getFhirType()).fhirToElexis(fhirResource, localObject);
	}

	public abstract Class<U> getFhirType();

	public abstract Class<?> getModelType();

	public Identifiable toEntityModelAdapter() {
		return (Identifiable) CoreModelServiceHolder.get().load(getId(), getModelType()).orElse(null);
	}

	public Class<?> getModelServiceClass() {
		return IFhirModelService.class;
	}
}
