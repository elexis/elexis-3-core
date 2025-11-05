package ch.elexis.core.ui.views.reminder.viewers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnType.ReminderColorType;

/**
 * Factory class responsible for creating and configuring columns in the
 * {@link TableViewer} of the Reminder View.
 * <p>
 * Each column represents a specific aspect of a reminder (type, date,
 * responsible user, status, patient, description, etc.) and provides custom
 * rendering (labels, icons, colors, and fonts) via individual
 * {@link ColumnLabelProvider} implementations.
 * </p>
 *
 * <p>
 * The factory also supports:
 * <ul>
 * <li>Dynamic hiding of columns based on user preferences</li>
 * <li>Automatic resizing of the last column to fit available space</li>
 * <li>Sorting of table data via {@link ReminderComparator}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Colors and icons are used to visually highlight the status of each reminder,
 * such as overdue, in progress, due today, or closed.
 * </p>
 *
 * <p>
 * This class is typically used by {@code ReminderView} during initialization to
 * build and configure the reminder table.
 * </p>
 *
 * @author Dalibor Aksic
 * @since 2025
 */
public class ReminderColumnFactory {

	private final Font boldFont;

	/**
	 * Constructs a new {@code ReminderColumnFactory}.
	 *
	 * @param boldFont the font to be used for high-priority reminder rows
	 */
	public ReminderColumnFactory(Font boldFont) {
		this.boldFont = boldFont;
	}

	/**
	 * Creates and adds the specified columns to the provided {@link TableViewer}.
	 * <p>
	 * Columns that are marked as hidden in user preferences will be skipped. After
	 * creation, the last visible column automatically expands to fill remaining
	 * horizontal space.
	 * </p>
	 *
	 * @param viewer the table viewer to which the columns are added
	 * @param types  the column types to create, defined in
	 *               {@link ReminderColumnType}
	 */
	public void createColumns(TableViewer viewer, ReminderColumnType... types) {
	    String hiddenPref = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_COLUMNS_HIDDEN, "");
	    Set<String> hiddenCols = Arrays.stream(hiddenPref.split(","))
	            .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
	    int index = 0;
	    for (ReminderColumnType type : types) {
			String header = type.getTitle();

			if (hiddenCols.contains(header)) {
				continue;
			}

	        TableViewerColumn col = switch (type) {
	            case TYPE -> createTypeColumn(viewer, index);
	            case DATE -> createDateColumn(viewer, index);
	            case RESPONSIBLE -> createResponsibleColumn(viewer, index);
	            case STATUS -> createStatusColumn(viewer, index);
	            case PATIENT -> createPatientColumn(viewer, index);
			case DESCRIPTION -> createDescriptionColumn(viewer, index);
	        };

	        TableColumn column = col.getColumn();
			column.setResizable(true);
			column.setMoveable(true);
			column.setData("hidden", false);

	        index++;
	    }

