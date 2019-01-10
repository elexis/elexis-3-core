package ch.elexis.core.model.builder;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.services.IModelService;

public class IArticleDefaultSignatureBuilder extends AbstractBuilder<IArticleDefaultSignature> {
	
	public IArticleDefaultSignatureBuilder(IModelService modelService, IArticle article){
		super(modelService);
		
		object = modelService.create(IArticleDefaultSignature.class);
		object.setArticle(article);
	}
}
