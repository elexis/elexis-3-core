package ch.elexis.core.ui.reminder.part.nattable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.ISpanningDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.cell.DataCell;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IReminder;

public class ReminderBodyDataProvider implements ISpanningDataProvider {

	private Object[][] dataMatrix;

	private int yOffFuture;

	private List<ReminderColumn> columns;

	@Override
	public int getColumnCount() {
		if (dataMatrix != null) {
			return dataMatrix[0].length;
		}
		return 0;
	}

	public Object getData(int columnIndex, int rowIndex) {
		if (columnIndex >= 0 && rowIndex >= 0) {
			Object data = dataMatrix[rowIndex][columnIndex];
			if (data instanceof Integer) {
				return dataMatrix[rowIndex - ((Integer) data)][columnIndex];
			}
			return dataMatrix[rowIndex][columnIndex];
		}
		return null;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		Object object = dataMatrix[rowIndex][columnIndex];
		if(object instanceof IReminder) {
			if (rowIndex > yOffFuture) {
				return RemiderRichTextUtil.richText((IReminder) object, true);
			}
			return RemiderRichTextUtil.richText((IReminder) object, true);
		} else if (object instanceof String) {
			if (rowIndex == 0) {
				return RemiderRichTextUtil.richText((String) object, 2);
			}
			return RemiderRichTextUtil.richText((String) object);
		}
		return null;
	}

	@Override
	public int getRowCount() {
		if(dataMatrix != null) {
			return dataMatrix.length;
		}
		return 0;
	}

	@Override
	public void setDataValue(int arg0, int arg1, Object arg2) {
		throw new IllegalStateException("Edit data not supported");
	}

	public void reload() {
		if (columns == null || columns.isEmpty()) {
			dataMatrix = null;
			return;
		}
		LoggerFactory.getLogger(getClass()).info("RELOAD START");
		List<Map<String, List<IReminder>>> dataListMap = new ArrayList<>();
		Map<String, Integer> maxMap = new HashMap<>();
		for (ReminderColumn reminderColumn : columns) {
			List<IReminder> allReminders = reminderColumn.loadReminders();
			List<IReminder> overDueReminders = getOverDue(allReminders);
			List<IReminder> todayReminders = getToday(allReminders);
			List<IReminder> tomorrowReminders = getTomorrow(allReminders);
			List<IReminder> futureReminders = getFuture(allReminders);
			List<IReminder> noDueReminders = getNoDue(allReminders);
			dataListMap.add(Map.of("overdue", overDueReminders, "today", todayReminders, "tomorrow", tomorrowReminders,
					"future", futureReminders, "nodue", noDueReminders));

			maxMap.put("overdue", Math.max(maxMap.getOrDefault("overdue", 0),
					overDueReminders.size()));
			maxMap.put("today", Math.max(maxMap.getOrDefault("today", 0),
					todayReminders.size()));
			maxMap.put("tomorrow", Math.max(maxMap.getOrDefault("tomorrow", 0),
					tomorrowReminders.size()));
			maxMap.put("future", Math.max(maxMap.getOrDefault("future", 0),
					futureReminders.size()));
			maxMap.put("nodue", Math.max(maxMap.getOrDefault("nodue", 0),
					noDueReminders.size()));
		}
		
		dataMatrix = new Object[2 + maxMap.get("overdue") + 1 + maxMap.get("today") + 1 + maxMap.get("tomorrow") + 1
				+ maxMap.get("future") + 1 + maxMap.get("nodue")][columns.size()];
		addColumnHeaderToMatrix(columns.stream().map(c -> c.getName()).toList().toArray(new String[columns.size()]));
		int yOff = 1;
		addHeaderToMatrix(0, yOff, "Überfällig");
		yOff += 1;
		for (int i = 0; i < columns.size(); i++) {
			addListToMatrix(i, yOff, dataListMap.get(i).get("overdue"));
		}
		yOff += maxMap.get("overdue");

		addHeaderToMatrix(0, yOff, "Heute");
		yOff += 1;
		for (int i = 0; i < columns.size(); i++) {
			addListToMatrix(i, yOff, dataListMap.get(i).get("today"));
		}
		yOff += maxMap.get("today");

		addHeaderToMatrix(0, yOff, "Morgen");
		yOff += 1;
		for (int i = 0; i < columns.size(); i++) {
			addListToMatrix(i, yOff, dataListMap.get(i).get("tomorrow"));
		}
		yOff += maxMap.get("tomorrow");

		yOffFuture = yOff;
		addHeaderToMatrix(0, yOff, "Später");
		yOff += 1;
		for (int i = 0; i < columns.size(); i++) {
			addListToMatrix(i, yOff, dataListMap.get(i).get("future"));
		}
		yOff += maxMap.get("future");

		addHeaderToMatrix(0, yOff, "Ohne Fälligkeit");
		yOff += 1;
		for (int i = 0; i < columns.size(); i++) {
			addListToMatrix(i, yOff, dataListMap.get(i).get("nodue"));
		}
		yOff += maxMap.get("nodue");
		LoggerFactory.getLogger(getClass()).info("RELOAD DONE");
	}

	private void addColumnHeaderToMatrix(String[] headers) {
		dataMatrix[0] = headers;
	}

	private void addHeaderToMatrix(int xOff, int yOff, String string) {
		dataMatrix[yOff][xOff] = string;
	}

	private void addListToMatrix(int xOff, int yOff, List<IReminder> list) {
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				dataMatrix[i + yOff][xOff] = list.get(i);
			}
		}
	}

	private List<IReminder> getNoDue(List<IReminder> all) {
		return all.stream().filter(r -> r.getDue() == null).toList();
	}

	private List<IReminder> getFuture(List<IReminder> all) {
		return all.stream().filter(r -> r.getDue() != null && r.getDue().isAfter(LocalDate.now().plusDays(1))).toList();
	}

	private List<IReminder> getTomorrow(List<IReminder> all) {
		return all.stream().filter(r -> r.getDue() != null && r.getDue().isEqual(LocalDate.now().plusDays(1))).toList();
	}

	private List<IReminder> getToday(List<IReminder> all) {
		return all.stream().filter(r -> r.getDue() != null && r.getDue().isEqual(LocalDate.now())).toList();
	}

	private List<IReminder> getOverDue(List<IReminder> all) {
		return all.stream().filter(r -> r.getDue() != null && r.getDue().isBefore(LocalDate.now())).toList();
	}

	public void setColumns(List<ReminderColumn> columns) {
		this.columns = columns;
	}

	public List<ReminderColumn> getColumns() {
		return columns;
	}

	@Override
	public DataCell getCellByPosition(int columnIndex, int rowIndex) {
		Object data = dataMatrix[rowIndex][columnIndex];

		int columnsSpan = rowIndex > 0 ? getColumnsSpan(data) : 1;

		return new DataCell(columnIndex, rowIndex, columnsSpan, 1);
	}

	private int getColumnsSpan(Object data) {
		if (data instanceof String) {
			return columns.size();
		}
		return 1;
	}
}
