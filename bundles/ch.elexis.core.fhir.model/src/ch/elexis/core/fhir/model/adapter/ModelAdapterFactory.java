package ch.elexis.core.fhir.model.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.LoggerFactory;

import ch.elexis.core.fhir.model.impl.FhirCoverage;
import ch.elexis.core.fhir.model.impl.FhirPatient;
import ch.elexis.core.fhir.model.impl.FhirPractitionerContact;
import ch.elexis.core.fhir.model.impl.FhirReminder;
import ch.elexis.core.fhir.model.impl.FhirUserGroup;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.Identifiable;

public class ModelAdapterFactory {

	private Map<Class<? extends BaseResource>, Class<?>> fhirAdapterClassMap;
	private Map<Class<? extends Identifiable>, Class<? extends BaseResource>> modelFhirClassMap;

	public ModelAdapterFactory() {
		fhirAdapterClassMap = Map.of(Task.class, FhirReminder.class, Patient.class, FhirPatient.class,
				Practitioner.class, FhirPractitionerContact.class, CareTeam.class, FhirUserGroup.class, Coverage.class,
				FhirCoverage.class);
		modelFhirClassMap = Map.of(IReminder.class, Task.class, IPatient.class, Patient.class, IMandator.class,
				Practitioner.class, IUserGroup.class, CareTeam.class, ICoverage.class, Coverage.class);
	}

	public Identifiable createAdapter(BaseResource resource) {
		if (resource != null) {
			Class<? extends Object> adapterClass = fhirAdapterClassMap.get(resource.getClass());
			if (adapterClass != null) {
				Constructor<? extends Object> constructor;
				try {
					constructor = adapterClass.getDeclaredConstructor(resource.getClass());
					return (Identifiable) constructor.newInstance(resource);
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException e) {
					LoggerFactory.getLogger(getClass()).error("Exception creating adapter for [" + resource + "]", e);
				}
			} else {
				throw new IllegalArgumentException("No adapter class for resource [" + resource + "]");
			}
		}
		return null;
	}

	public Class<? extends BaseResource> getFhirType(Class<? extends Identifiable> modelType) {
		if (modelType.equals(IContact.class) || modelType.equals(IMandator.class)) {
			return Practitioner.class;
		}
		return modelFhirClassMap.get(modelType);
	}
}
