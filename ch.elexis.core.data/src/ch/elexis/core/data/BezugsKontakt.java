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

package ch.elexis.core.data;

public class BezugsKontakt extends PersistentObject {
	public static final String RELATION = "Bezug"; //$NON-NLS-1$
	public static final String OTHER_ID = "otherID"; //$NON-NLS-1$
	public static final String MY_ID = "myID"; //$NON-NLS-1$
	private static final String tablename = "KONTAKT_ADRESS_JOINT"; //$NON-NLS-1$
	static {
		addMapping(tablename, MY_ID, OTHER_ID, RELATION);
	}
	
	public BezugsKontakt(Kontakt kontakt, Kontakt adr, String bezug){
		create(null);
		set(new String[] {
			MY_ID, OTHER_ID, RELATION
		}, kontakt.getId(), adr.getId(), bezug);
	}
	
	@Override
	public String getLabel(){
		Kontakt k = Kontakt.load(get(OTHER_ID));
		if (k.isValid()) {
			return get(RELATION) + ": " + k.getLabel(); //$NON-NLS-1$
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
		return tablename;
	}
	
	protected BezugsKontakt(){}
	
	protected BezugsKontakt(String id){
		super(id);
	}
	
}
