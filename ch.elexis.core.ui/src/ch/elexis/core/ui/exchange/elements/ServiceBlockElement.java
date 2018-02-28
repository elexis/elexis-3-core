/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, SGAM.informatics and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.exchange.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;
import ch.elexis.data.dto.CodeElementDTO;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class ServiceBlockElement extends XChangeElement {
	public static final String XMLNAME = "serviceblock";
	public static final String ENCLOSING = "serviceblocks";
	public static final String ATTR_NAME = "name";
	static java.util.List<IConfigurationElement> codesystems;
	
	static List<ICodeElement> codeElements;
	static HashMap<ICodeElement, CodeSelectorFactory> factories;
	
	static {
		codesystems = Extensions.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE);
		codeElements = new ArrayList<ICodeElement>(codesystems.size());
		factories = new HashMap<ICodeElement, CodeSelectorFactory>(codesystems.size());
		for (IConfigurationElement ic : codesystems) {
			try {
				PersistentObjectFactory po = (PersistentObjectFactory) ic
					.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_ELF);
				CodeSelectorFactory cs = (CodeSelectorFactory) ic
					.createExecutableExtension(ExtensionPointConstantsUi.VERRECHNUNGSCODE_CSF);
				if (cs == null) {
					SWTHelper.alert("Fehler", "CodeSelectorFactory is null");
				} else {
					ICodeElement ics = (ICodeElement) po.createTemplate(cs.getElementClass());
					if (ics == null) {
						SWTHelper.alert("Fehler", "CodeElement is null");
					}
					codeElements.add(ics);
					factories.put(ics, cs);
				}
				
			} catch (CoreException ex) {
				ExHandler.handle(ex);
			}
		}
		
	}
	
	public ServiceBlockElement asExporter(XChangeExporter p, Leistungsblock lb){
		asExporter(p);
		setAttribute(ATTR_NAME, lb.getName());
		List<ICodeElement> ics = lb.getElementReferences();
		for (ICodeElement ic : ics) {
			if (ic instanceof IVerrechenbar) {
				IVerrechenbar iv = (IVerrechenbar) ic;
				ServiceElement se = new ServiceElement().asExporter(sender, iv);
				add(se);
			} else if (ic instanceof CodeElementDTO) {
				ServiceElement se = new ServiceElement().asExporter(sender, (CodeElementDTO) ic);
				add(se);
			}
		}
		return this;
	}
	
	public void doImport(){
		String name = getAttr(ATTR_NAME);
		if (!StringTool.isNothing(name)) {
			Leistungsblock block = new Leistungsblock(name, CoreHub.actMandant);
			List<ServiceElement> lService =
				(List<ServiceElement>) getChildren(ServiceElement.XMLNAME, ServiceElement.class);
			for (ServiceElement se : lService) {
				if (!importCodeElement(block, se)) {
					importXidElement(block, se);
				}
			}
		}
	}
	
	/**
	 * Try loading using {@link XidElement}, if the {@link ICodeElement} referenced by the
	 * {@link XidElement} is not present, the CodeSelectorFactory is used as lookup.
	 * 
	 * @param block
	 * @param se
	 * 
	 * @deprecated use {@link ServiceBlockElement#importCodeElement(Leistungsblock, ServiceElement)}
	 */
	private void importXidElement(Leistungsblock block, ServiceElement se){
		XidElement xid = se.getXid();
		List<IPersistentObject> ls = xid.findObject();
		boolean bFound = false;
		for (IPersistentObject po : ls) {
			if (po instanceof IVerrechenbar) {
				block.addElement((IVerrechenbar) po);
				bFound = true;
				break;
			}
		}
		if (!bFound) { // we do not have a object with matching XID
			String contract = se.getAttr("contractName");
			String code = se.getAttr("contractCode");
			String lname = se.getAttr("name");
			boolean bMatched = false;
			for (ICodeElement ice : codeElements) {
				if (ice.getCodeSystemName().equals(contract)) {
					CodeSelectorFactory cof = factories.get(ice);
					if (cof != null) {
						PersistentObject po = cof.findElement(code);
						if (po != null && po.exists()) {
							bMatched = true;
							block.addElement((ICodeElement) po);
							break;
						} else {
							Eigenleistung custom = Eigenleistung.load(code);
							if (custom.exists()) {
								block.addElement(custom);
								bMatched = true;
								break;
							}
						}
					}
				}
			}
			if (!bMatched) {
				Eigenleistung custom =
					new Eigenleistung(code, lname, se.getAttr("cost"), se.getAttr("price"));
				block.addElement(custom);
			}
		}
	}
	
	/**
	 * If information of the {@link ServiceElement} includes contractName and contractCode, a new
	 * {@link CodeElementDTO} is added to the block. This implementation does not care if the actual
	 * {@link ICodeElement} is available.
	 * 
	 * @param block
	 * @param se
	 * @return
	 */
	private boolean importCodeElement(Leistungsblock block, ServiceElement se){
		String codeSystemName = se.getAttr("contractName");
		String code = se.getAttr("contractCode");
		String name = se.getAttr("name");
		if (codeSystemName != null && !codeSystemName.isEmpty() && code != null
			&& !code.isEmpty()) {
			CodeElementDTO codeElement = new CodeElementDTO(codeSystemName, code);
			if (name != null && !name.isEmpty()) {
				codeElement.setText(name);
			}
			block.addElement(codeElement);
			return true;
		}
		return false;
	}
	
	@Override
	public String getXMLName(){
		return XMLNAME;
	}
	
}
