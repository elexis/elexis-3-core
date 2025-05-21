/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - #2112
 *******************************************************************************/
package ch.elexis.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.events.MessageEvent;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

/**
 * Ein Anwender ist eine Person (und damit auch ein Kontakt), die zusätzlich das
 * Recht hat, diese Software zu benützen. Ein Anwender hat Username und
 * Passwort, sowie ein AgendaLabel. Jeder Anwender gehört zu mindestens einer
 * Gruppe.
 *
 * Diese Klasse enthält ausserdem die statische Methode "login", mit der ein
 * Anwender sich anmelden kann.
 *
 * @author Gerry
 *
 */
public class Anwender extends Person {

	public static final String ADMINISTRATOR = "Administrator";

	public static final String FLD_LABEL = "Label"; // contains username
	public static final String FLD_JOINT_REMINDERS = "Reminders";
	public static final String FLD_EXTINFO_MANDATORS = "Mandant";
	public static final String FLD_EXTINFO_STDMANDATOR = "StdMandant";

	static {
		addMapping(Kontakt.TABLENAME, FLD_EXTINFO, Kontakt.FLD_IS_USER, FLD_LABEL + "=Bezeichnung3",
				FLD_JOINT_REMINDERS + "=JOINT:ReminderID:ResponsibleID:REMINDERS_RESPONSIBLE_LINK");
	}

	public Anwender(String Username, String Password) {
		this(Username, Password, false);
	}

