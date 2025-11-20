package ch.elexis.core.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;

public interface IAppointmentService {

	/**
	 * Create a transient clone of the provided {@link IAppointment}.
	 *
	 * @param appointment
	 * @return
	 */
	public IAppointment clone(IAppointment appointment);

	/**
	 * Delete the {@link IAppointment}, checks for recurrence. If the
	 * {@link IAppointment} has recurrence information with the whole parameter
	 * either the whole series is deleted or just the appointment keeping the series
	 * in tact.
	 *
	 * @param appointment
	 * @param whole
	 * @return
	 */
	public boolean delete(IAppointment appointment, boolean whole);

	/**
	 * Contains the configured block times for a given schedule. Every day is
	 * populated, either by the configured setting, or the default.
	 *
	 * @param schedule to lookup
	 * @return the block times for each day. Each entry per day contains slots like
	 *         "0000-0800"
	 * @since 3.10
	 */
	public Map<DayOfWeek, String[]> getConfiguredBlockTimesBySchedule(String schedule);

	/**
	 * Asserts that the blocked times for a given schedule and date are set.
	 *
	 * @param date     to assert on
	 * @param schedule the schedule to assert for, may be <code>null</code> (which
	 *                 will assert block times for all configured areas/schedules
	 *                 for the given day)
	 * @since 3.10 renamed from updateBoundaries, changed signature
	 */
	public void assertBlockTimes(LocalDate date, @Nullable String schedule);

	/**
	 * Get the configured type string for the specified {@link AppointmentType}.
	 *
	 * @param type
	 * @return
	 */
	public String getType(AppointmentType type);

	/**
	 * Get the configured state string for the specified {@link AppointmentState}.
	 *
	 * @param type
	 * @return
	 */
	public String getState(AppointmentState state);

	/**
	 * Add the type string to the list of configured types.
	 *
	 * @param periodType
	 */
	public void addType(String type);

	/**
	 * Add the state string to the list of configured states.
	 *
	 * @param periodType
	 */
	public void addState(String state);

	/**
	 * Get all defined areas.
	 *
	 * @return
	 */
	public List<Area> getAreas();

	/**
	 *
	 * @param schedule
	 * @return the area for the given name/id or <code>null</code>
	 * @since 3.9
	 */
	public Area getAreaByNameOrId(String nameOrId);

	/**
	 * Define the {@link AreaType} of an area
	 *
	 * @param area     the id of the area
	 * @param areaType the type to define
	 * @param value    to set, for {@link AreaType#CONTACT} the
	 *                 {@link IContact#getId()}
	 * @since 3.10
	 */
	public void setAreaType(String area, AreaType areaType, String value);

	/**
	 * Get all configured type strings
	 *
	 * @return
	 */
	public List<String> getTypes();

	/**
	 * Get all configured state strings. The first element (idx: 0) is considered to denote empty or blocked,
	 * the second element (idx: 1) is considered to be the default state, according to {@link AppointmentState}
	 *
	 * @return
	 */
	public List<String> getStates();

	/**
	 * Get the appointment type color configured for the given userContact. Default
	 * is #3a87ad.
	 *
	 * @param userContact     if <code>null</code>, take user from context
	 * @param appointmentType the value of {@link IAppointment#getType()}
	 * @return
	 * @since 3.10
	 */
	public String getContactConfiguredTypeColor(IContact userContact, String appointmentType);

	/**
	 * Get the appointment state color configured for the given userContact. Default
	 * is #ffffff.
	 *
	 * @param userContact      if <code>null</code>, take user from context
	 * @param appointmentState the value of {@link IAppointment#getState()}
	 * @return
	 */
	public String getContactConfiguredStateColor(IContact userContact, String appointmentState);

	/**
	 * Get the {@link IAppointmentSeries} representation of the
	 * {@link IAppointment}. Returns empty if {@link IAppointment#isRecurring()} is
	 * not true.
	 *
	 * @param termin
	 * @return
	 */
	public Optional<IAppointmentSeries> getAppointmentSeries(IAppointment appointment);

