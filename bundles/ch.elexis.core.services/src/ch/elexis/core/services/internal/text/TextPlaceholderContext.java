package ch.elexis.core.services.internal.text;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class TextPlaceholderContext implements IContext {

	private ConcurrentHashMap<String, Object> context;

	private IContext parent;

	public TextPlaceholderContext(Identifiable identifiable) {
		context = new ConcurrentHashMap<>();
		setTyped(identifiable);
	}

	@Override
	public String getStationIdentifier() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz) {
		Optional<T> ret = Optional.ofNullable((T) context.get(clazz.getName()));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getTyped(clazz);
		}
		return ret;
	}

	@Override
	public void setTyped(Object object) {
		if (object != null) {
			if (object instanceof IUser) {
				// also set active user contact
				IContact userContact = ((IUser) object).getAssignedContact();
				setNamed(ACTIVE_USERCONTACT, userContact);
				// try to set default mandator
				Optional<IUserService> userService = OsgiServiceUtil.getService(IUserService.class);
				if (userService.isPresent()) {
					Optional<IMandator> defaultMandator = userService.get()
							.getDefaultExecutiveDoctorWorkingFor((IUser) object);
					if (defaultMandator.isPresent()) {
						setTyped(defaultMandator.get());
					}
				}
			}
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (object.equals(context.get(modelInterface.get().getName()))) {
				// object is already in the context do nothing otherwise loop happens
				return;
			}
			if (modelInterface.isPresent()) {
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
		}
	}

	private Optional<Class<?>> getModelInterface(Object object) {
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model")
					&& !interfaze.getName().contains("Identifiable")) {
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}

	@Override
	public void removeTyped(Class<?> clazz) {
		context.remove(clazz.getName());
	}

	@Override
	public Optional<?> getNamed(String name) {
		Optional<?> ret = Optional.ofNullable(context.get(name));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getNamed(name);
		}
		return ret;
	}

	@Override
	public void setNamed(String name, Object object) {
		if (object == null) {
			context.remove(name);
		} else if (object.equals(context.get(name))) {
			// object is already in the context do nothing otherwise loop happens
			return;
		} else {
			context.put(name, object);
		}
	}

	public void setParent(IContext parent) {
		this.parent = parent;
	}
}
