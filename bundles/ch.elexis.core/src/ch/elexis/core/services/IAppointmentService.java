package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.agenda.Area;
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
	 * Delete the {@link IAppointment}, checks for recurrence. If the {@link IAppointment} has
	 * recurrence information with the whole parameter either the whole series is deleted or just
	 * the appointment keeping the series in tact.
	 * 
	 * @param appointment
	 * @param whole
	 * @return
	 */
	public boolean delete(IAppointment appointment, boolean whole);
	
	/**
	 * Updates the boundaries and check whether the only entries are appointments if yes also check
	 * whether some boundaries are missing
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
}
