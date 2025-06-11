package ch.elexis.core.fhir.model.service;

import java.util.concurrent.TimeUnit;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClientFactory;
import okhttp3.Call;
import okhttp3.Call.Factory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class CustomOkHttpRestfulClientFactory extends OkHttpRestfulClientFactory {

	private Call.Factory myNativeClient;

	public CustomOkHttpRestfulClientFactory(FhirContext context) {
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

	@Override
	public synchronized Factory getNativeClient() {
		if (myNativeClient == null) {
			myNativeClient = new OkHttpClient().newBuilder().connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
					.connectionPool(new ConnectionPool(15, 5000, TimeUnit.MILLISECONDS))
					.readTimeout(getSocketTimeout(), TimeUnit.MILLISECONDS)
					.writeTimeout(getSocketTimeout(), TimeUnit.MILLISECONDS).build();
		}

		return myNativeClient;
	}
}
