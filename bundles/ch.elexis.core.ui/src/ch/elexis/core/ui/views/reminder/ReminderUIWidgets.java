package ch.elexis.core.ui.views.reminder;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

public class ReminderUIWidgets {

	public static class HeaderComposite extends Composite {
		private Label header;
		private ToolBarManager toolbarManager;
		private Runnable onLayoutNeeded;

		public HeaderComposite(Composite parent, int style, Runnable onLayoutNeeded) {
			super(parent, style);
			this.onLayoutNeeded = onLayoutNeeded;
			setBackground(parent.getBackground());
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);

			header = new Label(this, SWT.NONE);
			header.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			header.setBackground(getBackground());

			toolbarManager = new ToolBarManager();
			ToolBar toolbar = toolbarManager.createControl(this);
			toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			toolbar.setBackground(getBackground());
		}

		public void setTextFont(Font font) {
			header.setFont(font);
		}

		public void setText(String text) {
			header.setText(text);
			if (onLayoutNeeded != null) {
				onLayoutNeeded.run();
			}
		}
	}

	public static class TableViewerResizer {
		private static int minHeight = 25;

		public static void enableResizing(TableViewer tableViewer, ScrolledComposite scrolledComposite) {
			Table table = tableViewer.getTable();
			Composite parent = table.getParent();

			class ResizerState {
				boolean isResizing = false;
				int lastY = 0;
				int newY = 0;
			}
			final ResizerState state = new ResizerState();

			Listener displayFilter = new Listener() {
				@Override
				public void handleEvent(Event e) {
					if (table.isDisposed()) {
						return;
					}

					if (!state.isResizing && e.widget != table) {
						return;
					}

					Point pt = table.toControl(parent.getDisplay().getCursorLocation());
					int currentY = pt.y;

					switch (e.type) {
					case SWT.MouseDown:
						if (e.widget == table && isNearBottomEdge(table, currentY)) {
							state.isResizing = true;
							state.lastY = currentY;
							state.newY = currentY;
							table.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
							table.deselectAll();
							e.type = SWT.None;
						}
						break;

					case SWT.MouseMove:
						if (state.isResizing) {
							state.newY = currentY;
							table.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
							e.type = SWT.None;
						} else if (e.widget == table && isNearBottomEdge(table, currentY)) {
							table.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_SIZENS));
						} else if (e.widget == table && table.getCursor() != null) {
							table.setCursor(null);
						}
						break;

					case SWT.MouseUp:
						if (state.isResizing) {
							if (state.lastY != 0 && state.lastY != state.newY && state.newY != 0) {
								int deltaY = state.newY - state.lastY;
								GridData gd = (GridData) table.getLayoutData();

								if (deltaY != 0) {
									int newHeight = gd.heightHint + deltaY;
									if (newHeight > minHeight) {
										gd.heightHint = newHeight;
										table.setLayoutData(gd);
										table.getParent().layout(true, true);
										Point newSize = table.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
										scrolledComposite.setMinSize(newSize.x,
												Math.max(newSize.y, scrolledComposite.getClientArea().height));
										scrolledComposite.layout(true, true);
									}
								}
							}
							state.isResizing = false;
							state.lastY = 0;
							state.newY = 0;
							table.deselectAll();
							table.setCursor(null);
							e.type = SWT.None;
						}
						break;
					}
				}
			};

			Display display = parent.getDisplay();
			display.addFilter(SWT.MouseDown, displayFilter);
			display.addFilter(SWT.MouseMove, displayFilter);
			display.addFilter(SWT.MouseUp, displayFilter);

			table.addDisposeListener(e -> {
				if (!display.isDisposed()) {
					display.removeFilter(SWT.MouseDown, displayFilter);
					display.removeFilter(SWT.MouseMove, displayFilter);
					display.removeFilter(SWT.MouseUp, displayFilter);
				}
			});
		}

		private static boolean isNearBottomEdge(Table table, int y) {
			if (table.isDisposed())
				return false;
			
			int clientHeight = table.getClientArea().height;

			int grabArea = 18;
			return y >= (clientHeight - grabArea) && y <= (clientHeight + grabArea);
		}
	}

	public static class CustomTimePopupDialog extends Dialog {
		LocalDate today = LocalDate.now();
		private int selectedDays = 0;
		private String title = "Nächste %s Tage";

		public CustomTimePopupDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			updateTitle(parent);
			Composite area = (Composite) super.createDialogArea(parent);
			area.setLayout(new GridLayout(1, false));

			DateTime calendar = new DateTime(area, SWT.CALENDAR | SWT.BORDER);
			calendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			calendar.addListener(SWT.Selection, e -> {
				LocalDate selectedDate = LocalDate.of(calendar.getYear(), calendar.getMonth() + 1, calendar.getDay());
				if (selectedDate.isBefore(today)) {
					calendar.setDate(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
				} else {
					selectedDays = (int) ChronoUnit.DAYS.between(today, selectedDate);
					updateTitle(parent);
				}
			});

			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}

		public int getSelectedDays() {
			return selectedDays;
		}

		private void updateTitle(Composite parent) {
			parent.getShell().setText(String.format(title, selectedDays));
		}
	}
}