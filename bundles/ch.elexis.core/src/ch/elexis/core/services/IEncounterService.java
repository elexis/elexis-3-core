package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public interface IEncounterService {

	/**
	 * Set the date of the {@link IEncounter} to the provided new date. Check if the
	 * existing {@link IBilled} are valid on that date. If any is invalid the date
	 * is not set, and the returned Result contains additional information.
	 * 
	 * @param encounter
	 * @return
	 */
	public Result<IEncounter> setEncounterDate(IEncounter encounter, LocalDate newDate);

	/**
	 * Test if the {@link IEncounter} is editable in the current context.
	 *
	 * @param encounter
	 * @return if editable, <code>false</code> if encounter is <code>null</code>
	 */
	public boolean isEditable(IEncounter encounter);

	/**
	 * Transfer the {@link IEncounter} to the {@link ICoverage}. Existing
	 * {@link IBilled} will be updated according to the information of the
	 * {@link ICoverage}.
	 *
	 * @param encounter
	 * @param coverage
	 * @param ignoreEditable
	 * @return
	 */
	public Result<IEncounter> transferToCoverage(IEncounter encounter, ICoverage coverage, boolean ignoreEditable);

	/**
	 * Transfer the {@link IEncounter} to the {@link IMandator}. Existing
	 * {@link IBilled} will be updated according to the information of the
	 * {@link IMandator}.
	 *
	 * @param encounter
	 * @param mandator
	 * @param ignoreEditable
	 * @return
	 */
	public Result<IEncounter> transferToMandator(IEncounter encounter, IMandator mandator, boolean ignoreEditable);

	/**
	 * Get the last {@link IEncounter} that was performed on patient by the active
	 * {@link IMandator}. If create is true a new {@link IEncounter} is created if
	 * non exists.
	 *
	 * @param patient
	 * @param create
	 * @return
	 */
	public Optional<IEncounter> getLatestEncounter(IPatient patient, boolean create);

	/**
	 * Finds the last non deleted {@link IEncounter} over all {@link IMandator}s.
	 *
	 * @return
	 */
	public Optional<IEncounter> getLatestEncounter(IPatient patient);

	/**
	 *
	 * @param patient
	 * @return all encounters of this patient, ordered newest first - or an empty
	 *         list
	 */
	public List<IEncounter> getAllEncountersForPatient(IPatient patient);

	/**
	 * Update the encounter text with the content of {@link Samdas}. A new
	 * {@link VersionedResource} is created as head. Change is persisted.
	 *
	 * @param enc
	 * @param samdas
	 */
	public void updateVersionedEntry(IEncounter enc, Samdas samdas);

	/**
	 * Update the encounter text with the content of entryXml. A new
	 * {@link VersionedResource} is created as head. Change is persisted.
	 *
	 * @param enc
	 * @param entryXml
	 * @param remark
	 */
	public void updateVersionedEntry(IEncounter enc, String entryXml, String remark);

	/**
	 * Get the sales amount of the {@link IEncounter}.
	 *
	 * @param encounter
	 * @return
	 */
	public Money getSales(IEncounter encounter);

	/**
	 * Get all {@link IBilled} of the {@link IEncounter} that match the provided
	 * {@link IBillable}.
	 *
	 * @param newEncounter
	 * @param iVerrechenbar
	 * @return
	 */
	public List<IBilled> getBilledByBillable(IEncounter encounter, IBillable billable);

	/**
	 * Adds the users default {@link IDiagnosis} (if set) to the encounter. Saves
	 * the encounter.
	 *
	 * @param encounter
	 */
	public void addDefaultDiagnosis(IEncounter encounter);

	/**
	 * All {@link IBilled} of the {@link IEncounter} will be updated according to
	 * the information of the {@link ICoverage}.
	 * 
	 * @param encounter
	 * @return
	 */
	public Result<IEncounter> reBillEncounter(IEncounter encounter);

	/**
	 * Insert an XREF to the encounters {@link Samdas} text.
	 * 
	 * @param encounter
	 * @param provider  unique String identifying the provider
	 * @param id        String identifying the item
	 * @param pos       position of the item as offset relative to the contents
	 * @param text      text to insert
	 */
	void addXRef(IEncounter encounter, String provider, String id, int pos, String text);
}
