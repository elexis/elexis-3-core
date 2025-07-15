package ch.elexis.core.services.internal.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.XidServiceHolder;

public class AppointmentSeries implements IAppointmentSeries {

	private static Logger logger = LoggerFactory.getLogger(AppointmentSeries.class);

	private IAppointment appointment;

	private String groupId;

	private IAppointment rootAppointment;

	private SeriesType seriesType;
	private EndingType endingType;
	private String seriesPatternString;
	private String endingPatternString;

	private LocalDate endsOnDate;

	private LocalDate startDate;
	private LocalTime startTime;

	private LocalTime endTime;

	private boolean rootPresistent;

	private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");

	public AppointmentSeries(IAppointment appointment) {
		this.appointment = appointment;
		groupId = appointment.getLinkgroup();
		// load including already deleted root appointment, no special handling on
		// delete of root needed
		Optional<IAppointment> foundRoot = CoreModelServiceHolder.get().load(groupId, IAppointment.class, true);
		rootPresistent = foundRoot.isPresent();
		rootAppointment = foundRoot.orElse(appointment);

		if (rootPresistent) {
			parseSerienTerminConfigurationString(rootAppointment.getExtension());
		}
	}

	/**
	 * Initialize a {@link SerienTermin} according to a <i>serientermin
	 * configuration string</i> such as for example
	 * <code>1200,1230;W,1,3|4;04042008,EA,10</code> for the syntax see the
	 * documentation in. the {@link SerienTermin} class <br>
	 * <br>
	 * Use with care, malformed strings will not be treated defensively!
	 *
	 * Care about thread safety!
	 *
	 * @param serienTerminConfigurationString
	 */
	private void parseSerienTerminConfigurationString(String serienTerminConfigurationString) {
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
		default:
			break;
		}

	}

	@Override
	public String getReason() {
		return appointment.getReason();
	}

	@Override
	public void setReason(String value) {
		appointment.setReason(value);
	}

	@Override
	public String getState() {
		return appointment.getState();
	}

	@Override
	public void setState(String value) {
		appointment.setState(value);
	}

	@Override
	public String getType() {
		return appointment.getType();
	}

	@Override
	public void setType(String value) {
		appointment.setType(value);
	}

	@Override
	public Integer getDurationMinutes() {
		return appointment.getDurationMinutes();
	}

	@Override
	public String getSchedule() {
		return appointment.getSchedule();
	}

	@Override
	public void setSchedule(String value) {
		appointment.setSchedule(value);
	}

	@Override
	public String getCreatedBy() {
		return appointment.getCreatedBy();
	}

	@Override
	public void setCreatedBy(String value) {
		appointment.setCreatedBy(value);
	}

	@Override
	public String getSubjectOrPatient() {
		return appointment.getSubjectOrPatient();
	}

	@Override
	public void setSubjectOrPatient(String value) {
		appointment.setSubjectOrPatient(value);
	}

	@Override
	public int getPriority() {
		return appointment.getPriority();
	}

	@Override
	public void setPriority(int value) {
		appointment.setPriority(value);
	}

	@Override
	public int getTreatmentReason() {
		return appointment.getTreatmentReason();
	}

	@Override
	public void setTreatmentReason(int value) {
		appointment.setTreatmentReason(value);
	}

	@Override
	public int getInsuranceType() {
		return appointment.getInsuranceType();
	}

	@Override
	public void setInsuranceType(int value) {
		appointment.setInsuranceType(value);
	}

	@Override
	public int getCaseType() {
		return appointment.getCaseType();
	}

	@Override
	public void setCaseType(int value) {
		appointment.setCaseType(value);
	}

	@Override
	public String getLinkgroup() {
		return groupId;
	}

	@Override
	public void setLinkgroup(String value) {
		appointment.setLinkgroup(value);
	}

	@Override
	public String getExtension() {
		return appointment.getExtension();
	}

	@Override
	public void setExtension(String value) {
		appointment.setExtension(value);
	}

	@Override
	public String getCreated() {
		return appointment.getCreated();
	}

	@Override
	public void setCreated(String value) {
		appointment.setCreated(value);
	}

	@Override
	public String getLastEdit() {
		return appointment.getLastEdit();
	}

	@Override
	public void setLastEdit(String value) {
		appointment.setLastEdit(value);
	}

	@Override
	public String getStateHistory() {
		return appointment.getStateHistory();
	}

	@Override
	public void setStateHistory(String value) {
		appointment.setStateHistory(value);
	}

	@Override
	public boolean isRecurring() {
		return appointment.isRecurring();
	}

	@Override
	public IContact getContact() {
		return appointment.getContact();
	}

	@Override
	public String getStateHistoryFormatted(String formatPattern) {
		return appointment.getStateHistoryFormatted(formatPattern);
	}

	@Override
	public LocalDateTime getStartTime() {
		return appointment.getStartTime();
	}

	@Override
	public void setStartTime(LocalDateTime value) {

	}

	@Override
	public LocalDateTime getEndTime() {
		return appointment.getEndTime();
	}

	@Override
	public void setEndTime(LocalDateTime value) {

	}

	@Override
	public boolean isAllDay() {
		return appointment.isAllDay();
	}

	@Override
	public String getId() {
		return appointment.getId();
	}

	@Override
	public String getLabel() {
		return appointment.getLabel();
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		return XidServiceHolder.get().addXid(appointment, domain, id, updateIfExists);
	}

	@Override
	public IXid getXid(String domain) {
		return XidServiceHolder.get().getXid(appointment, domain);
	}

	@Override
	public Long getLastupdate() {
		return appointment.getLastupdate();
	}

	@Override
	public boolean isDeleted() {
		return appointment.isDeleted();
	}

	@Override
	public void setDeleted(boolean value) {
		appointment.setDeleted(value);
	}

	@Override
	public SeriesType getSeriesType() {
		return seriesType;
	}

	@Override
	public void setSeriesType(SeriesType value) {
		seriesType = value;
	}

	@Override
	public EndingType getEndingType() {
		return endingType;
	}

	@Override
	public void setEndingType(EndingType value) {
		endingType = value;
	}

	@Override
	public LocalDate getSeriesStartDate() {
		return startDate;
	}

	@Override
	public void setSeriesStartDate(LocalDate value) {
		startDate = value;
	}

	@Override
	public LocalDate getSeriesEndDate() {
		return endsOnDate;
	}

	@Override
	public void setSeriesEndDate(LocalDate value) {
		endsOnDate = value;
	}

	@Override
	public String getSeriesPatternString() {
		return seriesPatternString;
	}

	@Override
	public void setSeriesPatternString(String value) {
		seriesPatternString = value;
	}

	@Override
	public String getEndingPatternString() {
		return endingPatternString;
	}

	@Override
	public void setEndingPatternString(String value) {
		endingPatternString = value;
	}

	@Override
	public LocalTime getSeriesStartTime() {
		return startTime;
	}

	@Override
	public void setSeriesStartTime(LocalTime value) {
		startTime = value;
	}

	@Override
	public LocalTime getSeriesEndTime() {
		return endTime;
	}

	@Override
	public void setSeriesEndTime(LocalTime value) {
		endTime = value;
	}

	@Override
	public boolean isPersistent() {
		return rootPresistent;
	}

	@Override
	public IAppointment getRootAppointment() {
		return rootAppointment;
	}

	@Override
	public String getAsSeriesExtension() {
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
				sb.append(endingPatternString);
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

	@Override
	public List<IAppointment> getAppointments() {
		if (StringUtils.isBlank(getLinkgroup())) {
			return Collections.singletonList(this);
		}
		IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__LINKGROUP, COMPARATOR.EQUALS, getLinkgroup());
		return query.execute();
	}
}
