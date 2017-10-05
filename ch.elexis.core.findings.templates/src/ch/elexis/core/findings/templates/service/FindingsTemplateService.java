package ch.elexis.core.findings.templates.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.model.DataType;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputData;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.templates.model.Type;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.Patient;

@Component(service = FindingsTemplateService.class)
public class FindingsTemplateService {
	
	private static final String FINDINGS_TEMPLATE_ID_PREFIX = "Findings_Template_";
	
	private IFindingsService findingsService;
	private ICodingService codingService;
	
	public FindingsTemplateService(){
		
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsService(IFindingsService findingsServcie){
		this.findingsService = findingsServcie;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService codingService){
		this.codingService = codingService;
	}
	
	public FindingsTemplates getFindingsTemplates(String templateId){
		Assert.isNotNull(templateId);
		templateId = templateId.replaceAll(" ", "_");
		NamedBlob namedBlob = NamedBlob.load(FINDINGS_TEMPLATE_ID_PREFIX + templateId);
		if (namedBlob.exists() && namedBlob.getString() != null
			&& !namedBlob.getString().isEmpty()) {
			try {
				
				Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
				Map<String, Object> m = reg.getExtensionToFactoryMap();
				m.put("xmi", new XMIResourceFactoryImpl());
				// Obtain a new resource set
				ResourceSet resSet = new ResourceSetImpl();
				
				// Get the resource
				Resource resource = resSet.createResource(URI.createURI("findingsTemplate.xml"));
				resource.load(new URIConverter.ReadableInputStream(namedBlob.getString()), null);
				return (FindingsTemplates) resource.getContents().get(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		ModelFactory factory = ModelFactory.eINSTANCE;
		FindingsTemplates findingsTemplates = factory.createFindingsTemplates();
		findingsTemplates.setId(FINDINGS_TEMPLATE_ID_PREFIX + templateId);
		findingsTemplates.setTitle("Standard Vorlagen");
		return findingsTemplates;
	}
	
	public String createXMI(FindingsTemplates findingsTemplates){
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());
		ResourceSet resSet = new ResourceSetImpl();
		Resource resource = resSet.createResource(URI.createURI("findingsTemplate.xml"));
		resource.getContents().add(findingsTemplates);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try {
			resource.save(os, Collections.EMPTY_MAP);
			os.flush();
			String aString = new String(os.toByteArray(), "UTF-8");
			os.close();
			return aString;
		} catch (IOException e) {
			LoggerFactory.getLogger(FindingsTemplateService.class).error("", e);
		}
		return null;
	}
	
	public void saveFindingsTemplates(Optional<FindingsTemplates> findingsTemplates){
		if (findingsTemplates.isPresent()) {
			String result = createXMI(findingsTemplates.get());
			if (result != null) {
				NamedBlob namedBlob = NamedBlob.load(findingsTemplates.get().getId());
				namedBlob.putString(result);
			} else {
				//cannot save
				LoggerFactory.getLogger(FindingsTemplateService.class)
					.warn("cannot save template - xmi string is null");
			}
		}
		
	}
	
	public void addComponent(IFinding iFinding, FindingsTemplate findingsTemplate){
		if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iFinding;
			ch.elexis.core.findings.ObservationComponent component =
				new ch.elexis.core.findings.ObservationComponent(UUID.randomUUID().toString());
			getOrCreateCode(findingsTemplate.getTitle())
				.ifPresent(code -> component.getCoding().add(code));
			
			if (findingsTemplate.getInputData() instanceof InputDataNumeric) {
				InputDataNumeric inputDataNumeric =
					(InputDataNumeric) findingsTemplate.getInputData();
				
				component.setNumericValue(Optional.empty());
				component.setNumericValueUnit(Optional.of(inputDataNumeric.getUnit()));
				component.getExtensions().put(ObservationComponent.EXTENSION_OBSERVATION_TYPE_URL,
					ObservationType.NUMERIC.name());
			}
			if (findingsTemplate.getInputData() instanceof InputDataText) {
				component.getExtensions().put(ObservationComponent.EXTENSION_OBSERVATION_TYPE_URL,
					ObservationType.TEXT.name());
			}
			iObservation.addComponent(component);
		}
	}
	
	public IFinding createFinding(Patient patient, FindingsTemplate findingsTemplate)
		throws ElexisException{
		IFinding iFinding = null;
		if (patient != null && patient.exists()) {
			validateCycleDetection(findingsTemplate, 0, 100, findingsTemplate.getTitle(), false);
			
			Type type = findingsTemplate.getType();
			
			switch (type) {
			case CONDITION:
				ICondition iCondition = create(ICondition.class);
				iCondition.setCategory(ConditionCategory.PROBLEMLISTITEM);
				iFinding = iCondition;
				setFindingsAttributes(iFinding, patient, findingsTemplate.getTitle());
				break;
			case EVALUATION:
				iFinding = create(IClinicalImpression.class);
				setFindingsAttributes(iFinding, patient, findingsTemplate.getTitle());
				break;
			case OBSERVATION_VITAL:
				iFinding = createObservation(patient, findingsTemplate);
				setFindingsAttributes(iFinding, patient, findingsTemplate.getTitle());
				break;
			case PROCEDURE:
				iFinding = create(IProcedureRequest.class);
				setFindingsAttributes(iFinding, patient, findingsTemplate.getTitle());
				break;
			default:
				break;
			}
		} else {
			throw new ElexisException("Kein Patient ausgewählt.");
		}
		return iFinding;
	}
	
	private Optional<ICoding> getOrCreateCode(String code){
		// if code is not present create a new local code
		codingService.addLocalCoding(new ICoding() {
			
			@Override
			public String getSystem(){
				return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
			}
			
			@Override
			public String getDisplay(){
				return code;
			}
			
			@Override
			public String getCode(){
				return code;
			}
		});
		return codingService.getCode(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(), code);
	}
	
	public void updateOberservationText(IObservation iObservation){
		StringBuilder builder = new StringBuilder();
		builder = getOberservationText(iObservation);
		iObservation.setText(builder.toString());
	}
	
	private StringBuilder getOberservationText(IObservation iObservation){
		StringBuilder builder = new StringBuilder();
		List<ch.elexis.core.findings.ObservationComponent> compChildrens =
			iObservation.getComponents();
		
		addCodingToText(builder, iObservation.getCoding());
		
		for (ch.elexis.core.findings.ObservationComponent component : compChildrens) {
			builder.append(", ");
			addCodingToText(builder, component.getCoding());
		}
		return getObservationTextChildrens(iObservation, builder);
	}
	
	public Optional<ICoding> findOneCode(List<ICoding> coding, CodingSystem codingSystem){
		for (ICoding iCoding : coding) {
			if (codingSystem.getSystem().equals(iCoding.getSystem())) {
				return Optional.of(iCoding);
			}
		}
		return Optional.empty();
	}
	
	private StringBuilder getObservationTextChildrens(IObservation iObservation,
		StringBuilder builder){
		List<IObservation> refChildrens =
			iObservation.getTargetObseravtions(ObservationLinkType.REF);
		for (IObservation child : refChildrens) {
			builder.append(", ");
			addCodingToText(builder, child.getCoding());
			getObservationTextChildrens(child, builder);
		}
		return builder;
	}
	
	private void addCodingToText(StringBuilder builder, List<ICoding> codings){
		ICoding coding =
			findOneCode(codings, CodingSystem.ELEXIS_LOCAL_CODESYSTEM).orElse(null);
		builder.append(coding != null ? coding.getDisplay() : "");
	}
	
	private void setFindingsAttributes(IFinding iFinding, Patient patient, String text){
		if (iFinding != null) {
			iFinding.setPatientId(patient.getId());
			
			if (iFinding instanceof IObservation) {
				IObservation iObservation = (IObservation) iFinding;
				List<ICoding> codings = iObservation.getCoding();
				getOrCreateCode(text).ifPresent(code -> codings.add(code));
				iObservation.setCoding(codings);
			} else {
				iFinding.setText(text);
			}
		}
	}
	
	public void validateCycleDetection(FindingsTemplate findingsTemplate,
		int depth, int maxDepth, String mainTemplateTitle, boolean autoRemoveCycle)
		throws ElexisException{
		if (++depth > maxDepth) {
			StringBuilder builder = new StringBuilder();
			if (autoRemoveCycle) {
				builder.append("Das Hinzufügen der Vorlage '");
				builder.append(mainTemplateTitle);
				builder.append(
					"' ist nicht möglich.\n\nEin Zyklus wurde gefunden, oder die maximal erlaubte Komplexität von ");
				builder.append(maxDepth);
				builder.append(" wurde überschritten.");
			}
			else {
				builder.append("Es trat ein Fehler in der Vorlage auf.\n");
				builder.append("Die maximale Komplexität von ");
				builder.append(maxDepth);
				builder.append(" wurde überschritten.");
				builder.append("\n\n");
				builder.append("Bitte überprüfen Sie ihre Vorlage '");
				builder.append(mainTemplateTitle);
				builder.append("' auf Zyklen, oder verringern Sie die Komplexität.");
			}
			throw new ElexisException(builder.toString());
		}
		InputData inputData = findingsTemplate.getInputData();
		if (inputData instanceof InputDataGroup) {
			InputDataGroup group = (InputDataGroup) inputData;
			for (FindingsTemplate item : group.getFindingsTemplates()) {
				validateCycleDetection(item, depth, maxDepth, mainTemplateTitle, autoRemoveCycle);
			}
		} else if (inputData instanceof InputDataGroupComponent) {
			InputDataGroupComponent group = (InputDataGroupComponent) inputData;
			for (FindingsTemplate item : group.getFindingsTemplates()) {
				validateCycleDetection(item, depth, maxDepth, mainTemplateTitle, autoRemoveCycle);
			}
		}
	}
	
	private IFinding createObservation(Patient patient, FindingsTemplate findingsTemplate)
		throws ElexisException{
		IObservation iObservation = create(IObservation.class);
		iObservation.setEffectiveTime(LocalDateTime.now());
		switch (findingsTemplate.getType()) {
		case OBSERVATION_VITAL:
			iObservation.setCategory(ObservationCategory.VITALSIGNS);
			break;
		default:
			break;
		}
		
		InputData inputData = findingsTemplate.getInputData();
		if (inputData instanceof InputDataGroup) {
			iObservation.setObservationType(ObservationType.REF);
			InputDataGroup group = (InputDataGroup) inputData;
			for (FindingsTemplate findingsTemplates : group.getFindingsTemplates()) {
				IFinding iFinding = createFinding(patient, findingsTemplates);
				if (iFinding instanceof IObservation) {
					IObservation target = (IObservation) iFinding;
					target.setReferenced(true);
					iObservation.addTargetObservation(target, ObservationLinkType.REF);
				}
			}
		} else if (inputData instanceof InputDataGroupComponent) {
			iObservation.setObservationType(ObservationType.COMP);
			InputDataGroupComponent group = (InputDataGroupComponent) inputData;
			for (FindingsTemplate findingsTemplates : group.getFindingsTemplates()) {
				addComponent(iObservation, findingsTemplates);
			}
		} else if (inputData instanceof InputDataNumeric) {
			iObservation.setObservationType(ObservationType.NUMERIC);
			InputDataNumeric inputDataNumeric = (InputDataNumeric) inputData;
			iObservation.setNumericValue(null, inputDataNumeric.getUnit());
		}
		else if (inputData instanceof InputDataText) {
			iObservation.setObservationType(ObservationType.TEXT);
		}
		return iObservation;
	}
	
	public <T extends IFinding> T create(Class<T> clazz){
		return findingsService.create(clazz);
	}
	
	public List<IFinding> getFindings(Patient patient){
		if (patient != null && patient.exists()) {
			String patientId = patient.getId();
			List<IFinding> items = getObservations(patientId);
			/*	TODO currently only observations needed
			 * items.addAll(getConditions(patientId));
				items.addAll(getClinicalImpressions(patientId));
				items.addAll(getPrecedureRequest(patientId));
				*/
			return items;
		}
		return Collections.emptyList();
	}
	
	private List<IFinding> getObservations(String patientId){
		return findingsService.getPatientsFindings(patientId, IObservation.class).stream()
			.filter(item -> {
				IObservation iObservation = (IObservation) item;
				ObservationCategory category = iObservation.getCategory();
				if (category == ObservationCategory.VITALSIGNS
					|| category == ObservationCategory.SOAP_SUBJECTIVE
					|| category == ObservationCategory.SOAP_OBJECTIVE) {
					
					return !iObservation.isReferenced();
				}
				return false;
			}).collect(Collectors.toList());
	};
	
	/* TODO currently not needed
	private List<IFinding> getConditions(String patientId){
		return findingsService.getPatientsFindings(patientId, ICondition.class).stream()
			.filter(item -> {
				ConditionCategory category = ((ICondition) item).getCategory();
				if (category == ConditionCategory.PROBLEMLISTITEM) {
					return true;
				}
				return false;
			}).collect(Collectors.toList());
	};
	
	private List<IFinding> getClinicalImpressions(String patientId){
		return findingsService.getPatientsFindings(patientId, IClinicalImpression.class).stream()
			.collect(Collectors.toList());
	};
	
	private List<IFinding> getPrecedureRequest(String patientId){
		return findingsService.getPatientsFindings(patientId, IProcedureRequest.class).stream()
			.collect(Collectors.toList());
	};
	*/
	public Type getType(IFinding iFinding){
		if (iFinding instanceof IObservation) {
			if (((IObservation) iFinding).getCategory() == ObservationCategory.SOAP_SUBJECTIVE) {
				return Type.OBSERVATION_VITAL;
			} else if (((IObservation) iFinding)
				.getCategory() == ObservationCategory.SOAP_OBJECTIVE) {
				return Type.OBSERVATION_VITAL;
			} else if (((IObservation) iFinding).getCategory() == ObservationCategory.VITALSIGNS) {
				return Type.OBSERVATION_VITAL;
			}
		} else if (iFinding instanceof ICondition) {
			if (((ICondition) iFinding).getCategory() == ConditionCategory.PROBLEMLISTITEM) {
				return Type.CONDITION;
			}
		} else if (iFinding instanceof IClinicalImpression) {
			return Type.EVALUATION;
		} else if (iFinding instanceof IProcedureRequest) {
			return Type.PROCEDURE;
		}
		return null;
	}
	
	public String getTypeAsText(Type type){
		if (type != null) {
			switch (type) {
			case CONDITION:
				return "Problem";
			case EVALUATION:
				return "Beurteilung";
			case OBSERVATION_VITAL:
				return "Beobachtung Vitalzeichen";
			case PROCEDURE:
				return "Prozedere";
			default:
				break;
			
			}
		}
		return "";
	}
	
	public String getDataTypeAsText(DataType dataType){
		switch (dataType) {
		case GROUP:
			return "Gruppe";
		case GROUP_COMPONENT:
			return "Komponent";
		case NUMERIC:
			return "Numerisch";
		case TEXT:
			return "Text";
		default:
			return "";
		
		}
	}
}
