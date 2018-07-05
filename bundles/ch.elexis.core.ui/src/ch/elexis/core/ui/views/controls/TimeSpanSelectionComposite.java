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

import ch.rgw.tools.TimeSpan;
import ch.rgw.tools.TimeTool;

public class TimeSpanSelectionComposite extends Composite implements ISelectionProvider {
	
	private ListenerList selectionListeners = new ListenerList();
	
	private DateTime timespanFrom;
	private DateTime timespanTo;
	
	private TimeSpan timeSpan;
	
	public TimeSpanSelectionComposite(Composite parent, int style){
		super(parent, style);
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(4, false));
		Label label = new Label(this, SWT.NONE);
		label.setText("Von");
		timespanFrom = new DateTime(this, SWT.NONE);
		timespanFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				updateTimeSpan(timespanFrom);
				callSelectionListeners();
			}
		});
		label = new Label(this, SWT.NONE);
		label.setText("Bis");
		timespanTo = new DateTime(this, SWT.NONE);
		timespanTo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				updateTimeSpan(timespanTo);
				callSelectionListeners();
			}
		});
	}
	
	private void updateTimeSpan(DateTime dateTime){
		if (timeSpan == null) {
			timeSpan = new TimeSpan();
		}
		if (timespanFrom == dateTime) {
			setDateTime(dateTime, timeSpan.from);
		} else if (timespanTo == dateTime) {
			setDateTime(dateTime, timeSpan.until);
		}
	}
	
	public void setTimeSpan(TimeSpan timeSpan){
		this.timeSpan = timeSpan;
		if(timeSpan != null) {
			setDate(timeSpan.from, timespanFrom);
			setDate(timeSpan.until, timespanTo);
		}
	}
	
	/**
	 * Update the date value of the DateTime.
	 * 
	 * @param time
	 * @param dateTime
	 */
	private void setDate(TimeTool time, DateTime dateTime){
		dateTime.setDay(time.get(TimeTool.DAY_OF_MONTH));
		dateTime.setMonth(time.get(TimeTool.MONTH));
		dateTime.setYear(time.get(TimeTool.YEAR));
	}
	
	/**
	 * Update the date value of the TimeTool.
	 * 
	 * @param time
	 * @param dateTime
	 */
	private void setDateTime(DateTime dateTime, TimeTool time){
		time.set(TimeTool.DAY_OF_MONTH, dateTime.getDay());
		time.set(TimeTool.MONTH, dateTime.getMonth());
		time.set(TimeTool.YEAR, dateTime.getYear());
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
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.add(listener);
	}
	
	@Override
	public ISelection getSelection(){
		if (timeSpan != null) {
			return new StructuredSelection(timeSpan);
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
				if (element instanceof TimeSpan) {
					setTimeSpan((TimeSpan) element);
				}
			} else {
				setTimeSpan(null);
			}
		}
	}
}
