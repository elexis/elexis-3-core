package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.services.IOrderService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.ui.e4.providers.IdentifiableLabelProvider;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.TimeTool;

public class DailyConsumptionOrderDialog extends TitleAreaDialog {

	private CDateTime dtDate;
	private TableViewer tableViewer;
	private IOrder currOrder;
	private TimeTool selectedDate;
	private Map<IArticle, Integer> diffEntries = new LinkedHashMap<>();

	private List<IMandator> limitationList = new ArrayList<>();
	private Map<IArticle, Integer> tempEntries = new LinkedHashMap<>();

	private final IOrderService orderService;

	public DailyConsumptionOrderDialog(Shell parentShell, IOrder currOrder) {
		super(parentShell);
		this.currOrder = currOrder;
		this.orderService = OrderServiceHolder.get();
	}


	@Override
	public void create() {
		super.create();
		setTitle(Messages.DailyOrderDialog_Title);
		setMessage(Messages.DailyOrderDialog_Message);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		area.setLayout(new GridLayout(1, false));

		Composite dateComposite = new Composite(area, SWT.NONE);
		dateComposite.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		dateComposite.setLayout(new GridLayout(2, false));

		dtDate = new CDateTime(dateComposite, CDT.DATE_MEDIUM | CDT.DROP_DOWN | SWT.BORDER);
		dtDate.setSelection(new Date());
		selectedDate = new TimeTool(dtDate.getSelection());
		dtDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedDate = new TimeTool(dtDate.getSelection());
				updateTempEntries();
				diffEntries.clear();
				diffEntries = orderService.calculateDailyDifferences(selectedDate.toLocalDate(), limitationList);
				tableViewer.setInput(diffEntries.entrySet().stream().filter(entry -> entry.getValue() != 0)
						.collect(Collectors.toList()));
			}
		});

		Button btnFilterMandators = new Button(dateComposite, SWT.CHECK);
		btnFilterMandators.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnFilterMandators.setText("nur von folgenden Mandanten ...");
		btnFilterMandators.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				limitationList.clear();
				btnFilterMandators.setText("nur von folgenden Mandanten ...");
				if (btnFilterMandators.getSelection()) {
					ListSelectionDialog lsd = new ListSelectionDialog(Display.getDefault().getActiveShell(),
							CoreModelServiceHolder.get().getQuery(IMandator.class).execute(),
							ArrayContentProvider.getInstance(), IdentifiableLabelProvider.getInstance(),
							Messages.DailyOrderMandant);
					int open = lsd.open();
					if (open == Dialog.OK) {
						limitationList.addAll(Arrays.asList(lsd.getResult()).stream().map(m -> (IMandator) m)
								.collect(Collectors.toList()));
						String label = Messages.DailyOrderMandantOnlyFollowing
								+ limitationList.stream().map(m -> m.getLabel()).reduce((u, t) -> u + ", " + t)
										.orElse(Messages.DailyOrderMandantNone);
						btnFilterMandators.setText(label);
						updateTempEntries();
						tableViewer.setInput(tempEntries.entrySet());
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

		TableViewerColumn colAmount = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcAmount = colAmount.getColumn();
		tcAmount.setText(Messages.Core_Number);
		tcLayout.setColumnData(tcAmount, new ColumnPixelData(70, false));
		colAmount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Map.Entry<?, ?> entry) {
					return String.valueOf(entry.getValue());
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getForeground(Object element) {
				if (element instanceof Map.Entry<?, ?> entry) {
					int value = (Integer) entry.getValue();
					if (value < 0) {
						return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
					}
				}
				return super.getForeground(element);
			}
		});

		TableViewerColumn colArticle = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tcArticle = colArticle.getColumn();
		tcArticle.setText(Messages.Core_Article);
		tcLayout.setColumnData(tcArticle, new ColumnPixelData(300, true));
		colArticle.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Map.Entry<?, ?> entry && entry.getKey() instanceof IArticle art) {
					return art.getLabel();
				}
				return StringUtils.EMPTY;
			}
		});

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		updateTempEntries();


		diffEntries.clear();
		diffEntries = orderService.calculateDailyDifferences(selectedDate.toLocalDate(), limitationList);

		tableViewer.setInput(
				diffEntries.entrySet().stream().filter(entry -> entry.getValue() != 0).collect(Collectors.toList()));

		return area;
	}

	private void updateTempEntries() {
		tempEntries.clear();
		tempEntries.putAll(orderService.calculateDailyConsumption(selectedDate.toLocalDate(), limitationList));
	}

	@Override
	protected void okPressed() {
		Optional<IMandator> mandator = ContextServiceHolder.get().getActiveMandator();
		if (orderService != null) {
			Map<IArticle, Integer> toCreate = diffEntries.entrySet().stream().filter(e -> e.getValue() > 0)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
			Map<IArticle, Integer> toReduce = diffEntries.entrySet().stream().filter(e -> e.getValue() < 0)
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
			List<IOrder> relevantOrders = orderService.findOrderByDate(selectedDate.toLocalDate());
			orderService.createOrderEntries(relevantOrders, currOrder, toCreate, mandator.orElse(null));
			toReduce.forEach((article, amount) -> {
				orderService.reduceOpenEntries(relevantOrders, article, -amount);
			});
		}
		super.okPressed();
	}


	public IOrder getOrder() {
		return currOrder;
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}
}
