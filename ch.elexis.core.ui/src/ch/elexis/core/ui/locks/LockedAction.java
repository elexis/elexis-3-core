package ch.elexis.core.ui.locks;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.PersistentObject;

public abstract class LockedAction<T extends PersistentObject> extends RestrictedAction {

	private T object;

	public LockedAction(String text) {
		super(null, text);
		setEnabled(false);
	}

	@Override
	public void reflectRight() {
		object = getTargetedObject();

		if (object == null) {
			return;
		}

		setEnabled(CoreHub.ls.ownsLock(object.storeToString()));
	}

	public void doRun() {
		if (CoreHub.ls.ownsLock(object.storeToString())) {
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
