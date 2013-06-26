/*******************************************************************************
 * Portions created by Andrey Onistchuk are copyright (c) 2005 Andrey Onistchuk.
 * 
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrey Onistchuk - initial implementation.
 *     Sebastian Thomschke - small bug fixes.
 *******************************************************************************/
package com.tiff.common.ui.datepicker;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TypedListener;

/**
 * The date picker panel
 * 
 * changes by sebthom ~ renamed private method doubleClick to onMouseDoubleClick ~ renamed private
 * method dateSelected to onDateSelected ~ removed StringBuffer usage in getCurrentMonthName method
 * - corrected firstDayOfTheWeek bug (26.06.2004) ~ setDate will throw the Selection event + you can
 * use setDate(null) to clear the selection, the calendar will display the current date, but getDate
 * will return null until the user explicitely selects a date from the control
 * 
 * changes by Daniel Lutz <danlutz@watz.ch> ~ DatePanel.onPaint(): draw currently selected day/date
 * in respect to selectedDate instead of interally selected day of month
 * 
 * @author <a href="mailto:andy@tiff.ru">Andrey Onistchuk</a>
 * @version $Revision: 1.2 $
 */
public class DatePicker extends Composite {
	
	// ~ Inner Classes ----------------------------------------------------------
	private class DatePanel extends Canvas {
		// ~ Instance fields ----------------------------------------------------
		private int colSize;
		private Display display = Display.getCurrent();
		private int rowSize;
		private Calendar temp = Calendar.getInstance();
		
