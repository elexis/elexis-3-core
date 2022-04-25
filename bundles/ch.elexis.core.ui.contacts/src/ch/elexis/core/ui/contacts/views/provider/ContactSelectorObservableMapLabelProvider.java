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
import java.time.format.DateTimeFormatter;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.ui.icons.Images;

public class ContactSelectorObservableMapLabelProvider extends ObservableMapLabelProvider
		implements ITableLabelProvider {

	public ContactSelectorObservableMapLabelProvider(IObservableMap[] observeMaps) {
		super(observeMaps);
	}

	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	private StringBuilder sb;

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		IContact k = (IContact) element;

		if (k.isOrganization()) {
			return Images.IMG_ORGANISATION.getImage();
		}

		if (k.isPerson()) {
			if (!k.isPatient()) {
				return Images.IMG_PERSON_GREY.getImage();
			}

			IPerson p = CoreModelServiceHolder.get().load(k.getId(), IPerson.class).get();
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
		}

		return Images.IMG_EMPTY_TRANSPARENT.getImage();
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		IContact contact = (IContact) element;

		if (contact.isOrganization()) {
			sb = new StringBuilder();
			if (contact.getDescription1() != null) {
				sb.append(contact.getDescription1() + " ");
			}
			if (contact.getDescription2() != null)
				sb.append(contact.getDescription2());
			return sb.toString();
		}

		if (contact.isPerson()) {
			IPerson person = CoreModelServiceHolder.get().load(contact.getId(), IPerson.class).get();
			sb = new StringBuilder();
			if (person.getTitel() != null)
				sb.append(person.getTitel() + " ");
			sb.append(contact.getDescription1() + ", ");
			sb.append(contact.getDescription2());
			if (person.getTitelSuffix() != null)
				sb.append(", " + person.getTitelSuffix());
			sb.append(" (" + geschlechtToLabel(person.getGender()) + ")");
			if (person.getDateOfBirth() != null)
				sb.append(", " + person.getDateOfBirth().format(DateTimeFormatter.BASIC_ISO_DATE));
			return sb.toString();
		}

		return contact.getDescription1();
	}

	private String geschlechtToLabel(Gender geschlecht) {
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
