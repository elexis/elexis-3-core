package ch.elexis.core.findings.fhir.po.service.internal;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.fhir.po.model.Condition;
import ch.elexis.core.findings.fhir.po.service.FindingsFactory;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;

public class CreateOrUpdateHandler {

	private FindingsFactory factory;

	public CreateOrUpdateHandler(FindingsFactory factory) {
		this.factory = factory;
	}

	public Optional<ICondition> createOrUpdateCondition(Patient patient) {
		String diagnosis = patient.getDiagnosen();
		if (diagnosis != null && !diagnosis.isEmpty()) {
			return Optional.of(getOrCreateCondition(patient));
		}
		return Optional.empty();
	}

	private void updateCondition(ICondition condition, Patient patient){
		String diagnosis = patient.getDiagnosen();
		if (diagnosis != null && !diagnosis.isEmpty()) {
			condition.setText(diagnosis);
			condition.setCategory(ConditionCategory.DIAGNOSIS);
		} else {
			condition.setText("");
		}
		((Condition) condition).addXid("www.elexis.info/condition/updated",
			Long.toString(patient.getLastUpdate()), true);
	}

	private ICondition createCondition(Patient patient){
		ICondition condition = factory.createCondition();
		condition.setPatientId(patient.getId());
		condition.setStatus(ConditionStatus.ACTIVE);
		updateCondition(condition, patient);
		return condition;
	}

	private ICondition getOrCreateCondition(Patient patient){
		Query<Condition> query = new Query<>(Condition.class);
		query.add(Condition.FLD_PATIENTID, Query.EQUALS, patient.getId());
		List<Condition> existingConditions = query.execute();
		for (Condition condition : existingConditions) {
			String xid = condition.getXid("www.elexis.info/condition/updated");
			if (xid != null && !xid.isEmpty()) {
				long updated = Long.parseLong(xid);
				if (patient.getLastUpdate() > updated) {
					updateCondition(condition, patient);
				}
				return condition;
			}
		}
		return createCondition(patient);
	}

	//	public List<EncounterModelAdapter> createOrUpdateEncounters(List<Behandlung> behandlungen) {
	//		List<EncounterModelAdapter> ret = new ArrayList<>();
	//		for (Behandlung behandlung : behandlungen) {
	//			String patientId = behandlung.getFall().getPatientKontakt().getId();
	//			JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
	//			query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
	//			query.add(Encounter_.consultationid, JPAQuery.QUERY.EQUALS, behandlung.getId());
	//			List<Encounter> encounters = query.execute();
	//			if (encounters.isEmpty()) {
	//				ret.add(createEncounter(behandlung));
	//			} else {
	//				ret.add(updateEncounter(new EncounterModelAdapter(encounters.get(0)), behandlung));
	//			}
	//		}
	//		return ret;
	//	}
	//
	//	public EncounterModelAdapter createEncounter(Behandlung behandlung) {
	//		EncounterModelAdapter encounter = (EncounterModelAdapter) factory.createEncounter();
	//		return updateEncounter(encounter, behandlung);
	//	}
	//
	//	public EncounterModelAdapter updateEncounter(EncounterModelAdapter encounter, Behandlung behandlung) {
	//		encounter.setConsultationId(behandlung.getId());
	//		encounter.setServiceProviderId(behandlung.getMandant().getId());
	//
	//		LocalDate encounterDate = behandlung.getDatum();
	//		if (encounterDate != null) {
	//			encounter.setEffectiveTime(encounterDate.atStartOfDay());
	//		}
	//		Fall fall = behandlung.getFall();
	//		if (fall != null) {
	//			Kontakt patient = fall.getPatientKontakt();
	//			if (patient != null) {
	//				encounter.setPatientId(patient.getId());
	//			}
	//		}
	//
	//		VersionedResource vr = behandlung.getEintrag();
	//		if (vr != null) {
	//			Samdas samdas = new Samdas(vr.getHead());
	//			encounter.setText(samdas.getRecordText());
	//		}
	//
	//		factory.saveFinding(encounter);
	//		return encounter;
	//	}

}
