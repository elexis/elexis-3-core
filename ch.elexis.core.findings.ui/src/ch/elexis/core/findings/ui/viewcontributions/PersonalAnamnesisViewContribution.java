package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.composites.PersonalAnamnesisComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class PersonalAnamnesisViewContribution implements IViewContribution {
	
	PersonalAnamnesisComposite anamnesisComposite;
	
	@Override
	public void setUnlocked(boolean unlocked){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLocalizedTitle(){
		return "Persönliche Anamnese";
	}
	
	@Override
	public boolean isAvailable(){
		//		return CoreHub.globalCfg.get(SettingsConstants.PERSANAM_SETTINGS_USE_STRUCTURED, false);
		return false;
	}
	
	@Override
	public Composite initComposite(Composite parent){
		anamnesisComposite = new PersonalAnamnesisComposite(parent, SWT.NONE);
		return anamnesisComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		if (anamnesisComposite != null && FindingsServiceComponent.getService() != null
			&& detailObject instanceof Patient) {
			List<? extends IFinding> conditions = FindingsServiceComponent.getService()
				.getPatientsFindings(((Patient) detailObject).getId(), ICondition.class);
			conditions = conditions.stream()
				.filter(finding -> isPersonalAnamnesis(finding))
				.collect(Collectors.toList());
			if (conditions.size() == 1) {
				anamnesisComposite.setInput(Optional.of(((List<ICondition>) conditions).get(0)));
			} else {
				MessageDialog.openWarning(anamnesisComposite.getShell(), "Persönliche Anamnese",
					conditions.isEmpty() ? "Keine persönliche Anamnese gefunden."
							: "Mehr als eine persönliche Anamnese gefunden.");
			}
		} else if (anamnesisComposite != null) {
			anamnesisComposite.setInput(Optional.empty());
		}
	}
	
	private boolean isPersonalAnamnesis(IFinding iFinding){
		return iFinding instanceof ICondition;
		//			&& ((ICondition) iFinding).getCategory() == ConditionCategory.;
	}
}
