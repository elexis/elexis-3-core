package ch.elexis.core.model.builder;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.ArticleTyp;
import ch.rgw.tools.Money;

public class IArticleBuilder extends AbstractBuilder<IArticle> {
	
	public IArticleBuilder(IModelService modelService, String name, String code, ArticleTyp typ){
		super(modelService);
		
		object = modelService.create(IArticle.class);
		object.setName(name);
		object.setCode(code);
		object.setTyp(typ);
	}
	
	public IArticleBuilder purchasePrice(Money value){
		object.setPurchasePrice(value);
		return this;
	}
	
	public IArticleBuilder sellingPrice(Money value){
		object.setSellingPrice(value);
		return this;
	}
}
