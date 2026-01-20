package ch.elexis.core.ui.e4.user;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.services.holder.ConfigServiceHolder;

public class UserCasePreferences {

	public static final String MENUSEPARATOR = "------------------------------------"; //$NON-NLS-1$
	private static final String PREFSDELIMITER_REGEX = "\\`\\^"; //$NON-NLS-1$
	
	public static int getBillingSystemsMenuSeparatorPos(String[] input) {
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = ConfigServiceHolder.getUser(Preferences.USR_TOPITEMSSORTING, StringUtils.EMPTY);
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList = new LinkedList<>(Arrays.asList(topItemsSorting));
		if ((!lTopItemsLinkedList.isEmpty()) && (!lTopItemsLinkedList.get(0).equalsIgnoreCase(StringUtils.EMPTY)))
			return lTopItemsLinkedList.size();
		else
			return -1;

	}

	/**
	 * sort the input: start with sorting as specified in topItemsSorting found in
	 * the user prefs, add a separator and then sort the rest alphabetically
	 *
	 * @param input String[] of all billing systems, unsorted
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input) {
		// read the sorting for this user form prefs, convert to LinkedList for editing
		String topItemsSortingStr = ConfigServiceHolder.getUser(Preferences.USR_TOPITEMSSORTING, StringUtils.EMPTY);
		String[] topItemsSorting = topItemsSortingStr.split(PREFSDELIMITER_REGEX);
		LinkedList<String> lTopItemsLinkedList = new LinkedList<>(Arrays.asList(topItemsSorting));
		return sortBillingSystems(input, lTopItemsLinkedList);
	}
	
	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting,
	 * add a separator if there are topitems and then sort the rest alphabetically
	 *
	 * @param input           String[] of all billing systems, unsorted
	 * @param topItemsSorting LinkedList<String> Array of billing systems in the
	 *                        order they should appear in the menu/combo
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting) {
		return sortBillingSystems(input, topItemsSorting, false);
	}

	/**
	 * sort the input: start with sorting as specified in parameter topItemsSorting,
	 * always add a separator and then sort the rest alphabetically
	 *
	 * @param input               String[] of all billing systems, unsorted
	 * @param topItemsSorting     LinkedList<String> Array of billing systems in the
	 *                            order they should appear in the menu/combo
	 * @param alwaysShowSeparator boolean should the separator also be shown when NO
	 *                            topItems are present
	 * @return String[] the sorted list
	 */
	public static String[] sortBillingSystems(String[] input, LinkedList<String> topItemsSorting,
			boolean alwaysShowSeparator) {
		// create a copy of topItemsSorting - we append the other items to the end later
		LinkedList<String> lTopItemsSorting = (LinkedList<String>) topItemsSorting.clone();

		// create sorted list for the other items
		SortedList<String> sortedList = new SortedList<>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return (o1.compareTo(o2));
			}
		});
		for (int i = 0; i < input.length; i++) {
			String item = input[i];
			if (!lTopItemsSorting.contains(item))
				sortedList.add(item);
		}

		// now append the sorted items to the copied top items
		if (alwaysShowSeparator
				|| ((!topItemsSorting.isEmpty()) && (!topItemsSorting.get(0).equalsIgnoreCase(StringUtils.EMPTY)))) {
			lTopItemsSorting.add(MENUSEPARATOR);
		}
		lTopItemsSorting.addAll(sortedList);
		lTopItemsSorting.remove(StringUtils.EMPTY);

		String[] output = new String[lTopItemsSorting.size()];
		lTopItemsSorting.toArray(output);

		return output;
	}
	
	public class SeparatorIBillingSystem implements IBillingSystem {

		@Override
		public String getName() {
			return UserCasePreferences.MENUSEPARATOR;
		}

		@Override
		public BillingLaw getLaw() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
