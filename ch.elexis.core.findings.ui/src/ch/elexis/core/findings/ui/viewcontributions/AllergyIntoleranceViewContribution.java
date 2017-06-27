package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.composites.AllergyIntoleranceComposite;
import ch.elexis.core.findings.ui.preferences.SettingsConstants;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class AllergyIntoleranceViewContribution implements IViewContribution {
	
	AllergyIntoleranceComposite allergyIntoleranceComposite;
	
	@Override
	public void setUnlocked(boolean unlocked){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLocalizedTitle(){
		return "Allergien";
	}
	
	@Override
	public boolean isAvailable(){
		return CoreHub.globalCfg.get(SettingsConstants.ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED,
			false);
	}
	
	@Override
	public Composite initComposite(Composite parent){
		allergyIntoleranceComposite = new AllergyIntoleranceComposite(parent, SWT.NONE);
		return allergyIntoleranceComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		List<? extends IFinding> iFindings = null;
		if (allergyIntoleranceComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				iFindings = FindingsServiceComponent.getService().getPatientsFindings(
					((Patient) detailObject).getId(), IAllergyIntolerance.class);
			}
			
			if (iFindings != null && iFindings.size() >= 1) {
				if (iFindings.size() > 1) {
					MessageDialog.openWarning(allergyIntoleranceComposite.getShell(),
						"Allergien",
						"Mehr als eine Allergien Einträge gefunden.\n Nur die letzte Allergie wird angezeigt.");
				}
				allergyIntoleranceComposite
					.setInput(Optional.of(((List<IAllergyIntolerance>) iFindings).get(0)));
			} else {
				allergyIntoleranceComposite.setInput(Optional.empty());
			}
		}
		
	}
}
