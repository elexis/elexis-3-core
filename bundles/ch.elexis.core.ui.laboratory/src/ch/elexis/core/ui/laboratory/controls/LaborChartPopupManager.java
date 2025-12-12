package ch.elexis.core.ui.laboratory.controls;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.DoubleStream;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.ICustomPaintListener;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.interfaces.ILabItem;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.ui.laboratory.controls.model.LaborItemResults;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

public class LaborChartPopupManager {
	private Shell chartPopup;
	private Chart chart;
	private Patient actPatient;
	private Font legendFont;
	private List<Font> fonts = new ArrayList<>();

	public LaborChartPopupManager(Patient patient) {
		this.actPatient = patient;
	}

	public void createChartPopup(TreeItem item, MouseEvent e, TreeViewer viewer, Patient actPatient) {
		this.actPatient = actPatient;
		Shell parentShell = viewer.getControl().getShell();
		LaborItemResults laborItemResults = (LaborItemResults) item.getData();
		ILabItem labItem = laborItemResults.getLabItem();
		if (labItem.getTyp().equals(LabItemTyp.DOCUMENT)) {
			return;
		}
		disposeResources();
		chartPopup = new Shell(parentShell, SWT.NO_TRIM | SWT.TOOL);
		setupChartPopupLayout();
		chart = new Chart(chartPopup, SWT.NONE);
		setupChartLayout(chart);
		List<LabResult> labResults = new ArrayList<>(laborItemResults.getAllResults());
		List<LabResult> lastSevenResults = getLastYearResults(labResults);
		configureChart(chart, lastSevenResults);
		chartPopup.setSize(700, 300);
		Point location = adjustPopupLocation(viewer.getControl().toDisplay(e.x, e.y), parentShell,
				chartPopup.getSize().x, chartPopup.getSize().y);
		chartPopup.setLocation(location.x, location.y);
		chartPopup.open();
		addDisposeListener();
	}


	public Chart createChart(Composite parent, List<LabResult> labResults, Patient patient) {
		this.actPatient = patient;
		chart = new Chart(parent, SWT.NONE);
		setupChartLayout(chart);
		configureChart(chart, labResults);
		if (!labResults.isEmpty()) {
			String firstElement = labResults.get(0).toString();
			String title = firstElement.replaceAll("\\(.*", "").trim();
			chart.getTitle().setText(title);
		}
		chart.getLegend().setVisible(false);
		chart.getLegend().setPosition(SWT.RIGHT);
		addDisposeListener();
		return chart;
	}

