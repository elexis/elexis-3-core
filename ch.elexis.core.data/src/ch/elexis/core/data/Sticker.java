/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.elexis.core.datatypes.ISticker;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Eine Markierung für im Prinzip beliebige Objekte. Ein Objekt, das eine Etikette hat, kann diese
 * Etikette zur Darstellung verwenden
 * 
 * @author gerry
 * 
 */
public class Sticker extends PersistentObject implements Comparable<ISticker>, ISticker {
	public static final String NAME = "Name";
	public static final String TABLENAME = "ETIKETTEN";
	public static final String LINKTABLE = "ETIKETTEN_OBJECT_LINK";
	public static final String CLASSLINK = "ETIKETTEN_OBJCLASS_LINK";
	static final HashMap<Class<?>, List<Sticker>> cache = new HashMap<Class<?>, List<Sticker>>();
	
	static {
		addMapping(TABLENAME, DATE_COMPOUND, "BildID=Image", "vg=foreground", "bg=background",
			NAME, "wert=importance"
		
		);
	}
	
	public Sticker(String name){
		create(null);
		set(NAME, name);
	}
	
	@Override
	public String getLabel(){
		return get(NAME);
	}
	
	public int getWert(){
		return checkZero(get("wert"));
	}
	
	public void setWert(int w){
		set("wert", Integer.toString(w));
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	@Override
	public boolean delete(){
		StringBuilder sb = new StringBuilder();
		Stm stm = getConnection().getStatement();
		
		sb.append("DELETE FROM ").append(Sticker.LINKTABLE).append(" WHERE ")
			.append("etikette = '").append(getId()).append("'");
		stm.exec(sb.toString());
		getConnection().releaseStatement(stm);
		return super.delete();
	}
	
	private static String insertStickerClassString = "INSERT INTO " + CLASSLINK
		+ " (objclass,sticker) VALUES (?,?);";
	private static PreparedStatement insertStickerClass = null;
	
	public void setClassForSticker(Class clazz){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + CLASSLINK + " (objclass,sticker) VALUES (")
			.append(JdbcLink.wrap(clazz.getName())).append(",").append(getWrappedId()).append(");");
		getConnection().exec(sb.toString());
		
	}
	
	public void removeClassForSticker(Class<?> clazz){
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM " + CLASSLINK + " WHERE objclass=")
			.append(JdbcLink.wrap(clazz.getName())).append(" AND sticker=").append(getWrappedId());
		getConnection().exec(sb.toString());
	}
	
	private static String queryClassStickerString = "SELECT objclass FROM " + Sticker.CLASSLINK
		+ " WHERE sticker=?";
	private static PreparedStatement queryClasses = null;
	
	public List<String> getClassesForSticker(){
		ArrayList<String> ret = new ArrayList<String>();
		if (queryClasses == null) {
			queryClasses = getConnection().prepareStatement(queryClassStickerString);
		}
		
		try {
			queryClasses.setString(1, getId());
			ResultSet res = queryClasses.executeQuery();
			while (res != null && res.next()) {
				ret.add(res.getString(1));
			}
			res.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ret;
		}
		return ret;
		
	}
	
	private static String queryStickerClassString = "SELECT sticker FROM " + Sticker.CLASSLINK
		+ " WHERE objclass=?";
	private static PreparedStatement queryStickers = null;
	
	/**
	 * Find all Stickers applicable for a given class
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Sticker> getStickersForClass(Class<?> clazz){
		List<Sticker> ret = cache.get(clazz);
		if (ret != null) {
			return ret;
		}
		HashSet<Sticker> uniqueRet = new HashSet<Sticker>();
		if (queryStickers == null) {
			queryStickers = getConnection().prepareStatement(queryStickerClassString);
		}
		
		try {
			queryStickers.setString(1, clazz.getName());
			ResultSet res = queryStickers.executeQuery();
			while (res != null && res.next()) {
				Sticker et = Sticker.load(res.getString(1));
				if (et != null && et.exists()) {
					uniqueRet.add(Sticker.load(res.getString(1)));
				}
			}
			res.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return ret;
		}
		cache.put(clazz, new ArrayList<Sticker>(uniqueRet));
		return new ArrayList<Sticker>(uniqueRet);
	}
	
	public static Sticker load(String id){
		Sticker ret = new Sticker(id);
		if (!ret.exists()) {
			return null;
		}
		return ret;
	}
	
	protected Sticker(String id, String name){
		super(id);
	}
	
	protected Sticker(){}
	
	public int compareTo(ISticker o){
		if (o != null) {
			return o.getWert() - getWert();
		}
		return 1;
	}
	
	@Override
	public boolean getVisibility(){
		if (getWert() >= 0)
			return true;
		else
			return false;
	}
	
	@Override
	public void setVisibility(boolean visibility){
		if (getVisibility() == visibility)
			return;
		setWert((visibility == true) ? 0 : -1);
	}
}
