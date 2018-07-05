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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class DaysOrDateSelectionComposite extends Composite implements ISelectionProvider {
	
	private ListenerList selectionListeners = new ListenerList();
	
	private Spinner days;
	private DateTime date;
	
	private LocalDate dateValue;
	private int daysValue;
	
	public DaysOrDateSelectionComposite(Composite parent, int style){
		super(parent, style);
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(4, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Tage");
		days = new Spinner(this, SWT.BORDER);
		days.setMaximum(999);
		days.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				updateDays();
				callSelectionListeners();
			}
		});
		
		label = new Label(this, SWT.NONE);
		label.setText("Datum");
		date = new DateTime(this, SWT.NONE);
		date.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				updateDate();
				callSelectionListeners();
			}
		});
		// initialize values and ui
		setDate(LocalDate.now());
	}
	
	private void updateDays(){
		daysValue = days.getSelection();
		dateValue = LocalDate.now().minusDays(days.getSelection());
		date.setDate(dateValue.getYear(), dateValue.getMonthValue() - 1, dateValue.getDayOfMonth());
	}
	
	private void updateDate(){
		dateValue = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDay());
		daysValue = (int) ChronoUnit.DAYS.between(dateValue, LocalDate.now());
		days.setSelection(daysValue);
	}
	
	/**
	 * Update the date value and the days value.
	 * 
	 * @param newDate
	 */
	public void setDate(LocalDate newDate){
		if (newDate != null) {
			dateValue = newDate;
			date.setDate(dateValue.getYear(), dateValue.getMonthValue() - 1,
				dateValue.getDayOfMonth());
			daysValue = (int) ChronoUnit.DAYS.between(dateValue, LocalDate.now());
			days.setSelection(daysValue);
		}
	}
	
	private void callSelectionListeners(){
		Object[] listeners = selectionListeners.getListeners();
		if (listeners != null && listeners.length > 0) {
			for (Object object : listeners) {
				((ISelectionChangedListener) object)
					.selectionChanged(new SelectionChangedEvent(this, getSelection()));
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.add(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (dateValue != null) {
			return new StructuredSelection(dateValue);
		}
		return StructuredSelection.EMPTY;
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.remove(listener);
	}
	
	@Override
	public void setSelection(ISelection selection){
		if (selection instanceof IStructuredSelection) {
			if (!selection.isEmpty()) {
				Object element = ((IStructuredSelection) selection).getFirstElement();
				if (element instanceof LocalDate) {
					setDate((LocalDate) element);
				}
			} else {
				setDate(null);
			}
		}
	}
}
