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
package ch.elexis.core.ui.eigenleistung;

import ch.elexis.core.ui.actions.FlatDataLoader;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Query;

public class EigenleistungLoader extends FlatDataLoader {
	
	public EigenleistungLoader(CommonViewer cv){
		super(cv, new Query<Eigenleistung>(Eigenleistung.class));
		setOrderFields(Eigenleistung.BEZEICHNUNG, Eigenleistung.CODE);
	}
	
}
