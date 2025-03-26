package ch.elexis.core.findings.util.fhir;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.Identifiable;

public class ResourceInfo {

	private String resourceId;
	private Class<? extends Identifiable> localClass;
	private Class<? extends IBaseResource> fhirClass;

	private ResourceInfo() {
	}

	/**
	 * 
	 * @param fhirResourceId e.g. <code>Patient/23423</code>
	 * @return
	 */
	public static ResourceInfo of(String fhirResourceId) {
		String[] split = fhirResourceId.split("/");
		ResourceInfo resourceInfo = new ResourceInfo();
		resourceInfo.fhirClass = ElexisFhirTypeMap.mapFromString(split[0]);
		resourceInfo.localClass = ElexisFhirTypeMap.mapFromFhir(resourceInfo.fhirClass);
		resourceInfo.resourceId = split[1];
		return resourceInfo;
	}

	public static ResourceInfo of(Identifiable localObject) {
		ResourceInfo resourceInfo = new ResourceInfo();
		resourceInfo.localClass = localObject.getClass();
		resourceInfo.fhirClass = ElexisFhirTypeMap.mapFromLocal(localObject.getClass());
		resourceInfo.resourceId = localObject.getId();
		return resourceInfo;
	}

	public IFhirTransformer<?, ?> getTransformer(IFhirTransformerRegistry transformerRegistry) {
		return transformerRegistry.getTransformerFor(fhirClass, localClass);
	}

	public Class<? extends Identifiable> getLocalClass() {
		return localClass;
	}

	public Class<? extends IBaseResource> getFhirClass() {
		return fhirClass;
	}

	public String getId() {
		return resourceId;
	}

	public IIdType getIId() {
		return new IdDt(fhirClass.getSimpleName(), resourceId);
	}
}
