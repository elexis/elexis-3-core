/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.controls;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.beans.ContactBean;

public abstract class AbstractComposite extends Composite {
	
	IObservableValue contactObservable = new WritableValue(null, ContactBean.class);
	
	public AbstractComposite(Composite parent, int style){
		super(parent, style);
	}
	
	void bindValue(Text text, String property, DataBindingContext bindingContext){
		IObservableValue textObserveWidget =
			SWTObservables.observeDelayedValue(5, SWTObservables.observeText(text, SWT.Modify));
		IObservableValue observeValue =
			BeansObservables.observeDetailValue(contactObservable, property, String.class);
		bindingContext.bindValue(textObserveWidget, observeValue, null, null);
	}
	
	public abstract void setContact(ContactBean k);
}
