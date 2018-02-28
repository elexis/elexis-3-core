package ch.elexis.core.data.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.X509TrustManager;

public class CompositeX509TrustManager implements X509TrustManager {
	
	private final Set<X509TrustManager> trustManagers = new HashSet<X509TrustManager>();
	
	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType)
		throws CertificateException{
		for (X509TrustManager trustManager : trustManagers) {
			try {
				trustManager.checkClientTrusted(chain, authType);
				return; // someone trusts them. success!
			} catch (CertificateException e) {
				// maybe someone else will trust them
			}
		}
		throw new CertificateException("None of the TrustManagers trust this certificate chain");
	}
	
	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType)
		throws CertificateException{
		for (X509TrustManager trustManager : trustManagers) {
			try {
				trustManager.checkServerTrusted(chain, authType);
				return; // someone trusts them. success!
			} catch (CertificateException e) {
				// maybe someone else will trust them
			}
		}
		throw new CertificateException("None of the TrustManagers trust this certificate chain");
	}
	
	@Override
	public X509Certificate[] getAcceptedIssuers(){
		List<X509Certificate> ret = new ArrayList<>();
		for (X509TrustManager trustManager : trustManagers) {
			ret.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
		}
		return ret.toArray(new X509Certificate[ret.size()]);
	}
	
	public void addTrustManager(X509TrustManager trustManager){
		trustManagers.add(trustManager);
	}
	
}
