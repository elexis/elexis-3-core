package ch.elexis.core.fhir.model.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.CareTeam;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.LoggerFactory;

import ch.elexis.core.fhir.model.impl.FhirImage;
import ch.elexis.core.fhir.model.impl.FhirPatient;
import ch.elexis.core.fhir.model.impl.FhirPerson;
import ch.elexis.core.fhir.model.impl.FhirPractitioner;
import ch.elexis.core.fhir.model.impl.FhirReminder;
import ch.elexis.core.fhir.model.impl.FhirUserGroup;
import ch.elexis.core.fhir.model.interfaces.IFhirBased;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.Identifiable;

public class ModelAdapterFactory {

	private Map<Class<? extends BaseResource>, Class<? extends IFhirBased>> fhirAdapterClassMap;
	private Map<Class<? extends Identifiable>, Class<? extends BaseResource>> modelFhirClassMap;

	public ModelAdapterFactory() {
		fhirAdapterClassMap = Map.of(Task.class, FhirReminder.class, Person.class, FhirPerson.class, Patient.class,
				FhirPatient.class, Practitioner.class, FhirPractitioner.class, CareTeam.class, FhirUserGroup.class);
		modelFhirClassMap = Map.of(IReminder.class, Task.class, IPerson.class, Person.class, IPatient.class,
				Patient.class, IMandator.class, Practitioner.class, IUserGroup.class, CareTeam.class);
	}

	public Identifiable createAdapter(BaseResource resource) {
		Class<? extends IFhirBased> adapterClass = fhirAdapterClassMap.get(resource.getClass());
		if (adapterClass != null) {
			try {
				Constructor<? extends IFhirBased> constructor = adapterClass
						.getDeclaredConstructor(resource.getClass());
				IFhirBased fhirBased = constructor.newInstance(resource);
				return (Identifiable) LazyFhirProxy.createLazy(fhirBased);
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				LoggerFactory.getLogger(getClass()).error("Exception creating adapter for [" + resource + "]", e);
			}
		}
		throw new IllegalArgumentException("No adapter class for resource [" + resource + "]");
	}

	public <T> T createAdapter(Class<T> clazz) {
		if (Objects.equals(clazz, IImage.class)) {
			Constructor<? extends Object> constructor;
			try {
				Class<? extends Object> adapterClass = FhirImage.class;
				constructor = adapterClass.getDeclaredConstructor();
				return (T) constructor.newInstance();
			} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				LoggerFactory.getLogger(getClass()).error("Exception creating adapter for [" + clazz + "]", e);
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
