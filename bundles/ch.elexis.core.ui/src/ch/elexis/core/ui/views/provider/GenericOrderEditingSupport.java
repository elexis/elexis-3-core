package ch.elexis.core.ui.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.OrderEntryState;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import ch.elexis.core.ui.views.OrderManagementView;

public class GenericOrderEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    private final EditingColumnType columnType;
    private final IOrder order;
	private final IOrderService orderService;
	private final OrderManagementView orderManagementView;
	private final int columnIndex; 

	public GenericOrderEditingSupport(OrderManagementView orderManagementView, TableViewer viewer,
			EditingColumnType columnType, IOrder order, int columnIndex,
			IOrderService orderService) {
        super(viewer);
        this.viewer = viewer;
        this.columnType = columnType;
        this.order = order;
		this.orderService = orderService;
		this.orderManagementView = orderManagementView;
		this.columnIndex = columnIndex;
    }

	@Override
	protected CellEditor getCellEditor(Object element) {
		switch (columnType) {
		case SUPPLIER:
			return new ContactSelectionDialogCellEditor(viewer.getTable(),
					Messages.OrderManagement_SelectSupplier_Title, Messages.OrderManagement_SelectSupplier_Message);

		case DELIVERED:
		case ORDERED:
		default:
			TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable(), SWT.NONE);
			Text text = (Text) textCellEditor.getControl();
			text.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

			Runnable goNext = () -> {
				int total = viewer.getTable().getItemCount();
				int row = viewer.getTable().getSelectionIndex();
				do {
					row++;
				} while (row < total && !canEdit(viewer.getElementAt(row)));
				if (row < total) {
					viewer.getTable().setSelection(row);
					viewer.editElement(viewer.getElementAt(row), columnIndex);
				} else {
					orderManagementView.handleOrderButtonClick();
				}
			};
			Runnable goPrev = () -> {
				int row = viewer.getTable().getSelectionIndex();
				do {
					row--;
				} while (row >= 0 && !canEdit(viewer.getElementAt(row)));
				if (row >= 0) {
					viewer.getTable().setSelection(row);
					viewer.editElement(viewer.getElementAt(row), columnIndex);
				}
			};

			text.addTraverseListener(e -> {
				if (e.detail == SWT.TRAVERSE_RETURN || e.detail == SWT.TRAVERSE_TAB_NEXT) {
					e.doit = false;
					Display.getDefault().asyncExec(goNext);
				} else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = false;
					Display.getDefault().asyncExec(goPrev);
				}
			});
			text.addKeyListener(new org.eclipse.swt.events.KeyAdapter() {
				@Override
				public void keyPressed(org.eclipse.swt.events.KeyEvent e) {
					if (e.keyCode == SWT.ARROW_DOWN) {
						goNext.run();
					} else if (e.keyCode == SWT.ARROW_UP) {
						goPrev.run();
					}
				}
			});

			return textCellEditor;
		}
	}


	@Override
	protected boolean canEdit(Object element) {
		if (!(element instanceof IOrderEntry entry)) {
			return false;
		}
		return switch (columnType) {
		case ORDERED -> entry.getState() == OrderEntryState.OPEN;
		case DELIVERED -> orderManagementView.isDeliveryEditMode()
				&& (entry.getState() == OrderEntryState.ORDERED || entry.getState() == OrderEntryState.PARTIAL_DELIVER);
		case SUPPLIER -> true;
		};
	}


    @Override
    protected Object getValue(Object element) {
        if (!(element instanceof IOrderEntry entry)) {
            return null;
        }
		return switch (columnType) {
		case ORDERED -> {
                int amount = entry.getAmount();
			yield amount > 0 ? String.valueOf(amount) : StringUtils.EMPTY;
		}
		case DELIVERED -> String.valueOf(StringUtils.EMPTY);
		case SUPPLIER -> entry.getProvider();
		};
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (!(element instanceof IOrderEntry entry)) {
            return;
        }
        try {
            switch (columnType) {
			case ORDERED -> {
				int newAmount = Integer.parseInt(value.toString().trim());
				int oldAmount = entry.getAmount();
				if (oldAmount != newAmount) {
					entry.setAmount(newAmount);
					CoreModelServiceHolder.get().save(entry);
					viewer.refresh(entry);
					if (orderService.getHistoryService() != null && order != null) {
						orderService.getHistoryService().logChangedAmount(order, entry, oldAmount, newAmount);
					}
				}
			}
			case DELIVERED -> {
				int part = Integer.parseInt(value.toString().trim());
				orderManagementView.getPendingDeliveredValues().put(entry, part);
				viewer.update(entry, null);
			}
				case SUPPLIER -> {
                    if (value instanceof IContact contact) {
                        entry.setProvider(contact);
						if (orderService.getHistoryService() != null && order != null) {
							orderService.getHistoryService().logSupplierAdded(order, entry, contact.getLabel());
                        }
                        CoreModelServiceHolder.get().save(entry);
                        viewer.refresh(entry);
						orderManagementView.refresh();
                    }
				}
            }
        } catch (NumberFormatException e) {
			// parseInt fehlgeschlagen -> Ignorieren
        }
    }

	public enum EditingColumnType {
		ORDERED, DELIVERED, SUPPLIER
	}
}
