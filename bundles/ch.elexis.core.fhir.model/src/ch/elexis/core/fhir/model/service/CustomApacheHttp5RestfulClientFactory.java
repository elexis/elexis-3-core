package ch.elexis.core.fhir.model.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.apache.ApacheHttp5RestfulClientFactory;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class CustomApacheHttp5RestfulClientFactory extends ApacheHttp5RestfulClientFactory {

	private Call.Factory myNativeClient;

	public CustomApacheHttp5RestfulClientFactory(FhirContext context) {
		super(context);
	}

	@Override
	protected void resetHttpClient() {
		myNativeClient = null;
	}

	/**
	 * Only accepts clients of type {@link OkHttpClient}
	 *
	 * @param okHttpClient
	 */
	@Override
	public void setHttpClient(Object okHttpClient) {
		myNativeClient = (Call.Factory) okHttpClient;
	}

//	@Override
//	public synchronized Factory getNativeClient() {
//		if (myNativeClient == null) {
//			myNativeClient = new OkHttpClient().newBuilder().connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
//					.connectionPool(new ConnectionPool(15, 5000, TimeUnit.MILLISECONDS))
//					.readTimeout(getSocketTimeout(), TimeUnit.MILLISECONDS)
//					.writeTimeout(getSocketTimeout(), TimeUnit.MILLISECONDS).build();
//		}
//
//		return myNativeClient;
//	}

}
