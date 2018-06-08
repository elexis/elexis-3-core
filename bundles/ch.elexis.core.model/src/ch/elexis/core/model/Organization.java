package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;

public class Organization extends Contact implements IOrganization {
	
	public Organization(Kontakt model){
		super(model);
	}
}
