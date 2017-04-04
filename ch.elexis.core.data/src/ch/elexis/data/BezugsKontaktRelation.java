package ch.elexis.data;

import java.util.List;
import java.util.UUID;

import ch.elexis.core.types.RelationshipType;

public class BezugsKontaktRelation {
	
	public static String VALUE_SEPERATOR = ";";
	private String name;
	private RelationshipType srcRelationType;
	private RelationshipType destRelationType;
	
	private final String id;
	
	public BezugsKontaktRelation(BezugsKontaktRelation bezugsKontaktRelation){
		this.id = bezugsKontaktRelation.getId();
		this.name = bezugsKontaktRelation.getName();
		this.srcRelationType = bezugsKontaktRelation.getSrcRelationType();
		this.destRelationType = bezugsKontaktRelation.getDestRelationType();
	}

	public BezugsKontaktRelation(){
		super();
		this.id = UUID.randomUUID().toString();
	}
	
	public BezugsKontaktRelation(String name, RelationshipType srcRelationType,
		RelationshipType destRelationType){
		this();
		this.name = name;
		this.srcRelationType = srcRelationType;
		this.destRelationType = destRelationType;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setSrcRelationType(RelationshipType srcRelationType){
		this.srcRelationType = srcRelationType;
	}
	
	public RelationshipType getSrcRelationType(){
		return srcRelationType;
	}
	
	public void setDestRelationType(RelationshipType destRelationType){
		this.destRelationType = destRelationType;
	}
	
	public RelationshipType getDestRelationType(){
		return destRelationType;
	}
	
	public BezugsKontaktRelation loadValuesByCfg(String cfgKey){
		String[] cfgParts = cfgKey.split(BezugsKontaktRelation.VALUE_SEPERATOR);
		int length = cfgParts.length;
		if (length > 0) {
			this.setName(cfgParts[0]);
			if (length > 1) {
				this.setSrcRelationType(RelationshipType.get(getIntValue(cfgParts[1])));
				if (length > 2) {
					this.setDestRelationType(RelationshipType.get(getIntValue(cfgParts[2])));
				}
			}
			else {
				this.setSrcRelationType(RelationshipType.AGENERIC);
				this.setDestRelationType(RelationshipType.AGENERIC);
			}
		}
		return this;
	}
	
	public String getCfgString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append(name);
		buffer.append(VALUE_SEPERATOR);
		buffer.append(
			srcRelationType != null ? srcRelationType.getValue() : RelationshipType.AGENERIC_VALUE);
		buffer.append(VALUE_SEPERATOR);
		buffer.append(destRelationType != null ? destRelationType.getValue()
				: RelationshipType.AGENERIC_VALUE);
		return buffer.toString();
	}
	
	private int getIntValue(String txt){
		try {
			return Integer.parseInt(txt);
		} catch (NumberFormatException e) {
			
		}
		return RelationshipType.AGENERIC.getValue();
	}
	
	/**
	 * updates the current {@link BezugsKontakt} with the values of the new one
	 */
	public void updateToNewBezugKontakt(BezugsKontaktRelation newBezugKontaktRelation){
		List<BezugsKontakt> bezugsKontakts = findAllBezugKontaksByName(this.getName(), true);
		for (BezugsKontakt bezugsKontakt : bezugsKontakts) {
			bezugsKontakt.set(new String[] {
				BezugsKontakt.RELATION,
				BezugsKontakt.FLD_MY_RTYPE, BezugsKontakt.FLD_OTHER_RTYPE
			}, newBezugKontaktRelation.getName(),
				String.valueOf(newBezugKontaktRelation.getDestRelationType().getValue()),
				String.valueOf(newBezugKontaktRelation.getSrcRelationType().getValue()));
		}
	}
	
	/**
	 * returns all {@link BezugsKontakt} by name
	 * 
	 * @param name
	 * @param ignoreCase
	 * @return all {@link BezugsKontakt} by name
	 */
	public List<BezugsKontakt> findAllBezugKontaksByName(String name, boolean ignoreCase){
		Query<BezugsKontakt> query = new Query<>(BezugsKontakt.class);
		query.add(BezugsKontakt.RELATION, Query.EQUALS, name, ignoreCase);
		return query.execute();
	}
	
	public String getId(){
		return id;
	}
	
}
