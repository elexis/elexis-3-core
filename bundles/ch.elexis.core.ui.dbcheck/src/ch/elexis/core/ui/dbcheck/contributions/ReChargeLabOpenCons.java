package ch.elexis.core.ui.dbcheck.contributions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabMapping;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;

public class ReChargeLabOpenCons extends ExternalMaintenance {
	
	private ICodeElementService codeElementService;
	
	private ServiceReference<ICodeElementService> codeServiceRef;
	
	private IBillingService billingService;
	
	private ServiceReference<IBillingService> billingServiceRef;
	
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion){
		StringJoiner sj = new StringJoiner("\n");
		if (initCodeElementService() && initBillingService()) {
			Query<Patient> queryPatients = new Query<>(Patient.class);
			List<Patient> patients = queryPatients.execute();
			
			pm.beginTask("Laborwerte nach verrechnen", patients.size());
			for (Patient patient : patients) {
				Map<LocalDate, Konsultation> openKonsultationMap = getOpenKonsultationMap(patient);
				if (!openKonsultationMap.isEmpty()) {
					Query<LabResult> queryLabResults = new Query<>(LabResult.class);
					queryLabResults.add(LabResult.PATIENT_ID, Query.EQUALS, patient.getId());
					List<LabResult> labResults = queryLabResults.execute();
					for (LabResult labResult : labResults) {
						if (labResult.getOrigin() != null && labResult.getItem() != null) {
							LabMapping mapping = LabMapping.getByContactAndItemId(
								labResult.getOrigin().getId(), labResult.getItem().getId());
							if (mapping != null && mapping.isCharge()) {
								String ealCode = ((LabItem) labResult.getItem()).getBillingCode();
								if (ealCode != null && !ealCode.isEmpty()) {
									LocalDate labResultLocalDate = getLocalDate(labResult);
									Konsultation openKons =
										openKonsultationMap.get(labResultLocalDate);
									if (openKons != null) {
										Optional<ICodeElement> matchingVerrechenbar =
											codeElementService.loadFromString("EAL 2009", ealCode,
												getContext(openKons));
										if (matchingVerrechenbar.isPresent()) {
											if (!isAlreadyBilled(openKons,
												matchingVerrechenbar.get())) {
												Optional<IEncounter> encounter =
													CoreModelServiceHolder.get()
														.load(openKons.getId(), IEncounter.class);
												billingService.bill(
													(IBillable) matchingVerrechenbar.get(),
													encounter.get(), 1.0);
											}
										}
									} else {
										sj.add("No open cons to bill [" + ealCode + "] on date ["
											+ labResultLocalDate + "] of pat ["
											+ patient.getPatCode() + "]");
									}
								}
							}
						}
					}
				}
				pm.worked(1);
			}
			
			pm.done();
			deInitCodeElementService();
			deInitBillingService();
		}
		return sj.toString();
	}
	
	private LocalDate getLocalDate(LabResult labResult){
		if (labResult.getObservationTime() != null) {
			return labResult.getObservationTime().toLocalDate();
		} else if (labResult.getDateTime() != null) {
			return labResult.getDateTime().toLocalDate();
		}
		LoggerFactory.getLogger(getClass())
			.warn("No local date for lab result [" + labResult.getId() + "]");
		return LocalDate.MIN;
	}
	
	private boolean isAlreadyBilled(Konsultation openKons, ICodeElement iCodeElement){
		List<Verrechnet> leistungen = openKons.getLeistungen();
		for (Verrechnet verrechnet : leistungen) {
			IVerrechenbar verrechenbar = verrechnet.getVerrechenbar();
			if (verrechenbar.getCodeSystemName().equals(iCodeElement.getCodeSystemName())
				&& verrechenbar.getCode().equals(iCodeElement.getCode())) {
				return true;
			}
		}
		return false;
	}
	
	private Map<LocalDate, Konsultation> getOpenKonsultationMap(Patient patient){
		Map<LocalDate, Konsultation> ret = new HashMap<>();
		for (Fall fall : patient.getFaelle()) {
			if (fall.isOpen()) {
				for (Konsultation konsultation : fall.getBehandlungen(false)) {
					if (konsultation.isBillable()) {
						ret.put(konsultation.getDateTime().toLocalDate(), konsultation);
					}
				}
			}
		}
		return ret;
	}
	
	private HashMap<Object, Object> getContext(Konsultation consultation){
		HashMap<Object, Object> ret = new HashMap<>();
		if (consultation != null) {
			ret.put(ContextKeys.CONSULTATION, consultation);
			IPersistentObject coverage = consultation.getFall();
			if (coverage != null) {
				ret.put(ContextKeys.COVERAGE, coverage);
			}
		}
		return ret;
	}
	
	private void deInitCodeElementService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		if (codeServiceRef != null) {
			context.ungetService(codeServiceRef);
			codeElementService = null;
		}
	}
	
	private boolean initCodeElementService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		codeServiceRef = context.getServiceReference(ICodeElementService.class);
		if (codeServiceRef != null) {
			codeElementService = context.getService(codeServiceRef);
			return true;
		} else {
			return false;
		}
	}
	
	private void deInitBillingService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		if (billingServiceRef != null) {
			context.ungetService(billingServiceRef);
			billingService = null;
		}
	}
	
	private boolean initBillingService(){
		BundleContext context =
			FrameworkUtil.getBundle(ReChargeTarmedOpenCons.class).getBundleContext();
		billingServiceRef = context.getServiceReference(IBillingService.class);
		if (billingServiceRef != null) {
			billingService = context.getService(billingServiceRef);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String getMaintenanceDescription(){
		return "Laborwerte aller offenen Konsutlationen neu verrechnen.";
	}
}
