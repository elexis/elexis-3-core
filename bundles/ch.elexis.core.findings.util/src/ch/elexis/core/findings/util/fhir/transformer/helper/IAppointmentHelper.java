package ch.elexis.core.findings.util.fhir.transformer.helper;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.Appointment.AppointmentStatus;
import org.hl7.fhir.r4.model.Slot.SlotStatus;

import ch.elexis.core.model.IAppointment;

public class IAppointmentHelper extends AbstractHelper {

	public SlotStatus getSlotStatus(IAppointment localObject) {
		String type = localObject.getType();

		// TODO we need a dynamic mapping in the core, like it
		// is already present for RH, for example:
		switch (type) {
		case "frei":
			return SlotStatus.FREE;
		case "gesperrt":
			return SlotStatus.BUSYUNAVAILABLE;
		default:
			return SlotStatus.BUSY;
		}
	}

	public String getDescription(IAppointment localObject) {
		String grund = localObject.getReason();
		if (grund == null || grund.length() < 1) {
			return localObject.getType();
		}
		return grund;
	}

	public void mapApplyAppointmentStatus(Appointment target, IAppointment source) {
		String appointmentState = source.getState();

		// TODO we need a dynamic mapping in the core, like it
		// is already present for RH, for example:
		switch (appointmentState) {
		case "eingetroffen":
			target.setStatus(AppointmentStatus.ARRIVED);
			return;
		case "erledigt":
			target.setStatus(AppointmentStatus.FULFILLED);
			return;
		case "abgesagt":
			target.setStatus(AppointmentStatus.CANCELLED);
			return;
		case "nicht erschienen":
			target.setStatus(AppointmentStatus.NOSHOW);
			return;
		default:
			target.setStatus(AppointmentStatus.BOOKED);
		}
	}

	/**
	 * Map and apply the source status to the target status
	 *
	 * @param target
	 * @param source
	 */
	public void mapApplyAppointmentStatus(IAppointment target, Appointment source) {
		AppointmentStatus status = source.getStatus();
		switch (status) {
		case FULFILLED:
			target.setState("erledigt");
			return;
		case ARRIVED:
			target.setState("eingetroffen");
			return;
		case CANCELLED:
			target.setState("abgesagt");
			return;
		case NOSHOW:
			target.setState("nicht erschienen");
			return;
		default:
			target.setState("-");
		}

	}

}
