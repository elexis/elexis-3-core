package ch.elexis.core.ui.e4.dialog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.model.Identifiable;

/**
 * Dialog for managing the selection from a list of objects. For each Object a
 * checkbox with text is displayed. For Identifiable instances the Label
 * property is displayed, else toString.
 */
public class GenericSelectionDialog extends TitleAreaDialog {

	private List<?> input;
	private Map<Object, Button> buttonMap = new HashMap<>();
	private List<Object> selection = new LinkedList<>();

	private String title;
	private String message;

	private LabelProvider labelProvider;

	public GenericSelectionDialog(Shell parentShell, List<?> input, String title, String message) {
		super(parentShell);
		this.title = title;
		this.message = message;
		this.input = input;
		setBlockOnOpen(true);
	}

	public GenericSelectionDialog(Shell parentShell, List<?> input) {
		this(parentShell, input, "Auswahl", null);
	}

	public void setSelection(List<Object> selection) {
		this.selection = new LinkedList<>(selection);
	}

	public IStructuredSelection getSelection() {
		return new StructuredSelection(selection);
	}

	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(title);
		setMessage(message);

		Composite ret = (Composite) super.createDialogArea(parent);
		ScrolledComposite sc = new ScrolledComposite(ret, SWT.H_SCROLL | SWT.V_SCROLL);

		Composite child = new Composite(sc, SWT.NONE);
		child.setLayout(new GridLayout());

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 400;
		sc.setLayoutData(data);

		// create the UI
		for (Object object : input) {
			Button button = new Button(child, SWT.CHECK);
			button.setText(getLabel(object));
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (button.getSelection()) {
						selection.add(object);
					} else {
						selection.remove(object);
					}
				}
			});
			buttonMap.put(object, button);
		}

		sc.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setContent(child);

		updateSelectionUi();

		return ret;
	}

	private void updateSelectionUi() {
		if (selection != null && !selection.isEmpty() && !buttonMap.isEmpty()) {
			for (Object object : selection) {
				buttonMap.get(object).setSelection(true);
			}
		}
	}

	private String getLabel(Object object) {
		if (labelProvider != null) {
			return labelProvider.getText(object);
		}
		if (object instanceof Identifiable) {
			return ((Identifiable) object).getLabel();
		} else if (object != null) {
			return object.toString();
		} else {
			return StringUtils.EMPTY;
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
}
