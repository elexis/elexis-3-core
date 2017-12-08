package ch.elexis.core.ui.locks;

/**
 * Mixin interface. The object implementing it supports to unlock the modification
 * of contained attributes. This can be used to unblock editing elements for which
 * a data-lock is available.
 *
 */
public interface IUnlockable {

	/**
	 * 
	 * @param unlocked
	 *            if <b><code>true</code></b> sets the respective implementation to
	 *            <b>ALLOW EDITING</b> its elements (i.e. it is unlocked). If <code>false</code> denies
	 *            editing its attributes.
	 */
	public void setUnlocked(boolean unlocked);

}
