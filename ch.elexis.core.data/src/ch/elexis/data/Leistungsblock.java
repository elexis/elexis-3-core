/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.ICodeElement;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;

public class Leistungsblock extends PersistentObject implements ICodeElement {
	public static final String TABLENAME = "LEISTUNGSBLOCK"; //$NON-NLS-1$
	public static final String LEISTUNGEN = "Leistungen"; //$NON-NLS-1$
	public static final String MANDANT_ID = "MandantID"; //$NON-NLS-1$
	public static final String NAME = "Name"; //$NON-NLS-1$
	public static final String XIDDOMAIN = "www.xid.ch/id/elexis_leistungsblock"; //$NON-NLS-1$
	
	static {
		addMapping(TABLENAME, NAME, MANDANT_ID, LEISTUNGEN);
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, "Leistungsblock", Xid.ASSIGNMENT_LOCAL //$NON-NLS-1$
			| Xid.QUALITY_GUID);
	}
	
	public Leistungsblock(String Name, Mandant m){
		create(null);
		String[] f = new String[] {
			NAME, MANDANT_ID
		};
		set(f, Name, m.getId());
	}
	
	public String getName(){
		return checkNull(get(NAME));
	}
	
	/**
	 * return a List of elements contained in this block will never return null, but the list might
	 * be empty
	 * 
	 * @return a possibly empty list of ICodeElements
	 */
	public List<ICodeElement> getElements(){
		return load();
	}
	
	/**
	 * Add an ICodeElement to this block
	 * 
	 * @param v
	 *            an Element
	 */
	public void addElement(ICodeElement v){
		if (v != null) {
			List<ICodeElement> lst = load();
			int i = 0;
			for (ICodeElement ice : lst) {
				if (ice.getCode().compareTo(v.getCode()) > 0) {
					break;
				}
				i++;
			}
			lst.add(i, v);
			flush(lst);
		}
	}
	
	public void removeElement(ICodeElement v){
		if (v != null) {
			List<ICodeElement> lst = load();
			lst.remove(v);
			flush(lst);
		}
	}
	
	/**
	 * Move a CodeElement inside the block
	 * 
	 * @param v
	 *            the element to move
	 * @param offset
	 *            offset to move. negative values move up, positive down
	 */
	public void moveElement(ICodeElement v, int offset){
		if (v != null) {
			List<ICodeElement> lst = load();
			int idx = lst.indexOf(v);
			if (idx != -1) {
				int npos = idx + offset;
				if (npos < 0) {
					npos = 0;
				} else if (npos >= lst.size()) {
					npos = lst.size() - 1;
				}
				ICodeElement el = lst.remove(idx);
				lst.add(npos, el);
				flush(lst);
			}
		}
	}
	
	@Override
	public String storeToString(){
		return toString(load());
	}
	
	public String toString(List<ICodeElement> lst){
		StringBuilder st = new StringBuilder();
		for (ICodeElement v : lst) {
			st.append(((PersistentObject) v).storeToString()).append(","); //$NON-NLS-1$
		}
		return st.toString().replaceFirst(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
	@Override
	public String getLabel(){
		return get(NAME);
	}
	
	public String getText(){
		return get(NAME);
	}
	
	public String getCode(){
		return get(NAME);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Leistungsblock load(String id){
		return new Leistungsblock(id);
	}
	
	protected Leistungsblock(String id){
		super(id);
	}
	
	protected Leistungsblock(){}
	
	private boolean flush(List<ICodeElement> lst){
		try {
			if (lst == null) {
				lst = new ArrayList<ICodeElement>();
			}
			String storable = toString(lst);
			setBinary(LEISTUNGEN, CompEx.Compress(storable, CompEx.ZIP));
			return true;
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return false;
	}
	
	private List<ICodeElement> load(){
		ArrayList<ICodeElement> lst = new ArrayList<ICodeElement>();
		try {
			lst = new ArrayList<ICodeElement>();
			byte[] compressed = getBinary(LEISTUNGEN);
			if (compressed != null) {
				String storable = new String(CompEx.expand(compressed), "UTF-8"); //$NON-NLS-1$
				for (String p : storable.split(",")) { //$NON-NLS-1$
					lst.add((ICodeElement) CoreHub.poFactory.createFromString(p));
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return lst;
	}
	
	@Deprecated
	public boolean isEmpty(){
		byte[] comp = getBinary(LEISTUNGEN);
		return (comp == null);
	}
	
	public String getCodeSystemName(){
		return "Block"; //$NON-NLS-1$
	}
	
	public String getCodeSystemCode(){
		return "999"; //$NON-NLS-1$
	}
	
	@Override
	public boolean isDragOK(){
		return true;
	}
	
	@Override
	public List<Object> getActions(Object kontext){
		// TODO Auto-generated method stub
		return null;
	}
}
