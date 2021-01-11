package ch.elexis.core.spotlight.ui.internal.ready;

import java.util.List;
import java.util.Timer;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IAppointment;
import ch.elexis.core.services.IAppointmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

@Component(immediate = true, service = SpotlightReadyService.class)
public class SpotlightReadyService {
	
	private static final String PATIENT_SELECTION_HISTORY = "patient-selection-history";
	
	@Reference
	private IContextService contextService;
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	@Reference
	private IAppointmentService appointmentService;
	
	private Timer timer;
	private SpotlightReadyRefreshTimerTask spotlightReadyRefreshTimerTask;
	
	@Activate
	public void activate(){
		PatientSelectionHistorySupplier patientSelectionHistorySupplier =
			new PatientSelectionHistorySupplier();
		CoreUiUtil.injectServicesWithContext(patientSelectionHistorySupplier);
		contextService.getRootContext().setNamed(PATIENT_SELECTION_HISTORY,
			patientSelectionHistorySupplier);
		
		timer = new Timer("spotlight-ready-refresh");
		spotlightReadyRefreshTimerTask = new SpotlightReadyRefreshTimerTask(contextService,
			coreModelService, appointmentService);
		timer.schedule(spotlightReadyRefreshTimerTask, 1 * 1000, 10 * 1000);
	}
	
	@Deactivate
	public void deactivate(){
		timer.cancel();
	}
	
	/**
	 * @return the last 5 selected patients in format [patient id, String (selection time), String
	 *         (patient label)]
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getLastPatientSelections(){
		return (List<Object[]>) contextService.getRootContext().getNamed(PATIENT_SELECTION_HISTORY)
			.get();
	}
	
	public IAppointment getNextAppointment(){
		return spotlightReadyRefreshTimerTask.getNextAppointment();
	}
	
	public String getNextAppointmentLabel(){
		return spotlightReadyRefreshTimerTask.getNextAppointmentLabel();
	}
	
	public long getInfoAgeInSeconds(){
		return spotlightReadyRefreshTimerTask.getInfoAgeInSeconds();
	}
	
	public Long getNewLabValuesCount(){
		return spotlightReadyRefreshTimerTask.getNewLabValuesCount();
	}
	
	public Long getNewDocumentsCount(){
		return spotlightReadyRefreshTimerTask.getNewDocumentsCount();
	}
	
}
