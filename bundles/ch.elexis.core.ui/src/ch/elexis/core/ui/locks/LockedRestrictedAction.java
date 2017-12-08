package ch.elexis.core.ui.locks;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.PersistentObject;

abstract public class LockedRestrictedAction<T extends PersistentObject> extends RestrictedAction {

	private T object;

	public LockedRestrictedAction(ACE requiredACE, String text) {
		super(requiredACE, text);
		setEnabled(false);
	}

	@Override
	public void reflectRight() {
		setEnabled(false);

		boolean rights = CoreHub.acl.request(necessaryRight);
		if (!rights) {
			return;
		}

		object = getTargetedObject();

		if (object == null) {
			return;
		}

		setEnabled(CoreHub.getLocalLockService().isLocked(object));
	}

	public void doRun() {
		if (CoreHub.getLocalLockService().isLocked(object) && CoreHub.acl.request(necessaryRight)) {
			if (object != null) {
				doRun((T) object);
			}
		}
	};

	public abstract T getTargetedObject();

	/**
	 * 
	 * @param element
	 *            not <code>null</code>, where the provided element was verified
	 *            according to the given rules
	 */
	public abstract void doRun(T element);
}
