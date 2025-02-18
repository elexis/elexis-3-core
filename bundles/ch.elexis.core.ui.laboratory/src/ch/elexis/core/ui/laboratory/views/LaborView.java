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

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.laboratory.controls.LaborChartComposite;
import ch.elexis.core.ui.laboratory.controls.LaborOrdersComposite;
import ch.elexis.core.ui.laboratory.controls.LaborResultsComposite;
import ch.elexis.core.ui.laboratory.dialogs.LaborVerordnungDialog;
import ch.elexis.core.ui.util.Importer;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.IRefreshable;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Anzeige von Laboritems und Anzeige und Eingabemöglichkeit von Laborwerten.
 *
 * Der Algorithmus geht so: Zuerst werden alle Laboritems eingesammelt und
 * gemäss ihren Gruppen und Prioritäten sortiert (nur beim create) Beim Einlesen
 * eines neuen Patienten werden zunächst alle Daten gesammelt, an denen für
 * diesen Patienten Laborwerte vorliegen. Diese werden nach Alter sortiert und
 * mit den jeweiligen Laborwerten zusammengefasst. Jeweils NUMCOLUMNS Daten
 * werden auf einer Anzeigeseite angezeigt. Der Anwender kann auf den Seiten
 * blättern, aber es werden alle Laborwerte des aktuellen Patienten im Speicher
 * gehalten.
 *
 * @author gerry
 */
public class LaborView extends ViewPart implements IRefreshable {

	public static final String ID = "ch.elexis.Labor"; //$NON-NLS-1$
	private static Log log = Log.get("LaborView"); //$NON-NLS-1$

