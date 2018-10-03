package ch.elexis.core.ui.dialogs;

import java.util.List;
import java.util.Optional;

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

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.StockServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.ui.util.SWTHelper;


public class DailyOrderDialog extends TitleAreaDialog {
	
	private DateTime dtDate;
	private TableViewer tableViewer;
	private IOrder currOrder;

	public DailyOrderDialog(Shell parentShell, IOrder currOrder){
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
				tableViewer.setInput(currOrder.getEntries());
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
		tableViewer.setInput(currOrder.getEntries());
		
		return area;
	}
	
	private void loadArticlesUsedOnSelectedDay(){
		String date = dtDate.getYear() + String.format("%02d", dtDate.getMonth() + 1)
			+ String.format("%02d", dtDate.getDay());
		
		IQuery<IEncounter> query = CoreModelServiceHolder.get().getQuery(IEncounter.class);
		query.and(ModelPackage.Literals.IENCOUNTER__DATE, COMPARATOR.EQUALS, date);
		List<IEncounter> dayEncounters = query.execute();
		
		for (IEncounter encounter : dayEncounters) {
			List<IBilled> encounterBilled = encounter.getBilled();
			for (IBilled billed : encounterBilled) {
				IBillable billable = billed.getBillable();
				if (billable instanceof IArticle) {
					IArticle art = (IArticle) billable;
					Optional<IMandator> mandator =
						ContextServiceHolder.get().getRootContext().getActiveMandator();
					IStockEntry stockEntry =
						StockServiceHolder.get().findPreferredStockEntryForArticle(
							StoreToStringServiceHolder.getStoreToString(art),
							mandator.isPresent() ? mandator.get().getId() : null);
					if (stockEntry != null) {
						currOrder.addEntry(stockEntry.getArticle(), stockEntry.getStock(),
							stockEntry.getProvider(), (int) Math.round(billed.getAmount()));
					} else {
						currOrder.addEntry(art, null, null, (int) Math.round(billed.getAmount()));
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
			IOrderEntry be = (IOrderEntry) element;
			switch (columnIndex) {
			case 0:
				return Integer.toString(be.getAmount());
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
			return currOrder.getEntries().toArray(new IOrderEntry[0]);
		}
		
		@Override
		public void dispose(){}
	}
	
	public IOrder getOrder(){
		return currOrder;
	}
}
