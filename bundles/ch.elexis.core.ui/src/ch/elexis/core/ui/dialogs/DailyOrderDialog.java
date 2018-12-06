package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.PersistentObjectLabelProvider;
import ch.elexis.data.Artikel;
import ch.elexis.data.Bestellung;
import ch.elexis.data.BestellungEntry;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Query;
import ch.elexis.data.StockEntry;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.TimeTool;

public class DailyOrderDialog extends TitleAreaDialog {
	
	private CDateTime dtDate;
	private TableViewer tableViewer;
	private Bestellung currOrder;
	private TimeTool selectedDate;
	
	private List<Mandant> limitationList = new ArrayList<>();
	
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
		dateComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		dateComposite.setLayout(new GridLayout(2, false));
		dtDate = new CDateTime(dateComposite, CDT.DATE_MEDIUM | CDT.DROP_DOWN | SWT.BORDER);
		dtDate.setSelection(new Date());
		dtDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				modifyArticlesUsedOn(selectedDate, false);
				selectedDate = new TimeTool(dtDate.getSelection());
				modifyArticlesUsedOn(selectedDate, true);
				tableViewer.setInput(currOrder.getEntries());
			}
		});
		selectedDate = new TimeTool(dtDate.getSelection());
		
		Button btnFilterMandators = new Button(dateComposite, SWT.CHECK);
		btnFilterMandators.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnFilterMandators.setText("nur von folgenden Mandanten ...");
		btnFilterMandators.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				limitationList.clear();
				btnFilterMandators.setText("nur von folgenden Mandanten ...");
				if (btnFilterMandators.getSelection()) {
					ListSelectionDialog lsd = new ListSelectionDialog(UiDesk.getTopShell(),
						new Query<>(Mandant.class).execute(), ArrayContentProvider.getInstance(),
						PersistentObjectLabelProvider.getInstance(), "Mandanten auswaehlen");
					int open = lsd.open();
					if (open == Dialog.OK) {
						modifyArticlesUsedOn(selectedDate, false); // clear old list
						limitationList.addAll(Arrays.asList(lsd.getResult()).stream()
							.map(m -> (Mandant) m).collect(Collectors.toList()));
						String label = "nur von folgenden Mandanten: " + limitationList.stream()
							.map(m -> m.getLabel()).reduce((u, t) -> u + ", " + t).orElse("keine");
						btnFilterMandators.setText(label);
						modifyArticlesUsedOn(selectedDate, true); // populate new list
						tableViewer.setInput(currOrder.getEntries());
					} else {
						btnFilterMandators.setSelection(false);
					}
				} 
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
		
		modifyArticlesUsedOn(selectedDate, true);
		tableViewer.setInput(currOrder.getEntries());
		
		return area;
	}
	
	/**
	 * 
	 * @param priorDate
	 * @param add
	 * @since 3.7
	 */
	private void modifyArticlesUsedOn(TimeTool priorDate, boolean add){
		Query<Konsultation> qbe = new Query<Konsultation>(Konsultation.class);
		qbe.add(Konsultation.FLD_DATE, Query.EQUALS, priorDate.toDBString(false));
		if (!limitationList.isEmpty()) {
			qbe.startGroup();
			limitationList.stream().forEach(m -> {
				qbe.add(Konsultation.FLD_MANDATOR_ID, Query.EQUALS, m.getId());
				qbe.or();
			});
			qbe.endGroup();
		}
		System.out.println(qbe.getActualQuery());
		List<Konsultation> cons = qbe.execute();
		
		for (Konsultation c : cons) {
			List<Verrechnet> leistungen = c.getLeistungen();
			for (Verrechnet v : leistungen) {
				IVerrechenbar vv = v.getVerrechenbar();
				if (vv instanceof Artikel) {
					Artikel art = (Artikel) vv;
					Mandant mandator = ElexisEventDispatcher.getSelectedMandator();
					IStockEntry stockEntry =
						CoreHub.getStockService().findPreferredStockEntryForArticle(
							art.storeToString(), (mandator != null) ? mandator.getId() : null);
					
					int zahl = v.getZahl();
					if (!add) {
						zahl *= -1;
					}
					
					if (stockEntry != null) {
						StockEntry se = (StockEntry) stockEntry;
						currOrder.addBestellungEntry(se.getArticle(), se.getStock(),
							se.getProvider(), zahl);
					} else {
						currOrder.addBestellungEntry(art, null, null, zahl);
					}
				}
			}
		}
	}
	
	class OrderLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		public Image getColumnImage(final Object element, final int columnIndex){
			return null;
		}
		
		public String getColumnText(final Object element, final int columnIndex){
			BestellungEntry be = (BestellungEntry) element;
			switch (columnIndex) {
			case 0:
				return Integer.toString(be.getCount());
			case 1:
				return be.getArticle().getLabel();
			default:
				return "?"; //$NON-NLS-1$
			}
		}
		
	}
	
	class OrderContentProvider implements IStructuredContentProvider {
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
		
		@Override
		public Object[] getElements(Object inputElement){
			return currOrder.getEntries().toArray(new BestellungEntry[0]);
		}
		
		@Override
		public void dispose(){}
	}
	
	public Bestellung getOrder(){
		return currOrder;
	}
	
	@Override
	protected void cancelPressed(){
		modifyArticlesUsedOn(selectedDate, false);
		super.cancelPressed();
	}
}
