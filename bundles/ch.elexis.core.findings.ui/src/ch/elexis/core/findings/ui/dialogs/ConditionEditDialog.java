package ch.elexis.core.findings.ui.dialogs;

import java.util.Optional;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ui.composites.ConditionComposite;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;

public class ConditionEditDialog extends TitleAreaDialog {

	private static final int DEFAULT_SHELL_STYLE = SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE | SWT.MIN | SWT.MAX;

	private ConditionComposite conditionComposite;

	private Optional<ICondition> condition = Optional.empty();
	private ConditionCategory category;

	private boolean reusable;

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


	public void setReusable(boolean reusable) {
		this.reusable = reusable;
	}


	public boolean canReuseFor(Shell parentShell, ConditionCategory category) {
		return reusable && this.category == category && getParentShell() == parentShell && parentShell != null
				&& !parentShell.isDisposed() && getShell() != null && !getShell().isDisposed();
	}


	public void preload() {
		if (getShell() == null || getShell().isDisposed()) {
			create();
		}
		if (conditionComposite != null && !conditionComposite.isDisposed()) {
			conditionComposite.preload();
		}
	}

	public boolean isOpen() {
		Shell shell = getShell();
		return shell != null && !shell.isDisposed() && shell.isVisible();
	}

	public ConditionCategory getCategory() {
		return category;
	}


	public void setCondition(ICondition condition) {
		this.condition = Optional.ofNullable(condition);
		if (conditionComposite != null && !conditionComposite.isDisposed()) {
			conditionComposite.setCondition(condition);
		}
		updateTitle();
	}

	@Override
	public void create() {
		super.create();
		updateTitle();
	}

	private void updateTitle() {
		if (getShell() != null && !getShell().isDisposed()) {
			setTitle(category.getLocalized() + " Daten " + (condition.isPresent() ? "editieren" : "anlegen") + ".");
		}
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
	public int open() {
		if (!reusable) {
			return super.open();
		}
		setReturnCode(CANCEL);
		setBlockOnOpen(false);
		super.open();
		return runUntilHidden();
	}


	private int runUntilHidden() {
		Shell shell = getShell();
		if (shell == null || shell.isDisposed()) {
			return getReturnCode();
		}
		Display display = shell.getDisplay();
		while (!shell.isDisposed() && shell.isVisible()) {
			try {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).error("Exception while editing condition", e);
			}
		}
		if (!display.isDisposed()) {
			display.update();
		}
		return getReturnCode();
	}

	@Override
	public boolean close() {
		if (reusable && getShell() != null && !getShell().isDisposed()) {
			getShell().setVisible(false);
			return true;
		}
		return super.close();
	}

	public void dispose() {
		reusable = false;
		if (getShell() != null && !getShell().isDisposed()) {
			super.close();
		}
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
