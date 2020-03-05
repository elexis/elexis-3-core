package ch.elexis.core.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Termin;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.rgw.tools.StringTool;

public class Appointment extends AbstractIdDeleteModelAdapter<Termin>
		implements IdentifiableWithXid, IAppointment {
	
	public Appointment(Termin entity){
		super(entity);
	}
	
	@Override
	public String getReason(){
		return StringUtils.defaultString(getEntity().getGrund());
	}
	
	@Override
	public void setReason(String value){
		getEntityMarkDirty().setGrund(value);
	}
	
	@Override
	public String getState(){
		return getEntity().getTerminStatus();
	}
	
	@Override
	public void setState(String value){
		getEntityMarkDirty().setTerminStatus(value);
		getEntityMarkDirty()
			.setStatusHistory(getStateHistory() + StringTool.lf
				+ toMinutesTimeStamp(LocalDateTime.now()) + ";" + value);
	}
	
	@Override
	public String getType(){
		return getEntity().getTerminTyp();
	}
	
	@Override
	public void setType(String value){
		getEntityMarkDirty().setTerminTyp(value);
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
			getEntityMarkDirty().setTag(value.toLocalDate());
			int begin = (value.getHour() * 60) + value.getMinute();
			getEntityMarkDirty().setBeginn(Integer.toString(begin));
		} else {
			getEntityMarkDirty().setTag(null);
			getEntityMarkDirty().setBeginn(null);
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
				getEntityMarkDirty().setDauer(Long.toString(until));
			} else if (getDurationMinutes() != null) {
				setStartTime(value.minus(Duration.ofMinutes(getDurationMinutes())));
			} else {
				setStartTime(value);
				getEntityMarkDirty().setDauer(Integer.toString(0));
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
		getEntityMarkDirty().setBereich(value);
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
		// ids do not contain spaces, do not perform expensive load from db
		if (getEntity().getPatId() != null && !getEntity().getPatId().contains(" ")) {
			IContact contact = getContact();
			if (contact != null) {
				if (contact.isPatient()) {
					contact = CoreModelServiceHolder.get()
						.load(getEntity().getPatId(), IPatient.class, false, false).orElse(null);
				}
				return contact.getLabel();
			}
		}
		return StringUtils.defaultString(getEntity().getPatId());
	}

	@Override
	public void setSubjectOrPatient(String value){
		getEntityMarkDirty().setPatId(value);
	}

	@Override
	public int getPriority(){
		return getEntity().getPriority();
	}

	@Override
	public void setPriority(int value){
		getEntityMarkDirty().setPriority(value);
	}

	@Override
	public int getTreatmentReason(){
		return getEntity().getTreatmentReason();
	}

	@Override
	public void setTreatmentReason(int value){
		getEntityMarkDirty().setTreatmentReason(value);
	}

	@Override
	public String getLinkgroup(){
		return getEntity().getLinkgroup();
	}
	
	@Override
	public void setLinkgroup(String value){
		getEntityMarkDirty().setLinkgroup(value);
	}
	
	@Override
	public String getExtension(){
		return getEntity().getExtension();
	}
	
	@Override
	public void setExtension(String value){
		getEntityMarkDirty().setExtension(value);
	}
	
	@Override
	public IContact getContact(){
		return CoreModelServiceHolder.get()
			.load(getEntity().getPatId(), IContact.class, false, false).orElse(null);
	}

	@Override
	public String getCreated(){
		return getEntity().getAngelegt();
	}

	@Override
	public void setCreated(String value){
		getEntityMarkDirty().setAngelegt(value);
	}

	@Override
	public String getLastEdit(){
		return getEntity().getLastedit();
	}

	@Override
	public void setLastEdit(String value){
		getEntityMarkDirty().setLastedit(value);
	}

	@Override
	public String getStateHistory(){
		return StringUtils.defaultString(getEntity().getStatusHistory());
	}

	@Override
	public void setStateHistory(String value){
		getEntityMarkDirty().setStatusHistory(value);
	}
	
	@Override
	public String getStateHistoryFormatted(String formatPattern){
		if (StringUtils.isNotBlank(getStateHistory())) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
			StringBuilder sb = new StringBuilder();
			
			String lines[] = getStateHistory().split(StringTool.lf);
			for (String l : lines) {
				String f[] = l.split(";");
				if (f.length != 2)
					continue;
				
				LocalDateTime tt = fromMinutesTimeStamp(f[0]);
				sb.append(formatter.format(tt)).append(": ").append(f[1]).append(StringTool.lf);
			}
			return sb.toString();
		}
		return "";
	}
	
	private LocalDateTime fromMinutesTimeStamp(String timestamp){
		if (StringUtils.isNotBlank(timestamp) && StringUtils.isNumeric(timestamp)) {
			long minutes = Long.parseLong(timestamp);
			return LocalDateTime.ofInstant(Instant.ofEpochSecond(minutes * 60),
				ZoneId.systemDefault());
		}
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(0L), ZoneId.systemDefault());
	}
	
	private String toMinutesTimeStamp(LocalDateTime localDateTime){
		long minutes = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toEpochSecond() / 60;
		return Long.toString(minutes);
	}
	
	@Override
	public boolean isRecurring(){
		if (!StringUtils.isBlank(getLinkgroup())) {
			return CoreModelServiceHolder.get().load(getLinkgroup(), IAppointment.class)
				.isPresent();
		}
		return false;
	}
}
