package ch.elexis.core.model.builder;

import java.time.LocalDateTime;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IModelService;

public class IAppointmentBuilder extends AbstractBuilder<IAppointment> {
	
	public IAppointmentBuilder(IModelService modelService, String schedule, LocalDateTime startTime,
		LocalDateTime endTime, String type, String state, int priority, String subjectOrPatient){
		super(modelService);
		
		object = modelService.create(IAppointment.class);
		object.setSchedule(schedule);
		object.setStartTime(startTime);
		object.setEndTime(endTime);
		object.setType(type);
		object.setState(state);
		object.setPriority(priority);
		object.setSubjectOrPatient(subjectOrPatient);
	}
	
	public IAppointmentBuilder(IModelService modelService, String schedule, LocalDateTime startTime,
		LocalDateTime endTime, String type, String state){
		this(modelService, schedule, startTime, endTime, type, state, 0, null);
	}
}
