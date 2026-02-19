package ch.elexis.core.ui.medication.views;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.medication.IMedicationInteractionUi;

public class MedicationTableViewerContentProvider implements IStructuredContentProvider {

	private List<IPrescription> input;

	private List<MedicationTableViewerItem> currentItems;

	private StructuredViewer viewer;

	private int pageSize;
	private int currentPageOffset;

	private IMedicationInteractionUi interactionUi;

	public MedicationTableViewerContentProvider(StructuredViewer viewer) {
		this.viewer = viewer;
		this.pageSize = 500;
		this.currentPageOffset = 0;
	}

	@Override
	public void dispose() {
		viewer = null;
		currentItems = null;
		input = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof List<?>) {
			currentPageOffset = 0;
			input = (List<IPrescription>) newInput;
			createCurrentItems();
		} else {
			currentPageOffset = 0;
			input = Collections.emptyList();
			currentItems = Collections.emptyList();
		}
	}

	private void createCurrentItems() {
		if (input.size() < pageSize) {
			currentItems = MedicationTableViewerItem.createFromPrescriptionList(input, viewer);
		} else {
			if (currentPageOffset + pageSize < input.size()) {
				currentItems = MedicationTableViewerItem.createFromPrescriptionList(
						input.subList(currentPageOffset, currentPageOffset + pageSize), viewer);
			} else {
				currentItems = MedicationTableViewerItem
						.createFromPrescriptionList(input.subList(currentPageOffset, input.size()), viewer);
			}
		}
		if (interactionUi != null) {
			interactionUi.setPrescriptions(input);
			currentItems.stream().forEach(i -> i.setInteractionUi(interactionUi));
		}
	}

	public void nextPage() {
		if ((currentPageOffset + pageSize) < input.size()) {
			currentPageOffset += pageSize;
			createCurrentItems();
		}
	}

	public boolean hasNext() {
		return input != null && (currentPageOffset + pageSize) < input.size();
	}

	public void previousPage() {
		if (currentPageOffset >= pageSize) {
			currentPageOffset -= pageSize;
			createCurrentItems();
		}
	}

	public boolean hasPrevious() {
		return input != null && currentPageOffset >= pageSize;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return currentItems.toArray();
	}

	public static class MedicationContentProviderComposite extends Composite {

		private Label currentState;
		private MedicationTableViewerContentProvider contentProvider;
		private ToolBarManager toolbarmgr;

		public MedicationContentProviderComposite(Composite parent, int style) {
			super(parent, style);
			setLayout(new RowLayout(SWT.HORIZONTAL));
			createContent();
		}

		private void createContent() {
			currentState = new Label(this, SWT.NONE);

			toolbarmgr = new ToolBarManager();
			toolbarmgr.add(new PreviousPage());
			toolbarmgr.add(new NextPage());
			toolbarmgr.createControl(this);
		}

		public void setContentProvider(MedicationTableViewerContentProvider contentProvider) {
			this.contentProvider = contentProvider;
			refresh();
		}

		public void refresh() {
			if (contentProvider != null && contentProvider.input != null) {
				currentState.setText(contentProvider.currentPageOffset + " - " //$NON-NLS-1$
						+ (contentProvider.currentPageOffset + contentProvider.pageSize) + " / " //$NON-NLS-1$
						+ contentProvider.input.size());
			} else {
				currentState.setText(" / "); //$NON-NLS-1$
			}
			layout();
			for (IContributionItem item : toolbarmgr.getItems()) {
				item.update();
			}
		}

		private class NextPage extends Action {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_NEXT.getImageDescriptor();
			}

			@Override
			public void run() {
				contentProvider.nextPage();
				contentProvider.viewer.refresh();
				refresh();
			}

			@Override
			public boolean isEnabled() {
				return contentProvider != null && contentProvider.hasNext();
			}
		}

		private class PreviousPage extends Action {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_PREVIOUS.getImageDescriptor();
			}

			@Override
			public void run() {
				contentProvider.previousPage();
				contentProvider.viewer.refresh();
				refresh();
			}

			@Override
			public boolean isEnabled() {
				return contentProvider != null && contentProvider.hasPrevious();
			}
		}
	}

	public void setInteractionUi(IMedicationInteractionUi interactionUi) {
		this.interactionUi = interactionUi;
	}
}
