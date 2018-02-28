package ch.elexis.core.services;

/**
 * Interface to make a decision how to resolve a conflict.
 * 
 * @author thomas
 *
 */
public interface IConflictHandler {
	
	/**
	 * Result definition for a conflict.
	 *
	 */
	public enum Result {
			KEEP, OVERWRITE, ABORT
	}
	
	public Result getResult();
}
