package ch.elexis.core.findings.fhir.po.migrator.strategy;

import java.util.Optional;

import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;

public interface IMigrationStrategy {
	
	public Optional<IObservation> migrate();
	
	public void setTemplateService(FindingsTemplateService tempalteService);
	
	public void setTemplate(FindingsTemplate template);
}
