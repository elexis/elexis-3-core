package ch.elexis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IPersistentObject;

public class VerrechenbarFavorites {
	
	public static final String USER_CFG_FAVORITES = "verrechenbar/favoriten";
	private static List<Favorite> favorites;
	private static Logger log = LoggerFactory.getLogger(VerrechenbarFavorites.class);
	
	private static ElexisEventListenerImpl eeli_pat =
		new ElexisEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			
			public void run(ElexisEvent ev){
				log.debug("User changed, nulling favorites.");
				favorites = null;
			};
		};
		
	static {
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
	}
	
	/**
	 * 
	 * @return the favorite {@link VerrechenbarAdapter} elements of THIS user, in a pre-ordered list
	 */
	public static List<Favorite> getFavorites(){
		if (favorites == null) {
			favorites = new ArrayList<VerrechenbarFavorites.Favorite>();
			
			String favs = CoreHub.userCfg.get(USER_CFG_FAVORITES, "");
			String[] entries = favs.split(";");
			for (int i = 0; i < entries.length; i++) {
				String entry = entries[i];
				if (entry.contains(",")) {
					String[] value = entry.split(",");
					Favorite f = new Favorite(value[1], value[0], i);
					favorites.add(f);
				}
			}
			
			Collections.sort(favorites);
		}
		
		return favorites;
	}
	
	public static void storeFavorites(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < favorites.size(); i++) {
			Favorite f = favorites.get(i);
			sb.append(f.macroString + "," + f.storeToString);
			if (i != favorites.size() - 1)
				sb.append(";");
		}
		CoreHub.userCfg.set(USER_CFG_FAVORITES, sb.toString());
		CoreHub.userCfg.flush();
	}
	
	/**
	 * @param val
	 *            toggle element as favorite of the user
	 * @since 3.1
	 */
	public static void setFavorite(IPersistentObject po, boolean val){
		Favorite fav = VerrechenbarFavorites.isFavorite(po);
		if (val) {
			if (fav != null)
				return;
			String storeToString;
			if (po instanceof Leistungsblock) {
				storeToString =
					Leistungsblock.class.getName() + StringConstants.DOUBLECOLON + po.getId();
			} else {
				storeToString = po.storeToString();
			}
			VerrechenbarFavorites.getFavorites().add(new Favorite(storeToString, "", 0));
		} else {
			if (fav == null)
				return;
			VerrechenbarFavorites.getFavorites().remove(fav);
		}
		
		// TODO reload favorites view
		VerrechenbarFavorites.storeFavorites();
	}
	
	/**
	 * 
	 * @return the {@link Favorite} if a favorite {@link VerrechenbarAdapter} of this user, else
	 *         null
	 */
	public static Favorite isFavorite(IPersistentObject po){
		for (Favorite favorite : getFavorites()) {
			String comparator = "";
			if (po instanceof Leistungsblock) {
				comparator =
					Leistungsblock.class.getName() + StringConstants.DOUBLECOLON + po.getId();
			} else {
				comparator = po.storeToString();
			}
			if (comparator.equalsIgnoreCase(favorite.storeToString)) {
				return favorite;
			}
		}
		return null;
	}
	
	/**
	 * Find the favorite for a provided macro by current user
	 * 
	 * @return
	 * @since 3.1
	 */
	public static @Nullable Favorite findFavoritByMacroForCurrentUser(@Nullable String macro){
		if (macro == null)
			return null;
		for (Favorite favorite : getFavorites()) {
			if (macro.equals(favorite.macroString))
				return favorite;
		}
		return null;
	}
	
	public static class Favorite implements Comparable<Favorite> {
		
		String storeToString;
		String macroString;
		int order;
		
		private PersistentObjectFactory pof = new PersistentObjectFactory();
		
		public Favorite(String storeToString, String macroString, int order){
			this.storeToString = storeToString;
			this.macroString = macroString;
			this.order = order;
		}
		
		@Override
		public int compareTo(Favorite o){
			return Integer.compare(order, o.order);
		}
		
		public String getStoreToString(){
			return storeToString;
		}
		
		public void setStoreToString(String storeToString){
			this.storeToString = storeToString;
		}
		
		public String getMacroString(){
			if (storeToString.startsWith(Leistungsblock.class.getName())) {
				Leistungsblock po = (Leistungsblock) getPersistentObject();
				return (po != null) ? po.getMacro() : macroString;
			}
			return macroString;
		}
		
		public void setMacroString(String macroString){
			if (storeToString.startsWith(Leistungsblock.class.getName())) {
				Leistungsblock po = (Leistungsblock) getPersistentObject();
				if (po != null) {
					po.setMacro(macroString);
				} else {
					log.warn("Could not set macroString " + macroString
						+ " to Leistungsblock  as po is null.");
				}
			}
			this.macroString = macroString;
		}
		
		public int getOrder(){
			return order;
		}
		
		public void setOrder(int order){
			this.order = order;
		}
		
		/**
		 * @return the {@link IPersistentObject} as resolved via {@link #storeToString},
		 *         <code>null</code> if erroneous
		 */
		public @Nullable IPersistentObject getPersistentObject(){
			return pof.createFromString(storeToString);
		}
	}
}
