package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.rgw.tools.Result;

public interface IEncounterService {
	
	/**
	 * Test if the {@link IEncounter} is editable in the current context.
	 * 
	 * @param encounter
	 * @return
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
	 * Get the last {@link IEncounter} that was performed on patient.
	 * 
	 * @param patient
	 * @return
	 */
	public Optional<IEncounter> getLastEncounter(IPatient patient);
}
