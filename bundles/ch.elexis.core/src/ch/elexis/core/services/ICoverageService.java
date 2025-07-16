package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.ch.BillingLaw;

public interface ICoverageService {

	public static enum Tiers {
		PAYANT("TP"), GARANT("TG");

		private String shortName;

		private Tiers(String string) {
			this.shortName = string;
		}

		public String getShortName() {
			return shortName;
		}
	};

	/**
	 * Test if all the required fields of the {@link ICoverage} are set.
	 *
	 * @param coverage
	 * @return
	 */
	public boolean isValid(ICoverage coverage);

	/**
	 * Retrieve a required String Value from this billing system's definition. If no
	 * variable with that name is found, the billings system constants will be
	 * searched
	 *
	 * @param name
	 * @return a string that might be empty but will never be null.
	 */
	public String getRequiredString(ICoverage coverage, String name);

	/**
	 * Set a required String value to a billing system's definition.
	 *
	 * @param coverage
	 * @param name
	 * @param value
	 */
	public void setRequiredString(ICoverage coverage, String name, String value);

	/**
	 * Retrieve a required {@link IContact} from this Fall's Billing system's
	 * requirements
	 *
	 * @param name the requested Kontakt's name
	 * @return the {@link IContact} or null if no such {@link IContact} was found
	 */
	public IContact getRequiredContact(ICoverage coverage, String name);

	public void setRequiredContact(ICoverage coverage, String name, IContact value);

	public Tiers getTiersType(ICoverage coverage);

	public boolean getCopyForPatient(ICoverage coverage);

	public void setCopyForPatient(ICoverage coverage, boolean copy);

	public String getDefaultCoverageLabel();

	public String getDefaultCoverageReason();

	public String getDefaultCoverageLaw();

	/**
	 * Create a copy of the provided {@link ICoverage} including billing systems
	 * required and optional data fields.
	 *
	 * @param coverage
	 * @return
	 */
	public ICoverage createCopy(ICoverage coverage);

	/**
	 * Finds the last non deleted {@link IEncounter} of the coverage.
	 *
	 * @return
	 */
	public Optional<IEncounter> getLatestEncounter(ICoverage coverage);

	/**
	 * Returns the latest open coverage of the patient
	 * 
	 * @param patient
	 * @return
	 */
	Optional<ICoverage> getLatestOpenCoverage(IPatient patient);

	/**
	 * Creates a default coverage for the patient
	 * 
	 * @param patient
	 * @return
	 */
	ICoverage createDefaultCoverage(IPatient patient);

	/**
	 * Search for the first {@link ICoverage} in state {@link ICoverage#isOpen()} of
	 * the {@link IPatient} with a matching law.
	 *
	 * @param patient
	 * @param law
	 * @return
	 * @since 3.12
	 */
	Optional<ICoverage> getCoverageWithLaw(IPatient patient, BillingLaw... laws);

	/**
	 * Test if the {@link ICoverage} can be deleted. Should have no references from
	 * {@link IEncounter}s, {@link ISickCertificate}s or {@link IInvoice}s.
	 * 
	 * @param element
	 * @return
	 */
	public boolean canDelete(ICoverage element);
}
