package ch.elexis.core.findings;

import java.util.Optional;

public interface ICondition extends IFinding {
	public enum ConditionCategory {
		UNKNOWN, DIAGNOSIS, COMPLAINT
	}

	public enum ConditionStatus {
		UNKNOWN, ACTIVE, RELAPSE, REMISSION, RESOLVED
	}

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

	/**
	 * Get the condition category.
	 */
	public ConditionCategory getCategory();

	/**
	 * Set the condition category.
	 * 
	 * @param category
	 */
	public void setCategory(ConditionCategory category);

	/**
	 * Get the condition status.
	 * 
	 * @return
	 */
	public ConditionStatus getStatus();

	/**
	 * Set the condition category.
	 * 
	 * @param status
	 */
	public void setStatus(ConditionStatus status);
}
