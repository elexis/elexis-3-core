package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IVaccination;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;

public class IVaccinationBuilder extends AbstractBuilder<IVaccination> {

	public IVaccinationBuilder(IModelService modelService, IContextService context, IArticle article,
			IPatient patient) {
		super(modelService);

		object = modelService.create(IVaccination.class);
		object.setDateOfAdministration(LocalDate.now());
		object.setPatient(patient);
		object.setArticle(article);
		object.setArticleName(article.getName());
		object.setArticleGtin(article.getGtin());
		object.setArticleAtc(article.getAtcCode());
		if (context != null) {
			object.setPerformer(context.getActiveUserContact().orElse(null));
		}
	}

	public IVaccinationBuilder(IModelService modelService, IContextService context, String articleName,
			String articleGtin, String articleAtc, IPatient patient) {
		super(modelService);

		object = modelService.create(IVaccination.class);
		object.setDateOfAdministration(LocalDate.now());
		object.setPatient(patient);
		object.setArticleName(articleName);
		object.setArticleGtin(articleGtin);
		object.setArticleAtc(articleAtc);
		if (context != null) {
			object.setPerformer(context.getActiveUserContact().orElse(null));
		}
	}

	public IVaccinationBuilder ingredientsAtc(String ingredientsAtc) {
		object.setIngredientsAtc(ingredientsAtc);
		return this;
	}

	public IVaccinationBuilder dateOfAdministration(LocalDate dateOfAdministration) {
		object.setDateOfAdministration(dateOfAdministration);
		return this;
	}
}
