package ch.elexis.core.data.lab;

import ch.elexis.core.types.PathologicDescription;

public class LabResultEvaluationResult {
	
	/**
	 * the final state has been determined by the evaluator, no further evaluation required
	 */
	private boolean finallyDetermined;
	private boolean isPathologic;
	private PathologicDescription pathologicDescription;
	
	public LabResultEvaluationResult(boolean finallyDetermined){
		this.finallyDetermined = finallyDetermined;
	}
	
	public LabResultEvaluationResult(boolean finallyDetermined, boolean isPathologic,
		PathologicDescription pathologicDescription){
		this.finallyDetermined = finallyDetermined;
		this.isPathologic = isPathologic;
		this.pathologicDescription = pathologicDescription;
	}
	
	public boolean isFinallyDetermined(){
		return finallyDetermined;
	}
	
	public PathologicDescription getPathologicDescription(){
		return pathologicDescription;
	}
	
	public boolean isPathologic(){
		return isPathologic;
	}
	
}
