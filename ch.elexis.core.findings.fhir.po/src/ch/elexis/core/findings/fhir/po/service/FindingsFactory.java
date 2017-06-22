package ch.elexis.core.findings.fhir.po.service;

import java.util.Date;

import org.hl7.fhir.dstu3.model.IdType;

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
import ch.elexis.core.findings.util.ModelUtil;

public class FindingsFactory implements IFindingsFactory {
	
	@Override
	public IEncounter createEncounter(){
		Encounter ret = (Encounter) new Encounter().create();
		org.hl7.fhir.dstu3.model.Encounter fhirEncounter = new org.hl7.fhir.dstu3.model.Encounter();
		fhirEncounter.setId(new IdType("Encounter", ret.getId()));
		ModelUtil.saveResource(fhirEncounter, ret);
		return ret;
	}
	
	@Override
	public IObservation createObservation(){
		Observation ret = (Observation) new Observation().create();
		org.hl7.fhir.dstu3.model.Observation fhirOberservation = new org.hl7.fhir.dstu3.model.Observation();
		fhirOberservation.setId(new IdType("Observation", ret.getId()));
		ModelUtil.saveResource(fhirOberservation, ret);
		return ret;
	}
	
	@Override
	public ICondition createCondition(){
		Condition ret = (Condition) new Condition().create();
		org.hl7.fhir.dstu3.model.Condition fhirCondition = new org.hl7.fhir.dstu3.model.Condition();
		fhirCondition.setId(new IdType("Condition", ret.getId()));
		fhirCondition.setAssertedDate(new Date());
		ModelUtil.saveResource(fhirCondition, ret);
		return ret;
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
