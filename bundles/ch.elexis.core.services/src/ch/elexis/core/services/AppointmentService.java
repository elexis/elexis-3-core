package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.Messages;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

@Component
public class AppointmentService implements IAppointmentService {
	
	public static final String AG_TERMINTYPEN = "agenda/TerminTypen"; //$NON-NLS-1$
	public static final String AG_TERMINSTATUS = "agenda/TerminStatus"; //$NON-NLS-1$

	private static final int TYPE_RESERVED = 1;
	
	private static final int STATE_EMPTY = 0;
	private static final int STATE_DEFAULT = 1;
	
	private List<String> types = null;
	private List<String> states = null;
	
	@Reference
	private IConfigService iConfigService;
	
	@Override
	public IAppointment clone(IAppointment appointment){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Activate
	public void activate(){
		//@TODO server support ?
		types = iConfigService.getAsList(AG_TERMINTYPEN, null);
		states = iConfigService.getAsList(AG_TERMINSTATUS, null);
		if (types == null || types.size() < 3) {
			types = Arrays.asList(Messages.Appointment_Range_Free,
				Messages.Appointment_Range_Locked, Messages.Appointment_Normal_Appointment);
		}
		if (states == null || states.size() < 2) {
			states = Arrays.asList("-", Messages.Appointment_Planned_Appointment);
		}
	}
	
	@Override
	public boolean delete(IAppointment appointment, boolean whole){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void updateBoundaries(String schedule, LocalDate date){
		IQuery<IAppointment> query = CoreModelServiceHolder.get().getQuery(IAppointment.class);
		query.and(ModelPackage.Literals.IAPPOINTMENT__SCHEDULE, COMPARATOR.EQUALS, schedule);
		query.and("tag", COMPARATOR.EQUALS, date);
		
		List<IAppointment> resList = query.execute();

		String typReserved = types.get(TYPE_RESERVED);
		String stateEmpty = states.get(STATE_EMPTY);
		String stateDefault= states.get(STATE_DEFAULT);
		
		for (IAppointment termin : resList) {
			if (termin.getType().equals(typReserved)) {
				return;
			}
		}
		
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getState(AppointmentState state){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addType(String type){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void addState(String state){
		// TODO Auto-generated method stub
		
	}
}
