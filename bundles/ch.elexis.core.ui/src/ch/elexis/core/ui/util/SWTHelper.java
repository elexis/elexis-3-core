/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.wb.swt.SWTResourceManager;

import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.rgw.tools.StringTool;

/** statische Hilfsfunktionen für SWT-Objekte */
public class SWTHelper {
	/**
	 * Singleton-Variable, die einen FocusListener enthaelt, der in einem
	 * Text-Control den Text selektiert, wenn das Control den Focus erhaelt. Siehe
	 * setSelectOnFocus().
	 */
	private static FocusListener selectOnFocusListener = null;
	private static Log log = Log.get("Global: "); //$NON-NLS-1$

	/** Ein Objekt innerhalb des parents zentrieren */
	public static void center(final Shell parent, final Composite child) {
		if (parent != null && child != null) {
			Rectangle par = parent.getBounds();
			Rectangle ch = child.getBounds();
			if (par != null && ch != null) {
				int xOff = (par.width - ch.width) / 2;
				int yOff = (par.height - ch.height) / 2;
				child.setBounds(par.x + xOff, par.y + yOff, ch.width, ch.height);
			}
		}
	}

	/** Ein Objekt innerhalb des parents zentrieren */
	public static void center(final Shell parent, final Shell child) {
		if (parent != null && child != null) {
			Rectangle par = parent.getBounds();
			Rectangle ch = child.getBounds();
			if (par != null && ch != null) {
				int xOff = (par.width - ch.width) / 2;
				int yOff = (par.height - ch.height) / 2;
				child.setBounds(par.x + xOff, par.y + yOff, ch.width, ch.height);
			}
		}
	}

	/**
	 * Ein Objekt auf dem Bildschirm zentrieren. Robust. Sollte nie eine Exception
	 * werfen. Ändert im Zweifelsfall nichts an der Position.
	 */
	public static void center(final Shell child) {
		if (child != null) {
			Display display = UiDesk.getDisplay();
			if (display != null) {
				Rectangle par = display.getBounds();
				if (par != null) {
					Rectangle ch = child.getBounds();
					if (ch != null) {
						ch.width = Math.max(30, ch.width);
						ch.height = Math.max(20, ch.height);
						int xOff = (par.width - ch.width) / 2;
						int yOff = (par.height - ch.height) / 2;
						child.setBounds(par.x + xOff, par.y + yOff, ch.width, ch.height);
					}
				}
			}

		}
	}

	/** Einen Text zentriert in ein Rechteck schreiben */
	public static void writeCentered(final GC gc, final String text, final Rectangle bounds) {
		int w = gc.getFontMetrics().getAverageCharWidth();
		int h = gc.getFontMetrics().getHeight();
		int woff = (bounds.width - text.length() * w) >> 1;
		int hoff = (bounds.height - h) >> 1;
		gc.drawString(text, bounds.x + woff, bounds.y + hoff);
	}

