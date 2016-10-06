package ch.elexis.core.ui.medication.billing;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.interfaces.IVerrechnetAdjuster;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Prescription;
import ch.elexis.data.Prescription.EntryType;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class PrescriptionVerrechnetAdjuster implements IVerrechnetAdjuster {
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	
	@Override
	public void adjust(Verrechnet verrechnet){
		executor.submit(new Runnable() {
			@Override
			public void run(){
				IVerrechenbar verrechenbar = verrechnet.getVerrechenbar();
				if (verrechenbar instanceof Artikel) {
					Artikel article = (Artikel) verrechenbar;
					Optional<Patient> patientOpt = getPatient(verrechnet);
					if (patientOpt.isPresent()) {
						// lookup existing prescriptions
						Query<Prescription> query = new Query<>(Prescription.class);
						query.add(Prescription.FLD_ARTICLE, Query.EQUALS, article.storeToString());
						query.add(Prescription.FLD_PATIENT_ID, Query.EQUALS,
							patientOpt.get().getId());
						List<Prescription> existingPrescriptions = query.execute();
						// create new dispensation
						boolean dispensationExists = false;
						if (!existingPrescriptions.isEmpty()) {
							// only create new dispensation if no dispensation on the same day
							for (Prescription prescription : existingPrescriptions) {
								if (prescription.getEntryType() == EntryType.SELF_DISPENSED) {
									TimeTool prescriptionDate =
										new TimeTool(prescription.getBeginDate());
									TimeTool verrechnetDate = getVerrechnetDate(verrechnet);
									if (prescriptionDate.isSameDay(verrechnetDate)) {
										dispensationExists = true;
										break;
									}
								}
							}
						}
						if (!dispensationExists) {
							createDispensationPrescription(article, patientOpt.get(), verrechnet);
							ElexisEventDispatcher.reload(Prescription.class);
						}
					}
				}
			}
		});
	}
	
	private Optional<Patient> getPatient(Verrechnet verrrechnet){
		Konsultation konsultation = verrrechnet.getKons();
		if (konsultation != null) {
			Fall fall = konsultation.getFall();
			if (fall != null) {
				return Optional.of(fall.getPatient());
			}
		}
		return Optional.empty();
	}
	
	private TimeTool getVerrechnetDate(Verrechnet verrrechnet){
		Konsultation konsultation = verrrechnet.getKons();
		return new TimeTool(konsultation.getDatum());
	}
	
	private Prescription createDispensationPrescription(Artikel article, Patient patient,
		Verrechnet verrechnet){
		Prescription prescription = new Prescription(article, patient, "", "");
		prescription.setExtInfoStoredObjectByKey(Prescription.FLD_EXT_VERRECHNET_ID,
			verrechnet.getId());
		verrechnet.setDetail(Verrechnet.FLD_EXT_PRESC_ID, prescription.getId());
		prescription.setEntryType(EntryType.SELF_DISPENSED);
		prescription.stop(null);
		return prescription;
	}
	
	@Override
	public void adjustGetNettoPreis(Verrechnet verrechnet, Money price){
		// TODO Auto-generated method stub
		
	}
	
}
