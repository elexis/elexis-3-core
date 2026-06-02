package ch.elexis.core.findings.ui.viewcontributions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.composites.SocialAnamnesisComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class SocialAnamnesisViewContribution implements IViewContribution {

	private static final Logger log = LoggerFactory.getLogger(SocialAnamnesisViewContribution.class);
	private SocialAnamnesisComposite anamnesisComposite;

	@Override
	public void setUnlocked(boolean unlocked) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getLocalizedTitle() {
		return "Sozialanamnese";
	}

	@Override
	public boolean isAvailable() {
		return ConfigServiceHolder.getGlobal(IMigratorService.SOCANAM_SETTINGS_USE_STRUCTURED, false);
	}

	@Override
	public Composite initComposite(Composite parent) {
		anamnesisComposite = new SocialAnamnesisComposite(parent, SWT.NONE);
		return anamnesisComposite;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData) {
		List<? extends IFinding> observations = null;

		if (anamnesisComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				observations = FindingsServiceComponent.getService()
						.getPatientsFindings(((Patient) detailObject).getId(), IObservation.class);
				observations = observations.stream().filter(finding -> isSocialAnamnesis(finding))
						.collect(Collectors.toList());
			}

			if (observations != null && !observations.isEmpty()) {
				if (observations.size() > 1) {
					log.warn("Multiple social histories found for patient. Showing the latest entry.");
					MessageDialog.openWarning(anamnesisComposite.getShell(), "Sozialanamnese",
							"Mehr als eine Sozialanamnese gefunden.\nNur die letzte wird angezeigt.");
				}
				anamnesisComposite.setInput(Optional.of(((List<IObservation>) observations).get(0)));
			} else {
				anamnesisComposite.setInput(Optional.empty());
			}
		}
	}

	private boolean isSocialAnamnesis(IFinding iFinding) {
		if (iFinding instanceof IObservation
				&& ((IObservation) iFinding).getCategory() == ObservationCategory.SOCIALHISTORY) {
			for (ICoding code : ((IObservation) iFinding).getCoding()) {
				if (code.getCode().equals(ObservationCode.ANAM_SOCIAL.getCode())) {
					return true;
				}
			}
		}
		return false;
	}
}