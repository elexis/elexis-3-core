package ch.elexis.core.ui.locks;

import org.eclipse.jface.action.Action;

import ch.elexis.core.data.service.LocalLockServiceHolder;

/**
 * Action will be active if lock LockService#isLocked on the targeted Object is
 * true.
 *
 * @author thomas
 *
 * @param <T>
 */
public abstract class LockedAction<T> extends Action {

	private T object;

	public LockedAction(String text) {
		super(text);
		setEnabled(false);
	}

	@Override
	public boolean isEnabled() {
		object = getTargetedObject();

		if (object == null) {
			return false;
		}

		return LocalLockServiceHolder.get().isLockedLocal(object);
	}

	@Override
	public void run() {
		if (LocalLockServiceHolder.get().isLockedLocal(object)) {
			if (object != null) {
				doRun((T) object);
			}
		}
	};

	public abstract T getTargetedObject();

	/**
	 *
	 * @param element not <code>null</code>, where the provided element was verified
	 *                according to the given rules
	 */
	public abstract void doRun(T element);

}
