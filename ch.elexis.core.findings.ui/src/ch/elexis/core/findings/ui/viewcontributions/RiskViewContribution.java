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
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.composites.RiskComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class RiskViewContribution implements IViewContribution {
	
	RiskComposite riskComposite;
	
	@Override
	public void setUnlocked(boolean unlocked){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getLocalizedTitle(){
		return "Risiken";
	}
	
	@Override
	public boolean isAvailable(){
		return CoreHub.globalCfg.get(IMigratorService.RISKFACTOR_SETTINGS_USE_STRUCTURED, false);
	}
	
	@Override
	public Composite initComposite(Composite parent){
		riskComposite = new RiskComposite(parent, SWT.NONE);
		return riskComposite;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData){
		List<? extends IFinding> observations = null;
		if (riskComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				observations = FindingsServiceComponent.getService()
					.getPatientsFindings(((Patient) detailObject).getId(), IObservation.class);
				observations = observations.stream().filter(finding -> isRisk(finding))
					.collect(Collectors.toList());
			}
			
			if (observations != null && observations.size() >= 1) {
				if (observations.size() > 1) {
					MessageDialog.openWarning(riskComposite.getShell(), "Risiken",
						"Mehr als eine Risiken Einträge gefunden.\n Nur der letzte Risiken Eintrag wird angezeigt.");
				}
				riskComposite
					.setInput(Optional.of(((List<IObservation>) observations).get(0)));
			} else {
				riskComposite.setInput(Optional.empty());
			}
		}
		
	}
	
	private boolean isRisk(IFinding iFinding){
		if (iFinding instanceof IObservation
			&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : ((IObservation) iFinding).getCoding()) {
				if (ObservationCode.ANAM_RISK.isSame(code)) {
					return true;
				}
			}
		}
		return false;
	}
}
