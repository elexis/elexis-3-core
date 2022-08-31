package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.types.TextTemplateCategory;

public class TextTemplate extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.TextTemplate>
		implements IdentifiableWithXid, Deleteable, ITextTemplate {

	public TextTemplate(ch.elexis.core.jpa.entities.TextTemplate entity) {
		super(entity);
	}

	@Override
	public TextTemplateCategory getCategory() {
		return getEntity().getCategory();
	}

	@Override
	public void setCategory(TextTemplateCategory value) {
		getEntityMarkDirty().setCategory(value);

	}

	@Override
	public IMandator getMandator() {
		return ModelUtil.getAdapter(getEntity().getMandator(), IMandator.class, true);
	}

	@Override
	public void setMandator(IMandator value) {
		if (value instanceof AbstractIdModelAdapter<?>) {
			getEntityMarkDirty().setMandator((Kontakt) ((AbstractIdModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setMandator(null);
		}
	}

	@Override
	public String getName() {
		return getEntity().getName();
	}

	@Override
	public void setName(String value) {
		getEntityMarkDirty().setName(value);
	}

	@Override
	public String getTemplate() {
		return getEntity().getTemplate();
	}

	@Override
	public void setTemplate(String value) {
		getEntityMarkDirty().setTemplate(value);
	}
}
