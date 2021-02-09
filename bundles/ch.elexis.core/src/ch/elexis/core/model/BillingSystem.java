package ch.elexis.core.model;

import ch.elexis.core.model.ch.BillingLaw;

public class BillingSystem implements IBillingSystem {
	
	public static BillingSystem UNKNOWN = new BillingSystem("UNKNOWN", null);
	
	private final String name;
	private final BillingLaw law;
	
	public BillingSystem(String name, BillingLaw law){
		this.name = name;
		this.law = law;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public BillingLaw getLaw(){
		return law;
	}
}
