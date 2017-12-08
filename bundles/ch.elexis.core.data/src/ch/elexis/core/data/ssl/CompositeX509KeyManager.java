package ch.elexis.core.data.ssl;

import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jdt.Nullable;

public class CompositeX509KeyManager implements X509KeyManager {
	
	private KeyStore defaultStore;
	
	private final HashMap<KeyStore, List<X509KeyManager>> keyManagers = new HashMap<>();
	
	public CompositeX509KeyManager(){
		try {
			defaultStore = KeyStore.getInstance(KeyStore.getDefaultType());
		} catch (KeyStoreException e) {
			LoggerFactory.getLogger(getClass()).error("Could not instantiate default store", e);
		}
	}
	
	/**
	 * Chooses the first non-null client alias returned from the delegate {@link X509TrustManagers},
	 * or {@code null} if there are no matches.
	 */
	@Override
	public @Nullable String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket){
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				String alias = x509KeyManager.chooseClientAlias(keyType, issuers, socket);
				if (alias != null) {
					return alias;
				}
			}
		}
		return null;
	}
	
	/**
	 * Chooses the first non-null server alias returned from the delegate {@link X509TrustManagers},
	 * or {@code null} if there are no matches.
	 */
	@Override
	public @Nullable String chooseServerAlias(String keyType, Principal[] issuers, Socket socket){
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				String alias = x509KeyManager.chooseServerAlias(keyType, issuers, socket);
				if (alias != null) {
					return alias;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first non-null private key associated with the given alias, or {@code null} if
	 * the alias can't be found.
	 */
	@Override
	public @Nullable PrivateKey getPrivateKey(String alias){
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				PrivateKey privateKey = x509KeyManager.getPrivateKey(alias);
				if (privateKey != null) {
					return privateKey;
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the first non-null certificate chain associated with the given alias, or {@code null}
	 * if the alias can't be found.
	 */
	@Override
	public @Nullable X509Certificate[] getCertificateChain(String alias){
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				X509Certificate[] chain = x509KeyManager.getCertificateChain(alias);
				if (chain != null && chain.length > 0) {
					return chain;
				}
			}
		}
		return null;
	}
	
	/**
	 * Get all matching aliases for authenticating the client side of a secure socket, or
	 * {@code null} if there are no matches.
	 */
	@Override
	public @Nullable String[] getClientAliases(String keyType, Principal[] issuers){
		List<String> ret = new ArrayList<>();
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				ret.addAll(Arrays.asList(x509KeyManager.getClientAliases(keyType, issuers)));
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	/**
	 * Get all matching aliases for authenticating the server side of a secure socket, or
	 * {@code null} if there are no matches.
	 */
	@Override
	public @Nullable String[] getServerAliases(String keyType, Principal[] issuers){
		List<String> ret = new ArrayList<>();
		for (List<X509KeyManager> keyManagers : keyManagers.values()) {
			for (X509KeyManager x509KeyManager : keyManagers) {
				ret.addAll(Arrays.asList(x509KeyManager.getServerAliases(keyType, issuers)));
			}
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	public void addKeyStore(KeyStore keyStore, String keystorePass){
		synchronized (keyManagers) {
			try {
				KeyManagerFactory factory =
					KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				factory.init(keyStore, keystorePass.toCharArray());
				KeyManager[] managers = factory.getKeyManagers();
				List<X509KeyManager> typedManagers = new ArrayList<>();
				for (KeyManager keyManager : managers) {
					if (keyManager instanceof X509KeyManager) {
						typedManagers.add((X509KeyManager) keyManager);
					}
				}
				keyManagers.put(keyStore, typedManagers);
			} catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException e) {
				LoggerFactory.getLogger(getClass()).error("Could not add trust store", e);
			}
		}
	}
	
	public void removeKeyStore(KeyStore keyStore){
		synchronized (keyManagers) {
			keyManagers.remove(keyStore);
		}
	}
	
	public void addKeyManager(X509KeyManager keyManager){
		List<X509KeyManager> defaultManagers = keyManagers.get(defaultStore);
		if (defaultManagers == null) {
			defaultManagers = new ArrayList<>();
		}
		defaultManagers.add(keyManager);
		keyManagers.put(defaultStore, defaultManagers);
	}
}
