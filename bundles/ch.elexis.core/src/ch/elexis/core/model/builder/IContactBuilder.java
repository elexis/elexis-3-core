package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IOrganization;
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

		/**
		 * This method does not initialize a patient number. The patient number is
		 * created by the persistence layer in {@link KontaktEntityListener}
		 * 
		 * @return
		 */
		public PersonBuilder patient() {
			object.setPatient(true);
			return this;
		}

		public PersonBuilder mandator() {
			object.setMandator(true);
			return this;
		}
	}

	public static class OrganizationBuilder extends AbstractBuilder<IOrganization> {
		public OrganizationBuilder(IModelService modelService, String name) {
			super(modelService);
			object = modelService.create(IOrganization.class);
			object.setDescription1(name);
			object.setOrganization(true);
		}

		public OrganizationBuilder laboratory() {
			object.setLaboratory(true);
			return this;
		}
	}
}
