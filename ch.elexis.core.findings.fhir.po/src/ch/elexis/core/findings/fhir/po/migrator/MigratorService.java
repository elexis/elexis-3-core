package ch.elexis.core.findings.fhir.po.migrator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
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
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter){
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migratePatientEncounters(patientId);
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				migratePatientCondition(patientId);
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
	 * Migrate the existing diagnose text of a patient to an {@link ICondition} instance. Migration
	 * is only performed if there is not already a diagnose in form of an {@link ICondition} present
	 * for the patient.
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
					ICondition condition = findingsService.getFindingsFactory().createCondition();
					condition.setPatientId(patientId);
					condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
					condition.setText(diagnosis);
					findingsService.saveFinding(condition);
				}
			}
		}
	}
	
	private boolean isDiagnose(IFinding iFinding){
		return iFinding instanceof ICondition
			&& ((ICondition) iFinding).getCategory() == ConditionCategory.PROBLEMLISTITEM;
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
		IEncounter encounter = findingsService.getFindingsFactory().createEncounter();
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
