/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.e4.controls;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.e4.dialog.GenericSelectionDialog;

/**
 * A {@link Composite} to manage the selection from a list of objects. A
 * {@link Label} shows the current selction and a {@link Button} is used to open
 * a {@link Dialog} to change the selection.
 *
 * @author thomas
 *
 */
public class GenericSelectionComposite extends Composite implements ISelectionProvider {

	private ListenerList selectionListeners = new ListenerList();

	private List<?> input;
	private IStructuredSelection selection;

	private Label selectLabel;
	private Button selectButton;

	private LabelProvider labelProvider;

	public void setLabelProvider(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	public GenericSelectionComposite(Composite parent, int style) {
		super(parent, style);
		createContent();
	}

	private void createContent() {
		setLayout(new GridLayout(2, false));

		selectLabel = new Label(this, SWT.NONE);
		selectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		selectButton = new Button(this, SWT.NONE);
		selectButton.setText("..."); //$NON-NLS-1$
		selectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		selectButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (input != null && !input.isEmpty()) {
					GenericSelectionDialog dialog = new GenericSelectionDialog(getShell(), input);
					if (selection != null) {
						dialog.setSelection(selection.toList());
					}
					if (dialog.open() == Dialog.OK) {
						setSelection(dialog.getSelection());
						callSelectionListeners();
					}
				}
			}

		});
	}

	private void updateLabel() {
		StringBuilder sb = new StringBuilder();
		if (selection != null && !selection.isEmpty()) {
			for (Object object : selection.toList()) {
				String label = getLabel(object);
				if (label != null && !label.isEmpty()) {
					if (sb.length() > 0) {
						sb.append(", ").append(label); //$NON-NLS-1$
					} else {
						sb.append(label);
					}
				}
			}
			selectLabel.setText(sb.toString());
		} else {
			selectLabel.setText(StringUtils.EMPTY);
		}
		getParent().layout();
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

	public void setInput(List<?> input) {
		this.input = input;
	}

	private void callSelectionListeners() {
		Object[] listeners = selectionListeners.getListeners();
		if (listeners != null && listeners.length > 0) {
			for (Object object : listeners) {
				((ISelectionChangedListener) object).selectionChanged(new SelectionChangedEvent(this, selection));
			}
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		if (selection != null) {
			return selection;
		}
		return StructuredSelection.EMPTY;
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			updateLabel();
		}
	}

}
