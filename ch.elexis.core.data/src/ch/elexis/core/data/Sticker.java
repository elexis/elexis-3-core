/*******************************************************************************
 * Copyright (c) 2008-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  MEDEVIT <office@medevit.at> - major changes in 3.0
 *******************************************************************************/
package ch.elexis.core.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ch.elexis.core.model.ISticker;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * Eine Markierung für im Prinzip beliebige Objekte. Ein Objekt, das eine
 * Etikette hat, kann diese Etikette zur Darstellung verwenden
 * 
 * @since 3.0.0 - division between core and Ui, see UiSticker class
 */
public class Sticker extends PersistentObject implements 
		ISticker {
	public static final String NAME = "Name";
	public static final String TABLENAME = "ETIKETTEN";
	public static final String FLD_LINKTABLE = "ETIKETTEN_OBJECT_LINK";
	public static final String FLD_CLASSLINK = "ETIKETTEN_OBJCLASS_LINK";
	public static final String FLD_BACKGROUND = "bg";
	public static final String FLD_FOREGROUND = "vg";
	public static final String FLD_IMAGE_ID = "BildID";
	public static final String FLD_VALUE = "wert";
	
	private static final String RGB_BLACK = "000000";
	private static final String RGB_WHITE = "FFFFFF";

	static final HashMap<Class<?>, List<Sticker>> cache = new HashMap<Class<?>, List<Sticker>>();

	static {
		addMapping(TABLENAME, DATE_COMPOUND, FLD_IMAGE_ID + "=Image",
				FLD_FOREGROUND + "=foreground", FLD_BACKGROUND + "=background",
				NAME, FLD_VALUE + "=importance");
	}

	public Sticker(String name, String fg, String bg){
		create(null);
		if (fg == null) {
			fg = RGB_BLACK;
		}
		if (bg == null) {
			bg = RGB_WHITE;
		}
		set(new String[] {
			NAME, FLD_FOREGROUND, FLD_BACKGROUND
		}, new String[] {
			name, fg, bg
		});
	}

	public static Sticker load(String id) {
		Sticker ret = new Sticker(id);
		if (!ret.exists()) {
			return null;
		}
		return ret;
	}

	protected Sticker(String id) {
		super(id);
	}

	protected Sticker() {
	}

	@Override
	public String getLabel() {
		return get(NAME);
	}

	public int getWert() {
		return checkZero(get(FLD_VALUE));
	}

	public void setWert(int w) {
		set(FLD_VALUE, Integer.toString(w));
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	@Override
	public boolean delete() {
		StringBuilder sb = new StringBuilder();
		Stm stm = getConnection().getStatement();

		sb.append("DELETE FROM ").append(Sticker.FLD_LINKTABLE)
				.append(" WHERE ").append("etikette = '").append(getId())
				.append("'");
		stm.exec(sb.toString());
		getConnection().releaseStatement(stm);
		return super.delete();
	}

	public void setClassForSticker(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append(
				"INSERT INTO " + FLD_CLASSLINK + " (objclass,sticker) VALUES (")
				.append(JdbcLink.wrap(clazz.getName())).append(",")
				.append(getWrappedId()).append(");");
		getConnection().exec(sb.toString());

	}

	public void removeClassForSticker(Class<?> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM " + FLD_CLASSLINK + " WHERE objclass=")
				.append(JdbcLink.wrap(clazz.getName())).append(" AND sticker=")
				.append(getWrappedId());
		getConnection().exec(sb.toString());
	}

	private static String queryClassStickerString = "SELECT objclass FROM "
			+ Sticker.FLD_CLASSLINK + " WHERE sticker=?";
	private static PreparedStatement queryClasses = null;

	public List<String> getClassesForSticker() {
		ArrayList<String> ret = new ArrayList<String>();
		if (queryClasses == null) {
			queryClasses = getConnection().prepareStatement(
					queryClassStickerString);
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

	private static String queryStickerClassString = "SELECT sticker FROM "
			+ Sticker.FLD_CLASSLINK + " WHERE objclass=?";
	private static PreparedStatement queryStickers = null;

	/**
	 * Find all Stickers applicable for a given class
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Sticker> getStickersForClass(Class<?> clazz) {
		List<Sticker> ret = cache.get(clazz);
		if (ret != null) {
			return ret;
		}
		HashSet<Sticker> uniqueRet = new HashSet<Sticker>();
		if (queryStickers == null) {
			queryStickers = getConnection().prepareStatement(
					queryStickerClassString);
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

	public int compareTo(ISticker o) {
		if (o != null) {
			return o.getWert() - getWert();
		}
		return 1;
	}

	public void setBackground(String bg) {
		set(FLD_BACKGROUND, bg);
	}

	public void setForeground(String fg) {
		set(FLD_FOREGROUND, fg);
	}

	@Override
	public String getBackground() {
		return get(FLD_BACKGROUND);
	}

	@Override
	public String getForeground() {
		return get(FLD_FOREGROUND);
	}

	@Override
	public boolean isVisible() {
		if (getWert() >= 0)
			return true;
		else
			return false;
	}

	@Override
	public void setVisible(boolean value) {
		// TODO Auto-generated method stub
		
	}

}
