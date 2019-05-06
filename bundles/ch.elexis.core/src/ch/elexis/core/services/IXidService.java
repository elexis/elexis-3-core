package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;

/**
 * Service for managing Xid domains.
 * 
 * @author thomas
 *
 */
public interface IXidService {
	
	/**
	 * Get the {@link IXidDomain} by its domain name or simple name.
	 * 
	 * @param name
	 * @return
	 */
	public IXidDomain getDomain(String name);
	
	public List<IXidDomain> getDomains(String name);
	
	public IXidDomain localRegisterXIDDomain(String domainName, String simpleName, int quality);
	
	public IXidDomain localRegisterXIDDomainIfNotExists(String domainName, String simpleName,
		int quality);
	
	/**
	 * Translate a simple name to a domain name. If the name is already a domain name it is returned
	 * unchanged.
	 * 
	 * @param domainName
	 * @return
	 */
	public String getDomainName(String domainName);
	
	/**
	 * Definition of a Xid domain object.
	 * 
	 * @author thomas
	 *
	 */
	public interface IXidDomain {
		
		public String getDomainName();
		
		public void setDomainName(String domainName);
		
		public String getSimpleName();
		
		public void setSimpleName(String simpleName);
		
		public int getQuality();
		
		public void setQuality(int quality);
	}
	
	/**
	 * Find a object with a {@link IXid} entry matching the provided domain information.
	 * 
	 * @param domainName
	 * @param domainId
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> findObject(String domainName, String domainId, Class<T> clazz);
	
	/**
	 * Add an {@link IXid} to the provided {@link Identifiable} with the provided domain and
	 * domainId. If there is already an existing {@link IXid} for the domain and the updateIfExists
	 * parameter is true, the information is updated and return value is true, else nothing is
	 * changed and return value is false. If a new {@link IXid} is created return value is true.
	 * 
	 * @param identifiable
	 * @param domain
	 * @param id
	 * @param updateIfExists
	 * @return
	 */
	public boolean addXid(Identifiable identifiable, String domain, String domainId,
		boolean updateIfExists);
	
	/**
	 * Get a {@link IXid} instance with the provided {@link Identifiable} as object and matching the
	 * provided domain.
	 * 
	 * @param identifiable
	 * @param domain
	 * @return
	 */
	public IXid getXid(Identifiable identifiable, String domain);
}
