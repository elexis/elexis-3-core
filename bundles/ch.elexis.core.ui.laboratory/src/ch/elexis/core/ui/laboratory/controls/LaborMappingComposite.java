package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Kontakt;
import ch.elexis.data.LabMapping;
import ch.elexis.data.Labor;

public class LaborMappingComposite extends Composite {

	protected TableViewer viewer;
	protected ILabItem labItem;

	protected List<LabMapping> content = Collections.emptyList();

	protected List<TransientLabMapping> transientContent = new ArrayList<>();

	public LaborMappingComposite(Composite parent, int style) {
		super(parent, style);

		createContent();
	}

	public void setLabItem(ILabItem labItem) {
		this.labItem = labItem;
		refreshContent();
	}

	public void persistTransientLabMappings(ILabItem labItem) {
		for (TransientLabMapping transientMapping : transientContent) {
			transientMapping.persist(labItem);
		}
	}

	private void refreshContent() {
		if (labItem != null) {
			content = LabMapping.getByLabItemId(labItem.getId());
			viewer.setInput(content);
		} else {
			content = Collections.emptyList();
			viewer.setInput(transientContent);
		}
	}

	protected void createContent() {
		setLayout(new GridLayout(2, false));

		Label title = new Label(this, SWT.NONE);
		title.setText(Messages.LaborMappingComposite_labelMappings);
		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		ToolBarManager toolbar = new ToolBarManager();
		toolbar.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_NEW.getImageDescriptor();
			}

			@Override
			public void run() {
				KontaktSelektor selektor = new KontaktSelektor(getShell(), Labor.class,
						Messages.Core_Laboratory_Selection,
						Messages.Core_Select_Laboratory, Kontakt.DEFAULT_SORT);
				if (selektor.open() == Dialog.OK) {
					Labor labor = (Labor) selektor.getSelection();
					if (labItem != null) {
						LabMapping mapping = new LabMapping(labor.getId(), labItem.getCode(), labItem.getId(),
								false); // $NON-NLS-1$
						refreshContent();
					} else {
						TransientLabMapping mapping = new TransientLabMapping();
						mapping.setOriginId(labor.getId());
						mapping.setItemName("???"); //$NON-NLS-1$
						transientContent.add(mapping);
						refreshContent();
					}
				}
			}
		});
		toolbar.add(new Action() {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_DELETE.getImageDescriptor();
			}

			@Override
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					if (selection.getFirstElement() instanceof LabMapping) {
						LabMapping mapping = (LabMapping) selection.getFirstElement();
						mapping.delete();
						refreshContent();
					} else if (selection.getFirstElement() instanceof TransientLabMapping) {
						transientContent.remove(selection.getFirstElement());
						refreshContent();
					}
				}
			}
		});
		ToolBar toolBar = toolbar.createControl(this);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		toolbar.update(true);

		viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		viewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.Core_Laboratory);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LabMapping) {
					return ((LabMapping) element).getOrigin().getLabel(true);
				} else if (element instanceof TransientLabMapping) {
					return ((TransientLabMapping) element).getOrigin().getLabel(true);
				}
				return StringUtils.EMPTY;
			}
		});

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.Core_Short_Label);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof LabMapping) {
					return ((LabMapping) element).getItemName();
				} else if (element instanceof TransientLabMapping) {
					return ((TransientLabMapping) element).getItemName();
				}
				return StringUtils.EMPTY;
			}
		});
		column.setEditingSupport(new ItemNameEditingSupport(viewer));
	}

	protected class ItemNameEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public ItemNameEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof LabMapping) {
				return ((LabMapping) element).getItemName();
			} else if (element instanceof TransientLabMapping) {
				return ((TransientLabMapping) element).getItemName();
			}
			return StringUtils.EMPTY;
		}

		@Override
		protected void setValue(Object element, Object userInputValue) {
			if (element instanceof LabMapping) {
				((LabMapping) element).setItemName(String.valueOf(userInputValue));
			} else if (element instanceof TransientLabMapping) {
				((TransientLabMapping) element).setItemName(String.valueOf(userInputValue));
			}
			viewer.update(element, null);
		}
	}

	public class TransientLabMapping {
		private String originId;
		private String itemName;
		private String labItemId;
		private boolean charge;

		public TransientLabMapping() {
			// TODO Auto-generated constructor stub
		}

		public void persist(ILabItem labItem) {
			new LabMapping(originId, itemName, labItem.getId(), charge);
		}

		public Kontakt getOrigin() {
			return Kontakt.load(originId);
		}

		public void setOriginId(String originId) {
			this.originId = originId;
		}

		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public String getLabItemId() {
			return labItemId;
		}

		public void setLabItemId(String labItemId) {
			this.labItemId = labItemId;
		}

		public boolean isCharge() {
			return charge;
		}

		public void setCharge(boolean charge) {
			this.charge = charge;
		}
	}
}
