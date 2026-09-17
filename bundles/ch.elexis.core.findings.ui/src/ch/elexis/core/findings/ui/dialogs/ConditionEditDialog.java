package ch.elexis.core.findings.ui.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ui.composites.ConditionComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class ConditionEditDialog extends TitleAreaDialog {

	private static final int DEFAULT_SHELL_STYLE = SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE | SWT.MIN | SWT.MAX;

	private ConditionComposite conditionComposite;

	private Optional<ICondition> condition = Optional.empty();
	private ConditionCategory category;

	public ConditionEditDialog(ConditionCategory category, Shell parentShell) {
		super(parentShell);
		this.category = category;
		setShellStyle(DEFAULT_SHELL_STYLE);
	}

	public ConditionEditDialog(ICondition condition, Shell parentShell) {
		super(parentShell);
		this.condition = Optional.of(condition);
		this.category = condition.getCategory();
		setShellStyle(DEFAULT_SHELL_STYLE);
	}

	@Override
	public void create() {
		super.create();
		setTitle(category.getLocalized() + " Daten " + (condition.isPresent() ? "editieren" : "anlegen") + ".");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new FillLayout());
		conditionComposite = new ConditionComposite(category, ret, SWT.NONE);
		condition.ifPresent(c -> conditionComposite.setCondition(c));
		return ret;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(900, 950);
	}

	@Override
	protected void okPressed() {
		condition = conditionComposite.getCondition();
		FindingsServiceComponent.getService().saveFinding(condition.get());
		super.okPressed();
	}

	public Optional<ICondition> getCondition() {
		return condition;
	}
}