	/** Eine Alertbox anzeigen (synchron) */
	public static void alert(final String title, final String message) {
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell shell = UiDesk.getDisplay().getActiveShell();
				if (shell == null) {
					shell = new Shell(UiDesk.getDisplay());
				}
				MessageBox msg = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				msg.setText(title);
				msg.setMessage(message);
				msg.open();
			}
		});
	}

	/**
	 * Eine Standard-Fehlermeldung asynchron im UI-Thread zeigen
	 *
	 * @param title   Titel
	 * @param message Nachricht
	 */
	public static void showError(final String title, final String message) {
		UiDesk.getDisplay().syncExec(new Runnable() {

			public void run() {
				Shell shell = UiDesk.getTopShell();
				MessageDialog.openError(shell, title, message);
			}
		});
	}

	/**
	 * Eine Standard-Fehlermeldung asynchron zeigen und loggen
	 *
	 * @param title   Titel
	 * @param message Nachricht
	 */
	public static void showError(final String logHeader, final String title, final String message) {
		log.log(logHeader + ": " + title + "->" + message, Log.ERRORS); //$NON-NLS-1$ //$NON-NLS-2$
		UiDesk.getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell shell = UiDesk.getDisplay().getActiveShell();
				MessageDialog.openError(shell, title, message);
			}
		});
	}

	/**
	 * Eine Standard-Infomeldung asynchron zeigen
	 *
	 * @param title   Titel
	 * @param message Nachricht
	 */
	public static void showInfo(final String title, final String message) {
		UiDesk.getDisplay().syncExec(new Runnable() {

			public void run() {
				Shell shell = UiDesk.getTopShell();
				MessageDialog.openInformation(shell, title, message);
			}
		});
	}

	/**
	 * Eine mit Ja oder Nein zu beantwortende Frage im UI-Thread zeigen
	 *
	 * @param title   Titel
	 * @param message Nachricht
	 * @return true: User hat Ja geklickt
	 */
	public static boolean askYesNo(final String title, final String message) {
		InSync rn = new InSync(title, message);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;
	}

	/**
	 * Ask question with custom button labels. The index of the pressed button is
	 * returned.
	 *
	 * @param title
	 * @param message
	 * @param dialogButtonLabels
	 * @return
	 */
	public static int ask(final String title, final String message, String... dialogButtonLabels) {
		InSyncMulti rn = new InSyncMulti(title, message, dialogButtonLabels);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;
	}

	private static class InSyncMulti implements Runnable {
		private int ret;
		private String title, message;
		private String[] dialogButtonLabels;

		InSyncMulti(final String title, final String message, String... dialogButtonLabels) {
			this.title = title;
			this.message = message;
			this.dialogButtonLabels = dialogButtonLabels;
		}

		public void run() {
			Shell shell = UiDesk.getTopShell();
			ret = MessageDialog.open(MessageDialog.QUESTION, shell, title, message, SWT.SHEET, dialogButtonLabels);
		}
	}

	private static class InSync implements Runnable {
		boolean ret;
		String title, message;

		InSync(final String title, final String message) {
			this.title = title;
			this.message = message;
		}

		public void run() {
			Shell shell = UiDesk.getTopShell();
			ret = MessageDialog.openConfirm(shell, title, message);
		}
	}

	/**
	 * Eine mit Ja, Nein oder Abbrechen zu beantwortende Frage im UI-Thread zeigen
	 *
	 * @param title   Titel
	 * @param message Nachricht
	 * @return true: User hat Ja geklickt
	 */
	public static Boolean askYesNoCancel(final String title, final String message) {
		InSyncYesNoCancel rn = new InSyncYesNoCancel(title, message);
		UiDesk.getDisplay().syncExec(rn);
		return rn.ret;
	}

	private static class InSyncYesNoCancel implements Runnable {
		Boolean ret = null;
		String title, message;

		InSyncYesNoCancel(final String title, final String message) {
			this.title = title;
			this.message = message;
		}

		public void run() {
			Shell shell = UiDesk.getTopShell();
			MessageDialog dialog = new MessageDialog(shell, title, null, // accept
					// the
					// default
					// window
					// icon
					message, MessageDialog.QUESTION,
					new String[] { Messages.SWTHelper_yes, Messages.SWTHelper_no, Messages.SWTHelper_cancel }, 0);
			// ok is the default
			int result = dialog.open();
			if (result != 2) {
				ret = result == 0;
			}
		}
	}

	/**
	 * Shortcut for getFillGridData(1,true,1,true);
	 *
	 * @return
	 */
	public static GridData getFillGridData() {
		return getFillGridData(1, true, 1, true);
	}

	/**
	 * Ein GridData-Objekt erzeugen, das den horizontalen und/oder vertikalen
	 * Freiraum ausfüllt.
	 *
	 * @param horizontal true, wenn horizontal gefüllt werden soll
	 * @param vertical   true, wenn vertikal gefüllt werden soll.
	 * @return ein neu erzeugtes, direkt verwendbares GridData-Objekt
	 */
	public static GridData getFillGridData(final int hSpan, final boolean hFill, final int vSpan, final boolean vFill) {
		int ld = 0;
		if (hFill) {
			ld = GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL;
		}
		if (vFill) {
			ld |= GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL;
		}
		GridData ret = new GridData(ld);
		ret.horizontalSpan = (hSpan < 1) ? 1 : hSpan;
		ret.verticalSpan = vSpan < 1 ? 1 : vSpan;
		return ret;
	}

	public static GridData fillGrid(final Composite parent, final int cols) {
		parent.setLayout(new GridLayout(cols, false));
		return getFillGridData(1, true, 1, true);
	}

	/**
	 * Set a GridData to the given Control that sets the specified height in lines
	 * calculated with the control's current font.
	 *
	 * @param control        the control
	 * @param lines          reuqested height of the control in lines
	 * @param fillHorizontal true if the control should require all horizontal space
	 * @return the GridData (that is already set to the control)
	 */
	public static GridData setGridDataHeight(final Control control, final int lines, final boolean fillHorizontal) {
		int h = Math.round(control.getFont().getFontData()[0].height);
		GridData gd = getFillGridData(1, fillHorizontal, 1, false);
		gd.heightHint = lines * (h + 2);
		control.setLayoutData(gd);
		return gd;
	}

	/**
	 * Constructor wrapper for TableWrapLayout, so that parameters are identical to
	 * GridLayout(numColumns, makeColumnsEqualWidth)
	 */
	public static TableWrapLayout createTableWrapLayout(final int numColumns, final boolean makeColumnsEqualWidth) {
		TableWrapLayout layout = new TableWrapLayout();

		layout.numColumns = numColumns;
		layout.makeColumnsEqualWidth = makeColumnsEqualWidth;

		return layout;
	}

	/**
	 * Ein TableWrapDAta-Objekt erzeugen, das den horizontalen und/oder vertikalen
	 * Freiraum ausfüllt.
	 *
	 * @param horizontal true, wenn horizontal gefüllt werden soll
	 * @param vertical   true, wenn vertikal gefüllt werden soll.
	 * @return ein neu erzeugtes, direkt verwendbares GridData-Objekt
	 */
	public static TableWrapData getFillTableWrapData(final int hSpan, final boolean hFill, final int vSpan,
			final boolean vFill) {
		TableWrapData layoutData = new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP);

		if (hFill) {
			layoutData.grabHorizontal = true;
			layoutData.align = TableWrapData.FILL;
		}
		if (vFill) {
			layoutData.grabVertical = true;
			layoutData.valign = TableWrapData.FILL;
		}

		layoutData.colspan = (hSpan < 1 ? 1 : hSpan);
		layoutData.rowspan = (vSpan < 1 ? 1 : vSpan);

		return layoutData;
	}

	/**
	 * Return a color that contrasts optimally to the given color
	 *
	 * @param col an SWT Color
	 * @return black if col was rather bright, white if col was rather dark.
	 */
	public static Color getContrast(final Color col) {
		double val = col.getRed() * 0.56 + col.getGreen() * 0.33 + col.getBlue() * 0.11;
		if (val <= 110) {
			return UiDesk.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		}
		return UiDesk.getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Return a Label that acts as a hyperlink
	 *
	 * @param parent parent control
	 * @param text   text to display
	 * @param lis    hyperlink listener that is called on Mouse click
	 * @return a Label
	 */
	public static Label createHyperlink(final Composite parent, final String text, final IHyperlinkListener lis) {
		final Label ret = new Label(parent, SWT.NONE);
		ret.setText(text);
		ret.setForeground(UiDesk.getColorRegistry().get(Messages.Core_Blue)); // $NON-NLS-1$
		ret.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				if (lis != null) {
					lis.linkActivated(new HyperlinkEvent(ret, ret, text, e.stateMask));
				}
			}

		});
		return ret;
	}

	/**
	 * Create a multiline text widget with a specified height in lines (calculated
	 * with the Text's default font)
	 *
	 * @param parent parent composite
	 * @param lines  requested height of the text field
	 * @param flags  creation flags (SWT.MULTI and SWT.WRAP are added automatocally)
	 * @return a Text control
	 */
	public static Text createText(final Composite parent, final int lines, final int flags) {
		int lNum = SWT.SINGLE;
		if (lines > 1) {
			lNum = SWT.MULTI | SWT.WRAP;
		}
		Text ret = new Text(parent, SWT.BORDER | flags | lNum);
		GridData gd = getFillGridData(1, true, 1, false);
		int h = Math.round(ret.getFont().getFontData()[0].height);
		gd.minimumHeight = (lines + 1) * (h + 2);
		gd.heightHint = gd.minimumHeight;
		ret.setLayoutData(gd);
		return ret;
	}

	public static Text createText(final FormToolkit tk, final Composite parent, final int lines, final int flags) {
		int lNum = SWT.SINGLE;
		if (lines > 1) {
			lNum = SWT.MULTI | SWT.WRAP;
		}
		Text ret = tk.createText(parent, StringUtils.EMPTY, lNum | flags | SWT.BORDER);
		GridData gd = getFillGridData(1, true, 1, true);
		int h = Math.round(ret.getFont().getFontData()[0].height);
		gd.minimumHeight = (lines + 1) * (h + 2);
		gd.heightHint = gd.minimumHeight;
		ret.setLayoutData(gd);
		return ret;
	}

	public static LabeledInputField createLabeledField(final Composite parent, final String label,
			final LabeledInputField.Typ typ) {
		LabeledInputField ret = new LabeledInputField(parent, label, typ);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		return ret;
	}

	/**
	 * Check whether the String is empty and give an error message if so
	 *
	 * @param test the String to test
	 * @param name the name for the String
	 * @return false if it was empty
	 */
	public static boolean blameEmptyString(final String test, final String name) {
		if (StringTool.isNothing(test)) {
			showError(Messages.SWTHelper_BadParameter, name + Messages.SWTHelper_HasNoValidContents);
			return false;
		}
		return true;
	}

	/**
	 * Adds a FocusListener to <code>text</code> so that the text is selected as
	 * soon as the control gets the focus. The selection is cleared when the focus
	 * is lost.
	 *
	 * @param text the Text control to add a focus listener to
	 */
	public static void setSelectOnFocus(final Text text) {
		if (selectOnFocusListener == null) {
			selectOnFocusListener = new FocusListener() {
				public void focusGained(final FocusEvent e) {
					Text t = (Text) e.widget;
					t.selectAll();
				}

				public void focusLost(final FocusEvent e) {
					Text t = (Text) e.widget;
					if (t.getSelectionCount() > 0) {
						t.clearSelection();
					}
				}
			};
		}

		text.addFocusListener(selectOnFocusListener);
	}

	public static class SimpleDialog extends Dialog {
		IControlProvider dialogAreaProvider;

		public SimpleDialog(final IControlProvider control) {
			super(UiDesk.getTopShell());
			dialogAreaProvider = control;
		}

		@Override
		protected Control createDialogArea(final Composite parent) {
			return dialogAreaProvider.getControl(parent);
		}

		@Override
		protected void okPressed() {
			dialogAreaProvider.beforeClosing();
			super.okPressed();
		}

	}

	public interface IControlProvider {
		public Control getControl(Composite parent);

		public void beforeClosing();
	}

	public static java.awt.Font createAWTFontFromSWTFont(final Font swtFont) {
		String name = swtFont.getFontData()[0].getName();
		int style = swtFont.getFontData()[0].getStyle();
		int height = swtFont.getFontData()[0].getHeight();
		java.awt.Font awtFont = new java.awt.Font(name, style, height);
		return awtFont;
	}

	public static int size(final Rectangle r) {
		if (r == null) {
			return 0;
		}
		return (r.width - r.x) * (r.height - r.y);
	}

	public static Point getStringBounds(Composite c, String s) {
		GC gc = new GC(c);
		Point ret = gc.textExtent(s);
		gc.dispose();
		return ret;
	}

	/**
	 * Convenience method to add a separator bar to the composite.
	 * <p>
	 * The parent composite must have a <code>GridLayout</code>. The separator bar
	 * will span all columns of the parent grid layout. <br>
	 * <br>
	 * Code from: http://www.softwarerevolution.com/blueprints/ The Software
	 * Revolution Inc. by Thomas Holland under GPLv3
	 * </p>
	 *
	 * @param parent <code>Composite</code>
	 */
	public static void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		Layout parentlayout = parent.getLayout();
		if (parentlayout instanceof GridLayout) {
			int columns = ((GridLayout) parentlayout).numColumns;
			GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, columns, 1);
			separator.setLayoutData(gridData);
		}
	}

	/**
	 * This method "reloads" a view, by closing and opening it. It is the
	 * programmatical equivalent to closing a view and then select Open/View/and the
	 * view ID.
	 *
	 * This method should NOT be used, as it identifies an architectural problem.
	 * The UI itself should support the respective update.
	 *
	 * @param viewID
	 */
	public static void reloadViewPart(String viewID) {
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			IViewPart page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(viewID);
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(page);
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewID);
			} catch (PartInitException e) {
				Status status = new Status(IStatus.ERROR, Hub.PLUGIN_ID, "Error reopening viewPart " + viewID, e); //$NON-NLS-1$
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
		}
	}

	/**
	 * Creates a new GridLayout and can set all spaces and gaps to zero
	 *
	 * @param noSpace
	 * @param numColumns
	 * @return
	 */
	public static GridLayout createGridLayout(boolean noGaps, int numColumns) {
		GridLayout gd = new GridLayout(numColumns, false);
		if (noGaps) {
			gd.horizontalSpacing = 0;
			gd.verticalSpacing = 0;
			gd.marginHeight = 0;
			gd.marginWidth = 0;
		}
		return gd;
	}

	/**
	 * Creates a bold informational label used to indicate demo mode or restricted
	 * functionality within a preference or settings page.
	 * <p>
	 * The label is created with word wrap enabled, uses a bold "Lucida Grande" font
	 * (size 10) and is laid out to span 3 columns in a grid layout.
	 * </p>
	 *
	 * @param parent the parent composite to add the label to
	 * @param text   the message text to display inside the label
	 * @return the created {@link Label} instance
	 */
	public static Label createDemoInfoLabel(Composite parent, String text) {
		Label demoInfo = new Label(parent, SWT.WRAP);
		demoInfo.setText(text);
		demoInfo.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.BOLD)); //$NON-NLS-1$
		demoInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		return demoInfo;
	}
}
