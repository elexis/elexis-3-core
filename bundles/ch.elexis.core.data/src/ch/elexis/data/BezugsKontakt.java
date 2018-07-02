/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

import ch.elexis.core.types.RelationshipType;

public class BezugsKontakt extends PersistentObject {
	public static final String RELATION = "Bezug"; //$NON-NLS-1$
	public static final String OTHER_ID = "otherID"; //$NON-NLS-1$
	public static final String MY_ID = "myID"; //$NON-NLS-1$
	public static final String FLD_MY_RTYPE = "myRType"; // formal relationship type
	public static final String FLD_OTHER_RTYPE = "otherRType"; // other formal relationship
	
	public static final String TABLENAME = "KONTAKT_ADRESS_JOINT"; //$NON-NLS-1$
	
	static {
		addMapping(TABLENAME, MY_ID, OTHER_ID, RELATION, FLD_MY_RTYPE, FLD_OTHER_RTYPE);
	}
	
	/**
	 * @deprecated
	 * @param kontakt
	 * @param adr
	 * @param bezug
	 */
	public BezugsKontakt(Kontakt kontakt, Kontakt adr, String bezug){
		this(kontakt, adr,
			new BezugsKontaktRelation(bezug, RelationshipType.AGENERIC, RelationshipType.AGENERIC));
	}
	
	/**
	 * 
	 * @param kontakt
	 * @param adr
	 * @param bezugsKontaktType
	 * @since 3.2
	 */
	public BezugsKontakt(Kontakt kontakt, Kontakt adr, BezugsKontaktRelation bezugsKontaktType){
		create(null);
		
		set(new String[] {
			MY_ID, OTHER_ID, RELATION, FLD_MY_RTYPE, FLD_OTHER_RTYPE
		}, kontakt.getId(), adr.getId(), bezugsKontaktType.getName(),
			String.valueOf(bezugsKontaktType.getDestRelationType().getValue()),
			String.valueOf(bezugsKontaktType.getSrcRelationType().getValue()));
	}
	
	/**
	 * Updates the relation of a {@link BezugsKontakt}
	 * 
	 * @param bezugsKontaktRelation
	 */
	public void updateRelation(BezugsKontaktRelation bezugsKontaktRelation){
		set(new String[] {
			BezugsKontakt.RELATION, BezugsKontakt.FLD_MY_RTYPE, BezugsKontakt.FLD_OTHER_RTYPE
		}, bezugsKontaktRelation.getName(),
			bezugsKontaktRelation.getDestRelationType().getLiteral(),
			bezugsKontaktRelation.getSrcRelationType().getLiteral());
	}
	
	@Override
	public String getLabel(){
		Kontakt k = Kontakt.load(get(OTHER_ID));
		if (k.isValid()) {
			String rel = get(RELATION);
			if (rel.isEmpty()) {
				rel = get(FLD_OTHER_RTYPE);
				if (!rel.isEmpty()) {
					try {
						RelationshipType type = RelationshipType.get(Integer.parseInt(rel));
						if (type != null) {
							rel = type.getLocaleText();
						}
					} catch (Exception e) {
						
					}
				}
			}
			return rel + ": " + k.getLabel(); //$NON-NLS-1$
		} else {
			return Messages.BezugsKontakt_ContactDoesntExist;
		}
	}
	
	public static BezugsKontakt load(String id){
		return new BezugsKontakt(id);
	}
	
	public Kontakt getBezugsKontakt(){
		return Kontakt.load(get(OTHER_ID));
	}
	
	public String getBezug(){
		return checkNull(get(RELATION));
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected BezugsKontakt(){}
	
	protected BezugsKontakt(String id){
		super(id);
	}
}
