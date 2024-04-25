package ch.elexis.core.model.util.internal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Config;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IDiagnosisReference;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.service.CoreModelAdapterFactory;
import ch.elexis.core.model.service.holder.ContextServiceHolder;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IStoreToStringContribution;

/**
 * Utility class with core model specific methods
 *
 * @author thomas
 *
 */
public class ModelUtil {

	private static Logger logger = LoggerFactory.getLogger(ModelUtil.class);

	private static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	/**
	 * Test if there is a matching {@link Config} entry with a value that can be
	 * interpreted as true. If no {@link Config} is present defaultValue is
	 * returned.
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isConfig(String key, boolean defaultValue) {
		Optional<IConfig> loaded = CoreModelServiceHolder.get().load(key, IConfig.class);
		if (loaded.isPresent()) {
			String value = loaded.get().getValue();
			return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
		} else {
			return defaultValue;
		}
	}

	/**
	 * Test if there is a matching {@link Userconfig} entry for the owner, with a
	 * value that can be interpreted as true. If no {@link Userconfig} entry is
	 * present defaultValue is returned.
	 *
	 * @param owner
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isUserConfig(IContact owner, String key, boolean defaultValue) {
		if (owner != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get().getNamedQuery(IUserConfig.class, true,
					"ownerid", "param");
			List<IUserConfig> configs = configQuery
					.executeWithParameters(configQuery.getParameterMap("ownerid", owner.getId(), "param", key));
			if (configs.isEmpty()) {
				return defaultValue;
			} else {
				IConfig config = configs.get(0);
				if (configs.size() > 1) {
					logger.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = config.getValue();
				return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
			}
		}
		logger.warn("No user contact for query of key [" + key + "] returning default");
		return defaultValue;
	}

	/**
	 * Get a matching {@link Config} entry and return its value. If no
	 * {@link Config} is present defaultValue is returned.
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getConfig(String key, String defaultValue) {
		IQuery<IConfig> configQuery = CoreModelServiceHolder.get().getQuery(IConfig.class);
		configQuery.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.EQUALS, key);
		List<IConfig> configs = configQuery.execute();
		if (configs.isEmpty()) {
			return defaultValue;
		} else {
			IConfig config = configs.get(0);
			if (configs.size() > 1) {
				logger.warn("Multiple config entries for [" + key + "] using first.");
			}
			return config.getValue();
		}
	}

	/**
	 * Get the active {@link IContact} of the active {@link IUser} from the root
	 * {@link IContext}.
	 *
	 * @return
	 */
	public static Optional<IContact> getActiveUserContact() {
		if (ContextServiceHolder.isPresent()) {
			Optional<IContact> ret = ContextServiceHolder.get().getActiveUserContact();
			if (ret.isPresent()) {
				return ret;
			} else {
				Optional<IUser> user = ContextServiceHolder.get().getActiveUser();
				if (user.isPresent()) {
					return Optional.ofNullable(user.get().getAssignedContact());
				}
			}
		} else {
			logger.warn("No IContextService available.");
		}
		return Optional.empty();
	}

	/**
	 * Get a {@link IQuery} instance for the provided interfaceClazz.
	 *
	 * @param interfaceClazz
	 * @return
	 */
	public static <T> IQuery<T> getQuery(Class<T> interfaceClazz) {
		return CoreModelServiceHolder.get().getQuery(interfaceClazz);
	}

	/**
	 * Load the object using the core model service
	 *
	 * @param objectId
	 * @param clazz
	 * @return
	 */
	public static <T> T load(String objectId, Class<T> clazz) {
		Optional<T> ret = CoreModelServiceHolder.get().load(objectId, clazz);
		return ret.orElse(null);
	}

	/**
	 * Wrap the entity in a new ModelAdapter matching the provided type clazz. If
	 * entity is null, null is returned.
	 *
	 * @param entity
	 * @param clazz
	 * @return
	 */
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz) {
		return getAdapter(entity, clazz, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz, boolean registerEntityChangeEvent) {
		if (entity != null) {
			Optional<Identifiable> adapter = CoreModelAdapterFactory.getInstance().getModelAdapter(entity, clazz, true,
					registerEntityChangeEvent);
			return (T) adapter.orElse(null);
		}
		return null;
	}

	/**
	 * verify whether the proposed username is not already in use
	 *
	 * @param username
	 * @return <code>true</code> if the given username may be used
	 */
	public static boolean verifyUsernameNotTaken(String username) {
		return !CoreModelServiceHolder.get().load(username, IUser.class).isPresent();
	}

	public static AbstractModelService getModelService() {
		return (AbstractModelService) CoreModelServiceHolder.get();
	}

	public static Optional<Identifiable> getFromStoreToString(String storeToString) {
		return StoreToStringServiceHolder.get().loadFromString(storeToString);
	}

	public static Optional<String> getStoreToString(Identifiable identifiable) {
		return StoreToStringServiceHolder.get().storeToString(identifiable);
	}

	public static IDiagnosisReference getOrCreateDiagnosisReference(IDiagnosis diagnosis) {
		Optional<String> storeToString = StoreToStringServiceHolder.get().storeToString(diagnosis);
		if (storeToString.isPresent()) {
			String[] parts = storeToString.get().split(IStoreToStringContribution.DOUBLECOLON);
			INamedQuery<IDiagnosisReference> query = CoreModelServiceHolder.get()
					.getNamedQuery(IDiagnosisReference.class, true, "code", "diagnosisClass");
			List<IDiagnosisReference> existing = query.executeWithParameters(
					query.getParameterMap("code", diagnosis.getCode(), "diagnosisClass", parts[0]));
			if (!existing.isEmpty()) {
				return existing.get(0);
			} else {
				IDiagnosisReference reference = CoreModelServiceHolder.get().create(IDiagnosisReference.class);
				reference.setCode(diagnosis.getCode());
				reference.setReferredClass(parts[0]);
				reference.setText(diagnosis.getText());
				CoreModelServiceHolder.get().save(reference);
				return reference;
			}
		}
		return null;
	}

	public static String toString(LocalDate date) {
		if (date == null) {
			return null;
		}

		return date.format(yyyyMMdd);
	}

	public static LocalDate toLocalDate(String dateValue) {
		if (dateValue == null || dateValue.isEmpty()) {
			return null;
		}

		try {
			return LocalDate.parse(dateValue, yyyyMMdd);
		} catch (DateTimeParseException e) {
			logger.warn("Error parsing [{}]", dateValue, e);
		}
		return null;
	}

	public static String getPersonalia(Kontakt kontakt) {
		StringBuilder sb = new StringBuilder(64);
		if (kontakt != null) {
			if (StringUtils.isNoneEmpty(kontakt.getDescription1())) {
				sb.append(kontakt.getDescription1());
			}
			if (StringUtils.isNotBlank(sb.toString())) {
				sb.append(StringUtils.SPACE);
			}
			if (StringUtils.isNoneEmpty(kontakt.getDescription2())) {
				sb.append(kontakt.getDescription2());
			}

			if (kontakt.getDob() != null) {
				sb.append(StringUtils.SPACE).append(defaultDateFormatter.format(kontakt.getDob()));
			}

			if (StringUtils.isNoneEmpty(kontakt.getTitel())) {
				sb.append(",").append(kontakt.getTitel());
			}
		}
		return sb.toString();
	}
}
