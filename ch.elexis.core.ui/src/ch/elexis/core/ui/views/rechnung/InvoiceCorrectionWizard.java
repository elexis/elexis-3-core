package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.dto.InvoiceCorrectionDTO;
import ch.elexis.data.dto.InvoiceHistoryEntryDTO;

public class InvoiceCorrectionWizard extends Wizard {
	
	Page1 page1;
	Page2 page2;
	private InvoiceCorrectionDTO invoiceCorrectionDTO;
	
	public InvoiceCorrectionWizard(InvoiceCorrectionDTO invoiceCorrectionDTO){
		super();
		setNeedsProgressMonitor(true);
		this.invoiceCorrectionDTO = invoiceCorrectionDTO;
	}
	
	@Override
	public boolean performFinish(){
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public String getWindowTitle(){
		return "Rechnungskorrektur";
	}
	
	@Override
	public void addPages(){
		page1 = new Page1(invoiceCorrectionDTO);
		page2 = new Page2(invoiceCorrectionDTO);
		addPage(page1);
		addPage(page2);
	}
	
	class Page1 extends WizardPage {
		
		private Text text1;
		private Composite container;
		private InvoiceCorrectionDTO invoiceCorrectionDTO;
		
		protected Page1(InvoiceCorrectionDTO invoiceCorrectionDTO){
			super("Rechnungskorrektur");
			setTitle("Rechnungskorrektur");
			setDescription(
				"Anbei finden Sie eine Übersicht der getätigten Änderungen.\nDurch das Bestätigen auf Rechnung erstellen werden die Änderungen übernommen und eine neue Rechnung erstellt.");
			setControl(text1);
			this.invoiceCorrectionDTO = invoiceCorrectionDTO;
		}
		
		@Override
		public void createControl(Composite parent){
			container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout(1, false));
			
			TableViewer viewer =
				new TableViewer(container,
					SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
						| SWT.READ_ONLY);
			viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.getTable().setLinesVisible(true);
			viewer.setInput(invoiceCorrectionDTO.getHistory());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					int i = invoiceCorrectionDTO.getHistory().indexOf(element);
					return String.valueOf(++i) + ".   "
						+ ((InvoiceHistoryEntryDTO) element).getText();
				}
			});
			
			TableColumn singleColumn = new TableColumn(viewer.getTable(), SWT.NONE);
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(100));
			container.setLayout(tableColumnLayout);
			
			setControl(container);
			setPageComplete(!invoiceCorrectionDTO.getHistory().isEmpty());
		}
		
	}
	
	class Page2 extends WizardPage {
		private Text txtOutput;
		private Text text1;
		private Composite container;
		private InvoiceCorrectionDTO invoiceCorrectionDTO;
		private CheckboxTableViewer viewer;
		
		protected Page2(InvoiceCorrectionDTO invoiceCorrectionDTO){
			super("Rechnungskorrektur");
			setTitle("Rechnungskorrektur");
			setDescription("Folgende Änderungen wurden erfolgreich übernommen.");
			setControl(text1);
			this.invoiceCorrectionDTO = invoiceCorrectionDTO;
		}
		
		@Override
		public void createControl(Composite parent){
			container = new Composite(parent, SWT.NONE);
			container.setLayout(new GridLayout(1, false));
			
			Composite tableComp = new Composite(container, SWT.NONE);
			tableComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer = CheckboxTableViewer.newCheckList(tableComp,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
			viewer.getTable().setLinesVisible(true);
			viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer.setContentProvider(new ArrayContentProvider());
			viewer.setInput(invoiceCorrectionDTO.getHistory());
			viewer.addCheckStateListener(new ICheckStateListener() {
				
				@Override
				public void checkStateChanged(CheckStateChangedEvent event){
					viewer.setChecked(event.getElement(), !event.getChecked());
				}
			});
			viewer.setLabelProvider(new WizardLabelProvider());
			
			TableColumn singleColumn = new TableColumn(viewer.getTable(), SWT.NONE);
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(100));
			tableComp.setLayout(tableColumnLayout);
			
			
			Label lblOutput = new Label(container, SWT.NONE);
			lblOutput.setText("Ausgabe");
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 100;
			txtOutput =
				new Text(container,
				SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
			txtOutput.setLayoutData(gd);
			setControl(container);
			setPageComplete(!invoiceCorrectionDTO.getHistory().isEmpty());
		}
		
		public Text getTxtOutput(){
			return txtOutput;
		}
		
		public InvoiceCorrectionDTO getInvoiceCorrectionDTO(){
			return invoiceCorrectionDTO;
		}
		
		public void setChecked(Object element, boolean state){
			viewer.setChecked(element, state);
			viewer.refresh(true);
		}
		
		public void updateProcess(){
			for (InvoiceHistoryEntryDTO invoiceHistoryEntryDTO : invoiceCorrectionDTO
				.getHistory()) {
				if (!invoiceHistoryEntryDTO.isIgnored()) {
					setChecked(invoiceHistoryEntryDTO,
						Boolean.TRUE.equals(invoiceHistoryEntryDTO.isSuccess()));
				} else {
					viewer.remove(invoiceHistoryEntryDTO);
				}
				
			}
		}
		
	}
	
	@Override
	public boolean canFinish(){
		return page2.equals(getContainer().getCurrentPage());
	}
	
	class WizardLabelProvider extends LabelProvider implements IColorProvider {
		
		@Override
		public Color getForeground(Object element){
			return UiDesk.getColor(UiDesk.COL_BLACK);
		}
		
		@Override
		public Color getBackground(Object element){
			InvoiceHistoryEntryDTO item = (InvoiceHistoryEntryDTO) element;
			if (item.isSuccess() == null)
			{
				return UiDesk.getColor(UiDesk.COL_WHITE);
			}
			if (Boolean.TRUE.equals(item.isSuccess())) {
				return UiDesk.getColorFromRGB("E2FFC3");
			}
			return UiDesk.getColorFromRGB("FFDDDD");
			
		}
		
		@Override
		public String getText(Object element){
			if (invoiceCorrectionDTO != null) {
				int i = invoiceCorrectionDTO.getHistory().indexOf(element);
				return String.valueOf(++i) + ".   " + ((InvoiceHistoryEntryDTO) element).getText();
			}
			return "";
		}
	}
}