	    viewer.getTable().addListener(SWT.Resize, e -> {
	        var table = viewer.getTable();
	        if (table.getColumnCount() > 0) {
	            int totalWidth = table.getClientArea().width;
	            int fixedWidth = 0;
	            for (int i = 0; i < table.getColumnCount() - 1; i++) {
	                fixedWidth += table.getColumn(i).getWidth();
	            }
	            int remaining = Math.max(100, totalWidth - fixedWidth);
	            table.getColumn(table.getColumnCount() - 1).setWidth(remaining);
	        }
	    });
	}


	// ====================== COLUMNS ======================

	/**
	 * Creates the "Type" column which displays an icon representing the reminder
	 * type.
	 */
	private TableViewerColumn createTypeColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setWidth(ReminderColumnType.TYPE.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof IReminder reminder && reminder.getType() != null) {
					return switch (reminder.getType()) {
					case PRINT, PRINT_DRUG_STICKER -> Images.IMG_PRINTER.getImage();
					case MAKE_APPOINTMENT -> Images.IMG_CALENDAR.getImage();
					case DISPENSE_MEDICATION -> Images.IMG_PILL.getImage();
					case PROCESS_SERVICE_RECORDING -> Images.IMG_MONEY.getImage();
					case CHECK_LAB_RESULT, READ_DOCUMENT -> Images.IMG_EYE_WO_SHADOW.getImage();
					case SEND_DOCUMENT -> Images.IMG_MAIL_SEND.getImage();
					default -> null;
					};
				}
				return null;
			}

			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IReminder reminder && reminder.getType() != null) {
					return reminder.getType().getLocaleText();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Creates the "Date" column which shows the due date of the reminder and colors
	 * rows based on the due state (overdue, due, or open).
	 */
	private TableViewerColumn createDateColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(ReminderColumnType.DATE.getTitle());
		col.getColumn().setWidth(ReminderColumnType.DATE.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

			@Override
			public String getText(Object element) {
				IReminder r = (IReminder) element;
				return r.getDue() != null ? fmt.format(r.getDue()) : StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				if (!(element instanceof IReminder r))
					return null;
				LocalDate now = LocalDate.now();
				LocalDate due = r.getDue();
				if (r.getStatus() != null && r.getStatus().toString().equalsIgnoreCase("IN_PROGRESS")) {
					return ReminderColorType.IN_PROGRESS.getColor();
				}
				if (due != null) {
					if (due.isBefore(now))
						return ReminderColorType.OVERDUE.getColor();
					if (due.isEqual(now))
						return ReminderColorType.DUE.getColor();
					return ReminderColorType.OPEN.getColor();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Creates the "Status" column which displays the localized process status text
	 * and applies color highlighting depending on reminder state.
	 */
	private TableViewerColumn createStatusColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(ReminderColumnType.STATUS.getTitle());
		col.getColumn().setWidth(ReminderColumnType.STATUS.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IReminder r && r.getStatus() != null) {
					return r.getStatus().getLocaleText();
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getForeground(Object element) {
				if (!(element instanceof IReminder r))
					return null;
				LocalDate now = LocalDate.now();
				LocalDate due = r.getDue();
				if (r.getStatus() != null && r.getStatus().toString().equalsIgnoreCase("IN_PROGRESS")) {
					return ReminderColorType.IN_PROGRESS.getColor();
				}
				if (due != null) {
					if (due.isBefore(now))
						return ReminderColorType.OVERDUE.getColor();
					if (due.isEqual(now))
						return ReminderColorType.DUE.getColor();
					return ReminderColorType.OPEN.getColor();
				}
				if (r.getStatus() != null) {
					switch (r.getStatus()) {
					case CLOSED -> {
						return Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
					}
					case OPEN -> {
						return ReminderColorType.OPEN.getColor();
					}
					case ON_HOLD -> {
						return Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW);
					}
					default -> {
						return null;
					}
					}
				}
				return null;
			}

			@Override
			public Color getBackground(Object element) {
				if (element instanceof IReminder r && isClosed(r)) {
					return ReminderColorType.CLOSED.getColor();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Creates the "Patient" column which shows the linked patient's full name.
	 */
	private TableViewerColumn createPatientColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(ReminderColumnType.PATIENT.getTitle());
		col.getColumn().setWidth(ReminderColumnType.PATIENT.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IReminder r && r.getContact() != null) {
					IContact c = r.getContact();
					return c.getDescription1() + StringUtils.SPACE + c.getDescription2();
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				if (element instanceof IReminder r && isClosed(r)) {
					return ReminderColorType.CLOSED.getColor();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Creates the "Description" column which displays the subject or message text.
	 * High-priority reminders are shown in bold.
	 */
	private TableViewerColumn createDescriptionColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(ReminderColumnType.DESCRIPTION.getTitle());
		col.getColumn().setWidth(ReminderColumnType.DESCRIPTION.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IReminder r = (IReminder) element;
				return (r.getSubject() == null || r.getSubject().isEmpty()) ? r.getMessage() : r.getSubject();
			}

			@Override
			public Font getFont(Object element) {
				if (element instanceof IReminder r && r.getPriority() == Priority.HIGH) {
					return boldFont;
				}
				return null;
			}

			@Override
			public Color getBackground(Object element) {
				if (element instanceof IReminder r && isClosed(r)) {
					return ReminderColorType.CLOSED.getColor();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Creates the "Responsible" column which lists all responsible users or
	 * mandators.
	 */
	private TableViewerColumn createResponsibleColumn(TableViewer viewer, int index) {
		TableViewerColumn col = new TableViewerColumn(viewer, SWT.NONE);
		col.getColumn().setText(ReminderColumnType.RESPONSIBLE.getTitle());
		col.getColumn().setWidth(ReminderColumnType.RESPONSIBLE.getDefaultWidth());
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IReminder r) {
					if (r.isResponsibleAll())
						return Messages.Core_All;
					return r.getResponsible().stream()
							.map(c -> c.isMandator()
									? c.getDescription1() + StringUtils.SPACE + c.getDescription2()
									: c.getLabel())
							.reduce((a, b) -> a + " | " + b).orElse(StringUtils.EMPTY);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(Object element) {
				if (element instanceof IReminder r && isClosed(r)) {
					return ReminderColorType.CLOSED.getColor();
				}
				return null;
			}
		});
		col.getColumn().addSelectionListener(createSortSelectionAdapter(viewer, col.getColumn(), index));
		return col;
	}

	/**
	 * Checks whether the given reminder is in {@link ProcessStatus#CLOSED}.
	 *
	 * @param r reminder instance
	 * @return {@code true} if the reminder is closed, {@code false} otherwise
	 */
	private boolean isClosed(IReminder r) {
		return r.getStatus() == ProcessStatus.CLOSED;
	}

	// =====================================================================
	// Sorting support
	// =====================================================================

	/**
	 * Creates a {@link SelectionAdapter} that updates the current sorting column
	 * and direction whenever a table column header is clicked.
	 *
	 * @param viewer the table viewer being sorted
	 * @param column the clicked column
	 * @param index  the column index
	 * @return the configured selection adapter
	 */
	private SelectionAdapter createSortSelectionAdapter(final TableViewer viewer, final TableColumn column, final int index) {
	    return new SelectionAdapter() {
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	            ViewerComparator comp = viewer.getComparator();
	            if (comp instanceof ReminderComparator rc) {
	                rc.setColumn(index);
	                int dir = rc.getDirection();
	                viewer.getTable().setSortColumn(column);
	                viewer.getTable().setSortDirection(dir);
	                viewer.refresh(true);
	            }
	        }
	    };
	}

	/**
	 * Comparator used for sorting {@link IReminder} entries in the reminder table.
	 * <p>
	 * This comparator defines the sorting logic for all reminder table columns,
	 * such as type, date, responsible user, status, patient, and description.
	 * </p>
	 *
	 * <p>
	 * The {@code column} field determines which column is currently sorted, while
	 * the {@code direction} field controls the sort order (ascending or
	 * descending).
	 * </p>
	 *
	 * <p>
	 * Sorting is performed in a null-safe way and never throws an exception. If an
	 * unexpected error occurs, the comparator returns equality (0) to prevent UI
	 * crashes.
	 * </p>
	 *
	 * <h3>Supported columns:</h3>
	 * <ul>
	 * <li><b>0:</b> Type</li>
	 * <li><b>1:</b> Date</li>
	 * <li><b>2:</b> Responsible</li>
	 * <li><b>3:</b> Status</li>
	 * <li><b>4:</b> Patient</li>
	 * <li><b>5:</b> Description</li>
	 * </ul>
	 *
	 * <p>
	 * The default sort direction is {@link SWT#DOWN}.
	 * </p>
	 */
	public static class ReminderComparator extends ViewerComparator implements Comparator<IReminder> {
		private int column = -1;
		private int direction = SWT.DOWN;
		@Override
		public int compare(IReminder r1, IReminder r2) {
			int result = 0;
			try {
				switch (column) {
				case 0 -> { // TYPE
					String t1 = (r1.getType() != null) ? r1.getType().getLocaleText() : StringUtils.EMPTY;
					String t2 = (r2.getType() != null) ? r2.getType().getLocaleText() : StringUtils.EMPTY;
					result = compareByString(t1, t2);
				}
				case 1 -> result = compareByDate(r1, r2); // DATE
				case 2 -> { // RESPONSIBLE
					String resp1 = getResponsibleString(r1);
					String resp2 = getResponsibleString(r2);
					result = compareByString(resp1, resp2);
				}
				case 3 -> { // STATUS
					String s1 = (r1.getStatus() != null) ? r1.getStatus().getLocaleText() : StringUtils.EMPTY;
					String s2 = (r2.getStatus() != null) ? r2.getStatus().getLocaleText() : StringUtils.EMPTY;
					result = compareByString(s1, s2);
				}
				case 4 -> { // PATIENT
					String p1 = getPatientName(r1);
					String p2 = getPatientName(r2);
					result = compareByString(p1, p2);
				}
				case 5 -> { // DESCRIPTION
					String subj1 = (StringUtils.isNotEmpty(r1.getSubject())) ? r1.getSubject() : r1.getMessage();
					String subj2 = (StringUtils.isNotEmpty(r2.getSubject())) ? r2.getSubject() : r2.getMessage();
					result = compareByString(subj1, subj2);
				}
				default -> result = compareByDate(r1, r2);
				}
			} catch (Exception e) {
				// fallback: never crash sorting
				result = 0;
			}
			return (direction == SWT.UP) ? -result : result;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return compare((IReminder) e1, (IReminder) e2);
		}

		private int compareByDate(IReminder r1, IReminder r2) {
			if (r1.getDue() != null && r2.getDue() != null)
				return r1.getDue().compareTo(r2.getDue());
			if (r1.getDue() == null && r2.getDue() == null)
				return 0;
			return (r1.getDue() == null) ? 1 : -1;
		}

		private int compareByString(String s1, String s2) {
			if (s1 == null)
				s1 = StringUtils.EMPTY;
			if (s2 == null)
				s2 = StringUtils.EMPTY;
			return s1.compareToIgnoreCase(s2);
		}

		private String getPatientName(IReminder r) {
			if (r.getContact() == null)
				return StringUtils.EMPTY;
			IContact c = r.getContact();
			return (StringUtils.defaultString(c.getDescription1()) + StringUtils.SPACE
					+ StringUtils.defaultString(c.getDescription2())).trim();
		}

		private String getResponsibleString(IReminder r) {
			if (r.isResponsibleAll())
				return "Alle";
			return r.getResponsible().stream()
					.map(c -> c.isMandator() ? c.getDescription1() + StringUtils.SPACE + c.getDescription2()
							: c.getLabel())
					.reduce((a, b) -> a + " | " + b).orElse(StringUtils.EMPTY);
		}

		public void setColumn(int index) {
			if (column == index) {
				direction = (direction == SWT.DOWN) ? SWT.UP : SWT.DOWN;
			} else {
				column = index;
				direction = SWT.DOWN;
			}
		}
		public int getDirection() {
			return direction;
		}
	}
}