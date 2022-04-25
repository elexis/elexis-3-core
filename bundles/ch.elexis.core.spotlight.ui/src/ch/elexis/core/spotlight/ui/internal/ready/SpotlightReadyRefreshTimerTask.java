package ch.elexis.core.spotlight.ui.internal.ready;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;

public class SpotlightReadyRefreshTimerTask extends TimerTask {

	private static String NO_NEXT_APPOINTMENT_LABEL = "Heute keine weiteren Termine";

	private long lastRunTime;

	private IContextService contextService;
	private IModelService coreModelService;

	private long lastUpdateHandled;
	private Map<String, Area> appointmentUserAreas;
	private IAppointment nextAppointment;
	private String nextAppointmentLabel;

	private Long newLabValuesCount;

	private Long newDocumentsCount;

	public SpotlightReadyRefreshTimerTask(IContextService contextService, IModelService coreModelService,
			IAppointmentService appointmentService) {
		this.contextService = contextService;
		this.coreModelService = coreModelService;

		lastUpdateHandled = 0;

		nextAppointmentLabel = NO_NEXT_APPOINTMENT_LABEL;
		appointmentUserAreas = new HashMap<String, Area>();
		appointmentService.getAreas().forEach(entry -> {
			String contactId = entry.getContactId();
			if (StringUtils.isNotBlank(contactId) && entry.getType() == AreaType.CONTACT) {
				appointmentUserAreas.put(contactId, entry);
			}
		});
	}

	@Override
	public void run() {

		lastRunTime = System.currentTimeMillis();

		refreshNextAppointment();
		refreshReminders();
		refreshOpenLabValues();
		refreshOpenDocuments();

		lastUpdateHandled = lastRunTime;
	}

	private void refreshReminders() {
		// TODO Auto-generated method stub

	}

	private static final String QUERY_TEMPLATE_INBOX = "SELECT COUNT(ID) FROM at_medevit_elexis_inbox WHERE deleted = '0' AND mandant ='%s' AND state = '0' AND object LIKE '%s%%'";

	private void refreshOpenDocuments() {
		String mandatorId = contextService.getActiveMandator().map(m -> m.getId()).orElse(null);
		if (mandatorId != null) {
			String query = String.format(QUERY_TEMPLATE_INBOX, mandatorId, "ch.elexis.omnivore");
			Number result = (Number) coreModelService.executeNativeQuery(query).findFirst().orElse(null);
			newDocumentsCount = result.longValue();
		} else {
			newDocumentsCount = null;
		}

	}

	private void refreshOpenLabValues() {
		String mandatorId = contextService.getActiveMandator().map(m -> m.getId()).orElse(null);
		if (mandatorId != null) {
			String query = String.format(QUERY_TEMPLATE_INBOX, mandatorId, "ch.elexis.data.labresult");
			Number result = (Number) coreModelService.executeNativeQuery(query).findFirst().orElse(null);
			newLabValuesCount = result.longValue();
		} else {
			newLabValuesCount = null;
		}

	}

	private void refreshNextAppointment() {
		IContact userContact = contextService.getActiveUserContact().orElse(null);
		if (userContact != null) {

			Area userArea = appointmentUserAreas.get(userContact.getId());
			if (userArea != null) {
				IQuery<IAppointment> nextAppointmentQuery = coreModelService.getQuery(IAppointment.class, true, false);
				nextAppointmentQuery.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS,
						userArea.getName());

				// TODO considers only next appointment TODAY!
				LocalDateTime now = LocalDateTime.now();
				int minuteOfDay = now.getHour() * 60 + now.getMinute();
				nextAppointmentQuery.and("Tag", COMPARATOR.EQUALS, now.toLocalDate());
				nextAppointmentQuery.and("Beginn", COMPARATOR.GREATER_OR_EQUAL, minuteOfDay);

				nextAppointmentQuery.limit(1);
				nextAppointmentQuery.orderBy("Beginn", ORDER.ASC);
				nextAppointment = nextAppointmentQuery.executeSingleResult().orElse(null);
				nextAppointmentLabel = (nextAppointment != null) ? nextAppointment.getLabel()
						: NO_NEXT_APPOINTMENT_LABEL;
			}
		}

	}

	public IAppointment getNextAppointment() {
		return nextAppointment;
	}

	public String getNextAppointmentLabel() {
		return nextAppointmentLabel;
	}

	public long getInfoAgeInSeconds() {
		return (System.currentTimeMillis() - lastRunTime) / 1000;
	}

	public Long getNewLabValuesCount() {
		return newLabValuesCount;
	}

	public Long getNewDocumentsCount() {
		return newDocumentsCount;
	}

}
