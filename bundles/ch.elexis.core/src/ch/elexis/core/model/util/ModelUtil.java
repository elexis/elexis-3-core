package ch.elexis.core.model.util;

import java.util.Optional;

import ch.rgw.tools.Money;

public class ModelUtil {
	
	/**
	 * If string is parse able as {@link Integer}, the value is interpreted as cents.
	 * 
	 * @param string
	 * @return
	 */
	public static Optional<Money> getMoneyForCentString(String string){
		try {
			int amount = Integer.parseInt(string);
			return Optional.of(new Money(amount));
		} catch (NumberFormatException e) {
			// ignore
		}
		return Optional.empty();
	}
	
}
