package ch.elexis.core.findings.fhir.po.migrator.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.fhir.po.migrator.messwert.MesswertFieldMapping;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;

public class MesswertMigrationStrategyFactory {
	
	private static Logger logger = LoggerFactory.getLogger(MesswertMigrationStrategyFactory.class);
	
	private static FindingsTemplateService tempalteService;
	
	private static HashMap<String, FindingsTemplate> codeToTemplateMap = new HashMap<>();
	
	public static IMigrationStrategy get(MesswertFieldMapping mapping, Messwert messwert,
		List<IObservation> createdObservations){
		
		FindingsTemplate template = getTemplate(mapping);
		if (template != null) {
			if (template.getInputData() instanceof InputDataGroupComponent) {
				IMigrationStrategy migration =
					new ComponentMigration(mapping, messwert, createdObservations);
				migration.setTemplateService(tempalteService);
				migration.setTemplate(template);
				return migration;
			} else {
				if (template.getInputData() instanceof InputDataNumeric) {
					IMigrationStrategy migration = new NumericMigration(mapping, messwert);
					migration.setTemplateService(tempalteService);
					migration.setTemplate(template);
					return migration;
				} else if (template.getInputData() instanceof InputDataText) {
					IMigrationStrategy migration = new TextMigration(mapping, messwert);
					migration.setTemplateService(tempalteService);
					migration.setTemplate(template);
					return migration;
				}
			}
		}
		// default no migration strategy
		return new AbstractMigrationStrategy() {
			@Override
			public Optional<IObservation> migrate(){
				logger.warn("No migration available for mapping " + mapping.getLocalBefund() + "."
					+ mapping.getLocalBefundField() + " to " + mapping.getFindingsCode()
					+ " using template " + ((template != null) ? template.getTitle() : "none"));
				return Optional.empty();
			}
		};
	}
	
	private static FindingsTemplate getTemplate(MesswertFieldMapping mapping){
		FindingsTemplate template = codeToTemplateMap.get(mapping.getFindingsCode());
		if (template == null) {
			FindingsTemplates availableTemplates =
				tempalteService.getFindingsTemplates("Standard Vorlagen");
			
			String[] parts = mapping.getFindingsCode().split("\\.");
			if (parts.length > 0) {
				for (FindingsTemplate fTemplate : availableTemplates.getFindingsTemplates()) {
					if (fTemplate.getTitle().equals(parts[0])) {
						codeToTemplateMap.put(mapping.getFindingsCode(), fTemplate);
						template = fTemplate;
						break;
					}
				}
			}
		}
		return template;
	}
	
	public static void setFindingsTemplateService(FindingsTemplateService service){
		tempalteService = service;
	}
	
	public static void clearCodeToTemplateCache(){
		codeToTemplateMap.clear();
	}
}
