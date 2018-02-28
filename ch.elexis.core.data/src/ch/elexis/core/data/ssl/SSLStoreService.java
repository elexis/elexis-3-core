package ch.elexis.core.data.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.ISSLStoreService;

@Component
public class SSLStoreService implements ISSLStoreService {
	private CompositeX509KeyManager compositeKeyManager;
	private CompositeX509TrustManager compositeTrustManager;
	
	@Activate
	public void activate(){
		SSLContext context;
		try {
			X509KeyManager jvmKeyManager = getJvmKeyManager();
			X509TrustManager jvmTrustManager = getJvmTrustManager();
			
			compositeKeyManager = new CompositeX509KeyManager();
			KeyManager[] keyManagers = {
				compositeKeyManager
			};
			compositeKeyManager.addKeyManager(jvmKeyManager);
			
			compositeTrustManager = new CompositeX509TrustManager();
			TrustManager[] trustManagers = {
				compositeTrustManager
			};
			compositeTrustManager.addTrustManager(jvmTrustManager);
			
			context = SSLContext.getInstance("SSL");
			context.init(keyManagers, trustManagers, null);
			SSLContext.setDefault(context);
		} catch (NoSuchAlgorithmException | KeyManagementException | UnrecoverableKeyException
				| KeyStoreException e) {
			LoggerFactory.getLogger(getClass()).error("Could not initialize SSL context", e);
		}
	}
	
	private X509KeyManager getJvmKeyManager()
		throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException{
		KeyManagerFactory factory =
			KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		factory.init(null, null);
		return (X509KeyManager) factory.getKeyManagers()[0];
	}
	
	private X509TrustManager getJvmTrustManager()
		throws NoSuchAlgorithmException, KeyStoreException{
		TrustManagerFactory factory =
			TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init((KeyStore) null);
		return (X509TrustManager) factory.getTrustManagers()[0];
	}
	
	@Override
	public Optional<KeyStore> loadKeyStore(InputStream keystoreInput, String keystorePass,
		String algorithm){
		try {
			KeyStore keyStore = KeyStore.getInstance(algorithm);
			keyStore.load(keystoreInput, keystorePass.toCharArray());
			return Optional.of(keyStore);
		} catch (NoSuchAlgorithmException | CertificateException | IOException
				| KeyStoreException e) {
			LoggerFactory.getLogger(getClass()).error("Could not load key store", e);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<KeyStore> loadKeyStore(String keystorePath, String truststorePass,
		String algorithm){
		try (FileInputStream input = new FileInputStream(keystorePath)) {
			return loadKeyStore(input, truststorePass, algorithm);
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).error("Could not load key store", e);
		}
		return Optional.empty();
	}
	
	@Override
	public void addTrustStore(KeyStore keyStore){
		try {
			TrustManagerFactory factory =
				TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			factory.init(keyStore);
			TrustManager[] managers = factory.getTrustManagers();
			for (TrustManager trustManager : managers) {
				if (trustManager instanceof X509TrustManager) {
					compositeTrustManager.addTrustManager((X509TrustManager) trustManager);
				}
			}
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			LoggerFactory.getLogger(getClass()).error("Could not add trust store", e);
		}
	}
	
	@Override
	public void addKeyStore(KeyStore keyStore, String keystorePass){
		compositeKeyManager.addKeyStore(keyStore, keystorePass);
	}
	
	@Override
	public void removeKeyStore(KeyStore keyStore){
		compositeKeyManager.removeKeyStore(keyStore);
	}
}
