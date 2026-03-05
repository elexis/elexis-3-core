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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.controls.GenericSearchSelectionDialog;

public class ManagedInsuranceWizardPage2 extends WizardPage {

	private TableViewer notAssignedOrganizationsTable;

	private NotAssignedViewerComparator comparator;

	private Map<String, Long> countCoveragesMap;

	private ManagedInsuranceModel currentManagedInsuranceModel;

	private List<IOrganization> notAssignedOrganizations;

	private List<String> managedInsuranceIds;

	private Button showIgnoredBtn;

	protected ManagedInsuranceWizardPage2(String pageName, List<IOrganization> notAssignedOrganizations,
			Map<String, Long> countCoveragesMap, ManagedInsuranceModel currentManagedInsuranceModel) {
		super(pageName);
		setTitle(pageName);
		setMessage(
				"Zu der Organisation die passende Versicherung auswählen, und dann mit dem Haken bestätigen. Wenn keine Versicherung passt, ignorieren aktivieren.");
		setImageDescriptor(ImageDescriptor
				.createFromURI(URI.create("platform:/plugin/ch.elexis.core.ui.contacts/rsc/mngd_wizard.png")));
		this.currentManagedInsuranceModel = currentManagedInsuranceModel;

		this.notAssignedOrganizations = notAssignedOrganizations;
		this.countCoveragesMap = countCoveragesMap;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		notAssignedOrganizationsTable = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		notAssignedOrganizationsTable.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

		notAssignedOrganizationsTable.getTable().setHeaderVisible(true);
		notAssignedOrganizationsTable.getTable().setLinesVisible(true);

		notAssignedOrganizationsTable.setContentProvider(new ArrayContentProvider());

		TableViewerColumn column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(450);
		column.getColumn().setText("Organisation");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IOrganization) {
					return ((IOrganization) element).getLabel();
				}
				return "?";
			}
		});
		column.getColumn().addSelectionListener(getSelectionAdapter(0));

		column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Zusatz");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IOrganization) {
					return ((IOrganization) element).getDescription3();
				}
				return "?";
			}
		});
		column.getColumn().addSelectionListener(getSelectionAdapter(1));

		column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Anzahl Fälle");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IOrganization) {
					Long count = countCoveragesMap.get(((IOrganization) element).getId());
					return count != null ? Long.toString(count) : "...";
				}
				return "?";
			}
		});
		column.getColumn().addSelectionListener(getSelectionAdapter(2));

		column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText("Ignorieren");
		column.setLabelProvider(new EmulatedCheckBoxLabelProvider() {
			@Override
			protected boolean isChecked(Object element) {
				return currentManagedInsuranceModel.getIgnored().contains(((IOrganization) element).getId());
			}
		});
		column.setEditingSupport(new CheckBoxColumnEditingSupport(notAssignedOrganizationsTable));
		column.getColumn().addSelectionListener(getSelectionAdapter(3));

		column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(450);
		column.getColumn().setText("Versicherung");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IOrganization) {
					String mappedId = currentManagedInsuranceModel.getMapping().get(((IOrganization) element).getId());
					if (StringUtils.isNoneBlank(mappedId)) {
						IOrganization insurance = CoreModelServiceHolder.get().load(mappedId, IOrganization.class)
								.get();
						return getInsuranceLabel(insurance);
					}
				}
				return "Hier klicken um die Versicherung zu zuweisen.";
			}
		});
		column.setEditingSupport(new EditingSupport(notAssignedOrganizationsTable) {

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new ContactSelectionDialogCellEditor(notAssignedOrganizationsTable.getTable(),
						StringUtils.EMPTY, StringUtils.EMPTY) {
					@Override
					protected Object openDialogBox(Control cellEditorWindow) {
						List<IContact> insurances = new ArrayList<>(managedInsuranceIds.stream()
								.map(id -> CoreModelServiceHolder.get().load(id, IOrganization.class).get()).toList());
						GenericSearchSelectionDialog dialog = new GenericSearchSelectionDialog(
								cellEditorWindow.getShell(), insurances, "Versicherungen",
								"Die passende Versicherung auswählen.", StringUtils.EMPTY, null,
								SWT.SINGLE);
						dialog.setCustomLabelProvider(new LabelProvider() {
							@Override
							public String getText(Object element) {
								return getInsuranceLabel((IOrganization) element);
							};
						});

						if (dialog.open() == Window.OK) {
							return (IOrganization) dialog.getSelection().getFirstElement() != null
									? ((IOrganization) dialog.getSelection().getFirstElement()).getId()
									: null;
						}
						return null;
					}
				};
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				return currentManagedInsuranceModel.getMapping().get(element);
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (value != null) {
					String id = ((IOrganization) element).getId();
					currentManagedInsuranceModel.getMapping().put(id, (String) value);
					currentManagedInsuranceModel.save();
					notAssignedOrganizationsTable.refresh(element);
				}
			}
			
		});
		column = new TableViewerColumn(notAssignedOrganizationsTable, SWT.NONE);
		column.getColumn().setWidth(32);
		column.getColumn().setText("");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof IOrganization) {
					String mappedId = currentManagedInsuranceModel.getMapping().get(((IOrganization) element).getId());
					if (StringUtils.isNoneBlank(mappedId)) {
						return Images.IMG_TICK.getImage();
					}
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				return null;
			}
		});
		
		comparator = new NotAssignedViewerComparator();
		notAssignedOrganizationsTable.setComparator(comparator);
		
		// connect double click on column to actions
		Table table = notAssignedOrganizationsTable.getTable();
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
					if (clickedIndex == 5) {
						confirmSelection();
					}
				}
			}

			private void confirmSelection() {
				IStructuredSelection selection = notAssignedOrganizationsTable.getStructuredSelection();
				if (!selection.isEmpty()) {
					String id = ((IOrganization) selection.getFirstElement()).getId();
					String mappedId = currentManagedInsuranceModel.getMapping().get(id);
					if (StringUtils.isNotBlank(mappedId)) {
						if (!currentManagedInsuranceModel.getConfirmed().contains(id)) {
							currentManagedInsuranceModel.getIgnored().remove(id);
							currentManagedInsuranceModel.getConfirmed().add(id);
							currentManagedInsuranceModel.save();
							notAssignedOrganizationsTable.refresh(true);
						}
					}
				}
			}
		});

		showIgnoredBtn = new Button(composite, SWT.CHECK);
		showIgnoredBtn.setText("Ignorierte anzeigen");
		showIgnoredBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notAssignedOrganizationsTable.refresh(true);
			}
		});

		setControl(composite);

		managedInsuranceIds = new ArrayList<>(new ManagedInsurancesUniqueIdsSupplier().get());
		managedInsuranceIds.sort((l,r) -> {
			return CoreModelServiceHolder.get().load(l, IOrganization.class).get().getLabel()
					.compareTo(CoreModelServiceHolder.get().load(r, IOrganization.class).get().getLabel());
		});
		initTable();

		notAssignedOrganizationsTable.addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IOrganization) {
					String id = ((IOrganization) element).getId();
					if (!currentManagedInsuranceModel.getConfirmed().contains(id)) {
						if (!showIgnoredBtn.getSelection()) {
							return !currentManagedInsuranceModel.getIgnored().contains(id);
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	private String getInsuranceLabel(IOrganization insurance) {
		StringBuilder sb = new StringBuilder();
		sb.append(insurance.getDescription1()).append(StringUtils.SPACE)
				.append(StringUtils.defaultString(insurance.getDescription2()));
		if (!StringUtils.isBlank(insurance.getInsuranceLawCode())) {
			sb.append(" (").append(insurance.getInsuranceLawCode()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sb.append(", ").append(StringUtils.defaultString(insurance.getStreet())).append(", ") //$NON-NLS-1$
				.append(StringUtils.defaultString(insurance.getZip())).append(StringUtils.SPACE)
				.append(StringUtils.defaultString(insurance.getCity()));
		return sb.toString();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			notAssignedOrganizationsTable.refresh(true);
		}
	}

	private void initTable() {
		Display.getDefault().asyncExec(() -> notAssignedOrganizationsTable.setInput(notAssignedOrganizations));
	}

	public boolean finish() {
		// TODO Auto-generated method stub
		return false;
	}

	abstract static class EmulatedCheckBoxLabelProvider extends ColumnLabelProvider {

		private static Image CHECKED = AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.core.ui.contacts", //$NON-NLS-1$
				"rsc/checked_checkbox.png").createImage(); //$NON-NLS-1$

		private static Image UNCHECKED = AbstractUIPlugin.imageDescriptorFromPlugin("ch.elexis.core.ui.contacts", //$NON-NLS-1$
				"rsc/unchecked_checkbox.png").createImage(); //$NON-NLS-1$

		@Override
		public String getText(Object element) {
			return null;
		}

		@Override
		public Image getImage(Object element) {
			return isChecked(element) ? CHECKED : UNCHECKED;
		}

		protected abstract boolean isChecked(Object element);
	}

	private class CheckBoxColumnEditingSupport extends EditingSupport {

		private final TableViewer tableViewer;

		public CheckBoxColumnEditingSupport(TableViewer viewer) {
			super(viewer);
			this.tableViewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object o) {
			return new CheckboxCellEditor(null, SWT.CHECK);
		}

		@Override
		protected boolean canEdit(Object o) {
			return true;
		}

		@Override
		protected Object getValue(Object o) {
			return currentManagedInsuranceModel.getIgnored().contains(((IOrganization) o).getId());
		}

		@Override
		protected void setValue(Object o, Object value) {
			if ((Boolean) value) {
				currentManagedInsuranceModel.getIgnored().add(((IOrganization) o).getId());
			} else {
				currentManagedInsuranceModel.getIgnored().remove(((IOrganization) o).getId());
			}
			currentManagedInsuranceModel.save();
			tableViewer.refresh(true);
		}
	}

	private SelectionAdapter getSelectionAdapter(int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				notAssignedOrganizationsTable.getTable().setSortDirection(dir);
				notAssignedOrganizationsTable.refresh();
			}
		};
		return selectionAdapter;
	}

	private class NotAssignedViewerComparator extends ViewerComparator implements Comparator<IOrganization> {
		private int propertyIndex;
		private static final int DESCENDING = 1;
		private int direction = DESCENDING;

		public NotAssignedViewerComparator() {
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
			IOrganization left = (IOrganization) e1;
			IOrganization right = (IOrganization) e2;
			return compare(left, right);
		}

		@Override
		public int compare(IOrganization left, IOrganization right) {
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = right.getLabel().compareTo(left.getLabel());
				break;
			case 1:
				rc = StringUtils.defaultString(right.getDescription3())
						.compareTo(StringUtils.defaultString(left.getDescription3()));
				break;
			case 2:
				Long rCount = countCoveragesMap.get(right.getId());
				Long lCount = countCoveragesMap.get(left.getId());
				if (rCount != null && lCount != null) {
					return rCount.compareTo(lCount);
				}
				break;
			case 3:
				Boolean rIgnore = currentManagedInsuranceModel.getIgnored().contains(right.getId());
				Boolean lIgnore = currentManagedInsuranceModel.getIgnored().contains(left.getId());
				if (rIgnore != null && lIgnore != null) {
					return rIgnore.compareTo(lIgnore);
				}
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