	/**
	 *
	 * @param username
	 * @param password
	 * @param isExecutiveDoctor additionally assign the
	 *                          {@link RoleConstants#ACCESSCONTROLE_ROLE_MEDICAL_PRACTITIONER}
	 * @since 3.1
	 */
	public Anwender(String username, String password, boolean isExecutiveDoctor) {
		create(null);
		super.setConstraint();

		User user = new User(this, username, password);
		if (isExecutiveDoctor) {
			user.setAssignedRole(Role.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_USER), true);
			user.setAssignedRole(Role.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_PRACTITIONER), true);
		}
	}

	public Anwender(final String Name, final String Vorname, final String Geburtsdatum, final String s) {
		super(Name, Vorname, Geburtsdatum, s);
	}

	public static Anwender load(final String id) {
		Anwender ret = new Anwender(id);
		if (ret.state() > PersistentObject.INVALID_ID) {
			return ret;
		}
		return null;
	}

	/**
	 * Return a short or long label for this Anwender
	 *
	 * This implementation returns the "Label" field for both label types
	 *
	 * @return a label describing this Person
	 */
	@Override
	public String getLabel(final boolean shortLabel) {
		String l = get(FLD_LABEL);
		if (StringTool.isNothing(l)) {
			l = checkNull(get(Person.NAME)) + StringTool.space + checkNull(get(Person.FIRSTNAME));
			if (StringTool.isNothing(l)) {
				l = "unbekannt";
			}
		}
		return l;
	}

	/**
	 * Kurzname setzen. Zuerst prüfen, ob es wirklich ein neuer Name ist, um
	 * unnötigen Netzwerkverkehr zu vermeiden
	 */
	public void setLabel(final String label) {
		String oldlabel = getLabel();
		if (!label.equals(oldlabel)) {
			set(FLD_LABEL, label);
		}
	}

	/**
	 * Retrieve reminders for this {@link Anwender}
	 *
	 * @return a
	 */
	public SortedSet<Reminder> getReminders() {
		SortedSet<Reminder> ret = new TreeSet<>();
		List<String[]> rem = getList(FLD_JOINT_REMINDERS, (String[]) null);
		if (rem != null) {
			for (String[] l : rem) {
				ret.add(Reminder.load(l[0]));
			}
		}
		return ret;
	}

	/**
	 *
	 * @return
	 * @since 3.1
	 */
	public @NonNull List<Mandant> getExecutiveDoctorsWorkingFor() {
		List<Mandant> mandantenList = CoreHub.getMandantenList();
		String mandators = (String) getExtInfoStoredObjectByKey(FLD_EXTINFO_MANDATORS);
		if (mandators == null)
			return Collections.emptyList();

		List<String> man = Arrays.asList(mandators.split(","));
		return mandantenList.stream().filter(p -> man.contains(p.getLabel())).collect(Collectors.toList());
	}

	/**
	 *
	 * @param m
	 * @param checked <code>true</code> add or <code>false</code> remove
	 * @since 3.1
	 */
	public void addOrRemoveExecutiveDoctorWorkingFor(Mandant m, boolean checked) {
		HashSet<Mandant> hashSet = new HashSet<>(getExecutiveDoctorsWorkingFor());
		if (checked) {
			hashSet.add(m);
		} else {
			hashSet.remove(m);
		}
		List<String> edList = hashSet.stream().map(p -> p.getLabel()).collect(Collectors.toList());
		setExtInfoStoredObjectByKey(FLD_EXTINFO_MANDATORS, edList.isEmpty() ? StringUtils.EMPTY : ts(edList));
	}

	public void setStdExecutiveDoctorWorkingFor(Mandant m) {
		if (m == null) {
			setExtInfoStoredObjectByKey(FLD_EXTINFO_STDMANDATOR, null);
		} else {
			setExtInfoStoredObjectByKey(FLD_EXTINFO_STDMANDATOR, m.getLabel());
		}
	}

	public Mandant getStdExecutiveDoctorWorkingFor() {
		String stdMandator = (String) getExtInfoStoredObjectByKey(FLD_EXTINFO_STDMANDATOR);
		if (stdMandator != null && !stdMandator.isEmpty()) {
			List<Mandant> mandantenList = CoreHub.getMandantenList();
			for (Mandant mandant : mandantenList) {
				if (mandant.getLabel().equals(stdMandator)) {
					return mandant;
				}
			}
		}
		return null;
	}

	@Override
	protected String getConstraint() {
		return Kontakt.FLD_IS_USER + StringTool.equals + JdbcLink.wrap(StringConstants.ONE);
	}

	@Override
	protected void setConstraint() {
		set(new String[] { FLD_IS_USER, FLD_IS_PERSON }, new String[] { StringConstants.ONE, StringConstants.ONE });
	}

	protected Anwender() {/* leer */
	}

	protected Anwender(final String id) {
		super(id);
	}

	/**
	 * Initializes the first user to the system, the administrator
	 */
	protected static void initializeAdministratorUser(IAccessControlService accessControlService,
			IModelService coreModelService) {
		new User(); // compatibility - needed because of static db inits
		Anwender admin = new Anwender();
		admin.create(null);
		admin.set(new String[] { Person.NAME, FLD_LABEL, Kontakt.FLD_IS_USER }, ADMINISTRATOR, ADMINISTRATOR,
				StringConstants.ONE);
		accessControlService.doPrivileged(() -> {
			Optional<IUser> user = coreModelService.load(ADMINISTRATOR, IUser.class);
			if (user.isPresent()) {
				user.get().setAssignedContact(coreModelService.load(admin.getId(), IContact.class).orElse(null));
				user.get().removeRole(
						coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_USER, IRole.class).get());
				coreModelService.save(user.get());
			} else {
				throw new IllegalStateException("Incorrect DB state - No admin user found!");
			}
		});
	}

	/**
	 * @since 3.1
	 */
	public static void logoff() {
		CoreHub.logoffAnwender();
	}

	/**
	 * Sets the initial {@link Mandant}. Should be used carefully. Only
	 * {@link CoreHub} use it after login.
	 * 
	 * @deprecated initial {@link IMandator} is set by the {@link IContextService}
	 *             impl.
	 */
	@Deprecated
	public void setInitialMandator() {
		Mandant initialMandator = null;
		List<Mandant> workingFor = getExecutiveDoctorsWorkingFor();
		if (workingFor != null && !workingFor.isEmpty()) {
			Mandant stdWorkingFor = getStdExecutiveDoctorWorkingFor();
			initialMandator = workingFor.get(0);
			for (Mandant mandant : workingFor) {
				if (mandant.equals(this)) {
					initialMandator = mandant;
					break;
				}
				if (stdWorkingFor != null && mandant.equals(stdWorkingFor)) {
					initialMandator = stdWorkingFor;
					break;
				}
			}
		}
		if (initialMandator != null) {
			CoreHub.setMandant(initialMandator);
		} else {
			Mandant m = Mandant.load(CoreHub.getLoggedInContact().getId());
			if ((m != null) && m.isValid()) {
				CoreHub.setMandant(m);
			} else {
				List<Mandant> ml = new Query<Mandant>(Mandant.class).execute();
				if ((ml != null) && (!ml.isEmpty())) {
					m = ml.get(0);
					CoreHub.setMandant(m);

				} else {
					MessageEvent.fireError("Kein Mandant definiert",
							"Sie können Elexis erst normal benutzen, wenn Sie mindestens einen Mandanten definiert haben");
				}
			}
		}
	}

	/**
	 *
	 * @return <code>true</code> if {@link Anwender} is also {@link Mandant}
	 * @since 3.1
	 */
	public boolean isExecutiveDoctor() {
		Mandant m = Mandant.load(getId());
		return (m.exists() && m.isValid());
	}

	/**
	 *
	 * @param value <code>true</code> to define as {@link Mandant}, else
	 *              <code>false</code>
	 * @since 3.1
	 */
	public void setExecutiveDoctor(boolean value) {
		set(FLD_IS_MANDATOR, ts(value));
	}
}
