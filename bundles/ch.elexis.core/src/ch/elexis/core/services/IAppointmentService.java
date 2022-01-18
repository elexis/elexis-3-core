package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	 * Updates the boundaries and check whether the only entries are appointments if
	 * yes also check whether some boundaries are missing
	 * 
	 * @param schedule
	 * @param date
	 */
	public void updateBoundaries(String schedule, LocalDate date);

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
	 * Define the {@link AreaType} of an area
	 * 
	 * @param area
	 *            the id of the area
	 * @param areaType
	 *            the type to define
	 * @param value
	 *            to set, for {@link AreaType#CONTACT} the {@link IContact#getId()}
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
	 * Get all configured state strings
	 * 
	 * @return
	 */
	public List<String> getStates();

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
}
