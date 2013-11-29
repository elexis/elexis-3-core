/*******************************************************************************
 * Copyright (c) 2006-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted from elexis main and adapted for usage
 *******************************************************************************/
package ch.elexis.core.ui.eigenartikel;

import org.eclipse.swt.SWT;

import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.eigenartikel.EigenartikelPersistentObjectFactory;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.DefaultButtonProvider;
import ch.elexis.core.ui.views.artikel.ArtikelContextMenu;
import ch.elexis.core.ui.views.artikel.ArtikelLabelProvider;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.PersistentObject;

public class EigenartikelSelector extends CodeSelectorFactory {
	
	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv){
		new ArtikelContextMenu(
			(Eigenartikel) new EigenartikelPersistentObjectFactory()
				.createTemplate(Eigenartikel.class),
			cv, null);
		
		EigenartikelLoader eal = new EigenartikelLoader(cv);
		DefaultControlFieldProvider dcfp = new DefaultControlFieldProvider(cv, new String[] {
			Eigenartikel.FLD_NAME
		});
		DefaultButtonProvider dbp =
			new ViewerConfigurer.DefaultButtonProvider(cv, Eigenartikel.class);
		SimpleWidgetProvider swp =
			new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_LAZYLIST, SWT.NONE, null);
		
		return new ViewerConfigurer(eal, new ArtikelLabelProvider(), dcfp, dbp, swp);
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	@Override
	public void dispose(){
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getCodeSystemName(){
		return Eigenartikel.TYPNAME;
	}
	
}
