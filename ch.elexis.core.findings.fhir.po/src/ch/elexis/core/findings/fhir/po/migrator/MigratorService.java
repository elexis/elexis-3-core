package ch.elexis.core.findings.fhir.po.migrator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.fhir.po.model.Encounter;
import ch.elexis.core.findings.fhir.po.service.FindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;

@Component
public class MigratorService implements IMigratorService {
	
	private FindingsService findingsService;
	
	public MigratorService(){
		findingsService = new FindingsService();
	}
	
	@Override
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter,
		ICoding coding){
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migratePatientEncounters(patientId);
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				migratePatientCondition(patientId);
			}
			
			if (filter.isAssignableFrom(IObservation.class)) {
				if (ObservationCode.ANAM_PERSONAL.isSame(coding)) {
					migratePatientPersAnamnese(patientId);
				}
				else if (ObservationCode.ANAM_RISK.isSame(coding)) {
					migratePatientRiskfactors(patientId);
				}
			}
			if (filter.isAssignableFrom(IFamilyMemberHistory.class)) {
				migratePatientFamAnamnese(patientId);
			}
			if (filter.isAssignableFrom(IAllergyIntolerance.class)) {
				migrateAllergyIntolerance(patientId);
			}
		}
	}
	
	@Override
	public void migrateConsultationsFindings(String consultationId,
		Class<? extends IFinding> filter){
		if (consultationId != null && !consultationId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migrateConsultationEncounter(consultationId);
			}
		}
	}
	
	/**
	 * Migrate the existing personal anamnesis text of a patient to an {@link IObservation}
	 * instance. Migration is only performed if there is not already a personal anamnesis in form of
	 * an {@link IObservation} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientPersAnamnese(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			String anamnese = patient.getPersAnamnese();
			if (anamnese != null && !anamnese.isEmpty()) {
				List<IFinding> observations =
					findingsService.getPatientsFindings(patientId, IObservation.class);
				observations = observations.parallelStream()
					.filter(iFinding -> isPersAnamnese(iFinding))
					.collect(Collectors.toList());
				if (observations.isEmpty()) {
					IObservation observation =
						findingsService.create(IObservation.class);
					observation.setPatientId(patientId);
					observation.setCategory(ObservationCategory.SOCIALHISTORY);
					observation.setText(anamnese);
					observation.setCoding(Collections
						.singletonList(new TransientCoding(ObservationCode.ANAM_PERSONAL)));
					findingsService.saveFinding(observation);
				}
			}
		}
	}
	
	/**
	 * Migrate the existing risk factors text of a patient to an {@link IObservation} instance.
	 * Migration is only performed if there is not already a risk factors in form of an
	 * {@link IObservation} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientRiskfactors(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			String risk = patient.getRisk();
			if (risk != null && !risk.isEmpty()) {
				List<IFinding> observations =
					findingsService.getPatientsFindings(patientId, IObservation.class);
				observations = observations.parallelStream()
					.filter(iFinding -> isRiskfactor(iFinding))
					.collect(Collectors.toList());
				if (observations.isEmpty()) {
					IObservation observation =
						findingsService.create(IObservation.class);
					observation.setPatientId(patientId);
					observation.setCategory(ObservationCategory.SOCIALHISTORY);
					observation.setText(risk);
					observation.setCoding(Collections
						.singletonList(new TransientCoding(ObservationCode.ANAM_RISK)));
					findingsService.saveFinding(observation);
				}
			}
		}
	}
	
	/**
	 * Migrate the existing personal animesis text of a patient to an {@link ICondition} instance.
	 * Migration is only performed if there is not already a diagnose in form of an
	 * {@link ICondition} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientCondition(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			String diagnosis = patient.getDiagnosen();
			if (diagnosis != null && !diagnosis.isEmpty()) {
				List<IFinding> conditions =
					findingsService.getPatientsFindings(patientId, ICondition.class);
				conditions = conditions.parallelStream().filter(iFinding -> isDiagnose(iFinding))
					.collect(Collectors.toList());
				if (conditions.isEmpty()) {
					ICondition condition = findingsService.create(ICondition.class);
					condition.setPatientId(patientId);
					condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
					condition.setText(diagnosis);
					findingsService.saveFinding(condition);
				}
			}
		}
	}
	
	/**
	 * Migrate the existing family anamnesis text of a patient to an {@link IFamilyMemberHistory}
	 * instance. Migration is only performed if there is not already a family anamnesis in form of
	 * an {@link IFamilyMemberHistory} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientFamAnamnese(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			String anamnese = patient.getFamilyAnamnese();
			if (anamnese != null && !anamnese.isEmpty()) {
				List<IFinding> iFindings =
					findingsService.getPatientsFindings(patientId, IFamilyMemberHistory.class);
				if (iFindings.isEmpty()) {
					IFamilyMemberHistory familyMemberHistory =
						findingsService.create(IFamilyMemberHistory.class);
					familyMemberHistory.setPatientId(patientId);
					familyMemberHistory.setText(anamnese);
					findingsService.saveFinding(familyMemberHistory);
				}
			}
		}
	}
	
	/**
	 * Migrate the existing allergy intolerance text of a patient to an {@link IAllergyIntolerance}
	 * instance. Migration is only performed if there is not already a allergy intolerance in form
	 * of an {@link IAllergyIntolerance} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migrateAllergyIntolerance(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			String allergies = patient.getAllergies();
			if (allergies != null && !allergies.isEmpty()) {
				List<IFinding> iFindings =
					findingsService.getPatientsFindings(patientId, IAllergyIntolerance.class);
				if (iFindings.isEmpty()) {
					IAllergyIntolerance allergyIntolerance =
						findingsService.create(IAllergyIntolerance.class);
					allergyIntolerance.setPatientId(patientId);
					allergyIntolerance.setText(allergies);
					findingsService.saveFinding(allergyIntolerance);
				}
			}
		}
	}
	
	private boolean isDiagnose(IFinding iFinding){
		return iFinding instanceof ICondition
			&& ((ICondition) iFinding).getCategory() == ConditionCategory.PROBLEMLISTITEM;
	}
	
	private boolean isPersAnamnese(IFinding iFinding){
		if (iFinding instanceof IObservation
			&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : ((IObservation) iFinding).getCoding()) {
				if (ObservationCode.ANAM_PERSONAL.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isRiskfactor(IFinding iFinding){
		if (iFinding instanceof IObservation
			&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : ((IObservation) iFinding).getCoding()) {
				if (ObservationCode.ANAM_RISK.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<Konsultation> findAllConsultationsForPatient(Patient patient){
		List<Konsultation> ret = new ArrayList<>();
		Fall[] faelle = patient.getFaelle();
		if (faelle != null && faelle.length > 0) {
			for (Fall fall : faelle) {
				ret.addAll(Arrays.asList(fall.getBehandlungen(false)));
			}
		}
		return ret;
	}
	
	private void migratePatientEncounters(String patientId){
		Patient patient = Patient.load(patientId);
		if (patient != null && patient.exists()) {
			List<Konsultation> consultations = findAllConsultationsForPatient(patient);
			consultations.stream().forEach(cons -> migrateEncounter(cons));
		}
	}
	
	private void migrateConsultationEncounter(String consultationId){
		Konsultation cons = Konsultation.load(consultationId);
		if (cons != null && cons.exists()) {
			migrateEncounter(cons);
		}
	}
	
	private void migrateEncounter(Konsultation cons){
		String patientId = cons.getFall().getPatient().getId();
		
		Query<Encounter> query = new Query<>(Encounter.class);
		query.add(Encounter.FLD_PATIENTID, Query.EQUALS, patientId);
		query.add(Encounter.FLD_CONSULTATIONID, Query.EQUALS, cons.getId());
		List<Encounter> encounters = query.execute();
		
		if (encounters.isEmpty()) {
			createEncounter(cons);
		} else {
			updateEncounter(encounters.get(0), cons);
		}
	}
	
	private void createEncounter(Konsultation cons){
		IEncounter encounter = findingsService.create(IEncounter.class);
		updateEncounter(encounter, cons);
	}
	
	private void updateEncounter(IEncounter encounter, Konsultation cons){
		encounter.setConsultationId(cons.getId());
		encounter.setMandatorId(cons.getMandant().getId());
		
		LocalDate encounterDate = new TimeTool(cons.getDatum()).toLocalDate();
		if (encounterDate != null) {
			encounter.setStartTime(encounterDate.atStartOfDay());
			encounter.setEndTime(encounterDate.atTime(23, 59, 59));
		}
		Fall fall = cons.getFall();
		if (fall != null) {
			Patient patient = fall.getPatient();
			if (patient != null) {
				encounter.setPatientId(patient.getId());
			}
		}
		
		VersionedResource vr = cons.getEintrag();
		if (vr != null) {
			Samdas samdas = new Samdas(vr.getHead());
			encounter.setText(samdas.getRecordText());
		}
		
		List<ICoding> coding = encounter.getType();
		if (!ModelUtil.isSystemInList(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), coding)) {
			coding.add(new TransientCoding(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), "text",
				"Nicht strukturierte Konsultation"));
			encounter.setType(coding);
		}
		
		findingsService.saveFinding(encounter);
	}
}
