package ch.elexis.core.findings.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;

import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.services.IAppointmentService;

public class TerminUtil {
	
	private IAppointmentService appointmentService;
	
	public TerminUtil(IAppointmentService appointmentService){
		this.appointmentService = appointmentService;
	}
	
	public Map<String, String> getAgendaAreas(){
		Map<String, String> areas = new HashMap<>();
		
		List<Area> agendaAreas = appointmentService.getAreas();
		for (Area area : agendaAreas) {
			areas.put(getIdForBereich(area.getName()), area.getName());
		}
		
		return areas;
	}
	
	public Optional<String> resolveAgendaAreaByScheduleId(String idPart){
		return Optional.ofNullable(getAgendaAreas().get(idPart));
	}
	
	public String getIdForBereich(String area){
		return DigestUtils.md5Hex(area);
	}
	
}
