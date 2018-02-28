package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.composites.FamilyAnamnesisComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class FamilyAnamnesisViewContribution implements IViewContribution {
	
	FamilyAnamnesisComposite famAnamnesisComposite;
	
	@Override
	public void setUnlocked(boolean unlocked){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLocalizedTitle(){
		return "Familien Anamnese";
	}
	
	@Override
	public boolean isAvailable(){
		return CoreHub.globalCfg.get(IMigratorService.FAMANAM_SETTINGS_USE_STRUCTURED, false);
	}
	
	@Override
	public Composite initComposite(Composite parent){
		famAnamnesisComposite = new FamilyAnamnesisComposite(parent, SWT.NONE);
		return famAnamnesisComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		List<? extends IFinding> iFindings = null;
		if (famAnamnesisComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				iFindings = FindingsServiceComponent.getService().getPatientsFindings(
					((Patient) detailObject).getId(), IFamilyMemberHistory.class);
			}
			
			if (iFindings != null && iFindings.size() >= 1) {
				if (iFindings.size() > 1) {
					MessageDialog.openWarning(famAnamnesisComposite.getShell(), "Familien Anamnese",
						"Mehr als eine familien Anamnese gefunden.\n Nur die letzte familien Anamnese wird angezeigt.");
				}
				famAnamnesisComposite
					.setInput(Optional.of(((List<IFamilyMemberHistory>) iFindings).get(0)));
			} else {
				famAnamnesisComposite.setInput(Optional.empty());
			}
		}
		
	}
}
