package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.util.Optional;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.prescription.Constants;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

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
			getEntityMarkDirty().setPatient(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setPatient(null);
		}
	}
	
	@Override
	public IContact getPrescriptor(){
		return ModelUtil.getAdapter(getEntity().getPrescriptor(), IContact.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPrescriptor(IContact value){
		if (value != null) {
			getEntityMarkDirty()
				.setPrescriptor(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntityMarkDirty().setPrescriptor(null);
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
			getEntityMarkDirty().setArtikel(storeToString.orElse(null));
		} else {
			getEntityMarkDirty().setArtikel(null);
		}
	}
	
	@Override
	public LocalDateTime getDateFrom(){
		return getEntity().getDateFrom();
	}
	
	@Override
	public void setDateFrom(LocalDateTime date){
		getEntityMarkDirty().setDateFrom(date);
	}
	
	@Override
	public LocalDateTime getDateTo(){
		return getEntity().getDateUntil();
	}
	
	@Override
	public void setDateTo(LocalDateTime value){
		getEntityMarkDirty().setDateUntil(value);
	}
	
	@Override
	public String getDosageInstruction(){
		return getEntity().getDosis();
	}
	
	@Override
	public void setDosageInstruction(String dosageInstruction){
		getEntityMarkDirty().setDosis(dosageInstruction);
	}
	
	@Override
	public String getRemark(){
		return getEntity().getBemerkung();
	}
	
	@Override
	public void setRemark(String value){
		getEntityMarkDirty().setBemerkung(value);
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
		getEntityMarkDirty().setEntryType(value);
	}
	
	@Override
	public String getDisposalComment(){
		return (String) extInfoHandler.getExtInfo(Constants.FLD_EXT_DISPOSAL_COMMENT);
	}
	
	@Override
	public void setDisposalComment(String value){
		extInfoHandler.setExtInfo(Constants.FLD_EXT_DISPOSAL_COMMENT, value);
	}
	
	@Override
	public int getSortOrder(){
		return getEntity().getSortorder();
	}
	
	@Override
	public void setSortOrder(int value){
		getEntityMarkDirty().setSortorder(value);
	}
	
	@Override
	public boolean isApplied(){
		String value = (String) getExtInfo(Constants.FLD_EXT_IS_APPLIED);
		return value != null ? Boolean.valueOf(value) : false;
	}
	
	@Override
	public void setApplied(boolean value){
		setExtInfo(Constants.FLD_EXT_IS_APPLIED, Boolean.toString(value));
	}
	
	@Override
	public IRecipe getRecipe(){
		String recipeId = getEntity().getRezeptID();
		if (recipeId != null && !recipeId.isEmpty()) {
			return CoreModelServiceHolder.get().load(recipeId, IRecipe.class).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setRecipe(IRecipe value){
		getEntityMarkDirty().setRezeptID(value.getId());
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public IBilled getBilled(){
		String billedId = (String) extInfoHandler.getExtInfo(Constants.FLD_EXT_VERRECHNET_ID);
		if (billedId != null && !billedId.isEmpty()) {
			return CoreModelServiceHolder.get().load(billedId, IBilled.class).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setBilled(IBilled value){
		if (value != null) {
			extInfoHandler.setExtInfo(Constants.FLD_EXT_VERRECHNET_ID, value.getId());
		} else {
			extInfoHandler.setExtInfo(Constants.FLD_EXT_VERRECHNET_ID, null);
		}
		
	}
}
