package ch.elexis.core.findings.fhir.po.dataaccess;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class FindingsDataAccessor implements IDataAccess {
	public static final String FINDINGS_PATIENT_DIAGNOSIS = "Patient Diagnosen";
	public static final String FINDINGS_PATIENT_PERSANAM = "Patient PersAnam";
	public static final String FINDINGS_PATIENT_ALLERGIES = "Patient Allergien";
	public static final String FINDINGS_PATIENT_FAMANAM = "Patient FamAnam";
	public static final String FINDINGS_PATIENT_RISK = "Patient Risk";
	
	private IFindingsService findingsService;
	private ICodingService codingService;
	
	
	ArrayList<Element> elementsList;
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_DIAGNOSIS,
			"[Befunde:-:-:" + FINDINGS_PATIENT_DIAGNOSIS + "]", null, 0),
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_PERSANAM,
			"[Befunde:-:-:" + FINDINGS_PATIENT_PERSANAM + "]", null, 0),
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_ALLERGIES,
			"[Befunde:-:-:" + FINDINGS_PATIENT_ALLERGIES + "]", null, 0),
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_FAMANAM,
			"[Befunde:-:-:" + FINDINGS_PATIENT_FAMANAM + "]", null, 0),
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_RISK,
			"[Befunde:-:-:" + FINDINGS_PATIENT_RISK + "]", null, 0)
	};
	
	public FindingsDataAccessor(){
		// initialize the list of defined elements
		elementsList = new ArrayList<Element>();
		for (int i = 0; i < elements.length; i++) {
			elementsList.add(elements[i]);
		}
		// initialize the findings service references
		findingsService = (IFindingsService) getService(IFindingsService.class);
		codingService = (ICodingService) getService(ICodingService.class);
	}
	
	private Object getService(Class<?> clazz){
		// use osgi service reference to get the service
		BundleContext context =
			FrameworkUtil.getBundle(FindingsDataAccessor.class).getBundleContext();
		if (context != null) {
			ServiceReference<?> serviceReference = context.getServiceReference(clazz.getName());
			if (serviceReference != null) {
				return context.getService(serviceReference);
			}
		}
		return null;
	}
	
	@Override
	public String getName(){
		return "Befunde strukturiert";
	}
	
	@Override
	public String getDescription(){
		return "Befunde strukturiert";
	}
	
	@Override
	public List<Element> getList(){
		return elementsList;
	}
	
	@Override
	public Result<Object> getObject(String descriptor, PersistentObject dependentObject,
		String dates, String[] params){
		Result<Object> result = new Result<Object>("");
		
		Patient patient = null;
		if (dependentObject instanceof Patient) {
			patient = (Patient) dependentObject;
		} else {
			patient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		}
		
		if (patient != null) {
			if (FINDINGS_PATIENT_DIAGNOSIS.equalsIgnoreCase(descriptor)) {
				result = getDiagnosisText(patient);
			} else if (FINDINGS_PATIENT_PERSANAM.equalsIgnoreCase(descriptor)) {
				result = getPersAnamText(patient);
			} else if (FINDINGS_PATIENT_ALLERGIES.equalsIgnoreCase(descriptor)) {
				result = getAllergiesText(patient);
			} else if (FINDINGS_PATIENT_FAMANAM.equalsIgnoreCase(descriptor)) {
				result = getFamAnamText(patient);
			} else if (FINDINGS_PATIENT_RISK.equalsIgnoreCase(descriptor)) {
				result = getRisk(patient);
			}
		}
		return result;
	}
	
	private Result<Object> getRisk(Patient patient){
		List<IObservation> observations =
			findingsService.getPatientsFindings(patient.getId(), IObservation.class);
		observations = observations.parallelStream()
			.filter(iFinding -> TextUtil.isRiskfactor(iFinding)).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		observations.stream().forEach(observation -> {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(TextUtil.getText(observation, codingService));
		});
		return new Result<Object>(sb.toString());
	}
	
	private Result<Object> getFamAnamText(Patient patient){
		List<IFamilyMemberHistory> famanams =
			findingsService.getPatientsFindings(patient.getId(), IFamilyMemberHistory.class);
		StringBuilder sb = new StringBuilder();
		famanams.stream().forEach(famanam -> {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(TextUtil.getText(famanam, codingService));
		});
		return new Result<Object>(sb.toString());
	}
	
	private Result<Object> getAllergiesText(Patient patient){
		List<IAllergyIntolerance> allergies =
			findingsService.getPatientsFindings(patient.getId(), IAllergyIntolerance.class);
		StringBuilder sb = new StringBuilder();
		allergies.stream().forEach(allergy -> {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(TextUtil.getText(allergy, codingService));
		});
		return new Result<Object>(sb.toString());
	}
	
	private Result<Object> getPersAnamText(Patient patient){
		List<IObservation> observations =
			findingsService.getPatientsFindings(patient.getId(), IObservation.class);
		observations = observations.parallelStream()
			.filter(iFinding -> TextUtil.isPersAnamnese(iFinding)).collect(Collectors.toList());
		StringBuilder sb = new StringBuilder();
		observations.stream().forEach(observation -> {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(TextUtil.getText(observation, codingService));
		});
		return new Result<Object>(sb.toString());
	}
	
	private Result<Object> getDiagnosisText(Patient patient){
		List<ICondition> findings =
			findingsService.getPatientsFindings(patient.getId(), ICondition.class);
		List<ICondition> conditions = getDiagnosis(findings);
		StringBuilder sb = new StringBuilder();
		conditions.stream().forEach(condition -> {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(TextUtil.getText(condition, codingService));
		});
		return new Result<Object>(sb.toString());
	}
	
	private List<ICondition> getDiagnosis(List<ICondition> findings){
		List<ICondition> ret = new ArrayList<>();
		findings.stream().forEach(finding -> {
			ICondition iCondition = finding;
			if (iCondition.getCategory().equals(ConditionCategory.PROBLEMLISTITEM)) {
				ret.add(iCondition);
			}
		});
		ret.sort((left, right) -> {
			LocalDate lRecorded =
				left.getDateRecorded().orElse(LocalDate.of(1970, Month.JANUARY, 1));
			LocalDate rRecorded =
				right.getDateRecorded().orElse(LocalDate.of(1970, Month.JANUARY, 1));
			return rRecorded.compareTo(lRecorded);
		});
		return ret;
	}
	

}
