package ch.elexis.core.services;

import ch.elexis.core.model.IAppointment;

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
}
