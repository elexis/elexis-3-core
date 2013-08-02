/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.eigenartikel;

import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.ui.actions.FlatDataLoader;
import ch.elexis.core.ui.util.viewers.CommonViewer;

public class EigenartikelLoader extends FlatDataLoader {
	
	public EigenartikelLoader(CommonViewer cv){
		super(cv, new Query<Eigenartikel>(Eigenartikel.class));
		setOrderFields(Eigenartikel.FLD_NAME);
		addQueryFilter(new EigenartikelLabelFilter());
	}
	
	static class EigenartikelLabelFilter implements QueryFilter {
		
		@Override
		public void apply(Query<? extends PersistentObject> qbe){
			qbe.add(Eigenartikel.FLD_TYP, Query.EQUALS, Eigenartikel.TYPNAME);
		}
	}
	
}
