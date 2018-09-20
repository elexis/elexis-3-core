/*******************************************************************************
 * Copyright (c) 2006-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - orders are now persisted into own table 
 *    MEDEVIT - major refactoring to fit multi stock changes
 *******************************************************************************/

package ch.elexis.data;

import java.util.Comparator;
import java.util.List;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IOrder;
import ch.elexis.core.data.interfaces.IOrderEntry;
import ch.elexis.core.data.interfaces.IStock;
import ch.elexis.core.jdt.Nullable;
import ch.rgw.tools.TimeTool;

public class Bestellung extends PersistentObject implements IOrder {
	
	public static final String TABLENAME = "BESTELLUNGEN";
	
	public static final String FLD_DATE = "DATUM";
	public static final String FLD_JOINT_BESTELLUNGEN_ENTRIES = "BESTELLUNGEN_ENTRIES";
	
	public enum ListenTyp {
			PHARMACODE, NAME, VOLL
	};
	
	static {
		addMapping(TABLENAME,
			FLD_JOINT_BESTELLUNGEN_ENTRIES + "=LIST:BESTELLUNG:" + BestellungEntry.TABLENAME);
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	public static Bestellung load(String id){
		return new Bestellung(id);
	}
	
	protected Bestellung(){}
	
	protected Bestellung(String id){
		super(id);
	}
	
	public Bestellung(String name, Anwender an){
		TimeTool t = new TimeTool();
		create(name + StringConstants.COLON + t.toString(TimeTool.TIMESTAMP) + StringConstants.COLON
			+ an.getId());
	}
	
	@Override
	public String getLabel(){
		String[] i = getId().split(StringConstants.COLON);
		TimeTool t = new TimeTool(i[1]);
		return i[0] + ": " + t.toString(TimeTool.FULL_GER); //$NON-NLS-1$
	}
	
	@Override
	public IOrderEntry addEntry(Object article, IStock stock, Object provider, int num){
		return addBestellungEntry(((Artikel) article), ((Stock) stock), ((Kontakt) provider), num);
	}
	
	public BestellungEntry addBestellungEntry(Artikel article, Stock stock, Kontakt provider,
		int num){
		
		if (provider == null) {
			String providerId =
				CoreHub.globalCfg.get(Preferences.INVENTORY_DEFAULT_ARTICLE_PROVIDER, null);
			if (providerId != null) {
				Kontakt defProvider = Kontakt.load(providerId);
				if (defProvider.exists()) {
					provider = defProvider;
				}
			}
		}
		
		BestellungEntry i = findBestellungEntry(stock, article);
		if (i != null) {
			int count = i.getCount();
			count = count += num;
			i.setCount(count);
		} else {
			i = new BestellungEntry(this, article, stock, provider, num);
		}
		return i;
	}
	
	public @Nullable BestellungEntry findBestellungEntry(@Nullable Stock stock, Artikel article){
		Query<BestellungEntry> qbe = new Query<BestellungEntry>(BestellungEntry.class);
		qbe.add(BestellungEntry.FLD_BESTELLUNG, Query.EQUALS, getId());
		qbe.add(BestellungEntry.FLD_ARTICLE_ID, Query.EQUALS, article.getId());
		qbe.add(BestellungEntry.FLD_ARTICLE_TYPE, Query.EQUALS, article.getClass().getName());
		if (stock != null) {
			qbe.add(BestellungEntry.FLD_STOCK, Query.EQUALS, stock.getId());
		}
		
		List<BestellungEntry> result = qbe.execute();
		if (result.size() > 0) {
			return result.get(0);
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
				String[] i = bestellung.getId().split(StringConstants.COLON);
				timeTool.set(i[1]);
			} catch (Exception e) {
				timeTool.set("1.1.1970");
			}
		}
		
	}
	
	public List<BestellungEntry> getEntries(){
		List<BestellungEntry> execute = new Query<BestellungEntry>(BestellungEntry.class,
			BestellungEntry.FLD_BESTELLUNG, getId()).execute();
		return execute;
	}
	
	public static void markAsOrdered(List<BestellungEntry> list){
		for (BestellungEntry item : list) {
			item.setState(BestellungEntry.STATE_ORDERED);
		}
	}
	
	public void removeEntry(BestellungEntry entry){
		entry.removeFromDatabase();
	}
	
	public boolean isDone(){
		List<BestellungEntry> entries = getEntries();
		for (BestellungEntry bestellungEntry : entries) {
			if (bestellungEntry.getState() != BestellungEntry.STATE_DONE) {
				return false;
			}
		}
		return true;
	}
	
	public TimeTool getTime() {
		TimeTool ret = new TimeTool();
		try {
			String[] i = getId().split(":"); //$NON-NLS-1$
			ret.set(i[1]);
		} catch (Exception e) {
			ret.set("1.1.1970");
		}
		return ret;
	}
}
