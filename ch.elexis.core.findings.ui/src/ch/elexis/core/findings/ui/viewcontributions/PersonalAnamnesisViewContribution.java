package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.ui.composites.PersonalAnamnesisComposite;
import ch.elexis.core.findings.ui.preferences.SettingsConstants;
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
		return CoreHub.globalCfg.get(SettingsConstants.PERSANAM_SETTINGS_USE_STRUCTURED, false);
	}
	
	@Override
	public Composite initComposite(Composite parent){
		anamnesisComposite = new PersonalAnamnesisComposite(parent, SWT.NONE);
		return anamnesisComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		List<? extends IFinding> observations = null;
		if (anamnesisComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				observations = FindingsServiceComponent.getService()
					.getPatientsFindings(((Patient) detailObject).getId(), IObservation.class);
				observations = observations.stream().filter(finding -> isPersonalAnamnesis(finding))
					.collect(Collectors.toList());
			}
			
			if (observations != null && observations.size() >= 1) {
				if (observations.size() > 1) {
					MessageDialog.openWarning(anamnesisComposite.getShell(), "Persönliche Anamnese",
						"Mehr als eine persönliche Anamnese gefunden.\n Nur die letzte persönliche Anamnese wird angezeigt.");
				}
				anamnesisComposite
					.setInput(Optional.of(((List<IObservation>) observations).get(0)));
			} else {
				anamnesisComposite.setInput(Optional.empty());
			}
		}
		
	}
	
	private boolean isPersonalAnamnesis(IFinding iFinding){
		if (iFinding instanceof IObservation
			&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : ((IObservation) iFinding).getCoding()) {
				if (ObservationCode.ANAM_PERSONAL.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
}
