package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.entities.VKPreis;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;

public class BillingSystemFactor extends AbstractIdModelAdapter<VKPreis>
		implements IdentifiableWithXid, IBillingSystemFactor {
	
	public BillingSystemFactor(VKPreis entity){
		super(entity);
	}
	
	@Override
	public String getSystem(){
		return getEntity().getTyp();
	}
	
	@Override
	public void setSystem(String value){
		getEntity().setTyp(value);
	}
	
	@Override
	public double getFactor(){
		String strValue = getEntity().getMultiplikator();
		if (strValue != null) {
			try {
				return Double.parseDouble(strValue);
			} catch (NumberFormatException e) {
				// ignore not a double ... return 0
			}
		}
		return 0;
	}
	
	@Override
	public void setFactor(double value){
		getEntityMarkDirty().setMultiplikator(Double.toString(value));
	}
	
	@Override
	public LocalDate getValidFrom(){
		return getEntity().getDatum_von();
	}
	
	@Override
	public void setValidFrom(LocalDate value){
		getEntityMarkDirty().setDatum_von(value);
	}
	
	@Override
	public LocalDate getValidTo(){
		return getEntity().getDatum_bis();
	}
	
	@Override
	public void setValidTo(LocalDate value){
		getEntityMarkDirty().setDatum_bis(value);
	}
}
