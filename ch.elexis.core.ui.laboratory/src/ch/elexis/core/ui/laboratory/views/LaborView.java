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

package ch.elexis.core.ui.laboratory.views;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.LabItem;
import ch.elexis.core.data.LabResult;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Person;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.DateSelectorDialog;
import ch.elexis.core.ui.dialogs.DisplayLabDokumenteDialog;
import ch.elexis.core.ui.dialogs.DisplayTextDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.TimeTool;

/**
 * Anzeige von Laboritems und Anzeige und Eingabemöglichkeit von Laborwerten.
 * 
 * Der Algorithmus geht so: Zuerst werden alle Laboritems eingesammelt und gemäss ihren Gruppen und
 * Prioritäten sortiert (nur beim create) Beim Einlesen eines neuen Patienten werden zunächst alle
 * Daten gesammelt, an denen für diesen Patienten Laborwerte vorliegen. Diese werden nach Alter
 * sortiert und mit den jeweiligen Laborwerten zusammengefasst. Jeweils NUMCOLUMNS Daten werden auf
 * einer Anzeigeseite angezeigt. Der Anwender kann auf den Seiten blättern, aber es werden alle
 * Laborwerte des aktuellen Patienten im Speicher gehalten.
 * 
 * @author gerry
 */
public class LaborView extends ViewPart implements IActivationListener, ISaveablePart2 {
	private static final String KEY_TEXT = "Text"; //$NON-NLS-1$
	private static final String PATTERN_DECIMAL = "[0-9\\.]+"; //$NON-NLS-1$
	private static final String KEY_VALUES = "Values"; //$NON-NLS-1$
	private static final String KEY_ITEM = "Item"; //$NON-NLS-1$
	private static Logger log = LoggerFactory.getLogger(LaborView.class); //$NON-NLS-1$
	
	final static int NUMCOLUMNS = 7; // Pro Seite angezeigte Laborspalten
	final static int COL_OFFSET = 2; // Für Information benötigte Spalten
	final static Color COL_PATHOLOGIC = UiDesk.getColor(UiDesk.COL_RED);
	final static Color COL_REMARK = UiDesk.getColor(UiDesk.COL_BLUE);
	final static Color COL_BACKGND = UiDesk.getColor(UiDesk.COL_WHITE);
	int actPage; // Aktuell angezeigte Seite
	int firstColumn, lastColumn; // Erste und letzte Datumspalte der
	// aktuellen Seite
	Patient actPatient; // Aktuell ausgewählter Patient
	
	/* Tabelle */
	Table table;
	TableColumn[] columns;
	TableItem[] rows;
	TableCursor cursor;
	ControlEditor editor;
	
	private Hashtable<String, List<LabItem>> hGroups; // Gruppen von
	// Laboritems
	private Hashtable<String, Integer> hLabItems; // Mapping von Laboritems
	// auf Tabellenzeilen
	List<String> lGroupNames; // Alphabetische Gruppenliste
	// List<LabResult> lResults;
	String[] sDaten; // Sortierte Liste aller Daten von Laborresultaten
	Hashtable<String, Integer> hDaten; // Mapping von Datum auf Tabellenspalten
	
