package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;

public class Organization extends Contact implements IOrganization {
	
	public Organization(Kontakt model){
		super(model);
	}

	@Override
	public String getInsuranceXmlName(){
		return getEntity().getAllergies();
	}

	@Override
	public void setInsuranceXmlName(String value){
		getEntity().setAllergies(value);
	}

	@Override
	public String getInsuranceLawCode(){
		return getEntity().getTitelSuffix();
	}

	@Override
	public void setInsuranceLawCode(String value){
		getEntity().setTitelSuffix(value);
	}
}
