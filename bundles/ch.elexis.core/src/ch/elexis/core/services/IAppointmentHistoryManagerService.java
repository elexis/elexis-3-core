package ch.elexis.core.services;

import java.time.LocalDateTime;

import ch.elexis.core.model.IAppointment;

/**
 * Service interface for managing appointment history entries.
 */
public interface IAppointmentHistoryManagerService {

	/**
	 * Adds a new entry to the appointment's status history.
	 *
	 * @param appointment The appointment to which the history entry will be added.
	 * @param action      Description of the action performed.
	 */
	void addHistoryEntry(IAppointment appointment, String action);

	/**
	 * Logs the movement of an appointment, including area changes.
	 *
	 * @param appointment  The appointment that was moved.
	 * @param oldStartTime The old start time of the appointment.
	 * @param newStartTime The new start time of the appointment.
	 * @param oldArea      The old area of the appointment.
	 * @param newArea      The new area of the appointment.
	 */
	void logAppointmentMove(IAppointment appointment, LocalDateTime oldStartTime, LocalDateTime newStartTime,
			String oldArea, String newArea);

	/**
	 * Logs the copying of an appointment from one ID to another.
	 *
	 * @param appointment The appointment that was copied.
	 * @param originalId  The ID of the original appointment.
	 * @param newId       The ID of the new appointment.
	 */
	void logAppointmentCopyFromTo(IAppointment appointment, String originalId, String newId);

	/**
	 * Logs the copying of an appointment.
	 *
	 * @param appointment The appointment that was copied.
	 * @param newId       The ID of the new appointment.
	 */
	void logAppointmentCopy(IAppointment appointment, String newId);

	/**
	 * Logs the change in duration of an appointment.
	 *
	 * @param appointment The appointment whose duration was changed.
	 * @param oldEndTime  The old end time of the appointment.
	 * @param newEndTime  The new end time of the appointment.
	 */
	void logAppointmentDurationChange(IAppointment appointment, LocalDateTime oldEndTime, LocalDateTime newEndTime);

	/**
	 * Returns the formatted history of an appointment.
	 *
	 * @param appointment   The appointment whose history is to be formatted.
	 * @param formatPattern The date format pattern to use.
	 * @return A formatted history string.
	 */
	String getFormattedHistory(IAppointment appointment, String formatPattern);

	/**
	 * Logs the editing of an appointment.
	 *
	 * @param appointment The appointment that was edited.
	 */
	void logAppointmentEdit(IAppointment appointment);

	/**
	 * Logs the deletion of an appointment.
	 *
	 * @param appointment The appointment that was deleted.
	 */
	void logAppointmentDeletion(IAppointment appointment);
}
