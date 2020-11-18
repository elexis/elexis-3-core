package ch.elexis.core.spotlight.ui.controls.detail;

import java.time.format.DateTimeFormatter;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.text.model.Samdas;

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
	
	/**
	 * Extracted from ch.elexis.core.ui.actions.HistoryLoader
	 * 
	 * @param encounterText
	 * @param multiline
	 * @return
	 */
	String parseEncounterText(String encounterText){
		System.out.println(encounterText);
		if (encounterText != null) { 
			if(encounterText.startsWith("<")) {
				Samdas samdas = new Samdas(encounterText);
				String recordText = samdas.getRecordText();
				recordText = maskHTML(recordText);
				return recordText;
			}
			return encounterText.trim();
		}
		return "";
	}
	
	/**
	 * From ch.elexis.core.ui.actions.HistoryLoader
	 * 
	 * @param input
	 * @return
	 */
	private String maskHTML(String input){
		String s = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}
}
