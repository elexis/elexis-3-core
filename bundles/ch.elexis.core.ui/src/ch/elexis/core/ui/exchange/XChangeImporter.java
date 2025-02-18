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

import java.io.CharArrayReader;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPath;

import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.exchange.elements.XChangeElement;
import ch.elexis.core.ui.util.Log;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class XChangeImporter implements IDataReceiver {
	private final XChangeContainer container = new XChangeContainer();
	private final Log log = Log.get("xChange Importer"); //$NON-NLS-1$

	public Result finalizeImport() {
		// TODO Auto-generated method stub
		return null;
	}

	public Result<Object> load(Element input, Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Find the registered Data handler that matches best the given element
	 *
	 * @param el Element o be imported
	 * @return the best matching handler or null if no handler exists at all for the
	 *         given data type
	 */
	public IExchangeContributor findImportHandler(XChangeElement el) {
		int matchedRestrictions = 0;
		IConfigurationElement cand = null;
		for (IConfigurationElement ice : container.getXChangeContributors()) {
			String datatype = ice.getAttribute("ElementType"); //$NON-NLS-1$
			if (datatype.equalsIgnoreCase(el.getXMLName())) {
				if (cand == null) {
					cand = ice;
				}
				String restriction = ice.getAttribute("restrictions"); //$NON-NLS-1$
				if (restriction != null) {
					String[] restrictions = restriction.split(","); //$NON-NLS-1$
					int matches = 0;
					for (String r : restrictions) {
						try {
							XPath xpath = XPath.newInstance(r);
							List<?> nodes = xpath.selectNodes(el);
							if (!nodes.isEmpty()) {
								if (++matches > matchedRestrictions) {
									cand = ice;
									matchedRestrictions = matches;
								} else if (matches == matchedRestrictions) {
									if (compareValues(ice, cand) == -1) {
										cand = ice;
									}
								}
							}
						} catch (JDOMException e) {
							ElexisStatus status = new ElexisStatus(ElexisStatus.WARNING, Hub.PLUGIN_ID,
									ElexisStatus.CODE_NONE, "Parse error JDOM: " + e.getMessage(), e, //$NON-NLS-1$
									ElexisStatus.LOG_WARNINGS);
							throw new ExchangeException(status);
						}
					}

				} else {
					if (compareValues(ice, cand) == -1)
						cand = ice;
				}
			}

		}
		if (cand != null) {
			try {
				return (IExchangeContributor) cand.createExecutableExtension("Actor"); //$NON-NLS-1$
			} catch (CoreException ce) {
				ExHandler.handle(ce);
			}
		}
		return null;
	}

	int compareValues(IConfigurationElement ic1, IConfigurationElement ic2) {
		int r1 = 0;
		int r2 = 0;
		String v1 = ic1.getAttribute("value"); //$NON-NLS-1$
		String v2 = ic2.getAttribute("value"); //$NON-NLS-1$
		if (v1 != null && v1.matches("[0-9]+")) { //$NON-NLS-1$
			r1 = Integer.parseInt(v1);
		}
		if (v2 != null && v2.matches("[0-9]+")) { //$NON-NLS-1$
			r2 = Integer.parseInt(v2);
		}
		if (r1 == r2) {
			return 0;
		}
		return r1 > r2 ? -1 : 1;
	}

	public void addBinary(String id, byte[] cnt) {
		container.binFiles.put(id, cnt);
	}

	public XChangeContainer getContainer() {
		return container;
	}

	public boolean load(String input) {
		SAXBuilder builder = new SAXBuilder();
		try {
			CharArrayReader car = new CharArrayReader(input.toCharArray());
			container.setDocument(builder.build(car));
			container.setValid(true);
		} catch (Exception e) {
			ExHandler.handle(e);
			container.setValid(false);
		}
		return container.isValid();

	}
}
