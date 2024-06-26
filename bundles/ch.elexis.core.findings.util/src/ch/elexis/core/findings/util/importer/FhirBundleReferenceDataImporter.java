package ch.elexis.core.findings.util.importer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.fhir.FhirChConstants;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.interfaces.AbstractReferenceDataImporter;
import ch.elexis.core.interfaces.IReferenceDataImporter;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IXidService;

@Component(property = IReferenceDataImporter.REFERENCEDATAID + "=fhirbundle", service = IReferenceDataImporter.class)
public class FhirBundleReferenceDataImporter extends AbstractReferenceDataImporter {

	@Reference
	protected IFhirTransformerRegistry transformerRegistry;

	@Reference
	protected IXidService xidService;

	@Override
	public IStatus performImport(IProgressMonitor ipm, InputStream input, Integer newVersion) {
		return performImport(ipm, input, newVersion, null);
	}

	public IStatus performImport(IProgressMonitor ipm, InputStream input, Integer newVersion,
			Consumer<Object> updateLocalObjectConsumer) {
		if(input != null) {
			try {
				String jsonString = IOUtils.toString(input, "UTF-8");
				IBaseResource resource = ModelUtil.getAsResource(jsonString);
				if (resource instanceof Bundle) {
					Bundle bundle = (Bundle) resource;
					for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
						Optional<?> localObject = Optional.empty();
						Resource entryResource = bundleEntry.getResource();
						if (entryResource instanceof Organization) {
							Organization fhirObject = (Organization) entryResource;
							IFhirTransformer<Organization, IOrganization> transformer = transformerRegistry
									.getTransformerFor(Organization.class, IOrganization.class);
							localObject = getLocalObjectByIdentifiers(fhirObject, IOrganization.class);
							if (localObject.isEmpty()) {
								localObject = transformer.createLocalObject(fhirObject);
							} else {
								localObject = transformer.updateLocalObject(fhirObject,
										(IOrganization) localObject.get());
							}
						} else {
							LoggerFactory.getLogger(getClass())
									.warn("Unknown entry resource type [" + entryResource + "]");
						}
						if (localObject.isPresent() && updateLocalObjectConsumer != null) {
							updateLocalObjectConsumer.accept(localObject.get());
						}
					}
				}
				return Status.OK_STATUS;
			} catch (IOException e) {
				LoggerFactory.getLogger(getClass()).error("Error importing FHIR bundle", e);
			}			
		} else {
			LoggerFactory.getLogger(getClass()).warn("No input to import");
		}
		return Status.CANCEL_STATUS;
	}

	private <T> Optional<T> getLocalObjectByIdentifiers(Resource fhirObject, Class<T> clazz) {
		List<Identifier> identifiers = getIdentifiersReflective(fhirObject);
		for (Identifier identifier : identifiers) {
			String localSystem = toLocalSystem(identifier.getSystem());
			List<T> found = xidService.findObjects(localSystem, identifier.getValue(), clazz);
			if (!found.isEmpty()) {
				if (found.size() > 1) {
					LoggerFactory.getLogger(getClass()).warn("Found ["
							+ found.stream().map(f -> ((Identifiable) f).getId()).collect(Collectors.joining(","))
							+ "] for [" + localSystem + "|" + identifier.getValue() + "] using first.");
				}
				return Optional.of(found.get(0));
			}
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	private List<Identifier> getIdentifiersReflective(Resource resource) {
		try {
			Method getterMethod = resource.getClass().getMethod("getIdentifier", (Class[]) null);
			Object list = getterMethod.invoke(resource, (Object[]) null);
			if (list instanceof List<?>) {
				return (List<Identifier>) list;
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			LoggerFactory.getLogger(getClass()).warn("Could not get identifiers of [" + resource + "]", e.getMessage());
		}
		return Collections.emptyList();
	}

	private String toLocalSystem(String system) {
		switch (system) {
		case XidConstants.CH_AHV:
		case FhirChConstants.OID_AHV13_SYSTEM:
			return XidConstants.CH_AHV;
		case XidConstants.EAN:
		case FhirChConstants.OID_GLN_SYSTEM:
			return XidConstants.EAN;
		case XidConstants.DOMAIN_BSVNUM:
		case FhirChConstants.BSV_NUMMER_SYSTEM:
			return XidConstants.DOMAIN_BSVNUM;
		default:
			break;
		}
		return null;
	}

	@Override
	public int getCurrentVersion() {
		return -1;
	}

}
