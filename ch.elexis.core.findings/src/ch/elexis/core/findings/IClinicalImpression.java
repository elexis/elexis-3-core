package ch.elexis.core.findings;

import java.util.Optional;

public interface IClinicalImpression extends IFinding {
	/**
	 * Get the {@link IEncounter} referenced.
	 * 
	 * @return
	 */
	public Optional<IEncounter> getEncounter();
	
	/**
	 * Update the {@link IEncounter} referenced. Also updates the patientId with the value of the
	 * {@link IEncounter}.
	 * 
	 * @param encounter
	 */
	public void setEncounter(IEncounter encounter);
}
