package ch.elexis.core.ui.views.provider;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
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
import ch.elexis.core.ui.editors.ContactSelectionDialogCellEditor;
import ch.elexis.core.ui.util.OrderHistoryManager;
import ch.elexis.core.ui.util.OrderManagementUtil;
import ch.elexis.core.ui.views.OrderManagementView;

public class GenericOrderEditingSupport extends EditingSupport {

    private final TableViewer viewer;
    private final EditingColumnType columnType;
    private final IOrder order;
    private final OrderHistoryManager historyManager;
	private final OrderManagementView orderManagementView;
	private final int columnIndex; 

	public GenericOrderEditingSupport(OrderManagementView orderManagementView, TableViewer viewer,
			EditingColumnType columnType, IOrder order, OrderHistoryManager historyManager, int columnIndex) {
        super(viewer);
        this.viewer = viewer;
        this.columnType = columnType;
        this.order = order;
        this.historyManager = historyManager;
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
		case DELIVERED ->
			entry.getState() == OrderEntryState.ORDERED || entry.getState() == OrderEntryState.PARTIAL_DELIVER;
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
		case DELIVERED -> String.valueOf(entry.getDelivered());
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
                    int ordered = Integer.parseInt(value.toString().trim());
                    entry.setAmount(ordered);
                    CoreModelServiceHolder.get().save(entry);
                    viewer.refresh(entry);
				}
				case DELIVERED -> {
					int part = Integer.parseInt(value.toString().trim());
					int oldDelivered = entry.getDelivered();
					int newTotalDelivery = oldDelivered + part;
					int ordered = entry.getAmount();

					if (newTotalDelivery > ordered) {
						boolean confirm = MessageDialog.openQuestion(viewer.getControl().getShell(),
								Messages.OrderManagement_Overdelivery_Title,
								MessageFormat.format(Messages.OrderManagement_Overdelivery_Message, oldDelivered, part,
										newTotalDelivery, ordered));

						if (!confirm) {
							viewer.refresh(entry);
							return;
						}
					}
					OrderManagementUtil.saveSingleDelivery(entry, part);

					boolean allDelivered = order.getEntries().stream()
							.allMatch(e -> e.getState() == OrderEntryState.DONE);
					if (allDelivered) {
						orderManagementView.reload();
					}
					viewer.refresh(entry);
				}
				case SUPPLIER -> {
                    if (value instanceof IContact contact) {
                        entry.setProvider(contact);
                        if (historyManager != null && order != null) {
                            historyManager.logSupplierAdded(order, entry, contact.getLabel());
                        }
                        CoreModelServiceHolder.get().save(entry);
                        viewer.refresh(entry);
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
