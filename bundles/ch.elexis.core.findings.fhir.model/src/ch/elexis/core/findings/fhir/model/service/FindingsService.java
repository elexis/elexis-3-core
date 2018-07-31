package ch.elexis.core.findings.fhir.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;

@Component
public class FindingsService implements IFindingsService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	private IModelService findingsModelService;
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IFinding> List<T> getPatientsFindings(String patientId, Class<T> filter){
		List<T> ret = new ArrayList<>();
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, IEncounter.class));
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, ICondition.class));
			}
			if (filter.isAssignableFrom(IClinicalImpression.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId,
					IClinicalImpression.class));
			}
			if (filter.isAssignableFrom(IObservation.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, IObservation.class));
			}
			if (filter.isAssignableFrom(IProcedureRequest.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, IProcedureRequest.class));
			}
			if (filter.isAssignableFrom(IFamilyMemberHistory.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, IFamilyMemberHistory.class));
			}
			if (filter.isAssignableFrom(IAllergyIntolerance.class)) {
				ret.addAll((Collection<? extends T>) queryByPatientId(patientId, IAllergyIntolerance.class));
			}
		}
		return (List<T>) ret;
	}
	
	private <T> List<T> queryByPatientId(String patientId, Class<T> clazz){
		if (patientId != null) {
			INamedQuery<T> query = findingsModelService.getNamedQuery(clazz, "patientid");
			return query.executeWithParameters(
				findingsModelService.getParameterMap("patientid", patientId));
		}
		return Collections.emptyList();
	}
	
	private Optional<IEncounter> getEncounter(String consultationId) {
		INamedQuery<IEncounter> query = findingsModelService.getNamedQuery(IEncounter.class, "consultationid");
		List<IEncounter> encounters = query.executeWithParameters(findingsModelService.getParameterMap("consultationid", consultationId));
		if (!encounters.isEmpty()) {
			if (encounters.size() > 1) {
				LoggerFactory.getLogger(getClass())
					.warn("Got more than one encounter for consultation id [" + consultationId
						+ "] using first");
			}
			return Optional.of(encounters.get(0));
		}
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IFinding> List<T> getConsultationsFindings(String consultationId,
		Class<T> filter){
		List<T> ret = new ArrayList<>();
		if (consultationId != null && !consultationId.isEmpty()) {
			Optional<IEncounter> encounter = getEncounter(consultationId);
			if (encounter.isPresent()) {
				if (filter.isAssignableFrom(IEncounter.class)) {
					ret.add((T) encounter.get());
				}
				if (filter.isAssignableFrom(IClinicalImpression.class)) {
					ret.addAll((Collection<? extends T>) queryByEncounterId(encounter.get().getId(),
						IClinicalImpression.class));
				}
				if (filter.isAssignableFrom(IObservation.class)) {
					ret.addAll((Collection<? extends T>) queryByEncounterId(encounter.get().getId(),
						IObservation.class));
				}
				if (filter.isAssignableFrom(IProcedureRequest.class)) {
					ret.addAll((Collection<? extends T>) queryByEncounterId(encounter.get().getId(),
						IProcedureRequest.class));
				}
			}
		}
		return (List<T>) ret;
	}
	
	private <T> List<T> queryByEncounterId(String consultationId, Class<T> clazz){
		if (consultationId != null) {
			INamedQuery<T> query = findingsModelService.getNamedQuery(clazz, "encounterid");
			return query.executeWithParameters(
				findingsModelService.getParameterMap("encounterid", consultationId));
		}
		return Collections.emptyList();
	}
	
	@Override
	public void saveFinding(IFinding finding){
		findingsModelService.save(finding);
	}
	
	@Override
	public void deleteFinding(IFinding finding){
		findingsModelService.delete(finding);
	}
	
	@Override
	public <T extends IFinding> T create(Class<T> type){
		T created = findingsModelService.create(type);
		ModelUtil.initFhir(created, type);
		return created;
	}
	
	@Override
	public <T extends IFinding> Optional<T> findById(String id, Class<T> clazz, boolean skipChecks){
		return findingsModelService.load(id, clazz, skipChecks);
	}
}
