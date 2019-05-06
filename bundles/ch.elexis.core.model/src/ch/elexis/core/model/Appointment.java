package ch.elexis.core.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import ch.elexis.core.jpa.entities.Termin;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class Appointment extends AbstractIdDeleteModelAdapter<Termin>
		implements IdentifiableWithXid, IAppointment {
	
	public Appointment(Termin entity){
		super(entity);
	}
	
	@Override
	public String getReason(){
		return getEntity().getGrund();
	}
	
	@Override
	public void setReason(String value){
		getEntity().setGrund(value);
	}
	
	@Override
	public String getState(){
		return getEntity().getTerminStatus();
	}
	
	@Override
	public void setState(String value){
		getEntity().setTerminStatus(value);
	}
	
	@Override
	public String getType(){
		return getEntity().getTerminTyp();
	}
	
	@Override
	public void setType(String value){
		getEntity().setTerminTyp(value);
	}
	
	@Override
	public LocalDateTime getStartTime(){
		LocalDate day = getEntity().getTag();
		if (day != null) {
			try {
				int begin = Integer.valueOf(getEntity().getBeginn());
				return day.atStartOfDay().plus(Duration.ofMinutes(begin));
			} catch (NumberFormatException nfe) {}
		}
		return null;
	}
	
	@Override
	public void setStartTime(LocalDateTime value){
		if (value != null) {
			getEntity().setTag(value.toLocalDate());
			int begin = (value.getHour() * 60) + value.getMinute();
			getEntity().setBeginn(Integer.toString(begin));
		} else {
			getEntity().setTag(null);
			getEntity().setBeginn(null);
		}
	}
	
	@Override
	public LocalDateTime getEndTime(){
		LocalDateTime start = getStartTime();
		if(start != null) {
			Integer duration = getDurationMinutes();
			if(duration != null) {
				return start.plus(Duration.ofMinutes(duration));
			}
		}
		return null;
	}
	
	@Override
	public void setEndTime(LocalDateTime value){
		if (value != null) {
			if (getStartTime() != null) {
				long until = getStartTime().until(value, ChronoUnit.MINUTES);
				getEntity().setDauer(Long.toString(until));
			} else if (getDurationMinutes() != null) {
				setStartTime(value.minus(Duration.ofMinutes(getDurationMinutes())));
			} else {
				setStartTime(value);
				getEntity().setDauer(Integer.toString(0));
			}
		} else {
			getEntity().setDauer(null);
		}
	}
	
	@Override
	public Integer getDurationMinutes(){
		try {
			return Integer.valueOf(getEntity().getDauer());
		} catch(NumberFormatException nfe) {}
		return null;
	}
	
	@Override
	public String getSchedule(){
		return getEntity().getBereich();
	}
	
	@Override
	public void setSchedule(String value){
		getEntity().setBeginn(value);
	}
	
	@Override
	public IUser getCreatedBy(){
		return null;
	}
	
	@Override
	public void setCreatedBy(IUser value){
		// TODO Auto-generated method stub
	}

	@Override
	public String getSubjectOrPatient(){
		return getEntity().getPatId();
	}

	@Override
	public void setSubjectOrPatient(String value){
		getEntity().setPatId(value);
	}

	@Override
	public int getPriority(){
		return getEntity().getPriority();
	}

	@Override
	public void setPriority(int value){
		getEntity().setPriority(value);
	}

	@Override
	public int getTreatmentReason(){
		return getEntity().getTreatmentReason();
	}

	@Override
	public void setTreatmentReason(int value){
		getEntity().setTreatmentReason(value);	
	}

	@Override
	public String getLinkgroup(){
		return getEntity().getLinkgroup();
	}
	
	@Override
	public void setLinkgroup(String value){
		getEntity().setLinkgroup(value);
	}
	
	@Override
	public String getExtension(){
		return getEntity().getExtension();
	}
	
	@Override
	public void setExtension(String value){
		getEntity().setExtension(value);
	}
	
	@Override
	public IContact getContact(){
		return CoreModelServiceHolder.get().load(getSubjectOrPatient(), IContact.class)
			.orElse(null);
	}
}
