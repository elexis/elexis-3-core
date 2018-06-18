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
import java.util.Optional;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.CodeElementServiceHolder;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.data.dto.CodeElementDTO;
import ch.rgw.compress.CompEx;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.VersionInfo;

public class Leistungsblock extends PersistentObject implements ICodeElement {
	public static final String TABLENAME = "LEISTUNGSBLOCK"; //$NON-NLS-1$
	public static final String VERSION_ID = "Version";
	private static final String VERSION = "1.0.0";
	
	public static final String FLD_MANDANT_ID = "MandantID"; //$NON-NLS-1$
	public static final String FLD_NAME = "Name"; //$NON-NLS-1$
	public static final String FLD_LEISTUNGEN = "Leistungen"; //$NON-NLS-1$
	public static final String FLD_MACRO = "Macro"; //$NON-NLS-1$
	public static final String FLD_CODEELEMENTS = "codeelements"; //$NON-NLS-1$
	
	public static final String XIDDOMAIN = "www.xid.ch/id/elexis_leistungsblock"; //$NON-NLS-1$
	public static final String XIDDOMAIN_SIMPLENAME = "Leistungsblock";//$NON-NLS-1$
	
	private static final String SEPARATOR = ":=:";
	
	// @formatter:off
	private static final String upd100 =
			"ALTER TABLE " + TABLENAME + " ADD " + FLD_CODEELEMENTS + " TEXT;"
			+ "INSERT INTO " + TABLENAME + " (ID, " + FLD_NAME + ") VALUES (" + JdbcLink.wrap(Leistungsblock.VERSION_ID) + ", " + JdbcLink.wrap(VERSION) + ");";

	public static final String createDB = "CREATE TABLE " + TABLENAME + " (" 
		+"ID 					VARCHAR(25) primary key, "  
		+"lastupdate 			BIGINT," 
		+"deleted 				CHAR(1) default '0'," 
		
		+ "MandantID			VARCHAR(25),"
		+ "Name		 			VARCHAR(30),"
		+ "Macro				VARCHAR(30),"
		+ "Leistungen			BLOB," 
		+ "codeelements	 		TEXT" 
		+ ");"
		+ "CREATE INDEX block1 on " + TABLENAME + " (" + FLD_NAME + ");"		
		+ "CREATE INDEX block2 on " + TABLENAME + " (" + FLD_MANDANT_ID + ");"		
		+ "CREATE INDEX block3 on " + TABLENAME + " (" + FLD_MACRO + ");"		
		+ "INSERT INTO " + TABLENAME + " (ID, " + FLD_NAME + ") VALUES (" + JdbcLink.wrap(Leistungsblock.VERSION_ID) + ", " + JdbcLink.wrap(VERSION) + ");";
	//@formatter:on
	
