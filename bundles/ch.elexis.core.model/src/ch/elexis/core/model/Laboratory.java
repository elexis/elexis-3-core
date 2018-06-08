package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;

public class Laboratory extends Contact implements ILaboratory {
	
	public Laboratory(Kontakt model){
		super(model);
	}
}
