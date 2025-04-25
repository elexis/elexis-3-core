package ch.elexis.core.findings.util.fhir;

import java.util.HashMap;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;

public class ElexisFhirTypeMap {

	private static final HashMap<Class<? extends IBaseResource>, Class<? extends Identifiable>> fhirToLocalMap;
	private static final HashMap<Class<? extends Identifiable>, Class<? extends IBaseResource>> localToFhirMap;

	static {
		fhirToLocalMap = new HashMap<Class<? extends IBaseResource>, Class<? extends Identifiable>>();
		localToFhirMap = new HashMap<Class<? extends Identifiable>, Class<? extends IBaseResource>>();

		fhirToLocalMap.put(Patient.class, IPatient.class);
		localToFhirMap.put(IPatient.class, Patient.class);
		fhirToLocalMap.put(Person.class, IPerson.class);
		localToFhirMap.put(IPerson.class, Person.class);
		fhirToLocalMap.put(Organization.class, IOrganization.class);
		localToFhirMap.put(IOrganization.class, Organization.class);
	}

	public static Class<? extends IBaseResource> mapFromLocal(Class<? extends Identifiable> localObject) {
		return localToFhirMap.get(localObject);
	}

	public static Class<? extends Identifiable> mapFromFhir(Class<? extends IBaseResource> fhirClass) {
		return fhirToLocalMap.get(fhirClass);
	}

	public static Class<? extends IBaseResource> mapFromString(String resourceType) {
		return switch (resourceType.toLowerCase()) {
		case "patient" -> Patient.class;
		case "person" -> Person.class;
		case "organization" -> Organization.class;
		// TODO extend

		default -> throw new IllegalArgumentException("Unexpected value: " + resourceType);
		};
	}

}
