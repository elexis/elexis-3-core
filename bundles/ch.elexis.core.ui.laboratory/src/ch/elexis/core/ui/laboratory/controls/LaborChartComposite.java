package ch.elexis.core.ui.laboratory.controls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtchart.Chart;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.core.ui.laboratory.views.LaborView;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborChartComposite extends Composite {

	public static final String ID = "ch.elexis.LaborResultsComposite"; //$NON-NLS-1$

	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	private Patient actPatient;
	private LaborView parentLaborView;
	private LaborChartPopupManager laborChartPopupManager;
	private List<Chart> charts = new ArrayList<>();
	public TreeViewer viewer;
	private DateTime fromDate;
	private DateTime toDate;
	private Button updateChartsButton;
	private List<TreeItem> selectedItems = new ArrayList<>();
	private Composite body;
	private Composite chartsComposite;
	private Button legendCheckbox;

	public LaborChartComposite(Composite parent, int style, LaborView parentLaborView) {
		super(parent, style);
		this.parentLaborView = parentLaborView;
		laborChartPopupManager = new LaborChartPopupManager(actPatient);
		createContent();
	}

	public Composite getChartsComposite() {
		return chartsComposite;
	}

	private void createContent() {
		setLayout(new GridLayout(1, false));
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		body = form.getBody();
		body.setLayout(new GridLayout(1, false));
		Composite topComposite = new Composite(body, SWT.NONE);
		GridLayout topLayout = new GridLayout(3, false);
		topComposite.setLayout(topLayout);
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		createCalendarAndButton(topComposite);
		chartsComposite = new Composite(body, SWT.NONE);
		chartsComposite.setLayout(new GridLayout(1, false));
		chartsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		updateCharts(chartsComposite);
	}

	public void selectPatient(Patient patient) {
		actPatient = patient;
		if (!isVisible()) {
			return;
		}
		clearCharts();
		setRedraw(false);
		TimeTool now = new TimeTool();
		setRedraw(true);
	}

	private void createCalendarAndButton(Composite parent) {
		fromDate = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN);
		toDate = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN);
		GridData fromDateGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		fromDate.setLayoutData(fromDateGridData);
		GridData toDateGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		toDate.setLayoutData(toDateGridData);
		fromDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate from = getDate(fromDate);
				LocalDate to = getDate(toDate);
				LocalDate currentDate = LocalDate.now();
				if (from.isAfter(to)) {
					setDate(fromDate, to);
					showMessage(Messages.Error_StartDateAfterEndDate);
				}
				if (to.isAfter(currentDate)) {
					setDate(toDate, currentDate);
					showMessage(Messages.Error_EndDateInFuture);
				}
			}
		});
		toDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				LocalDate from = getDate(fromDate);
				LocalDate to = getDate(toDate);
				LocalDate currentDate = LocalDate.now();
				if (from.isAfter(to)) {
					setDate(fromDate, to);
					showMessage(Messages.Error_StartDateAfterEndDate);
				}
				if (to.isAfter(currentDate)) {
					setDate(toDate, currentDate);
					showMessage(Messages.Error_EndDateInFuture);
				}
			}
		});
		createUpdateChartsButton(parent);
		createLegendCheckbox(parent);
	}

	public void setSelectedItems(List<TreeItem> items) {
		this.selectedItems = items != null ? new ArrayList<>(items) : new ArrayList<>();
	}

	public List<TreeItem> getSelectedItems() {
		return selectedItems;
	}

	private void clearCharts() {
		for (Chart chart : charts) {
			if (chart != null && !chart.isDisposed()) {
				chart.dispose();
			}
		}
		charts.clear();
	}

	private LocalDate getDate(DateTime dateTime) {
		int day = dateTime.getDay();
		int month = dateTime.getMonth();
		int year = dateTime.getYear();
		return LocalDate.of(year, month + 1, day);
	}

	private void setDate(DateTime dateTime, LocalDate date) {
		dateTime.setDate(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
	}

	private void showMessage(String message) {
		MessageBox messageBox = new MessageBox(fromDate.getShell(), SWT.OK | SWT.ICON_INFORMATION);
		messageBox.setMessage(message);
		messageBox.open();
	}

	private void createUpdateChartsButton(Composite parent) {
		updateChartsButton = new Button(parent, SWT.PUSH | SWT.FLAT);
		updateChartsButton.setText(Messages.Button_UpdateCharts);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		updateChartsButton.setLayoutData(gridData);
		updateChartsButton.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				e.gc.setLineWidth(1);
				e.gc.drawRectangle(0, 0, updateChartsButton.getSize().x - 1, updateChartsButton.getSize().y - 1);
			}
		});
		updateChartsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateCharts(chartsComposite);
			}
		});
	}

	private void createLegendCheckbox(Composite parent) {
		legendCheckbox = new Button(parent, SWT.CHECK);
		legendCheckbox.setText(Messages.Checkbox_ShowLegend);
		legendCheckbox.setSelection(true);
		legendCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean showLegend = legendCheckbox.getSelection();
				for (Chart chart : charts) {
					if (chart != null && !chart.isDisposed()) {
						chart.getLegend().setVisible(showLegend);
						chart.redraw();
					}
				}
			}
		});
	}
	public void updateCharts(Composite chartsComposite) {
		selectedItems = parentLaborView.getSelectedItems();
		clearCharts();
		LocalDate from = getDate(fromDate);
		LocalDate to = getDate(toDate);
		LocalDate currentDate = LocalDate.now();
		boolean showAll = from.isEqual(currentDate) && to.isEqual(currentDate);
		int index = 0;
		for (TreeItem item : selectedItems) {
			if (index >= 5) {
				break;
			}
			LaborItemResults laborItemResults = (LaborItemResults) item.getData();
			List<LabResult> labResults = new ArrayList<>(laborItemResults.getAllResults());
			List<LabResult> filteredResults;
			if (showAll) {
				filteredResults = labResults;
			} else {
				filteredResults = filterResultsByDateRange(labResults, from, to);
			}
			if (!filteredResults.isEmpty()) {
				Chart chart = laborChartPopupManager.createChart(chartsComposite, filteredResults, actPatient);
				chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				chart.getLegend().setVisible(legendCheckbox.getSelection());
				charts.add(chart);
				index++;
			}
		}
		body.layout(true, true);
		for (Chart chart : charts) {
			chart.redraw();
		}
	}

	private List<LabResult> filterResultsByDateRange(List<LabResult> labResults, LocalDate from, LocalDate to) {
		List<LabResult> filteredResults = new ArrayList<>();
		for (LabResult result : labResults) {
			TimeTool resultDateTool = result.getAnalyseTime();
			if (resultDateTool == null) {
				resultDateTool = result.getObservationTime();
				if (resultDateTool == null) {
					resultDateTool = result.getTransmissionTime();
				}
			}
			if (resultDateTool != null) {
				LocalDate resultDate = LocalDate.of(resultDateTool.get(TimeTool.YEAR),
						resultDateTool.get(TimeTool.MONTH) + 1, resultDateTool.get(TimeTool.DAY_OF_MONTH));
				if ((resultDate.isEqual(from) || resultDate.isAfter(from))
						&& (resultDate.isEqual(to) || resultDate.isBefore(to))) {
					filteredResults.add(result);
				}
			}
		}
		return filteredResults;
	}
}
