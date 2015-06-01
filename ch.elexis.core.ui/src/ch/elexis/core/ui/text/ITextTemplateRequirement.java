package ch.elexis.core.ui.text;

public interface ITextTemplateRequirement {
	
	/**
	 * get names of required text templates
	 * 
	 * @return
	 */
	public String[] getNamesOfRequiredTextTemplate();
	
	/**
	 * get descriptions to required templates, including their function and usage points
	 * 
	 * @return
	 */
	public String[] getDescriptionsOfRequiredTextTemplate();
	
}