package ch.elexis.core.findings.fhir.po.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.fhir.po.model.AllergyIntolerance;
import ch.elexis.core.findings.fhir.po.model.ClinicalImpression;
import ch.elexis.core.findings.fhir.po.model.Condition;
import ch.elexis.core.findings.fhir.po.model.Encounter;
import ch.elexis.core.findings.fhir.po.model.FamilyMemberHistory;
import ch.elexis.core.findings.fhir.po.model.Observation;
import ch.elexis.core.findings.fhir.po.model.ProcedureRequest;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

@Component
public class FindingsService implements IFindingsService {
	private Logger logger = LoggerFactory.getLogger(FindingsService.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IFinding> List<T> getPatientsFindings(String patientId, Class<T> filter){
		List<IFinding> ret = new ArrayList<>();
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				ret.addAll(getEncounters(patientId));
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				ret.addAll(getConditions(patientId));
			}
			if (filter.isAssignableFrom(IClinicalImpression.class)) {
				ret.addAll(getClinicalImpressions(patientId, null));
			}
			if (filter.isAssignableFrom(IObservation.class)) {
				ret.addAll(getObservations(patientId, null));
			}
			if (filter.isAssignableFrom(IProcedureRequest.class)) {
				ret.addAll(getProcedureRequests(patientId, null));
			}
			if (filter.isAssignableFrom(IFamilyMemberHistory.class)) {
				ret.addAll(getFamilyMemberHistory(patientId));
			}
			if (filter.isAssignableFrom(IAllergyIntolerance.class)) {
				ret.addAll(getAllergyIntolerance(patientId));
			}
		}
		return (List<T>) ret;
	}
	
	private List<ProcedureRequest> getProcedureRequests(String patientId, String encounterId){
		Query<ProcedureRequest> query = new Query<>(ProcedureRequest.class);
		if (patientId != null) {
			query.add(ProcedureRequest.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(ProcedureRequest.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
		}
		return query.execute();
	}
	
	private List<ClinicalImpression> getClinicalImpressions(String patientId, String encounterId){
		Query<ClinicalImpression> query = new Query<>(ClinicalImpression.class);
		if (patientId != null) {
			query.add(ClinicalImpression.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(ClinicalImpression.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
		}
		return query.execute();
	}
	
	private List<Condition> getConditions(String patientId){
		Query<Condition> query = new Query<>(Condition.class);
		if (patientId != null) {
			query.add(Condition.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		return query.execute();
	}
	
	private List<FamilyMemberHistory> getFamilyMemberHistory(String patientId){
		Query<FamilyMemberHistory> query = new Query<>(FamilyMemberHistory.class);
		if (patientId != null) {
			query.add(FamilyMemberHistory.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		return query.execute();
	}
	
	private List<AllergyIntolerance> getAllergyIntolerance(String patientId){
		Query<AllergyIntolerance> query = new Query<>(AllergyIntolerance.class);
		if (patientId != null) {
			query.add(AllergyIntolerance.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		return query.execute();
	}
	
	private List<Observation> getObservations(String patientId, String encounterId){
		Query<Observation> query = new Query<>(Observation.class);
		if (patientId != null) {
			query.add(Observation.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(Observation.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
		}
		return query.execute();
	}
	
	private List<Encounter> getEncounters(String patientId){
		Query<Encounter> query = new Query<>(Encounter.class);
		if (patientId != null) {
			query.add(Encounter.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		return query.execute();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IFinding> List<T> getConsultationsFindings(String consultationId,
		Class<T> filter){
		List<IFinding> ret = new ArrayList<>();
		if (consultationId != null && !consultationId.isEmpty()) {
			Optional<IEncounter> encounter = getEncounter(consultationId);
			if (encounter.isPresent()) {
				if (filter.isAssignableFrom(IEncounter.class)) {
					ret.add(encounter.get());
				}
				if (filter.isAssignableFrom(IClinicalImpression.class)) {
					ret.addAll(getClinicalImpressions(null, encounter.get().getId()));
				}
				if (filter.isAssignableFrom(IObservation.class)) {
					ret.addAll(getObservations(null, encounter.get().getId()));
				}
				if (filter.isAssignableFrom(IProcedureRequest.class)) {
					ret.addAll(getProcedureRequests(null, encounter.get().getId()));
				}
			}
		}
		return (List<T>) ret;
	}
	
	private Optional<IEncounter> getEncounter(String consultationId){
		Query<Encounter>query = new Query<>(Encounter.class);
		query.add(Encounter.FLD_CONSULTATIONID, Query.EQUALS, consultationId);
		List<Encounter> encounters = query.execute();
		if(encounters != null && !encounters.isEmpty()) {
			if (encounters.size() > 1) {
				logger.warn("Too many encounters [" + encounters.size()
					+ "] found for consultation [" + consultationId + "] using first.");
			}
			return Optional.of(encounters.get(0));
		}
		return Optional.empty();
	}
	
	@Override
	public void saveFinding(IFinding finding){
		if (!(finding instanceof IPersistentObject)) {
			logger
				.error("Can not save IFinding which is not an IPersistentObject [" + finding + "]");
		} else {
			if (!((IPersistentObject) finding).exists()) {
				logger.error(
					"Can not save IFinding which does not exist in database [" + finding + "]");
			}
		}
	}
	
	@Override
	public void deleteFinding(IFinding finding){
		if (!(finding instanceof IPersistentObject)) {
			logger.error(
				"Can not delete IFinding which is not an IPersistentObject [" + finding + "]");
		} else {
			if (!((IPersistentObject) finding).exists()) {
				logger.error(
					"Can not delete IFinding which does not exist in database [" + finding + "]");
			} else {
				((PersistentObject) finding).delete();
			}
		}
	}
	
	@Override
	public <T extends IFinding> Optional<T> findById(String id, Class<T> clazz){
		IFinding loadedObj = null;
		if (clazz.isAssignableFrom(ICondition.class)) {
			loadedObj = Condition.load(id);
		}
		if (loadedObj != null && ((IPersistentObject) loadedObj).exists()) {
			return Optional.of(clazz.cast(loadedObj));
		}
		IObservation observation = create(IObservation.class);
		return Optional.empty();
	}
	
	@Override
	public <T extends IFinding> T create(Class<T> type){
		if (type.equals(IEncounter.class)) {
			Encounter ret = (Encounter) new Encounter().create();
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
				new org.hl7.fhir.dstu3.model.Encounter();
			fhirEncounter.setId(new IdType("Encounter", ret.getId()));
			ModelUtil.saveResource(fhirEncounter, ret);
			return type.cast(ret);
		}
		else if (type.equals(IObservation.class)) {
			Observation ret = (Observation) new Observation().create();
			org.hl7.fhir.dstu3.model.Observation fhirOberservation =
				new org.hl7.fhir.dstu3.model.Observation();
			fhirOberservation.setId(new IdType("Observation", ret.getId()));
			ModelUtil.saveResource(fhirOberservation, ret);
			return type.cast(ret);
		}
		else if (type.equals(ICondition.class)) {
			Condition ret = (Condition) new Condition().create();
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				new org.hl7.fhir.dstu3.model.Condition();
			fhirCondition.setId(new IdType("Condition", ret.getId()));
			fhirCondition.setAssertedDate(new Date());
			ModelUtil.saveResource(fhirCondition, ret);
			return type.cast(ret);
		}
		else if (type.equals(IClinicalImpression.class)) {
			ClinicalImpression ret = new ClinicalImpression();
			return type.cast((IClinicalImpression) ret.create());
		}
		else if (type.equals(IProcedureRequest.class)) {
			ProcedureRequest ret = new ProcedureRequest();
			return type.cast((IProcedureRequest) ret.create());
		}
		else if (type.equals(IFamilyMemberHistory.class)) {
			FamilyMemberHistory ret = (FamilyMemberHistory) new FamilyMemberHistory().create();
			org.hl7.fhir.dstu3.model.FamilyMemberHistory fhFamilyMemberHistory =
				new org.hl7.fhir.dstu3.model.FamilyMemberHistory();
			fhFamilyMemberHistory.setId(new IdType("FamilyMemberHistory", ret.getId()));
			ModelUtil.saveResource(fhFamilyMemberHistory, ret);
			return type.cast(ret);
		}
		else if (type.equals(IAllergyIntolerance.class)) {
			AllergyIntolerance ret = (AllergyIntolerance) new AllergyIntolerance().create();
			org.hl7.fhir.dstu3.model.AllergyIntolerance fhAllergyIntolerance =
				new org.hl7.fhir.dstu3.model.AllergyIntolerance();
			fhAllergyIntolerance.setId(new IdType("AllergyIntolerance", ret.getId()));
			ModelUtil.saveResource(fhAllergyIntolerance, ret);
			return type.cast(ret);
		}
		return null;
	}
}
