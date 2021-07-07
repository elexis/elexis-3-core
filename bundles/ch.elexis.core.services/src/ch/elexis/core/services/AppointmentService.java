package ch.elexis.core.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Messages;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.model.agenda.EndingType;
import ch.elexis.core.model.agenda.SeriesType;
import ch.elexis.core.model.builder.IAppointmentBuilder;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.internal.model.AppointmentSeries;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

@Component
public class AppointmentService implements IAppointmentService {
	
	public static final String AG_TERMINTYPEN = "agenda/TerminTypen"; //$NON-NLS-1$
	public static final String AG_TERMINSTATUS = "agenda/TerminStatus"; //$NON-NLS-1$
	public static final String AG_BEREICHE = "agenda/bereiche"; //$NON-NLS-1$
	public static final String AG_BEREICH_PREFIX = "agenda/bereich/"; //$NON-NLS-1$
	public static final String AG_BEREICH_TYPE_POSTFIX = "/type"; //$NON-NLS-1$
	public static final String AG_TIMEPREFERENCES = "agenda/zeitvorgaben"; //$NON-NLS-1$
	
	private static final int TYPE_FREE = 0; // frei
	private static final int TYPE_RESERVED = 1; // reserviert
	private static final int TYPE_DEFAULT = 2; // standard
	
	private static final int STATE_EMPTY = 0; // leer
	private static final int STATE_DEFAULT = 1; // standard
	
	private List<String> states = null;
	
	@Reference
	private IConfigService iConfigService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService iModelService;
	
	@Override
	public IAppointment clone(IAppointment appointment){
		return new IAppointmentBuilder(iModelService, appointment.getSchedule(),
			appointment.getStartTime(), appointment.getEndTime(), appointment.getType(),
			appointment.getState(), appointment.getPriority(), appointment.getSubjectOrPatient())
				.buildAndSave();
	}
	
	@Activate
	public void activate(){
		// @TODO server support ?
		states = getStates();
	}
	
