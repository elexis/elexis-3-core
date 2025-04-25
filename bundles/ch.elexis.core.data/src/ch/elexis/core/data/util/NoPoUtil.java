package ch.elexis.core.data.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;

/**
 * Utility class for compatibility between the Elexis persistence
 * implementations {@link PersistentObject} and JPA based {@link Identifiable}.
 *
 * @author thomas
 *
 */
public class NoPoUtil {

	private static PersistentObjectFactory poFactory = new PersistentObjectFactory();

	/**
	 * Load {@link PersistentObject} implementation for the provided
	 * {@link Identifiable}. If loading via {@link PersistentObjectFactory} and the
	 * {@link IStoreToStringService} does not load an instanceof clazz, the load
	 * method of the clazz.
	 * 
	 * @param identifiable
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T loadAsPersistentObject(Identifiable identifiable, Class<T> clazz) {
		PersistentObject loaded = loadAsPersistentObject(identifiable);
		if (loaded != null && !clazz.isAssignableFrom(loaded.getClass())) {
			return load(loaded, clazz);
		}
		return (T) loaded;
	}

	@SuppressWarnings("unchecked")
	private static <T> T load(PersistentObject loaded, Class<T> clazz) {
		try {
			Method load = clazz.getMethod("load", new Class[] { String.class }); //$NON-NLS-1$
			return (T) (load.invoke(null, new Object[] { loaded.getId() }));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			LoggerFactory.getLogger(NoPoUtil.class)
					.warn("Could not load [" + loaded + "] as [" + clazz + "]", e);
		}
		return null;
	}

	/**
	 * Load {@link PersistentObject} implementation for the provided
	 * {@link Identifiable} using the {@link PersistentObjectFactory} and the
	 * {@link IStoreToStringService}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public static PersistentObject loadAsPersistentObject(Identifiable identifiable) {
		return loadAsPersistentObject(identifiable, true);
	}

	/**
	 * Load {@link PersistentObject} implementation for the provided
	 * {@link Identifiable} using the {@link PersistentObjectFactory} and the
	 * {@link IStoreToStringService}.
	 * 
	 * @param identifiable
	 * @param throwException
	 * @return
	 */
	public static PersistentObject loadAsPersistentObject(Identifiable identifiable, boolean throwException) {
		if (identifiable != null) {
			Optional<String> storeToString = StoreToStringServiceHolder.get().storeToString(identifiable);
			if (storeToString.isPresent()) {
				PersistentObject ret = poFactory.createFromString(storeToString.get());
				if (ret != null) {
					return ret;
				}
			}
			if (throwException) {
				throw new IllegalStateException("Could not load [" + identifiable + "] [" + storeToString.orElse("?")
						+ "] as PersistentObject");
			}
		}
		return null;
	}

	/**
	 * Load {@link PersistentObject} implementations for the provided
	 * {@link Identifiable}s using the {@link PersistentObjectFactory} and the
	 * {@link IStoreToStringService}.
	 *
	 * @param identifiable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> loadAsPersistentObject(List<Identifiable> identifiables, Class<T> type) {
		if (identifiables != null && !identifiables.isEmpty()) {
			List<T> ret = new ArrayList<>();
			for (Identifiable identifiable : identifiables) {
				ret.add((T) loadAsPersistentObject(identifiable, true));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	/**
	 * Load {@link PersistentObject} implementations for the provided
	 * {@link Identifiable}s using the {@link PersistentObjectFactory} and the
	 * {@link IStoreToStringService}.
	 *
	 * @param identifiable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] loadAsPersistentObject(Identifiable[] identifiables, Class<T> type) {
		if (identifiables != null && identifiables.length > 0) {
			T[] ret = (T[]) Array.newInstance(type, identifiables.length);
			for (int i = 0; i < identifiables.length; i++) {
				ret[i] = (T) loadAsPersistentObject(identifiables[i], true);
			}
			return ret;
		}
		return (T[]) Array.newInstance(type, 0);
	}

	/**
	 * Load {@link Identifiable} implementations for the provided
	 * {@link PersistentObject}s using the core {@link IModelService} and fallback
	 * to {@link IStoreToStringService}.
	 *
	 * @param <T>
	 * @param persistentObjects
	 * @param type
	 * @return
	 */
	public static <T> List<T> loadAsIdentifiable(List<? extends PersistentObject> persistentObjects, Class<T> type) {
		if (persistentObjects != null && !persistentObjects.isEmpty()) {
			List<T> ret = new ArrayList<>();
			for (PersistentObject persistentObject : persistentObjects) {
				loadAsIdentifiable(persistentObject, type).ifPresent(identifiable -> {
					ret.add(identifiable);
				});
			}
			return ret;
		}
		return Collections.emptyList();
	}

	/**
	 * Load {@link Identifiable} implementation for the provided
	 * {@link PersistentObject} using the core {@link IModelService} and fallback to
	 * {@link IStoreToStringService}.
	 *
	 * @param <T>
	 * @param persistentObject
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Optional<T> loadAsIdentifiable(PersistentObject persistentObject, Class<T> type) {
		if (persistentObject != null) {
			Optional<T> loaded = CoreModelServiceHolder.get().load(persistentObject.getId(), type);
			if (loaded.isPresent()) {
				return loaded;
			} else {
				loaded = (Optional<T>) StoreToStringServiceHolder.get()
						.loadFromString(persistentObject.storeToString());
				if (loaded.isPresent()) {
					return loaded;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Refresh {@link Identifiable} instances of the {@link PersistentObject}.
	 *
	 * @param persistentObject
	 */
	public static void refreshIdentifiable(PersistentObject persistentObject) {
		Optional<Identifiable> loaded = StoreToStringServiceHolder.get()
				.loadFromString(persistentObject.storeToString());
		loaded.ifPresent(i -> {
			CoreModelServiceHolder.get().refresh(i, true);
		});
	}

	/**
	 * Get a database search String for a Elexis date database value. <br />
	 * Used for S:D: mapped values in Query#add, copied and slightly adapted.
	 *
	 * @param value
	 * @return
	 */
	public static String getElexisDateSearchString(String value) {
		StringBuilder sb = null;
		String ret = value.replaceAll("%", StringUtils.EMPTY);
		final String filler = "%%%%%%%%";
		// are we looking for the year?
		if (ret.matches("[0-9]{3,}")) {
			sb = new StringBuilder(ret);
			sb.append(filler);
			ret = sb.substring(0, 8);
		} else {
			// replace single digits as in 1.2.1932 with double digits
			// as in 01.02.1932
			int dotCount = StringUtils.countMatches(ret, ".");
			String[] parts = ret.split("\\.");
			StringJoiner sj = new StringJoiner(StringUtils.EMPTY);
			for (String string : parts) {
				if (string.length() == 1 && Character.isDigit(string.charAt(0))) {
					sj.add("0" + string);
				} else {
					sj.add(string);
				}
			}
			// remove dots
			sb = new StringBuilder(sj.toString());
			int lengthNoDots = sb.length();
			// String must consist of 8 or more digits (ddmmYYYY)
			sb.append(filler);
			if (dotCount == 1 && lengthNoDots == 6) {
				// convert to YYYYmmdd format
				ret = sb.substring(2, 6) + sb.substring(0, 2) + sb.substring(6, 8);
			} else {
				// convert to YYYYmmdd format
				ret = sb.substring(4, 8) + sb.substring(2, 4) + sb.substring(0, 2);
			}
		}
		return ret;
	}
}
