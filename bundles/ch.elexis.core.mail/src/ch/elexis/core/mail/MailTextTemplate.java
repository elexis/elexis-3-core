package ch.elexis.core.mail;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.ITextTemplate;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.AbstractBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.TextTemplateCategory;

public class MailTextTemplate {

	public static List<ITextTemplate> load() {
		IQuery<ITextTemplate> query = CoreModelServiceHolder.get().getQuery(ITextTemplate.class);
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__CATEGORY, COMPARATOR.EQUALS, TextTemplateCategory.MAIL);
		query.orderBy(ModelPackage.Literals.ITEXT_TEMPLATE__NAME, ORDER.ASC);
		List<ITextTemplate> allTemplates = query.execute();
		if (ContextServiceHolder.get().getActiveUser().isPresent()) {
			if (!ContextServiceHolder.get().getActiveUser().get().isAdministrator()) {
				allTemplates = allTemplates.stream().filter(b -> isAllOrCurrentMandator(b))
						.collect(Collectors.toList());
			}
		}
		return allTemplates;
	}

	private static boolean isAllOrCurrentMandator(ITextTemplate template) {
		if (template.getMandator() == null) {
			return true;
		}
		if (ContextServiceHolder.get().getActiveMandator().isPresent()) {
			return template.getMandator().equals(ContextServiceHolder.get().getActiveMandator().get());
		}
		return false;
	}

	public static Optional<ITextTemplate> load(String templateName) {
		if (ContextServiceHolder.get().getActiveMandator().isPresent()) {
			Optional<ITextTemplate> mandatorTemplate = getTemplate(ContextServiceHolder.get().getActiveMandator().get(),
					templateName);
			if (mandatorTemplate.isPresent()) {
				return mandatorTemplate;
			}
		}
		Optional<ITextTemplate> allTemplate = getTemplate(null, templateName);
		return allTemplate;
	}

	private static Optional<ITextTemplate> getTemplate(IMandator mandator, String name) {
		IQuery<ITextTemplate> query = CoreModelServiceHolder.get().getQuery(ITextTemplate.class);
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__CATEGORY, COMPARATOR.EQUALS, TextTemplateCategory.MAIL);
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__MANDATOR, COMPARATOR.EQUALS, mandator);
		query.and(ModelPackage.Literals.ITEXT_TEMPLATE__NAME, COMPARATOR.EQUALS, name);
		return query.executeSingleResult();
	}

	public static class Builder extends AbstractBuilder<ITextTemplate> {

		public Builder() {
			super(CoreModelServiceHolder.get());
			object = modelService.create(ITextTemplate.class);
			object.setCategory(TextTemplateCategory.MAIL);
		}

		public Builder name(String string) {
			object.setName(string);
			return this;
		}

		public Builder text(String string) {
			object.setTemplate(string);
			return this;
		}

		public Builder mandator(IMandator mandator) {
			object.setMandator(mandator);
			return this;
		}
	}
}