	private List<IAppointment> getLinkedAppoinments(IAppointment orig){
		if (StringTool.isNothing(orig.getLinkgroup())) {
			return Collections.singletonList(orig);
		}
		
		IQuery<IAppointment> query = iModelService.getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__LINKGROUP, COMPARATOR.EQUALS,
			orig.getLinkgroup());
		return query.execute();
	}
	
	@Override
	public boolean delete(IAppointment appointment, boolean whole){

		// @TODO checkLock is deprecated not needed ?
		
		// check if appointment isLinked
		if (!StringTool.isNothing(appointment.getLinkgroup())) {
			List<IAppointment> linked = getLinkedAppoinments(appointment);
			if (whole) {
				// delete whole series
				iModelService.delete(linked);
			} else {
				if (appointment.getId().equals(appointment.getLinkgroup())) {
					if (linked.size() > 1) {
						int index = 0;
						IAppointment moveto = linked.get(index);
						while (moveto.getId().equals(appointment.getLinkgroup())) {
							moveto = linked.get(++index);
						}
						moveto.setSubjectOrPatient(appointment.getSubjectOrPatient());
						moveto.setReason(appointment.getReason());

						// TODO created by not working
						// moveto.set(Termin.FLD_CREATOR, get(Termin.FLD_CREATOR));
						moveto.setCreatedBy(appointment.getCreatedBy());
						moveto.setExtension(appointment.getExtension());
						iModelService.save(moveto);
						
						for (IAppointment termin : linked) {
							termin.setLinkgroup(moveto.getId());
						}
						iModelService.save(linked);
					}
				}
				// delete this
				iModelService.delete(appointment);
			}
		} else {
			iModelService.delete(appointment);
		}
		return true;
	}
	
	@Override
	public void updateBoundaries(String schedule, LocalDate date){
		IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS, schedule);
		query.and("tag", COMPARATOR.EQUALS, date);
		
		List<IAppointment> resList = query.execute();
		
		String typReserved = getType(AppointmentType.BOOKED);
		String stateEmpty = getState(AppointmentState.EMPTY);
		String stateDefault = getState(AppointmentState.DEFAULT);
		
		for (IAppointment termin : resList) {
			if (termin.getType().equals(typReserved)) {
				return;
			}
		}
		
		@SuppressWarnings("unchecked")
		Hashtable<String, String> map = StringTool.foldStrings(
			ConfigServiceHolder.get().get("agenda/tagesvorgaben" + "/" + schedule, null));
		if (map == null) {
			map = new Hashtable<String, String>();
		}
		
		int d = new TimeTool(date).get(Calendar.DAY_OF_WEEK);
		String ds = map.get(TimeTool.wdays[d - 1]);
		if (StringTool.isNothing(ds)) {
			// default für Tagesgrenzen falls nicht definiert
			ds = "0000-0800\n1800-2359"; //$NON-NLS-1$
		}
		String[] flds = ds.split("\r*\n\r*"); //$NON-NLS-1$
		for (String fld : flds) {
			String from = fld.substring(0, 4);
			String until = fld.replaceAll("-", "").substring(4); //$NON-NLS-1$ //$NON-NLS-2$
			// Lege Termine für die Tagesgrenzen an
			IAppointment iAppointment = CoreModelServiceHolder.get().create(IAppointment.class);
			LocalDateTime startTime =
				date.atStartOfDay().plusMinutes(TimeTool.getMinutesFromTimeString(from));
			LocalDateTime endTime =
				date.atStartOfDay().plusMinutes(TimeTool.getMinutesFromTimeString(until));
			iAppointment.setSchedule(schedule);
			iAppointment.setStartTime(startTime);
			iAppointment.setType(typReserved);
			iAppointment.setState(stateEmpty);
			iAppointment.setEndTime(endTime);
			String ts = Integer.toString(TimeTool.getTimeInSeconds() / 60);
			iAppointment.setCreated(ts);
			iAppointment.setLastEdit(ts);
			iAppointment.setStateHistory(stateDefault);
			CoreModelServiceHolder.get().save(iAppointment);
		}
	}
	
	@Override
	public String getType(AppointmentType type){
		List<String> types = getTypes();
		if (type != null) {
			switch (type) {
			case BOOKED:
				return types.get(TYPE_RESERVED);
			case DEFAULT:
				return types.get(TYPE_DEFAULT);
			case FREE:
				return types.get(TYPE_FREE);
			default:
				break;
			
			}
		}
		return null;
	}
	
	@Override
	public String getState(AppointmentState state){
		if (state != null) {
			switch (state) {
			case DEFAULT:
				return states.get(STATE_DEFAULT);
			case EMPTY:
				return states.get(STATE_EMPTY);
			default:
				break;
			
			}
		}
		return null;
	}
	
	@Override
	public void addType(String type){
		String tt = StringTool.join(getTypes(), ",") + "," + type;
		iConfigService.set(AG_TERMINTYPEN, tt);
	}
	
	@Override
	public void addState(String state){
		// TODO cannot add new states in Termin.java
		String tt = StringTool.join(states, ",") + "," + state;
		iConfigService.set(AG_TERMINSTATUS, tt);
		states = iConfigService.getAsList(AG_TERMINSTATUS, null);
	}
	
	@Override
	public List<Area> getAreas(){
		List<Area> ret = new ArrayList<>();
		List<String> areas = iConfigService.getAsList(AG_BEREICHE);
		areas.forEach(entry -> {
			String typeString =
				iConfigService.get(AG_BEREICH_PREFIX + entry + AG_BEREICH_TYPE_POSTFIX, null);
			AreaType type = AreaType.GENERIC;
			String contactId = null;
			if (typeString != null) {
				type = AreaType.CONTACT;
				contactId = typeString.substring(AreaType.CONTACT.name().length() + 1);
			}
			ret.add(new Area(entry, type, contactId));
		});
		return ret;
	}
	
	@Override
	public List<String> getTypes(){
		List<String> ret = new ArrayList<String>(
			iConfigService.getAsList(AG_TERMINTYPEN, Collections.emptyList()));
		if (ret.isEmpty() || ret.size() < 3) {
			ret = Arrays.asList(Messages.Appointment_Range_Free, Messages.Appointment_Range_Locked,
				Messages.Appointment_Normal_Appointment);
			iConfigService.setFromList(AG_TERMINTYPEN, ret);
		}
		return ret;
	}
	
	@Override
	public List<String> getStates(){
		List<String> ret = iConfigService.getAsList(AG_TERMINSTATUS, null);
		if (ret == null || ret.size() < 2) {
			ret = Arrays.asList("-", Messages.Appointment_Planned_Appointment);
		}
		return ret;
	}
	
	@Override
	public Optional<IAppointmentSeries> getAppointmentSeries(IAppointment appointment){
		if (appointment != null && appointment.isRecurring()) {
			return Optional.of(new AppointmentSeries(appointment));
		}
		return Optional.empty();
	}
	
	@Override
	public IAppointmentSeries createAppointmentSeries(){
		IAppointment appointment = CoreModelServiceHolder.get().create(IAppointment.class);
		// set some default values
		appointment.setSchedule(getAreas().get(0).getName());
		ContextServiceHolder.get().getActiveUser().ifPresent(au -> {
			appointment.setCreatedBy(au.getLabel());
		});
		LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
		appointment.setStartTime(LocalDateTime.of(monday, LocalTime.of(8, 0, 0)));
		appointment.setEndTime(LocalDateTime.of(monday, LocalTime.of(8, 30, 0)));
		// mark as recurring root
		appointment.setLinkgroup(appointment.getId());
		IAppointmentSeries ret = new AppointmentSeries(appointment);
		ret.setSeriesStartDate(appointment.getStartTime().toLocalDate());
		ret.setSeriesStartTime(appointment.getStartTime().toLocalTime());
		
		ret.setSeriesType(SeriesType.WEEKLY);
		ret.setSeriesPatternString("1," + Calendar.MONDAY);
		ret.setEndingType(EndingType.ON_SPECIFIC_DATE);
		ret.setSeriesEndDate(appointment.getStartTime().plusDays(7).toLocalDate());
		ret.setSeriesEndTime(appointment.getEndTime().toLocalTime());
		
		Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
		appointment.setSubjectOrPatient(patient.isPresent() ? patient.get().getId() : "");
		return ret;
	}
	
	public List<IAppointment> saveAppointmentSeries(IAppointmentSeries appointmentSeries){
		List<IAppointment> series = new ArrayList<>();
		IAppointment root = appointmentSeries.getRootAppointment();
		root.setType("series");
		LocalDate rootStartDate = getRootTerminStartTime(appointmentSeries).toLocalDate();
		appointmentSeries.setSeriesStartDate(rootStartDate);
		root.setStartTime(LocalDateTime.of(appointmentSeries.getSeriesStartDate(),
			appointmentSeries.getSeriesStartTime()));
		root.setEndTime(LocalDateTime.of(appointmentSeries.getSeriesStartDate(),
			appointmentSeries.getSeriesEndTime()));
		root.setExtension(appointmentSeries.getAsSeriesExtension());
		
		series.add(root);
		series.addAll(createSubSequentDates(appointmentSeries));
		CoreModelServiceHolder.get().save(series);
		return series;
	}
	
	private TimeTool getRootTerminStartTime(IAppointmentSeries appointmentSeries){
		LocalDateTime startdatetime = LocalDateTime.of(appointmentSeries.getSeriesStartDate(),
			appointmentSeries.getSeriesStartTime());
		Calendar cal = GregorianCalendar.from(startdatetime.atZone(ZoneId.systemDefault()));
		TimeTool tt = new TimeTool(cal.getTime());
		
		switch (appointmentSeries.getSeriesType()) {
		case DAILY:
			return tt;
		
		case WEEKLY:
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(cal.getTime());
			int firstDay = Integer
				.parseInt(appointmentSeries.getSeriesPatternString().split(",")[1].charAt(0) + "");
			cal2.set(Calendar.DAY_OF_WEEK, firstDay);
			TimeTool ret = new TimeTool(cal2.getTime());
			return ret;
		
		case MONTHLY:
			int monthDay = Integer.parseInt(appointmentSeries.getSeriesPatternString());
			Calendar calendarMonth = Calendar.getInstance();
			calendarMonth.clear();
			calendarMonth.set(Calendar.YEAR, tt.get(TimeTool.YEAR));
			if (tt.get(Calendar.DAY_OF_MONTH) <= monthDay) {
				calendarMonth.set(Calendar.MONTH, tt.get(Calendar.MONTH));
			} else {
				calendarMonth.set(Calendar.MONTH, tt.get(Calendar.MONTH));
				calendarMonth.add(Calendar.MONTH, 1);
			}
			calendarMonth.set(Calendar.DAY_OF_MONTH, monthDay);
			return new TimeTool(calendarMonth.getTime());
		
		case YEARLY:
			Calendar targetCal = Calendar.getInstance();
			targetCal.clear();
			targetCal.set(Calendar.YEAR, tt.get(TimeTool.YEAR));
			int day = Integer.parseInt(appointmentSeries.getSeriesPatternString().substring(0, 2));
			int month =
				Integer.parseInt(appointmentSeries.getSeriesPatternString().substring(2, 4));
			targetCal.set(Calendar.DAY_OF_MONTH, day);
			targetCal.set(Calendar.MONTH, month - 1);
			TimeTool target = new TimeTool(targetCal.getTime());
			if (tt.isBefore(target))
				return target;
			target.add(TimeTool.YEAR, 1);
			return target;
		}
		return tt;
	}
	
	private List<IAppointment> createSubSequentDates(IAppointmentSeries appointmentSeries){
		List<IAppointment> ret = new ArrayList<>();
		
		TimeTool dateIncrementer = new TimeTool(LocalDateTime
			.of(appointmentSeries.getSeriesStartDate(), appointmentSeries.getSeriesStartTime()));
		
		int occurences = 0;
		TimeTool endingDate = null;
		if (appointmentSeries.getEndingType().equals(EndingType.AFTER_N_OCCURENCES)) {
			occurences = (Integer.parseInt(appointmentSeries.getEndingPatternString()) - 1);
		} else {
			endingDate = new TimeTool(appointmentSeries.getSeriesEndDate());
		}
		
		switch (appointmentSeries.getSeriesType()) {
		case DAILY:
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE)) {
				occurences = dateIncrementer.daysTo(endingDate) + 1;
			}
			for (int i = 0; i < occurences; i++) {
				dateIncrementer.add(Calendar.DAY_OF_YEAR, 1);
				ret.add(writeSubsequentDateEntry(appointmentSeries, dateIncrementer));
			}
			break;
		case WEEKLY:
			String[] separatedSeriesPattern = appointmentSeries.getSeriesPatternString().split(",");
			int weekStepSize = Integer.parseInt(separatedSeriesPattern[0]);
			// handle week 1
			for (int i = 1; i < separatedSeriesPattern[1].length(); i++) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(dateIncrementer.getTime());
				int dayValue = Integer.parseInt(separatedSeriesPattern[1].charAt(i) + "");
				cal.set(Calendar.DAY_OF_WEEK, dayValue);
				ret.add(writeSubsequentDateEntry(appointmentSeries, new TimeTool(cal.getTime())));
			}
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE)) {
				long milisecondsDiff = 0;
				if (endingDate != null) {
					milisecondsDiff =
						endingDate.getTime().getTime() - dateIncrementer.getTime().getTime();
				}
				
				int days = (int) (milisecondsDiff / (1000 * 60 * 60 * 24));
				int weeks = days / 7;
				occurences = weeks / weekStepSize;
			}
			// handle subsequent weeks
			for (int i = 0; i < occurences; i++) {
				dateIncrementer.add(Calendar.WEEK_OF_YEAR, weekStepSize);
				for (int j = 0; j < separatedSeriesPattern[1].length(); j++) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(dateIncrementer.getTime());
					int dayValue = Integer.parseInt(separatedSeriesPattern[1].charAt(j) + "");
					cal.set(Calendar.DAY_OF_WEEK, dayValue);
					ret.add(
						writeSubsequentDateEntry(appointmentSeries, new TimeTool(cal.getTime())));
				}
			}
			break;
		case MONTHLY:
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE)
				&& endingDate != null) {
				occurences =
					(endingDate.get(Calendar.YEAR) - dateIncrementer.get(Calendar.YEAR)) * 12
						+ (endingDate.get(Calendar.MONTH) - dateIncrementer.get(Calendar.MONTH))
						+ (endingDate.get(Calendar.DAY_OF_MONTH) >= dateIncrementer
							.get(Calendar.DAY_OF_MONTH) ? 0 : -1);
			}
			for (int i = 0; i < occurences; i++) {
				dateIncrementer.add(Calendar.MONTH, 1);
				ret.add(writeSubsequentDateEntry(appointmentSeries, dateIncrementer));
			}
			break;
		case YEARLY:
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE)
				&& endingDate != null) {
				int monthOccurences =
					(endingDate.get(Calendar.YEAR) - dateIncrementer.get(Calendar.YEAR)) * 12
						+ (endingDate.get(Calendar.MONTH) - dateIncrementer.get(Calendar.MONTH))
						+ (endingDate.get(Calendar.DAY_OF_MONTH) >= dateIncrementer
							.get(Calendar.DAY_OF_MONTH) ? 0 : -1);
				occurences = (monthOccurences / 12);
			}
			for (int i = 0; i < occurences; i++) {
				dateIncrementer.add(Calendar.YEAR, 1);
				ret.add(writeSubsequentDateEntry(appointmentSeries, dateIncrementer));
			}
			break;
		default:
			break;
		}
		return ret;
	}
	
	private IAppointment writeSubsequentDateEntry(IAppointmentSeries appointmentSeries,
		TimeTool dateIncrementer){
		IAppointment ret = CoreModelServiceHolder.get().create(IAppointment.class);
		ret.setStartTime(dateIncrementer.toLocalDateTime());
		ret.setEndTime(LocalDateTime.of(ret.getStartTime().toLocalDate(),
			appointmentSeries.getSeriesEndTime()));
		ret.setType("series");
		ret.setReason(appointmentSeries.getReason());
		if (appointmentSeries.getContact() != null) {
			ret.setSubjectOrPatient(appointmentSeries.getContact().getId());
		} else {
			ret.setSubjectOrPatient(appointmentSeries.getSubjectOrPatient());
		}
		ret.setSchedule(appointmentSeries.getSchedule());
		ret.setState(appointmentSeries.getState());
		ret.setCreatedBy(appointmentSeries.getCreatedBy());
		ret.setTreatmentReason(appointmentSeries.getTreatmentReason());
		ret.setLinkgroup(appointmentSeries.getRootAppointment().getId());
		return ret;
	}
	
	@Override
	public void deleteAppointmentSeries(IAppointmentSeries appointmentSeries){
		if (appointmentSeries != null && appointmentSeries.isPersistent()) {
			IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
			query.and("linkgroup", COMPARATOR.EQUALS,
				appointmentSeries.getRootAppointment().getId());
			List<IAppointment> appointments = query.execute();
			CoreModelServiceHolder.get().delete(appointments);
		}
	}
	
	@Override
	public Map<String, Integer> getPreferredDurations(String areaName){
		Map<String, Integer> ret = new HashMap<String, Integer>();
		if (StringUtils.isNotBlank(areaName)) {
			String mTimes = iConfigService.get(AG_TIMEPREFERENCES + "/" + areaName, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (StringUtils.isNotBlank(mTimes)) {
				String[] types = mTimes.split("::"); //$NON-NLS-1$
				for (String t : types) {
					String[] line = t.split("="); //$NON-NLS-1$
					if (line.length != 2) {
						LoggerFactory.getLogger(getClass())
							.warn("Error in preferred duration preference [" + mTimes + "]");
						continue;
					}
					try {
						Integer duration = Integer.parseInt(line[1].trim());
						ret.put(line[0], duration);
					} catch (NumberFormatException e) {
						LoggerFactory.getLogger(getClass())
							.warn("Duration not numeric in preference [" + mTimes + "]");
						continue;
					}
				}
			}
			if (ret.get("std") == null) { //$NON-NLS-1$
				ret.put("std", 30); //$NON-NLS-1$
			}
		}
		return ret;
	}
	
	@Override
	public Optional<IContact> resolveAreaAssignedContact(String areaName){
		if (areaName != null) {
			String areaType =
				ConfigServiceHolder.get().get("agenda/bereich/" + areaName + "/type", null);
			if (areaType != null && areaType.startsWith(AreaType.CONTACT.name())) {
				String contactId = areaType.substring(AreaType.CONTACT.name().length() + 1);
				return CoreModelServiceHolder.get().load(contactId, IContact.class);
			}
		}
		
		return Optional.empty();
	}
}
