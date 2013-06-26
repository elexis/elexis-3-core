/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.StringTool;

/**
 * A class to display and let the user enter or change currency strings
 * 
 * @author gerry
 * 
 */
public class MoneyInput extends Composite {
	Text text;
	List<SelectionListener> listeners = new LinkedList<SelectionListener>();
	
	public MoneyInput(final Composite parent){
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		text = new Text(this, SWT.BORDER);
		prepare();
	}
	
	public MoneyInput(final Composite parent, final String label){
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		new Label(this, SWT.NONE).setText(label);
		text = new Text(this, SWT.BORDER);
		prepare();
		text.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
	}
	
	public MoneyInput(final Composite parent, final String label, final ch.rgw.tools.Money money){
		this(parent, label);
		text.setText(money.getAmountAsString());
	}
	
	private void prepare(){
		text.addFocusListener(new FocusAdapter() {
			
			@Override
			public void focusLost(final FocusEvent e){
				try {
					String t = text.getText();
					if (t.length() == 0) {
						text.setText(new ch.rgw.tools.Money().getAmountAsString());
					} else {
						ch.rgw.tools.Money.checkInput(t);
					}
					for (SelectionListener lis : listeners) {
						Event ev = new Event();
						ev.widget = e.widget;
						ev.display = e.display;
						lis.widgetSelected(new SelectionEvent(ev));
					}
				} catch (ParseException px) {
					SWTHelper.alert(Messages.getString("MoneyInput.InvalidAmountCaption"), //$NON-NLS-1$
						Messages.getString("MoneyInput.InvalidAmountContents")); //$NON-NLS-1$
				}
			}
		});
		/*
		 * text.addVerifyListener(new VerifyListener(){ public void verifyText(VerifyEvent e) {
		 * if(e.character==SWT.DEL || e.character==SWT.BS){ e.doit=true; }else{ String
		 * t=text.getText()+e.character; if(t.length()<2 || t.matches("[0-9]+[\\.,]?[0-9]{0,2}")){
		 * e.doit=true; }else{ e.doit=false; } } }});
		 */
		
	}
	
	/**
	 * Return the entered value as Money.
	 * 
	 * @param bNullIfEmpty
	 *            if nothing was entered return null (Otherwise: return 0.00)
	 */
	public ch.rgw.tools.Money getMoney(final boolean bNullIfEmpty){
		String t = text.getText();
		if (StringTool.isNothing(t)) {
			if (bNullIfEmpty) {
				return null;
			} else {
				return new ch.rgw.tools.Money();
			}
		}
		try {
			return new Money(t);
		} catch (ParseException px) {
			ExHandler.handle(px);
			return null; // sollte nicht passieren
		}
	}
	
	public void setMoney(final String m){
		text.setText(m);
	}
	
	public Text getControl(){
		return text;
	}
	
	public void addSelectionListener(final SelectionListener lis){
		listeners.add(lis);
	}
	
	public void removeSelectionListener(final SelectionListener lis){
		listeners.remove(lis);
	}
	
	/**
	 * get the money out of a SWT Text-field This might fail if the text field's contents doesn't
	 * conform to the current locale's standard currency format
	 * 
	 * @return the Money or null
	 */
	public static ch.rgw.tools.Money getFromTextField(Text textField){
		try {
			return new ch.rgw.tools.Money(textField.getText());
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
}
