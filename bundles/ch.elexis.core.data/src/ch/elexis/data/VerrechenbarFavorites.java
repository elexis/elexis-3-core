package ch.elexis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;

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
		ElexisEventDispatcher.reload(Favorite.class);
	}
	
	/**
	 * Add or remove the object to or from the {@link Favorite}s of the user. The
	 * {@link IStoreToStringService} must be able to create a store to string from the object.
	 * 
	 * @param billable
	 * @param val
	 */
	public static void setFavorite(Object object, boolean val){
		Favorite fav = VerrechenbarFavorites.isFavorite(object);
		if (val) {
			if (fav != null)
				return;
			String storeToString = StoreToStringServiceHolder.getStoreToString(object);
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
	 * Test if the object is a {@link Favorite} of the user.
	 * 
	 * @param billable
	 * @return
	 */
	public static Favorite isFavorite(Object object){
		for (Favorite favorite : getFavorites()) {
			String comparator = StoreToStringServiceHolder.getStoreToString(object);
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
				ICodeElementBlock block = (ICodeElementBlock) getObject().orElse(null);
				return (block != null) ? block.getMacro() : macroString;
			}
			return macroString;
		}
		
		public void setMacroString(String macroString){
			if (storeToString.startsWith(Leistungsblock.class.getName())) {
				ICodeElementBlock block = (ICodeElementBlock) getObject().orElse(null);
				if (block != null) {
					block.setMacro(macroString);
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
		 * Get the {@link Identifiable} referenced by this {@link Favorite} using an
		 * {@link IStoreToStringService}.
		 * 
		 * @return
		 */
		public Optional<Identifiable> getObject(){
			return StoreToStringServiceHolder.get().loadFromString(storeToString);
		}
	}
}
