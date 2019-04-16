/*******************************************************************************
 * Copyright (c) 2006-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - several contributions
 *******************************************************************************/

package ch.elexis.core.ui.views.codesystems;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.commands.CreateEigenleistungUi;
import ch.elexis.core.ui.commands.EditEigenleistungUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.StringTool;

public class BlockDetailDisplay implements IDetailDisplay {
	
	private ScrolledForm form;
	private FormToolkit tk;
	private Text tName;
	private Text tMacro;
	private Combo cbMandant;
	private TableViewer viewer;
	private Button bNew, bEigen, bDiag;
	private List<IMandator> lMandanten;
	private DataBindingContext dbc = new DataBindingContext();
	private WritableValue<ICodeElementBlock> master =
		new WritableValue(null, ICodeElementBlock.class);
	
	private BlockComparator comparator;
	
	private Action removeLeistung, moveUpAction, moveDownAction, editAction, countAction;
	private TableViewerFocusCellManager focusCellManager;
	
	private final IChangeListener changeListener = (event) -> {
		if (master.getValue() instanceof ICodeElementBlock) {
			CoreModelServiceHolder.get().save(master.getValue());
		}
		ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, master.getValue());
	};
	
	@Inject
	public void selection(
		@org.eclipse.e4.core.di.annotations.Optional ICodeElementBlock block){
		if (block != null && !form.isDisposed()) {
			display(block);
		}
	}
	
	public Composite createDisplay(final Composite parent, final IViewSite site){
		tk = UiDesk.getToolkit();
		form = tk.createScrolledForm(parent);
		form.setData("TEST_COMP_NAME", "blkd_form"); //$NON-NLS-1$
		
		Composite body = form.getBody();
		body.setData("TEST_COMP_NAME", "blkd_body"); //$NON-NLS-1$
		body.setBackground(parent.getBackground());
		body.setLayout(new GridLayout(2, false));
		
		tk.createLabel(body, Messages.BlockDetailDisplay_name)
			.setBackground(parent.getBackground());
		tName = tk.createText(body, StringConstants.EMPTY, SWT.BORDER);
		tName.setData("TEST_COMP_NAME", "blkd_Name_lst"); //$NON-NLS-1$
		tName.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		IObservableValue txtNameObservableUi =
			WidgetProperties.text(SWT.Modify).observeDelayed(100, tName);
		IObservableValue txtNameObservable =
			PojoProperties.value("code", String.class).observeDetail(master);
		txtNameObservable.addChangeListener(changeListener);
		dbc.bindValue(txtNameObservableUi, txtNameObservable);
		
		tk.createLabel(body, Messages.BlockDetailDisplay_macro)
			.setBackground(parent.getBackground());
		tMacro = tk.createText(body, StringConstants.EMPTY, SWT.BORDER);
		tMacro.setData("TEST_COMP_NAME", "blkd_Makro_lst"); //$NON-NLS-1$
		tMacro.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		IObservableValue txtMacroObservableUi =
			WidgetProperties.text(SWT.Modify).observeDelayed(100, tMacro);
		IObservableValue txtMacroObservable =
			PojoProperties.value("macro", String.class).observeDetail(master);
		txtMacroObservable.addChangeListener(changeListener);
		dbc.bindValue(txtMacroObservableUi, txtMacroObservable);
		
		tk.createLabel(body, StringConstants.MANDATOR).setBackground(parent.getBackground());
		cbMandant = new Combo(body, SWT.NONE);
		cbMandant.setData("TEST_COMP_NAME", "blkd_Mandant_cb"); //$NON-NLS-1$
		cbMandant.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		tk.adapt(cbMandant);
		IQuery<IMandator> query = CoreModelServiceHolder.get().getQuery(IMandator.class);
		query.and(ModelPackage.Literals.ICONTACT__MANDATOR, COMPARATOR.EQUALS, Boolean.TRUE);
		lMandanten = query.execute();
		cbMandant.add(Messages.BlockDetailDisplay_all);
		for (IMandator m : lMandanten) {
			cbMandant.add(m.getLabel());
		}
		cbMandant.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				int idx = cbMandant.getSelectionIndex();
				Optional<ICodeElementBlock> selected =
					ContextServiceHolder.get().getTyped(ICodeElementBlock.class);
				selected.ifPresent(block -> {
					if (idx > 0) {
						block.setMandator(lMandanten.get(idx - 1));
					} else {
						block.setMandator(null);
					}
				});
			}
			
		});
		Group gList = new Group(body, SWT.BORDER);
		gList.setText(Messages.BlockDetailDisplay_services); //$NON-NLS-1$
		gList.setLayoutData(SWTHelper.getFillGridData(2, true, 1, true));
		gList.setLayout(new FillLayout());
		tk.adapt(gList);
		viewer =
			new TableViewer(gList, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setData("TEST_COMP_NAME", "blkd_Leistung_Lst"); //$NON-NLS-1$
		tk.adapt(viewer.getControl(), true, true);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		comparator = new BlockComparator();
		createColumns();
		
		updateViewerInput(
			ContextServiceHolder.get().getTyped(ICodeElementBlock.class).orElse(null));
		
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {
			textTransfer
		};
		viewer.addDropSupport(DND.DROP_COPY, types, new DropTargetListener() {
			public void dragEnter(final DropTargetEvent event){
				if (event.data instanceof IArticle) {
					if (((IArticle) event.data).isProduct()) {
						event.detail = event.detail = DND.DROP_NONE;
						return;
					}
				}
				event.detail = DND.DROP_COPY;
			}
			
			public void dragLeave(final DropTargetEvent event){}
			
			public void dragOperationChanged(final DropTargetEvent event){}
			
			public void dragOver(final DropTargetEvent event){}
			
			public void drop(final DropTargetEvent event){
				Optional<ICodeElementBlock> block =
					ContextServiceHolder.get().getTyped(ICodeElementBlock.class);
				String drp = (String) event.data;
				String[] dl = drp.split(","); //$NON-NLS-1$
				for (String obj : dl) {
					Object dropped = StoreToStringServiceHolder.getLoadFromString(obj);
					if (dropped instanceof ICodeElement) {
						block.ifPresent(b -> {
							b.addElement((ICodeElement) dropped);
						});
					} else {
						LoggerFactory.getLogger(getClass())
							.warn("Dropped unknown store to string [" + dl + "]");
					}
				}
				block.ifPresent(b -> {
					CoreModelServiceHolder.get().save(b);
					updateViewerInput(b);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
						ICodeElementBlock.class);
				});
			}
			
			public void dropAccept(final DropTargetEvent event){
			
			}
			
		});
		// connect double click on column to actions
		focusCellManager =
			new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				ViewerCell focusCell = focusCellManager.getFocusCell();
				int columnIndex = focusCell.getColumnIndex();
				if (columnIndex == 0) {
					countAction.run();
				}
			}
		});
		
		bNew = tk.createButton(body, Messages.BlockDetailDisplay_addPredefinedServices, SWT.PUSH); //$NON-NLS-1$
		bNew.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bNew.setData("TEST_COMP_NAME", "blkd_addPredefinedServices_btn"); //$NON-NLS-1$
		bNew.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				try {
					site.getPage().showView(LeistungenView.ID);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							"Fehler beim Starten des Leistungscodes " + ex.getMessage(), ex,
							ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		
		bEigen =
			tk.createButton(body, Messages.BlockDetailDisplay_addSelfDefinedServices, SWT.PUSH); //$NON-NLS-1$
		bEigen.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bEigen.setData("TEST_COMP_NAME", "blkd_createPredefinedServices_btn"); //$NON-NLS-1$
		bEigen.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e){
				try {
					// execute the command
					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getService(IHandlerService.class);
					
					handlerService.executeCommand(CreateEigenleistungUi.COMMANDID, null);
				} catch (Exception ex) {
					throw new RuntimeException(CreateEigenleistungUi.COMMANDID, ex);
				}
				viewer.refresh();
			}
		});
		
		bDiag = tk.createButton(body, "Diagnose hinzufügen", SWT.PUSH); //$NON-NLS-1$
		bDiag.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		bDiag.setData("TEST_COMP_NAME", "btn_addDiagnosis_btn"); //$NON-NLS-1$
		bDiag.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				try {
					site.getPage().showView(DiagnosenView.ID);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							"Fehler beim Starten des Diagnosecodes " + ex.getMessage(), ex,
							ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		
		makeActions();
		ViewMenus menus = new ViewMenus(site);
		menus.createControlContextMenu(viewer.getControl(), new ViewMenus.IMenuPopulator() {
			public IAction[] fillMenu(){
				BlockElementViewerItem element =
					(BlockElementViewerItem) ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
				if (element != null) {
					if (element.isCodeElementInstanceOf(Eigenleistung.class)) {
						return new IAction[] {
							moveUpAction, moveDownAction, null, countAction, removeLeistung,
							editAction
						};
					} else {
						return new IAction[] {
							moveUpAction, moveDownAction, null, countAction, removeLeistung
						};
					}
				}
				return new IAction[0];
			}
		});
		// menus.createViewerContextMenu(lLst,moveUpAction,moveDownAction,null,removeLeistung,editAction);
		return body;
	}
	
	public Class<?> getElementClass(){
		return ICodeElementBlock.class;
	}
	
	public void display(final Object obj){
		ICodeElementBlock block = (ICodeElementBlock) obj;
		master.setValue(block);
		
		if (obj == null) {
			bNew.setEnabled(false);
			cbMandant.select(0);
		} else {
			IMandator mandator = block.getMandator();
			int sel = 0;
			if (mandator != null) {
				String[] items = cbMandant.getItems();
				sel = StringTool.getIndex(items, mandator.getLabel());
			}
			cbMandant.select(sel);
			bNew.setEnabled(true);
		}
		updateViewerInput(block);
	}
	
	private void updateViewerInput(ICodeElementBlock block){
		viewer.setInput(BlockElementViewerItem.of(block, true));
	}
	
	public String getTitle(){
		return Messages.BlockDetailDisplay_blocks; //$NON-NLS-1$
	}
	
	private void createColumns(){
		String[] titles = {
			"Anz.", "Code", "Bezeichnung"
		};
		int[] bounds = {
			45, 125, 600
		};
		
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BlockElementViewerItem) {
					BlockElementViewerItem item = (BlockElementViewerItem) element;
					return Integer.toString(item.getCount());
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[1], bounds[1], 1, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BlockElementViewerItem) {
					BlockElementViewerItem item = (BlockElementViewerItem) element;
					return item.getCode();
				}
				return "";
			}
		});
		
		col = createTableViewerColumn(titles[2], bounds[2], 2, SWT.NONE);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof BlockElementViewerItem) {
					BlockElementViewerItem item = (BlockElementViewerItem) element;
					return item.getText();
				}
				return "";
			}
			
			@Override
			public Color getBackground(final Object element){
				if (element instanceof BlockElementViewerItem) {
					BlockElementViewerItem item = (BlockElementViewerItem) element;
					String codeSystemName = item.getCodeSystemName();
					if (codeSystemName != null) {
						String rgbColor = CoreHub.globalCfg
							.get(Preferences.LEISTUNGSCODES_COLOR + codeSystemName, "ffffff");
						return UiDesk.getColorFromRGB(rgbColor);
					}
				}
				return null;
			}
		});
		TableColumn tableColumn = col.getColumn();
		col.getColumn().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if (viewer.getComparator() == null) {
					viewer.setComparator(comparator);
					comparator.setDirection(SWT.DOWN);
				} else {
					int direction = comparator.getDirection();
					if (direction == SWT.DOWN) {
						comparator.setDirection(SWT.UP);
					} else {
						comparator.setDirection(SWT.NONE);
						viewer.setComparator(null);
					}
				}
				viewer.getTable().setSortDirection(comparator.getDirection());
				viewer.getTable().setSortColumn(tableColumn);
				viewer.refresh();
			}
		});
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, int colNumber,
		int style){
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(false);
		return viewerColumn;
	}
	
	private void makeActions(){
		removeLeistung = new Action(Messages.BlockDetailDisplay_remove) { //$NON-NLS-1$
			@Override
			public void run(){
				Optional<ICodeElementBlock> block =
					ContextServiceHolder.get().getTyped(ICodeElementBlock.class);
				block.ifPresent(b -> {
					IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
					BlockElementViewerItem selectedElement =
						(BlockElementViewerItem) sel.getFirstElement();
					if (selectedElement != null) {
						selectedElement.remove();
						updateViewerInput(b);
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, b);
					}
				});
			}
		};
		moveUpAction = new Action(Messages.BlockDetailDisplay_moveUp) { //$NON-NLS-1$
			@Override
			public void run(){
				moveElement(true);
			}
		};
		moveDownAction = new Action(Messages.BlockDetailDisplay_moveDown) { //$NON-NLS-1$
			@Override
			public void run(){
				moveElement(false);
			}
		};
		editAction = new Action(Messages.BlockDetailDisplay_changeAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.BlockDetailDisplay_changeActionTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				BlockElementViewerItem selectedElement =
					(BlockElementViewerItem) sel.getFirstElement();
				PersistentObject poElement = null;
				ICodeElement firstElement = selectedElement.getFirstElement();
				if (firstElement instanceof Identifiable) {
					poElement = NoPoUtil.loadAsPersistentObject((Identifiable) firstElement);
				} else if (firstElement instanceof PersistentObject) {
					poElement = (PersistentObject) firstElement;
				}
				if (poElement != null) {
					EditEigenleistungUi.executeWithParams(poElement);
					
					ContextServiceHolder.get().getTyped(ICodeElementBlock.class).ifPresent(b -> {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, b);
					});
				}
			}
		};
		countAction = new Action("Anzahl") {
			@Override
			public void run(){
				IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
				BlockElementViewerItem selectedElement =
					(BlockElementViewerItem) sel.getFirstElement();
				InputDialog dlg = new InputDialog(UiDesk.getTopShell(), "Anzahl ändern", //$NON-NLS-1$
					"Ganzzahlige neue Anzahl", //$NON-NLS-1$
					Integer.toString(selectedElement.getCount()), new IInputValidator() {
						@Override
						public String isValid(String string){
							if (string != null && !string.isEmpty()) {
								try {
									Integer.parseInt(string);
								} catch (NumberFormatException e) {
									return "Kein ganzzahliger Wert";
								}
							}
							return null;
						}
					});
				if (dlg.open() == Dialog.OK) {
					try {
						String val = dlg.getValue();
						selectedElement.setCount(Integer.parseInt(val));
						Optional<ICodeElementBlock> block =
							ContextServiceHolder.get().getTyped(ICodeElementBlock.class);
						block.ifPresent(b -> {
							CoreModelServiceHolder.get().save(b);
							updateViewerInput(b);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, b);
						});
					} catch (NumberFormatException ne) {
						// ignore
					}
				}
			}
		};
	}
	
	private void moveElement(final boolean up){
		Optional<ICodeElementBlock> block =
			ContextServiceHolder.get().getTyped(ICodeElementBlock.class);
		block.ifPresent(b -> {
			IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
			BlockElementViewerItem selectedElement = (BlockElementViewerItem) sel.getFirstElement();
			if (selectedElement != null) {
				b.moveElement(selectedElement.getFirstElement(), up);
				CoreModelServiceHolder.get().save(b);
				updateViewerInput(b);
			}
		});
	}
	
	private class BlockComparator extends ViewerComparator {
		
		private int direction = 0;
		
		public void setDirection(int value){
			if (value == SWT.DOWN) {
				direction = 1;
			} else if (value == SWT.UP) {
				direction = -1;
			} else {
				direction = 0;
			}
		}
		
		public int getDirection(){
			if (direction == 1) {
				return SWT.DOWN;
			} else if (direction == -1) {
				return SWT.UP;
			}
			return SWT.NONE;
		}
		
		@Override
		public int compare(Viewer viewer, Object left, Object right){
			if (left instanceof BlockElementViewerItem && right instanceof BlockElementViewerItem) {
				if (direction != 0) {
					return ((BlockElementViewerItem) left).getText()
						.compareTo(((BlockElementViewerItem) right).getText()) * direction;
				}
				return ((BlockElementViewerItem) left).getText()
					.compareTo(((BlockElementViewerItem) right).getText());
			}
			return super.compare(viewer, left, right);
		}
	}
}
