package ch.elexis.core.jpa.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "ACE_OBJECT")
public class ACEObject {
	
	private long id;
	
	private String object;
	
	private String objectId;

	private String right;
	
}
