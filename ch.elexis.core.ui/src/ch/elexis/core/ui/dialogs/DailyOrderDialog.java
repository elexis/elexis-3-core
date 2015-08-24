package ch.elexis.core.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.Bestellung.Item;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Query;
import ch.elexis.data.Verrechnet;

public class DailyOrderDialog extends TitleAreaDialog {
	
	private DateTime dtDate;
	private TableViewer tableViewer;
	private Bestellung currOrder;
	
	public DailyOrderDialog(Shell parentShell, Bestellung currOrder){
		super(parentShell);
		this.currOrder = currOrder;
	}
	
	@Override
	public void create(){
		super.create();
		setTitle(Messages.DailyOrderDialog_Title);
		setMessage(Messages.DailyOrderDialog_Message);
	}
	
	@Override
	protected boolean isResizable(){
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		area.setLayout(new GridLayout(1, false));
		
		Composite dateComposite = new Composite(area, SWT.NONE);
		dateComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		dateComposite.setLayout(new GridLayout(1, false));
		dtDate = new DateTime(area, SWT.DATE | SWT.DROP_DOWN);
		dtDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				loadArticlesUsedOnSelectedDay();
				tableViewer.refresh(true);
			}
		});
		
		Composite tableComposite = new Composite(area, SWT.NONE);
		tableComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableComposite.setLayout(tcLayout);
		
		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		TableViewerColumn tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcAmount = tvc.getColumn();
		tcLayout.setColumnData(tcAmount, new ColumnPixelData(70, false, false));
		tcAmount.setText(Messages.DailyOrderDialog_Amount);
		
		tvc = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcArticle = tvc.getColumn();
		tcLayout.setColumnData(tcArticle, new ColumnPixelData(300, true, true));
		tcArticle.setText(Messages.DailyOrderDialog_Article);
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new OrderLabelProvider());
		
		loadArticlesUsedOnSelectedDay();
		tableViewer.setInput(currOrder.asList());
		
		return area;
	}
	
	private void loadArticlesUsedOnSelectedDay(){
		currOrder.asList().clear();
		
		String date =
			dtDate.getYear() + String.format("%02d", dtDate.getMonth() + 1)
				+ String.format("%02d", dtDate.getDay());
		
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_DATE, Query.EQUALS, date);
		List<Konsultation> cons = qbe.execute();
		
		for (Konsultation c : cons) {
			List<Verrechnet> leistungen = c.getLeistungen();
			for (Verrechnet v : leistungen) {
				IVerrechenbar vv = v.getVerrechenbar();
				if (vv instanceof Artikel) {
					Artikel a = (Artikel) vv;
					currOrder.addItem(a, v.getZahl());
				}
			}
		}
	}
	
	class OrderLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(final Object element, final int columnIndex){
			return null;
		}
		
		public String getColumnText(final Object element, final int columnIndex){
			if (element instanceof Bestellung.Item) {
				Item it = (Item) element;
				switch (columnIndex) {
				case 0:
					return Integer.toString(it.num);
				case 1:
					return it.art.getLabel();
				default:
					return "?"; //$NON-NLS-1$
				}
			}
			return "??"; //$NON-NLS-1$
		}
		
	}
	
	class OrderContentProvider implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
		
		@Override
		public Object[] getElements(Object inputElement){
			if (currOrder != null) {
				return currOrder.asList().toArray();
			}
			return new Object[0];
		}
		
		@Override
		public void dispose(){}
	}
	
	public Bestellung getOrder(){
		return currOrder;
	}
}
