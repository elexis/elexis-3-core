/*******************************************************************************
 * Copyright (c) 2006-2018, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted from elexis main and adapted for usage
 *    <office@medevit.at> - 3.2 format introduction (products and items)
 *    <office@medevit.at> - 3.6 removal of 3.2 format conversion
 *******************************************************************************/

package ch.elexis.core.eigenartikel;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.ArticleTyp;

public class EigenartikelUtil {
	
	/**
	 * Synchronizes a products description with is "child" packages and adds as package to product
	 * 
	 * @param product
	 * @param eaPackage
	 *            if <code>null</code> all current children are fetched and updated, if an
	 *            Eigenartikel is provided it is added as a package
	 */
	@SuppressWarnings("unchecked")
	public static void copyProductAttributesToArticleSetAsChild(IArticle product,
		IArticle eaPackage){
		
		List<IArticle> eaPackages = new ArrayList<IArticle>();
		if (eaPackage != null) {
			eaPackages.add(eaPackage);
		} else {
			IQuery<IArticle> query = CoreModelServiceHolder.get().getQuery(IArticle.class);
			query.and(ModelPackage.Literals.IARTICLE__TYP, COMPARATOR.EQUALS,
				ArticleTyp.EIGENARTIKEL);
			query.and(ModelPackage.Literals.IARTICLE__PRODUCT, COMPARATOR.EQUALS, product);
			eaPackages.addAll(query.execute());
		}
		
		for (IArticle ea : eaPackages) {
			ea.setProduct(product);
			ea.setSubTyp(product.getSubTyp());
			ea.setAtcCode(product.getAtcCode());
			ea.setTyp(product.getTyp());
			ea.setName(product.getName());
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, ea);
		}
		CoreModelServiceHolder.get().save((List<Identifiable>) (List<?>) eaPackages);
	}
}
