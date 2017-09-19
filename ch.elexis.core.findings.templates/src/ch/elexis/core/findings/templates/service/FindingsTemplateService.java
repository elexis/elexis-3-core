package ch.elexis.core.findings.templates.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputData;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.ModelFactory;
import ch.elexis.core.findings.templates.model.Type;
import ch.elexis.data.NamedBlob;
import ch.elexis.data.Patient;

@Component(service = FindingsTemplateService.class)
public class FindingsTemplateService {
	
	private static final String FINDINGS_TEMPLATE_ID = "Findings_Template_1";
	
	private IFindingsService findingsService;
	
	public FindingsTemplateService(){
		
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsService(IFindingsService findingsServcie){
		this.findingsService = findingsServcie;
	}
	
	public FindingsTemplates getFindingsTemplates(){
		
		NamedBlob namedBlob = NamedBlob.load(FINDINGS_TEMPLATE_ID);
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
		findingsTemplates.setId(FINDINGS_TEMPLATE_ID);
		findingsTemplates.setTitle("Vorlagen");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void saveFindingsTemplates(Optional<FindingsTemplates> findingsTemplates){
		if (findingsTemplates.isPresent()) {
			String result = createXMI(findingsTemplates.get());
			if (result != null) {
				NamedBlob namedBlob = NamedBlob.load(findingsTemplates.get().getId());
				namedBlob.putString(result);
			}
			else {
				//cannot save
			}
		}
		
	}
	
	public IFinding createFinding(Patient patient, FindingsTemplate findingsTemplate){
		IFinding iFinding = null;
		if (patient != null && patient.exists()) {
			Type type = findingsTemplate.getType();
			switch (type) {
			case CONDITION:
				ICondition iCondition = create(ICondition.class);
				iCondition.setCategory(ConditionCategory.PROBLEMLISTITEM);
				iFinding = iCondition;
				break;
			case EVALUATION:
				iFinding = create(IClinicalImpression.class);
				break;
			case OBSERVATION:
			case OBSERVATION_OBJECTIVE:
			case OBSERVATION_SUBJECTIVE:
			case OBSERVATION_VITAL:
				iFinding = createObservation(patient, findingsTemplate);
				break;
			case PROCEDURE:
				iFinding = create(IProcedureRequest.class);
				break;
			default:
				break;
			}
			if (iFinding != null) {
				iFinding.setPatientId(patient.getId());
				iFinding.setText(findingsTemplate.getTitle());
			}
		}
		return iFinding;
	}
	
	private IFinding createObservation(Patient patient, FindingsTemplate findingsTemplate){
		IObservation iObservation = create(IObservation.class);
		switch (findingsTemplate.getType()) {
		case OBSERVATION_OBJECTIVE:
			iObservation.setCategory(ObservationCategory.SOAP_OBJECTIVE);
			break;
		case OBSERVATION_SUBJECTIVE:
			iObservation.setCategory(ObservationCategory.SOAP_SUBJECTIVE);
			break;
		case OBSERVATION_VITAL:
		case OBSERVATION:
			iObservation.setCategory(ObservationCategory.VITALSIGNS);
			break;
		default:
			break;
		}
		
		InputData inputData = findingsTemplate.getInputData();
		if (inputData instanceof InputDataGroup) {
			InputDataGroup group = (InputDataGroup) inputData;
			for (FindingsTemplate findingsTemplates : group.getFindingsTemplates()) {
				IFinding iFinding = createFinding(patient, findingsTemplates);
				if (iFinding instanceof IObservation) {
					IObservation target = (IObservation) iFinding;
					iObservation.addTargetObservation(target, ObservationLinkType.REF);
				}
			}
		} else if (inputData instanceof InputDataGroupComponent) {
			InputDataGroupComponent group = (InputDataGroupComponent) inputData;
			for (FindingsTemplate findingsTemplates : group.getFindingsTemplates()) {
				IFinding iFinding = createFinding(patient, findingsTemplates);
				if (iFinding instanceof IObservation) {
					IObservation target = (IObservation) iFinding;
					iObservation.addTargetObservation(target, ObservationLinkType.COMP);
				}
			}
		}
		else if (inputData instanceof InputDataNumeric) {
			InputDataNumeric inputDataNumeric = (InputDataNumeric) inputData;
			BigDecimal bigDecimal = new BigDecimal(0);
			bigDecimal.setScale(inputDataNumeric.getDecimalPlace());
			iObservation.setNumericValue(bigDecimal, inputDataNumeric.getUnit());
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
			items.addAll(getConditions(patientId));
			items.addAll(getClinicalImpressions(patientId));
			items.addAll(getPrecedureRequest(patientId));
			return items;
		}
		return Collections.emptyList();
	}
	
	private List<IFinding> getObservations(String patientId){
		return findingsService.getPatientsFindings(patientId, IObservation.class).stream()
			.filter(item -> {
				ObservationCategory category = ((IObservation) item).getCategory();
				if (category == ObservationCategory.VITALSIGNS
					|| category == ObservationCategory.SOAP_SUBJECTIVE
					|| category == ObservationCategory.SOAP_OBJECTIVE) {
					
					return item.getSourceObservations(ObservationLinkType.COMP).isEmpty()
						&& item.getSourceObservations(ObservationLinkType.REF).isEmpty(); // has no parents
				}
				return false;
			}).collect(Collectors.toList());
	};
	
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
	
	public Type getType(IFinding iFinding){
		if (iFinding instanceof IObservation) {
			if (((IObservation) iFinding).getCategory() == ObservationCategory.SOAP_SUBJECTIVE) {
				return Type.OBSERVATION_SUBJECTIVE;
			} else if (((IObservation) iFinding)
				.getCategory() == ObservationCategory.SOAP_OBJECTIVE) {
				return Type.OBSERVATION_OBJECTIVE;
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
	
	public String getTypeAsText(IFinding iFinding){
		Type type = getType(iFinding);
		if (type != null) {
			switch (type) {
			case CONDITION:
				return "Problem";
			case EVALUATION:
				return "Beurteilung";
			case OBSERVATION:
				break;
			case OBSERVATION_OBJECTIVE:
				return "Objektiv";
			case OBSERVATION_SUBJECTIVE:
				return "Subjektiv";
			case OBSERVATION_VITAL:
				return "Vitalzeichen";
			case PROCEDURE:
				return "Prozedere";
			default:
				break;
			
			}
		}
		return "";
	}
	
}
