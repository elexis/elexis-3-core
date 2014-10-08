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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.laboratory.controls.LaborOrdersComposite;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.dialogs.LaborVerordnungDialog;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.ExHandler;
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
public class LaborView extends ViewPart implements ISaveablePart2 {
	
	public static final String ID = "ch.elexis.Labor"; //$NON-NLS-1$
	private static Log log = Log.get("LaborView"); //$NON-NLS-1$
	
	private CTabFolder tabFolder;
	private LaborResultsComposite resultsComposite;
	private LaborOrdersComposite ordersComposite;
	
	private Action fwdAction, backAction, printAction, importAction, xmlAction, newAction,
			newColumnAction, refreshAction, expandAllAction, collapseAllAction;
	private ViewMenus menu;
	
	private ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		@Override
		public void runInUi(ElexisEvent ev){
			resultsComposite.selectPatient((Patient) ev.getObject());
			ordersComposite.selectPatient((Patient) ev.getObject());
		}
	};
	
	private ElexisEventListener eeli_labitem = new ElexisEventListener() {
		private final ElexisEvent eetmpl = new ElexisEvent(null, LabItem.class,
			ElexisEvent.EVENT_RELOAD);
		
		@Override
		public ElexisEvent getElexisEventFilter(){
			return eetmpl;
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			UiDesk.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run(){
					resultsComposite.reload();
				}
			});
		}
	};
	
	private ElexisEventListener eeli_labresult = new ElexisEventListener() {
		private final ElexisEvent eetmpl = new ElexisEvent(null, LabResult.class,
			ElexisEvent.EVENT_RELOAD);
		
		@Override
		public ElexisEvent getElexisEventFilter(){
			return eetmpl;
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			UiDesk.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run(){
					resultsComposite.reload();
					ordersComposite.reload();
				}
			});
		}
	};
	
	private ElexisEventListener eeli_laborder = new ElexisEventListener() {
		private final ElexisEvent eetmpl = new ElexisEvent(null, LabOrder.class,
			ElexisEvent.EVENT_RELOAD);
		
		@Override
		public ElexisEvent getElexisEventFilter(){
			return eetmpl;
		}
		
		@Override
		public void catchElexisEvent(ElexisEvent ev){
			UiDesk.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run(){
					ordersComposite.reload();
				}
			});
		}
	};
	
	@Override
	public void createPartControl(final Composite parent){
		setTitleImage(Images.IMG_VIEW_LABORATORY.getImage());
		
		tabFolder = new CTabFolder(parent, SWT.TOP);
		tabFolder.setLayout(new FillLayout());
		
		final CTabItem resultsTabItem = new CTabItem(tabFolder, SWT.NULL);
		resultsTabItem.setText("Resultate");
		resultsComposite = new LaborResultsComposite(tabFolder, SWT.NONE);
		resultsTabItem.setControl(resultsComposite);
		
		final CTabItem ordersTabItem = new CTabItem(tabFolder, SWT.NULL);
		ordersTabItem.setText("Verordnungen");
		ordersComposite = new LaborOrdersComposite(tabFolder, SWT.NONE);
		ordersTabItem.setControl(ordersComposite);
		
		tabFolder.setSelection(0);
		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				resultsComposite.reload();
				ordersComposite.reload();
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
				Extensions.getExtensions(ExtensionPointConstantsUi.LABORDATENIMPORT),
				"ToolbarAction", //$NON-NLS-1$ //$NON-NLS-2$
				false);
		for (IAction ac : importers) {
			tm.add(ac);
		}
		if (importers.size() > 0) {
			tm.add(new Separator());
		}
		tm.add(refreshAction);
		tm.add(newColumnAction);
		tm.add(newAction);
		tm.add(backAction);
		tm.add(fwdAction);
		tm.add(expandAllAction);
		tm.add(collapseAllAction);
		tm.add(printAction);
		
		// register event listeners
		ElexisEventDispatcher.getInstance().addListeners(eeli_labitem, eeli_laborder,
			eeli_labresult, eeli_pat);
		Patient act = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if ((act != null && act != resultsComposite.getPatient())) {
			resultsComposite.selectPatient(act);
		}
	}
	
	@Override
	public void dispose(){
		super.dispose();
		ElexisEventDispatcher.getInstance().removeListeners(eeli_labitem, eeli_laborder,
			eeli_labresult, eeli_pat);
	}
	
	@Override
	public void setFocus(){
		// TODO Automatisch erstellter Methoden-Stub
		
	}
	
	private void makeActions(){
		fwdAction = new Action(Messages.LaborView_nextPage) {
			@Override
			public void run(){
				resultsComposite.setColumnOffset(resultsComposite.getColumnOffset() + 1);
				resultsComposite.reload();
				tabFolder.setSelection(0);
			}
		};
		backAction = new Action(Messages.LaborView_prevPage) {
			@Override
			public void run(){
				resultsComposite.setColumnOffset(resultsComposite.getColumnOffset() - 1);
				resultsComposite.reload();
				tabFolder.setSelection(0);
			}
		};
		printAction = new Action(Messages.LaborView_print) {
			@Override
			public void run(){
				try {
					LaborblattView lb =
						(LaborblattView) getViewSite().getPage().showView(LaborblattView.ID);
					Patient pat = ElexisEventDispatcher.getSelectedPatient();
					lb.createLaborblatt(pat, resultsComposite.getPrintHeaders(),
						resultsComposite.getPrintRows(), resultsComposite.getSkipIndex());
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		importAction = new Action(Messages.LaborView_import) {
			{
				setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
				setToolTipText(Messages.LaborView_importToolTip);
			}
			
			@Override
			public void run(){
				Importer imp =
					new Importer(getViewSite().getShell(),
						ExtensionPointConstantsUi.LABORDATENIMPORT); //$NON-NLS-1$
				imp.create();
				imp.setMessage(Messages.LaborView_selectDataSource);
				imp.getShell().setText(Messages.LaborView_labImporterCaption);
				imp.setTitle(Messages.LaborView_labImporterText);
				imp.open();
			}
		};
		xmlAction = new Action(Messages.LaborView_xmlExport) {
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
							SWTHelper.alert(Messages.LaborView_ErrorCaption,
								Messages.LaborView_couldntwrite + fname);
							
						}
					}
				}
			}
		};
		newColumnAction = new Action(Messages.LaborView_newDate) {
			@Override
			public void run(){
				tabFolder.setSelection(0);
				resultsComposite.toggleNewColumn();
			}
		};
		newAction = new Action(Messages.LaborView_newDate) {
			@Override
			public void run(){
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient == null) {
					return;
				}
				TimeTool date = new TimeTool();
				LaborVerordnungDialog dialog =
					new LaborVerordnungDialog(getSite().getShell(), patient, date);
				if (dialog.open() == LaborVerordnungDialog.OK) {
					tabFolder.setSelection(1);
					ordersComposite.reload();
				}
			}
		};
		refreshAction = new Action(Messages.LaborView_Refresh) {
			@Override
			public void run(){
				resultsComposite.reload();
				ordersComposite.reload();
			}
		};
		expandAllAction = new Action("Expand All") {
			@Override
			public void run(){
				if (tabFolder.getSelectionIndex() == 0) {
					resultsComposite.expandAll();
				} else if (tabFolder.getSelectionIndex() == 1) {
					ordersComposite.expandAll();
				}
			}
			
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_ARROWDOWN.getImageDescriptor();
			}
		};
		collapseAllAction = new Action("Collapse All") {
			@Override
			public void run(){
				if (tabFolder.getSelectionIndex() == 0) {
					resultsComposite.collapseAll();
				} else if (tabFolder.getSelectionIndex() == 1) {
					ordersComposite.collapseAll();
				}
			}
			
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_ARROWUP.getImageDescriptor();
			}
		};
		
		newColumnAction.setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
		newAction.setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
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
			if (actpat == null) {
				return doc;
			}
			r.setAttribute("Patient", actpat.getLabel()); //$NON-NLS-1$
			doc.setRootElement(r);
			Element Daten = new Element("Daten"); //$NON-NLS-1$
			
			r.setAttribute("Patient", actpat.getLabel()); //$NON-NLS-1$
			
			HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> groupedResults =
				LabResult.getGrouped(actpat);
			
			List<String> dates = getDates(groupedResults);
			
			for (String d : dates) {
				Element dat = new Element("Datum"); //$NON-NLS-1$
				dat.setAttribute("Tag", d); //$NON-NLS-1$
				Daten.addContent(dat);
			}
			r.addContent(Daten);
			
			ArrayList<String> groupNames = new ArrayList<String>();
			groupNames.addAll(groupedResults.keySet());
			
			for (String g : groupNames) {
				Element eGroup = new Element("Gruppe"); //$NON-NLS-1$
				eGroup.setAttribute("Name", g); //$NON-NLS-1$
				
				HashMap<String, HashMap<String, List<LabResult>>> itemMap = groupedResults.get(g);
				List<LabItem> items = getItems(itemMap);
				if (items == null) {
					log.log("Ungültige Gruppe " + g, Log.WARNINGS); //$NON-NLS-1$
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
					
					HashMap<String, List<LabResult>> resultsPerDate =
						itemMap.get(it.getShortLabel());
					Set<String> resultDates = resultsPerDate.keySet();
					
					for (String date : resultDates) {
						List<LabResult> results = resultsPerDate.get(date);
						Element eResult = new Element("Resultat"); //$NON-NLS-1$
						eResult.setAttribute("Datum", date); //$NON-NLS-1$
						eItem.addContent(eResult);
						for (LabResult lr : results) {
							eResult.addContent(lr.getResult());
						}
						Element ref = new Element("Referenz"); //$NON-NLS-1$
						ref.setAttribute(Person.MALE, it.get("RefMann")); //$NON-NLS-1$ //$NON-NLS-2$
						ref.setAttribute(Person.FEMALE, it.get("RefFrauOrTx")); //$NON-NLS-1$ //$NON-NLS-2$
						eItem.addContent(ref);
					}
					eGroup.addContent(eItem);
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
	
	private List<LabItem> getItems(HashMap<String, HashMap<String, List<LabResult>>> itemMap){
		Set<String> keys = itemMap.keySet();
		ArrayList<LabItem> ret = new ArrayList<LabItem>();
		for (String string : keys) {
			Collection<List<LabResult>> values = itemMap.get(string).values();
			if (!values.isEmpty()) {
				Iterator<List<LabResult>> iter = values.iterator();
				List<LabResult> first = iter.next();
				while (first.isEmpty() && iter.hasNext()) {
					first = iter.next();
				}
				ret.add(first.get(0).getItem());
			}
		}
		return ret;
	}
	
	public List<String> getDates(
		HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> map){
		ArrayList<String> ret = new ArrayList<String>();
		HashSet<String> dateStrings = new HashSet<String>();
		for (String group : map.keySet()) {
			HashMap<String, HashMap<String, List<LabResult>>> itemMap = map.get(group);
			for (String item : itemMap.keySet()) {
				dateStrings.addAll(itemMap.get(item).keySet());
			}
		}
		ret.addAll(dateStrings);
		Collections.sort(ret);
		return ret;
	}
	
	public void activation(final boolean mode){}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public void reloadContents(final Class clazz){
		if (clazz.equals(LabItem.class)) {
			
		}
	}
	
}
