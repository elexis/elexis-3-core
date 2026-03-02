/*******************************************************************************
 * Copyright (c) 2026 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     T. Huster - initial API and implementation
 *******************************************************************************/
package ch.elexis.core.ui.contacts.wizard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.icons.Images;

public class ManagedInsuranceWizardPage3 extends WizardPage {

	private TableViewer assignedOrganizationsTable;

	private AssignedViewerComparator comparator;

	private ManagedInsuranceModel currentManagedInsuranceModel;

	private List<String> managedInsuranceIds;

	protected ManagedInsuranceWizardPage3(String pageName, ManagedInsuranceModel currentManagedInsuranceModel) {
		super(pageName);
		setTitle(pageName);
		setMessage(
				"Überprüfen ob die der Organisation zugewiesene Versicherung passt. Und ggf. mit dem Kreuz die Zuordnung rückgängig machen.");
		setImageDescriptor(ImageDescriptor
				.createFromURI(URI.create("platform:/plugin/ch.elexis.core.ui.contacts/rsc/mngd_wizard.png")));
		this.currentManagedInsuranceModel = currentManagedInsuranceModel;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		assignedOrganizationsTable = new TableViewer(composite, SWT.BORDER);
		assignedOrganizationsTable.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		assignedOrganizationsTable.getTable().setHeaderVisible(true);
		assignedOrganizationsTable.getTable().setLinesVisible(true);

		assignedOrganizationsTable.setContentProvider(new ArrayContentProvider());

		TableViewerColumn column = new TableViewerColumn(assignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(450);
		column.getColumn().setText("Organisation");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					IOrganization organization = CoreModelServiceHolder.get()
							.load((String) element, IOrganization.class).get();
					return organization.getLabel();
				}
				return "?";
			}
		});
		column.getColumn().addSelectionListener(getSelectionAdapter(0));

		column = new TableViewerColumn(assignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(450);
		column.getColumn().setText("Versicherung");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					String mappedId = currentManagedInsuranceModel.getMapping().get(element);
					Optional<IOrganization> organization = CoreModelServiceHolder.get().load(mappedId,
							IOrganization.class);
					if (organization.isPresent()) {
						return organization.get().getLabel();
					}
				}
				return "?";
			}
		});
		column.getColumn().addSelectionListener(getSelectionAdapter(1));
		
		column = new TableViewerColumn(assignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(32);
		column.getColumn().setText("");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof String) {
					return Images.IMG_DELETE.getImage();
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});

		comparator = new AssignedViewerComparator();
		assignedOrganizationsTable.setComparator(comparator);

		// connect double click on column to actions
		Table table = assignedOrganizationsTable.getTable();
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				int clickedIndex = -1;
				// calculate column of click
				int width = 0;
				TableColumn[] columns = table.getColumns();
				for (int i = 0; i < columns.length; i++) {
					TableColumn tc = columns[i];
					if (width < e.x && e.x < width + tc.getWidth()) {
						clickedIndex = i;
						break;
					}
					width += tc.getWidth();
				}
				if (clickedIndex != -1) {
					if (clickedIndex == 2) {
						unconfirmSelection();
					}
				}
			}

			private void unconfirmSelection() {
				IStructuredSelection selection = assignedOrganizationsTable.getStructuredSelection();
				if (!selection.isEmpty()) {
					String id = (String) selection.getFirstElement();
					if (currentManagedInsuranceModel.getConfirmed().contains(id)) {
						currentManagedInsuranceModel.getConfirmed().remove(id);
						currentManagedInsuranceModel.save();
						assignedOrganizationsTable.refresh();
					}
				}
			}
		});

		setControl(composite);

		managedInsuranceIds = new ArrayList<>(new ManagedInsurancesUniqueIdsSupplier().get());
		managedInsuranceIds.sort((l,r) -> {
			return CoreModelServiceHolder.get().load(l, IOrganization.class).get().getLabel()
					.compareTo(CoreModelServiceHolder.get().load(r, IOrganization.class).get().getLabel());
		});
		refreshTable();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			refreshTable();
		}
	}

	private void refreshTable() {
		assignedOrganizationsTable.setInput(currentManagedInsuranceModel.getConfirmed());
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}

	private SelectionAdapter getSelectionAdapter(int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				assignedOrganizationsTable.getTable().setSortDirection(dir);
				assignedOrganizationsTable.refresh();
			}
		};
		return selectionAdapter;
	}

	private class AssignedViewerComparator extends ViewerComparator implements Comparator<String> {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public AssignedViewerComparator() {
			this.propertyIndex = 0;
			direction = DESCENDING;
		}

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = 1 - direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = DESCENDING;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return compare((String) e1, (String) e2);
		}

		@Override
		public int compare(String left, String right) {
			int rc = 0;
			IOrganization lOrganization = CoreModelServiceHolder.get().load(left, IOrganization.class).get();
			IOrganization rOrganization = CoreModelServiceHolder.get().load(right, IOrganization.class).get();
			switch (propertyIndex) {
			case 0:
				rc = rOrganization.getLabel().compareTo(lOrganization.getLabel());
				break;
			case 1:
				String mappedId = currentManagedInsuranceModel.getMapping().get(left);
				lOrganization = CoreModelServiceHolder.get().load(mappedId, IOrganization.class).get();
				mappedId = currentManagedInsuranceModel.getMapping().get(right);
				rOrganization = CoreModelServiceHolder.get().load(mappedId, IOrganization.class).get();
				rc = rOrganization.getLabel().compareTo(lOrganization.getLabel());
				break;

			default:
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}
	}
}
