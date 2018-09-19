package ch.elexis.core.services;

public interface IElexisEntityManager {
	
	/**
	 * Get a managed javax.persistence.EntityManager instance for the Elexis persistence unit.
	 * 
	 * @return
	 */
	public default Object getEntityManager(){
		return getEntityManager(true);
	}
	
	/**
	 * Get a javax.persistence.EntityManager instance for the Elexis persistence unit. If managed is
	 * true the returned javax.persistence.EntityManager will be automatically closed on Thread
	 * termination, making loaded Objects detached.<br />
	 * <br />
	 * <b>Always</b> close non managed javax.persistence.EntityManager instance after work is done
	 * using {@link IElexisEntityManager#closeEntityManager(Object)}.
	 * 
	 * @param managed
	 * @return
	 */
	public Object getEntityManager(boolean managed);
	
	/**
	 * Close the javax.persistence.EntityManager and release all registered resources.
	 * 
	 * @param entityManager
	 */
	public void closeEntityManager(Object entityManager);
	
	/**
	 * Execute a SQL script against the current persistence unit database. <b>This method can only
	 * be used in test mode.</b>
	 * 
	 * @param changeId
	 * @param sqlScript
	 * @return true if script execution was successful
	 */
	public boolean executeSQLScript(String changeId, String sqlScript);
}
