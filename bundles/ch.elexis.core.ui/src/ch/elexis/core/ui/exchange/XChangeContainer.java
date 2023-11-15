/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich, SGAM.Informatics and Elexis
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.util.ElexisIdGenerator;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.exchange.elements.ContactElement;
import ch.elexis.core.ui.exchange.elements.ContactsElement;
import ch.elexis.core.ui.exchange.elements.DocumentElement;
import ch.elexis.core.ui.exchange.elements.EpisodeElement;
import ch.elexis.core.ui.exchange.elements.FindingElement;
import ch.elexis.core.ui.exchange.elements.MedicationElement;
import ch.elexis.core.ui.exchange.elements.RecordElement;
import ch.elexis.core.ui.exchange.elements.RiskElement;
import ch.elexis.core.ui.exchange.elements.XChangeElement;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.XMLTool;

public class XChangeContainer {
	public static final String Version = "2.0.0"; //$NON-NLS-1$
	public static final String ATTR_LANGUAGE = "language"; //$NON-NLS-1$
	private static final String ATTR_PROTOCOL_VERSION = "protocolVersion"; //$NON-NLS-1$
	private static final String ATTR_CREATOR_VERSION = "creatorVersion"; //$NON-NLS-1$
	private static final String ATTR_CREATOR_ID = "creatorID"; //$NON-NLS-1$
	public static final String ATTR_CREATOR_NAME = "creatorName"; //$NON-NLS-1$
	public static final String ATTR_RESPONSIBLE = "responsible"; //$NON-NLS-1$
	public static final String ATTR_DESTINATION = "destination"; //$NON-NLS-1$
	public static final String ATTR_ORIGIN = "origin"; //$NON-NLS-1$
	private static final String XCHANGE_MAGIC = "xChange"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	public static final String ATTR_TIMESTAMP = "timestamp"; //$NON-NLS-1$
	private static final String PLURAL = "s"; //$NON-NLS-1$
	public static final Namespace ns = Namespace.getNamespace(XCHANGE_MAGIC, "http://informatics.sgam.ch/xChange"); //$NON-NLS-1$
	public static final Namespace nsxsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XML Schema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final Namespace nsschema = Namespace.getNamespace("schemaLocation", //$NON-NLS-1$
			"http://informatics.sgam.ch/xChange xchange.xsd"); //$NON-NLS-1$

	public static final String ROOT_ELEMENT = XCHANGE_MAGIC;
	public static final String ROOTPATH = StringTool.slash + ROOT_ELEMENT + StringTool.slash;

	public static final String ENCLOSE_CONTACTS = ContactElement.XMLNAME + PLURAL;
	// public static final String PATIENT_ELEMENT = "patient"; //$NON-NLS-1$
	public static final String ENCLOSE_DOCUMENTS = DocumentElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_RECORDS = RecordElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_FINDINGS = FindingElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_MEDICATIONS = MedicationElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_RISKS = RiskElement.XMLNAME + PLURAL;
	public static final String ENCLOSE_EPISODES = EpisodeElement.XMLNAME + PLURAL;

	private Document doc;
	private final Element eHeader = new Element("header", ns); //$NON-NLS-1$
	private Element eRoot;
	private boolean bValid = false;

	protected static Log log = Log.get("XChange"); //$NON-NLS-1$

	protected HashMap<String, byte[]> binFiles = new HashMap<String, byte[]>();
	/**
	 * Collection of all UserChoices to display to tzhe user for selection
	 */
	protected HashMap<Element, UserChoice> choices = new HashMap<Element, UserChoice>();

	/**
	 * Mapping between element in the xChange Container to the corresponding
	 * internal data object
	 */
	private final HashMap<XChangeElement, PersistentObject> mapElementToObject = new HashMap<XChangeElement, PersistentObject>();

	/**
	 * Mapping from an internal data object to an element in the xChange Container
	 */
	private final HashMap<PersistentObject, XChangeElement> mapObjectToElement = new HashMap<PersistentObject, XChangeElement>();

	private final List<IConfigurationElement> lex = Extensions
			.getExtensions(ExtensionPointConstantsUi.XCHANGE_CONTRIBUTION);

