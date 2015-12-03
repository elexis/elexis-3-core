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
import java.util.Collections;
import java.util.List;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICodeElement;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class Leistungsblock extends PersistentObject implements ICodeElement {
	public static final String TABLENAME = "LEISTUNGSBLOCK"; //$NON-NLS-1$
	
	public static final String FLD_MANDANT_ID = "MandantID"; //$NON-NLS-1$
	public static final String FLD_NAME = "Name"; //$NON-NLS-1$
	public static final String FLD_LEISTUNGEN = "Leistungen"; //$NON-NLS-1$
	public static final String FLD_MACRO = "Macro"; //$NON-NLS-1$
	
	public static final String XIDDOMAIN = "www.xid.ch/id/elexis_leistungsblock"; //$NON-NLS-1$
	public static final String XIDDOMAIN_SIMPLENAME = "Leistungsblock";//$NON-NLS-1$
	
	static {
		addMapping(TABLENAME, FLD_NAME, FLD_MANDANT_ID, FLD_LEISTUNGEN, FLD_MACRO);
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, XIDDOMAIN_SIMPLENAME,
			Xid.ASSIGNMENT_LOCAL | Xid.QUALITY_GUID);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected Leistungsblock(String id){
		super(id);
	}
	
	protected Leistungsblock(){}
	
	public Leistungsblock(String Name, Mandant m){
		create(null);
		String[] f = new String[] {
			FLD_NAME, FLD_MANDANT_ID, FLD_MACRO
		};
		set(f, Name, m.getId(), Name);
	}
	
	public static Leistungsblock load(String id){
		return new Leistungsblock(id);
	}
	
	public String getName(){
		return checkNull(get(FLD_NAME));
	}
	
	/**
	 * @param name
	 * @since 3.1
	 */
	public void setName(String name){
		set(FLD_NAME, name);
	}
	
	@Override
	public String getLabel(){
		String name = getName();
		String macro = getMacro();
		if (macro.length() == 0 || macro.equals(name))
			return name;
		return name + " [" + macro + "]";
	}
	
	public String getText(){
		return get(FLD_NAME);
	}
	
	public String getCode(){
		return get(FLD_NAME);
	}
	
	/**
	 * @return the current valid macro, that is the value of {@link #FLD_MACRO} if defined, else
	 *         {@link #FLD_NAME}
	 * @since 3.1
	 */
	public String getMacro(){
		String[] vals = get(true, FLD_MACRO, FLD_NAME);
		if (vals[0].length() == 0)
			return vals[1];
		return vals[0];
	}
	
	/**
	 * @param macro
	 * @since 3.1
	 */
	public void setMacro(String macro){
		set(FLD_MACRO, macro);
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
	
	/**
	 * DOES NOT DELIVER the storeToString of this {@link Leistungsblock}, but a comma separated list
	 * of all contained {@link ICodeElement} objects
	 */
	@Override
	public String storeToString(){
		return toString(load());
	}
	
	public String toString(List<ICodeElement> lst){
		StringBuilder st = new StringBuilder();
		for (ICodeElement v : lst) {
			st.append(((PersistentObject) v).storeToString()).append(StringConstants.COMMA);
		}
		return st.toString().replaceFirst(",$", StringConstants.EMPTY); //$NON-NLS-1$
	}
	
	private boolean flush(List<ICodeElement> lst){
		try {
			if (lst == null) {
				lst = new ArrayList<ICodeElement>();
			}
			String storable = toString(lst);
			setBinary(FLD_LEISTUNGEN, CompEx.Compress(storable, CompEx.ZIP));
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
			byte[] compressed = getBinary(FLD_LEISTUNGEN);
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
		byte[] comp = getBinary(FLD_LEISTUNGEN);
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
	
	/**
	 * 
	 * @param macro
	 *            the macro name
	 * @return all {@link Leistungsblock} elements that are registered for the provided macro. This
	 *         includes all specifically registered for the given {@link Mandant} and all that are
	 *         available in general (no {@link Mandant} declaration)
	 * @since 3.1
	 */
	public static @NonNull List<Leistungsblock> findMacrosValidForCurrentMandator(
		@Nullable String macro){
		Mandant selectedMandator = ElexisEventDispatcher.getSelectedMandator();
		if (macro == null || selectedMandator == null)
			return Collections.emptyList();
		
		Query<Leistungsblock> qbe = new Query<Leistungsblock>(Leistungsblock.class);
		qbe.startGroup();
		qbe.add(Leistungsblock.FLD_NAME, Query.EQUALS, macro);
		qbe.or();
		qbe.add(Leistungsblock.FLD_MACRO, Query.EQUALS, macro);
		qbe.endGroup();
		qbe.startGroup();
		qbe.add(Leistungsblock.FLD_MANDANT_ID, Query.EQUALS,
			selectedMandator.getId());
		qbe.or();
		qbe.add(Leistungsblock.FLD_MANDANT_ID, Query.EQUALS, StringTool.leer);
		qbe.endGroup();
		
		List<Leistungsblock> execute = qbe.execute();
		ArrayList<Leistungsblock> ret = new ArrayList<>();
		for (Leistungsblock lb : execute) {
			String macro2 = checkNull(lb.getMacro());
			if (macro.equals(macro2) || macro2.length() == 0) {
				ret.add(lb);
			}
		}
		
		return ret;
	}
}
