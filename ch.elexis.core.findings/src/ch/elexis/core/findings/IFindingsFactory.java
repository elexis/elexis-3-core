package ch.elexis.core.findings;

public interface IFindingsFactory {
	
	public IEncounter createEncounter();
	
	public IObservation createObservation();
	
	public ICondition createCondition();
	
	public IClinicalImpression createClinicalImpression();
	
	public IProcedureRequest createProcedureRequest();
}
