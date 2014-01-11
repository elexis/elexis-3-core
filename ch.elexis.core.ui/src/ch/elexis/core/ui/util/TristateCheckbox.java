/*******************************************************************************
 * Copyright (c) 2013, H. Marlovits and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    H. Marlovits - initial implementation
 *******************************************************************************/
package ch.elexis.core.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.data.PersistentObject.TristateBoolean;

/*
 * how to set true/false/undef for checkbox composites:<br>
 * 	true/checked:. . selection = true<br>
 * . . . . . . . . . grayed = false<br>
 * 	false/unchecked: selection = false<br>
 * . . . . . . . . . grayed = false<br>
 * 	undef: . . . . . selection = true<br>
 * . . . . . . . . . grayed = true<br>
 */

/**
 * A tristate checkbox. The states cycle through "filled with a square"/undefined/"partly selected",
 * checked/true and unchecked/false.<br>
 * The starting state is undef/filled with a square<br>
 * To set the tristate selection state of the tristate checkbox use {@link #getTristate()}.<br>
 * To set the tristate selection state of the tristate checkbox use.<br>
 * {@link #setTristate(TristateBoolean)}.<br>
 * To set the cycling order use {@link #setCyclingOrder(boolean)}. Default is false after undef.
 * 
 * @author H. Marlovits
 * @since 3.0.0
 */
public class TristateCheckbox extends Button {
	// default tristate at start
	private static final TristateBoolean START_STATE = TristateBoolean.UNDEF;
	private static final boolean START_FALSEFIRST = true;
	
	// cycling order - in which order the states are cycling through
	protected boolean falseFirst = START_FALSEFIRST;
	
	/**
	 * Constructs a new instance of a checkbox-style button with tristate behaviour. False state
	 * follows after undefined.
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of control to construct. You do not need to set SWT.CHECK which is
	 *            automatically set.
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public TristateCheckbox(Composite parent, int style){
		// create a simple button, but add SWT.CHECK style
		super(parent, (style | SWT.CHECK));
		// add the selection listener to implement the tristate behaviour
		addSelectionListener(new TristateSelectionListener(false));
		// set starting state/cycling order
		setTristate(START_STATE);
		setCyclingOrder(true);
	}
	
	/**
	 * Constructs a new instance of a checkbox-style button with tristate behaviour.
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new instance (cannot be null)
	 * @param style
	 *            the style of control to construct. You do not need to set SWT.CHECK which is
	 *            automatically set.
	 * @param falseFirst
	 *            set how the states are cycling through. true if unchecked follows after undefined,
	 *            false if checked follows after undefined.
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public TristateCheckbox(Composite parent, int style, boolean falseFirst){
		// create a simple button, but add SWT.CHECK style
		super(parent, (style | SWT.CHECK));
		// add the selection listener to implement the tristate behaviour
		addSelectionListener(new TristateSelectionListener(false));
		// set starting state/cycling order
		setTristate(START_STATE);
		setCyclingOrder(falseFirst);
	}
	
	/**
	 * get the current tristate selection state
	 * 
	 * @return the current tristate selection state, one of TristateCheckbox.TristateValue
	 *         (TRUE/FALSE/UNDEF)
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public TristateBoolean getTristate(){
		checkWidget();
		// get the selection/grayed states
		boolean selection = getSelection();
		boolean grayed = getGrayed();
		// return the right value
		if (selection) {
			if (grayed) {
				return TristateBoolean.UNDEF;
			} else {
				return TristateBoolean.TRUE;
			}
		} else {
			return TristateBoolean.FALSE;
		}
	}
	
	/**
	 * get the current tristate selection state as String as it will be saved in the db. Usually
	 * you'll use {@link #getTristate()}.
	 * 
	 * @return the current tristate selection state as String, one of
	 *         StringConstants.ONE/StringConstants.ZERO/StringConstants.EMPTY
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public String getTristateDbValue(){
		checkWidget();
		// get the selection/grayed states
		boolean selection = getSelection();
		boolean grayed = getGrayed();
		// return the right value
		if (selection) {
			if (grayed) {
				return StringConstants.EMPTY;
			} else {
				return StringConstants.ONE;
			}
		} else {
			return StringConstants.ZERO;
		}
	}
	
	/**
	 * set the tristate selection state
	 * 
	 * @param newState
	 *            the new tristate selection state, one of TristateCheckbox.TristateValue
	 *            (TRUE/FALSE/UNDEF)
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public void setTristate(TristateBoolean newState){
		checkWidget();
		if (newState == TristateBoolean.TRUE) {
			setSelection(true);
			setGrayed(false);
		} else if (newState == TristateBoolean.FALSE) {
			setSelection(false);
			setGrayed(false);
		} else if (newState == TristateBoolean.UNDEF) {
			setSelection(true);
			setGrayed(true);
		}
	}
	
	/**
	 * set the tristate selection state by using a String as it will be saved in the db. Usually
	 * you'll use {@link #setTristate(TristateBoolean)}.
	 * 
	 * @param newState
	 *            the new tristate selection as a String value as it is saved in the db, one of
	 *            StringConstants.ONE/StringConstants.ZERO/StringConstants.EMPTY
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public void setTristateDbValue(String newState){
		checkWidget();
		if (newState.equalsIgnoreCase(StringConstants.ONE)) {
			setSelection(true);
			setGrayed(false);
		} else if (newState.equalsIgnoreCase(StringConstants.ZERO)) {
			setSelection(false);
			setGrayed(false);
		} else {
			setSelection(true);
			setGrayed(true);
		}
	}
	
	/**
	 * set how the states of the tristate checkbox are cycling through<br>
	 * undef->false->true OR<br>
	 * undef->true->false<br>
	 * default is false first
	 * 
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	public void setCyclingOrder(boolean falseFirst){
		this.falseFirst = falseFirst;
	}
	
	/**
	 * I explicitly want to subclass... Shouldn't cause any problems since I just implement a
	 * constructor calling super and implement some new methods.
	 * 
	 * @since 3.0.0
	 * @author H. Marlovits
	 */
	protected void checkSubclass(){}
	
	/**
	 * a class for implementing a tristate behaviour for checkboxes. Just add this as a selection
	 * listener to the checkbox. The states cycle through undefined -> checked -> unchecked or
	 * undefined -> unchecked -> checked
	 * 
	 * @author H. Marlovits
	 */
	public class TristateSelectionListener implements SelectionListener {
		boolean falseFirst = true;
		
		/**
		 * Create a new TristateSelectionListener.
		 * 
		 * @param falseFirst
		 *            specifies the order of the states. if true then false follows after undef, if
		 *            false then true follows after undef
		 * @since 3.0.0
		 * @author H. Marlovits
		 */
		TristateSelectionListener(boolean falseFirst){
			this.falseFirst = falseFirst;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e){
			Button button = ((Button) e.getSource());
			boolean selection = !button.getSelection();
			boolean grayed = button.getGrayed();
			if (falseFirst) {
				if (selection) {
					if (grayed) {
						button.setSelection(false);
						button.setGrayed(false);
					} else {
						button.setSelection(true);
						button.setGrayed(true);
					}
				} else {
					button.setSelection(true);
					button.setGrayed(false);
				}
			} else {
				if (selection) {
					if (grayed) {
						button.setSelection(true);
						button.setGrayed(false);
					} else {
						button.setSelection(false);
						button.setGrayed(false);
					}
				} else {
					button.setSelection(true);
					button.setGrayed(true);
				}
			}
		}
		
		@Override
		public void widgetDefaultSelected(SelectionEvent e){}
	}
}