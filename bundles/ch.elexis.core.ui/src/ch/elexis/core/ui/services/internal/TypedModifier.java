package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.Set;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.events.MessageEvent;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.PatientConstants;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.services.IEncounterService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.PersistentObject;

public class TypedModifier {

	private IUserService userService;

	private IEncounterService encounterService;

	private ICoverageService coverageService;

	private ILocalLockService localLockService;

	private Context context;

	public TypedModifier(Context context) {
		this.context = context;
	}

	public void modifyFor(Object object) {
		if (object instanceof IPatient) {
			IPatient patient = (IPatient) object;
			Optional<IEncounter> latestEncounter = getEncounterService().getLatestEncounter((IPatient) object);
			if (latestEncounter.isPresent()) {
				context.setTyped(latestEncounter.get(), true);
				context.setTyped(latestEncounter.get().getCoverage(), true);
			} else {
				context.removeTyped(IEncounter.class);
				context.removeTyped(ICoverage.class);
			}
			setMandatorFromStammarztIfAllowed(patient);
		}
		if (object instanceof ICoverage) {
			Optional<IEncounter> latestEncounter = getCoverageService().getLatestEncounter((ICoverage) object);
			if (latestEncounter.isPresent()) {
				context.setTyped(latestEncounter.get(), true);
			} else {
				context.removeTyped(IEncounter.class);
			}
		}
		if (object instanceof IEncounter) {
			context.setTyped(((IEncounter) object).getCoverage(), true);
		}
		if (object instanceof IUser) {
			IUser user = (IUser) object;

			// also set active user contact
			IContact userContact = ((IUser) object).getAssignedContact();
			context.setNamed(IContext.ACTIVE_USERCONTACT, userContact);

			Optional<IMandator> defaultWorkingFor = getUserService().getDefaultExecutiveDoctorWorkingFor(user);
			if (defaultWorkingFor.isPresent()) {
				ContextServiceHolder.get().setActiveMandator(defaultWorkingFor.get());
			} else {
				Set<IMandator> workingFor = getUserService().getExecutiveDoctorsWorkingFor(user);
				if (!workingFor.isEmpty()) {
					ContextServiceHolder.get().setActiveMandator(workingFor.iterator().next());
				} else {
					MessageEvent.fireError("Kein Mandant definiert",
							"Sie können Elexis erst normal benutzen, wenn Sie für den Benutzer einen Mandanten definiert haben");
				}
			}
		}
	}

	public void modifyRemove(Class<?> clazz) {
		if (clazz.equals(IUser.class)) {
			ContextServiceHolder.get().setActivePatient(null);
			ContextServiceHolder.get().setActiveMandator(null);
		}
	}

	private IUserService getUserService() {
		if (userService == null) {
			userService = OsgiServiceUtil.getService(IUserService.class).get();
		}
		return userService;
	}

	private IEncounterService getEncounterService() {
		if (encounterService == null) {
			encounterService = OsgiServiceUtil.getService(IEncounterService.class).get();
		}
		return encounterService;
	}

	private ICoverageService getCoverageService() {
		if (coverageService == null) {
			coverageService = OsgiServiceUtil.getService(ICoverageService.class).get();
		}
		return coverageService;
	}

	private ILocalLockService getLocalLockService() {
		if (localLockService == null) {
			localLockService = OsgiServiceUtil.getService(ILocalLockService.class).get();
		}
		return localLockService;
	}

	public void releaseAndRefreshLock(Object object) {
		if (object instanceof Identifiable || object instanceof PersistentObject) {
			if (getLocalLockService().isLockedLocal(object)) {
				getLocalLockService().releaseLock(object);
			}
		}
	}

	/**
	 * Sets the active {@link IMandator} based on the assigned "Stammarzt" of the
	 * given {@link IPatient}, but only if the following conditions are met:
	 * <ul>
	 * <li>The user preference {@link Preferences#USR_AUTOMATIC_STAMMARZT_MANDANT}
	 * is enabled.</li>
	 * <li>The patient has a valid <code>ExtInfo</code> field referencing a known
	 * {@link IMandator} ID.</li>
	 * <li>The currently active {@link IUser} is authorized to act for that
	 * {@link IMandator} (via {@link IUserService}).</li>
	 * </ul>
	 * <p>
	 * If any condition is not met, no change to the context is performed.
	 * </p>
	 *
	 * @param patient the patient to evaluate, must not be {@code null}
	 */
	public void setMandatorFromStammarztIfAllowed(IPatient patient) {
		if (ConfigServiceHolder.getUser(Preferences.USR_AUTOMATIC_STAMMARZT_MANDANT, false) && patient != null) {
			Object stammarztExt = patient.getExtInfo(PatientConstants.FLD_EXTINFO_STAMMARZT);
			if (stammarztExt instanceof String stammarztId && !stammarztId.isBlank()) {
				java.util.Optional<IMandator> mandatorOpt = CoreModelServiceHolder.get().load(stammarztId,
						IMandator.class);
				mandatorOpt.ifPresent(mandator -> {
					java.util.Optional<IUser> userOpt = ContextServiceHolder.get().getActiveUser();
					if (userOpt.isPresent()) {
						IUser user = userOpt.get();
						Set<IMandator> userMandators = getUserService().getExecutiveDoctorsWorkingFor(user);
						if (userMandators.contains(mandator)) {
							ContextServiceHolder.get().setActiveMandator(mandator);
						}
					}
				});
			}
		}
  }
}
