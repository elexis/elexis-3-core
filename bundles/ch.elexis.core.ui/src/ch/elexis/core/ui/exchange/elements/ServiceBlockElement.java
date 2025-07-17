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

import java.util.List;

import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.exchange.XChangeExporter;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.dto.CodeElementDTO;
import ch.rgw.tools.StringTool;

public class ServiceBlockElement extends XChangeElement {
	public static final String XMLNAME = "serviceblock"; //$NON-NLS-1$
	public static final String ENCLOSING = "serviceblocks"; //$NON-NLS-1$
	public static final String ATTR_NAME = "name"; //$NON-NLS-1$

	public ServiceBlockElement asExporter(XChangeExporter p, Leistungsblock lb) {
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

	public void doImport() {
		String name = getAttr(ATTR_NAME);
		if (!StringTool.isNothing(name)) {
			Leistungsblock block = new Leistungsblock(name, ContextServiceHolder.getActiveMandatorOrNull().getId());
			List<ServiceElement> lService = (List<ServiceElement>) getChildren(ServiceElement.XMLNAME,
					ServiceElement.class);
			for (ServiceElement se : lService) {
				if (!importCodeElement(block, se)) {
					LoggerFactory.getLogger(getClass()).warn("Could not import code element ["
							+ se.getAttr("contractName") + " / " + se.getAttr("contractCode") + "]");
				}
			}
		}
	}

	/**
	 * If information of the {@link ServiceElement} includes contractName and
	 * contractCode, a new {@link CodeElementDTO} is added to the block. This
	 * implementation does not care if the actual {@link ICodeElement} is available.
	 *
	 * @param block
	 * @param se
	 * @return
	 */
	private boolean importCodeElement(Leistungsblock block, ServiceElement se) {
		String codeSystemName = se.getAttr("contractName"); //$NON-NLS-1$
		String code = se.getAttr("contractCode"); //$NON-NLS-1$
		String name = se.getAttr("name"); //$NON-NLS-1$
		if (codeSystemName != null && !codeSystemName.isEmpty() && code != null && !code.isEmpty()) {
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
	public String getXMLName() {
		return XMLNAME;
	}

}
