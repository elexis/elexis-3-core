package ch.elexis.core.findings.ui.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;
import ch.elexis.core.findings.ui.composites.AllergyIntoleranceComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class AllergyIntoleranceEditDialog extends TitleAreaDialog {

	private AllergyIntoleranceComposite conditionComposite;

	private Optional<IAllergyIntolerance> allergyIntolerance = Optional.empty();
	private AllergyIntoleranceCategory category;

	public AllergyIntoleranceEditDialog(AllergyIntoleranceCategory category, Shell parentShell) {
		super(parentShell);
		this.category = category;
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
	}

	public AllergyIntoleranceEditDialog(IAllergyIntolerance allergyIntolerance, Shell parentShell) {
		super(parentShell);
		this.allergyIntolerance = Optional.of(allergyIntolerance);
		this.category = allergyIntolerance.getCategory();
		setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE);
	}

	@Override
	public void create() {
		super.create();
		setTitle(
				category.getLocalized() + " Daten " + (allergyIntolerance.isPresent() ? "editieren" : "anlegen") + ".");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new FillLayout());
		conditionComposite = new AllergyIntoleranceComposite(ret, SWT.NONE);
		conditionComposite.setAllergyIntolerance(allergyIntolerance);
		return ret;
	}

	@Override
	protected void okPressed() {
		allergyIntolerance = conditionComposite.getAllergyIntolerance();
		FindingsServiceComponent.getService().saveFinding(allergyIntolerance.get());
		super.okPressed();
	}

	public Optional<IAllergyIntolerance> getAllergyIntolerance() {
		return allergyIntolerance;
	}
}
