/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation, adapted from JavaAgenda
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * A class to display and let the user change or enter numbers. It will also display a spinner
 * besides the input field to let the user increment or decrement the value by a specific count
 * between a given mi and max value.
 * 
 * @author gerry
 * 
 */
public class NumberInput extends Composite {
	Spinner inp;
	
	/**
	 * Create an empty number input with a default increment of 5, a minimum of 5 and a maximum of
	 * 1440 (which happens to be the number of minutes in a day)
	 * 
	 * @param parent
	 * @param label
	 *            the label to display on top of the input field
	 */
	public NumberInput(Composite parent, String label){
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		new Label(this, SWT.NONE).setText(label);
		inp = new Spinner(this, SWT.NONE);
		inp.setMinimum(5);
		inp.setMaximum(1440);
		inp.setIncrement(5);
	}
	
	/**
	 * set the spinner's minimum, maximum and increment values
	 * 
	 * @param min
	 *            the spinner can not reduce the number lower than min
	 * @param max
	 *            the spinner can not increase the num,ber higher than max
	 * @param inc
	 *            the spinner will increase or decrease every time by inc
	 */
	public void setMinMaxInc(int min, int max, int inc){
		inp.setMinimum(min);
		inp.setMaximum(max);
		inp.setIncrement(inc);
	}
	
	/**
	 * Get the actually displayed value of the input field
	 * 
	 * @return a value that is not necessarily between min and max
	 */
	public int getValue(){
		return inp.getSelection();
	}
	
	public Spinner getControl(){
		return inp;
	}
	
	/**
	 * Set the value to display
	 * 
	 * @param val
	 *            an integer that has not necessarily to be between min and max
	 */
	public void setValue(int val){
		inp.setSelection(val);
	}
}
