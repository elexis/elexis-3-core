package ch.elexis.core.services;

import ch.elexis.core.jdt.NonNull;

public interface ITraceService {

	/**
	 * Add a trace entry to the system.
	 * 
	 * @param username    who did the action
	 * @param workstation where the action was done
	 * @param action      what was done
	 */
	void addTraceEntry(@NonNull String username, @NonNull String workstation, @NonNull String action);

	/**
	 * Add a trace entry to the system, make the system derive the context. If the
	 * required info is already known,
	 * {@link #addTraceEntry(String, String, String)} should be preferred.
	 * 
	 * @param action
	 */
	void addTraceEntry(@NonNull String action);

}
