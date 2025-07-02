/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich, SGAM.Informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.exchange;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom2.Document;
import org.jdom2.Element;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.exchange.elements.ContactElement;
import ch.elexis.core.ui.exchange.elements.ContactRefElement;
import ch.elexis.core.ui.exchange.elements.ContactsElement;
import ch.elexis.core.ui.exchange.elements.MedicalElement;
import ch.elexis.core.ui.exchange.elements.XidElement;
import ch.elexis.data.BezugsKontakt;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;

public abstract class XChangeExporter implements IDataSender {
	private final XChangeContainer container = new XChangeContainer();

	@Override
	public boolean canHandle(Class<? extends PersistentObject> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canHandle(Identifiable identifiable) {
		// TODO Auto-generated method stub
		return false;
	}

	public Element getRoot() {
		return container.getRoot();
	}

	public Document getDocument() {
		return container.getDocument();
	}

	public XChangeContainer getContainer() {
		return container;
	}

	/**
	 * Add a new Contact to the file. It will only be added, if it does not yet
	 * exist. Rule for the created ID: If a Contact has a really unique ID (EAN,
	 * Unique Patient Identifier) then this shold be used. Otherwise a unique id
	 * should be generated (here we take the existing id from Elexis which is by
	 * definition already a UUID)
	 *
	 * @param k the contact to insert
	 * @return the Element node of the newly inserted (or earlier inserted) contact
	 */
	@SuppressWarnings("unchecked")
	public ContactElement addContact(Kontakt k) {
		ContactsElement eContacts = container.getContactsElement();
		List<ContactElement> lContacts = (List<ContactElement>) eContacts.getChildren(ContactElement.XMLNAME,
				ContactElement.class);
		for (ContactElement e : lContacts) {
			XidElement xid = e.getXid();
			if ((xid != null) && (xid.match(k) == XidElement.XIDMATCH.SURE)) {
				// TODO
				// e.setContainer(container);
				return e;
			}
		}
		ContactElement contact = new ContactElement().asExporter(this, k);
		eContacts.add(contact);
		container.addChoice(contact.getElement(), k.getLabel(), k);
		return contact;
	}

	/**
	 * Add a Patient to the Container.This will as well export the medical history
	 * of the patient and ist thus the starting point for exporting the EMR.
	 *
	 * @param pat the Patient to export
	 * @return the ContactElement that was created from the Patient (containing the
	 *         Medical-Element)
	 */
	public ContactElement addPatient(Patient pat) {
		ContactElement ret = addContact(pat);
		List<BezugsKontakt> bzl = pat.getBezugsKontakte();

		for (BezugsKontakt bz : bzl) {
			ret.add(new ContactRefElement().asExporter(this, bz));
		}

		MedicalElement eMedical = new MedicalElement().asExporter(this, pat);
		ret.add(eMedical);

		getContainer().addChoice(eMedical.getElement(), Messages.XChangeContainer_kg, eMedical);
		for (IConfigurationElement ice : getContainer().getXChangeContributors()) {

		}
		/*
		 * for (IExchangeContributor iex : getContainer().getXChangeContributors()) {
		 * iex.exportHook(eMedical); }
		 */
		return ret;
	}

	/**
	 * Add a binary content to the Container
	 *
	 * @param id       a unique identifier for the content
	 * @param contents the content
	 */
	public void addBinary(String id, byte[] contents) {
		container.binFiles.put(id, contents);
	}

}