	static {
		addMapping(TABLENAME, FLD_MANDANT_ID, FLD_NAME, FLD_LEISTUNGEN, FLD_MACRO,
			FLD_CODEELEMENTS);
		
		if (!tableExists(TABLENAME)) {
			createOrModifyTable(createDB);
		} else {
			Leistungsblock version = load(Leistungsblock.VERSION_ID);
			if (!version.exists()) {
				createOrModifyTable(upd100);
				Executors.newSingleThreadExecutor().execute(new Runnable() {
					@Override
					public void run(){
						updateToCodeelements();
					}
				});
			}
			VersionInfo vi = new VersionInfo(version.get(FLD_NAME));
			if (vi.isOlder(VERSION)) {
				// add update code here ...
				version.set(FLD_NAME, VERSION);
			}
		}
		
		Xid.localRegisterXIDDomainIfNotExists(XIDDOMAIN, XIDDOMAIN_SIMPLENAME,
			Xid.ASSIGNMENT_LOCAL | Xid.QUALITY_GUID);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	private static void updateToCodeelements(){
		Query<Leistungsblock> query = new Query<Leistungsblock>(Leistungsblock.class);
		query.add(Leistungsblock.FLD_ID, Query.NOT_EQUAL, Leistungsblock.VERSION_ID);
		List<Leistungsblock> blocks = query.execute();
		for (Leistungsblock leistungsblock : blocks) {
			List<ICodeElement> leistungen = getLeistungen(leistungsblock);
			for (ICodeElement iCodeElement : leistungen) {
				leistungsblock.addElement(iCodeElement);
			}
		}
	}
	
	private static List<ICodeElement> getLeistungen(Leistungsblock leistungsblock){
		ArrayList<ICodeElement> lst = new ArrayList<ICodeElement>();
		try {
			lst = new ArrayList<ICodeElement>();
			byte[] compressed = leistungsblock.getBinary(FLD_LEISTUNGEN);
			if (compressed != null) {
				String storable = new String(CompEx.expand(compressed), "UTF-8"); //$NON-NLS-1$
				for (String p : storable.split(",")) { //$NON-NLS-1$
					ICodeElement iCodeElement =
						(ICodeElement) CoreHub.poFactory.createFromString(p);
					if (iCodeElement != null) {
						lst.add(iCodeElement);
					} else {
						LoggerFactory.getLogger(Leistungsblock.class)
							.warn("Could not load code [" + p + "]");
					}
				}
			}
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return lst;
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
	 * Get an instantiated list of {@link ICodeElement} instances referenced by this block,
	 * non-resolvable elements are silently skipped.
	 * 
	 * @return a possibly empty list of ICodeElements
	 */
	public List<ICodeElement> getElements(){
		ICodeElementService service = CodeElementServiceHolder.getService();
		List<ICodeElement> ret = new ArrayList<>();
		if (service != null) {
			String codeelements = get(FLD_CODEELEMENTS);
			if (!codeelements.isEmpty()) {
				String[] parts = codeelements.split("\\" + SEPARATOR);
				for (String part : parts) {
					Optional<ICodeElement> created =
						service.createFromString(part, CodeElementServiceHolder.createContext());
					created.ifPresent(c -> ret.add(c));
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get a list of {@link ICodeElement} referenced by this block.
	 * 
	 * @return a possibly empty list of ICodeElements
	 */
	public List<ICodeElement> getElementReferences(){
		ICodeElementService service = CodeElementServiceHolder.getService();
		List<ICodeElement> ret = new ArrayList<>();
		if (service != null) {
			String codeelements = get(FLD_CODEELEMENTS);
			if (!codeelements.isEmpty()) {
				String[] parts = codeelements.split("\\" + SEPARATOR);
				for (String part : parts) {
					String[] elementParts = service.getStoreToStringParts(part);
					if (elementParts != null && elementParts.length > 1) {
						CodeElementDTO reference =
							new CodeElementDTO(elementParts[0], elementParts[1]);
						if (elementParts.length > 2) {
							reference.setText(elementParts[2]);
						}
						ret.add(reference);
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * Get a list of {@link ICodeElement} references of this block, which are not contained in the
	 * elements list. This is useful to determine if all {@link ICodeElement} of the block are in
	 * the elements list.
	 * 
	 * @param elements
	 * @return
	 */
	public List<ICodeElement> getDiffToReferences(List<ICodeElement> elements){
		List<ICodeElement> references = getElementReferences();
		if (references.size() > elements.size()) {
			// use copy to iterate 
			for (ICodeElement reference : references.toArray(new ICodeElement[references.size()])) {
				for (ICodeElement element : elements) {
					if (element.getCodeSystemName().equals(reference.getCodeSystemName())
						&& element.getCode().equals(reference.getCode())) {
						references.remove(reference);
					}
				}
			}
		} else {
			references.clear();
		}
		return references;
	}
	
	private int getIndexOf(List<ICodeElement> elements, ICodeElement element){
		if (element != null && elements != null) {
			for (int i = 0; i < elements.size(); i++) {
				String eCodeSystemName = element.getCodeSystemName();
				String esCodeSystemName = elements.get(i).getCodeSystemName();
				String eCode = element.getCode();
				String esCode = elements.get(i).getCode();
				if (eCodeSystemName != null && esCodeSystemName != null && eCode != null
					&& esCode != null) {
					if (eCodeSystemName.equals(esCodeSystemName) && eCode.equals(esCode)) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	/**
	 * Add a reference to the {@link ICodeElement} to this block. If there is already a reference to
	 * the {@link ICodeElement} in the block the new reference is added before that reference.
	 * 
	 * @param v
	 *            an Element
	 */
	public void addElement(ICodeElement element){
		if (element != null) {
			List<ICodeElement> elements = getElementReferences();
			int index = getIndexOf(elements, element);
			if (index != -1) {
				elements.add(index, element);
			} else {
				elements.add(element);
			}
			storeElements(elements);
		}
	}
	
	private void storeElements(List<ICodeElement> elements){
		ICodeElementService service = CodeElementServiceHolder.getService();
		if (service != null) {
			StringBuilder sb = new StringBuilder();
			for (ICodeElement element : elements) {
				if (sb.length() > 0) {
					sb.append(SEPARATOR);
				}
				sb.append(service.storeToString(element));
			}
			set(FLD_CODEELEMENTS, sb.toString());
		}
	}
	
	/**
	 * Remove the first matching reference to the {@link ICodeElement} from the block.
	 * 
	 * @param element
	 */
	public void removeElement(ICodeElement element){
		if (element != null) {
			List<ICodeElement> elements = getElementReferences();
			int index = getIndexOf(elements, element);
			if (index != -1) {
				elements.remove(index);
			}
			storeElements(elements);
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
	public void moveElement(ICodeElement element, int offset){
		if (element != null) {
			List<ICodeElement> elements = getElementReferences();
			int index = getIndexOf(elements, element);
			if (index != -1) {
				int npos = index + offset;
				if (npos < 0) {
					npos = 0;
				} else if (npos >= elements.size()) {
					npos = elements.size() - 1;
				}
				ICodeElement el = elements.remove(index);
				elements.add(npos, el);
				storeElements(elements);
			}
		}
	}
	
	public String toString(List<ICodeElement> lst){
		StringBuilder st = new StringBuilder();
		for (ICodeElement v : lst) {
			st.append(((PersistentObject) v).storeToString()).append(StringConstants.COMMA);
		}
		return st.toString().replaceFirst(",$", StringConstants.EMPTY); //$NON-NLS-1$
	}
	
	public String getCodeSystemName(){
		return "Block"; //$NON-NLS-1$
	}
	
	@Override
	public boolean isDragOK(){
		return true;
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
		qbe.add(Leistungsblock.FLD_ID, Query.NOT_EQUAL, Leistungsblock.VERSION_ID);
		qbe.startGroup();
		qbe.add(Leistungsblock.FLD_NAME, Query.EQUALS, macro);
		qbe.or();
		qbe.add(Leistungsblock.FLD_MACRO, Query.EQUALS, macro);
		qbe.endGroup();
		qbe.and();
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
	
	public boolean isEmpty(){
		return checkNull(get(FLD_CODEELEMENTS)).isEmpty();
	}
}