	private Action fwdAction, backAction, printAction, importAction, xmlAction, newAction,
			setStateAction, refreshAction;
	private ViewMenus menu;
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	// Formula handling
	private final static Pattern varsPattern = Pattern.compile("[a-zA-Z0-9]+_[0-9]+"); //$NON-NLS-1$
	private final HashMap<String, List<LabItem>> formulaRelations =
		new HashMap<String, List<LabItem>>();
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev) {
			selectPatient((Patient) ev.getObject());
		};
	};
	
	private ElexisEventListener eeli_labitem = new ElexisEventListener() {
		private final ElexisEvent eetmpl = new ElexisEvent(null, LabItem.class,
			ElexisEvent.EVENT_RELOAD);
		
		public ElexisEvent getElexisEventFilter(){
			return eetmpl;
		}
		
		public void catchElexisEvent(ElexisEvent ev){
			UiDesk.getDisplay().asyncExec(new Runnable() {
				public void run(){
					rebuild();
				}
			});
		}
	};
	
	@Override
	public void createPartControl(final Composite parent){
		setTitleImage(Images.IMG_VIEW_LABORATORY.getImage());
		parent.setLayout(new GridLayout());
		form = tk.createForm(parent);
		form.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		
		table = new Table(body, SWT.FULL_SELECTION | SWT.LEFT | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		table.addListener(SWT.PaintItem, new Listener() {
			
			private void paintCell(final String text, final Event event, final Color foregnd,
				final Color backgnd){
				Point size = event.gc.textExtent(text);
				int offset1 = Math.max(0, (event.width - size.x) / 2);
				int offset2 = Math.max(0, (event.height - size.y) / 2);
				GC gc = event.gc;
				gc.setForeground(backgnd);
				gc.fillRectangle(event.x, event.y, event.width, event.height);
				gc.setForeground(foregnd);
				event.gc.drawText(text, event.x + offset1, event.y + offset2, true);
			}
			
			public void handleEvent(final Event event){
				TableItem item = (TableItem) event.item;
				String text = item.getText(event.index);
				LabItem it = (LabItem) item.getData(KEY_ITEM);
				if (it != null) {
					LabResult[] lrs = (LabResult[]) item.getData(KEY_VALUES);
					if (lrs != null) {
						int screenIdx = event.index - COL_OFFSET;
						if ((screenIdx >= 0) && (screenIdx < lrs.length)) {
							LabResult lr = lrs[screenIdx];
							if (lr != null) {
								if (lr.isFlag(LabResult.PATHOLOGIC)) {
									paintCell(text, event, COL_PATHOLOGIC, COL_BACKGND);
								}
								if (lr.getComment().length() > 0) {
									paintCell(text, event, COL_REMARK, COL_BACKGND);
								}
							}
						}
					}
				}
			}
			
		});
		
		cursor = new TableCursor(table, SWT.NONE);
		editor = new ControlEditor(cursor);
		editor.grabHorizontal = true;
		editor.grabVertical = true;
		
		/*
		 * Tastatursteuerung für die Tabelle: Druck auf Eingabetaste lässt die Zelle editieren,
		 * sofern sie auf einem editierbaren Feld ist. Wenn sie nicht auf einem editierbaren Feld
		 * ist, wird der stattdessen Cursor eine Zeile nach unten bewegt. Druck auf irgendeine Zahl-
		 * oder Buchstabentaste lässt die Zelle editieren, wenn sie editierbar ist. Editierbar ist
		 * eine Zelle dann, wenn sie sich a) in einer Spalte mit einem Datum im Kopf befindet, und
		 * b) sich in einer Zeile mit einem LaborItem am Anfang befindet.
		 */
		cursor.addSelectionListener(new SelectionAdapter() {
			// Tabellenauswahl soll dem Cursor folgen
			@Override
			public void widgetSelected(final SelectionEvent e){
				table.setSelection(new TableItem[] {
					cursor.getRow()
				});
			}
			
			// Eingabetaste
			@Override
			public void widgetDefaultSelected(final SelectionEvent e){
				
				TableItem row = cursor.getRow();
				LabItem li = (LabItem) row.getData(KEY_ITEM);
				if (li == null) {
					cursorDown();
					return;
				}
				int column = cursor.getColumn();
				if (columns[column].getText().matches(PATTERN_DECIMAL)) {
					doEdit(row.getText(column));
				}
				
			}
		});
		// Sonstige Taste
		cursor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e){
				if (e.character == SWT.DEL) {
					return;
				}
				TableItem row = cursor.getRow();
				e.doit = false;
				if (row.getData(KEY_ITEM) == null) {
					return;
				}
				if (e.character > 0x30) {
					StringBuilder sb = new StringBuilder();
					sb.append(e.character);
					int column = cursor.getColumn();
					if (columns[column].getText().matches(PATTERN_DECIMAL)) {
						doEdit(sb.toString());
					}
				}
			}
		});
		cursor.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDoubleClick(final MouseEvent e){
				LabResult lr = actResult();
				if (lr != null) {
					LabItem li = lr.getItem();
					if (li.getTyp().equals(LabItem.typ.TEXT) || (lr.getComment().length() > 0)) {
						DisplayTextDialog dlg =
							new DisplayTextDialog(
								getViewSite().getShell(),
								Messages.LaborView_textResultTitle, li.getName(), lr.getComment()); //$NON-NLS-1$
						Font font = null;
						// HL7 Befunde enthalten oft mit Leerzeichen formatierte Bemerkungen,
						// die nur mit nicht-proportionalen Fonts dargestellt werden können
						// Wir versuchen also, die Anzeige mit Courier New, ohne zu wissen ob die
						// auf Mac und Linux auch drauf sind.
						// Falls der Font nicht geladen werden kann, wird der System-Default Font
						// verwendet
						// Hier die Fonts, welche getestet worden sind:
						// Windows: Courier New (getestet=
						// Mac: nicht getestet
						// Linux: nicht getestet
						try {
							font = new Font(null, "Courier New", 9, SWT.NORMAL); //$NON-NLS-1$
						} catch (Exception ex) {
							// Do nothing -> Use System Default font
						} finally {
							dlg.setFont(font);
						}
						dlg.setWhitespaceNormalized(false);
						dlg.open();
					} else if (li.getTyp().equals(LabItem.typ.DOCUMENT)) {
						Patient patient = ElexisEventDispatcher.getSelectedPatient();
						if (patient != null) {
							Query<LabResult> labResultQuery = new Query<LabResult>(LabResult.class);
							labResultQuery.add(LabResult.PATIENT_ID, Query.EQUALS, patient.getId());
							labResultQuery.add(LabResult.DATE, Query.EQUALS, lr.getDate());
							labResultQuery.add(LabResult.ITEM_ID, Query.EQUALS, li.getId());
							List<LabResult> labResultList = labResultQuery.execute();
							if (labResultList != null && labResultList.size() > 0) {
								new DisplayLabDokumenteDialog(getViewSite().getShell(), Messages.LaborView_Documents, labResultList).open();//$NON-NLS-1$
							}
						}
					}
				}
				super.mouseDoubleClick(e);
			}
			
		});
		
		makeActions();
		menu = new ViewMenus(getViewSite());
		menu.createMenu(newAction, backAction, fwdAction, printAction, importAction, xmlAction);
		// Orders
		final LaborOrderPulldownMenuCreator menuCreator =
			new LaborOrderPulldownMenuCreator(parent.getShell());
		if (menuCreator.getSelected() != null) {
			IAction dropDownAction = menuCreator.getAction();
			
			IActionBars actionBars = getViewSite().getActionBars();
			IToolBarManager toolbar = actionBars.getToolBarManager();
			
			toolbar.add(dropDownAction);
			
			// Set data
			dropDownAction.setText(menuCreator.getSelected().getText());
			dropDownAction.setToolTipText(menuCreator.getSelected().getToolTipText());
			dropDownAction.setImageDescriptor(menuCreator.getSelected().getImageDescriptor());
		}
		// Importers
		IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
		List<IAction> importers =
			Extensions.getClasses(
				Extensions.getExtensions("ch.elexis.LaborDatenImport"), "ToolbarAction", //$NON-NLS-1$ //$NON-NLS-2$
				false);
		for (IAction ac : importers) {
			tm.add(ac);
		}
		if (importers.size() > 0) {
			tm.add(new Separator());
		}
		tm.add(refreshAction);
		tm.add(newAction);
		tm.add(backAction);
		tm.add(fwdAction);
		tm.add(printAction);
		// menu.createToolbar(newAction,backAction,fwdAction,printAction);
		final MenuManager mgr = new MenuManager("path"); //$NON-NLS-1$
		Menu menu = mgr.createContextMenu(cursor);
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager){
				LabResult lr = getSelectedResult();
				if (lr != null) {
					LabItem li = lr.getItem();
					if (!li.getTyp().equals(LabItem.typ.DOCUMENT)) {
						mgr.add(setStateAction);
						setStateAction.setChecked(lr.isFlag(LabResult.PATHOLOGIC));
					}
				}
				
			}
		});
		cursor.setMenu(menu);
		// menu.createControlContextMenu(cursor, setStateAction);
		rebuild();
		GlobalEventDispatcher.addActivationListener(this, this);
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	public void rebuild(){
		actPage = 0;
		hDaten = new Hashtable<String, Integer>();
		hGroups = new Hashtable<String, List<LabItem>>(50, 0.7f);
		lGroupNames = new ArrayList<String>(20);
		showBusy(true);
		createColumns();
		loadItems();
		createRows();
		actPatient = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		loadValues();
		showBusy(false);
	}
	
	LabResult actResult(){
		TableItem it = cursor.getRow();
		int idx = cursor.getColumn();
		LabItem lit = (LabItem) it.getData(KEY_ITEM);
		if (lit != null) {
			LabResult[] lrs = (LabResult[]) it.getData(KEY_VALUES);
			if (lrs == null) {
				lrs = new LabResult[NUMCOLUMNS];
				it.setData(KEY_VALUES, lrs);
			}
			int column = idx - COL_OFFSET;
			if (column >= 0) {
				return lrs[idx - COL_OFFSET];
			}
		}
		return null;
	}
	
	/*
	 * Tabellenzelle editieren. CR oder Pfeil unten verlässt die Zelle mit Speichern und geht zur
	 * nächst unteren Zelle. Esc verlässt die Zelle ohne speichern
	 */
	private void doEdit(final String inp){
		final Text text = new Text(cursor, SWT.NONE);
		text.setText(inp);
		text.setSelection(inp.length());
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e){
				if ((e.character == SWT.CR) || (e.keyCode == SWT.ARROW_DOWN)) {
					TableItem it = cursor.getRow();
					int idx = cursor.getColumn(); // Spalte der Anzeige
					int idx_values = firstColumn + idx - COL_OFFSET; // Spalte
					// bezogen
					// auf
					// alle
					// Daten
					LabItem lit = (LabItem) it.getData(KEY_ITEM);
					if (lit != null) {
						LabResult[] lrs = (LabResult[]) it.getData(KEY_VALUES);
						if (lrs == null) {
							lrs = new LabResult[NUMCOLUMNS];
							it.setData(KEY_VALUES, lrs);
						}
						LabResult lr = lrs[idx - COL_OFFSET];
						TimeTool ttDaten = new TimeTool(sDaten[idx_values]);
						String t = text.getText();
						if (lr == null) {
							if (t.length() > 0) {
								lr = new LabResult(actPatient, ttDaten, lit, text.getText(), ""); //$NON-NLS-1$
								lrs[idx - COL_OFFSET] = lr;
							}
						} else {
							if (t.length() == 0) {
								lr.delete();
							} else {
								lr.undelete();
								lr.setResult(text.getText());
							}
						}
						if (lr != null) {
							List<LabItem> toCalc = formulaRelations.get(lr.getItem().makeVarName());
							if (toCalc != null) {
								for (LabItem litem : toCalc) {
									try {
										String evaluated = litem.evaluate(actPatient, ttDaten);
										if (evaluated != null) {
											LabResult artifact =
												LabResult.getForDate(actPatient, ttDaten, litem);
											if (artifact == null) {
												artifact =
													new LabResult(actPatient, ttDaten, litem,
														evaluated, ""); //$NON-NLS-1$
											} else {
												artifact.setResult(evaluated);
											}
											Integer row = hLabItems.get(litem.getId());
											if (row == null) {
												continue;
											}
											rows[row].setText(idx, evaluated);
											
										}
									} catch (ElexisException ex) {
										Status status =
											new Status(IStatus.ERROR, Hub.PLUGIN_ID, ex
												.getLocalizedMessage(), ex);
										StatusManager.getManager().handle(status,
											StatusManager.SHOW);
									}
								}
							}
						}
					}
					
					it.setText(idx, text.getText());
					text.dispose();
					cursorDown();
					table.setFocus();
				}
				if (e.character == SWT.DEL) {
					text.setText(""); //$NON-NLS-1$
				} else if (e.character == SWT.ESC) {
					text.dispose();
					table.setFocus();
				}
			}
		});
		editor.setEditor(text);
		text.setFocus();
	}
	
	private void cursorDown(){
		int row = table.getSelectionIndex();
		if (row == rows.length - 1) {
			return;
		}
		cursor.setSelection(row + 1, cursor.getColumn());
		table.setSelection(row + 1);
		LabItem it = (LabItem) cursor.getRow().getData(KEY_ITEM);
		if (it == null) {
			cursorDown();
		}
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	public void selectionEvent(final PersistentObject obj){
		if (obj instanceof Patient) {
			actPatient = (Patient) obj;
			loadValues();
		} else if (obj instanceof Konsultation) {
			Patient p = ((Konsultation) obj).getFall().getPatient();
			if ((actPatient == null) || (!p.equals(actPatient))) {
				actPatient = p;
				loadValues();
			}
		}
		
	}
	
	private void selectPatient(Patient p){
		actPatient = p;
		loadValues();
	}
	
	/*
	 * Daten eines neuen Patienten einlesen
	 */
	private void loadValues(){
		hDaten.clear();
		sDaten = null;
		if (actPatient != null) {
			form.setText(actPatient.getLabel());
			// Zuerst sehen, für wieviele Daten Laborwerte vorliegen, und diese
			// Daten auf Index mappen
			// Hier müssen wir ausnahmsweise direkt auf den JdbcLink zugreifen
			Stm stm = PersistentObject.getConnection().getStatement();
			ResultSet rs = stm.query("SELECT DISTINCT Datum FROM LABORWERTE WHERE PatientID=" //$NON-NLS-1$
				+ actPatient.getWrappedId() + " AND deleted='0' ORDER BY Datum"); //$NON-NLS-1$
			LinkedList<String> lDaten = new LinkedList<String>();
			try {
				int col = 0;
				while ((rs != null) && (rs.next() == true)) {
					String dat = rs.getString(1);
					lDaten.add(dat);
					hDaten.put(dat, col++);
				}
				sDaten = lDaten.toArray(new String[0]);
				loadPage(getLastPage());
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
			// Referenzwerte je nach Geschlecht eintragen
			boolean s = (actPatient.getGeschlecht().equals("m")); //$NON-NLS-1$
			for (int i = 0; i < rows.length; i++) {
				TableItem ti = rows[i];
				LabItem li = (LabItem) ti.getData(KEY_ITEM);
				if (li != null) {
					if (s == true) {
						ti.setText(1, li.getRefM());
					} else {
						ti.setText(1, li.getRefW());
					}
				}
			}
		} else {
			form.setText(Messages.LaborView_NoPatientSelected); //$NON-NLS-1$
		}
	}
	
	private int getLastPage(){
		return sDaten.length / NUMCOLUMNS;
	}
	
	/*
	 * Eine Seite mit Laborwerten (=NUMCOLUMNS Spalten) einlesen
	 */
	private void loadPage(final int p){
		// Zuerst prüfen, ob die angeforderte Seite gültig ist
		if (p < 0) {
			return;
		}
		actPage = p;
		
		// Dann alte Einträge löschen
		String[] line = new String[NUMCOLUMNS + COL_OFFSET];
		for (int i = COL_OFFSET; i < NUMCOLUMNS + COL_OFFSET; i++) {
			line[i] = ""; //$NON-NLS-1$
			columns[i].setText(""); //$NON-NLS-1$
		}
		// Zeilentitel und Wertelisten vorbelegen
		for (int i = 0; i < rows.length; i++) {
			line[0] = ((String) rows[i].getData(KEY_TEXT));
			rows[i].setText(line);
			rows[i].setData(KEY_VALUES, new LabResult[NUMCOLUMNS]);
		}
		firstColumn = (p == 0) ? 0 : (p * (NUMCOLUMNS - 1));
		lastColumn = firstColumn + NUMCOLUMNS - 1;
		
		// Keine Anzeigbaren Daten vorhanden?
		if ((sDaten == null) || (sDaten.length == 0) || (sDaten.length < firstColumn)) {
			loadPage(p - 1);
			return;
		}
		
		// Query für alle Laborwerte zwischen erstem und letztem Datum der
		// aktuellen Seite
		String sBegin = sDaten[firstColumn];
		Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
		qbe.add(LabResult.PATIENT_ID, Query.EQUALS, actPatient.getId());
		qbe.add(LabResult.DATE, Query.GREATER_OR_EQUAL, sBegin);
		
		// int numvalid=NUMCOLUMNS; // Wieviele Spalten können tatsächlich
		// angezeigt werden?
		if (lastColumn < sDaten.length) {
			qbe.add(LabResult.DATE, Query.LESS_OR_EQUAL, sDaten[lastColumn]);
		} else {
			lastColumn = sDaten.length - 1;
		}
		int numvalid = lastColumn - firstColumn + 1;
		List<LabResult> list = qbe.execute();
		
		// Spaltenköpfe beschriften
		TimeTool dats = new TimeTool();
		for (int i = 0; i < numvalid; i++) {
			String dat = sDaten[i + firstColumn];
			dats.set(dat);
			columns[COL_OFFSET + i].setText(dats.toString(TimeTool.DATE_GER));
		}
		
		// Laborresultate eintragen mithilfe der Mappings in hDaten und
		// hLabItems
		TimeTool tt = new TimeTool();
		for (LabResult lr : list) {
			LabItem lit = lr.getItem();
			if (lit == null) {
				log.warn("Fehlerhaftes LabResult " + lr.getId()); //$NON-NLS-1$
				continue;
			}
			tt.set(lr.getDate());
			int col_values = hDaten.get(tt.toString(TimeTool.DATE_COMPACT)); // Absolute
			// Spalte
			int col_display = col_values - firstColumn + COL_OFFSET; // Spalte
			// relativ
			// zur
			// Seite
			Integer row = hLabItems.get(lit.getId()); // Zeile für die Anzeige
			if (row == null) {
				continue;
			}
			if (LabItem.typ.DOCUMENT.equals(lit.getTyp())) {
				rows[row].setText(col_display, Messages.LaborView_Open); //$NON-NLS-1$
			} else {
				rows[row].setText(col_display, lr.getResult()); // Spalte für die
			}
			// Anzeige
			LabResult[] lrs = (LabResult[]) rows[row].getData(KEY_VALUES); // LabResult
			// anfügen
			if (lrs == null) {
				lrs = new LabResult[NUMCOLUMNS];
				rows[row].setData(KEY_VALUES, lrs);
			}
			lrs[col_values - firstColumn] = lr;
		}
	}
	
	/*
	 * Zeilen erstellen und Mappings zwischen LabItem und Zeilennummer erstellen. Jeder Gruppentitel
	 * in blauer Farbe, darunter die Gruppe, dann eine Leerzeile.
	 */
	private void createRows(){
		table.removeAll();
		hLabItems = new Hashtable<String, Integer>(50, 0.75f);
		ArrayList<TableItem> lTI = new ArrayList<TableItem>(50);
		int line = 0;
		int iteration = 0;
		for (String g : lGroupNames) {
			iteration++;
			List<LabItem> groupItems = hGroups.get(g);
			if (groupItems == null) {
				log.error("Fehler bei Laborgruppe " + g); //$NON-NLS-1$
				continue;
			}
			TableItem ti = new TableItem(table, SWT.NONE);
			ti.setForeground(UiDesk.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
			
			// split group order token and group name
			Matcher m = Pattern.compile("(\\S+)\\s+(.+)").matcher(g); //$NON-NLS-1$
			if (m.matches()) {
				String name = m.group(2);
				ti.setText(0, name);
				ti.setData(KEY_TEXT, name);
			} else {
				ti.setText("? " + g + " ?"); //$NON-NLS-1$ //$NON-NLS-2$
				ti.setData(KEY_TEXT, "? " + g + " ?"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			lTI.add(ti);
			line += 1;
			for (LabItem it : groupItems) {
				TableItem ti2 = new TableItem(table, SWT.NONE);
				ti2.setData(KEY_ITEM, it);
				ti2.setText(it.getShortLabel());
				ti2.setData(KEY_TEXT, it.getShortLabel());
				lTI.add(ti2);
				if (it.getTyp().equals(LabItem.typ.FORMULA)) {
					String formel = it.getFormula();
					Matcher matcher = varsPattern.matcher(formel);
					while (matcher.find()) {
						String var = matcher.group();
						List<LabItem> itList = formulaRelations.get(var);
						if (itList == null) {
							itList = new ArrayList<LabItem>();
							formulaRelations.put(var, itList);
						}
						itList.add(it);
					}
				}
				hLabItems.put(it.getId(), line++);
			}
			if (iteration < lGroupNames.size()) {
				TableItem tiSpace = new TableItem(table, SWT.NONE);
				tiSpace.setText(" "); //$NON-NLS-1$
				tiSpace.setData(KEY_TEXT, " "); //$NON-NLS-1$
				lTI.add(tiSpace);
				line += 1;
			}
		}
		rows = lTI.toArray(new TableItem[0]);
	}
	
	/**
	 * Liste der Laboritems, Gruppiert nach groups und Sequenznummer aufbauen
	 * 
	 */
	private void loadItems(){
		Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
		List<LabItem> lItems = qbe.execute();
		
		for (LabItem it : (List<LabItem>) lItems) {
			String group = it.getGroup();
			List<LabItem> lGroupItems = hGroups.get(group); // Existiert die
			// Gruppe schon?
			if (lGroupItems == null) {
				lGroupItems = new ArrayList<LabItem>(); // Wenn nein, neu
				// erstellen
				hGroups.put(group, lGroupItems); // und in die
				// Groups-Hashtable einfügen
				int i = 0;
				for (i = 0; i < lGroupNames.size(); i++) { // Dann sortiert in
					// die Gruppenliste
					// eintragen.
					if (group.compareTo(lGroupNames.get(i)) < 0) {
						break;
					}
				}
				lGroupNames.add(i, group);
			}
			lGroupItems.add(it); // Schliesslich den Item einfügen
			Collections.sort(lGroupItems); // und die Itemliste neu sortieren
		}
		
	}
	
	public LabResult getSelectedResult(){
		TableItem item = cursor.getRow();
		LabItem it = (LabItem) item.getData(KEY_ITEM);
		if (it != null) {
			LabResult[] lrs = (LabResult[]) item.getData(KEY_VALUES);
			if (lrs != null) {
				LabResult lr = lrs[cursor.getColumn() - COL_OFFSET];
				if (lr != null) {
					return lr;
				}
			}
		}
		return null;
	}
	
	private void createColumns(){
		if (columns != null) {
			for (int i = 0; i < columns.length; i++) {
				columns[i].dispose();
			}
			columns = null;
		}
		columns = new TableColumn[NUMCOLUMNS + COL_OFFSET];
		for (int i = 0; i < NUMCOLUMNS + COL_OFFSET; i++) {
			columns[i] = new TableColumn(table, SWT.LEFT);
			columns[i].setWidth(75);
			columns[i].addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(final SelectionEvent e){
					TimeTool dOld = new TimeTool();
					if (dOld.set(((TableColumn) e.getSource()).getText()) == true) {
						DateSelectorDialog dsl = new DateSelectorDialog(getViewSite().getShell());
						if (dsl.open() == Dialog.OK) {
							TimeTool dat = dsl.getSelectedDate();
							String nDat = dat.toString(TimeTool.DATE_COMPACT);
							Query<LabResult> qbe = new Query<LabResult>(LabResult.class);
							qbe.add(LabResult.DATE, Query.EQUALS,
								dOld.toString(TimeTool.DATE_COMPACT));
							qbe.add(LabResult.PATIENT_ID, Query.EQUALS, actPatient.getId());
							for (LabResult lr : qbe.execute()) {
								lr.set(LabResult.DATE, nDat);
							}
							loadValues();
							loadPage(actPage);
						}
					}
				}
				
			});
			
		}
		columns[0].setWidth(200);
		columns[1].setWidth(70);
		columns[0].setText(Messages.LaborView_parameter); //$NON-NLS-1$
		columns[1].setText(Messages.LaborView_reference); //$NON-NLS-1$
	}
	
	private void makeActions(){
		fwdAction = new Action(Messages.LaborView_nextPage) { //$NON-NLS-1$
				@Override
				public void run(){
					loadPage(actPage + 1);
				}
			};
		backAction = new Action(Messages.LaborView_prevPage) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actPage > 0) {
						loadPage(actPage - 1);
					}
				}
			};
		printAction = new Action(Messages.LaborView_print) { //$NON-NLS-1$
				@Override
				public void run(){
					try {
						LaborblattView lb =
							(LaborblattView) getViewSite().getPage().showView(LaborblattView.ID);
						Patient pat = ElexisEventDispatcher.getSelectedPatient();;
						String[] headers = new String[columns.length];
						for (int i = 0; i < headers.length; i++) {
							headers[i] = columns[i].getText();
						}
						lb.createLaborblatt(pat, headers, rows);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
			};
		importAction = new Action(Messages.LaborView_import) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
					setToolTipText(Messages.LaborView_importToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					Importer imp =
						new Importer(getViewSite().getShell(), "ch.elexis.LaborDatenImport"); //$NON-NLS-1$
					imp.create();
					imp.setMessage(Messages.LaborView_selectDataSource); //$NON-NLS-1$
					imp.getShell().setText(Messages.LaborView_labImporterCaption); //$NON-NLS-1$
					imp.setTitle(Messages.LaborView_labImporterText); //$NON-NLS-1$
					imp.open();
				}
			};
		xmlAction = new Action(Messages.LaborView_xmlExport) { //$NON-NLS-1$
				@Override
				public void run(){
					Document doc = makeXML();
					if (doc != null) {
						FileDialog fsel =
							new FileDialog(Hub.plugin.getWorkbench().getActiveWorkbenchWindow()
								.getShell());
						String fname = fsel.open();
						if (fname != null) {
							try {
								FileOutputStream fout = new FileOutputStream(fname);
								OutputStreamWriter cout = new OutputStreamWriter(fout, "UTF-8"); //$NON-NLS-1$
								XMLOutputter xout = new XMLOutputter(Format.getPrettyFormat());
								xout.output(doc, cout);
								cout.close();
								fout.close();
							} catch (Exception ex) {
								ExHandler.handle(ex);
								SWTHelper
									.alert(
										Messages.LaborView_ErrorCaption, Messages.LaborView_couldntwrite + fname); //$NON-NLS-1$ //$NON-NLS-2$
								
							}
						}
					}
				}
			};
		newAction = new Action(Messages.LaborView_newDate) { //$NON-NLS-1$
				@Override
				public void run(){
					DateSelectorDialog dsd = new DateSelectorDialog(getViewSite().getShell());
					dsd.create();
					Point m = UiDesk.getDisplay().getCursorLocation();
					dsd.getShell().setLocation(m.x, m.y);
					if (dsd.open() == Dialog.OK) {
						if (sDaten == null) {
							return;
						}
						
						String date = dsd.getSelectedDate().toString(TimeTool.DATE_COMPACT);
						String[] nDates = new String[sDaten.length + 1];
						System.arraycopy(sDaten, 0, nDates, 0, sDaten.length);
						nDates[sDaten.length] = date;
						hDaten.put(date, sDaten.length);
						sDaten = nDates;
						loadPage(getLastPage());
					}
					
				}
			};
		setStateAction =
			new Action(Messages.LaborView_pathologic, Action.AS_CHECK_BOX) { //$NON-NLS-1$
				@Override
				public void run(){
					LabResult lr = getSelectedResult();
					lr.setFlag(LabResult.PATHOLOGIC, isChecked());
					loadPage(getLastPage());
				}
			};
		refreshAction = new Action(Messages.LaborView_Refresh) { //$NON-NLS-1$
				@Override
				public void run(){
					rebuild();
				}
			};
		
		newAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor()); // Hub.getImageDescriptor("rsc/add.gif"));
		fwdAction.setImageDescriptor(Images.IMG_NEXT.getImageDescriptor());
		backAction.setImageDescriptor(Images.IMG_PREVIOUS.getImageDescriptor());
		printAction.setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
		xmlAction.setImageDescriptor(Images.IMG_EXPORT.getImageDescriptor());
		refreshAction.setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
	}
	
	public Document makeXML(){
		Document doc = null;
		try {
			doc = new Document();
			Element r = new Element("Laborblatt"); //$NON-NLS-1$
			r.setAttribute("Erstellt", new TimeTool() //$NON-NLS-1$
				.toString(TimeTool.FULL_GER));
			Patient actpat = ElexisEventDispatcher.getSelectedPatient();
			if (actpat != null) {
				r.setAttribute("Patient", actpat.getLabel()); //$NON-NLS-1$
			}
			doc.setRootElement(r);
			
			Element Daten = new Element("Daten"); //$NON-NLS-1$
			for (String d : sDaten) {
				Element dat = new Element("Datum"); //$NON-NLS-1$
				dat.setAttribute("Tag", d); //$NON-NLS-1$
				Daten.addContent(dat);
			}
			r.addContent(Daten);
			for (String g : lGroupNames) {
				Element eGroup = new Element("Gruppe"); //$NON-NLS-1$
				eGroup.setAttribute("Name", g); //$NON-NLS-1$
				List<LabItem> items = hGroups.get(g);
				if (items == null) {
					log.warn("Ungültige Gruppe " + g); //$NON-NLS-1$
					continue;
				}
				if (items.size() == 0) {
					continue;
				}
				for (LabItem it : items) {
					Element eItem = new Element("Parameter"); //$NON-NLS-1$
					eItem.setAttribute("Name", it.getName()); //$NON-NLS-1$
					eItem.setAttribute("Kürzel", it.getKuerzel()); //$NON-NLS-1$
					eItem.setAttribute("Einheit", it.getEinheit()); //$NON-NLS-1$
					boolean hasContent = false;
					for (String t : sDaten) {
						Element eResult = new Element("Resultat"); //$NON-NLS-1$
						eResult.setAttribute("Datum", t); //$NON-NLS-1$
						eItem.addContent(eResult);
						List<LabResult> results = new LinkedList<LabResult>(); // hValues.get(t);
						for (LabResult lr : results) {
							if (lr.getItem().equals(it)) {
								eResult.addContent(lr.getResult());
								hasContent = true;
								
							}
						}
					}
					if (hasContent == true) {
						Element ref = new Element("Referenz"); //$NON-NLS-1$
						ref.setAttribute(Person.MALE, it.get("RefMann")); //$NON-NLS-1$ //$NON-NLS-2$
						ref.setAttribute(Person.FEMALE, it.get("RefFrauOrTx")); //$NON-NLS-1$ //$NON-NLS-2$
						eItem.addContent(ref);
						eGroup.addContent(eItem);
					}
				}
				if (eGroup.getContentSize() != 0) {
					r.addContent(eGroup);
				}
			}
			
		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
		return doc;
	}
	
	public void visible(final boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_labitem, eeli_pat);
			Patient act = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
			if ((act != null)
				&& ((actPatient == null) || (!act.getId().equals(actPatient.getId())))) {
				actPatient = act;
				loadValues();
			}
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_labitem, eeli_pat);
		}
		
	}
	
	public void activation(final boolean mode){}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public void reloadContents(final Class clazz){
		if (clazz.equals(LabItem.class)) {
			
		}
	}
	
}
