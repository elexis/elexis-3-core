package ch.elexis.core.findings.fhir.po.migrator.strategy;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;

public abstract class AbstractMigrationStrategy implements IMigrationStrategy {
	
	protected FindingsTemplateService templateService;
	protected FindingsTemplate template;
	
	@Override
	public void setTemplateService(FindingsTemplateService tempalteService){
		this.templateService = tempalteService;
	}
	
	@Override
	public void setTemplate(FindingsTemplate template){
		this.template = template;
	}
	
}
