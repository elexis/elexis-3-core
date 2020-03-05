package ch.elexis.core.model.agenda;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.IModelService;

/**
 * Adapter for a recurring {@link IAppointment}
 * 
 * @author thomas
 *
 */
public class RecurringAppointment {
	
	public static Logger logger = LoggerFactory.getLogger(RecurringAppointment.class);
	
	private String groupId;
	private IAppointment rootTermin;
	private IContact contact;
	
	private String freeText;
	private String reason;
	private Date beginTime;
	private Date endTime;
	private Date seriesStartDate;
	
	private String seriesPatternString;
	
	private EndingType endingType;
	
	private String endingPatternString;
	
	private Date endsOnDate;
	
	private String endsAfterNDates;
	
	private SeriesType seriesType;
	
	//@formatter:off
	/**
	 * configuration string syntax
	 * 
	 * BEGINTIME,ENDTIME;SERIES_TYPE;[SERIES_PATTERN];BEGINDATE;[ENDING_TYPE];[ENDING_PATTERN]
	 * 
	 * [SERIES_TYPE]
	 * D aily
	 * W eekly
	 * M onthly
	 * Y early
	 * 
	 * [SERIES_PATTERN]
	 * daily		""
	 * weekly		Number_of_weeks_between, day { day } .
	 * monthly		day_of_month
	 * yearly		ddMM
	 * 
	 * [ENDING_TYPE]
	 * O ends after n occurences -> requires number of occurences
	 * D ends on date -> requires date
	 * 
	 * [ENDING_PATTERN]
	 * if EA: number
	 * if EO: date
	 */
	//@formatter:on
	
	public RecurringAppointment(IAppointment appointment, IModelService modelService){
		groupId = appointment.getLinkgroup();
		rootTermin = modelService.load(groupId, IAppointment.class)
			.orElseThrow(() -> new IllegalStateException(
				"Not existing root appointment with id [" + groupId + "]"));
		contact = rootTermin.getContact();
		if (contact == null) {
			setFreeText(rootTermin.getSubjectOrPatient());
		}
		reason = rootTermin.getReason();
		parseSerienTerminConfigurationString(rootTermin.getExtension());
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
		SimpleDateFormat timeDf = new SimpleDateFormat("HHmm");
		SimpleDateFormat dateDf = new SimpleDateFormat("ddMMyyyy");
		
		try {
			beginTime = timeDf.parse(termin[0]);
			endTime = timeDf.parse(termin[1]);
			seriesStartDate = dateDf.parse(terms[3]);
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
				endsOnDate = dateDf.parse(endingPatternString);
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
	
	public Date getBeginTime(){
		return beginTime;
	}
	
	public void setBeginTime(Date beginTime){
		this.beginTime = beginTime;
	}
	
	public Date getEndTime(){
		return endTime;
	}
	
	public void setEndTime(Date endTime){
		this.endTime = endTime;
	}
	
	public Date getSeriesStartDate(){
		return seriesStartDate;
	}
	
	public void setSeriesStartDate(Date seriesStartDate){
		this.seriesStartDate = seriesStartDate;
	}
	
	public EndingType getEndingType(){
		return endingType;
	}
	
	public void setEndingType(EndingType endingType){
		this.endingType = endingType;
	}
	
	public String getSeriesPatternString(){
		return seriesPatternString;
	}
	
	public void setSeriesPatternString(String seriesPatternString){
		this.seriesPatternString = seriesPatternString;
	}
	
	public String getEndingPatternString(){
		return endingPatternString;
	}
	
	public void setEndingPatternString(String endingPatternString){
		this.endingPatternString = endingPatternString;
	}
	
	public IContact getContact(){
		return contact;
	}
	
	public void setContact(IContact contact){
		this.contact = contact;
	}
	
	public String getReason(){
		return reason;
	}
	
	public void setReason(String reason){
		this.reason = reason;
	}
	
	public SeriesType getSeriesType(){
		return seriesType;
	}
	
	public void setSeriesType(SeriesType seriesType){
		this.seriesType = seriesType;
	}
	
	public Date getEndsOnDate(){
		return endsOnDate;
	}
	
	public void setEndsOnDate(Date endsOnDate){
		this.endsOnDate = endsOnDate;
	}
	
	public String getEndsAfterNDates(){
		return endsAfterNDates;
	}
	
	public void setEndsAfterNDates(String endsAfterNDates){
		this.endsAfterNDates = endsAfterNDates;
	}
	
	public IAppointment getRootAppoinemtent(){
		return rootTermin;
	}
	
	public String getFreeText(){
		return freeText;
	}
	
	public void setFreeText(String freeText){
		this.freeText = freeText;
	}
}
