package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;

public class IContactBuilder {

	public static class PersonBuilder extends AbstractBuilder<IPerson> {
		public PersonBuilder(IModelService modelService, String firstName, String lastName, LocalDate dateOfBirth,
				Gender sex) {
			super(modelService);
			object = modelService.create(IPerson.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setDateOfBirth(dateOfBirth.atStartOfDay());
			object.setGender(sex);
			object.setPerson(true);
		}

		public PersonBuilder mandator() {
			object.setMandator(true);
			return this;
		}
	}
	
	public static class PatientBuilder extends AbstractBuilder<IPatient> {
		public PatientBuilder(IModelService modelService, String firstName, String lastName, LocalDate dateOfBirth,
			Gender sex){
			super(modelService);
			object = modelService.create(IPatient.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setDateOfBirth(dateOfBirth.atStartOfDay());
			object.setGender(sex);
			object.setPerson(true);
			object.setPatient(true);
		}
	}

	public static class OrganizationBuilder extends AbstractBuilder<IOrganization> {
		public OrganizationBuilder(IModelService modelService, String name) {
			super(modelService);
			object = modelService.create(IOrganization.class);
			object.setDescription1(name);
		}
	}
	
	public static class LaboratoryBuilder extends AbstractBuilder<ILaboratory> {
		public LaboratoryBuilder(IModelService modelService, String name){
			super(modelService);
			object = modelService.create(ILaboratory.class);
			object.setDescription1(name);
		}
		
	}
}
