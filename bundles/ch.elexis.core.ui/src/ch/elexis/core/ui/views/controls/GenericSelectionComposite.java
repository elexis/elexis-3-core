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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.AbstractTableViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.data.PersistentObject;

/**
 * A {@link Composite} to manage the selection from a list of objects. A {@link Label} shows the
 * current selction and a {@link Button} is used to open a {@link Dialog} to change the selection.
 * 
 * @author thomas
 *
 */
public class GenericSelectionComposite extends Composite implements ISelectionProvider {
	
	private ListenerList<ISelectionChangedListener> selectionListeners =
		new ListenerList<ISelectionChangedListener>();
	
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
					GenericSelectionDialog dialog = new GenericSelectionDialog(getShell(), input,
						"Auswahl", null, null, SWT.MULTI);
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
	public static class GenericSelectionDialog extends TitleAreaDialog {
		
		private List<?> input;
		private List<Object> selection = new LinkedList<>();
		
		private String title;
		private String message;
		private Image image;
		private int style;
		
		private AbstractTableViewer structuredViewer;
		private SearchDataDialog filter;
		
		public GenericSelectionDialog(Shell parentShell, List<?> input, String title,
			String message, Image image, int style){
			super(parentShell);
			this.title = title;
			this.message = message;
			this.input = input;
			this.image = image;
			this.style = style;
		}
		
		public void setSelection(List<Object> selection){
			this.selection = new LinkedList<>(selection);
		}
		
		public IStructuredSelection getSelection(){
			return new StructuredSelection(selection);
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			setTitle(title);
			setMessage(message);
			setTitleImage(image);
			
			Composite ret = (Composite) super.createDialogArea(parent);
			
			Text text = new Text(ret, SWT.BORDER);
			GridData textGridData = new GridData();
			textGridData.grabExcessVerticalSpace = false;
			textGridData.grabExcessHorizontalSpace = true;
			textGridData.horizontalAlignment = GridData.FILL;
			textGridData.verticalAlignment = GridData.BEGINNING;
			text.setLayoutData(textGridData);
			
			Collections.sort(input, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2){
					if (o1 instanceof PersistentObject) {
						return ((PersistentObject) o1).getLabel()
							.compareToIgnoreCase(((PersistentObject) o2).getLabel());
					}
					return 0;
				}
			});
			
			if (style == SWT.SINGLE) {
				structuredViewer = new TableViewer(ret, SWT.NONE);
			} else {
				structuredViewer = CheckboxTableViewer.newCheckList(ret, SWT.NONE);
			}
			
			GridData viewerGridData = new GridData(GridData.FILL_BOTH);
			viewerGridData.heightHint = 250;
			viewerGridData.widthHint = 300;
			((TableViewer) structuredViewer).getTable().setLayoutData(viewerGridData);
			structuredViewer.setContentProvider(ArrayContentProvider.getInstance());
			
			structuredViewer.setLabelProvider(new LabelProvider() {
				public String getText(Object elements){
					if (elements instanceof PersistentObject) {
						return ((PersistentObject) elements).getLabel();
					} else if (elements != null) {
						return elements.toString();
					}
					return null;
				}
			});
			structuredViewer.setInput(input.toArray());
			
			text.addKeyListener(new KeyAdapter() {
				public void keyReleased(KeyEvent keyEvent){
					filter.setSearchText(text.getText());
					structuredViewer.refresh();
					isLastElement(structuredViewer);
				}
			});
			
			filter = new SearchDataDialog();
			structuredViewer.addFilter(filter);
			
			return ret;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void okPressed(){
			if (style == SWT.SINGLE) {
				IStructuredSelection selection = structuredViewer.getStructuredSelection();
				this.selection = (selection.toList());
			} else {
				this.selection =
					Arrays.asList(((CheckboxTableViewer) structuredViewer).getCheckedElements());
			}
			super.okPressed();
		}
		
		private void isLastElement(StructuredViewer structuredViewer){
			if (((TableViewer) structuredViewer).getTable().getItems().length == 1) {
				((TableViewer) structuredViewer).getTable().getItem(0).setChecked(true);
				((TableViewer) structuredViewer).getTable().setSelection(0);
			} else {
				((CheckboxTableViewer) structuredViewer).setAllChecked(false);
				structuredViewer.setSelection(null);
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
	
	public static class SearchDataDialog extends ViewerFilter {
		
		private String searchString;
		
		public void setSearchText(String search){
			this.searchString = ".*" + search + ".*";
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			
			if (element instanceof PersistentObject) {
				PersistentObject pObject = (PersistentObject) element;
				if (pObject.getLabel().toLowerCase().matches(searchString.toLowerCase())) {
					return true;
				}
			} else if (element != null) {
				if (element.toString().toLowerCase().matches(searchString.toLowerCase())) {
					return true;
				}
			}
			return false;
		}
	}
}
