/*******************************************************************************
 * Copyright (c) 2006-2012, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - orders are now persisted into own table 
 *    
 *******************************************************************************/

package ch.elexis.core.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Log;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class Bestellung extends PersistentObject {
	public static final String VERSION = "1.0.0";
	private static final String FLD_ITEMS = "Liste"; //$NON-NLS-1$
	private static final String TABLENAME = "BESTELLUNGEN"; //$NON-NLS-1$
	
	public static final String ISORDERED = "ch.elexis.data.Bestellung.isOrdered"; //$NON-NLS-1$
	
	private List<Item> alItems;
	
	private static Log logger = Log.get(Bestellung.class.getName());
	
	public enum ListenTyp {
		PHARMACODE, NAME, VOLL
	};
	
	static final String create = "CREATE TABLE " + TABLENAME + " ("
		+ "ID       	VARCHAR(80) primary key, " + "lastupdate BIGINT,"
		+ "deleted  	CHAR(1) default '0'," + "datum      CHAR(8)," + "Contents 	BLOB);"
		+ "INSERT INTO " + TABLENAME + " (ID,datum,deleted) VALUES ('1'," + JdbcLink.wrap(VERSION)
		+ ",'1');";
	
	public static void initialize(){
		createOrModifyTable(create);
		
		// Starting with 2.1.7.rc0 orders are excavated from the HEAP2 table into an own table,
		// the following lines move the respective entries into the new table
		Query<NamedBlob2> eoq = new Query<NamedBlob2>(NamedBlob2.class);
		List<NamedBlob2> eor = eoq.execute();
		for (NamedBlob2 nb2 : eor) {
			String[] entry = nb2.getId().split(":");
			if (entry.length == 3) {
				Bestellung b = new Bestellung();
				if (b.create(nb2.getId())) {
					b.set(FLD_ITEMS, nb2.getString());
					logger.log("Moved order " + nb2.getId() + " from HEAP2 to " + TABLENAME,
						Log.INFOS);
					nb2.delete();
				} else {
					logger.log("Error creating " + nb2.getId() + " in table " + TABLENAME,
						Log.INFOS);
				}
			}
		}
	}
	
	static {
		addMapping(TABLENAME, "Liste=S:C:Contents"); //$NON-NLS-1$
		
		// Starting with 2.1.7.rc0 orders are excavated from the HEAP2 table, here
		// we check whether the table is existing (new method), else we need to call
		// the merge code
		if (!PersistentObject.tableExists(TABLENAME))
			initialize();
	}
	
	public Bestellung(String name, Anwender an){
		TimeTool t = new TimeTool();
		create(name + ":" + t.toString(TimeTool.TIMESTAMP) + ":" + an.getId()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public String getLabel(){
		String[] i = getId().split(":"); //$NON-NLS-1$
		TimeTool t = new TimeTool(i[1]);
		return i[0] + ": " + t.toString(TimeTool.FULL_GER); //$NON-NLS-1$
	}
	
	public String asString(ListenTyp type){
		initItems();
		StringBuilder ret = new StringBuilder();
		for (Item i : alItems) {
			switch (type) {
			case PHARMACODE:
				ret.append(i.art.getPharmaCode());
				break;
			case NAME:
				ret.append(i.art.getLabel());
				break;
			case VOLL:
				ret.append(i.art.getPharmaCode()).append(StringTool.space).append(i.art.getName());
				break;
			default:
				break;
			}
			ret.append(",").append(i.num).append(StringTool.lf); //$NON-NLS-1$
		}
		return ret.toString();
	}
	
	public List<Item> asList(){
		initItems();
		return alItems;
	}
	
	public void addItem(Artikel art, int num){
		initItems();
		Item i = findItem(art);
		if (i != null) {
			i.num += num;
		} else {
			alItems.add(new Item(art, num));
		}
	}
	
	public Item findItem(Artikel art){
		initItems();
		for (Item i : alItems) {
			if (i.art.getId().equals(art.getId())) {
				return i;
			}
		}
		return null;
	}
	
	public void removeItem(Item art){
		initItems();
		alItems.remove(art);
		
	}
	
	public void save(){
		initItems();
		StringBuilder sb = new StringBuilder();
		for (Item i : alItems) {
			sb.append(i.art.getId()).append(",").append(i.num).append(";"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		set(FLD_ITEMS, sb.toString());
	}
	
	private void initItems(){
		if (alItems == null) {
			load();
		}
	}
	
	public void load(){
		String[] it = checkNull(get(FLD_ITEMS)).split(";"); //$NON-NLS-1$
		if (alItems == null) {
			alItems = new ArrayList<Item>();
		} else {
			alItems.clear();
		}
		for (String i : it) {
			String[] fld = i.split(","); //$NON-NLS-1$
			if (fld.length == 2) {
				Artikel art = Artikel.load(fld[0]);
				if (art != null && art.exists()) {
					alItems.add(new Item(art, Integer.parseInt(fld[1])));
				}
			}
		}
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Bestellung load(String id){
		Bestellung ret = new Bestellung(id);
		if (ret != null) {
			// load items lazy
			// ret.load();
		}
		return ret;
	}
	
	protected Bestellung(){}
	
	protected Bestellung(String id){
		super(id);
	}
	
	public static class Item {
		public Item(Artikel a, int n){
			art = a;
			num = n;
		}
		
		public Artikel art;
		public int num;
	}
	
	public static void markAsOrdered(Item[] list){
		for (Item item : list) {
			if (item.art != null)
				item.art.setExt(Bestellung.ISORDERED, "true");
		}
	}
	
	public static Bestellung lookupLastWithArticle(Artikel artikel){
		Query<Bestellung> qbe = new Query<Bestellung>(Bestellung.class);
		List<Bestellung> bestellungen = qbe.execute();
		Collections.sort(bestellungen, new BestellungDateComparator());
		for (Bestellung bestellung : bestellungen) {
			List<Item> items = bestellung.asList();
			for (Item item : items) {
				if (item.art.getId().equals(artikel.getId()))
					return bestellung;
			}
		}
		return null;
	}
	
	public static class BestellungDateComparator implements Comparator<Bestellung> {
		private TimeTool t1 = new TimeTool();
		private TimeTool t2 = new TimeTool();
		
		@Override
		public int compare(Bestellung b1, Bestellung b2){
			setTimeTool((Bestellung) b1, t1);
			setTimeTool((Bestellung) b2, t2);
			if (t1.after(t2))
				return -1;
			if (t2.after(t1))
				return 1;
			return 0;
		}
		
		private void setTimeTool(Bestellung bestellung, TimeTool timeTool){
			try {
				String[] i = bestellung.getId().split(":"); //$NON-NLS-1$
				timeTool.set(i[1]);
			} catch (Exception e) {
				timeTool.set("1.1.1970");
			}
		}
		
	}
}
