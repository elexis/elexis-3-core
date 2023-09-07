package ch.elexis.core.ui.locks;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.actions.RestrictedAction;

abstract public class LockedRestrictedAction<T> extends RestrictedAction {

	private T object;

	public LockedRestrictedAction(EvaluatableACE requiredACE, String text) {
		super(requiredACE, text);
		setEnabled(false);
	}

	@Override
	public void reflectRight() {
		setEnabled(false);

		boolean rights = AccessControlServiceHolder.get().evaluate(evaluatableAce);
		if (!rights) {
			return;
		}

		object = getTargetedObject();

		if (object == null) {
			return;
		}

		setEnabled(LocalLockServiceHolder.get().isLocked(object));
	}

	@Override
	public void doRun() {
		if (LocalLockServiceHolder.get().isLocked(object)
				&& AccessControlServiceHolder.get().evaluate(evaluatableAce)) {
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