	private void setupChartPopupLayout() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		chartPopup.setLayout(gridLayout);
	}

	public void setupChartLayout(Chart chart) {
		chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		chart.getTitle().setText("");
		chart.getAxisSet().getXAxis(0).getTitle().setText("");
		chart.getAxisSet().getYAxis(0).getTitle().setText("");
	}

	private void addDisposeListener() {
		if (chartPopup != null && !chartPopup.isDisposed()) {
			chartPopup.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					disposeResources();
				}
			});
		}

		if (chart != null && !chart.isDisposed()) {
			chart.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					disposeResources();
				}
			});
		}
	}


	private void disposeResources() {
		for (Font font : fonts) {
			if (font != null && !font.isDisposed()) {
				font.dispose();
			}
		}
		fonts.clear();
		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
		}
		if (chartPopup != null && !chartPopup.isDisposed()) {
			chartPopup.dispose();
		}
	}

	public void configureChart(Chart chart, List<LabResult> labResults) {
		labResults.sort(Comparator.comparing(result -> result.getObservationTime().getTime()));
		List<Date> xSeriesList = new ArrayList<>();
		List<Double> ySeriesList = new ArrayList<>();
		List<String> specialValuesList = new ArrayList<>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		processLabResults(labResults, xSeriesList, ySeriesList, specialValuesList);
		double[] xSeries = xSeriesList.stream().mapToDouble(Date::getTime).toArray();
		double[] ySeries = ySeriesList.stream().mapToDouble(Double::doubleValue).toArray();
		String seriesName = getSeriesName(labResults);
		ILineSeries lineSeries = createLineSeries(chart, seriesName, xSeries, ySeries);
		double[] referenceRange = getReferenceRange(labResults);
		double[] axisRanges = adjustAxisRanges(chart, xSeries, ySeries, referenceRange, specialValuesList, dateFormat);
		plotAnnotations(chart, xSeries, ySeries, specialValuesList, referenceRange, axisRanges);
		plotDataPoints(chart, xSeries, ySeries, specialValuesList, referenceRange, labResults);
		chart.redraw();
	}

	private void plotDataPoints(Chart chart, double[] xSeries, double[] ySeries, List<String> specialValuesList,
			double[] referenceRange, List<LabResult> labResults) {
		for (int i = 0; i < xSeries.length; i++) {
			double x = xSeries[i];
			double y = ySeries[i];
			LabResult labResult = labResults.get(i);
			String pointLabel = String.format("%.1f (%s)", y,
					labResult.getObservationTime().toString(TimeTool.DATE_GER));
			ILineSeries pointSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, pointLabel);
			pointSeries.setYSeries(new double[] { y });
			pointSeries.setXSeries(new double[] { x });
			pointSeries.setLineStyle(LineStyle.NONE);
			String specialValue = specialValuesList.get(i);
			int symbolSize = 4;
			pointSeries.setSymbolSize(symbolSize);
			Color symbolColor;
			if (specialValue.equals("<")) {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_DARK_MAGENTA);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.INVERTED_TRIANGLE);
				pointSeries.setSymbolSize(6);
			} else if (specialValue.equals(">")) {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_DARK_CYAN);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.TRIANGLE);
				pointSeries.setSymbolSize(7);
			} else if (specialValue.equals("<=")) {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_BLUE);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.DIAMOND);
			} else if (specialValue.equals(">=")) {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_RED);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.SQUARE);
			} else if (y < referenceRange[0] || y > referenceRange[1]) {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_RED);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.CIRCLE);
			} else {
				symbolColor = chart.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
				pointSeries.setSymbolType(ILineSeries.PlotSymbolType.CIRCLE);
			}
			pointSeries.setSymbolColor(symbolColor);
		}
		legendFont = new Font(chart.getDisplay(), "Arial", 8, SWT.NORMAL);
		fonts.add(legendFont);
		chart.getLegend().setFont(legendFont);
		chart.redraw();
	}

	private List<LabResult> getLastYearResults(List<LabResult> labResults) {
		if (labResults.isEmpty()) {
			return new ArrayList<>();
		}
		int anzahlMonate = Integer.parseInt(ConfigServiceHolder.getUser(Preferences.LABSETTINGS_ANZAHL_MONATE, "12"));
		labResults.sort(Comparator.comparing(result -> result.getObservationTime().getTime()));
		Date lastResultDate = labResults.get(labResults.size() - 1).getObservationTime().getTime();
	    Calendar calendar = Calendar.getInstance();
		calendar.setTime(lastResultDate);
		calendar.add(Calendar.MONTH, -anzahlMonate);
	    Date oneYearAgo = calendar.getTime();
		List<LabResult> filteredResults = new ArrayList<>();
	    for (LabResult result : labResults) {
			if (result.getObservationTime().getTime().after(oneYearAgo)) {
	            filteredResults.add(result);
	        }
	    }
	    return filteredResults;
	}

	private void processLabResults(List<LabResult> results, List<Date> xSeriesList, List<Double> ySeriesList,
			List<String> specialValuesList) {
		for (LabResult result : results) {
			try {
				TimeTool observationTime = result.getObservationTime();
				Date date = observationTime.getTime();
				xSeriesList.add(date);
				String resultValue = result.getResult();
				String specialValue = getSpecialValue(resultValue);
				double yValue = 0.0;

				if (resultValue != null && !resultValue.isEmpty()) {
					if (specialValue.isEmpty()) {
						yValue = Double.parseDouble(resultValue);
					} else {
						String cleanedValue = resultValue.replaceAll("[^0-9.]", "").trim();
						if (!cleanedValue.isEmpty()) {
							yValue = Double.parseDouble(cleanedValue);
						}
					}
				}
				ySeriesList.add(yValue);
				specialValuesList.add(specialValue);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	private String getSpecialValue(String resultValue) {
		if (resultValue.startsWith("<")) {
			return "<";
		} else if (resultValue.startsWith(">")) {
			return ">";
		} else if (resultValue.startsWith("<=")) {
			return "<=";
		} else if (resultValue.startsWith(">=")) {
			return ">=";
		}
		return "";
	}

	private String getSeriesName(List<LabResult> results) {
		return results.isEmpty() ? "Parameterwert" : results.get(0).getItem().getName();
	}

	private ILineSeries createLineSeries(Chart chart, String seriesName, double[] xSeries, double[] ySeries) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, seriesName);
		lineSeries.setYSeries(ySeries);
		lineSeries.setXSeries(xSeries);
		lineSeries.setLineStyle(LineStyle.SOLID);
		lineSeries.setSymbolType(ILineSeries.PlotSymbolType.NONE);
		return lineSeries;
	}

	private double[] getReferenceRange(List<LabResult> results) {
		double[] referenceRange = new double[2];
		try {
			String[] referenceValues = "w".equals(actPatient.getGeschlecht())
					? results.get(0).getItem().getReferenceFemale().split("-")
					: results.get(0).getItem().getReferenceMale().split("-");
			referenceRange[0] = Double.parseDouble(referenceValues[0]);
			referenceRange[1] = Double.parseDouble(referenceValues[1]);
		} catch (Exception ex) {
			referenceRange[0] = Double.MIN_VALUE;
			referenceRange[1] = Double.MAX_VALUE;
		}
		return referenceRange;
	}

	private double[] adjustAxisRanges(Chart chart, double[] xSeries, double[] ySeries, double[] referenceRange,
			List<String> specialValuesList, SimpleDateFormat dateFormat) {
		if (xSeries.length == 0 || ySeries.length == 0) {
			double[] defaultRange = { 0, 1, 0, 1 };
			chart.getAxisSet().getXAxis(0).setRange(new Range(defaultRange[0], defaultRange[1]));
			chart.getAxisSet().getYAxis(0).setRange(new Range(defaultRange[2], defaultRange[3]));
			return defaultRange;
		}
		double xMin = DoubleStream.of(xSeries).min().orElse(0);
		double xMax = DoubleStream.of(xSeries).max().orElse(1);
		double yMin = getYMin(ySeries, referenceRange);
		double yMax = getYMax(ySeries, referenceRange);
		if (xMin == xMax) {
			xMin -= 1;
			xMax += 1;
		}
		if (yMin == yMax) {
			yMin -= 1;
			yMax += 1;
		}
		double xRange = xMax - xMin;
		double yRange = yMax - yMin;
		double xMinAdjusted = adjustMin(xMin, xRange);
		double xMaxAdjusted = adjustMax(xMax, xRange);
		double yMinAdjusted = adjustMin(yMin, yRange);
		double yMaxAdjusted = adjustMax(yMax, yRange);
		if (Double.isInfinite(xMinAdjusted) || Double.isNaN(xMinAdjusted))
			xMinAdjusted = 0;
		if (Double.isInfinite(xMaxAdjusted) || Double.isNaN(xMaxAdjusted))
			xMaxAdjusted = 1;
		if (Double.isInfinite(yMinAdjusted) || Double.isNaN(yMinAdjusted))
			yMinAdjusted = 0;
		if (Double.isInfinite(yMaxAdjusted) || Double.isNaN(yMaxAdjusted))
			yMaxAdjusted = 1;
		final double[] xMinAdjustedArr = { xMinAdjusted };
		final double[] xMaxAdjustedArr = { xMaxAdjusted };
		final double[] yMinAdjustedArr = { yMinAdjusted };
		final double[] yMaxAdjustedArr = { yMaxAdjusted };
		setupChartAxes(chart, yMinAdjustedArr, yMaxAdjustedArr, xMinAdjustedArr, xMaxAdjustedArr, dateFormat,
				specialValuesList);
		return new double[] { xMinAdjusted, xMaxAdjusted, yMinAdjusted, yMaxAdjusted };
	}

	private double getYMin(double[] ySeries, double[] referenceRange) {
		double yMin = DoubleStream.of(ySeries).min().orElse(0);
		double referenceRangeSize = referenceRange[1] - referenceRange[0];
		if (referenceRangeSize == Double.MAX_VALUE - Double.MIN_VALUE) {
			return yMin;
		}
		if (yMin >= referenceRange[0]) {
			yMin = referenceRange[0] - 0.2 * referenceRangeSize;
		} else {
			yMin = yMin - 0.15 * (DoubleStream.of(ySeries).max().orElse(referenceRange[1]) - yMin);
		}
		return yMin;
	}

	private double getYMax(double[] ySeries, double[] referenceRange) {
		double yMax = DoubleStream.of(ySeries).max().orElse(0);
		double referenceRangeSize = referenceRange[1] - referenceRange[0];
		if (referenceRangeSize == Double.MAX_VALUE - Double.MIN_VALUE) {
			return yMax;
		}
		if (yMax <= referenceRange[1]) {
			yMax = referenceRange[1] + 0.2 * referenceRangeSize;
		} else {
			yMax = yMax + 0.15 * (yMax - DoubleStream.of(ySeries).min().orElse(referenceRange[0]));
		}
		return yMax;
	}

	private double adjustMin(double min, double range) {
		double adjusted = min - 0.15 * range;
		return adjusted == min ? adjusted - 1 : adjusted;
	}

	private double adjustMax(double max, double range) {
		double adjusted = max + 0.15 * range;
		return adjusted == max ? adjusted + 1 : adjusted;
	}

	private void setupChartAxes(Chart chart, double[] yMinAdjustedArr, double[] yMaxAdjustedArr,
			double[] xMinAdjustedArr, double[] xMaxAdjustedArr, SimpleDateFormat dateFormat,
			List<String> specialValuesList) {
		chart.getLegend().setVisible(false);
		chart.getAxisSet().getYAxis(0).getTitle().setText("");
		chart.getAxisSet().getXAxis(0).getTitle().setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getYAxis(0).getTitle().setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getXAxis(0).getTick().setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getYAxis(0).getTick().setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		chart.getAxisSet().getXAxis(0).getTick().setFormat(new Format() {
			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
				long value = ((Number) obj).longValue();
				Date date = new Date(value);
				return dateFormat.format(date, toAppendTo, pos);
			}
			@Override
			public Object parseObject(String source, ParsePosition pos) {
				return null;
			}
		});
		chart.getAxisSet().getXAxis(0).setRange(new Range(xMinAdjustedArr[0], xMaxAdjustedArr[0]));
		chart.getAxisSet().getYAxis(0).setRange(new Range(yMinAdjustedArr[0], yMaxAdjustedArr[0]));
		FontData[] fontData = chart.getTitle().getFont().getFontData();
		for (FontData fd : fontData) {
			fd.setHeight(8);
		}
		Font newFont = new Font(chart.getDisplay(), fontData);
		chart.getTitle().setFont(newFont);
		fonts.add(newFont);
		chart.getTitle().setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}

	private void plotAnnotations(Chart chart, double[] xSeries, double[] ySeries, List<String> specialValuesList,
			double[] referenceRange, double[] axisRanges) {
		chart.getPlotArea().addCustomPaintListener(new ICustomPaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				gc.setFont(chart.getFont());
				IAxis xAxis = chart.getAxisSet().getXAxis(0);
				IAxis yAxis = chart.getAxisSet().getYAxis(0);
				double xMinAdjusted = axisRanges[0];
				double xMaxAdjusted = axisRanges[1];
				plotReferenceRange(gc, xAxis, yAxis, referenceRange, new double[] { xMinAdjusted },
						new double[] { xMaxAdjusted });
				plotDataAnnotations(gc, xSeries, ySeries, specialValuesList, xAxis, yAxis);
			}
		});
	}

	private void plotReferenceRange(GC gc, IAxis xAxis, IAxis yAxis, double[] referenceRange, double[] xMinAdjustedArr,
			double[] xMaxAdjustedArr) {
		if (referenceRange[0] != Double.MIN_VALUE && referenceRange[1] != Double.MAX_VALUE) {
			int yMinPixel = yAxis.getPixelCoordinate(referenceRange[0]);
			int yMaxPixel = yAxis.getPixelCoordinate(referenceRange[1]);
			gc.setBackground(chart.getDisplay().getSystemColor(SWT.COLOR_GREEN));
			gc.setAlpha(50);
			gc.fillRectangle(xAxis.getPixelCoordinate(xMinAdjustedArr[0]), yMaxPixel,
					xAxis.getPixelCoordinate(xMaxAdjustedArr[0]) - xAxis.getPixelCoordinate(xMinAdjustedArr[0]),
					yMinPixel - yMaxPixel);
			gc.setAlpha(255);
		} else {

		}
	}

	private void plotDataAnnotations(GC gc, double[] xSeries, double[] ySeries, List<String> specialValuesList,
			IAxis xAxis, IAxis yAxis) {
		boolean lastWasLeft = false;
		for (int i = 0; i < xSeries.length; i++) {
			double x = xSeries[i];
			double y = ySeries[i];
			String annotation = getAnnotation(y, specialValuesList.get(i));
			int xPixel = xAxis.getPixelCoordinate(x);
			int yPixel = yAxis.getPixelCoordinate(y);
			if (i > 0 && Math.abs(ySeries[i] - ySeries[i - 1]) < 1e-6) {
				if (lastWasLeft) {
					gc.drawText(annotation, xPixel + 10, yPixel - 10, true);
					lastWasLeft = false;
				} else {
					gc.drawText(annotation, xPixel - gc.textExtent(annotation).x - 10, yPixel - 10, true);
					lastWasLeft = true;
				}
			} else {
				if (i > 0 && ySeries[i] < ySeries[i - 1]) {
					gc.drawText(annotation, xPixel + 10, yPixel - 10, true);
					lastWasLeft = false;
				} else {
					gc.drawText(annotation, xPixel - gc.textExtent(annotation).x - 10, yPixel - 10, true);
					lastWasLeft = true;
				}
			}
			if (!specialValuesList.get(i).isEmpty()) {
				Font originalFont = gc.getFont();
				Font boldFont = new Font(gc.getDevice(), originalFont.getFontData()[0].getName(),
						originalFont.getFontData()[0].getHeight(), SWT.BOLD);
				gc.setFont(boldFont);
				gc.setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_RED));
				gc.setFont(originalFont);
				boldFont.dispose();
				gc.setForeground(chart.getDisplay().getSystemColor(SWT.COLOR_BLACK));
			}
		}
	}

	private String getAnnotation(double y, String specialValue) {
		if (specialValue.equals("<")) {
			return String.format("< %.2f", y);
		} else if (specialValue.equals(">")) {
			return String.format("> %.2f", y);
		} else if (specialValue.equals("<=")) {
			return String.format("<= %.2f", y);
		} else if (specialValue.equals(">=")) {
			return String.format(">= %.2f", y);
		}
		return String.format("%.2f", y);
	}

	public boolean dispose() {
		if (chartPopup != null && !chartPopup.isDisposed()) {
			chartPopup.dispose();
			chartPopup = null;
		}
		if (chart != null && !chart.isDisposed()) {
			chart.dispose();
			chart = null;
		}
		if (legendFont != null && !legendFont.isDisposed()) {
			legendFont.dispose();
			legendFont = null;
		}
		return false;
	}

	private Point adjustPopupLocation(Point location, Shell parentShell, int popupWidth, int popupHeight) {
		Rectangle parentShellBounds = parentShell.getBounds();
		location.y = adjustPopupLocationY(location.y + 30, parentShellBounds, popupHeight);
		location.x = adjustPopupLocationX(location.x + 30, parentShellBounds, popupWidth);
		return location;
	}

	private int adjustPopupLocationY(int y, Rectangle parentShellBounds, int popupHeight) {
		if (y + popupHeight > parentShellBounds.y + parentShellBounds.height) {
			return y - popupHeight - 20;
		}
		return y + 20;
	}

	private int adjustPopupLocationX(int x, Rectangle parentShellBounds, int popupWidth) {
		if (x + popupWidth > parentShellBounds.x + parentShellBounds.width) {
			return x - popupWidth - 20;
		}
		return x + 20;
	}
}
