package ch.elexis.core.model.agenda;

public class Area {
	
	private final String name;
	private final AreaType type;
	private final String contactId;
	
	/**
	 * 
	 * @param name
	 *            the name of the area
	 * @param type
	 * @param contactId
	 *            <code>null</code> if {@link AreaType#GENERIC}, else the contact id
	 */
	public Area(String name, AreaType type, String contactId){
		this.name = name;
		this.type = type;
		this.contactId = contactId;
	}
	
	public String getName(){
		return name;
	}
	
	public AreaType getType(){
		return type;
	}
	
	public String getContactId(){
		return contactId;
	}
	
}