		// ~ Constructors -------------------------------------------------------
		public DatePanel(Composite parent, int style){
			super(parent, style | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
			
			GC gc = new GC(this);
			Point p = gc.stringExtent("Q");
			gc.dispose();
			colSize = p.x * 3;
			rowSize = (int) (p.y * 1.4);
			
			addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent event){
					onPaint(event);
				}
			});
			addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e){
					redraw();
				}
			});
			addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e){
					onKeyDown(e);
				}
			});
			addMouseListener(new MouseAdapter() {
				public void mouseDoubleClick(MouseEvent e){
					onMouseDoubleClick();
				}
				
				public void mouseDown(MouseEvent e){
					onMouseDown(e);
				}
			});
			addMouseMoveListener(new MouseMoveListener() {
				public void mouseMove(MouseEvent e){
					onMouseMove(e);
				}
			});
		}
		
		// ~ Methods ------------------------------------------------------------
		private int computeOffset(int day){
			switch (day) {
			case Calendar.MONDAY:
				return 1;
			case Calendar.TUESDAY:
				return 2;
			case Calendar.WEDNESDAY:
				return 3;
			case Calendar.THURSDAY:
				return 4;
			case Calendar.FRIDAY:
				return 5;
			case Calendar.SATURDAY:
				return 6;
			case Calendar.SUNDAY:
				return 7;
			}
			return -1;
		}
		
		public Point computeSize(int wHint, int hHint, boolean changed){ // Höhe vergrössert (gerry)
			return new Point(colSize * 7, rowSize * 8);
		}
		
		/**
		 * Method drawTextImage.
		 * 
		 * @param gc
		 * @param string
		 * @param x
		 * @param y
		 * @param colSize
		 * @param rowSize
		 */
		private void drawTextImage(GC gc, String string, int x, int y, int colSize, int rowSize){
			gc.fillRectangle(x, y, colSize, rowSize);
			gc.drawString(string, x, y, true);
		}
		
		private int getDayFromPoint(int x, int y){
			int result = -1;
			
			for (int i = 1; i <= 31; i++) {
				Point p = getDayPoint(i);
				Rectangle r = new Rectangle(p.x, p.y, colSize, rowSize);
				if (r.contains(x, y)) {
					result = i;
					break;
				}
			}
			return result;
		}
		
		private String getDayName(int day){
			return dateSymbols.getShortWeekdays()[day];
		}
		
		private Point getDayPoint(int day){
			syncTime();
			temp.set(Calendar.DAY_OF_MONTH, 1);
			
			int firstDayOffset = computeOffset(temp.get(Calendar.DAY_OF_WEEK)) - 1;
			temp.set(Calendar.DAY_OF_MONTH, day);
			int dayOffset = computeOffset(temp.get(Calendar.DAY_OF_WEEK));
			int x = (dayOffset - 1) * colSize;
			int y = (1 + (((firstDayOffset + day) - 1) / 7)) * rowSize;
			
			return new Point(x, y);
		}
		
		private int getMaxDay(){
			syncTime();
			
			int day = 28;
			
			for (int i = 0; i < 10; i++) {
				temp.set(Calendar.DAY_OF_MONTH, day);
				
				if (temp.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
					return day - 1;
				}
				day++;
			}
			return -1;
		}
		
		private void onKeyDown(KeyEvent e){
			if (e.character == SWT.ESC) {
				onDateSelected(false);
				return;
			}
			
			if ((e.character == ' ') || (e.character == '\r')) {
				onDateSelected(true);
				return;
			}
			
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int oldDay = day;
			int oldMonth = month;
			
			if (e.keyCode == SWT.ARROW_LEFT) {
				day--;
			}
			
			if (e.keyCode == SWT.ARROW_RIGHT) {
				day++;
			}
			
			if (e.keyCode == SWT.ARROW_UP) {
				day = ((day - 7) < 1 ? oldDay : day - 7);
			}
			
			if (e.keyCode == SWT.ARROW_DOWN) {
				day = ((day + 7) > getMaxDay() ? oldDay : day + 7);
			}
			
			if (e.keyCode == SWT.PAGE_UP) {
				month--;
			}
			
			if (e.keyCode == SWT.PAGE_DOWN) {
				month++;
			}
			
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DAY_OF_MONTH, day);
			
			if ((day != oldDay) || (month != oldMonth)) {
				redraw();
				
				if (month != oldMonth) {
					updateMonthLabel();
				}
			}
		}
		
		private void onMouseDown(MouseEvent e){
			int day = getDayFromPoint(e.x, e.y);
			
			if (day > 0) {
				cal.set(Calendar.DAY_OF_MONTH, day);
				onDateSelected(true);
				updateDate();
			}
		}
		
		private void onMouseMove(MouseEvent e){
			int day = getDayFromPoint(e.x, e.y);
			selection = day;
			updateDate();
		}
		
		private boolean isSelectedDate(Calendar cal){
			if (selectedDate == null) {
				return false;
			}
			
			final Calendar temp = Calendar.getInstance();
			temp.setTime(selectedDate);
			
			if (temp.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
				&& temp.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
				&& temp.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
				
				return true;
			} else {
				return false;
			}
		}
		
		private void onPaint(PaintEvent event){
			Rectangle rect = getClientArea();
			GC gc0 = event.gc;
			Image image = new Image(display, rect.width, rect.height);
			GC gc = new GC(image);
			gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillRectangle(rect);
			
			int x = 0;
			int y = 0;
			
			// draw the weekdays
			int firstDayOfWeek = Calendar.getInstance().getFirstDayOfWeek();
			for (int i = 0; i < 7; i++) {
				if (i == 6) {
					gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
				}
				int dayIndex = firstDayOfWeek + i > 7 ? firstDayOfWeek + i - 7 : firstDayOfWeek + i;
				drawTextImage(gc, getDayName(dayIndex), x, 0, colSize, rowSize);
				x += colSize;
			}
			
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			y += rowSize;
			gc.drawLine(0, 0, rect.width, 0);
			gc.drawLine(0, y - 1, rect.width, y - 1);
			
			syncTime();
			
			int day = 1;
			
			while (true) {
				temp.set(Calendar.DAY_OF_MONTH, day);
				
				if (temp.get(Calendar.MONTH) != cal.get(Calendar.MONTH)) {
					break;
				}
				
				int dayOffset = computeOffset(temp.get(Calendar.DAY_OF_WEEK));
				Point p = getDayPoint(day);
				
				// if (day == cal.get(Calendar.DAY_OF_MONTH))
				if (isSelectedDate(temp)) {
					gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					gc.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
				} else if (day == selection) {
					gc.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
					gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
				} else {
					gc.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
					gc.setForeground(display.getSystemColor(dayOffset == 7 ? SWT.COLOR_RED
							: SWT.COLOR_BLACK));
				}
				
				drawTextImage(gc, "" + day, p.x, p.y, colSize, rowSize);
				day++;
			}
			
			gc0.drawImage(image, 0, 0);
			gc.dispose();
			image.dispose();
		}
		
		private void syncTime(){
			temp.setTimeInMillis(cal.getTimeInMillis());
		}
	}
	
	// ~ Instance fields --------------------------------------------------------
	private Calendar cal = Calendar.getInstance();
	
	private DatePanel datePanel;
	private DateFormatSymbols dateSymbols = new DateFormatSymbols();
	private Label monthLabel;
	private Date selectedDate;
	private int selection = -1;
	
	// ~ Constructors -----------------------------------------------------------
	public DatePicker(Composite parent, int style){
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.verticalSpacing = gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_CYAN));
		setLayout(gridLayout);
		
		GridData gridData;
		
		// previous year
		Button prevYear = new Button(this, SWT.FLAT);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.heightHint = gridData.widthHint = 20;
		prevYear.setLayoutData(gridData);
		prevYear.setText("<<");
		prevYear.setSelection(false);
		prevYear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				cal.roll(Calendar.YEAR, -1);
				updateDate();
			}
		});
		
		// previous month
		Button prevMonth = new Button(this, SWT.FLAT);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.heightHint = gridData.widthHint = 20;
		prevMonth.setLayoutData(gridData);
		prevMonth.setText("<");
		prevMonth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				cal.add(Calendar.MONTH, -1);
				updateDate();
			}
		});
		
		// current month
		monthLabel = new Label(this, SWT.CENTER);
		gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.CENTER);
		gridData.heightHint = prevYear.computeSize(20, 20).y;
		gridData.grabExcessHorizontalSpace = true;
		monthLabel.setLayoutData(gridData);
		
		// next month
		Button nextMonth = new Button(this, SWT.FLAT);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.heightHint = gridData.widthHint = 20;
		nextMonth.setLayoutData(gridData);
		nextMonth.setText(">");
		nextMonth.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				cal.add(Calendar.MONTH, 1);
				updateDate();
			}
		});
		
		// next year
		Button nextYear = new Button(this, SWT.FLAT);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.heightHint = gridData.widthHint = 20;
		nextYear.setLayoutData(gridData);
		nextYear.setText(">>");
		nextYear.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e){
				cal.roll(Calendar.YEAR, 1);
				updateDate();
			}
		});
		
		// a panel
		datePanel = new DatePanel(this, SWT.NONE);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalSpan = 5;
		datePanel.setLayoutData(gridData);
		
		updateDate();
	}
	
	// ~ Methods ----------------------------------------------------------------
	
	/**
	 * Adds the listener to receive events.
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @exception SWTError
	 *                (ERROR_THREAD_INVALID_ACCESS) when called from the wrong thread
	 * @exception SWTError
	 *                (ERROR_WIDGET_DISPOSED) when the widget has been disposed
	 * @exception SWTError
	 *                (ERROR_NULL_ARGUMENT) when listener is null
	 * 
	 * @see SelectionListener
	 * @see #removeSelectionListener
	 */
	public void addSelectionListener(SelectionListener listener){
		checkWidget();
		
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed){
		Point pSize = datePanel.computeSize(wHint, hHint, changed);
		Point labelSize = monthLabel.computeSize(wHint, hHint, changed);
		
		int x = (pSize.x < (labelSize.x + 80) ? labelSize.x + 80 : pSize.x);
		int y = (pSize.y < (labelSize.y + 20) ? labelSize.y + 20 : pSize.y);
		return new Point(x, y);
	}
	
	/**
	 * gets the name of the current month
	 */
	private String getCurrentMonthName(){
		return getMonthName(cal.get(Calendar.MONTH)) + ", " + cal.get(Calendar.YEAR);
	}
	
	/**
	 * gets the currently selected date
	 */
	public Date getDate(){
		return selectedDate == null ? null : (Date) selectedDate.clone();
	}
	
	/**
	 * gets the name of the month
	 * 
	 * @param month
	 */
	private String getMonthName(int month){
		return dateSymbols.getMonths()[month];
	}
	
	private void onDateSelected(boolean good){
		if (good)
			selectedDate = cal.getTime();
		
		Event event = new Event();
		event.doit = good;
		notifyListeners(SWT.Selection, event);
	}
	
	private void onMouseDoubleClick(){
		Event event = new Event();
		event.doit = true;
		notifyListeners(SWT.MouseDoubleClick, event);
	}
	
	/**
	 * Removes the listener.
	 * 
	 * @param listener
	 *            the listener
	 * 
	 * @exception SWTError
	 *                (ERROR_THREAD_INVALID_ACCESS) when called from the wrong thread
	 * @exception SWTError
	 *                (ERROR_WIDGET_DISPOSED) when the widget has been disposed
	 * @exception SWTError
	 *                (ERROR_NULL_ARGUMENT) when listener is null
	 * 
	 * @see SelectionListener
	 * @see #addSelectionListener
	 */
	public void removeSelectionListener(SelectionListener listener){
		checkWidget();
		
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}
	
	/**
	 * resets the date picker's date to today
	 */
	public void reset(){
		cal = Calendar.getInstance();
		updateDate();
	}
	
	/**
	 * sets the date
	 * 
	 * @param date
	 */
	public void setDate(Date date){
		if (date == null)
			selectedDate = null;
		else
			selectedDate = (Date) date.clone();
		
		cal.setTime(selectedDate == null ? new Date() : date);
		
		// updateMonthLabel();
		// redraw();
		updateDate();
	}
	
	private void updateDate(){
		datePanel.redraw();
		updateMonthLabel();
	}
	
	private void updateMonthLabel(){
		monthLabel.setText(getCurrentMonthName());
	}
}