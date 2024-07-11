/*******************************************************************************
 * Copyright (c) 2005-2023, MEDEVIT and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  <office@medevit.at> - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.views;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrderEntry;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IStockService.Availability;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.OrderServiceHolder;
import ch.elexis.core.services.holder.StockCommissioningServiceHolder;
import ch.elexis.core.services.holder.StockServiceHolder;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.OrderImportDialog;
import ch.elexis.core.ui.dialogs.StockSelectorDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.editors.KontaktSelektorDialogCellEditor;
import ch.elexis.core.ui.editors.NumericCellEditorValidator;
import ch.elexis.core.ui.editors.ReflectiveEditingSupport;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.dnd.IdentifiableDragSource;
import ch.elexis.core.ui.util.dnd.IdentifiableDropTarget;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider;
import ch.elexis.core.ui.views.provider.StockEntryLabelProvider.ColumnStockEntryLabelProvider;
import ch.elexis.data.PersistentObject;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;

public class StockView extends ViewPart implements IRefreshable {
	public StockView() {
	}

	public static final String ID = "ch.elexis.core.ui.views.StockView"; //$NON-NLS-1$
	private Logger log = LoggerFactory.getLogger(StockView.class);

	private Text filterText;
	private StockEntryLoader loader;

	private TableViewer viewer;
	private StockEntryLabelProvider labelProvider;

	private ViewMenus viewMenus;
	private IAction refreshAction, exportAction, scanInventoryAction;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private static final int STOCK = 0;
	private static final int PHARMACODE = 1;
	private static final int GTIN = 2;
	private static final int NAME = 3;
	private static final int VP = 4;
	private static final int MIN = 5;
	private static final int IST = 6;
	private static final int MAX = 7;
	private static final int SUPPLIER = 8;

	private StockViewComparator comparator;

	String[] columns = { Messages.Core_Stock, Messages.Core_Phamacode, Messages.LagerView_gtin, Messages.Core_Name,
			Messages.LagerView_vkPreis, Messages.LagerView_minBestand, Messages.LagerView_istBestand,
			Messages.LagerView_maxBestand, Messages.Core_Article_provider };
	int[] colwidth = { 50, 75, 90, 250, 50, 35, 35, 35, 150 };

	@Override
	public void createPartControl(Composite parent) {
		labelProvider = new StockEntryLabelProvider();
		parent.setLayout(new GridLayout(2, false));

		filterText = new Text(parent, SWT.BORDER | SWT.SEARCH);
		filterText.setMessage("Filter");
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		filterText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				refresh();
			}
		});
		ToolBarManager tbm = new ToolBarManager();
		tbm.add(new Action(StringUtils.EMPTY, Action.AS_CHECK_BOX) {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_FILTER.getImageDescriptor();
			}

			@Override
			public String getToolTipText() {
				return "Nur zu bestellende anzeigen";
			}

			@Override
			public void run() {
				StockEntryLoader.setFilterOrderOnly(isChecked());
				refresh();
			}
		});
		tbm.add(new Action(StringUtils.EMPTY, Action.AS_CHECK_BOX) {
			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_CART.getImageDescriptor();
			}

			@Override
			public String getToolTipText() {
				return "Nur einzubuchende anzeigen";
			}

			@Override
			public void run() {
				StockEntryLoader.setFilterBookInOnly(isChecked());
				refresh();
			}
		});
		tbm.createControl(parent);

		viewer = new TableViewer(parent, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.SINGLE | SWT.VIRTUAL);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(labelProvider);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		for (int i = 0; i < columns.length; i++) {
			TableViewerColumn tvc = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn tc = tvc.getColumn();
			tc.setText(columns[i]);
			tc.setWidth(colwidth[i]);
			tc.setData(i);
			tvc.setLabelProvider(new ColumnStockEntryLabelProvider(i, labelProvider));

			final int columnIndex = i;
			tc.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comparator.setColumn(columnIndex);
					refresh();
				}
			});

			ReflectiveEditingSupport poes = null;
			if (i == 5) {
				poes = new ReflectiveEditingSupport(viewer, ModelPackage.Literals.ISTOCK_ENTRY__MINIMUM_STOCK.getName(),
						new NumericCellEditorValidator(), true);
			} else if (i == 6) {
				poes = new ReflectiveEditingSupport(viewer, ModelPackage.Literals.ISTOCK_ENTRY__CURRENT_STOCK.getName(),
						new NumericCellEditorValidator(), true) {
					@Override
					protected boolean canEdit(Object element) {
						boolean canEdit = super.canEdit(element);
						if (canEdit) {
							IStockEntry se = (IStockEntry) element;
							return !se.getStock().isCommissioningSystem();
						}
						return true;
					};
				};
			} else if (i == 7) {
				poes = new ReflectiveEditingSupport(viewer, ModelPackage.Literals.ISTOCK_ENTRY__MAXIMUM_STOCK.getName(),
						new NumericCellEditorValidator(), true);
			}

			if (poes != null) {
				tvc.setEditingSupport(poes.setModelService(CoreModelServiceHolder.get()));
			}

			if (i == 8) {
				EditingSupport providerEditingSupport = new EditingSupport(viewer) {

					@Override
					protected void setValue(Object element, Object value) {
						if (value instanceof PersistentObject) {
							value = NoPoUtil.loadAsIdentifiable((PersistentObject) value, IContact.class).orElse(null);
						}

						IStockEntry se = (IStockEntry) element;
						if (se == null) {
							return;
						}

						LockResponse lr = LocalLockServiceHolder.get().acquireLock(se);
						if (!lr.isOk()) {
							return;
						}

						se.setProvider((IContact) value);

						lr = LocalLockServiceHolder.get().releaseLock((se));
						if (!lr.isOk()) {
							log.warn("Error releasing lock for [{}]: {}", se.getId(), lr.getStatus()); //$NON-NLS-1$
						}
						getViewer().refresh();
					}

					@Override
					protected Object getValue(Object element) {
						IStockEntry se = (IStockEntry) element;
						if (se == null) {
							return null;
						}
						return se.getProvider();
					}

					@Override
					protected CellEditor getCellEditor(Object element) {
						return new KontaktSelektorDialogCellEditor(((TableViewer) getViewer()).getTable(),
								"Lieferant auswählen", "Bitte selektieren Sie den Lieferant");
					}

					@Override
					protected boolean canEdit(Object element) {
						IStockEntry stockEntry = (IStockEntry) element;
						return (stockEntry != null && stockEntry.getArticle() != null);
					}
				};
				tvc.setEditingSupport(providerEditingSupport);
			}
		}
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer,
				new FocusCellHighlighter(viewer) {
				});
		ColumnViewerEditorActivationStrategy editorActivationStrategy = new ColumnViewerEditorActivationStrategy(
				viewer) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				ViewerCell cell = (ViewerCell) event.getSource();
				return cell.getColumnIndex() > 4 && cell.getColumnIndex() < 9;
			}
		};
		TableViewerEditor.create(viewer, focusCellManager, editorActivationStrategy,
				TableViewerEditor.TABBING_HORIZONTAL);

		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		contextMenu.addMenuListener(new IMenuListener() {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new CheckInOrderedAction(viewer));
				manager.add(new DeleteStockEntryAction(viewer));
				ArticleMachineOutputAction amoa = new ArticleMachineOutputAction(viewer);
				if (amoa.isVisible()) {
					manager.add(amoa);
					manager.add(new FullMachineInventoryAction(viewer));
				}
			}
		});

		new IdentifiableDropTarget(viewer.getControl(), new IdentifiableDropTarget.IReceiver() {

			@Override
			public void dropped(List<Identifiable> identifiables) {
				for (Identifiable identifiable : identifiables) {
					if (identifiable instanceof IArticle) {
						if (!((IArticle) identifiable).isProduct()) {
							StockSelectorDialog ssd = new StockSelectorDialog(UiDesk.getTopShell(), false);
							int open = ssd.open();
							if (open == Dialog.OK) {
								if (ssd.getResult().length > 0) {
									IStock stock = (IStock) ssd.getResult()[0];
									if (stock != null) {
										StockServiceHolder.get().storeArticleInStock(stock,
												StoreToStringServiceHolder.getStoreToString(identifiable));
										viewer.refresh();
									}
								}
							}
						}
					}
				}
			}

			@Override
			public boolean accept(List<Identifiable> identifiables) {
				for (Identifiable identifiable : identifiables) {
					if (!(identifiable instanceof IArticle)) {
						return false;
					}
				}
				return true;
			}
		});

		// add drag support
		new IdentifiableDragSource(viewer);

		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);

		comparator = new StockViewComparator();
		viewer.setComparator(comparator);

		makeActions();
		viewMenus = new ViewMenus(getViewSite());
		viewMenus.createToolbar(refreshAction, scanInventoryAction);
		viewMenus.createMenu(exportAction);

		getSite().getPage().addPartListener(udpateOnVisible);

	}

	@Optional
	@Inject
	public void udpate(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IStockEntry entry) {
		if (entry != null) {
			if (viewer != null && !viewer.getControl().isDisposed()) {
				viewer.update(entry, null);
			}
		}
	}

	private void makeActions() {
		scanInventoryAction = new Action(Messages.StockView_InventoryMode) {
			{
				setImageDescriptor(Images.IMG_SCANNER_BARCODE.getImageDescriptor());
			}

			@Override
			public void run() {
				new OrderImportDialog(UiDesk.getTopShell(), null, OrderImportDialog.ACTION_MODE_INVENTORY).open();
			}
		};

		refreshAction = new Action(Messages.StockView_reload) {
			{
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}

			@Override
			public void run() {
				refresh();
			}
		};

		exportAction = new Action(Messages.Core_export_to_csv, Images.IMG_EXPORT.getImageDescriptor()) {
			@Override
			public void run() {
				FileDialog dialog = new FileDialog(UiDesk.getTopShell(), SWT.SAVE);
				dialog.setFilterExtensions(new String[] { "*.csv" }); //$NON-NLS-1$
				dialog.setFilterNames(new String[] { "Comma Separated Values (CSV)" });

				dialog.setOverwrite(true);
				dialog.setFileName("lager_export.csv"); //$NON-NLS-1$
				String pathToSave = dialog.open();
				if (pathToSave != null) {
					CSVWriter csv = null;
					try {
						int errorUnknownArticle = 0;
						int success = 0;
						FileOutputStream fos = new FileOutputStream(pathToSave);
						OutputStreamWriter osw = new OutputStreamWriter(fos, "ISO-8859-1");
						csv = new CSVWriter(osw);
						log.debug("csv export started for: " + pathToSave); //$NON-NLS-1$
						String[] header = new String[] { "Name", "Pharmacode", "EAN", "Max", "Min",
								"Aktuell Packung an Lager", "Aktuell an Lager (Anbruch)", "Stück pro Packung",
								"Stück pro Abgabe", "Einkaufspreis", "Verkaufspreis", "Typ (P, N, ...)", "Lieferant" };
						csv.writeNext(header);

						List<IStockEntry> entries = CoreModelServiceHolder.get().getQuery(IStockEntry.class).execute();
						for (IStockEntry iStockEntry : entries) {
							String[] line = new String[header.length];
							IArticle article = iStockEntry.getArticle();
							if (article != null) {
								line[0] = article.getLabel();
								line[1] = article.getCode();
								line[2] = article.getGtin();
								line[3] = String.valueOf(iStockEntry.getMaximumStock());
								line[4] = String.valueOf(iStockEntry.getMinimumStock());
								line[5] = String.valueOf(iStockEntry.getCurrentStock());
								line[6] = String.valueOf(iStockEntry.getFractionUnits());
								line[7] = String.valueOf(article.getPackageSize());
								line[8] = String.valueOf(article.getSellingSize());
								line[9] = new Money(article.getPurchasePrice()).getAmountAsString();
								line[10] = new Money(article.getSellingPrice()).getAmountAsString();
								line[11] = Character.toString(article.getSubTyp().getTypeChar());
								IContact provider = iStockEntry.getProvider();
								if (provider != null) {
									line[12] = provider.getLabel();
								}
								csv.writeNext(line);
								success++;
							} else {
								errorUnknownArticle++;
								log.warn("cannot export: id [" + iStockEntry.getId() + "] " + iStockEntry.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
						csv.close();
						log.debug("csv export finished for: " + pathToSave); //$NON-NLS-1$
						StringBuffer msg = new StringBuffer();
						msg.append("Der Export nach ");
						msg.append(pathToSave);
						msg.append(" ist abgeschlossen.");
						msg.append("\n\n");
						msg.append(success);
						msg.append(" Artikel wurden erfolgreich exportiert.");
						if (errorUnknownArticle > 0) {
							msg.append(StringUtils.LF);
							msg.append(errorUnknownArticle);
							msg.append(" Artikel konnten nicht exportiert werden (Unbekannte Artikel Typen).");
						}
						SWTHelper.showInfo("Lager export", msg.toString());
					} catch (Exception ex) {
						ExHandler.handle(ex);
						log.error("csv exporter error", ex); //$NON-NLS-1$
						SWTHelper.showError("Fehler", ex.getMessage());
					} finally {
						if (csv != null) {
							try {
								csv.close();
							} catch (IOException e) {
								log.error("cannot close csv exporter", e); //$NON-NLS-1$
							}
						}
					}
				}

			}
		};
	}

	@Override
	public void setFocus() {
		filterText.setFocus();
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	public class FullMachineInventoryAction extends Action {
		private Viewer viewer;

		public FullMachineInventoryAction(Viewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_SYNC.getImageDescriptor();
		}

		@Override
		public String getText() {
			return Messages.StockView_PerformFullInventoryOnCommSystem;
		}

		@Override
		public void run() {
			IStockEntry stockEntry = fetchSelection();
			IStatus status = StockCommissioningServiceHolder.get().synchronizeInventory(stockEntry.getStock(), null,
					null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}

		private IStockEntry fetchSelection() {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof IStockEntry) {
				return (IStockEntry) selection.getFirstElement();
			}
			return null;
		};
	}

	public class ArticleMachineOutputAction extends Action {
		private Viewer viewer;

		public ArticleMachineOutputAction(Viewer viewer) {
			this.viewer = viewer;
		}

		public boolean isVisible() {
			IStockEntry stockEntry = fetchSelection();
			if (stockEntry != null) {
				IStock stock = stockEntry.getStock();
				if (stock != null) {
					String driverUuid = stock.getDriverUuid();
					return (driverUuid != null && driverUuid.length() > 8);
				}
			}
			return false;
		}

		@Override
		public String getText() {
			return Messages.StockView_OutlayArticle;
		}

		@Override
		public void run() {
			IStockEntry stockEntry = fetchSelection();
			IStatus status = StockCommissioningServiceHolder.get().performArticleOutlay(stockEntry, 1, null);
			if (!status.isOK()) {
				ElexisStatus elStatus = new ElexisStatus(status);
				StatusManager.getManager().handle(elStatus, StatusManager.SHOW);
			}
		}

		private IStockEntry fetchSelection() {
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof IStockEntry) {
				return (IStockEntry) selection.getFirstElement();
			}
			return null;
		};
	}

	public class CheckInOrderedAction extends Action {
		private Viewer viewer;
		private IStockEntry stockEntry;

		public CheckInOrderedAction(Viewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean isEnabled() {
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry.getArticle() != null) {
					return (OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry) != null);
				}
			}
			return false;
		}

		@Override
		public String getText() {
			return Messages.BestellView_CheckInCaption;
		}

		@Override
		public void run() {
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry.getArticle() != null) {
					IOrderEntry orderEntry = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(stockEntry);
					OrderImportDialog dialog = new OrderImportDialog(viewer.getControl().getShell(),
							orderEntry.getOrder());
					dialog.open();
					viewer.refresh();
				}
			}
		}
	}

	public class DeleteStockEntryAction extends Action {
		private Viewer viewer;
		private IStockEntry stockEntry;

		public DeleteStockEntryAction(Viewer viewer) {
			this.viewer = viewer;
		}

		@Override
		public boolean isEnabled() {
			stockEntry = null;
			IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty() && selection.getFirstElement() instanceof IStockEntry) {
				stockEntry = (IStockEntry) selection.getFirstElement();
				if (stockEntry != null) {
					IStock stock = stockEntry.getStock();
					return stock != null && !stock.isCommissioningSystem();
				}
			}
			return false;
		}

		@Override
		public String getText() {
			return Messages.Core_Remove;
		}

		@Override
		public String getToolTipText() {
			return Messages.LagerView_deleteActionToolTip;
		}

		@Override
		public void run() {
			if (stockEntry != null) {
				IArticle article = stockEntry.getArticle();
				if (article != null && MessageDialog.openConfirm(viewer.getControl().getShell(),
						Messages.LagerView_deleteActionConfirmCaption,
						MessageFormat.format(Messages.LagerView_deleteConfirmBody, article.getName()))) {
					CoreModelServiceHolder.get().delete(stockEntry);
					viewer.refresh();
				}
			}
		}
	}

	private static class StockEntryLoader extends Job {
		private Viewer viewer;

		private static boolean filterOrderOnly = false;
		private static boolean filterBookInOnly = false;
		private String filter;

		private List<ch.elexis.core.model.IStockEntry> loaded;

		public StockEntryLoader(Viewer viewer, String filter) {
			super("Stock loading ...");
			this.viewer = viewer;
			this.filter = filter;
		}

		public static void setFilterOrderOnly(boolean checked) {
			filterOrderOnly = checked;
		}

		public static void setFilterBookInOnly(boolean checked) {
			filterBookInOnly = checked;
		}

		public StockEntryLoader(Viewer viewer) {
			super("Stock loading ...");
			this.viewer = viewer;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Stock loading ...", IProgressMonitor.UNKNOWN);
			loaded = StockServiceHolder.get().getAllStockEntries(false);
			if (filterOrderOnly) {
				loaded = loaded.parallelStream().filter(se -> selectOrderOnly(se)).collect(Collectors.toList());
			}
			if (filterBookInOnly) {
				loaded = loaded.parallelStream().filter(se -> selectToOrderOnly(se)).collect(Collectors.toList());
			}
			if (filter != null) {
				loaded = loaded.parallelStream().filter(se -> selectFilter(se, filter)).collect(Collectors.toList());
			}

			loaded.sort(compareArticleLabel());

			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			monitor.done();
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					viewer.setInput(loaded);
				}
			});
			return Status.OK_STATUS;
		}

		private Comparator<IStockEntry> compareArticleLabel() {
			return Comparator.comparing(o -> o.getArticle() != null ? o.getArticle().getLabel() : null,
					Comparator.nullsLast(Comparator.naturalOrder()));
		}

		private boolean selectOrderOnly(IStockEntry se) {
			Availability availability = StockServiceHolder.get().determineAvailability(se);
			if (availability != null) {
				switch (availability) {
				case CRITICAL_STOCK:
				case OUT_OF_STOCK:
					return true;
				default:
					return false;
				}
			}
			return false;
		}

		private boolean selectToOrderOnly(IStockEntry se) {
			IOrderEntry availability = OrderServiceHolder.get().findOpenOrderEntryForStockEntry(se);
			if (availability != null) {
				return true;
			}
			return false;
		}

		private boolean selectFilter(IStockEntry se, String filter) {
			if (se.getArticle() != null && se.getArticle().getLabel() != null) {
				if (se.getArticle().getLabel().toLowerCase().contains(filter.toLowerCase())) {
					return true;
				} else if (se.getArticle().getGtin() != null && se.getArticle().getGtin().contains(filter)) {
					return true;
				}
			}
			return false;
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Override
	public void refresh() {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			String search = filterText.getText();
			if (search != null && search.length() > 2) {
				if (loader != null) {
					loader.cancel();
				}
				loader = new StockEntryLoader(viewer, search);
				loader.schedule();
			} else {
				loader = new StockEntryLoader(viewer);
				loader.schedule();
			}
		}
	}

	public class StockViewComparator extends ViewerComparator {
		private int propertyIndex;

		private int direction = 1;

		public void setColumn(int column) {
			if (column == propertyIndex) {
				direction *= -1;
			}
			this.propertyIndex = column;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			IStockEntry s1 = (IStockEntry) e1;
			IStockEntry s2 = (IStockEntry) e2;

			switch (propertyIndex) {
			case STOCK:
				return s1.getStock().getId().compareTo(s2.getStock().getId()) * direction;
			case PHARMACODE:
				String pharmco1 = s1.getArticle().getCode();
				String pharmco2 = s2.getArticle().getCode();
				return Objects.compare(pharmco1, pharmco2, Comparator.nullsFirst(Comparator.naturalOrder()))
						* direction;
			case GTIN:
				String gtin1 = s1.getArticle().getGtin();
				String gtin2 = s2.getArticle().getGtin();
				return Objects.compare(gtin1, gtin2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			case NAME:
				String name1 = s1.getArticle().getName();
				String name2 = s2.getArticle().getName();
				return Objects.compare(name1, name2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			case VP:
				Money sp1 = s1.getArticle().getSellingPrice();
				Money sp2 = s2.getArticle().getSellingPrice();
				return Objects.compare(sp1, sp2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			case MIN:
				return Integer.compare(s1.getMinimumStock(), s2.getMinimumStock()) * direction;
			case IST:
				return Integer.compare(s1.getCurrentStock(), s2.getCurrentStock()) * direction;
			case MAX:
				return Integer.compare(s1.getMaximumStock(), s2.getMaximumStock()) * direction;
			case SUPPLIER: {
				String lieferant1 = ""; //$NON-NLS-1$
				String lieferant2 = ""; //$NON-NLS-1$
				if (s1.getProvider() != null) {
					lieferant1 = s1.getProvider().getLabel();
				}
				if (s2.getProvider() != null) {
					lieferant2 = s2.getProvider().getLabel();
				}
				return lieferant1.compareTo(lieferant2) * direction;
			}
			}

			return super.compare(viewer, e1, e2);
		}
	}
}
