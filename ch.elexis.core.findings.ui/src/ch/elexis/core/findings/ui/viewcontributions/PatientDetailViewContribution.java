package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.composites.DiagnoseListComposite;
import ch.elexis.core.findings.ui.preferences.SettingsConstants;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class PatientDetailViewContribution implements IViewContribution {
	
	DiagnoseListComposite conditionsComposite;
	
	@Override
	public void setUnlocked(boolean unlocked){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLocalizedTitle(){
		return "Diagnosen";
	}
	
	@Override
	public boolean isAvailable(){
		return CoreHub.globalCfg.get(SettingsConstants.DIAGNOSE_SETTINGS_USE_STRUCTURED, false);
	}
	
	@Override
	public Composite initComposite(Composite parent){
		conditionsComposite = new DiagnoseListComposite(parent, SWT.NONE);
		return conditionsComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		if (conditionsComposite != null && FindingsServiceComponent.getService() != null
			&& detailObject instanceof Patient) {
			List<? extends IFinding> conditions = FindingsServiceComponent.getService()
				.getPatientsFindings(((Patient) detailObject).getId(), ICondition.class);
			conditions = conditions.stream()
				.filter(finding -> (finding instanceof ICondition)
					&& ((ICondition) finding).getCategory() == ConditionCategory.DIAGNOSIS)
				.collect(Collectors.toList());
			conditionsComposite.setInput((List<ICondition>) conditions);
		}
	}
}
