package ch.elexis.core.jpa.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ACE_OBJECT")
public class ACEObject {
	
	private long id;
	
	private String object;
	
	private String objectId;

	private String right;
	
}
