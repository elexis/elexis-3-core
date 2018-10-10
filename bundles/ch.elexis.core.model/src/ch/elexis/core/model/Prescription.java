package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.prescription.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.util.internal.ModelUtil;

public class Prescription
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Prescription>
		implements IdentifiableWithXid, IPrescription {
	
	private ExtInfoHandler extInfoHandler;
	
	public Prescription(ch.elexis.core.jpa.entities.Prescription entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPatient(IPatient value){
		if (value != null) {
			getEntity().setPatient(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntity().setPatient(null);
		}
	}
	
	@Override
	public IArticle getArticle(){
		Optional<Identifiable> article = ModelUtil.getFromStoreToString(getEntity().getArtikel());
		return (IArticle) article.orElse(null);
	}
	
	@Override
	public void setArticle(IArticle article){
		if (article != null) {
			Optional<String> storeToString = ModelUtil.getStoreToString(article);
			getEntity().setArtikel(storeToString.orElse(null));
		} else {
			getEntity().setArtikel(null);
		}
	}
	
	@Override
	public LocalDateTime getDateFrom(){
		return getEntity().getDateFrom();
	}
	
	@Override
	public void setDateFrom(LocalDateTime date){
		getEntity().setDateFrom(date);
	}
	
	@Override
	public LocalDateTime getDateTo(){
		return getEntity().getDateUntil();
	}
	
	@Override
	public void setDateTo(LocalDateTime value){
		getEntity().setDateUntil(value);
	}
	
	@Override
	public String getDosageInstruction(){
		return getEntity().getDosis();
	}
	
	@Override
	public void setDosageInstruction(String dosageInstruction){
		getEntity().setDosis(dosageInstruction);
	}
	
	@Override
	public String getRemark(){
		return getEntity().getBemerkung();
	}
	
	@Override
	public void setRemark(String value){
		getEntity().setBemerkung(value);
	}
	
	@Override
	public String getStopReason(){
		return (String) extInfoHandler.getExtInfo(Constants.FLD_EXT_STOP_REASON);
	}
	
	@Override
	public void setStopReason(String value){
		extInfoHandler.setExtInfo(Constants.FLD_EXT_STOP_REASON, value);
	}
	
	@Override
	public EntryType getEntryType(){
		return getEntity().getEntryType();
	}
	
	@Override
	public void setEntryType(EntryType value){
		getEntity().setPrescriptionType(Integer.toString(value.numericValue()));
	}
	
	@Override
	public String getDisposalComment(){
		return (String) extInfoHandler.getExtInfo(Constants.FLD_EXT_DISPOSAL_COMMENT);
	}
	
	@Override
	public void setDisposalComment(String value){
		extInfoHandler.setExtInfo(Constants.FLD_EXT_DISPOSAL_COMMENT, value);
	}
	
}
