package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Brief;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Rezept;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Recipe extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Rezept>
		implements IdentifiableWithXid, IRecipe {

	public Recipe(Rezept entity) {
		super(entity);
	}

	@Override
	public IPatient getPatient() {
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
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
	public IMandator getMandator() {
		return ModelUtil.getAdapter(getEntity().getMandant(), IMandator.class);
	}

	@Override
	public void setMandator(IMandator value) {
		if (value != null) {
			getEntityMarkDirty().setMandant(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setMandant(null);
		}
	}

	@Override
	public LocalDateTime getDate() {
		return getEntity().getDatum().atStartOfDay();
	}

	@Override
	public void setDate(LocalDateTime value) {
		getEntityMarkDirty().setDatum(value.toLocalDate());
	}

	@Override
	public List<IPrescription> getPrescriptions() {
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getPrescriptions().parallelStream().filter(p -> !p.isDeleted())
				.map(b -> ModelUtil.getAdapter(b, IPrescription.class, true)).collect(Collectors.toList());
	}

	@Override
	public void removePrescription(IPrescription prescription) {
		CoreModelServiceHolder.get().delete(prescription);
	}

	@Override
	public IDocumentLetter getDocument() {
		return ModelUtil.getAdapter(getEntity().getBrief(), IDocumentLetter.class);
	}

	@Override
	public void setDocument(IDocumentLetter value) {
		if (value != null) {
			getEntityMarkDirty().setBrief(((AbstractIdModelAdapter<Brief>) value).getEntity());
		} else {
			getEntityMarkDirty().setBrief(null);
		}
	}

	@Override
	public String getLabel() {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		IMandator m = getMandator();
		if (m == null) {
			return dateFormat.format(getDate()) + " (unbekannt)";
		}
		return dateFormat.format(getDate()) + " " + m.getLabel();
	}
}
