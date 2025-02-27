package ch.elexis.core.ui.reminder.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.icons.Images;

public class BooleanExpandableComposite extends Composite {

	private Button checkBtn;

	private Button expandButton;

	private Runnable onExpand;

	public BooleanExpandableComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		setLayout(gl);

		checkBtn = new Button(this, SWT.CHECK);
		checkBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		expandButton = new Button(this, SWT.TOGGLE);
		expandButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		expandButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setExpanded(expandButton.getSelection());
			}
		});

		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd.heightHint = 8;
		separator.setLayoutData(gd);
	}

	public void setMessage(String string) {
		checkBtn.setText(string);
		layout();
	}

	public void addSelectionListener(SelectionListener listener) {
		checkBtn.addSelectionListener(listener);
	}

	public void setSelection(boolean value) {
		checkBtn.setSelection(value);
	}

	public boolean getSelection() {
		return checkBtn.getSelection();
	}

	public void setExpanded(boolean value) {
		if (value) {
			expandButton.setImage(Images.IMG_ARROWUP.getImage());
			for (Control control : getChildren()) {
				if (control != checkBtn && control != expandButton && control.getLayoutData() instanceof GridData) {
					((GridData) control.getLayoutData()).exclude = false;
					control.setVisible(true);
				}
			}
			if (onExpand != null) {
				onExpand.run();
			}
		} else {
			expandButton.setImage(Images.IMG_ARROWDOWN.getImage());
			for (Control control : getChildren()) {
				if (control != checkBtn && control != expandButton && control.getLayoutData() instanceof GridData) {
					((GridData) control.getLayoutData()).exclude = true;
					control.setVisible(false);
				}
			}
		}
		Shell shell = getDialogShell();
		if (shell != null) {
			shell.layout(true, true);
			if (value) {
				shell.pack();
				// set minimum so next pack does not make smaller
				shell.setMinimumSize(shell.getSize());
			}
		} else {
			getParent().layout(true, true);
		}
	}

	private Shell getDialogShell() {
		if (isOnDialog()) {
			Composite parent = getParent();
			do {
				if (parent instanceof Shell) {
					return (Shell) parent;
				}
			} while ((parent = parent.getParent()) != null);
		}
		return null;
	}

	private boolean isOnDialog() {
		// if there is more than on shell in the parent hierarchy
		// assume we are running on a Dialog
		Composite onDialogParent = getParent();
		int shellCnt = 0;
		do {
			if (onDialogParent instanceof Shell) {
				shellCnt++;
			}
		} while ((onDialogParent = onDialogParent.getParent()) != null);
		return shellCnt > 1;
	}

	public void onExpand(Runnable runnable) {
		this.onExpand = runnable;
	}
}
