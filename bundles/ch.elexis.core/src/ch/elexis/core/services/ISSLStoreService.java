package ch.elexis.core.services;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.Optional;

public interface ISSLStoreService {
	
	/**
	 * Add a TrustStore to the service.
	 * 
	 * @param keyStore
	 */
	void addTrustStore(KeyStore keyStore);
	
	/**
	 * Add a KeyStore to the service.
	 * 
	 * @param keyStore
	 * @param keystorePass
	 */
	void addKeyStore(KeyStore keyStore, String keystorePass);
	
	/**
	 * Remove a KeyStore from the service.
	 * 
	 * @param keyStore
	 */
	void removeKeyStore(KeyStore keyStore);
	
	/**
	 * Load the {@link KeyStore} from the file system.
	 * 
	 * @param keystoreInput
	 * @param truststorePass
	 * @param algorithm
	 * @return
	 */
	public Optional<KeyStore> loadKeyStore(String keystorePath, String truststorePass,
		String algorithm);
	
	/**
	 * Load the {@link KeyStore} from the {@link InputStream}.
	 * 
	 * @param keystoreInput
	 * @param truststorePass
	 * @param algorithm
	 * @return
	 */
	public Optional<KeyStore> loadKeyStore(InputStream keystoreInput, String truststorePass,
		String algorithm);
}
