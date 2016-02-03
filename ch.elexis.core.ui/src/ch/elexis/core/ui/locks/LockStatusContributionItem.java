package ch.elexis.core.ui.locks;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;

public class LockStatusContributionItem extends ContributionItem {

	private ElexisEventListener eeli_user = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev) {
			switch (ev.getType()) {
			case ElexisEvent.EVENT_LOCK_AQUIRED:
				item.setText("Lock auf " + ev.getObject().getLabel());
				item.setImage(Images.IMG_LOCK_CLOSED.getImage());
				break;
			case ElexisEvent.EVENT_LOCK_RELEASED:
				item.setText("Kein Patient gelocked.");
				item.setImage(Images.IMG_LOCK_OPEN.getImage());
				break;
			default:
				break;
			}
		};
	};

	private ToolItem item;

	public LockStatusContributionItem() {
		ElexisEventDispatcher.getInstance().addListeners(eeli_user);
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	public void fill(org.eclipse.swt.widgets.ToolBar parent, int index) {
		item = new ToolItem(parent, SWT.None);
		item.setText("Kein Patient gelocked.");
		item.setImage(Images.IMG_LOCK_OPEN.getImage());
	};

	@Override
	public void dispose() {
		ElexisEventDispatcher.getInstance().removeListeners(eeli_user);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}
