package ch.elexis.core.services;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IAppointment;
import ch.rgw.tools.StringTool;
import ch.elexis.core.l10n.Messages;

@Component
public class AppointmentHistoryManagerService implements IAppointmentHistoryManagerService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IContextService contextService;

	/**
	 * Adds a new entry to the appointment's status history.
	 *
	 * @param appointment The appointment
	 * @param action      Description of the action performed
	 */
	@Override
	public void addHistoryEntry(IAppointment appointment, String action) {
		String currentHistory = appointment.getStateHistory();
		String user = getCurrentUser();
		if (!StringUtils.isEmpty(currentHistory)) {
			currentHistory += StringTool.lf;
		}
		String timestamp = toMinutesTimeStamp(LocalDateTime.now());
		String entry = timestamp + ";" + action + " [" + user + "]";
		appointment.setStateHistory(currentHistory + entry);
		coreModelService.save(appointment);
	}

	/**
	 * Logs the movement of an appointment, including area changes.
	 *
	 * @param appointment  The appointment
	 * @param oldStartTime Old start time
	 * @param newStartTime New start time
	 * @param oldArea      Old area
	 * @param newArea      New area
	 */
	@Override
	public void logAppointmentMove(IAppointment appointment, LocalDateTime oldStartTime, LocalDateTime newStartTime,
			String oldArea, String newArea) {
		String entry = Messages.AppointmentHistory_Move_From + " " + formatDateTime(oldStartTime) + " (" + oldArea
				+ ") " + Messages.AppointmentHistory_Move_To + " "
				+ formatDateTime(newStartTime) + " (" + newArea + ")";
		addHistoryEntry(appointment, entry);
	}

	/**
	 * Logs the copying of an appointment from one ID to another.
	 *
	 * @param appointment The appointment
	 * @param originalId  The original appointment ID
	 * @param newId       The new appointment ID
	 */
	@Override
	public void logAppointmentCopyFromTo(IAppointment appointment, String originalId, String newId) {
		String entry = Messages.AppointmentHistory_Copy_From + " " + "{{" + originalId + "}}"
				+ Messages.AppointmentHistory_Move_To + " "
				+ "{{" + newId + "}}";
		addHistoryEntry(appointment, entry);
	}

	/**
	 * Logs the copying of an appointment.
	 *
	 * @param appointment The appointment
	 * @param originalId  The original appointment ID
	 */
	@Override
	public void logAppointmentCopy(IAppointment appointment, String originalId) {
		String entry = Messages.AppointmentHistory_Copied_To + " " + "{{" + originalId + "}}";
		addHistoryEntry(appointment, entry);
	}

	/**
	 * Logs the change in duration of an appointment.
	 *
	 * @param appointment The appointment
	 * @param oldEndTime  Old end time
	 * @param newEndTime  New end time
	 */
	@Override
	public void logAppointmentDurationChange(IAppointment appointment, LocalDateTime oldEndTime,
			LocalDateTime newEndTime) {
		String entry = Messages.AppointmentHistory_Duration_Changed_From + " " + formatDateTime(oldEndTime) + " "
				+ Messages.AppointmentHistory_Move_To + " "
				+ formatDateTime(newEndTime);
		addHistoryEntry(appointment, entry);
    }

	/**
	 * Returns the formatted history.
	 *
	 * @param appointment   The appointment
	 * @param formatPattern The date format pattern
	 * @return A formatted history string
	 */
	@Override
	public String getFormattedHistory(IAppointment appointment, String formatPattern) {
        StringBuilder sb = new StringBuilder();
        String history = appointment.getStateHistory();
        if (StringUtils.isNotBlank(history)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            String[] lines = history.split(StringTool.lf);
            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    LocalDateTime timestamp = fromMinutesTimeStamp(parts[0]);
					sb.append(formatter.format(timestamp)).append(": ").append(parts[1]).append(StringTool.lf);
				}
			}
		}
		return sb.toString();
	}

	private String getCurrentUser() {
		return contextService != null && contextService.getActiveUser().isPresent()
				? contextService.getActiveUser().get().getLabel()
				: "Unbekannt";
	}

	private String formatDateTime(LocalDateTime dateTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
		return formatter.format(dateTime);
	}

	private LocalDateTime fromMinutesTimeStamp(String timestamp) {
		long minutes = Long.parseLong(timestamp);
		return LocalDateTime.ofEpochSecond(minutes * 60, 0, ZoneId.systemDefault().getRules().getOffset(Instant.now()));
	}

	private String toMinutesTimeStamp(LocalDateTime localDateTime) {
		long minutes = localDateTime.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(localDateTime)) / 60;
		return Long.toString(minutes);
	}

	/**
	 * Logs the editing of an appointment.
	 *
	 * @param appointment The appointment
	 */
	@Override
	public void logAppointmentEdit(IAppointment appointment) {
		String timestamp = formatDateTime(LocalDateTime.now());
		String entry = MessageFormat.format(Messages.AppointmentHistory_Edited_On_By, timestamp);
		addHistoryEntry(appointment, entry);
	}

	/**
	 * Logs the deletion of an appointment.
	 *
	 * @param appointment The appointment
	 */
	@Override
	public void logAppointmentDeletion(IAppointment appointment) {
		String formattedDateTime = formatDateTime(LocalDateTime.now());
		String entry = MessageFormat.format(Messages.AppointmentHistory_Deleted_On_By, formattedDateTime);
		addHistoryEntry(appointment, entry);
	}
}
