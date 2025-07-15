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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IAppointmentSeries;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
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
	private IAccessControlService accessControlService;

	@Reference
	private IConfigService configService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IAppointmentHistoryManagerService appointmentHistoryManagerService;

	private LoadingCache<String, Map<String, Area>> cache;

	@Override
	public IAppointment clone(IAppointment appointment) {
		String contactOrSubjectId = appointment.getContact() != null ? appointment.getContact().getId()
				: appointment.getSubjectOrPatient();

		IAppointment newAppointment = new IAppointmentBuilder(coreModelService, appointment.getSchedule(),
				appointment.getStartTime(), appointment.getEndTime(), appointment.getType(), appointment.getState(),
				appointment.getPriority(), contactOrSubjectId).buildAndSave();
		appointmentHistoryManagerService.logAppointmentCopyFromTo(newAppointment, appointment.getId(),
				newAppointment.getId());
		return newAppointment;
	}

	@Activate
	public void activate() {
		// @TODO server support ?
		accessControlService.doPrivileged(() -> {
			states = getStates();
			cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(new AreaLoader());
			assertConfigInitialization();
		});
	}

	private class AreaLoader extends CacheLoader<String, Map<String, Area>> {
		@Override
		public Map<String, Area> load(String key) throws Exception {
			if ("key".equals(key)) {
				Map<String, Area> _map = new HashMap<>();
				getAreas().stream().forEach(area -> {
					_map.put(area.getId(), area);
					_map.put(area.getName(), area);
				});
				return _map;
			}
			return null;
		}
	}

	private void assertConfigInitialization() {
		List<String> ret = new ArrayList<>(configService.getAsList(AG_TERMINTYPEN, Collections.emptyList()));
		if (ret.isEmpty() || ret.size() < 3) {
			ret = Arrays.asList(Messages.Core_free, Messages.Agenda_Appointement_Locked,
					Messages.Agenda_Appointement_Normal);
			configService.setFromList(AG_TERMINTYPEN, ret);
		}
	}

	private List<IAppointment> getLinkedAppoinments(IAppointment orig) {
		if (StringTool.isNothing(orig.getLinkgroup())) {
			return Collections.singletonList(orig);
		}

		IQuery<IAppointment> query = coreModelService.getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__LINKGROUP, COMPARATOR.EQUALS, orig.getLinkgroup());
		return query.execute();
	}

	@Override
	public boolean delete(IAppointment appointment, boolean whole) {

		// @TODO checkLock is deprecated not needed ?

		// check if appointment isLinked
		if (!StringTool.isNothing(appointment.getLinkgroup())) {
			List<IAppointment> linked = getLinkedAppoinments(appointment);
			if (whole) {
				// Log deletion for each appointment before deleting
				for (IAppointment linkedAppointment : linked) {
					appointmentHistoryManagerService.logAppointmentDeletion(linkedAppointment);
				}
				// delete whole series
				coreModelService.delete(linked);
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
						coreModelService.save(moveto);

						for (IAppointment termin : linked) {
							termin.setLinkgroup(moveto.getId());
						}
						coreModelService.save(linked);
					}
				}
				appointmentHistoryManagerService.logAppointmentDeletion(appointment);
				// delete this
				coreModelService.delete(appointment);
			}
		} else {
			appointmentHistoryManagerService.logAppointmentDeletion(appointment);
			coreModelService.delete(appointment);
		}
		return true;
	}

	@Override
	public Map<DayOfWeek, String[]> getConfiguredBlockTimesBySchedule(String schedule) {
		@SuppressWarnings("unchecked")
		Hashtable<String, String> map = StringTool
				.foldStrings(configService.get(Preferences.AG_DAYPREFERENCES + "/" + schedule, null));
		if (map == null) {
			map = new Hashtable<>();
		}
		Map<DayOfWeek, String[]> blockTimesMap = new HashMap<>(map.size());
		int[] dayOfWeekLoc = new int[] { 7, 1, 2, 3, 4, 5, 6 }; // map our day index to DayOfWeek index
		for (int i = 0; i <= 6; i++) {
			DayOfWeek dayOfWeek = DayOfWeek.of(dayOfWeekLoc[i]);
			String intraDayLimits = map.get(TimeTool.wdays[i]);
			if (StringUtils.isEmpty(intraDayLimits)) {
				intraDayLimits = Preferences.AG_DAYPREFERENCES_DAYLIMIT_DEFAULT;
			}
			String[] splitLimits = intraDayLimits.split("\r*\n\r*");
			blockTimesMap.put(dayOfWeek, splitLimits);
		}
		if (validateBlockTimesMap(blockTimesMap)) {
			return blockTimesMap;
		} else {
			throw new IllegalArgumentException("Invalid block time definition for schedule [" + schedule + "]");
		}
	}

	private boolean validateBlockTimesMap(Map<DayOfWeek, String[]> blockTimesMap) {
		for (DayOfWeek dow : blockTimesMap.keySet()) {
			String[] values = blockTimesMap.get(dow);
			if (values == null || values.length == 0) {
				LoggerFactory.getLogger(getClass()).warn("No block times for " + dow);
				return false;
			}
			for (String string : values) {
				if (StringUtils.isEmpty(string)) {
					LoggerFactory.getLogger(getClass()).warn("Empty block time for " + dow);
					return false;
				}
				String[] parts = string.split("-");
				if(parts == null || parts.length != 2) {
					LoggerFactory.getLogger(getClass()).warn("Invalid block time " + string + " for " + dow);					
					return false;
				}
				try {
					Integer low = Integer.parseInt(parts[0]);
					Integer high = Integer.parseInt(parts[1]);
					if (low > high) {
						LoggerFactory.getLogger(getClass()).warn("Invalid block time " + string + " for " + dow);
						return false;
					}
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(getClass()).warn("Invalid block time " + string + " for " + dow);
					return false;
				}
			}
		}
		return true;
	}

	private void performAssertBlockTimesForSchedule(LocalDate date, String schedule) {
		IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS, schedule);
		query.and("tag", COMPARATOR.EQUALS, date);
		String typReserved = getType(AppointmentType.BOOKED);
		query.and(ModelPackage.Literals.IAPPOINTMENT__TYPE, COMPARATOR.EQUALS, typReserved);
		List<IAppointment> resList = query.execute();
		if (resList.isEmpty()) {

			// we did not find any entries of type reserved for this day,
			// thus we initialize them
			String stateEmpty = getState(AppointmentState.EMPTY);
			String stateDefault = getState(AppointmentState.DEFAULT);
			Map<DayOfWeek, String[]> configuredBlockTimesBySchedule = getConfiguredBlockTimesBySchedule(schedule);
			String[] flds = configuredBlockTimesBySchedule.get(date.getDayOfWeek());
			List<IAppointment> appointmentsToSave = new ArrayList<>();
			for (String fld : flds) {
				String from = fld.substring(0, 4);
				String until = fld.replaceAll("-", StringUtils.EMPTY).substring(4); //$NON-NLS-1$
				IAppointment iAppointment = CoreModelServiceHolder.get().create(IAppointment.class);
				LocalDateTime startTime = date.atStartOfDay().plusMinutes(TimeTool.getMinutesFromTimeString(from));
				LocalDateTime endTime = date.atStartOfDay().plusMinutes(TimeTool.getMinutesFromTimeString(until));
				iAppointment.setSchedule(schedule);
				iAppointment.setStartTime(startTime);
				iAppointment.setType(typReserved);
				iAppointment.setState(stateEmpty);
				iAppointment.setEndTime(endTime);
				String ts = Integer.toString(TimeTool.getTimeInSeconds() / 60);
				iAppointment.setCreated(ts);
				iAppointment.setLastEdit(ts);
				iAppointment.setStateHistory(stateDefault);
				appointmentsToSave.add(iAppointment);

			}
			CoreModelServiceHolder.get().save(appointmentsToSave);
		}

	}

	@Override
	public void assertBlockTimes(LocalDate date, @Nullable String schedule) {
		if (schedule != null) {
			performAssertBlockTimesForSchedule(date, schedule);
		} else {
			getAreas().forEach(area -> performAssertBlockTimesForSchedule(date, area.getName()));
		}
	}

	@Override
	public String getType(AppointmentType type) {
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
	public String getState(AppointmentState state) {
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
	public void addType(String type) {
		String tt = StringTool.join(getTypes(), ",") + "," + type;
		configService.set(AG_TERMINTYPEN, tt);
	}

	@Override
	public void addState(String state) {
		// TODO cannot add new states in Termin.java
		String tt = StringTool.join(states, ",") + "," + state;
		configService.set(AG_TERMINSTATUS, tt);
		states = configService.getAsList(AG_TERMINSTATUS, null);
	}

	@Override
	public void addArea(String name) {
		String tt = StringTool.join(getAreas().stream().map(Area::getName).toList(), ",") + "," + name;
		configService.set(AG_BEREICHE, tt);
	}

	@Override
	public List<Area> getAreas() {
		List<Area> ret = new ArrayList<>();
		List<String> areas = configService.getAsList(AG_BEREICHE);
		areas.forEach(entry -> {
			String typeString = configService.get(AG_BEREICH_PREFIX + entry + AG_BEREICH_TYPE_POSTFIX, null);
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
	public Area getAreaByNameOrId(String nameOrId) {
		try {
			return cache.get("key").get(nameOrId);
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).warn("Error getting area by name or id [" + nameOrId + "]", e);
		}
		return null;
	}

	@Override
	public void setAreaType(String area, AreaType areaType, String value) {
		String key = Preferences.AG_BEREICH_PREFIX + area + Preferences.AG_BEREICH_TYPE_POSTFIX;
		switch (areaType) {
		case CONTACT:
			configService.set(key, areaType.name() + "/" + value);
			break;
		default:
			configService.set(key, null);
		}
	}

	@Override
	public List<String> getTypes() {
		return new ArrayList<>(configService.getAsList(AG_TERMINTYPEN, Collections.emptyList()));
	}

	@Override
	public List<String> getStates() {
		List<String> ret = configService.getAsList(AG_TERMINSTATUS, null);
		if (ret == null || ret.size() < 2) {
			ret = Arrays.asList("-", Messages.Agenda_Planned_Appointement);
		}
		return ret;
	}

	@Override
	public String getContactConfiguredTypeColor(IContact userContact, String appointmentType) {
		if (userContact == null) {
			userContact = contextService.getActiveUserContact().orElse(null);
		}
		String ret = "#" + configService // $NON-NLS-1$
				.get(userContact, "agenda/farben/typ/" + appointmentType, "3a87ad", false); //$NON-NLS-1$
		if (isValidColor(ret)) {
			return ret;
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("Invalid color string [" + ret + "] configured for [" + appointmentType + "]");
			return "#3a87ad"; //$NON-NLS-1$
		}
	}

	@Override
	public String getContactConfiguredStateColor(IContact userContact, String appointmentState) {
		if (userContact == null) {
			userContact = contextService.getActiveUserContact().orElse(null);
		}
		String ret = "#" + ConfigServiceHolder.get().get(userContact, //$NON-NLS-1$
				"agenda/farben/status/" + appointmentState, "ffffff", false); //$NON-NLS-1$
		if (isValidColor(ret)) {
			return ret;
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("Invalid color string [" + ret + "] configured for [" + appointmentState + "]");
			return "#ffffff"; //$NON-NLS-1$
		}
	}

	private boolean isValidColor(String colorString) {
		if (StringUtils.isNotBlank(colorString)) {
			// ffffff or #ffffff
			return colorString.length() == 6 || colorString.length() == 7;
		}
		return false;
	}

	@Override
	public Optional<IAppointmentSeries> getAppointmentSeries(IAppointment appointment) {
		if (appointment != null && appointment.isRecurring()) {
			return Optional.of(new AppointmentSeries(appointment));
		}
		return Optional.empty();
	}

	@Override
	public IAppointmentSeries createAppointmentSeries() {
		IAppointment appointment = CoreModelServiceHolder.get().create(IAppointment.class);
		// set some default values
		appointment.setSchedule(getAreas().get(0).getName());
		ContextServiceHolder.get().getActiveUser().ifPresent(au -> {
			appointment.setCreatedBy(au.getLabel());
		});
		LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
		appointment.setStartTime(LocalDateTime.of(monday, LocalTime.of(8, 0, 0)));
		appointment.setEndTime(LocalDateTime.of(monday, LocalTime.of(8, 30, 0)));
		appointment.setState(Messages.Agenda_Planned_Appointement);
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
		appointment.setSubjectOrPatient(patient.isPresent() ? patient.get().getId() : StringUtils.EMPTY);
		return ret;
	}

	@Override
	public List<IAppointment> saveAppointmentSeries(IAppointmentSeries appointmentSeries) {
		List<IAppointment> series = new ArrayList<>();
		IAppointment root = appointmentSeries.getRootAppointment();
		root.setType("series");
		LocalDate rootStartDate = getRootTerminStartTime(appointmentSeries).toLocalDate();
		appointmentSeries.setSeriesStartDate(rootStartDate);
		root.setStartTime(
				LocalDateTime.of(appointmentSeries.getSeriesStartDate(), appointmentSeries.getSeriesStartTime()));
		root.setEndTime(LocalDateTime.of(appointmentSeries.getSeriesStartDate(), appointmentSeries.getSeriesEndTime()));
		root.setExtension(appointmentSeries.getAsSeriesExtension());

		series.add(root);
		series.addAll(createSubSequentDates(appointmentSeries));
		CoreModelServiceHolder.get().save(series);
		return series;
	}

	private TimeTool getRootTerminStartTime(IAppointmentSeries appointmentSeries) {
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
					.parseInt(appointmentSeries.getSeriesPatternString().split(",")[1].charAt(0) + StringUtils.EMPTY);
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
			int month = Integer.parseInt(appointmentSeries.getSeriesPatternString().substring(2, 4));
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

	private List<IAppointment> createSubSequentDates(IAppointmentSeries appointmentSeries) {
		List<IAppointment> ret = new ArrayList<>();

		TimeTool dateIncrementer = new TimeTool(
				LocalDateTime.of(appointmentSeries.getSeriesStartDate(), appointmentSeries.getSeriesStartTime()));

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
				int dayValue = Integer.parseInt(separatedSeriesPattern[1].charAt(i) + StringUtils.EMPTY);
				cal.set(Calendar.DAY_OF_WEEK, dayValue);
				ret.add(writeSubsequentDateEntry(appointmentSeries, new TimeTool(cal.getTime())));
			}
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE)) {
				long milisecondsDiff = 0;
				if (endingDate != null) {
					milisecondsDiff = endingDate.getTime().getTime() - dateIncrementer.getTime().getTime();
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
					int dayValue = Integer.parseInt(separatedSeriesPattern[1].charAt(j) + StringUtils.EMPTY);
					cal.set(Calendar.DAY_OF_WEEK, dayValue);
					ret.add(writeSubsequentDateEntry(appointmentSeries, new TimeTool(cal.getTime())));
				}
			}
			break;
		case MONTHLY:
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE) && endingDate != null) {
				occurences = (endingDate.get(Calendar.YEAR) - dateIncrementer.get(Calendar.YEAR)) * 12
						+ (endingDate.get(Calendar.MONTH) - dateIncrementer.get(Calendar.MONTH))
						+ (endingDate.get(Calendar.DAY_OF_MONTH) >= dateIncrementer.get(Calendar.DAY_OF_MONTH) ? 0
								: -1);
			}
			for (int i = 0; i < occurences; i++) {
				dateIncrementer.add(Calendar.MONTH, 1);
				ret.add(writeSubsequentDateEntry(appointmentSeries, dateIncrementer));
			}
			break;
		case YEARLY:
			if (appointmentSeries.getEndingType().equals(EndingType.ON_SPECIFIC_DATE) && endingDate != null) {
				int monthOccurences = (endingDate.get(Calendar.YEAR) - dateIncrementer.get(Calendar.YEAR)) * 12
						+ (endingDate.get(Calendar.MONTH) - dateIncrementer.get(Calendar.MONTH))
						+ (endingDate.get(Calendar.DAY_OF_MONTH) >= dateIncrementer.get(Calendar.DAY_OF_MONTH) ? 0
								: -1);
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

	private IAppointment writeSubsequentDateEntry(IAppointmentSeries appointmentSeries, TimeTool dateIncrementer) {
		IAppointment ret = CoreModelServiceHolder.get().create(IAppointment.class);
		ret.setStartTime(dateIncrementer.toLocalDateTime());
		ret.setEndTime(LocalDateTime.of(ret.getStartTime().toLocalDate(), appointmentSeries.getSeriesEndTime()));
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
	public void deleteAppointmentSeries(IAppointmentSeries appointmentSeries) {
		if (appointmentSeries != null && appointmentSeries.isPersistent()) {
			IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
			query.and("linkgroup", COMPARATOR.EQUALS, appointmentSeries.getRootAppointment().getId());
			List<IAppointment> appointments = query.execute();
			CoreModelServiceHolder.get().delete(appointments);
		}
	}

	@Override
	public Map<String, Integer> getPreferredDurations(String areaName) {
		Map<String, Integer> ret = new HashMap<>();
		if (StringUtils.isNotBlank(areaName)) {
			String mTimes = configService.get(AG_TIMEPREFERENCES + "/" + areaName, StringUtils.EMPTY); //$NON-NLS-1$
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
						LoggerFactory.getLogger(getClass()).warn("Duration not numeric in preference [" + mTimes + "]");
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
	public List<Area> getAoboAreas() {
		Optional<IUser> user = contextService.getActiveUser();
		if (user.isPresent()) {
			List<String> aoboMandatorIds = accessControlService.getAoboMandatorIds();
			return getAreas().stream()
					.filter(a -> a.getType() == AreaType.GENERIC || aoboMandatorIds.contains(a.getContactId()))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public Optional<IContact> resolveAreaAssignedContact(String areaName) {
		if (areaName != null) {
			String areaType = configService.get("agenda/bereich/" + areaName + "/type", null);
			if (areaType != null && areaType.startsWith(AreaType.CONTACT.name())) {
				String contactId = areaType.substring(AreaType.CONTACT.name().length() + 1);
				return CoreModelServiceHolder.get().load(contactId, IContact.class);
			}
		}
		
		return Optional.empty();
	}

	@Override
	public String resolveAreaByAssignedContact(IContact contact) {
		if (contact != null) {
			Optional<Area> area = getAreas().stream().filter(a -> StringUtils.equals(contact.getId(), a.getContactId()))
					.findFirst();
			return area.map(Area::getName).orElse(null);
		}
		return null;
	}
}
