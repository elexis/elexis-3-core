package ch.elexis.core.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ch.elexis.core.jpa.entities.Fall;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.rgw.tools.Money;

public class Invoice extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Invoice>
		implements IdentifiableWithXid, IInvoice {
	
	private ExtInfoHandler extInfoHandler;
	
	public Invoice(ch.elexis.core.jpa.entities.Invoice entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
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
	public InvoiceState getState(){
		return getEntity().getState();
	}
	
	@Override
	public void setState(InvoiceState value){
		getEntity().setState(value);
	}
	
	@Override
	public String getNumber(){
		return getEntity().getNumber();
	}
	
	@Override
	public IMandator getMandator(){
		return ModelUtil.getAdapter(getEntity().getMandator(), IMandator.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setMandator(IMandator value){
		if (value != null) {
			getEntity().setMandator(((AbstractIdModelAdapter<Kontakt>) value).getEntity());
		} else {
			getEntity().setMandator(null);
		}
	}
	
	@Override
	public ICoverage getCoverage(){
		return ModelUtil.getAdapter(getEntity().getFall(), ICoverage.class);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setCoverage(ICoverage value){
		if (value != null) {
			getEntity().setFall(((AbstractIdModelAdapter<Fall>) value).getEntity());
		} else {
			getEntity().setFall(null);
		}
	}
	
	@Override
	public List<IEncounter> getEncounters(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getEncounters().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IEncounter.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public List<IBilled> getBilled(){
		CoreModelServiceHolder.get().refresh(this);
		return getEntity().getInvoiceBilled().parallelStream().filter(b -> !b.isDeleted())
			.map(b -> ModelUtil.getAdapter(b, IInvoiceBilled.class, true))
			.collect(Collectors.toList());
	}
	
	@Override
	public void addTrace(String name, String value){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<String> getTrace(String name){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getInvoiceDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntity().setInvoiceDate(value);
	}
	
	@Override
	public LocalDate getDateFrom(){
		return getEntity().getInvoiceDateFrom();
	}
	
	@Override
	public void setDateFrom(LocalDate value){
		getEntity().setInvoiceDateFrom(value);
	}
	
	@Override
	public LocalDate getDateTo(){
		return getEntity().getInvoiceDateTo();
	}
	
	@Override
	public void setDateTo(LocalDate value){
		getEntity().setInvoiceDateTo(value);
	}
	
	@Override
	public Money getTotalAmount(){
		return ch.elexis.core.model.util.ModelUtil.getMoneyForCentString(getEntity().getAmount())
			.orElse(null);
	}
	
	@Override
	public void setTotalAmount(Money value){
		if (value != null) {
			getEntity().setAmount(value.getCentsAsString());
		} else {
			getEntity().setAmount(null);
		}
	}
	
	@Override
	public Money getOpenAmount(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Money getPayedAmount(){
		// TODO Auto-generated method stub
		return null;
	}
	
}
