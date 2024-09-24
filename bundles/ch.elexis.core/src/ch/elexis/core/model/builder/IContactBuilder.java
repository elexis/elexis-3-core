package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IAddress;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;

public class IContactBuilder {

	private IContactBuilder() {
	};

	public static class PersonBuilder extends AbstractBuilder<IPerson> {
		public PersonBuilder(IModelService modelService, String firstName, String lastName, LocalDate dateOfBirth,
				Gender sex) {
			super(modelService);
			object = modelService.create(IPerson.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setDateOfBirth(dateOfBirth.atStartOfDay());
			object.setGender(sex);
		}

		public PersonBuilder(IModelService modelService, String firstName, String lastName, Gender sex) {
			super(modelService);
			object = modelService.create(IPerson.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setGender(sex);
		}

		public PersonBuilder mandator() {
			object.setMandator(true);
			return this;
		}
	}

	public static class MandatorBuilder extends AbstractBuilder<IMandator> {
		public MandatorBuilder(IModelService modelService, String firstName, String lastName) {
			super(modelService);
			object = modelService.create(IMandator.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
		}
	}

	public static class PatientBuilder extends AbstractBuilder<IPatient> {
		public PatientBuilder(IModelService modelService, String firstName, String lastName, LocalDate dateOfBirth,
				Gender sex) {
			super(modelService);
			object = modelService.create(IPatient.class);
			object.setDescription1(lastName);
			object.setDescription2(firstName);
			object.setDateOfBirth(dateOfBirth.atStartOfDay());
			object.setGender(sex);
		}
	}

	public static class OrganizationBuilder extends AbstractBuilder<IOrganization> {
		public OrganizationBuilder(IModelService modelService, String name) {
			super(modelService);
			object = modelService.create(IOrganization.class);
			object.setDescription1(name);
		}

		/**
		 * Populates the default address. Setting both the values on the object itself,
		 * and on IContact#addAddress
		 *
		 * @param street
		 * @param zip
		 * @param city
		 * @param country
		 * @return
		 */
		public OrganizationBuilder defaultAddress(String street, String zip, String city, Country country) {
			IAddress defaultAddress = modelService.create(IAddress.class);
			object.setStreet(street);
			object.setZip(zip);
			object.setCity(city);
			object.setCountry(country);
			defaultAddress.setStreet1(street);
			defaultAddress.setZip(zip);
			defaultAddress.setCity(city);
			defaultAddress.setCountry(country);
			defaultAddress.setContact(object);
			// include address on save by adding to changed
			object.addChanged(defaultAddress);
			return this;
		}
	}

	public static class LaboratoryBuilder extends AbstractBuilder<ILaboratory> {
		public LaboratoryBuilder(IModelService modelService, String name) {
			super(modelService);
			object = modelService.create(ILaboratory.class);
			object.setDescription1(name);
		}

	}
}
