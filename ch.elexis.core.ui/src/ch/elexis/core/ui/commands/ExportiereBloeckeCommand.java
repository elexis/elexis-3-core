/*******************************************************************************
 * Copyright (c) 2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.core.ui.exchange.BlockExporter;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;

public class ExportiereBloeckeCommand extends AbstractHandler {
	public static final String ID = "serviceblocks.export";
	
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		List<Leistungsblock> bloecke = qbe.execute();
		BlockExporter bc = new BlockExporter();
		for (Leistungsblock block : bloecke) {
			try {
				bc.store(block);
			} catch (XChangeException xx) {
				ExHandler.handle(xx);
			}
		}
		
		try {
			bc.finalizeExport();
		} catch (XChangeException e) {
			ExHandler.handle(e);
			throw new ExecutionException(e.getMessage());
		}
		return null;
	}
	
}
