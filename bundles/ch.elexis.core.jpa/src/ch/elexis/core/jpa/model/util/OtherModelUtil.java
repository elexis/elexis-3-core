package ch.elexis.core.jpa.model.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.utils.OsgiServiceUtil;

public class OtherModelUtil {

	private static List<String> otherModelPackages = List.of("ch.elexis.core.fhir.model");

	public static boolean isOtherModelPackage(Object value) {
		if (value != null) {
			String packageName = value.getClass().getPackageName();
			return otherModelPackages.stream().filter(mp -> packageName.startsWith(mp)).findFirst().isPresent();
		}
		return false;
	}

	public static Optional<AbstractIdModelAdapter<?>> getEntityModelAdapter(Object value) {
		if (value != null) {
			try {
				Method method = Arrays.asList(value.getClass().getMethods()).stream()
						.filter(m -> "toEntityModelAdapter".equals(m.getName())).findFirst().orElse(null);
				if (method != null) {
					return Optional.ofNullable((AbstractIdModelAdapter<?>) method.invoke(value));
				}
			} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(OtherModelUtil.class).error("Exception getting entity model adapter", e);
			}
		}
		return Optional.empty();
	}

	public static void save(Identifiable identifiable) {
		if (identifiable != null) {
			try {
				Method method = Arrays.asList(identifiable.getClass().getMethods()).stream()
						.filter(m -> "getModelServiceClass".equals(m.getName())).findFirst().orElse(null);
				if (method != null) {
					Class<?> modelSerivceClass = (Class<?>) method.invoke(identifiable);
					Method saveMethod = Arrays.asList(modelSerivceClass.getMethods()).stream()
							.filter(m -> "save".equals(m.getName())
									&& Arrays.asList(m.getParameterTypes()).contains(Identifiable.class))
							.findFirst().orElse(null);
					if (modelSerivceClass != null && saveMethod != null) {
						Optional<?> modelService = OsgiServiceUtil.getService(modelSerivceClass);
						if (modelService.isPresent()) {
							saveMethod.invoke(modelService.get(), identifiable);
						}
					}
				}
			} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(OtherModelUtil.class).error("Exception getting entity model adapter", e);
			}
		}
	}

	public static void delete(Identifiable identifiable) {
		if (identifiable != null) {
			try {
				Method method = Arrays.asList(identifiable.getClass().getMethods()).stream()
						.filter(m -> "getModelServiceClass".equals(m.getName())).findFirst().orElse(null);
				if (method != null) {
					Class<?> modelSerivceClass = (Class<?>) method.invoke(identifiable);
					Method saveMethod = Arrays.asList(modelSerivceClass.getMethods()).stream()
							.filter(m -> "delete".equals(m.getName())
									&& Arrays.asList(m.getParameterTypes()).contains(Identifiable.class))
							.findFirst().orElse(null);
					if (modelSerivceClass != null && saveMethod != null) {
						Optional<?> modelService = OsgiServiceUtil.getService(modelSerivceClass);
						if (modelService.isPresent()) {
							saveMethod.invoke(modelService.get(), identifiable);
						}
					}
				}
			} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(OtherModelUtil.class).error("Exception getting entity model adapter", e);
			}
		}

	}
}
