package ch.elexis.core.fhir.model.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BearerTokenAuthInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.fhir.model.adapter.ElexisTypeMap;
import ch.elexis.core.fhir.model.adapter.ModelAdapterFactory;
import ch.elexis.core.fhir.model.impl.AbstractFhirModelAdapter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IElexisServerService;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.elexis.core.utils.CoreUtil;

@Component
public class FhirModelService implements IFhirModelService, IStoreToStringContribution {

	private static FhirContext context = FhirContext.forR4();

	@Reference
	private IElexisServerService elexisServer;

	@Reference
	private IContextService contextService;

	private IGenericClient client;

	private ModelAdapterFactory adapterFactory;

	@Activate
	public void activate() {
		adapterFactory = new ModelAdapterFactory();
	}

	private IGenericClient getGenericClient() {
		if (client == null) {
			context.setRestfulClientFactory(new CustomOkHttpRestfulClientFactory(context));

			client = context.newRestfulGenericClient(elexisServer.getConnectionUrl().replace("/services", "/fhir"));
			if (CoreUtil.isTestMode()) {
				// Create a logging interceptor
				LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
				loggingInterceptor.setLogRequestSummary(true);
				loggingInterceptor.setLogRequestBody(true);
				client.registerInterceptor(loggingInterceptor);
			}
			contextService.getTyped(AccessToken.class).ifPresent(accessToken -> {
				BearerTokenAuthInterceptor authInterceptor = new BearerTokenAuthInterceptor(accessToken.getToken());
				client.registerInterceptor(authInterceptor);
			});
		}
		return client;
	}

	@Override
	public <T> T create(Class<T> clazz) throws AccessControlException {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> adapt(Object fhirObject, Class<T> clazz) {
		Identifiable adapter = adapterFactory.createAdapter((BaseResource) fhirObject);
		if (adapter != null && clazz.isAssignableFrom(adapter.getClass())) {
			return Optional.of((T) adapter);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted) throws AccessControlException {
		if (StringUtils.isNotBlank(id)) {
			try {
				BaseResource fhirObject = getGenericClient().read()
						.resource(adapterFactory.getFhirType((Class<? extends Identifiable>) clazz)).withId(id)
						.execute();
				if (fhirObject != null) {
					return adapt(fhirObject, clazz);
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).warn(e.getMessage());
			}
		}
		return Optional.empty();
	}

	@Override
	public void save(Identifiable identifiable) throws AccessControlException {
		if (identifiable instanceof AbstractFhirModelAdapter) {
			getGenericClient().update().resource(((AbstractFhirModelAdapter<?>) identifiable).getFhirResource())
					.execute();
		}
	}

	@Override
	public void save(List<? extends Identifiable> identifiables) throws AccessControlException {
		if (identifiables != null) {
			identifiables.stream().forEach(i -> save(i));
		}
	}

	@Override
	public void delete(Identifiable identifiable) throws AccessControlException {
		if (identifiable instanceof AbstractFhirModelAdapter) {
			getGenericClient().delete()
					.resource(((AbstractFhirModelAdapter<?>) identifiable).getFhirResource())
					.execute();
		}
	}

	@Override
	public void delete(List<? extends Identifiable> identifiables) throws AccessControlException {
		if (identifiables != null) {
			identifiables.stream().forEach(i -> delete(i));
		}
	}

	@Override
	public void postEvent(String topic, Object object) {
		// TODO Auto-generated method stub

	}

	@Override
	public ConnectionStatus getConnectionStatus() {
		ConnectionStatus status = elexisServer.getConnectionStatus();
		if (status != null) {
			if (CoreUtil.isTestMode() || (elexisServer.getConnectionUrl() == null
					|| elexisServer.getConnectionUrl().contains("localhost"))) {
				return status;
			} else {
				Optional<AccessToken> accessToken = contextService.getTyped(AccessToken.class);
				if (accessToken.isPresent()) {
					return status;
				} else {
					LoggerFactory.getLogger(getClass()).warn("Elexis Server connected but no access token available");
				}
			}
		}
		return ConnectionStatus.LOCAL;
	}

	@Override
	public <T> List<T> getQueryResults(IQuery<IBaseBundle> query, Class<T> clazz) {
		Bundle results = query.returnBundle(Bundle.class).execute();
		if (results != null && results.hasEntry()) {
			return results.getEntry().stream().filter(e -> e.hasResource())
					.map(e -> adapt(e.getResource(), clazz).orElse(null)).filter(i -> i != null).toList();
		}
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> IQuery<IBaseBundle> getQuery(Class<T> clazz) {
		return getGenericClient().search()
				.forResource(adapterFactory.getFhirType((Class<? extends Identifiable>) clazz));
	}

	@Override
	public IQuery<IBaseBundle> getQuery(String byUrl) {
		return getGenericClient().search().byUrl(byUrl);
	}

	@Override
	public Optional<String> storeToString(Identifiable identifiable) {
		if (identifiable instanceof AbstractFhirModelAdapter) {
			AbstractFhirModelAdapter<?> fhirModelAdapter = (AbstractFhirModelAdapter<?>) identifiable;
			String classKey = ElexisTypeMap.getKeyForObject(fhirModelAdapter);
			if (classKey != null) {
				return Optional.of(classKey + StringConstants.DOUBLECOLON + identifiable.getId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Identifiable> loadFromString(String storeToString) {
		// leave loading from string to core model service
		return Optional.empty();
	}

	@Override
	public Class<?> getEntityForType(String type) {
		// leave to core model
		return null;
	}

	@Override
	public String getTypeForEntity(Object entityInstance) {
		// leave to core model
		return null;
	}

	@Override
	public String getTypeForModel(Class<?> interfaze) {
		// leave to core model
		return null;
	}
}