	// public abstract Kontakt findContact(String id);

	public XChangeContainer() {
		doc = new Document();
		eRoot = new Element(XChangeContainer.ROOT_ELEMENT, XChangeContainer.ns);
		eRoot.addNamespaceDeclaration(XChangeContainer.nsxsi);
		eRoot.addNamespaceDeclaration(XChangeContainer.nsschema);
		eRoot.setAttribute(ATTR_TIMESTAMP, new TimeTool().toString(TimeTool.DATETIME_XML));
		eRoot.setAttribute(ATTR_ID, XMLTool.idToXMLID(ElexisIdGenerator.generateId()));
		eRoot.setAttribute(ATTR_ORIGIN, XMLTool.idToXMLID(ContextServiceHolder.getActiveMandatorOrNull().getId()));
		eRoot.setAttribute(ATTR_DESTINATION, "undefined"); //$NON-NLS-1$
		eRoot.setAttribute(ATTR_RESPONSIBLE, XMLTool.idToXMLID(ContextServiceHolder.getActiveMandatorOrNull().getId()));
		doc.setRootElement(eRoot);

		eHeader.setAttribute(ATTR_CREATOR_NAME, Hub.APPLICATION_NAME);
		eHeader.setAttribute(ATTR_CREATOR_ID, "ch.elexis"); //$NON-NLS-1$
		eHeader.setAttribute(ATTR_CREATOR_VERSION, CoreHub.Version);
		eHeader.setAttribute(ATTR_PROTOCOL_VERSION, XChangeContainer.Version);
		eHeader.setAttribute(ATTR_LANGUAGE, Locale.getDefault().toString());
		eRoot.addContent(eHeader);

	}

	public void setDocument(Document doc) {
		this.doc = doc;
		eRoot = doc.getRootElement();
	}

	@Override
	public String toString() {
		Format format = Format.getPrettyFormat();
		format.setEncoding("utf-8"); //$NON-NLS-1$
		XMLOutputter xmlo = new XMLOutputter(format);
		String xmlAspect = xmlo.outputString(doc);
		return xmlAspect;
	}

	public Document getDocument() {
		return doc;
	}

	public boolean isValid() {
		return bValid;
	}

	public void setValid(boolean bValid) {
		this.bValid = bValid;
	}

	public List<IConfigurationElement> getXChangeContributors() {
		return lex;
	}

	/**
	 * Map a database object to an xChange container element and vice versa
	 *
	 * @param element the Element
	 * @param obj     the Object
	 */
	public void addMapping(XChangeElement element, PersistentObject obj) {
		mapElementToObject.put(element, obj);
		mapObjectToElement.put(obj, element);
	}

	/**
	 * Return the database Object that maps to a specified Element
	 *
	 * @param element the Element
	 * @return the object or null if no such mapping exists
	 */
	public PersistentObject getMapping(XChangeElement element) {
		return mapElementToObject.get(element);
	}

	/**
	 * return the Container Element that is mapped to a specified database object
	 *
	 * @param obj the object
	 * @return the element or null if no such mapping exists
	 */
	public XChangeElement getMapping(PersistentObject obj) {
		return mapObjectToElement.get(obj);
	}

	/**
	 * Retrieve the UserChoice attributed to a given Element
	 *
	 * @param key teh element
	 * @return the UserChoice or null if no such UserChoice exists
	 */
	public UserChoice getChoice(XChangeElement key) {
		return choices.get(key.getElement());
	}

	public UserChoice getChoice(Element key) {
		return choices.get(key);
	}

	public void addChoice(Element key, String name) {
		choices.put(key, new UserChoice(true, name, key));
	}

	public void addChoice(XChangeElement key, String name) {
		choices.put(key.getElement(), new UserChoice(true, name, key));
	}

	public ContactsElement getContactsElement() {
		Element ec = eRoot.getChild(ENCLOSE_CONTACTS, ns);
		ContactsElement eContacts = new ContactsElement();
		if (ec == null) {
			eRoot.addContent(eContacts.getElement());
			choices.put(eContacts.getElement(), new UserChoice(true, Messages.Core_Contacts, eContacts));
		} else {
			eContacts.setElement(ec);
		}
		return eContacts;
	}

