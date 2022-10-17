package ch.elexis.core.ui.preferences;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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

import ch.elexis.core.model.IXid;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.data.Organisation;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.data.Xid.XIDDomain;

public class XIDEdit extends PreferencePage implements IWorkbenchPreferencePage {
	Table table;
	TableViewer tv;
	XIDComparator comparator;
	CommonViewer XIDViewer;

	/**
	 * Hilfsklasse um XIDDomain vollständig (Domainname, Domainkurzname und
	 * Anzeigetyp ob Person oder Organisation) anzuzeigen. Diese Hilfsklasse
	 * representiert einen Zeileneintrag in der Tabelle.
	 */
	static class XIDRow {
		private XIDDomain domain;

		public XIDRow(XIDDomain domain) {
			this.domain = domain;
		}

		public String getDomainName() {
			return domain.getDomainName();
		}

		public String getDomainSimpleName() {
			return domain.getSimpleName();
		}

		public String getDisplayedFor() {
			XIDDomain xidDomain = Xid.getDomain(getDomainName());
			if (xidDomain.isDisplayedFor(Person.class)) {
				return "P";
			}
			if (xidDomain.isDisplayedFor(Organisation.class)) {
				return "O";
			}
			return "";
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		comparator = new XIDComparator();
	}

	@Override
	protected Control createContents(Composite parent) {
		tv = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		final Table table = tv.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table.setHeaderVisible(true);
		tv.setContentProvider(ArrayContentProvider.getInstance());

		Set<XIDRow> input = new HashSet<XIDEdit.XIDRow>();
		for (String dom : Xid.getXIDDomains()) {
			XIDDomain xdom = Xid.getDomain(dom);
			input.add(new XIDRow(xdom));
		}
		tv.setInput(input);

		TableViewerColumn colviewShortName = new TableViewerColumn(tv, SWT.NONE);
		TableColumn colShortName = colviewShortName.getColumn();
		colShortName.setWidth(200);
		colShortName.setText(Messages.XIDEdit_ShortName);
		colShortName.addSelectionListener(getSelectionAdapter(colShortName, 0));
		colviewShortName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XIDRow xid = (XIDRow) element;
				return xid.getDomainSimpleName();
			}
		});

		TableViewerColumn domName = new TableViewerColumn(tv, SWT.NONE);
		TableColumn domNameCol = domName.getColumn();
		domNameCol.setWidth(300);
		domNameCol.setText(Messages.XIDEdit_DomainName);
		domNameCol.addSelectionListener(getSelectionAdapter(domNameCol, 1));
		domName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XIDRow xid = (XIDRow) element;
				return xid.getDomainName();
			}
		});

		TableViewerColumn disp = new TableViewerColumn(tv, SWT.NONE);
		TableColumn dispCol = disp.getColumn();
		dispCol.setWidth(50);
		dispCol.setText(Messages.XIDEdit_Display);
		dispCol.addSelectionListener(getSelectionAdapter(dispCol, 2));
		disp.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				XIDRow xid = (XIDRow) element;
				return xid.getDisplayedFor();
			}
		});

		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = tv.getStructuredSelection();
				if (!selection.isEmpty()) {
					XIDRow id = (XIDRow) selection.getFirstElement();
					if (selection.getFirstElement() instanceof IXid) {
						id.getDomainName();
						tv.refresh();
					}
				}
			}
		});

		tv.setComparator(comparator);
		tv.refresh();
		return table;

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				comparator.changeDirection();
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
		private int direction = 1;

		public int getDirection() {
			return direction == 1 ? SWT.DOWN : SWT.UP;
		}

		public void changeDirection() {
			direction *= -1;
		}

		public void setColumn(int column) {
			this.propertyIndex = column;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			var row1 = (XIDRow) e1;
			var row2 = (XIDRow) e2;
			int rc = 0;
			switch (propertyIndex) {
			case 0:
				rc = Objects.compare(row1.getDomainSimpleName(), row2.getDomainSimpleName(),
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
				break;
			case 1:
				rc = Objects.compare(row1.getDomainName(), row2.getDomainName(),
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
				break;
			case 2:
				rc = Objects.compare(row1.getDisplayedFor(), row2.getDisplayedFor(),
						Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
				break;
			default:
				rc = 0;
			}

			return rc;
		}
	}
}
