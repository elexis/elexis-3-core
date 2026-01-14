package ch.elexis.core.fhir.mapper.r4.helper;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFinding.RawContentFormat;

public class FindingsContentHelper {
	private static FhirContext context;

	private IParser jsonParser;

	public FhirContext getFhirContext() {
		if (context == null) {
			context = FhirContext.forR4();
		}
		return context;
	}

	public IParser getJsonParser() {
		if (jsonParser == null) {
			jsonParser = getFhirContext().newJsonParser();
		}
		return jsonParser;
	}

	private IParser xmlParser;

	public IParser getXmlParser() {
		if (xmlParser == null) {
			xmlParser = getFhirContext().newXmlParser();
		}
		return xmlParser;
	}

	private Logger getLogger() {
		return LoggerFactory.getLogger(FindingsContentHelper.class);
	}

	public Optional<IBaseResource> getResource(IFinding finding) throws DataFormatException {
		IBaseResource resource = null;
		if (finding != null) {
			RawContentFormat contentFormat = finding.getRawContentFormat();
			if (contentFormat == RawContentFormat.FHIR_JSON) {
				String jsonContent = finding.getRawContent();
				if (jsonContent != null && !jsonContent.isEmpty()) {
					if (finding.getRawContent() != null && !finding.getRawContent().isEmpty()) {
						resource = getJsonParser().parseResource(finding.getRawContent());
					}
				}
			} else if (contentFormat == RawContentFormat.FHIR_XML) {
				String xmlContent = finding.getRawContent();
				if (xmlContent != null && !xmlContent.isEmpty()) {
					if (finding.getRawContent() != null && !finding.getRawContent().isEmpty()) {
						resource = getXmlParser().parseResource(finding.getRawContent());
					}
				}
			} else {
				getLogger().error("Could not get resource because of unknown content format [" + contentFormat + "]");
			}
		}
		return Optional.ofNullable(resource);
	}

	public void setResource(BaseResource resource, IFinding finding) throws DataFormatException {
		RawContentFormat contentFormat = finding.getRawContentFormat();
		if (contentFormat == RawContentFormat.FHIR_JSON) {
			String jsonContent = finding.getRawContent();
			if (jsonContent != null && !jsonContent.isEmpty()) {
				if (resource != null) {
					if (resource.getId() == null) {
						resource.setId(new IdType(resource.getClass().getSimpleName(), finding.getId()));
					}
					String resourceJson = getJsonParser().encodeResourceToString(resource);
					finding.setRawContent(resourceJson);
				}
			}
		} else if (contentFormat == RawContentFormat.FHIR_XML) {
			String xmlContent = finding.getRawContent();
			if (xmlContent != null && !xmlContent.isEmpty()) {
				if (resource != null) {
					if (resource.getIdElement() == null) {
						resource.setId(new IdType(resource.getClass().getSimpleName(), finding.getId()));
					}
					String resourceJson = getXmlParser().encodeResourceToString(resource);
					finding.setRawContent(resourceJson);
				}
			}
		} else {
			getLogger().error("Could not get resource because of unknown content format [" + contentFormat + "]");
		}
	}
}
