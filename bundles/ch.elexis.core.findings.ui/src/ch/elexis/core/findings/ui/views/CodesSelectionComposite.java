package ch.elexis.core.findings.ui.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.ui.UiDesk;

public class CodesSelectionComposite extends Composite implements ISelectionProvider {

	private List<CodeSelectionAction> currentSelection;

	private ListenerList<ISelectionChangedListener> selectionChangedListeners;

	private ToolBarManager manager;

	private List<ICoding> visibleCodings;

	public CodesSelectionComposite(Composite parent, int style) {
		super(parent, style);
		currentSelection = new ArrayList<>();
		selectionChangedListeners = new ListenerList<>();

		createContent();
	}

	private void createContent() {
		setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		setLayout(new FillLayout());

		manager = new ToolBarManager(SWT.WRAP);
		manager.createControl(this);
		refresh();
	}

	public void refresh() {
		updateVisibleCodings();
		updateSelectedCodings();
	}

	private void updateVisibleCodings() {
		visibleCodings = FindingsUiUtil.loadVisibleCodings();
		if (visibleCodings.isEmpty()) {
			visibleCodings = FindingsUiUtil.getAvailableCodings();
		}
		visibleCodings.sort(new Comparator<ICoding>() {
			@Override
			public int compare(ICoding arg0, ICoding arg1) {
				if (arg0 instanceof ILocalCoding && arg1 instanceof ILocalCoding) {
					ILocalCoding left = (ILocalCoding) arg0;
					ILocalCoding right = (ILocalCoding) arg1;
					return Integer.valueOf(left.getPrio()).compareTo(Integer.valueOf(right.getPrio()));
				}
				return 0;
			}
		});
		manager.removeAll();
		for (ICoding iCoding : visibleCodings) {
			manager.add(new CodeSelectionAction(iCoding));
		}
		manager.add(new AllSelectionAction());
		manager.update(true);
		this.getParent().layout(true);
	}

	private void updateSelectedCodings() {
		currentSelection.clear();
		List<ICoding> selectedCodings = FindingsUiUtil.loadSelectedCodings();
		if (selectedCodings != null) {
			IContributionItem[] items = manager.getItems();
			for (ICoding iCoding : selectedCodings) {
				for (IContributionItem iContributionItem : items) {
					if (iContributionItem instanceof ActionContributionItem) {
						IAction action = ((ActionContributionItem) iContributionItem).getAction();
						if (action instanceof CodeSelectionAction) {
							CodeSelectionAction csAction = (CodeSelectionAction) action;
							if (csAction.getiCoding().getCode().equals(iCoding.getCode())) {
								csAction.setChecked(true);
								currentSelection.add(csAction);
							}
						}
					}
				}
			}
			fireSelectionChanged();
			manager.update(true);
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(
				currentSelection.stream().map(action -> action.getiCoding()).collect(Collectors.toList()));
	}

	@Override
	public void setSelection(ISelection selection) {
		// ignore until needed
	}

	private void fireSelectionChanged() {
		ISelection selection = getSelection();
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			listener.selectionChanged(event);
		}
	}

	private class CodeSelectionAction extends Action {

		private ICoding iCoding;

		public CodeSelectionAction(ICoding iCoding) {
			super(iCoding.getDisplay(), Action.AS_CHECK_BOX);
			this.iCoding = iCoding;
		}

		public ICoding getiCoding() {
			return iCoding;
		}

		@Override
		public void run() {
			if (currentSelection.contains(this)) {
				currentSelection.remove(this);
			} else {
				currentSelection.add(this);
			}
			fireSelectionChanged();
			manager.update(true);
			FindingsUiUtil.saveSelectedCodings(
					currentSelection.stream().map(a -> a.getiCoding()).collect(Collectors.toList()));
		}
	}

	private class AllSelectionAction extends Action {

		public AllSelectionAction() {
			super("Alle", Action.AS_PUSH_BUTTON);
		}

		@Override
		public void run() {
			int currentSelectionSize = currentSelection.size();
			currentSelection.clear();
			IContributionItem[] items = manager.getItems();
			// clear if all items (exclude all item) are already selected
			if (currentSelectionSize == (items.length - 1)) {
				for (IContributionItem iContributionItem : items) {
					if (iContributionItem instanceof ActionContributionItem) {
						IAction action = ((ActionContributionItem) iContributionItem).getAction();
						if (action instanceof CodeSelectionAction) {
							CodeSelectionAction csAction = (CodeSelectionAction) action;
							csAction.setChecked(false);
						}
					}
				}
			} else {
				for (IContributionItem iContributionItem : items) {
					if (iContributionItem instanceof ActionContributionItem) {
						IAction action = ((ActionContributionItem) iContributionItem).getAction();
						if (action instanceof CodeSelectionAction) {
							CodeSelectionAction csAction = (CodeSelectionAction) action;
							csAction.setChecked(true);
							currentSelection.add(csAction);
						}
					}
				}
			}
			fireSelectionChanged();
			manager.update(true);

			FindingsUiUtil.saveSelectedCodings(
					currentSelection.stream().map(a -> a.getiCoding()).collect(Collectors.toList()));
		}
	}

	public void selectAllFilters() {
		int currentSelectionSize = currentSelection.size();
		currentSelection.clear();
		IContributionItem[] items = manager.getItems();
		// clear if all items (exclude "Alle"-Item) are already selected
		if (currentSelectionSize == (items.length - 1)) {
			for (IContributionItem iContributionItem : items) {
				if (iContributionItem instanceof ActionContributionItem) {
					IAction action = ((ActionContributionItem) iContributionItem).getAction();
					if (action instanceof CodeSelectionAction) {
						CodeSelectionAction csAction = (CodeSelectionAction) action;
						csAction.setChecked(false);
					}
				}
			}
		} else {
			for (IContributionItem iContributionItem : items) {
				if (iContributionItem instanceof ActionContributionItem) {
					IAction action = ((ActionContributionItem) iContributionItem).getAction();
					if (action instanceof CodeSelectionAction) {
						CodeSelectionAction csAction = (CodeSelectionAction) action;
						csAction.setChecked(true);
						currentSelection.add(csAction);
					}
				}
			}
		}
		fireSelectionChanged();
		manager.update(true);
		FindingsUiUtil
				.saveSelectedCodings(currentSelection.stream().map(a -> a.getiCoding()).collect(Collectors.toList()));
	}

	public boolean isAnyFilterDeselected() {
		IContributionItem[] items = manager.getItems();
		for (IContributionItem item : items) {
			if (item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) item).getAction();
				if (action instanceof CodeSelectionAction && !action.isChecked()) {
					return true;
				}
			}
		}
		return false;
	}

}
