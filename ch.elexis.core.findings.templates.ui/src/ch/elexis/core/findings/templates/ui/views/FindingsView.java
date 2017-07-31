package ch.elexis.core.findings.templates.ui.views;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;

@Component(service = {})
public class FindingsView extends ViewPart implements IActivationListener {
	
	public static FindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	
	public static IFindingsService findingsService;
	
	
	public FindingsView(){
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsTemplateService(FindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService service){
		codingService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsService(IFindingsService findingsServcie){
		FindingsView.findingsService = findingsServcie;
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		TableViewer viewer = new TableViewer(c, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				// TODO Auto-generated method stub
				if (element instanceof IFinding) {
					Optional<String> t = ((IFinding) element).getText();
					if (t.isPresent()) {
						return getPrefix((IFinding) element) + ": " + t.get();
					}
				}
				return "";
			}
		});
		
		if (ElexisEventDispatcher.getSelectedPatient() != null && ElexisEventDispatcher.getSelectedPatient().exists())
		{
			String patientId = ElexisEventDispatcher.getSelectedPatient().getId();
			List<IFinding> items = getObservations(patientId);
			items.addAll(getConditions(patientId));
			items.addAll(getClinicalImpressions(patientId));
			items.addAll(getPrecedureRequest(patientId));
			viewer.setInput(items);

		}
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	public String getPrefix(IFinding iFinding){
		if (iFinding instanceof IObservation) {
			if (((IObservation) iFinding).getCategory() == ObservationCategory.SOAP_SUBJECTIVE) {
				return "Subjektiv";
			} else if (((IObservation) iFinding)
				.getCategory() == ObservationCategory.SOAP_OBJECTIVE) {
				return "Objektiv";
			} else if (((IObservation) iFinding).getCategory() == ObservationCategory.VITALSIGNS) {
				return "Vitalzeichen";
			}
		} else if (iFinding instanceof ICondition) {
			if (((ICondition) iFinding).getCategory() == ConditionCategory.PROBLEMLISTITEM) {
				return "Problem";
			}
		} else if (iFinding instanceof IClinicalImpression) {
			return "Beurteilung";
		} else if (iFinding instanceof IProcedureRequest) {
			return "Prozedere";
		}
		return "";
	}
	
	private List<IFinding> getObservations(String patientId){
		return findingsService.getPatientsFindings(patientId, IObservation.class).stream()
			.filter(item -> {
				ObservationCategory category = ((IObservation) item).getCategory();
				if (category == ObservationCategory.VITALSIGNS
					|| category == ObservationCategory.SOAP_SUBJECTIVE
					|| category == ObservationCategory.SOAP_OBJECTIVE) {
					return true;
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

	@Override
	public void setFocus(){
		
	}
	

	@Override
	public void activation(boolean mode){
	}
	
	@Override
	public void visible(boolean mode){
		if (!mode) {
			
		}
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
}
