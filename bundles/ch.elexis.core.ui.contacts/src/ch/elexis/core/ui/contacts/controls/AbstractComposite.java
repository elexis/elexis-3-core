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
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.IContact;

public abstract class AbstractComposite extends Composite {

	IObservableValue<IContact> contactObservable = new WritableValue<IContact>(null, IContact.class);

	public AbstractComposite(Composite parent, int style) {
		super(parent, style);
	}

	void bindValue(Text text, String property, DataBindingContext bindingContext) {
		ISWTObservableValue<String> textObserveWidget = WidgetProperties.text(SWT.Modify).observeDelayed(50, text);
		IObservableValue<Object> observeValue = PojoProperties.value(IContact.class, property)
				.observeDetail(contactObservable);
		bindingContext.bindValue(textObserveWidget, observeValue, null, null);
	}

	public abstract void setContact(IContact k);
}
