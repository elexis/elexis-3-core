package ch.elexis.core.findings.ui.viewcontributions;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.composites.AllergyIntoleranceListComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.data.Patient;

public class AllergyIntoleranceViewContribution implements IViewContribution {

	private AllergyIntoleranceListComposite allergyIntoleranceComposite;

	@Override
	public void setUnlocked(boolean unlocked) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getLocalizedTitle() {
		return "Allergien";
	}

	@Override
	public boolean isAvailable() {
		return ConfigServiceHolder.getGlobal(IMigratorService.ALLERGYINTOLERANCE_SETTINGS_USE_STRUCTURED, false);
	}

	@Override
	public Composite initComposite(Composite parent) {
		allergyIntoleranceComposite = new AllergyIntoleranceListComposite(parent, SWT.NONE);
		return allergyIntoleranceComposite;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setDetailObject(Object detailObject, Object additionalData) {
		List<? extends IFinding> iFindings = null;
		if (allergyIntoleranceComposite != null) {
			if (FindingsServiceComponent.getService() != null && detailObject instanceof Patient) {
				iFindings = FindingsServiceComponent.getService().getPatientsFindings(((Patient) detailObject).getId(),
						IAllergyIntolerance.class);
			}

			if (iFindings != null && !iFindings.isEmpty()) {
				allergyIntoleranceComposite.setInput((List<IAllergyIntolerance>) iFindings);
			} else {
				allergyIntoleranceComposite.setInput(Collections.emptyList());
			}
		}
	}
}
