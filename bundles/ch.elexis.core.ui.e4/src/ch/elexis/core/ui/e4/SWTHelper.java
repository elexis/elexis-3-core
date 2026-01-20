package ch.elexis.core.ui.e4;

import org.eclipse.swt.layout.GridData;

public class SWTHelper {

	/**
	 * Shortcut for getFillGridData(1,true,1,true);
	 *
	 * @return
	 */
	public static GridData getFillGridData() {
		return getFillGridData(1, true, 1, true);
	}

	/**
	 * Ein GridData-Objekt erzeugen, das den horizontalen und/oder vertikalen
	 * Freiraum ausfüllt.
	 *
	 * @param horizontal true, wenn horizontal gefüllt werden soll
	 * @param vertical   true, wenn vertikal gefüllt werden soll.
	 * @return ein neu erzeugtes, direkt verwendbares GridData-Objekt
	 */
	public static GridData getFillGridData(final int hSpan, final boolean hFill, final int vSpan, final boolean vFill) {
		int ld = 0;
		if (hFill) {
			ld = GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL;
		}
		if (vFill) {
			ld |= GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL;
		}
		GridData ret = new GridData(ld);
		ret.horizontalSpan = (hSpan < 1) ? 1 : hSpan;
		ret.verticalSpan = vSpan < 1 ? 1 : vSpan;
		return ret;
	}

	public static boolean askYesNo(String invoice_System, String format) {
		// TODO Auto-generated method stub
		return false;
	}

	public static void alert(String invoice_System_cannot_be_changed,
			String fallDetailBlatt2_CantChangeBillingSystemBody) {
		// TODO Auto-generated method stub

	}

}
