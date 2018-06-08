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
package ch.elexis.core.ui.contacts.views.provider;

import java.text.SimpleDateFormat;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.data.beans.ContactBean;
import ch.elexis.core.data.interfaces.IContact;
import ch.elexis.core.data.interfaces.IPerson;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.icons.Images;

public class ContactSelectorObservableMapLabelProvider extends ObservableMapLabelProvider
		implements ITableLabelProvider {
		
	public ContactSelectorObservableMapLabelProvider(IObservableMap[] observeMaps){
		super(observeMaps);
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	private StringBuilder sb;
	
	@Override
	public Image getColumnImage(Object element, int columnIndex){
		ContactBean k = (ContactBean) element;
		switch (k.getContactType()) {
		case ORGANIZATION:
			return Images.IMG_ORGANISATION.getImage();
		case PERSON:
			IPerson p = (IPerson) k;
			if (!p.isPatient()) {
				return Images.IMG_PERSON_GREY.getImage();
			}
			if (p.getGender() == null)
				Images.IMG_EMPTY_TRANSPARENT.getImage();
			switch (p.getGender()) {
			case MALE:
				return Images.IMG_MANN.getImage();
			case FEMALE:
				return Images.IMG_FRAU.getImage();
			default:
				return Images.IMG_QUESTION_MARK.getImage();
			}
		default:
			return Images.IMG_EMPTY_TRANSPARENT.getImage();
		}
	}
	
	@Override
	public String getColumnText(Object element, int columnIndex){
		IContact contact = (IContact) element;
		switch (contact.getContactType()) {
		case ORGANIZATION:
			sb = new StringBuilder();
			if (contact.getDescription1() != null) {
				sb.append(contact.getDescription1() + " ");
			}
			if (contact.getDescription2() != null)
				sb.append(contact.getDescription2());
			return sb.toString();
		case PERSON:
			IPerson person = (IPerson) contact;
			sb = new StringBuilder();
			if (person.getTitel() != null)
				sb.append(person.getTitel() + " ");
			sb.append(contact.getDescription1() + ", ");
			sb.append(contact.getDescription2());
			if (person.getTitelSuffix() != null)
				sb.append(", " + person.getTitelSuffix());
			sb.append(" (" + geschlechtToLabel(person.getGender()) + ")");
			if (person.getDateOfBirth() != null)
				sb.append(", " + sdf.format(person.getDateOfBirth().getTime()));
			return sb.toString();
		default:
			return contact.getDescription1();
		}
	}
	
	private String geschlechtToLabel(Gender geschlecht){
		if (geschlecht == null)
			return "?";
		switch (geschlecht) {
		case MALE:
			return "m";
		case FEMALE:
			return "w";
		case UNKNOWN:
			return "?";
		default:
			return "";
		}
	}
}
