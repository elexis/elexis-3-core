package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.VersionedResource;

public interface IEncounterService {
	
	/**
	 * Test if the {@link IEncounter} is editable in the current context.
	 * 
	 * @param encounter
	 * @return if editable, <code>false</code> if encounter is <code>null</code>
	 */
	public boolean isEditable(IEncounter encounter);
	
	/**
	 * Transfer the {@link IEncounter} to the {@link ICoverage}. Existing {@link IBilled} will be
	 * updated according to the information of the {@link ICoverage}.
	 * 
	 * @param encounter
	 * @param coverage
	 * @param ignoreEditable
	 * @return
	 */
	public Result<IEncounter> transferToCoverage(IEncounter encounter, ICoverage coverage,
		boolean ignoreEditable);
	
	/**
	 * Get the last {@link IEncounter} that was performed on patient by the active
	 * {@link IMandator}. If create is true a new {@link IEncounter} is created if non exists.
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
	 * @return all encounters of this patient, ordered newest first - or an empty list
	 */
	public List<IEncounter> getAllEncountersForPatient(IPatient patient);
	
	/**
	 * Update the encounter text with the content of {@link Samdas}. A new {@link VersionedResource}
	 * is created as head.
	 * 
	 * @param enc
	 * @param samdas
	 */
	public void updateVersionedEntry(IEncounter enc, Samdas samdas);
	
	/**
	 * Get the sales amount of the {@link IEncounter}.
	 * 
	 * @param encounter
	 * @return
	 */
	public Money getSales(IEncounter encounter);
	

}
