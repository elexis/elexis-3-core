package ch.elexis.core.ui.views.provider;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
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
                return new ContactSelectionDialogCellEditor(
                    viewer.getTable(),
						Messages.OrderManagement_SelectSupplier_Title, Messages.OrderManagement_SelectSupplier_Message
                );
            case DELIVERED:
            case ORDERED:
            default:
				TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable(), SWT.NONE);
				final Text text = (Text) textCellEditor.getControl();
				text.addTraverseListener(new TraverseListener() {
					@Override
					public void keyTraversed(TraverseEvent e) {
						if (e.detail == SWT.TRAVERSE_RETURN) {
							e.doit = true;
							text.getDisplay().asyncExec(() -> {
								int currentRow = viewer.getTable().getSelectionIndex();
								int nextRow = currentRow + 1;
								int totalRows = viewer.getTable().getItemCount();
								if (nextRow < totalRows) {
									viewer.getTable().setSelection(nextRow);
									viewer.editElement(viewer.getElementAt(nextRow), columnIndex);
								}
							});
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
		case DELIVERED -> {
			boolean editableState = entry.getState() != OrderEntryState.OPEN;
			yield editableState && orderManagementView.isDeliveryEditMode();
		}
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
