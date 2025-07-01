/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.exchange;

import java.io.FileOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.exchange.elements.ServiceBlockElement;
import ch.elexis.core.ui.exchange.elements.ServiceBlocksElement;
import ch.elexis.core.ui.exchange.elements.XChangeElement;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;

public class BlockExporter extends XChangeExporter {
	ServiceBlocksElement lbs;

	public boolean canHandle(Class<? extends PersistentObject> clazz) {
		if (clazz.equals(Leistungsblock.class)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean canHandle(Identifiable identifiable) {
		return identifiable instanceof ICodeElementBlock;
	}

	public void finalizeExport() throws XChangeException {
		FileDialog fd = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
		fd.setText(Messages.BlockContainer_Blockbeschreibung);
		fd.setFilterExtensions(new String[] { "*.xchange" //$NON-NLS-1$
		});
		fd.setFilterNames(new String[] { Messages.BlockContainer_xchangefiles });
		String filename = fd.open();
		if (filename != null) {
			Format format = Format.getPrettyFormat();
			format.setEncoding("utf-8"); //$NON-NLS-1$
			XMLOutputter xmlo = new XMLOutputter(format);
			String xmlAspect = xmlo.outputString(getDocument());
			try {
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(xmlAspect.getBytes());
				fos.close();
			} catch (Exception ex) {
				ExHandler.handle(ex);
				throw new XChangeException("Output failed " + ex.getMessage()); //$NON-NLS-1$
			}
		}

	}

	public XChangeElement store(Object output) throws XChangeException {
		// create ServiceBlocksElement and attach it to the root of the container
		if (lbs == null) {
			lbs = (ServiceBlocksElement) new ServiceBlocksElement().asExporter(this);
			getContainer().getRoot().addContent(lbs.getElement());
		}

		if (output instanceof Leistungsblock) {
			ServiceBlockElement sbe = new ServiceBlockElement().asExporter(this, (Leistungsblock) output);
			lbs.add(sbe);
			return sbe;
		}
		throw new XChangeException("Can't handle object type " + output.getClass().getName()); //$NON-NLS-1$
	}

}
