package ch.elexis.core.services.handler;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class AppointmentExtensionHandler {

	public static final String MAIN_PREFIX = "Main:";
	public static final String KOMBI_PREFIX = "Kombi:";

	/**
	 * Retrieves the main appointment ID from the extension of the appointment.
	 *
	 * @param appointment the appointment
	 * @return the main appointment ID or null if not present
	 */
	public static String getMainAppointmentId(IAppointment appointment) {
		String extension = appointment.getExtension();
		if (extension != null && !extension.isEmpty()) {
			return extractIdByPrefix(extension, MAIN_PREFIX);
		}
		return null;
	}

	/**
	 * Checks if an appointment is the main appointment (marked by "Main:" in the
	 * extension).
	 *
	 * @param appointment the appointment to check
	 * @return true if the appointment is a main appointment, otherwise false
	 */
	public static boolean isMainAppointment(IAppointment appointment) {
		String mainId = getMainAppointmentId(appointment);
		return appointment.getId().equals(mainId);
	}

	/**
	 * Retrieves the IDs of linked appointments (Kombi) from the extension of the
	 * main appointment.
	 *
	 * @param mainAppointment the main appointment
	 * @return a list of linked appointments
	 */
	public static List<IAppointment> getLinkedAppointments(IAppointment mainAppointment) {
		List<IAppointment> linkedAppointments = new ArrayList<>();
		String extension = mainAppointment.getExtension();

		String linkPart = getLinkPart(extension);
		if (!linkPart.isBlank()) {
			String[] parts = linkPart.split(",");
			for (String part : parts) {
				part = part.trim();
				if (part.startsWith(KOMBI_PREFIX)) {
					String id = part.substring(KOMBI_PREFIX.length()).trim();
					CoreModelServiceHolder.get().load(id, IAppointment.class).ifPresent(linkedAppointments::add);
				}
			}
		}
		return linkedAppointments;
	}

	/**
	 * Retrieves all related appointment IDs, including linked (Kombi) appointments
	 * and the main appointment itself.
	 *
	 * @param appointment the appointment (can be a main or linked appointment)
	 * @return a list of all related appointment IDs (including the main and all
	 *         linked appointments)
	 */
	public static List<IAppointment> getAllRelatedAppointments(IAppointment appointment) {
		List<IAppointment> allRelatedAppointments = new ArrayList<>();
		String mainAppointmentId = getMainAppointmentId(appointment);

		if (mainAppointmentId != null && !mainAppointmentId.isEmpty()) {
			Optional<IAppointment> mainAppointment = CoreModelServiceHolder.get().load(mainAppointmentId,
					IAppointment.class);
			if (mainAppointment.isPresent()) {
				allRelatedAppointments.add(mainAppointment.get());
				List<IAppointment> linkedAppointments = getLinkedAppointments(mainAppointment.get());
				allRelatedAppointments.addAll(linkedAppointments);
			}
		} else {
			allRelatedAppointments.add(appointment);
			List<IAppointment> linkedAppointments = getLinkedAppointments(appointment);
			allRelatedAppointments.addAll(linkedAppointments);
		}
		return allRelatedAppointments;
	}

	/**
	 * Inserts a new main appointment ID into the extension.
	 *
	 * @param appointment the appointment
	 * @param mainId      the ID of the main appointment
	 */
	public static void setMainAppointmentId(IAppointment appointment, String mainId) {
		StringBuilder extensionBuilder = new StringBuilder();
		String currentExtension = appointment.getExtension();
		if (currentExtension != null && !currentExtension.isEmpty()) {
			// Remove old Main and Kombi entries
			String[] parts = currentExtension.split("\\|\\|");
			for (String part : parts) {
				if (!part.startsWith(MAIN_PREFIX) && !part.startsWith(KOMBI_PREFIX)) {
					extensionBuilder.append(part).append("||");
				}
			}
		}
		extensionBuilder.append(MAIN_PREFIX).append(mainId);
		appointment.setExtension(extensionBuilder.toString());
	}

	/**
	 * Inserts a new linked appointment ID into the extension.
	 *
	 * @param appointment the appointment
	 * @param linkedId    the ID of the linked appointment
	 */
	public static void addLinkedAppointmentId(IAppointment appointment, String linkedId) {
		StringBuilder extensionBuilder = new StringBuilder(
				appointment.getExtension() != null ? appointment.getExtension() + "," : "");
		extensionBuilder.append(KOMBI_PREFIX).append(linkedId);
		appointment.setExtension(extensionBuilder.toString());
	}

	private static String extractIdByPrefix(String extension, String prefix) {
		String linkPart = getLinkPart(extension);
		String[] parts = linkPart.split(",");
		for (String part : parts) {
			part = part.trim();
			if (part.startsWith(prefix)) {
				return part.substring(prefix.length()).trim();
			}
		}
		return null;
	}

	/**
	 * Inserts multiple linked appointment IDs into the extension of a main
	 * appointment.
	 *
	 * @param mainAppointment      The main appointment to which the links are
	 *                             added.
	 * @param linkedAppointmentIds The list of IDs of the linked appointments.
	 */
	public static void addMultipleLinkedAppointments(IAppointment mainAppointment, List<String> linkedAppointmentIds) {
		StringBuilder extensionBuilder = new StringBuilder(
				mainAppointment.getExtension() != null ? mainAppointment.getExtension() + "," : "");
		for (String linkedId : linkedAppointmentIds) {
			extensionBuilder.append(KOMBI_PREFIX).append(linkedId).append(",");
		}
		if (extensionBuilder.length() > 0 && extensionBuilder.charAt(extensionBuilder.length() - 1) == ',') {
			extensionBuilder.setLength(extensionBuilder.length() - 1);
		}
		mainAppointment.setExtension(extensionBuilder.toString());
	}

	/**
	 * Adds multiple linked appointment IDs to the extension and returns the updated
	 * extension string.
	 *
	 * @param mainAppointment      The main appointment whose extension will be
	 *                             updated.
	 * @param linkedAppointmentIds The list of IDs of the linked appointments.
	 * @return The new extension string after adding the linked appointments.
	 */
	public static String addMultipleLinkedAppointmentsAndReturn(IAppointment mainAppointment,
			List<String> linkedAppointmentIds) {

		addMultipleLinkedAppointments(mainAppointment, linkedAppointmentIds);
		return mainAppointment.getExtension();
	}

	private static String getLinkPart(String extension) {
		if (extension == null || extension.isBlank()) {
			return "";
		}
		return extension.split("\\|\\|", 2)[0];
	}
}
