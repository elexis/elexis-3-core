package ch.elexis.core.services.internal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class AppointmentSeries implements IAppointmentSeries {
	
	private static Logger logger = LoggerFactory.getLogger(AppointmentSeries.class);
	
	private IAppointment appointment;
	
	private String groupId;
	
	private IAppointment rootAppointment;
	
	private IContact contact;
	private String freeText; // if contact == null may contain freetext
	
	private String reason;
	
	private SeriesType seriesType;
	private EndingType endingType;
	private String seriesPatternString;
	private String endingPatternString;
	
	private LocalDate endsOnDate;
	
	private String endsAfterNDates;
	
	private LocalDate startDate;
	private LocalTime startTime;
	
	private LocalTime endTime;
	
	private boolean rootPresistent;
	
	private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
	
	public AppointmentSeries(IAppointment appointment){
		this.appointment = appointment;
		groupId = appointment.getLinkgroup();
		// load including already deleted root appointment, no special handling on delete of root needed
		Optional<IAppointment> foundRoot =
			CoreModelServiceHolder.get().load(groupId, IAppointment.class, true);
		rootPresistent = foundRoot.isPresent();
		rootAppointment = foundRoot.orElse(appointment);
		
		contact = rootAppointment.getContact();
		if (contact == null) {
			freeText = rootAppointment.getSubjectOrPatient();
		}
		reason = rootAppointment.getReason();
		if (rootPresistent) {
			parseSerienTerminConfigurationString(rootAppointment.getExtension());
		}
	}
	
	/**
	 * Initialize a {@link SerienTermin} according to a <i>serientermin configuration string</i>
	 * such as for example <code>1200,1230;W,1,3|4;04042008,EA,10</code> for the syntax see the
	 * documentation in. the {@link SerienTermin} class <br>
	 * <br>
	 * Use with care, malformed strings will not be treated defensively!
	 * 
	 * Care about thread safety!
	 * 
	 * @param serienTerminConfigurationString
	 */
	private void parseSerienTerminConfigurationString(String serienTerminConfigurationString){
		String[] terms = serienTerminConfigurationString.split(";");
		String[] termin = terms[0].split(",");
		
		try {
			startTime = LocalTime.parse(termin[0], timeFormatter);
			endTime = LocalTime.parse(termin[1], timeFormatter);
			startDate = LocalDate.parse(terms[3], dateFormatter);
		} catch (Exception e) {
			logger.error("unexpected exception", e);
		}
		
		char seriesTypeCharacter = terms[1].toUpperCase().charAt(0);
		setSeriesType(SeriesType.getForCharacter(seriesTypeCharacter));
		seriesPatternString = terms[2];
		
		char endingTypeCharacter = terms[4].toUpperCase().charAt(0);
		endingType = EndingType.getForCharacter(endingTypeCharacter);
		endingPatternString = terms[5];
		
		switch (endingType) {
		case ON_SPECIFIC_DATE:
			try {
				endsOnDate = LocalDate.parse(endingPatternString, dateFormatter);
			} catch (Exception e) {
				logger.error("unexpected exception", e);
			}
			break;
		case AFTER_N_OCCURENCES:
			endsAfterNDates = endingPatternString;
			break;
		default:
			break;
		}
		
	}
	
	@Override
	public String getReason(){
		return reason;
	}
	
	@Override
	public void setReason(String value){
		reason = value;
	}
	
	@Override
	public String getState(){
		return appointment.getState();
	}
	
	@Override
	public void setState(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getType(){
		return appointment.getType();
	}
	
	@Override
	public void setType(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Integer getDurationMinutes(){
		return appointment.getDurationMinutes();
	}
	
	@Override
	public String getSchedule(){
		return rootAppointment.getSchedule();
	}
	
	@Override
	public void setSchedule(String value){
		rootAppointment.setSchedule(value);
	}
	
	@Override
	public IUser getCreatedBy(){
		return appointment.getCreatedBy();
	}
	
	@Override
	public void setCreatedBy(IUser value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getSubjectOrPatient(){
		return rootAppointment.getSubjectOrPatient();
	}
	
	@Override
	public void setSubjectOrPatient(String value){
		rootAppointment.setSubjectOrPatient(value);
	}
	
	@Override
	public int getPriority(){
		return appointment.getPriority();
	}
	
	@Override
	public void setPriority(int value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public int getTreatmentReason(){
		return appointment.getTreatmentReason();
	}
	
	@Override
	public void setTreatmentReason(int value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLinkgroup(){
		return groupId;
	}
	
	@Override
	public void setLinkgroup(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getExtension(){
		return appointment.getExtension();
	}
	
	@Override
	public void setExtension(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getCreated(){
		return appointment.getCreated();
	}
	
	@Override
	public void setCreated(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLastEdit(){
		return appointment.getLastEdit();
	}
	
	@Override
	public void setLastEdit(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getStateHistory(){
		return appointment.getStateHistory();
	}
	
	@Override
	public void setStateHistory(String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isRecurring(){
		return appointment.isRecurring();
	}
	
	@Override
	public IContact getContact(){
		return contact;
	}
	
	@Override
	public String getStateHistoryFormatted(String formatPattern){
		return appointment.getStateHistoryFormatted(formatPattern);
	}
	
	@Override
	public LocalDateTime getStartTime(){
		return appointment.getStartTime();
	}
	
	@Override
	public void setStartTime(LocalDateTime value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public LocalDateTime getEndTime(){
		return appointment.getEndTime();
	}
	
	@Override
	public void setEndTime(LocalDateTime value){
		
	}
	
	@Override
	public String getId(){
		return appointment.getId();
	}
	
	@Override
	public String getLabel(){
		return appointment.getLabel();
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public IXid getXid(String domain){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Long getLastupdate(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isDeleted(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void setDeleted(boolean value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public SeriesType getSeriesType(){
		return seriesType;
	}
	
	@Override
	public void setSeriesType(SeriesType value){
		seriesType = value;
	}
	
	@Override
	public EndingType getEndingType(){
		return endingType;
	}
	
	@Override
	public void setEndingType(EndingType value){
		endingType = value;
	}
	
	@Override
	public LocalDate getSeriesStartDate(){
		return startDate;
	}
	
	@Override
	public void setSeriesStartDate(LocalDate value){
		startDate = value;
	}
	
	@Override
	public LocalDate getSeriesEndDate(){
		return endsOnDate;
	}
	
	@Override
	public void setSeriesEndDate(LocalDate value){
		endsOnDate = value;
	}
	
	@Override
	public String getSeriesPatternString(){
		return seriesPatternString;
	}
	
	@Override
	public void setSeriesPatternString(String value){
		seriesPatternString = value;
	}
	
	@Override
	public String getEndingPatternString(){
		return endingPatternString;
	}
	
	@Override
	public void setEndingPatternString(String value){
		endingPatternString = value;
	}
	
	@Override
	public LocalTime getSeriesStartTime(){
		return startTime;
	}
	
	@Override
	public void setSeriesStartTime(LocalTime value){
		startTime = value;
	}
	
	@Override
	public LocalTime getSeriesEndTime(){
		return endTime;
	}
	
	@Override
	public void setSeriesEndTime(LocalTime value){
		endTime = value;
	}
	
	@Override
	public boolean isPersistent(){
		return rootPresistent;
	}
	
	@Override
	public IAppointment getRootAppointment(){
		return rootAppointment;
	}
	
	@Override
	public String getAsSeriesExtension(){
		// BEGINTIME,ENDTIME;SERIES_TYPE;[SERIES_PATTERN];BEGINDATE;[ENDING_TYPE];[ENDING_PATTERN]
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(timeFormatter.format(startTime));
			sb.append(",");
			sb.append(timeFormatter.format(endTime));
			sb.append(";");
			sb.append(getSeriesType().getSeriesTypeCharacter());
			sb.append(";");
			sb.append(seriesPatternString);
			sb.append(";");
			sb.append(dateFormatter.format(startDate));
			sb.append(";");
			sb.append(endingType.getEndingTypeChar());
			sb.append(";");
			
			switch (getEndingType()) {
			case AFTER_N_OCCURENCES:
				sb.append(endsAfterNDates);
				break;
			case ON_SPECIFIC_DATE:
				sb.append(dateFormatter.format(endsOnDate));
				break;
			default:
				break;
			}
		} catch (NullPointerException npe) {
			sb.append("incomplete configuration string: " + npe.getMessage());
		}
		return sb.toString();
	}
}
