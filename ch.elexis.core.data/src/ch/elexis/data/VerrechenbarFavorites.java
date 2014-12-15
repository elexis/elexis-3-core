package ch.elexis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.elexis.core.data.activator.CoreHub;

public class VerrechenbarFavorites {
	
	public static final String USER_CFG_FAVORITES = "verrechenbar/favoriten";
	
	private static List<Favorite> favorites;
	
	/**
	 * 
	 * @return the favorite {@link VerrechenbarAdapter} elements of THIS user, in a pre-ordered list
	 */
	public static List<Favorite> getFavorites(){
		if(favorites == null) {
			favorites = new ArrayList<VerrechenbarFavorites.Favorite>();
			
			String favs = CoreHub.userCfg.get(USER_CFG_FAVORITES, "");
			if(favs.contains(";")) {
				String[] entries = favs.split(";");
				for (int i = 0; i < entries.length; i++) {
					String entry = entries[i];
					if(entry.contains(",")) {
						String[] value = entry.split(",");
						Favorite f = new Favorite(value[1], value[0], i);
						favorites.add(f);
					}
				}
			}
			
			Collections.sort(favorites);
		}	
		
		return favorites;
	}
	
	public static void storeFavorites() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < favorites.size(); i++) {
			Favorite f = favorites.get(i);
			sb.append(f.macroString+","+f.storeToString);
			if(i!=favorites.size()-1) sb.append(";");
		}
		CoreHub.userCfg.set(USER_CFG_FAVORITES, sb.toString());
		CoreHub.userCfg.flush();
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
			return macroString;
		}
		
		public void setMacroString(String macroString){
			this.macroString = macroString;
		}
		
		public int getOrder(){
			return order;
		}
		
		public void setOrder(int order){
			this.order = order;
		}
	}
}
