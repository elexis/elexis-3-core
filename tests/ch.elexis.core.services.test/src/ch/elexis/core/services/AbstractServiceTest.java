package ch.elexis.core.services;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class AbstractServiceTest {
	
	static IModelService coreModelService = AllServiceTests.getModelService();
	
	/**
	 * Accept all HTTPS certificates (used for servers running a self-signed certificate)
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static void acceptAllCerts() throws NoSuchAlgorithmException, KeyManagementException{
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers(){
					return null;
				}
				
				public void checkClientTrusted(X509Certificate[] certs, String authType){}
				
				public void checkServerTrusted(X509Certificate[] certs, String authType){}
			}
		};
		
		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		
		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session){
				return true;
			}
		};
		
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		
	}
}
