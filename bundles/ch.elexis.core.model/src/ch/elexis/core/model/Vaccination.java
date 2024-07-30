package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Vaccination extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Vaccination>
		implements IdentifiableWithXid, IVaccination {

	private static final String SIDE = "Side"; //$NON-NLS-1$

	public Vaccination(ch.elexis.core.jpa.entities.Vaccination entity) {
		super(entity);
	}

	@Override
	public IPatient getPatient() {
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class, true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPatient(IPatient value) {
		if (value != null) {
			getEntityMarkDirty().setPatient(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setPatient(null);
		}
	}

	@Override
	public IArticle getArticle() {
		Optional<Identifiable> article = ModelUtil.getFromStoreToString(getEntity().getArticle());
		return (IArticle) article.orElse(null);
	}

	@Override
	public void setArticle(IArticle article) {
		if (article != null) {
			Optional<String> storeToString = ModelUtil.getStoreToString(article);
			getEntityMarkDirty().setArticle(storeToString.orElse(null));
		} else {
			getEntityMarkDirty().setArticle(null);
		}
	}

	@Override
	public String getArticleName() {
		return getEntity().getArticleName();
	}

	@Override
	public void setArticleName(String value) {
		getEntityMarkDirty().setArticleName(value);
	}

	@Override
	public String getArticleGtin() {
		return getEntity().getArticleGtin();
	}

	@Override
	public void setArticleGtin(String value) {
		getEntityMarkDirty().setArticleGtin(value);
	}

	@Override
	public String getArticleAtc() {
		return getEntity().getArticleAtc();
	}

	@Override
	public void setArticleAtc(String value) {
		getEntityMarkDirty().setArticleAtc(value);
	}

	@Override
	public String getLotNumber() {
		return getEntity().getLotNumber();
	}

	@Override
	public void setLotNumber(String value) {
		getEntityMarkDirty().setLotNumber(value);
	}

	@Override
	public LocalDate getDateOfAdministration() {
		return getEntity().getDateOfAdministration();
	}

	@Override
	public void setDateOfAdministration(LocalDate value) {
		getEntityMarkDirty().setDateOfAdministration(value);
	}

	@Override
	public String getIngredientsAtc() {
		return getEntity().getIngredientsAtc();
	}

	@Override
	public void setIngredientsAtc(String value) {
		getEntityMarkDirty().setIngredientsAtc(value);
	}

	@Override
	public IContact getPerformer() {
		Optional<Identifiable> contact = ModelUtil.getFromStoreToString(getEntity().getPerformer());
		return (IContact) contact.orElse(null);
	}

	@Override
	public void setPerformer(IContact value) {
		if (value != null) {
			Optional<String> storeToString = ModelUtil.getStoreToString(value);
			getEntityMarkDirty().setPerformer(storeToString.orElse(null));
		} else {
			getEntityMarkDirty().setPerformer(null);
		}
	}

	@Override
	public String getSide() {
		return (String) getExtInfo(SIDE);
	}

	@Override
	public void setSide(String value) {
		setExtInfo(SIDE, value);
	}

	@Override
	public Object getExtInfo(Object key) {
		return extInfoHandler.getExtInfo(key);
	}

	@Override
	public void setExtInfo(Object key, Object value) {
		extInfoHandler.setExtInfo(key, value);
	}

	@Override
	public Map<Object, Object> getMap() {
		return extInfoHandler.getMap();
	}

	@Override
	public String getLabel() {
		return getSimpleLabel();
	}

	private String getSimpleLabel() {
		IArticle art = getArticle();
		if (art != null) {
			return art.getLabel();
		} else {
			return getArticleName();
		}
	}

	@Override
	public String getPerformerLabel() {
		IContact localPerformer = getPerformer();
		if (localPerformer != null) {
			return localPerformer.getLabel();
		}
		return getEntity().getPerformer();
	}

	@Override
	public void setPerformerLabel(String label) {
		getEntityMarkDirty().setPerformer(label);
	}
}
