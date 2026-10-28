package ch.elexis.core.findings.ui.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jface.action.Action;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.util.FindingSortOrder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;

public class MoveFindingAction<T extends IFinding> extends Action {

	private List<T> findings;
	private T finding;
	private int direction;
	private Consumer<List<T>> onMoved;

	public MoveFindingAction(List<T> findings, T finding, int direction, Consumer<List<T>> onMoved) {
		super(direction < 0 ? Messages.MoveFindingAction_MoveUp : Messages.MoveFindingAction_MoveDown,
				(direction < 0 ? Images.IMG_ARROWUP : Images.IMG_ARROWDOWN).getImageDescriptor());
		this.findings = findings;
		this.finding = finding;
		this.direction = direction;
		this.onMoved = onMoved;
	}

	@Override
	public void run() {
		List<T> moved = new ArrayList<>(findings);
		int index = moved.indexOf(finding);
		Collections.swap(moved, index, index + direction);
		lockAndMove(moved, 0);
	}

	private void lockAndMove(List<T> moved, int index) {
		if (index == moved.size()) {
			for (int i = 0; i < moved.size(); i++) {
				FindingSortOrder.setSortOrder(moved.get(i), i);
				FindingsServiceComponent.getService().saveFinding(moved.get(i));
			}
			onMoved.accept(moved);
			return;
		}
		AcquireLockUi.aquireAndRun(moved.get(index), new ILockHandler() {
			@Override
			public void lockFailed() {
			}

			@Override
			public void lockAcquired() {
				lockAndMove(moved, index + 1);
			}
		});
	}
}