	/**
	 * Create a new {@link IAppointmentSeries} without persisting.
	 *
	 * @return
	 */
	public IAppointmentSeries createAppointmentSeries();

	/**
	 * Persist the {@link IAppointmentSeries}, resulting in a list of
	 * {@link IAppointment} instances.
	 *
	 * @return
	 */
	public List<IAppointment> saveAppointmentSeries(IAppointmentSeries appointmentSeries);

	/**
	 * If the {@link IAppointmentSeries} is persistent, all {@link IAppointment}
	 * instances of the series are deleted.
	 *
	 * @param appointment
	 */
	public void deleteAppointmentSeries(IAppointmentSeries appointmentSeries);

	/**
	 * Test if the appointment is colliding with other appointments on the same day,
	 * or in case of an {@link IAppointmentSeries} with other appointments of type
	 * {@link AppointmentType#BOOKED} on all days of the series.
	 * 
	 * @param appointment
	 * @return
	 */
	public boolean isColliding(IAppointment appointment);

	/**
	 * Get a map with the configured preferred durations with appointment type as
	 * key.
	 *
	 * @param areaName
	 * @return
	 */
	public Map<String, Integer> getPreferredDurations(String areaName);

	/**
	 * Resolves the contact this area is allocated to. That is: If the the given
	 * area is a contact allocated area, the respective allocated contact is
	 * returned.
	 *
	 * @param localObject
	 * @return
	 * @see AreaType
	 * @since 3.9
	 */
	Optional<IContact> resolveAreaAssignedContact(String areaName);

	/**
	 * Resolves the areadId allocated to a specific contact. Reverse operation to
	 * {@link #resolveAreaAssignedContact(String)}
	 *
	 * @param contact
	 * @return the areaId allocated to this contact, <code>null</code> if not
	 *         available
	 * @since 3.10
	 */
	String resolveAreaByAssignedContact(IContact contact);

	/**
	 * Get all areas that are {@link AreaType#GENERIC} or have a contactId that is
	 * part of the {@link IAccessControlService#getAoboMandatorIds()} list.
	 * 
	 * @return
	 */
	List<Area> getAoboAreas();

	public void addArea(String name);

	/**
	 * Get all {@link IAppointment}s of the provided day of the provided schedule.
	 * If includeTransientFree is true, transient {@link IAppointment}s are included
	 * for times in between persistent {@link IAppointment}s.
	 * 
	 * @param schedule
	 * @param day
	 * @param includeTransientFree
	 * @return
	 */
	public List<IAppointment> getAppointments(String schedule, LocalDate day, boolean includeTransientFree);

	/**
	 * Creates linked (combination) appointments for a given main appointment, if
	 * applicable, based on the configured Kombitermine definitions.
	 * <p>
	 * The created appointments are <b>not persisted</b>; they must be saved
	 * manually by the caller. If the main appointment already has linked
	 * appointments, this method returns an empty list.
	 * </p>
	 *
	 * @param mainAppointment The main (root) appointment.
	 * @param patient         Optional patient (if available). Used to assign
	 *                        patient information to generated Kombi appointments.
	 * @param type            The selected appointment type used to look up Kombi
	 *                        definitions.
	 * @param freetext        Free-text name used when no {@link IContact} (patient)
	 *                        is assigned.
	 * @return A list of newly created, <b>unsaved</b> linked Kombi appointments.
	 *         Returns an empty list if no definitions exist or if the appointment
	 *         is already linked.
	 */
	public List<IAppointment> getKombiTermineIfApplicable(IAppointment mainAppointment, IContact patient,
			String type, String freetext);

	/**
	 * Returns only those appointments from the given list that overlap with
	 * existing appointments in the agenda.
	 *
	 * @param appointments the (virtual) appointments to check
	 * @return all appointments that would collide; never {@code null}
	 */
	public List<IAppointment> getCollidingAppointments(List<IAppointment> appointments);
}
