package ch.elexis.core.eigenartikel.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.localarticle.Constants;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.types.ArticleSubTyp;
import ch.elexis.core.types.ArticleTyp;
import ch.rgw.tools.Money;

@Component
public class EigenartikelCodeElementService implements ICodeElementServiceContribution {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Activate
	public void activate(){
		ExecutorService initExec = Executors.newSingleThreadExecutor();
		initExec.execute(() -> {
			// wait for login
			while (ElexisEventDispatcher.getSelectedMandator() == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			// init covid article
			INamedQuery<IArticle> query =
				coreModelService.getNamedQuery(IArticle.class, "typ", "code");
			
			List<IArticle> found = query.executeWithParameters(
				query.getParameterMap("typ", ArticleTyp.EIGENARTIKEL, "code", "3028"));
			if (found.isEmpty()) {
				IArticle product = new IArticleBuilder(CoreModelServiceHolder.get(),
					"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG",
					"3028",
					ArticleTyp.EIGENARTIKEL).build();
				product.setSubTyp(ArticleSubTyp.COVID);
				CoreModelServiceHolder.get().save(product);
				
				IArticle article = new IArticleBuilder(CoreModelServiceHolder.get(),
					"Ärztliche Pauschale SARS-CoV-2-Test nach Teststrategie BAG – Pauschale für Ärzte",
					"3028", ArticleTyp.EIGENARTIKEL).build();
				article.setSubTyp(ArticleSubTyp.COVID);
				article.setSellingPrice(new Money(5000));
				article.setProduct(product);
				CoreModelServiceHolder.get().save(article);
			}
			initExec.shutdown();
		});
	}
	
	@Override
	public String getSystem(){
		return Constants.TYPE_NAME;
	}
	
	@Override
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context){
		INamedQuery<IArticle> query = coreModelService.getNamedQuery(IArticle.class, "typ", "code");
		
		List<IArticle> found = query.executeWithParameters(
			query.getParameterMap("typ", ArticleTyp.EIGENARTIKEL, "code", code));
		if (!found.isEmpty()) {
			if (found.size() > 1) {
				LoggerFactory.getLogger(getClass()).warn("Found more than one "
					+ ArticleTyp.EIGENARTIKEL.getCodeSystemName() + " with code [" + code
					+ "] using first");
			}
			return Optional.of(found.get(0));
		} else {
			query = coreModelService.getNamedQuery(IArticle.class, "typ", "id");
			found = query.executeWithParameters(
				query.getParameterMap("typ", ArticleTyp.EIGENARTIKEL, "id", code));
			if (!found.isEmpty()) {
				if (found.size() > 1) {
					LoggerFactory.getLogger(getClass())
						.warn("Found more than one " + ArticleTyp.EIGENARTIKEL.getCodeSystemName()
							+ " with id [" + code + "] using first");
				}
				return Optional.of(found.get(0));
			}
		}
		return Optional.empty();
	}
	
	@Override
	public CodeElementTyp getTyp(){
		return CodeElementTyp.ARTICLE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ICodeElement> getElements(Map<Object, Object> context){
		INamedQuery<IArticle> query = coreModelService.getNamedQuery(IArticle.class, "typ");
		return (List<ICodeElement>) (List<?>) query.executeWithParameters(
			query.getParameterMap("typ", ArticleTyp.EIGENARTIKEL));
	}
}
