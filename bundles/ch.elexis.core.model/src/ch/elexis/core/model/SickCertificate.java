package ch.elexis.core.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.AUF;
import ch.elexis.core.jpa.entities.Brief;
import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;

public class SickCertificate extends AbstractIdDeleteModelAdapter<AUF>
		implements IdentifiableWithXid, ISickCertificate {
	
	public SickCertificate(AUF entity){
		super(entity);
	}
	
	@Override
	public IPatient getPatient(){
		return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
	}
	
	@Override
	public void setPatient(IPatient value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setPatient((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setPatient(null);
		}
	}
	
	@Override
	public ICoverage getCoverage(){
		return ModelUtil.getAdapter(getEntity().getFall(), ICoverage.class);
	}
	
	@Override
	public void setCoverage(ICoverage value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setFall((Fall) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setFall(null);
		}
	}
	
	@Override
	public IDocumentLetter getLetter(){
		return ModelUtil.getAdapter(getEntity().getBrief(), IDocumentLetter.class);
	}
	
	@Override
	public void setLetter(IDocumentLetter value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntityMarkDirty()
				.setBrief((Brief) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setBrief(null);
		}
	}
	
	@Override
	public int getPercent(){
		return getEntity().getProzent();
	}
	
	@Override
	public void setPercent(int value){
		getEntityMarkDirty().setProzent(value);
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntityMarkDirty().setDate(value);
	}
	
	@Override
	public LocalDate getStart(){
		return getEntity().getDateFrom();
	}
	
	@Override
	public void setStart(LocalDate value){
		getEntityMarkDirty().setDateFrom(value);
	}
	
	@Override
	public LocalDate getEnd(){
		return getEntity().getDateUntil();
	}
	
	@Override
	public void setEnd(LocalDate value){
		getEntityMarkDirty().setDateUntil(value);
	}
	
	@Override
	public String getReason(){
		return StringUtils.defaultString(getEntity().getReason());
	}
	
	@Override
	public void setReason(String value){
		getEntityMarkDirty().setReason(value);
	}
	
	@Override
	public String getNote(){
		return StringUtils.defaultString(getEntity().getNote());
	}
	
	@Override
	public void setNote(String value){
		getEntityMarkDirty().setNote(value);
	}
	
	@Override
	public String getLabel(){
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		
		StringBuilder sb = new StringBuilder();
		if (getDate() != null) {
			sb.append("[").append(dateFormat.format(getDate())).append("]: ");
		}
		sb.append(dateFormat.format(getStart())).append("-").append(dateFormat.format(getEnd()))
			.append(": ").append(getPercent()).append("% (").append(getReason()).append(")");
		return sb.toString();
	}
}
