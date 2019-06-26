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
package ch.elexis.core.ui.views.controls;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.data.PersistentObject;

/**
 * A {@link Composite} to manage the selection from a list of objects. A {@link Label} shows the
 * current selction and a {@link Button} is used to open a {@link Dialog} to change the selection.
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

	public GenericSelectionComposite(Composite parent, int style){
		super(parent, style);
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(2, false));
		
		selectLabel = new Label(this, SWT.NONE);
		selectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		selectButton = new Button(this, SWT.NONE);
		selectButton.setText("..."); //$NON-NLS-1$
		selectButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		selectButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e){
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
	
	private void updateLabel(){
		StringBuilder sb = new StringBuilder();
		if (selection != null && !selection.isEmpty()) {
			for (Object object : selection.toList()) {
				String label = GenericSelectionDialog.getLabel(object);
				if (label != null && !label.isEmpty()) {
					if (sb.length() > 0) {
						sb.append(", ").append(label);
					} else {
						sb.append(label);
					}
				}
			}
			selectLabel.setText(sb.toString());
		} else {
			selectLabel.setText("");
		}
		getParent().layout();
	}
	
	public void setInput(List<?> input){
		this.input = input;
	}
	
	private void callSelectionListeners(){
		Object[] listeners = selectionListeners.getListeners();
		if (listeners != null && listeners.length > 0) {
			for (Object object : listeners) {
				((ISelectionChangedListener) object)
					.selectionChanged(new SelectionChangedEvent(this, selection));
			}
		}
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.add(listener);
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.remove(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (selection != null) {
			return selection;
		}
		return StructuredSelection.EMPTY;
	}
	
	@Override
	public void setSelection(ISelection selection){
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
			updateLabel();
		}
	}
	
	/**
	 * Dialog for managing the selection from a list of objects. For each Object a checkbox with
	 * text is displayed. For {@link PersistentObject} instances the Label property is displayed,
	 * else toString.
	 * 
	 * @author thomas
	 *
	 */
	public static class GenericSelectionDialog extends Dialog {
		
		private List<?> input;
		private Map<Object, Button> buttonMap = new HashMap<>();
		private List<Object> selection = new LinkedList<>();
		
		public GenericSelectionDialog(Shell parentShell, List<?> input){
			super(parentShell);
			this.input = input;
		}
		
		public void setSelection(List<Object> selection){
			this.selection = new LinkedList<>(selection);
		}
		
		public IStructuredSelection getSelection(){
			return new StructuredSelection(selection);
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = (Composite) super.createDialogArea(parent);
			ScrolledComposite sc = new ScrolledComposite(ret, SWT.H_SCROLL | SWT.V_SCROLL);
			
			Composite child = new Composite(sc, SWT.NONE);
			child.setLayout(new GridLayout());
			
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			data.heightHint = 400;
			sc.setLayoutData(data);

			Label title = new Label(child, SWT.NONE);
			title.setText("Auswahl:");
			// create the UI
			for (Object object : input) {
				Button button = new Button(child, SWT.CHECK);
				button.setText(getLabel(object));
				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
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
		
		private void updateSelectionUi(){
			if (selection != null && !selection.isEmpty() && !buttonMap.isEmpty()) {
				for (Object object : selection) {
					buttonMap.get(object).setSelection(true);
				}
			}
		}
		
		protected static String getLabel(Object object){
			if (object instanceof PersistentObject) {
				return ((PersistentObject) object).getLabel();
			} else if (object != null) {
				return object.toString();
			} else {
				return "";
			}
		}
	}
}
