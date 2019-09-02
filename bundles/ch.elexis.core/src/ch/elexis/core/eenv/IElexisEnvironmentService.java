package ch.elexis.core.eenv;

public interface IElexisEnvironmentService {
	
	/**
	 * Entry in the Elexis#CONFIG TABLE with the elexis-environment hostname
	 */
	public static final String CFG_EE_HOSTNAME = "EE_HOSTNAME";
	
	/** 
	 * @return
	 */
	public String getHostname();
	
	/**
	 * @return the version of the elexis-environment connected to.
	 */
	public String getVersion();
	
	/**
	 * @param key
	 * @return a property provided by the elexis-environment
	 */
	public String getProperty(String key);
	
}
