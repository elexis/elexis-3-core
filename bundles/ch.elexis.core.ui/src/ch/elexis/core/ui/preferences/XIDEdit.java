package ch.elexis.core.ui.preferences;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.model.IXid;
import ch.elexis.core.services.IXidService;
import ch.elexis.core.services.IXidService.IXidDomain;
import ch.elexis.core.services.holder.XidServiceHolder;
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
	IXidService xidService;

	/**
	 * Hilfsklasse um XIDDomain vollständig (Domainname, Domainkurzname und
	 * Anzeigetyp ob Person oder Organisation) anzuzeigen. Diese Hilfsklasse
	 * representiert einen Zeileneintrag in der Tabelle.
	 */
	static class XIDRow {
		public final static String DISPLAY_FOR_PERSON = "P";
		public final static String DISPLAY_FOR_ORG = "O";
		private IXidDomain domain;

		public XIDRow(IXidDomain xdom) {
			this.domain = xdom;
		}

		public String getDomainName() {
			return domain.getDomainName();
		}

		public String getDomainSimpleName() {
			return domain.getSimpleName();
		}

		public String getDisplayedFor() {
			XIDDomain xidDomain = Xid.getDomain(getDomainName());
			StringBuffer sb = new StringBuffer();
			if (xidDomain.isDisplayedFor(Person.class)) {
				sb.append(DISPLAY_FOR_PERSON);
			}
			if (xidDomain.isDisplayedFor(Organisation.class)) {
				sb.append(DISPLAY_FOR_ORG);
			}
			return sb.toString();
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		comparator = new XIDComparator();
		xidService = XidServiceHolder.get();
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
		List<IXidDomain> domains = xidService.getDomains();
		for (IXidDomain dom : domains) {
			input.add(new XIDRow(dom));
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
		dispCol.setText(Messages.Core_Display);
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

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] sel = table.getSelection();
				if (sel != null && sel.length > 0) {
					XIDRow selDom = (XIDRow) sel[0].getData();
					new XidEditDialog(getShell(), selDom).open();
					for (TableItem it : table.getItems()) {
						XIDRow xidRow = (XIDRow) it.getData();
						it.setText(2, xidRow.getDisplayedFor());
					}
					table.redraw();
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

	static class XidEditDialog extends Dialog {
		Text tShort;
		Button bPerson;
		Button bOrg;
		XIDDomain mine;

		public XidEditDialog(Shell shell, XIDRow xidDom) {
			super(shell);
			mine = Xid.getDomain(xidDom.getDomainName());
		}

		@Override
		public void create() {
			super.create();
			getShell().setText(Messages.XIDEdit_XidOpetions);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite ret = (Composite) super.createDialogArea(parent);
			ret.setLayout(new RowLayout(SWT.VERTICAL));
			new Label(ret, SWT.NONE).setText(mine.getDomainName());
			tShort = new Text(ret, SWT.BORDER);
			tShort.setText(mine.getSimpleName());
			new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL);
			new Label(ret, SWT.NONE).setText(Messages.XIDEdit_ShowWith);
			bPerson = new Button(ret, SWT.CHECK);
			bOrg = new Button(ret, SWT.CHECK);
			bPerson.setText(Messages.XIDEdit_Persons);
			bOrg.setText(Messages.XIDEdit_Organizations);
			bPerson.setSelection(false);
			bOrg.setSelection(false);
			if (mine.isDisplayedFor(Person.class)) {
				bPerson.setSelection(true);
			}
			if (mine.isDisplayedFor(Organisation.class)) {
				bOrg.setSelection(true);
			}
			return ret;
		}

		@Override
		protected void okPressed() {
			if (bPerson.getSelection()) {
				mine.addDisplayOption(Person.class);
			} else {
				mine.removeDisplayOption(Person.class);
			}
			if (bOrg.getSelection()) {
				mine.addDisplayOption(Organisation.class);
			} else {
				mine.removeDisplayOption(Organisation.class);
			}
			mine.setSimpleName(tShort.getText());
			super.okPressed();
		}
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
