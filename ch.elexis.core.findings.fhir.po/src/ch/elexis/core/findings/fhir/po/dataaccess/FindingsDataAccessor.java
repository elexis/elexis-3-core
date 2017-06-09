package ch.elexis.core.findings.fhir.po.dataaccess;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Result;

public class FindingsDataAccessor implements IDataAccess {
	private static final String FINDINGS_PATIENT_DIAGNOSIS = "Patient Diagnosen";
	
	private IFindingsService findingsService;
	private ICodingService codingService;
	
	
	ArrayList<Element> elementsList;
	private Element[] elements = {
		new Element(IDataAccess.TYPE.STRING, FINDINGS_PATIENT_DIAGNOSIS,
			"[Befunde:-:-:" + FINDINGS_PATIENT_DIAGNOSIS + "]", null, 0)
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
		
		Patient patient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		
		if (patient != null) {
			if (descriptor.equalsIgnoreCase(FINDINGS_PATIENT_DIAGNOSIS)) {
				List<IFinding> findings =
					findingsService.getPatientsFindings(patient.getId(), ICondition.class);
				List<ICondition> conditions = getDiagnosis(findings);
				StringBuilder sb = new StringBuilder();
				conditions.stream().forEach(condition -> {
					if (sb.length() > 0) {
						sb.append("\n");
					}
					sb.append(TextUtil.getText(condition, codingService));
				});
				result = new Result<Object>(sb.toString());
			}
		}
		return result;
	}
	
	private List<ICondition> getDiagnosis(List<IFinding> findings){
		List<ICondition> ret = new ArrayList<>();
		findings.stream().forEach(finding -> {
			if (finding instanceof ICondition) {
				ICondition iCondition = (ICondition) finding;
				if (iCondition.getCategory().equals(ConditionCategory.PROBLEMLISTITEM)) {
					ret.add(iCondition);
				}
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