	public List<Element> getContactElements() {
		return getElements(ROOTPATH + ENCLOSE_CONTACTS + StringTool.slash + ContactElement.XMLNAME);
	}

	/**
	 * get a binary content from the Container
	 *
	 * @param id id of the content
	 * @return the content or null if no such content exists
	 */
	public byte[] getBinary(String id) {
		return binFiles.get(id);
	}

	public void addChoice(XChangeElement e, String name, Object o) {
		choices.put(e.getElement(), new UserChoice(true, name, o));
	}

	public void addChoice(Element e, String name, Object o) {
		choices.put(e, new UserChoice(true, name, o));
	}

	/**
	 * Get the root element.
	 *
	 * @return the root element
	 */
	public Element getRoot() {
		return eRoot;
	}

	/**
	 * Retrieve a List of all Elements with a given Name at a given path
	 *
	 * @param path a string of the form /element1/element2/name will get all
	 *             Elements with "name" in the body of element2. If name is *, will
	 *             retrieve all Children of element2. Path must begin at root level.
	 * @return a possibly empty list af all matching elements at the given position
	 */
	@SuppressWarnings("unchecked")
	public List<Element> getElements(String path) {
		LinkedList<Element> ret = new LinkedList<Element>();
		String[] trace = path.split(StringTool.slash);
		Element runner = eRoot;
		for (int i = 2; i < trace.length - 1; i++) {
			runner = runner.getChild(trace[i], ns);
			if (runner == null) {
				return ret;
			}
		}
		String name = trace[trace.length - 1];
		if (Objects.equals(name, "*")) { //$NON-NLS-1$
			return runner.getChildren();
		}
		return runner.getChildren(name, ns);
	}

	public Namespace getNamespace() {
		return ns;
	}

	/**
	 * get an Iterator over all binary contents of this Container
	 */
	public Iterator<Entry<String, byte[]>> getBinaries() {
		return binFiles.entrySet().iterator();
	}

	/*
	 * public List<Object> getSelectedChildren(Tree<UserChoice> tSelection){
	 * List<Object> ret=new LinkedList<Object>(); for(Tree<UserChoice>
	 * runner:tSelection.getChildren()){ UserChoice choice=runner.contents;
	 * if(choice.isSelected()){ ret.add(choice.object); } } return ret; }
	 */

	/**
	 * Set any implementation-spezific configuration
	 *
	 * @param props
	 */
	public void setConfiguration(Properties props) {
		this.props = props;
	}

	/**
	 * Set a named property for this container
	 *
	 * @param name  the name of the property
	 * @param value the value for the property
	 */
	public void setProperty(String name, String value) {
		if (props == null) {
			props = new Properties();
		}
		props.setProperty(name, value);
	}

	public String getProperty(String name) {
		if (props == null) {
			props = new Properties();
		}
		return props.getProperty(name);
	}

	protected Properties getProperties() {
		return props;
	}

	protected Properties props;

	/**
	 * A UserChoice contains the information, whether the user selected the
	 * associated object for transfer
	 *
	 * @author gerry
	 *
	 */
	public static class UserChoice {
		boolean bSelected;
		String title;
		Object object;

		/**
		 * Include the object in the transfer
		 *
		 * @param bSelection
		 */
		public void select(boolean bSelection) {
			bSelected = bSelection;
		}

		/**
		 * tell wether the object is selected for transfer
		 *
		 * @return
		 */
		public boolean isSelected() {
			return bSelected;
		}

		/**
		 * get the Title to display to the user when asked for selection
		 *
		 * @return
		 */
		public String getTitle() {
			return title;
		}

		/**
		 * Get the associated object
		 *
		 * @return
		 */
		public Object getObject() {
			return object;
		}

		/**
		 * Create a new UserChoice
		 *
		 * @param bSelected true if initially selected
		 * @param title     title to display to the user in selection form
		 * @param object    the object to select for transfer
		 */
		public UserChoice(boolean bSelected, String title, Object object) {
			this.bSelected = bSelected;
			this.title = title;
			this.object = object;
		}
	}

}
