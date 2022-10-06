package ch.elexis.core.ui.preferences;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.interfaces.IXid;
import ch.elexis.core.services.IXidService.IXidDomain;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;

public class XIDEdit extends PreferencePage implements IWorkbenchPreferencePage {
	Table table;
	TableViewer tv;
	XIDComparator comparator;
	CommonViewer XIDViewer;

	private static final String[] columnHeaders = { Messages.XIDEdit_ShortName, Messages.XIDEdit_DomainName,
			Messages.XIDEdit_Display };
	private static final int[] colWidth = new int[] { 200, 300, 50 };

	@Override
	protected Control createContents(Composite parent) {
		tv = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		tv.getTable().setHeaderVisible(true);
		tv.setContentProvider(ArrayContentProvider.getInstance());
//		tv.setComparator(comparator);
//		tv.setLabelProvider(new ColumnLabelProvider() {
//			@Override
//			public String getText(Object element) {
//				IXidDomain xid = (IXidDomain) element;
//				return xid.getSimpleName();
//
//			}
//		});
//		
		getQuery();

		tv.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ch.elexis.core.model.IXid xd = (ch.elexis.core.model.IXid) element;
				return xd.getDomainId().toString();
//							Xid.getSimpleNameForXIDDomain((String) element);

			}
		});

		comparator = new XIDComparator();
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn tvc = new TableColumn(table, SWT.NONE);
			tvc.setWidth(colWidth[i]);
			tvc.setText(columnHeaders[i]);
			tvc.addSelectionListener(getSelectionAdapter(tvc, i));
		}

		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = tv.getStructuredSelection();
				if (!selection.isEmpty()) {
					ch.elexis.core.model.IXid id = (ch.elexis.core.model.IXid) selection.getFirstElement();
					if (selection.getFirstElement() instanceof IXid) {
						id.getDomain();
						tv.refresh();
					}
				}
			}

		});
//
//		table.addMouseListener(new MouseAdapter() {
//
//			@Override
//			public void mouseDoubleClick(MouseEvent e) {
//				TableItem[] sel = table.getSelection();
//				if (sel != null && sel.length > 0) {
//					new XidEditDialog(getShell(), sel[0].getText(1)).open();
//					for (TableItem it : table.getItems()) {
//						XIDDomain xd = Xid.getDomain(it.getText(1));
////						 it.setText(0,Xid.getSimpleNameForXIDDomain(dom));
//						StringBuilder sb = new StringBuilder();
//						if (xd.isDisplayedFor(Person.class)) {
//							sb.append("P"); //$NON-NLS-1$
//						}
//						if (xd.isDisplayedFor(Organisation.class)) {
//							sb.append("O"); //$NON-NLS-1$
//						}
//						it.setText(2, sb.toString());
//					}
//					table.redraw();
//				}
//			}
//
//		});
		return table;

	}

	public void getQuery() {
		List<ch.elexis.core.model.IXid> input = CoreModelServiceHolder.get().getQuery(ch.elexis.core.model.IXid.class)
				.execute();

		tv.setInput(input);
	}


	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				tv.getTable().setSortDirection(dir);
				tv.getTable().setSortColumn(column);
				tv.refresh();
			}
		};
		return selectionAdapter;

	}


public class XIDComparator extends ViewerComparator {

	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public XIDComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			direction = 1 - direction;
		}
		this.propertyIndex = column;
		direction = DESCENDING;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		var kname1 = (IXidDomain) e1;
		var kname2 = (IXidDomain) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = Objects.compare(kname1.getSimpleName(), kname2.getSimpleName(),
				Comparator.nullsFirst(Comparator.naturalOrder())) * getDirection();
			break;
		default:
			rc = 0;

		}

		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
//		return super.compare(viewer, e1, e2);
//				kname1.getXid().getLabel().compareToIgnoreCase(kname2.getXid().getLabel());
	}

}

@Override
public void init(IWorkbench workbench) {
	// TODO Auto-generated method stub

}
}
