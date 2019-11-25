package ch.elexis.core.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.Messages;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.agenda.Area;
import ch.elexis.core.model.agenda.AreaType;
import ch.elexis.core.model.builder.IAppointmentBuilder;
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
	public static final String AG_BEREICHE = "agenda/bereiche"; //$NON-NLS-1$
	public static final String AG_BEREICH_PREFIX = "agenda/bereich/"; //$NON-NLS-1$
	public static final String AG_BEREICH_TYPE_POSTFIX = "/type"; //$NON-NLS-1$
	
	private static final int TYPE_FREE = 0; //frei
	private static final int TYPE_RESERVED = 1; //reserviert
	private static final int TYPE_DEFAULT = 2; //standard
	
	private static final int STATE_EMPTY = 0; //leer
	private static final int STATE_DEFAULT = 1; //standard
	
	private List<String> states = null;
	
	@Reference
	private IConfigService iConfigService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService iModelService;
	
	@Override
	public IAppointment clone(IAppointment appointment){
		/**
		 * DEPRECATED JPA // Termin ret = // new Termin(get(FLD_BEREICH), get(FLD_TAG),
		 * getStartMinute(), getStartMinute() // + getDauer(), getType(), getStatus(),
		 * get(FLD_PRIORITY)); // Kontakt k = getKontakt(); // if (k != null) { //
		 * ret.setKontakt(getKontakt()); // } // return ret;
		 **/
		return new IAppointmentBuilder(iModelService, appointment.getSchedule(),
			appointment.getStartTime(), appointment.getEndTime(), appointment.getType(),
			appointment.getState(), appointment.getPriority(), appointment.getSubjectOrPatient())
				.buildAndSave();
	}
	
	@Activate
	public void activate(){
		//@TODO server support ?
		List<String> types = getTypes();
		states = iConfigService.getAsList(AG_TERMINSTATUS, null);
		if (types == null || types.size() < 3) {
			types = Arrays.asList(Messages.Appointment_Range_Free,
				Messages.Appointment_Range_Locked, Messages.Appointment_Normal_Appointment);
		}
		if (states == null || states.size() < 2) {
			states = Arrays.asList("-", Messages.Appointment_Planned_Appointment);
		}
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
		//@TODO checkLock is deprecated not needed ?
		
		// check if appointment isLinked
		if (!StringTool.isNothing(appointment.getLinkgroup())) {
			List<IAppointment> linked = getLinkedAppoinments(appointment);
			if (whole) {
				// delete whole series
				iModelService.delete(linked.get(0));
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
						//TODO created by not working
						//moveto.set(Termin.FLD_CREATOR, get(Termin.FLD_CREATOR));
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
		/**
		 * DEPRECATED JPA // boolean confirmed = !askForConfirmation; // if (checkLock()) { //
		 * return false; // } // String linkgroup = get(FLD_LINKGROUP); //$NON-NLS-1$ // boolean
		 * isLinked = linkgroup != null && !linkgroup.isEmpty(); // // if (isLinked &&
		 * askForConfirmation) { // MessageDialog msd = // new MessageDialog(UiDesk.getTopShell(),
		 * Messages.Termin_deleteSeries, null, // Messages.Termin_thisAppIsPartOfSerie,
		 * MessageDialog.QUESTION, new String[] { // Messages.Termin_yes, Messages.Termin_no // },
		 * 1); // int retval = msd.open(); // if (retval == SWT.DEFAULT) // { // return false; // }
		 * // confirmed = (retval == Dialog.OK); // } // if (isLinked) { // List<Termin> linked =
		 * getLinked(this); // if (confirmed) { // // delete whole series // for (Termin ae :
		 * (List<Termin>) linked) { // ae.set(new String[] { // FLD_LASTEDIT, FLD_DELETED // }, new
		 * String[] { // createTimeStamp(), StringConstants.ONE // }); // } // } else { // if
		 * (getId().equals(linkgroup)) { // // move root information // if (linked.size() > 1) { //
		 * int index = 0; // Termin moveto = linked.get(index); // while
		 * (moveto.getId().equals(linkgroup)) { // moveto = linked.get(++index); // } //
		 * moveto.set(Termin.FLD_PATIENT, get(Termin.FLD_PATIENT)); // moveto.set(Termin.FLD_GRUND,
		 * get(Termin.FLD_GRUND)); // moveto.set(Termin.FLD_CREATOR, get(Termin.FLD_CREATOR)); //
		 * moveto.set(Termin.FLD_EXTENSION, get(Termin.FLD_EXTENSION)); // for (Termin termin :
		 * linked) { // termin.set(Termin.FLD_LINKGROUP, moveto.getId()); // } // } // } // //
		 * delete this // set(new String[] { // FLD_DELETED, FLD_LASTEDIT // }, StringConstants.ONE,
		 * createTimeStamp()); // } // } else { // // delete this // set(new String[] { //
		 * FLD_DELETED, FLD_LASTEDIT // }, StringConstants.ONE, createTimeStamp()); // } // return
		 * true;
		 **/
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
		//TODO cannot add new states in Termin.java
		String tt = StringTool.join(states, ",") + "," + state;
		iConfigService.set(AG_TERMINSTATUS, tt);
		states = iConfigService.getAsList(AG_TERMINSTATUS, null);
	}
	
	@Override
	public List<Area> getAreas(){
		List<Area> ret = new ArrayList<>();
		List<String> areas = iConfigService.getAsList(AG_BEREICHE, null);
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
}
