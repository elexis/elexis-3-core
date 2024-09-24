/*******************************************************************************

 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IBillable;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.ICustomService;
import ch.elexis.core.model.IDiagnosis;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.IService;
import ch.elexis.core.model.ac.EvACEs;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.IBillingService;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.BillingServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.ResultDialog;
import ch.elexis.core.ui.dialogs.StatusDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockDeniedNoActionLockHandler;
import ch.elexis.core.ui.processor.ArticleProcessor;
import ch.elexis.core.ui.util.CoreCommandUiUtil;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.core.ui.views.controls.InteractionLink;
import ch.elexis.data.Artikel;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite implements IUnlockable {

	private Text billedLabel;
	private InteractionLink interactionLink;
	private Table table;
	private TableViewer viewer;
	private MenuManager contextMenuManager;
	private IEncounter actEncounter;
	private String defaultRGB;
	private IWorkbenchPage page;
	private final GenericObjectDropTarget dropTarget;
	private IAction applyMedicationAction, chPriceAction, chCountAction, chTextAction, removeAction, removeAllAction;
	private TableColumnLayout tableLayout;

	private static final String INDICATED_MEDICATION = Messages.VerrechnungsDisplay_indicatedMedication;
	private static final String APPLY_MEDICATION = Messages.VerrechnungsDisplay_applyMedication;
	private static final String CHPRICE = Messages.VerrechnungsDisplay_changePrice;
	private static final String CHCOUNT = Messages.Core_Change_Number;
	private static final String REMOVE = Messages.VerrechnungsDisplay_removeElements;
	private static final String CHTEXT = Messages.VerrechnungsDisplay_changeText;
	private static final String REMOVEALL = Messages.VerrechnungsDisplay_removeAll;

	static Logger logger = LoggerFactory.getLogger(VerrechnungsDisplay.class);
	private ArticleProcessor articleProcessor;

	@Optional
	@Inject
	public void udpateEncounter(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IEncounter encounter) {
		if (encounter != null && encounter.equals(actEncounter) && isViewerCreated()) {
			setEncounter(actEncounter);
		}
	}

	@Optional
	@Inject
	public void udpateBilled(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IBilled billed) {
		if (billed != null && isViewerCreated()) {
			viewer.update(billed, null);
		}
	}

	private ToolBarManager toolBarManager;
	private TableViewerColumn partDisposalColumn;

	public VerrechnungsDisplay(final IWorkbenchPage p, Composite parent, int style) {
		super(parent, style);
		final int columns_for_each_drug = 4;
		setLayout(new GridLayout(columns_for_each_drug, false));
		this.page = p;
		defaultRGB = UiDesk.createColor(new RGB(255, 255, 255));

		Label label = new Label(this, SWT.NONE);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);
		label.setText(Messages.Invoice_amount_billed);

		billedLabel = new Text(this, SWT.WRAP);
		billedLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		interactionLink = new InteractionLink(this, SWT.NONE);
		interactionLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		toolBarManager = new ToolBarManager(SWT.RIGHT);
		IAction newAction = new Action() {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_NEW.getImageDescriptor();
			}

			@Override
			public void run() {
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper.alert(Messages.Core_Error, "LeistungenView.ID"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					page.showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status = new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.VerrechnungsDisplay_errorStartingCodeWindow + ex.getMessage(), ex,
							ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}

			@Override
			public boolean isEnabled() {
				return actEncounter != null && actEncounter.isBillable();
			}
		};
		newAction.setToolTipText(Messages.VerrechnungsDisplay_AddItem);
		toolBarManager.add(newAction);

		toolBarManager.add(new Action(StringUtils.EMPTY, Action.AS_CHECK_BOX) {

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_NOBILLING.getImageDescriptor();
			}

			@Override
			public String getText() {
				return Messages.VerrechnungsDisplay_no_invoice;
			}

			@Override
			public void run() {
				actEncounter.setBillable(!actEncounter.isBillable());
				CoreModelServiceHolder.get().save(actEncounter);
				updateUi();
			}

			@Override
			public boolean isChecked() {
				if (actEncounter != null) {
					return !actEncounter.isBillable();
				}
				return false;
			}

			@Override
			public boolean isEnabled() {
				return actEncounter != null && !isBilled(actEncounter);
			}

			private boolean isBilled(IEncounter encounter) {
				if (encounter != null) {
					return encounter.getInvoice() != null;
				}
				return false;
			}
		});

		// Populate toolbar contribution manager
		IMenuService menuService = PlatformUI.getWorkbench().getService(IMenuService.class);
		menuService.populateContributionManager(toolBarManager, "toolbar:ch.elexis.VerrechnungsDisplay"); //$NON-NLS-1$

		ToolBar toolBar = toolBarManager.createControl(this);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		makeActions();
		tableLayout = new TableColumnLayout();
		Composite tableComposite = new Composite(this, SWT.NONE);

		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, columns_for_each_drug, 1));
		tableComposite.setLayout(tableLayout);
		viewer = new TableViewer(tableComposite,
				SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		table = viewer.getTable();
		table.setMenu(createVerrMenu());
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createColumns();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty() && (selection.getFirstElement() instanceof IBilled)) {
					ContextServiceHolder.get().getRootContext().setTyped(selection.getFirstElement());
					CoreUiUtil.setCommandSelection("ch.elexis.VerrechnungsDisplay", selection.toList());
				}
			}
		});
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					if (table.getSelectionIndices().length >= 1 && removeAction != null) {
						removeAction.run();
					}
				}
			}
		});
		// connect double click on column to actions
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				int clickedIndex = -1;
				// calculate column of click
				int width = 0;
				TableColumn[] columns = table.getColumns();
				for (int i = 0; i < columns.length; i++) {
					TableColumn tc = columns[i];
					if (width < e.x && e.x < width + tc.getWidth()) {
						clickedIndex = i;
						break;
					}
					width += tc.getWidth();
				}
				if (clickedIndex != -1) {
					if (clickedIndex == 1) {
						chCountAction.run();
					} else if (clickedIndex == 4) {
						chPriceAction.run();
					} else if (clickedIndex == 5) {
						removeAction.run();
					}
				}
			}
		});

		dropTarget = new GenericObjectDropTarget(Messages.VerrechnungsDisplay_doBill, table, new DropReceiver()) {
			@Override
			protected Control getHighLightControl() {
				return VerrechnungsDisplay.this;
			}
		};
	}

	private void createColumns() {
		String[] titles = { StringTool.leer, Messages.Display_Column_Number, Messages.Core_Code,
				Messages.Core_Description, Messages.Core_Price, StringTool.leer };
		int[] weights = { 0, 8, 20, 50, 15, 7 };

		partDisposalColumn = createTableViewerColumn(titles[0], weights[0], 0, SWT.LEFT);
		partDisposalColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					if (isPartDisposal(billed)) {
						return Images.IMG_BLOCKS_SMALL.getImage();
					}
				}
				return super.getImage(element);
			}
		});
		TableViewerColumn col = createTableViewerColumn(titles[1], weights[1], 1, SWT.LEFT);
		col.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		        if (element instanceof IBilled) {
		            IBilled billed = (IBilled) element;
		            return Double.toString(billed.getAmount());
		        }
		        return StringUtils.EMPTY;
		    }
		});
		ViewerComparator numberComparator = new ViewerComparator() {
		    @Override
		    public int compare(Viewer viewer, Object e1, Object e2) {
		        if (e1 instanceof IBilled && e2 instanceof IBilled) {
		            IBilled b1 = (IBilled) e1;
		            IBilled b2 = (IBilled) e2;

		            double amount1 = b1.getAmount();
		            double amount2 = b2.getAmount();

		            int result = Double.compare(amount1, amount2);

		            return getSortedAscending(viewer) ? result : -result;
		        }
		        return 0;
		    }

		    private boolean getSortedAscending(Viewer viewer) {
		        if (viewer.getData("numberSortAscending") != null) {
		            return (Boolean) viewer.getData("numberSortAscending");
		        }
		        return true;
		    }
		};

		col.getColumn().addSelectionListener(new SelectionAdapter() {
		    @Override
		    public void widgetSelected(SelectionEvent e) {
		        viewer.setComparator(numberComparator);
		        if (viewer.getComparator() == numberComparator) {
					if (viewer.getData("numberSortAscending") != null) {
						viewer.setData("numberSortAscending", !(Boolean) viewer.getData("numberSortAscending"));
					} else {
						viewer.setData("numberSortAscending", Boolean.FALSE);
					}
					viewer.refresh();
				}
			}
		});

		col = createTableViewerColumn(titles[2], weights[2], 2, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return getServiceCode(billed);
				}
				return StringUtils.EMPTY;
			}
		});

		ViewerComparator codeComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof IBilled && e2 instanceof IBilled) {
					IBilled b1 = (IBilled) e1;
					IBilled b2 = (IBilled) e2;
					int result = b1.getCode().compareTo(b2.getCode());
					return getSortedAscending(viewer) ? result : -result;
				}
				return 0;
			}

			private boolean getSortedAscending(Viewer viewer) {
				if (viewer.getData("codeSortAscending") != null) {
					return (Boolean) viewer.getData("codeSortAscending");
				}
				return true;
			}
		};

		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setComparator(codeComparator);
				if (viewer.getComparator() == codeComparator) {
					if (viewer.getData("codeSortAscending") != null) {
						viewer.setData("codeSortAscending", !(Boolean) viewer.getData("codeSortAscending"));
					} else {
						viewer.setData("codeSortAscending", Boolean.FALSE);
					}
					viewer.refresh();
				}
			}
		});

		col = createTableViewerColumn(titles[3], weights[3], 3, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return billed.getText();
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Color getBackground(final Object element) {
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					return getBackgroundColor(billed);
				}
				return null;
			}
		});

		ViewerComparator descriptionComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof IBilled && e2 instanceof IBilled) {
					IBilled b1 = (IBilled) e1;
					IBilled b2 = (IBilled) e2;
					int result = b1.getText().compareTo(b2.getText());
					return getSortedAscending(viewer) ? result : -result;
				}
				return 0;
			}

			private boolean getSortedAscending(Viewer viewer) {
				if (viewer.getData("descriptionSortAscending") != null) {
					return (Boolean) viewer.getData("descriptionSortAscending");
				}
				return true;
			}
		};

		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setComparator(descriptionComparator);
				if (viewer.getComparator() == descriptionComparator) {
					if (viewer.getData("descriptionSortAscending") != null) {
						viewer.setData("descriptionSortAscending",
								!(Boolean) viewer.getData("descriptionSortAscending"));
					} else {
						viewer.setData("descriptionSortAscending", Boolean.FALSE);
					}
					viewer.refresh();
				}
			}
		});

		col = createTableViewerColumn(titles[4], weights[4], 4, SWT.RIGHT);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBilled) {
					IBilled billed = (IBilled) element;
					Money price = billed.getTotal();
					return price.getAmountAsString();
				}
				return StringUtils.EMPTY;
			}
		});
		ViewerComparator priceComparator = new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof IBilled && e2 instanceof IBilled) {
					IBilled b1 = (IBilled) e1;
					IBilled b2 = (IBilled) e2;
					int result = b1.getTotal().compareTo(b2.getTotal());
					return getSortedAscending(viewer) ? result : -result;
				}
				return 0;
			}

			private boolean getSortedAscending(Viewer viewer) {
				if (viewer.getData("priceSortAscending") != null) {
					return (Boolean) viewer.getData("priceSortAscending");
				}
				return true;
			}
		};
		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setComparator(priceComparator);
				if (viewer.getComparator() == priceComparator) {
					if (viewer.getData("priceSortAscending") != null) {
						viewer.setData("priceSortAscending", !(Boolean) viewer.getData("priceSortAscending"));
					} else {
						viewer.setData("priceSortAscending", Boolean.FALSE);
					}
					viewer.refresh();
				}
			}
		});

		col = createTableViewerColumn(titles[5], weights[5], 5, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				return Images.IMG_DELETE.getImage();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int weight, int colNumber, int style) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		column.setMoveable(false);
		tableLayout.setColumnData(column, new ColumnWeightData(weight));
		return viewerColumn;
	}

	private Color getBackgroundColor(IBilled billed) {
		IBillable billable = billed.getBillable();
		if (billable != null) {
			Color color = UiDesk.getColorFromRGB(defaultRGB);
			String codeName = billable.getCodeSystemName();

			if (codeName != null) {
				String rgbColor = ConfigServiceHolder.getGlobal(Preferences.LEISTUNGSCODES_COLOR + codeName,
						defaultRGB);
				color = UiDesk.getColorFromRGB(rgbColor);
			}
			return color;
		}
		return null;
	}

	public void clear() {
		actEncounter = null;
		setEncounter(actEncounter);
	}

	private boolean isViewerCreated() {
		return viewer != null && viewer.getTable() != null && !viewer.getTable().isDisposed();
	}

	private void updateBilledLabel() {
		ArrayList<IArticle> gtins = new ArrayList<>();
		if (actEncounter != null) {
			int sumMinutes = 0;
			Money sum = new Money(0);
			for (IBilled billed : actEncounter.getBilled()) {
				Money preis = billed.getTotal();
				sum.addMoney(preis);
				IBillable billable = billed.getBillable();
				if (billable instanceof IService) {
					sumMinutes += (((IService) billable).getMinutes() * billed.getAmount());
				}
				if (billable instanceof IArticle) {
					IArticle art = (IArticle) billable;
					gtins.add(art);
				}
			}
			interactionLink.updateAtcs(gtins);
			billedLabel.setText(String.format("%s %s / %s %s", //$NON-NLS-1$
					Messages.Core_Amount, sum.getAmountAsString(), Messages.Core_Time, sumMinutes));
		} else {
			billedLabel.setText(StringUtils.EMPTY);
		}
		layout();
	}

	/**
	 * Add a {@link PersistentObject} to the encounter.
	 *
	 * @param o
	 * @deprecated for {@link ch.elexis.core.model.ICodeElement} instances direct
	 *             use of {@link IBillingService} is recommended
	 */
	@Deprecated
	public void addPersistentObject(PersistentObject o) {
		if (actEncounter != null) {
			if (o instanceof Leistungsblock) {
				Leistungsblock block = (Leistungsblock) o;
				List<ch.elexis.core.data.interfaces.ICodeElement> elements = block.getElements();
				for (ch.elexis.core.data.interfaces.ICodeElement element : elements) {
					if (element instanceof PersistentObject) {
						addPersistentObject((PersistentObject) element);
					}
				}
				List<ch.elexis.core.data.interfaces.ICodeElement> diff = block.getDiffToReferences(elements);
				if (!diff.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					diff.forEach(r -> {
						if (sb.length() > 0) {
							sb.append(StringUtils.LF);
						}
						sb.append(r);
					});
					MessageDialog.openWarning(getShell(), "Warnung",
							"Warnung folgende Leistungen konnten im aktuellen Kontext (Fall, Konsultation, Gesetz) nicht verrechnet werden.\n"
									+ sb.toString());
				}
			}
			if (o instanceof Prescription) {
				Prescription presc = (Prescription) o;
				o = presc.getArtikel();
			}
			if (o instanceof IVerrechenbar) {
				if (AccessControlServiceHolder.get().evaluate(EvACEs.LSTG_VERRECHNEN) == false) {
					SWTHelper.alert(Messages.Core_Missing_rights, // $NON-NLS-1$
							Messages.VerrechnungsDisplay_missingRightsBody); // $NON-NLS-1$
				} else {
					Result<IVerrechenbar> result = Konsultation.load(actEncounter.getId())
							.addLeistung((IVerrechenbar) o);

					if (!result.isOK()) {
						SWTHelper.alert(Messages.VerrechnungsDisplay_imvalidBilling, result.toString()); // $NON-NLS-1$
					}
				}
			} else if (o instanceof IDiagnose) {
				Konsultation.load(actEncounter.getId()).addDiagnose((IDiagnose) o);
			}
			// refresh the modified billed object from the db
			actEncounter.getBilled().forEach(b -> CoreModelServiceHolder.get().refresh(b));
			setEncounter(actEncounter);
		}
	}

	private void updatePartDisposalColumn(List<IBilled> list) {
		boolean hasDisposal = false;
		for (IBilled billed : list) {
			if (isPartDisposal(billed)) {
				hasDisposal = true;
				break;
			}
		}
		if (hasDisposal) {
			partDisposalColumn.getColumn().setWidth(18);
		} else {
			partDisposalColumn.getColumn().setWidth(0);
		}
	}

	private boolean isPartDisposal(IBilled billed) {
		IBillable billable = billed.getBillable();
		if (billable instanceof IArticle) {
			IArticle a = (IArticle) billable;
			int abgabeEinheit = a.getSellingSize();
			if (abgabeEinheit > 0 && abgabeEinheit < a.getPackageSize()) {
				return true;
			}
		}
		return false;
	}

	private final class DropReceiver implements GenericObjectDropTarget.IReceiver {

		public boolean accept(PersistentObject o) {
			if (ElexisEventDispatcher.getSelectedPatient() != null) {
				if (o instanceof Artikel) {
					return !((Artikel) o).isProduct();
				}
				if (o instanceof IVerrechenbar) {
					return true;
				}
				if (o instanceof IDiagnose) {
					return true;
				}
				if (o instanceof Leistungsblock) {
					return true;
				}
				if (o instanceof Prescription) {
					Prescription p = ((Prescription) o);
					return (p.getArtikel() != null && !p.getArtikel().isProduct());
				}
			}
			return false;
		}

		private boolean accept(ch.elexis.core.model.ICodeElement codeElement) {
			if (codeElement instanceof IArticle) {
				return !((IArticle) codeElement).isProduct();
			} else if (codeElement instanceof IService) {
				return true;
			} else if (codeElement instanceof IDiagnosis) {
				return true;
			} else if (codeElement instanceof ICodeElementBlock) {
				return true;
			} else if (codeElement instanceof IBillable) {
				return true;
			}
			return false;
		}

		@Override
		public void dropped(List<Object> list, DropTargetEvent e) {
			if (actEncounter != null && accept(list)) {
				articleProcessor = new ArticleProcessor(actEncounter);
				for (Object object : list) {
					// map prescription to article
					if (object instanceof IPrescription) {
						object = ((IPrescription) object).getArticle();
					}
					if (object instanceof IArticle) {
						articleProcessor.processArticle((IArticle) object);
					} else if (object instanceof ICodeElementBlock) {
						articleProcessor.processCodeElementBlock((ICodeElementBlock) object);
					} else {
						articleProcessor.processOtherObject(object);
					}
				}
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, actEncounter);
			}
		}

		@Override
		public boolean accept(List<Object> list) {
			for (Object object : list) {
				if (object instanceof PersistentObject) {
					if (!accept((PersistentObject) object)) {
						return false;
					}
				} else if (object instanceof ch.elexis.core.model.ICodeElement) {
					return accept((ch.elexis.core.model.ICodeElement) object);
				}
			}
			return true;
		}
	}

	public void setEncounter(IEncounter encounter) {
		CoreUiUtil.removeCommandSelection("ch.elexis.VerrechnungsDisplay");
		actEncounter = encounter;
		if (actEncounter != null) {
			viewer.setInput(actEncounter.getBilled());
			updatePartDisposalColumn(actEncounter.getBilled());
			updateBilledLabel();
			updateUi();
		} else {
			viewer.setInput(Collections.emptyList());
			updateBilledLabel();
			updateUi();
		}
	}

	private void updateUi() {
		if (toolBarManager != null) {
			for (IContributionItem contribution : toolBarManager.getItems()) {
				contribution.update();
			}
			toolBarManager.update(true);
		}
		viewer.getTable().setEnabled(actEncounter != null && actEncounter.isBillable());
	}

	/**
	 * Filter codes of {@link Verrechnet} where ID is used as code. This is relevant
	 * for {@link Eigenleistung} and Eigenartikel.
	 *
	 * @param lst
	 * @return
	 */
	private String getServiceCode(IBilled billed) {
		String ret = billed.getCode();
		IBillable billable = billed.getBillable();
		if (billable != null) {
			if (billable instanceof ICustomService
					|| (billable instanceof IArticle && ((IArticle) billable).getTyp() == ArticleTyp.EIGENARTIKEL)) {
				if (billable.getId().equals(ret)) {
					ret = StringUtils.EMPTY;
				}
			}
		}
		return ret;
	}

	private Menu createVerrMenu() {
		contextMenuManager = new MenuManager();

		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				if (selection.size() > 1) {
					manager.add(removeAction);
				} else {
					if (!selection.isEmpty()) {
						IBilled billed = (IBilled) selection.getFirstElement();
						IBillable billable = billed.getBillable();

						manager.add(chPriceAction);
						manager.add(chCountAction);
						// TODO WTF
						// List<IAction> itemActions = (List<IAction>) (List<?>) vbar.getActions(v);
						// if ((itemActions != null) && (itemActions.size() > 0)) {
						// manager.add(new Separator());
						// for (IAction a : itemActions) {
						// if (a != null) {
						// manager.add(a);
						// }
						// }
						// }

						manager.add(new Separator());
						manager.add(chTextAction);
						manager.add(removeAction);
						manager.add(new Separator());
						manager.add(removeAllAction);
						if (billable instanceof IArticle) {
							manager.add(new Separator());
							manager.add(applyMedicationAction);
							// #8796
							manager.add(new Action(INDICATED_MEDICATION, Action.AS_CHECK_BOX) {
								@Override
								public void run() {
									IStructuredSelection selection = viewer.getStructuredSelection();
									for (Object selected : selection.toList()) {
										if (selected instanceof IBilled) {
											IBilled billed = (IBilled) selected;
											AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {

												@Override
												public void lockAcquired() {
													if (isIndicated(billed)) {
														billed.setExtInfo(Verrechnet.INDICATED, "false"); //$NON-NLS-1$
													} else {
														billed.setExtInfo(Verrechnet.INDICATED, "true"); //$NON-NLS-1$
													}
													CoreModelServiceHolder.get().save(billed);
												}
											});
										}
									}
								}

								private boolean isIndicated(IBilled billed) {
									String value = (String) billed.getExtInfo(Verrechnet.INDICATED);
									return "true".equalsIgnoreCase(value); //$NON-NLS-1$
								}

								@Override
								public boolean isChecked() {
									return isIndicated(billed);
								}
							});
						}
					}
					CoreCommandUiUtil.addCommandContributions(contextMenuManager, selection.toArray(),
							"popup:ch.elexis.VerrechnungsDisplay"); //$NON-NLS-1$
				}
			}
		});
		return contextMenuManager.createContextMenu(table);
	}

	private void makeActions() {
		// #3278
		applyMedicationAction = new Action(APPLY_MEDICATION) {
			@Override
			public void run() {
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							@Override
							public void lockAcquired() {
								billed.setExtInfo(Verrechnet.VATSCALE, Double.toString(0.0));

								int packageSize = ((IArticle) billed.getBillable()).getPackageSize();
								String proposal = (packageSize > 0) ? "1/" + packageSize : "1"; //$NON-NLS-1$ //$NON-NLS-2$
								changeQuantityDialog(proposal, billed);
								Object prescriptionId = billed.getExtInfo(Verrechnet.FLD_EXT_PRESC_ID);
								if (prescriptionId instanceof String) {
									Prescription prescription = Prescription.load((String) prescriptionId);
									if (prescription.getEntryType() == EntryType.SELF_DISPENSED) {
										prescription.setApplied(true);
									}
								}
							}
						});
					}
				}
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Images.IMG_SYRINGE.getImageDescriptor();
			}
		};

		removeAction = new Action(REMOVE) {
			@Override
			public void run() {
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				List<IPrescription> prescriptions = ArticleProcessor
						.getRecentPatientPrescriptions(actEncounter.getPatient());

				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						removeBilledItem(billed, prescriptions, true);
					}
				}
				setEncounter(actEncounter);
			}
		};

		removeAllAction = new Action(REMOVEALL) {
			@Override
			public void run() {
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				List<IBilled> allBilled = actEncounter.getBilled();
				List<IPrescription> prescriptions = ArticleProcessor
						.getRecentPatientPrescriptions(actEncounter.getPatient());

				for (IBilled billed : allBilled) {
					removeBilledItem(billed, prescriptions, false); // Keine Prüfung auf Dialog, direkt löschen
				}
				setEncounter(actEncounter);
			}
		};

		chPriceAction = new Action(CHPRICE) {

			@Override
			public void run() {
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {

							@Override
							public void lockAcquired() {
								Money oldPrice = billed.getPrice();
								String p = oldPrice.getAmountAsString();
								InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
										Messages.VerrechnungsDisplay_changePriceForService, // $NON-NLS-1$
										Messages.VerrechnungsDisplay_enterNewPrice, p, // $NON-NLS-1$
										null);
								if (dlg.open() == Dialog.OK) {
									try {
										String val = dlg.getValue().trim();
										Money newPrice = null;
										if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
											val = val.substring(0, val.length() - 1);
											double percent = Double.parseDouble(val);
											double factor = 1.0 + (percent / 100.0);
											newPrice = billed.getPrice().multiply(factor);
										} else {
											newPrice = new Money(val);
										}
										if (newPrice != null) {
											billed.setPrice(newPrice);
											CoreModelServiceHolder.get().save(billed);
											viewer.update(billed, null);
										}
									} catch (ParseException ex) {
										SWTHelper.showError(Messages.VerrechnungsDisplay_badAmountCaption, // $NON-NLS-1$
												Messages.VerrechnungsDisplay_badAmountBody); // $NON-NLS-1$
									}
								}
							}
						});
					}
				}
				updateBilledLabel();
			}
		};

		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run() {
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						String p = Double.toString(billed.getAmount());
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {

							@Override
							public void lockAcquired() {
								changeQuantityDialog(p, billed);
							}
						});
					}
				}
				updateBilledLabel();
			}
		};

		chTextAction = new Action(CHTEXT) {
			@Override
			public void run() {
				Result<IEncounter> editable = BillingServiceHolder.get().isEditable(actEncounter);
				if (!editable.isOK()) {
					ResultDialog.show(editable);
					return;
				}
				IStructuredSelection selection = viewer.getStructuredSelection();
				for (Object selected : selection.toList()) {
					if (selected instanceof IBilled) {
						IBilled billed = (IBilled) selected;
						AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
							@Override
							public void lockAcquired() {
								String oldText = billed.getText();
								InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
										Messages.VerrechnungsDisplay_changeTextCaption, // $NON-NLS-1$
										Messages.VerrechnungsDisplay_changeTextBody, // $NON-NLS-1$
										oldText, null);
								if (dlg.open() == Dialog.OK) {
									String input = dlg.getValue();
									if (input.matches("[0-9\\.,]+")) { //$NON-NLS-1$
										if (!SWTHelper.askYesNo(Messages.VerrechnungsDisplay_confirmChangeTextCaption, // $NON-NLS-1$
												Messages.VerrechnungsDisplay_confirmChangeTextBody)) { // $NON-NLS-1$
											return;
										}
									}
									billed.setText(input);
									CoreModelServiceHolder.get().save(billed);
									viewer.update(billed, null);
								}
							}
						});
					}
				}
			}
		};
	}

	private void changeQuantityDialog(String p, IBilled billed) {
		InputDialog dlg = new InputDialog(UiDesk.getTopShell(), Messages.VerrechnungsDisplay_changeNumberCaption, // $NON-NLS-1$
				Messages.VerrechnungsDisplay_changeNumberBody, // $NON-NLS-1$
				p, null);
		if (dlg.open() == Dialog.OK) {
			try {
				String val = dlg.getValue();
				if (!StringTool.isNothing(val)) {
					double changeAnzahl;
					IBillable billable = billed.getBillable();
					String text = billable.getText();
					if (val.indexOf(StringConstants.SLASH) > 0) {
						String[] frac = val.split(StringConstants.SLASH);
						changeAnzahl = Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
						text = billed.getText() + " (" + val //$NON-NLS-1$
								+ Messages.VerrechnungsDisplay_Orininalpackungen;
					} else if (val.indexOf('.') > 0) {
						changeAnzahl = Double.parseDouble(val);
					} else {
						changeAnzahl = Integer.parseInt(dlg.getValue());
					}

					IStatus status = BillingServiceHolder.get().changeAmountValidated(billed, changeAnzahl);
					if (!status.isOK()) {
						StatusDialog.show(status);
					}
					// refresh with changed amount
					CoreModelServiceHolder.get().refresh(billed);
					billed.setText(text);
					CoreModelServiceHolder.get().save(billed);
					viewer.update(billed, null);
				}
			} catch (NumberFormatException ne) {
				SWTHelper.showError(Messages.VerrechnungsDisplay_invalidEntryCaption, // $NON-NLS-1$
						Messages.VerrechnungsDisplay_invalidEntryBody); // $NON-NLS-1$
			}
		}
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}

	public MenuManager getMenuManager() {

		return contextMenuManager;
	}

	public StructuredViewer getViewer() {
		return viewer;
	}

	public void adaptMenus() {
		if (CoreUiUtil.isActiveControl(table)) {
			table.getMenu().setEnabled(AccessControlServiceHolder.get().evaluate(EvACEs.LSTG_VERRECHNEN));
		}
	}

	private void removeBilledItem(IBilled billed, List<IPrescription> prescriptions, boolean checkMedication) {
		boolean deleteMedications = false;
		if (checkMedication) {
			if (isMedicationLinked(billed, prescriptions)) {
				MessageDialog dialog = new MessageDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						Messages.VerrechnungsDisplay_deleteMedicationTitle, null,
						Messages.VerrechnungsDisplay_deleteMedicationMessage, MessageDialog.QUESTION,
						new String[] { Messages.VerrechnungsDisplay_deleteMedicationAll,
								Messages.VerrechnungsDisplay_deleteMedicationOnlyBilling,
								Messages.VerrechnungsDisplay_deleteMedicationCancel },
						0);
				int result = dialog.open();
				if (result == 2) {
					return;
				} else if (result == 0) {
					deleteMedications = true;
				}
			}
		} else {
			deleteMedications = true;
		}
		boolean lockDeleteMedication = deleteMedications;
		AcquireLockUi.aquireAndRun(billed, new LockDeniedNoActionLockHandler() {
			@Override
			public void lockAcquired() {
				if (lockDeleteMedication) {
					handleLinkedMedication(billed, prescriptions, CoreModelServiceHolder.get()::delete);
				}
				BillingServiceHolder.get().removeBilled(billed, actEncounter);
			}
		});
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IPrescription.class);
	}

	private void handleLinkedMedication(IBilled billed, List<IPrescription> prescriptions,
			Consumer<IPrescription> action) {
		prescriptions.stream().filter(prescription -> {
			Object extInfoObj = prescription
					.getExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID);
			return extInfoObj != null && billed.getId().equals(extInfoObj.toString());
		}).forEach(action);
	}

	private boolean isMedicationLinked(IBilled billed, List<IPrescription> prescriptions) {
		return prescriptions.stream().anyMatch(prescription -> {
			Object extInfoObj = prescription
					.getExtInfo(ch.elexis.core.model.prescription.Constants.FLD_EXT_VERRECHNET_ID);
			return extInfoObj != null && billed.getId().equals(extInfoObj.toString());
		});
	}


}
