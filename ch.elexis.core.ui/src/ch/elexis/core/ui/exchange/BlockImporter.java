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

import java.io.InputStream;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import ch.elexis.core.ui.exchange.elements.ServiceBlockElement;
import ch.elexis.core.ui.exchange.elements.ServiceBlocksElement;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Result;

public class BlockImporter extends XChangeImporter {
	
	public BlockImporter(InputStream is){
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = builder.build(is);
			getContainer().setDocument(doc);
		} catch (Exception e) {
			ExHandler.handle(e);
		}
		
	}
	
	@Override
	public Result<?> finalizeImport(){
		if (getContainer().getRoot() != null) {
			ServiceBlocksElement eBlocks = new ServiceBlocksElement();
			eBlocks.asImporter(
				this,
				getContainer().getRoot().getChild(ServiceBlockElement.ENCLOSING,
					XChangeContainer.ns));
			if (eBlocks != null) {
				List<ServiceBlockElement> lBlocks =
					(List<ServiceBlockElement>) eBlocks.getChildren(ServiceBlockElement.XMLNAME,
						ServiceBlockElement.class);
				for (ServiceBlockElement eBlock : lBlocks) {
					eBlock.doImport();
				}
				
			}
			
		}
		return new Result<String>("OK"); //$NON-NLS-1$
	}
	
	@Override
	public Result<Object> load(Element input, Object context){
		// TODO Auto-generated method stub
		return null;
	}
}
