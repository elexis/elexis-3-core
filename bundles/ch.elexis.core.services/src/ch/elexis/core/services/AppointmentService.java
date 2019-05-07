package ch.elexis.core.services;

import java.time.LocalDate;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.AppointmentState;
import ch.elexis.core.types.AppointmentType;

@Component
public class AppointmentService implements IAppointmentService {
	
	@Override
	public IAppointment clone(IAppointment appointment){
		// TODO Auto-generated method stub
		return null;
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
		
		//		List<IAppointment> resList = query.execute();
		//		// check whether the only entries are appointments if yes also check
		//		// whether some "Tagesgrenzen" are missing
		//		for (Termin termin : resList) {
		//			if (termin.getType().equals(Termin.typReserviert())) {
		//				return;
		//			}
		//		}
		//		
		//		Hashtable<String, String> map = Plannables.getDayPrefFor(resource);
		//		int d = date.get(Calendar.DAY_OF_WEEK);
		//		String ds = map.get(TimeTool.wdays[d - 1]);
		//		if (StringTool.isNothing(ds)) {
		//			// default für Tagesgrenzen falls nicht definiert
		//			ds = "0000-0800\n1800-2359"; //$NON-NLS-1$
		//		}
		//		String[] flds = ds.split("\r*\n\r*"); //$NON-NLS-1$
		//		for (String fld : flds) {
		//			String from = fld.substring(0, 4);
		//			String until = fld.replaceAll("-", "").substring(4); //$NON-NLS-1$ //$NON-NLS-2$
		//			// Lege Termine für die Tagesgrenzen an
		//			new Termin(resource, day, TimeTool.getMinutesFromTimeString(from),
		//				TimeTool.getMinutesFromTimeString(until), Termin.typReserviert(),
		//				Termin.statusLeer());
		//		}
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
