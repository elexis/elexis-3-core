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
import ch.elexis.core.data.interfaces.IOptifier;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.ArticleTyp;

public class EigenartikelUtil {
	
	private static IOptifier OPTIFIER;
	
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
	
	

	
//	@Override
//	public IOptifier getOptifier(){
//		if (OPTIFIER == null) {
//			OPTIFIER = new DefaultOptifier() {
//				@Override
//				public Result<IVerrechenbar> add(IVerrechenbar code, Konsultation kons){
//					boolean valid = true;
//					// test VVG if is typ EigenartikelTyp.COMPLEMENTARY
//					if (code instanceof Eigenartikel) {
//						Eigenartikel article = (Eigenartikel) code;
//						if (article.getTyp() == EigenartikelTyp.COMPLEMENTARY) {
//							String gesetz = kons.getFall().getConfiguredBillingSystemLaw().name();
//							String system = kons.getFall().getAbrechnungsSystem();
//							if (gesetz.isEmpty()) {
//								if (!"vvg".equalsIgnoreCase(system)) {
//									valid = false;
//								}
//							} else {
//								if (!"vvg".equalsIgnoreCase(gesetz)) {
//									valid = false;
//								}
//							}
//						}
//					}
//					return valid ? super.add(code, kons)
//							: new Result<IVerrechenbar>(Result.SEVERITY.WARNING, 0,
//								"Komplementärmedizinische Artikel können nur auf eine Fall mit Gesetz oder Name VVG verrechnet werden.",
//								null, false);
//				}
//			};
//		}
//		return OPTIFIER;
//	}
}
