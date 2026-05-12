package ch.elexis.core.model.util;

import java.util.Optional;

import ch.rgw.tools.Money;

public class ModelUtil {

	/**
	 * If string is parse able as {@link Integer}, the value is interpreted as
	 * cents.
	 *
	 * @param string
	 * @return
	 */
	public static Optional<Money> getMoneyForCentString(String string) {
		try {
			int cent = Integer.parseInt(string != null ? string.trim() : null);
			return Optional.of(new Money(cent));
		} catch (NumberFormatException e) {
			// ignore
		}
		return Optional.empty();
	}

	/**
	 * If string is parse able as {@link Double}, a Money representation of it is
	 * created.
	 *
	 * @param string
	 * @return
	 */
	public static Optional<Money> getMoneyForPriceString(String string) {
		try {
			double amount = Double.parseDouble(string != null ? string.trim() : null);
			return Optional.of(new Money(amount));
		} catch (NumberFormatException e) {
			// ignore
		}
		return Optional.empty();
	}

}
