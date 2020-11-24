package ch.elexis.core.spotlight.ui.controls.detail;

import java.time.format.DateTimeFormatter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IAppointment;

public class PatientDetailCompositeUtil {
	
	private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EE, dd.MM.yy HH:MM");
	
	String getAppointmentLabel(IAppointment appointment){
		return dayFormatter.format(appointment.getStartTime()) + " - " + appointment.getReason()
			+ " bei " + appointment.getSchedule();
	}
	
	void clearComposite(Composite composite, Control... exclude){
		Control[] children = composite.getChildren();
		for (Control control : children) {
			if (exclude == null) {
				control.dispose();
			} else {
				boolean excluded = false;
				for (int i = 0; i < exclude.length; i++) {
					if (exclude[i].equals(control)) {
						excluded = true;
						break;
					}
				}
				if (!excluded) {
					control.dispose();
				}
			}
		}
		composite.layout();
	}

}
