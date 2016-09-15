package ch.elexis.core.findings.fhir.po.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.fhir.po.model.ClinicalImpression;
import ch.elexis.core.findings.fhir.po.model.Condition;
import ch.elexis.core.findings.fhir.po.model.Encounter;
import ch.elexis.core.findings.fhir.po.model.Observation;
import ch.elexis.core.findings.fhir.po.model.ProcedureRequest;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

@Component
public class FindingsService implements IFindingsService {
	
	private Logger logger = LoggerFactory.getLogger(FindingsService.class);
	
	@Override
	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter){
		List<IFinding> ret = new ArrayList<>();
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				ret.addAll(getEncounters(patientId));
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				ret.addAll(getConditions(patientId, null));
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
		}
		return ret;
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
	
	private List<Condition> getConditions(String patientId, String encounterId){
		Query<Condition> query = new Query<>(Condition.class);
		if (patientId != null) {
			query.add(Condition.FLD_PATIENTID, Query.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(Condition.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
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
	
	@Override
	public List<IFinding> getConsultationsFindings(String consultationId,
		Class<? extends IFinding> filter){
		List<IFinding> ret = new ArrayList<>();
		if (consultationId != null && !consultationId.isEmpty()) {
			Optional<IEncounter> encounter = getEncounter(consultationId);
			if (encounter.isPresent()) {
				if (filter.isAssignableFrom(IEncounter.class)) {
					ret.add(encounter.get());
				}
				if (filter.isAssignableFrom(ICondition.class)) {
					ret.addAll(getConditions(null, encounter.get().getId()));
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
		return ret;
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
	public IFindingsFactory getFindingsFactory(){
		return new FindingsFactory();
	}
	
	@Override
	public Optional<IFinding> findById(String idPart){
		// TODO ...
		return Optional.empty();
	}
}
