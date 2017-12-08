package ch.elexis.core.findings.fhir.po.service.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseDatatype;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.INarrative;
import org.slf4j.LoggerFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.cache.AlwaysValidCacheEntryValidity;
import org.thymeleaf.cache.ICacheEntryValidity;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.StandardDialect;
import org.thymeleaf.standard.expression.IStandardExpression;
import org.thymeleaf.standard.expression.IStandardExpressionParser;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.DefaultTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IDatatype;
import ca.uhn.fhir.parser.DataFormatException;
import ch.elexis.core.findings.IFindingsService;

public class CustomNarrativeGenerator implements ca.uhn.fhir.narrative.INarrativeGenerator {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomNarrativeGenerator.class);

	private boolean initialized;

	private HashMap<Class<?>, String> classToName;
	private HashMap<String, String> nameToNarrativeTemplate;

	private TemplateEngine profileTemplateEngine;

	@Override
	public void generateNarrative(FhirContext theContext, IBaseResource theResource, INarrative theNarrative) {
		if (!initialized) {
			initialize(theContext);
		}

		String name = null;
		if (name == null) {
			name = classToName.get(theResource.getClass());
		}
		if (name == null) {
			name = theContext.getResourceDefinition(theResource).getName().toLowerCase();
		}

		if (name == null || !nameToNarrativeTemplate.containsKey(name)) {
			logger.debug("No narrative template available for resorce: {}", name);
			return;
		}

		try {
			Context context = new Context();
			context.setVariable("resource", theResource);
			context.setVariable("fhirVersion", theContext.getVersion().getVersion().name());

			String result = profileTemplateEngine.process(name, context);

			if (result == null || result.trim().isEmpty()) {
				return;
			}

			theNarrative.setDivAsString(result);
			theNarrative.setStatusAsString("generated");
			return;
		} catch (Exception e) {
			logger.error("Failed to generate narrative", e);
		}
	}

	private synchronized void initialize(final FhirContext theContext) {
		if (initialized) {
			return;
		}

		logger.info("Initializing narrative generator");

		classToName = new HashMap<Class<?>, String>();
		nameToNarrativeTemplate = new HashMap<String, String>();

		try {
			loadProperties("/rsc/narrative/custom.properties");
		} catch (IOException e) {
			logger.info("Failed to load property file.", e);
		}

		profileTemplateEngine = new TemplateEngine();
		ProfileResourceResolver resolver = new ProfileResourceResolver();
		profileTemplateEngine.setTemplateResolver(resolver);
		StandardDialect dialect = new StandardDialect() {
			public Set<IProcessor> getProcessors(String theDialectPrefix) {
				Set<IProcessor> retVal = super.getProcessors(theDialectPrefix);
				retVal.add(new NarrativeAttributeProcessor(theContext, theDialectPrefix));
				return retVal;
			}
		};
		profileTemplateEngine.setDialect(dialect);

		initialized = true;
	}

	private InputStream loadResource(String name) {
		return IFindingsService.class.getResourceAsStream(name);
	}

	private void loadProperties(String propFileName) throws IOException {
		Properties file = new Properties();
		InputStream resource = IFindingsService.class.getResourceAsStream(propFileName);
		file.load(resource);
		for (Object nextKeyObj : file.keySet()) {
			String nextKey = (String) nextKeyObj;
			if (nextKey.endsWith(".profile")) {
				String name = nextKey.substring(0, nextKey.indexOf(".profile"));
				if (name == null || name.trim().isEmpty()) {
					continue;
				}
				String narrativePropName = name + ".narrative";
				String narrativeName = file.getProperty(narrativePropName);
				if (narrativeName != null && !narrativeName.trim().isEmpty()) {
					String narrative = IOUtils.toString(loadResource(narrativeName), "UTF-8");
					nameToNarrativeTemplate.put(name, narrative);
				}
			} else if (nextKey.endsWith(".class")) {
				String name = nextKey.substring(0, nextKey.indexOf(".class"));
				if (name == null || name.trim().isEmpty()) {
					continue;
				}
				String className = file.getProperty(nextKey);
				Class<?> clazz;
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.debug("Unknown datatype class '{}' identified in narrative file {}", name, propFileName);
					clazz = null;
				}

				if (clazz != null) {
					classToName.put(clazz, name);
				}
			} else if (nextKey.endsWith(".narrative")) {
				String name = nextKey.substring(0, nextKey.indexOf(".narrative"));
				if (name == null || name.trim().isEmpty()) {
					continue;
				}
				String narrativePropName = name + ".narrative";
				String narrativeName = file.getProperty(narrativePropName);
				if (narrativeName != null && !narrativeName.trim().isEmpty()) {
					String narrative = IOUtils.toString(loadResource(narrativeName), "UTF-8");
					nameToNarrativeTemplate.put(name, narrative);
				}
				continue;
			} else {
				throw new ConfigurationException("Invalid property name: " + nextKey);
			}
		}
	}

	public class NarrativeAttributeProcessor extends AbstractAttributeTagProcessor {

		private FhirContext fhirContext;

		protected NarrativeAttributeProcessor(FhirContext theContext, String theDialectPrefix) {
			super(TemplateMode.XML, theDialectPrefix, null, false, "narrative", true, 0, true);
			this.fhirContext = theContext;
		}

		protected void doProcess(ITemplateContext theContext, IProcessableElementTag theTag,
				AttributeName theAttributeName, String theAttributeValue,
				IElementTagStructureHandler theStructureHandler) {
			IEngineConfiguration configuration = theContext.getConfiguration();
			IStandardExpressionParser expressionParser = StandardExpressions.getExpressionParser(configuration);

			final IStandardExpression expression = expressionParser.parseExpression(theContext, theAttributeValue);
			final Object value = expression.execute(theContext);

			if (value == null) {
				return;
			}

			Context context = new Context();
			context.setVariable("fhirVersion", fhirContext.getVersion().getVersion().name());
			context.setVariable("resource", value);

			String name = null;
			if (value != null) {
				Class<? extends Object> nextClass = value.getClass();
				do {
					name = classToName.get(nextClass);
					nextClass = nextClass.getSuperclass();
				} while (name == null && nextClass.equals(Object.class) == false);

				if (name == null) {
					if (value instanceof IBaseResource) {
						name = fhirContext.getResourceDefinition((Class<? extends IBaseResource>) value).getName();
					} else if (value instanceof IDatatype) {
						name = value.getClass().getSimpleName();
						name = name.substring(0, name.length() - 2);
					} else if (value instanceof IBaseDatatype) {
						name = value.getClass().getSimpleName();
						if (name.endsWith("Type")) {
							name = name.substring(0, name.length() - 4);
						}
					} else {
						throw new DataFormatException("Don't know how to determine name for type: " + value.getClass());
					}
					name = name.toLowerCase();
					if (!nameToNarrativeTemplate.containsKey(name)) {
						name = null;
					}
				}
			}

			if (name == null) {
				logger.debug("No narrative template available for type: {}", value.getClass());
				return;
			}

			String result = profileTemplateEngine.process(name, context);
			String trim = result.trim();

			theStructureHandler.setBody(trim, true);
		}
	}

	private final class ProfileResourceResolver extends DefaultTemplateResolver {

		protected boolean computeResolvable(IEngineConfiguration theConfiguration, String theOwnerTemplate,
				String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			String template = nameToNarrativeTemplate.get(theTemplate);
			return template != null;
		}

		protected TemplateMode computeTemplateMode(IEngineConfiguration theConfiguration, String theOwnerTemplate,
				String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return TemplateMode.XML;
		}

		protected ITemplateResource computeTemplateResource(IEngineConfiguration theConfiguration,
				String theOwnerTemplate, String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			String template = nameToNarrativeTemplate.get(theTemplate);
			return new StringTemplateResource(template);
		}

		protected ICacheEntryValidity computeValidity(IEngineConfiguration theConfiguration, String theOwnerTemplate,
				String theTemplate, Map<String, Object> theTemplateResolutionAttributes) {
			return AlwaysValidCacheEntryValidity.INSTANCE;
		}
	}
}
