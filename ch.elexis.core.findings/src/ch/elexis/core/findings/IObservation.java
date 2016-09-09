package ch.elexis.core.findings;

import java.util.List;
import java.util.Optional;

public interface IObservation extends IFinding {
	public List<IObservation> getSourceObservations();
	
	public void addSourceObservation(IObservation source);
	
	public List<IObservation> getTargetObseravtions();
	
	public void addTargetObservation(IObservation source);
	
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