	private CTabFolder tabFolder;
	private LaborResultsComposite resultsComposite;
	private LaborOrdersComposite ordersComposite;
	private LaborChartComposite laborChartComposite;
	protected boolean isCompareMode;
	private Action fwdAction, backAction, printAction, importAction, xmlAction, newAction, newColumnAction,
			refreshAction, expandAllAction, collapseAllAction, selectAction;
	private ViewMenus menu;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);
	private List<TreeItem> selectedItems = new ArrayList<>();
	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			resultsComposite.selectPatient((Patient) NoPoUtil.loadAsPersistentObject(patient));
			resetCheckboxes();
		}, tabFolder);
		CoreUiUtil.runAsyncIfActive(() -> {
			ordersComposite.selectPatient((Patient) NoPoUtil.loadAsPersistentObject(patient));
		}, tabFolder);
		CoreUiUtil.runAsyncIfActive(() -> {
			laborChartComposite.selectPatient((Patient) NoPoUtil.loadAsPersistentObject(patient));
		}, tabFolder);
	}

	@Inject
	@Optional
	public void reload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (resultsComposite != null && !resultsComposite.isDisposed() && ordersComposite != null
				&& !ordersComposite.isDisposed() && laborChartComposite != null && !laborChartComposite.isDisposed()) {
			if (ILabItem.class.equals(clazz)) {
				Display.getDefault().asyncExec(() -> {
					resultsComposite.reload();
				});
			} else if (ILabResult.class.equals(clazz)) {
				Display.getDefault().asyncExec(() -> {
					resultsComposite.reload();
					ordersComposite.reload();
				});
			} else if (ILabOrder.class.equals(clazz)) {
				Display.getDefault().asyncExec(() -> {
					ordersComposite.reload();
				});
			}
		}
	}

	@Override
	public void createPartControl(final Composite parent) {
		setTitleImage(Images.IMG_VIEW_LABORATORY.getImage());

		tabFolder = new CTabFolder(parent, SWT.TOP);
		tabFolder.setLayout(new FillLayout());

		final CTabItem resultsTabItem = new CTabItem(tabFolder, SWT.NULL);
		resultsTabItem.setText("Resultate");
		resultsComposite = new LaborResultsComposite(tabFolder, SWT.NONE, this);
		resultsTabItem.setControl(resultsComposite);

		final CTabItem ordersTabItem = new CTabItem(tabFolder, SWT.NULL);
		ordersTabItem.setText("Verordnungen");
		ordersComposite = new LaborOrdersComposite(tabFolder, SWT.NONE);
		ordersTabItem.setControl(ordersComposite);

		final CTabItem chartTabItem = new CTabItem(tabFolder, SWT.NULL);
		chartTabItem.setText("Histogramm");
		chartTabItem.setForeground(UiDesk.getColor(UiDesk.COL_GREY));
		laborChartComposite = new LaborChartComposite(tabFolder, SWT.NONE, this);
		laborChartComposite.setLayout(new FillLayout());
		chartTabItem.setControl(laborChartComposite);
		chartTabItem.setData("enabled", false);
		chartTabItem.getControl().setEnabled(false);

		tabFolder.setSelection(0);

		tabFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (tabFolder.getSelection() == chartTabItem) {
					if (Boolean.TRUE.equals(chartTabItem.getData("enabled"))) {
						laborChartComposite.updateCharts(laborChartComposite.getChartsComposite());
					} else {
						tabFolder.setSelection(0);
					}
				} else {
					resultsComposite.reload();
					ordersComposite.reload();
				}
			}
		});
		makeActions();
		menu = new ViewMenus(getViewSite());
		menu.createMenu(newAction, backAction, fwdAction, printAction, importAction, xmlAction, selectAction);
		// Orders
		final LaborOrderPulldownMenuCreator menuCreator = new LaborOrderPulldownMenuCreator(parent.getShell());
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
		List<IAction> importers = Extensions.getClasses(
				Extensions.getExtensions(ExtensionPointConstantsUi.LABORDATENIMPORT), "ToolbarAction", //$NON-NLS-1$ //$NON-NLS-2$
				false);
		for (IAction ac : importers) {
			tm.add(ac);
		}
		if (!importers.isEmpty()) {
			tm.add(new Separator());
		}
		tm.add(selectAction);
		tm.add(refreshAction);
		tm.add(newColumnAction);
		tm.add(newAction);
		tm.add(backAction);
		tm.add(fwdAction);
		tm.add(expandAllAction);
		tm.add(collapseAllAction);
		tm.add(printAction);

		// register event listeners
		Patient act = (Patient) ElexisEventDispatcher.getSelected(Patient.class);
		if ((act != null && act != resultsComposite.getPatient())) {
			resultsComposite.selectPatient(act);
		}
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	@Override
	public void setFocus() {
		if (resultsComposite.isVisible()) {
			resultsComposite.setFocus();
		} else if (ordersComposite.isVisible()) {
			ordersComposite.setFocus();
		}
	}

	@Override
	public void refresh() {
		activePatient(ContextServiceHolder.get().getActivePatient().orElse(null));
	}

	private void makeActions() {
		fwdAction = new Action(Messages.LaborView_nextPage) {
			@Override
			public void run() {
				resultsComposite.setColumnOffset(resultsComposite.getColumnOffset() + 1);
				resultsComposite.reload();
				tabFolder.setSelection(0);
			}
		};
		backAction = new Action(Messages.LaborView_prevPage) {
			@Override
			public void run() {
				resultsComposite.setColumnOffset(resultsComposite.getColumnOffset() - 1);
				resultsComposite.reload();
				tabFolder.setSelection(0);
			}
		};
		printAction = new Action(Messages.Core_Print_ellipsis) {
			@Override
			public void run() {
				try {
					LaborblattView lb = (LaborblattView) getViewSite().getPage().showView(LaborblattView.ID);
					Patient pat = ElexisEventDispatcher.getSelectedPatient();
					lb.createLaborblatt(pat, resultsComposite.getPrintHeaders(), resultsComposite.getPrintRows(),
							resultsComposite.getSkipIndex());
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}
		};
		importAction = new Action(Messages.Core_Import_Action) {
			{
				setImageDescriptor(Images.IMG_IMPORT.getImageDescriptor());
				setToolTipText(Messages.LaborView_importToolTip);
			}

			@Override
			public void run() {
				Importer imp = new Importer(getViewSite().getShell(), ExtensionPointConstantsUi.LABORDATENIMPORT); // $NON-NLS-1$
				imp.create();
				imp.setMessage(Messages.Core_Choose_Import_Source);
				imp.getShell().setText(Messages.LaborView_labImporterCaption);
				imp.setTitle(Messages.LaborView_labImporterText);
				imp.open();
			}
		};
		xmlAction = new Action(Messages.LaborView_xmlExport) {
			@Override
			public void run() {
				Document doc = makeXML();
				if (doc != null) {
					FileDialog fsel = new FileDialog(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell());
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
							SWTHelper.alert(Messages.Core_Error, Messages.LaborView_couldntwrite + fname);

						}
					}
				}
			}
		};
		selectAction = new Action("Ausgewählte Werte vergleichen") {
			@Override
			public void run() {
				isCompareMode = !isCompareMode;
				CTabItem chartTabItem = tabFolder.getItem(2); // Annahme: Der Diagramm-Tab ist der dritte Tab
				if (isCompareMode) {
					setText("Ausgewählte Werte vergleichen");
					setImageDescriptor(Images.IMG_CHART_CURVE.getImageDescriptor());

					resultsComposite.showCheckboxes(true);
					chartTabItem.setData("enabled", true);
					chartTabItem.getControl().setEnabled(true);
					chartTabItem.setText("Histogramm");
					chartTabItem.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
					laborChartComposite.updateCharts(laborChartComposite.getChartsComposite());
				} else {
					setText("Vergleichen");
					setImageDescriptor(Images.IMG_CHART_CURVE.getImageDescriptor());

					resultsComposite.showCheckboxes(false);
					chartTabItem.setData("enabled", false);
					chartTabItem.getControl().setEnabled(false);
					chartTabItem.setText("Histogramm");
					chartTabItem.setForeground(UiDesk.getColor(UiDesk.COL_GREY));
				}
				resultsComposite.getViewer().refresh();
			}
		};


		newColumnAction = new Action(Messages.Core_prescribe_Laboratory) {
			@Override
			public void run() {
				tabFolder.setSelection(0);
				resultsComposite.toggleNewColumn();
			}
		};
		newAction = new Action(Messages.Core_prescribe_Laboratory) {
			@Override
			public void run() {
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient == null) {
					return;
				}
				TimeTool date = new TimeTool();
				LaborVerordnungDialog dialog = new LaborVerordnungDialog(getSite().getShell(), patient, date);
				if (dialog.open() == LaborVerordnungDialog.OK) {
					tabFolder.setSelection(1);
				}
			}
		};
		refreshAction = new Action(Messages.Core_Update) {
			@Override
			public void run() {
				resultsComposite.reload();
				ordersComposite.reload();
			}
		};
		expandAllAction = new Action(Messages.LaborView_expand_all) {
			@Override
			public void run() {
				if (tabFolder.getSelectionIndex() == 0) {
					resultsComposite.expandAll();
				}
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_ARROWDOWN.getImageDescriptor();
			}
		};
		collapseAllAction = new Action(Messages.LaborView_collapse_all) {
			@Override
			public void run() {
				if (tabFolder.getSelectionIndex() == 0) {
					resultsComposite.collapseAll();
				}
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
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
		selectAction.setImageDescriptor(Images.IMG_CHART_CURVE.getImageDescriptor());
	}

	public Document makeXML() {
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

			HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> groupedResults = LabResult
					.getGrouped(actpat);

			List<String> dates = getDates(groupedResults);

			for (String d : dates) {
				Element dat = new Element("Datum"); //$NON-NLS-1$
				dat.setAttribute("Tag", d); //$NON-NLS-1$
				Daten.addContent(dat);
			}
			r.addContent(Daten);

			ArrayList<String> groupNames = new ArrayList<>();
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
				if (items.isEmpty()) {
					continue;
				}
				for (LabItem it : items) {
					Element eItem = new Element("Parameter"); //$NON-NLS-1$
					eItem.setAttribute("Name", it.getName()); //$NON-NLS-1$
					eItem.setAttribute("Kürzel", it.getKuerzel()); //$NON-NLS-1$
					eItem.setAttribute("Einheit", it.getEinheit()); //$NON-NLS-1$

					HashMap<String, List<LabResult>> resultsPerDate = itemMap.get(it.getShortLabel());
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

	private List<LabItem> getItems(HashMap<String, HashMap<String, List<LabResult>>> itemMap) {
		Set<String> keys = itemMap.keySet();
		ArrayList<LabItem> ret = new ArrayList<>();
		for (String string : keys) {
			Collection<List<LabResult>> values = itemMap.get(string).values();
			if (!values.isEmpty()) {
				Iterator<List<LabResult>> iter = values.iterator();
				List<LabResult> first = iter.next();
				while (first.isEmpty() && iter.hasNext()) {
					first = iter.next();
				}
				ret.add((LabItem) first.get(0).getItem());
			}
		}
		return ret;
	}

	public List<String> getDates(HashMap<String, HashMap<String, HashMap<String, List<LabResult>>>> map) {
		ArrayList<String> ret = new ArrayList<>();
		HashSet<String> dateStrings = new HashSet<>();
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

	public void activation(final boolean mode) {
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	public void reloadContents(final Class clazz) {
		if (clazz.equals(LabItem.class)) {

		}
	}

	public void setSelectedItems(List<TreeItem> items) {
		this.selectedItems = new ArrayList<>(items);
	}

	public List<TreeItem> getSelectedItems() {
		return selectedItems;
	}

	private void resetCheckboxes() {
		if (resultsComposite != null) {
			resultsComposite.resetCheckboxes();
		}
	}

}
