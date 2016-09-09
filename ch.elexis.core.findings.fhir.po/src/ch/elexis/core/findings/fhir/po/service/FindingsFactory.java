package ch.elexis.core.findings.fhir.po.service;

import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.fhir.po.model.ClinicalImpression;
import ch.elexis.core.findings.fhir.po.model.Condition;
import ch.elexis.core.findings.fhir.po.model.Encounter;
import ch.elexis.core.findings.fhir.po.model.Observation;
import ch.elexis.core.findings.fhir.po.model.ProcedureRequest;

public class FindingsFactory implements IFindingsFactory {
	
	@Override
	public IEncounter createEncounter(){
		Encounter ret = new Encounter();
		return (IEncounter) ret.create();
	}
	
	@Override
	public IObservation createObservation(){
		Observation ret = new Observation();
		return (IObservation) ret.create();
	}
	
	@Override
	public ICondition createCondition(){
		Condition ret = new Condition();
		return (ICondition) ret.create();
	}
	
	@Override
	public IClinicalImpression createClinicalImpression(){
		ClinicalImpression ret = new ClinicalImpression();
		return (IClinicalImpression) ret.create();
	}
	
	@Override
	public IProcedureRequest createProcedureRequest(){
		ProcedureRequest ret = new ProcedureRequest();
		return (IProcedureRequest) ret.create();
	}
}
