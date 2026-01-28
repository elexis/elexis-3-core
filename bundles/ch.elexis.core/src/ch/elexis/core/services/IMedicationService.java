package ch.elexis.core.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.prescription.EntryType;

public interface IMedicationService {

	/**
	 * Return the dose of a drugs as a list of up to 4 floats.<br>
	 * Up to Version 3.0.10 (hopefully) Elexis did not specify exactly the dose of a
	 * drug, but used a String where many doctors used shortcuts like 1-0-1-1 to
	 * specify that the number of entities (e.g. a tablet) for breakfast, lunch,
	 * supper, before night Here are some examples how this procedure deals with the
	 * some input. 0-0- 1/4-1/2 => <0.0,0.0,0.25> 0.5/-/- => <0.5> 0-0-0- 40E =>
	 * <0.0,0.0,0.0,40.0> 0.5 Stk alle 3 Tage => <0.5> 1inj Wo => <>
	 *
	 * More examples can be found in the unit test.
	 *
	 * @return a list of (up to 4) floats
	 */
	public List<Float> getDosageAsFloats(IPrescription prescription);

	/**
	 * Get the dosage of a whole day as float.
	 *
	 * @return
	 */
	public float getDailyDosageAsFloat(IPrescription prescription);

	/**
	 * Lookup a matching {@link IArticleDefaultSignature} for the {@link IArticle}.
	 * If no direct match by article is found a lookup with the atc code of the
	 * article is performed.
	 *
	 * @param article
	 * @return
	 */
	public Optional<IArticleDefaultSignature> getDefaultSignature(IArticle article);

	/**
	 * Lookup a matching {@link IArticleDefaultSignature} for the atc code.
	 *
	 * @param atcCode
	 * @return
	 */
	public Optional<IArticleDefaultSignature> getDefaultSignature(String atcCode);

	/**
	 * Get a transient {@link IArticleDefaultSignature} for the {@link IArticle}.
	 *
	 * @param article
	 * @return
	 */
	public IArticleDefaultSignature getTransientDefaultSignature(IArticle article);

	/**
	 * Stop the {@link IPrescription} at the provided stop date time.
	 *
	 * @param prescription
	 * @param stopDateTime
	 * @param stopReason
	 */
	public void stopPrescription(IPrescription prescription, LocalDateTime stopDateTime, String stopReason);

	/**
	 * Stop the {@link IPrescription} at the provided stop date time, without a
	 * reason.
	 *
	 * @param prescription
	 * @param stopDateTime
	 */
	public default void stopPrescription(IPrescription prescription, LocalDateTime stopDateTime) {
		stopPrescription(prescription, stopDateTime, null);
	}

	/**
	 * Create a transient copy of the prescription.
	 *
	 * @param prescription
	 * @return
	 */
	public IPrescription createPrescriptionCopy(IPrescription prescription);

	/**
	 * Create a {@link IRecipe} with the provided {@link IPatient} and
	 * prescriptions. The prescriptions are not altered, but new
	 * {@link IPrescription}s are created with {@link EntryType#RECIPE}. The
	 * {@link IRecipe} an its {@link IPrescription} entries are persisted.
	 *
	 * @param patient
	 * @param prescRecipes
	 * @return
	 */
	public IRecipe createRecipe(IPatient patient, List<IPrescription> prescRecipes);

	/**
	 *
	 * @return the signature split into a string array with 4 elements; will always
	 *         return an array of 4 elements, where empty entries are of type String
	 *         StringUtils.EMPTY
	 */
	public String[] getSignatureAsStringArray(String signature);
}
