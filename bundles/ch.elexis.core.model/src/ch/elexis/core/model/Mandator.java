package ch.elexis.core.model;

import ch.elexis.core.jpa.entities.Kontakt;

public class Mandator extends Contact implements IMandator {
	
	public Mandator(Kontakt model){
		super(model);
	}
}
