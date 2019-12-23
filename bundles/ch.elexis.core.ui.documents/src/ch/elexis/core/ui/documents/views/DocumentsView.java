/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.documents.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.documents.Messages;
import ch.elexis.core.ui.documents.handler.DocumentCrudHandler;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.AUF;
import ch.rgw.tools.TimeTool;

/**
 * A class do receive documents by drag&drop. Documents are imported into the database and linked to
 * the selected patient. On double-click they are opened with their associated application.
 */

public class DocumentsView extends ViewPart {
	private static Logger logger = LoggerFactory.getLogger(DocumentsView.class);
	
	private TreeViewer viewer;
	private Tree table;
	
	public static String importAction_ID = "ch.elexis.omnivore.data.DocumentView.importAction";
	
	private final String[] colLabels = {
		"", "", Messages.DocumentView_categoryColumn, Messages.DocumentView_lastChangedColumn,
		Messages.DocumentView_titleColumn, Messages.DocumentView_keywordsColumn
	};
	private final String colWidth = "20,20,100,100,200,500";
	private final String sortSettings = "0,1,-1,false";
	private String searchTitle = "";
	
	private DocumentsViewerComparator ovComparator;
	private Action doubleClickAction;
	
	@Inject
	void activePatient(@Optional IPatient patient){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			Display.getDefault().asyncExec(() -> {
				viewer.setInput(patient);
				viewer.expandAll();
			});
		}
	}
	
	@Optional
	@Inject
	void udpateDocument(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IDocument document){
		if (document != null && viewer != null && !viewer.getControl().isDisposed()) {
			ViewContentProvider viewContentProvider =
				(ViewContentProvider) viewer.getContentProvider();
			viewContentProvider.updateElement(document);
			viewer.refresh();
		}
	}
	
	@Optional
	@Inject
	void deleteDocument(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) IDocument document){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			ViewContentProvider viewContentProvider =
				(ViewContentProvider) viewer.getContentProvider();
			viewContentProvider.updateElement(document);
			viewer.setSelection(new StructuredSelection());
			viewer.refresh();
		}
	}
	
	@Optional
	@Inject
	void createDocument(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) IDocument document){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			ViewContentProvider viewContentProvider =
				(ViewContentProvider) viewer.getContentProvider();
			viewContentProvider.updateElement(document);
			viewer.refresh();
		}
	}
	
	@Optional
	@Inject
	void reloadDocument(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) IDocument document){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.refresh();
		}
	}
	
	@SuppressWarnings("restriction")
	@Inject
	ECommandService commandService;
	@SuppressWarnings("restriction")
	@Inject
	EHandlerService handlerService;
	
	class ViewFilterProvider extends ViewerFilter {
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element){
			
			if (searchTitle != null && !searchTitle.isEmpty()) {
				String searchText = searchTitle.toLowerCase();
				
				if (element instanceof ICategory) {
					StructuredViewer sviewer = (StructuredViewer) viewer;
					ITreeContentProvider provider =
						(ITreeContentProvider) sviewer.getContentProvider();
					for (Object child : provider.getChildren(element)) {
						if (select(viewer, element, child))
							return true;
					}
					return false;
				}
				if (element instanceof IDocument) {
					IDocument iDocument = (IDocument) element;
					if (iDocument.getCategory().getName().toLowerCase().startsWith(searchText)) {
						return true;
					}
					if (iDocument.getTitle().toLowerCase().contains(searchText)) {
						return true;
					}
					if (iDocument.getKeywords() != null && iDocument.getKeywords().toLowerCase()
						.contains(searchText.toLowerCase())) {
						return true;
					}
				}
				return false;
			}
			return true;
		}
		
	}
	
	class ViewContentProvider implements ITreeContentProvider {
		
		private Map<ICategory, List<IDocument>> documentsMap = new HashMap<>();
		private FilterCategory selectedFilter = null;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput){
			documentsMap.clear();
			if (newInput instanceof IPatient) {
				loadByFilterCategory((IPatient) newInput);
			}
		}
		
		public ViewContentProvider selectFilterCategory(ISelection selection){
			StructuredSelection sel = (StructuredSelection) selection;
			if (selection != null) {
				Object element = sel.getFirstElement();
				if (element instanceof FilterCategory) {
					selectedFilter = (FilterCategory) element;
					viewer.refresh(true);
					viewer.expandAll();
				}
			}
			return this;
		}
		
		private void loadByFilterCategory(IPatient newInput){
			if (newInput != null) {
				if (selectedFilter.isAll()) {
					documentsMap = DocumentStoreServiceHolder.getService()
						.getDocumentsByPatientId(newInput.getId());
					viewer.refresh(true);
				} else {
					loadElementsByCategory(newInput.getId(), selectedFilter);
				}
			}
		}
		
		public void updateElement(IDocument iDocument){
			if (iDocument != null) {
				removeFromCategories(iDocument);
				FilterCategory filterCategory = new FilterCategory(iDocument.getCategory());
				List<IDocument> categoryDocuments = documentsMap.get(filterCategory);
				if (categoryDocuments != null) {
					if (!categoryDocuments.contains(iDocument) && !iDocument.isDeleted()) {
						categoryDocuments.add(iDocument);
					} else if (categoryDocuments.contains(iDocument) && iDocument.isDeleted()) {
						categoryDocuments.remove(iDocument);
					}
				} else if (!iDocument.isDeleted()){
					categoryDocuments = new ArrayList<>();
					categoryDocuments.add(iDocument);
					documentsMap.put(filterCategory, categoryDocuments);
				}
			}
		}
		
		private void removeFromCategories(IDocument iDocument){
			Set<ICategory> categories = new HashSet<>(documentsMap.keySet());
			for (ICategory category : categories) {
				List<IDocument> categoryDocuments = documentsMap.get(category);
				categoryDocuments.remove(iDocument);
				if (categoryDocuments.isEmpty()) {
					documentsMap.remove(category);
				}
			}
		}
		
		public void createElement(IDocument iDocument){
			FilterCategory filterCategory = new FilterCategory(iDocument.getCategory());
			List<IDocument> iDocuments = documentsMap.get(filterCategory);
			if (iDocuments == null) {
				iDocuments = new ArrayList<>();
			}
			iDocuments.add(iDocument);
			documentsMap.put(filterCategory, iDocuments);
			viewer.refresh(true);
		}
		
		private void loadElementsByCategory(String patientId, ICategory iCategory){
			if (!(iCategory instanceof FilterCategory) || documentsMap.get(iCategory) == null) {
				List<IDocument> iDocuments = DocumentStoreServiceHolder.getService()
					.getDocumentsByCategory(patientId, iCategory);
				if (!iDocuments.isEmpty()) {
					documentsMap.put(new FilterCategory(iDocuments.get(0).getCategory()),
						iDocuments);
				}
			}
			viewer.refresh(true);
		}
		
		public void dispose(){}
		
		public Object[] getElements(Object parent){
			if (selectedFilter.isAll()) {
				List<ICategory> keys = new ArrayList<>(documentsMap.keySet());
				return keys.toArray();
			} else if (documentsMap.containsKey(selectedFilter)) {
				return new Object[] {
					selectedFilter
				};
			}
			return new Object[0];
		}
		
		public Object[] getChildren(Object parentElement){
			if (parentElement instanceof ICategory && documentsMap.containsKey(parentElement)) {
				return documentsMap.get(parentElement).toArray();
			} else {
				return new Object[0];
			}
		}
		
		public Object getParent(Object element){
			if (element instanceof IDocument) {
				IDocument dh = (IDocument) element;
				return new FilterCategory(dh.getCategory());
			}
			return null;
		}
		
		public boolean hasChildren(Object element){
			return documentsMap.containsKey(element);
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index){
			if (obj instanceof ICategory) {
				if (index == 2) {
					return ((ICategory) obj).getName();
				}
				return null;
				
			}
			
			IDocument dh = (IDocument) obj;
			switch (index) {
			case 0:
				return "";
			case 1:
				return dh.getStatus().getName().substring(0, 1);
			case 2:
				return "";
			case 3:
				return new TimeTool(dh.getLastchanged()).toString(TimeTool.FULL_GER);
			case 4:
				return dh.getTitle();
			case 5:
				return dh.getKeywords();
			default:
				return "?";
			}
		}
		
		public Image getColumnImage(Object obj, int index){
			if (index == 4) {
				if (obj instanceof IDocument
					&& LocalDocumentServiceHolder.getService().isPresent()) {
					java.util.Optional<Identifiable> opt = DocumentStoreServiceHolder.getService()
						.getPersistenceObject((IDocument) obj);
					if (opt.isPresent()
						&& LocalDocumentServiceHolder.getService().get().contains(opt.get())) {
						return Images.IMG_EDIT.getImage();
					}
				}
			} else if (index == 2 && obj instanceof ICategory) {
				return Images.IMG_FOLDER.getImage();
			}
			return null;
		}
		
		public Image getImage(Object obj){
			return null;
		}
	}
	
	/**
	 * The constructor.
	 */
	public DocumentsView(){
		
	}
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout(4, false));
		
		Composite filterComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		filterComposite.setLayoutData(data);
		filterComposite.setLayout(new GridLayout(2, false));
		
		final Text tSearch = new Text(filterComposite, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		tSearch.setLayoutData(data);
		tSearch.setMessage(Messages.DocumentView_searchLabel);
		// Add search listener
		ModifyListener searchListener = new ModifyListener() {
			public void modifyText(ModifyEvent e){
				searchTitle = tSearch.getText();
				refresh();
				if (searchTitle == null || searchTitle.isEmpty()) {
					viewer.collapseAll();
				} else {
					viewer.expandAll();
				}
			}
		};
		tSearch.addModifyListener(searchListener);
		
		createFlatMenu(filterComposite);
		// Table to display documents
		table = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		TreeColumn[] cols = new TreeColumn[colLabels.length];
		for (int i = 0; i < colLabels.length; i++) {
			cols[i] = new TreeColumn(table, SWT.NONE);
			cols[i].setText(colLabels[i]);
			cols[i].setData(new Integer(i));
		}
		applyUsersColumnWidthSetting();
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		
		viewer = new TreeViewer(table);
		
		DocumentsFilterBarComposite filterBarComposite = addFilterBar(parent);
		
		viewer.setContentProvider(
			new ViewContentProvider().selectFilterCategory(filterBarComposite.getSelection()));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setUseHashlookup(true);
		viewer.addFilter(new ViewFilterProvider());
		
		ovComparator = new DocumentsViewerComparator();
		viewer.setComparator(ovComparator);
		TreeColumn[] treeCols = viewer.getTree().getColumns();
		for (int i = 0; i < treeCols.length; i++) {
			TreeColumn tc = treeCols[i];
			tc.addSelectionListener(getSelectionAdapter(tc, i));
		}
		makeActions();
		applySortDirection();
		hookDoubleClickAction();
		
		final Transfer[] dropTransferTypes = new Transfer[] {
			FileTransfer.getInstance()
		};
		
		viewer.addDropSupport(DND.DROP_COPY, dropTransferTypes, new DropTargetAdapter() {
			
			@Override
			public void dragEnter(DropTargetEvent event){
				event.detail = DND.DROP_COPY;
			}
			
			@Override
			public void drop(DropTargetEvent event){
				if (dropTransferTypes[0].isSupportedType(event.currentDataType)) {
					String[] files = (String[]) event.data;
					ICategory category = null;
					if (event.item != null) {
						if (event.item.getData() instanceof IDocument) {
							IDocument dh = (IDocument) event.item.getData();
							category = dh.getCategory();
						} else if (event.item.getData() instanceof ICategory) {
							category = (ICategory) event.item.getData();
						}
						
					}
					
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
					Command cmd = commandService.getCommand(DocumentCrudHandler.CMD_NEW_DOCUMENT);
					if (files != null) {
						for (String file : files) {
							try {
								Map<String, String> params = new HashMap<>();
								params.put(DocumentCrudHandler.PARAM_FILE_PATH, file);
								if (category != null) {
									params.put(DocumentCrudHandler.PARAM_DOC_CATEGORY,
										category.getName());
								}
								ExecutionEvent ev = new ExecutionEvent(cmd, params, null, null);
								cmd.executeWithChecks(ev);
							} catch (Exception e) {
								logger.error("drop error", e);
							}
						}
					}
				}
			}
			
		});
		
		final Transfer[] dragTransferTypes = new Transfer[] {
			FileTransfer.getInstance(), TextTransfer.getInstance()
		};
		viewer.addDragSupport(DND.DROP_COPY, dragTransferTypes, new DragSourceAdapter() {
			private boolean failure;
			
			@Override
			public void dragStart(DragSourceEvent event){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				event.doit = selection.getFirstElement() instanceof IDocument;
			}
			
			@Override
			public void dragSetData(DragSourceEvent event){
				failure = false;
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				IDocument dh = (IDocument) selection.getFirstElement();
				if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
					String title = dh.getTitle();
					int end = dh.getTitle().lastIndexOf(".");
					if (end != -1) {
						title = (dh.getTitle()).substring(0, end);
					}
					
					try {
						String absPath = DocumentStoreServiceHolder.getService()
							.saveContentToTempFile(dh, title, dh.getExtension(), true);
						if (absPath != null) {
							event.data = new String[] {
								absPath
							};
						} else {
							event.doit = false;
							failure = true;
						}
						
					} catch (ElexisException e) {
						event.doit = false;
						failure = true;
						logger.error("drag error", e);
					}
				}
			}
			
			@Override
			public void dragFinished(DragSourceEvent event){
				if (!failure) {
					super.dragFinished(event);
				} else {
					SWTHelper.showError(Messages.DocumentView_exportErrorCaption,
						Messages.DocumentView_exportErrorEmptyText);
				}
				
			}
		});
		
		MenuManager menuManager = new MenuManager();
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuManager, viewer);
		getSite().setSelectionProvider(viewer);
		
		viewer.setInput(ContextServiceHolder.get().getActivePatient().orElse(null));
	}

	private void createFlatMenu(Composite filterComposite){
		ToolBarManager tMenuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tMenuManager.add(new Action("Neuer Brief", Action.AS_PUSH_BUTTON) {
			@SuppressWarnings("restriction")
			@Override
			public void run(){
				ParameterizedCommand cmd =
					commandService.createCommand("ch.elexis.core.ui.commands.BriefNew", null);
				if (cmd != null) {
					handlerService.executeHandler(cmd);
				}
				super.run();
			}
		});
		tMenuManager.add(new Action("AUF", Action.AS_PUSH_BUTTON) {
			@SuppressWarnings("restriction")
			@Override
			public void run(){
				ParameterizedCommand cmd =
					commandService.createCommand("ch.elexis.core.ui.commands.AufNew", null);
				if (cmd != null) {
					Object createdAuf = handlerService.executeHandler(cmd);
					if (createdAuf instanceof AUF) {
						// print
						cmd = commandService.createCommand("ch.elexis.core.ui.commands.AufPrint",
							null);
						if (cmd != null) {
							handlerService.executeHandler(cmd);
						}
					}
				}
				super.run();
			}
		});
		tMenuManager.add(new Action("Import", Action.AS_PUSH_BUTTON) {
			@SuppressWarnings("restriction")
			@Override
			public void run(){
				ParameterizedCommand cmd =
					commandService.createCommand(DocumentCrudHandler.CMD_NEW_DOCUMENT, null);
				if (cmd != null) {
					handlerService.executeHandler(cmd);
				}
				super.run();
			}
		});
		tMenuManager.createControl(filterComposite);
	}
	
	private DocumentsFilterBarComposite addFilterBar(Composite parent){
		
		List<FilterCategory> filters = new ArrayList<>();
		filters.add(new FilterCategory(null, "Alle"));
		filters.add(new FilterCategory(BriefConstants.UNKNOWN, "Briefe"));
		filters.add(new FilterCategory(BriefConstants.AUZ, "Auf"));
		filters.add(new FilterCategory(BriefConstants.RP, "Rezepte"));
		
		DocumentsFilterBarComposite filterBarComposite =
			new DocumentsFilterBarComposite(parent, SWT.NONE, filters);
		filterBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterBarComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				ViewContentProvider cp = (ViewContentProvider) viewer.getContentProvider();
				if (cp != null) {
					cp.selectFilterCategory(event.getSelection());
				}
			}
		});
		return filterBarComposite;
	}
	
	private SelectionListener getSelectionAdapter(final TreeColumn column, final int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ovComparator.setColumn(index);
				viewer.getTree().setSortDirection(ovComparator.getDirection());
				viewer.getTree().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}
	
	private void applySortDirection(){
		String[] usrSortSettings = sortSettings.split(",");
		
		/*	if (CoreHub.userCfg.get(PreferencePage.SAVE_SORT_DIRECTION, false)) {
				String sortSet =
					CoreHub.userCfg.get(PreferencePage.USR_SORT_DIRECTION_SETTINGS, sortSettings);
				usrSortSettings = sortSet.split(",");
			}*/
		
		int propertyIdx = Integer.parseInt(usrSortSettings[0]);
		int direction = Integer.parseInt(usrSortSettings[1]);
		if (propertyIdx != 0) {
			sortViewer(propertyIdx, direction);
		}
		
	}
	
	private void sortViewer(int propertyIdx, int direction){
		TreeColumn column = viewer.getTree().getColumn(propertyIdx);
		ovComparator.setColumn(propertyIdx);
		ovComparator.setDirection(direction);
		viewer.getTree().setSortDirection(ovComparator.getDirection());
		viewer.getTree().setSortColumn(column);
		viewer.refresh();
	}
	
	private void applyUsersColumnWidthSetting(){
		TreeColumn[] treeColumns = table.getColumns();
		String[] userColWidth = colWidth.split(",");
		/*if (CoreHub.userCfg.get(PreferencePage.SAVE_COLUM_WIDTH, false)) {
			String ucw = CoreHub.userCfg.get(PreferencePage.USR_COLUMN_WIDTH_SETTINGS, colWidth);
			userColWidth = ucw.split(",");
		}*/
		
		for (int i = 0; i < treeColumns.length; i++) {
			treeColumns[i].setWidth(Integer.parseInt(userColWidth[i]));
		}
	}
	
	@Override
	public void dispose(){
		//saveSortSettings();
		super.dispose();
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus(){
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}
	
	public void activation(boolean mode){
		if (mode == false) {
			TreeColumn[] treeColumns = viewer.getTree().getColumns();
			StringBuilder sb = new StringBuilder();
			for (TreeColumn tc : treeColumns) {
				sb.append(tc.getWidth());
				sb.append(",");
			}
			//	CoreHub.userCfg.set(PreferencePage.USR_COLUMN_WIDTH_SETTINGS, sb.toString());
			
			//	saveSortSettings();
		}
	}
	
	/*private void saveSortSettings(){
		int propertyIdx = ovComparator.getPropertyIndex();
		int direction = ovComparator.getDirectionDigit();
		int catDirection = ovComparator.getCategoryDirection();
		CoreHub.userCfg.set(PreferencePage.USR_SORT_DIRECTION_SETTINGS, propertyIdx + "," + direction
				+ "," + catDirection + "," + bFlat);
	}*/
	
	public void refresh(){
		viewer.refresh();
	}
	
	private void makeActions(){
		doubleClickAction = new Action() {
			public void run(){
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IDocument) {
					IDocument dh = (IDocument) obj;
					DocumentStoreServiceHolder.getService().getPersistenceObject(dh)
						.ifPresent(po -> {
							ICommandService commandService = (ICommandService) PlatformUI
								.getWorkbench().getService(ICommandService.class);
							Command command = commandService
								.getCommand("ch.elexis.core.ui.command.startEditLocalDocument");
							PlatformUI.getWorkbench().getService(IEclipseContext.class).set(
								command.getId().concat(".selection"), new StructuredSelection(po));
							try {
								command.executeWithChecks(
									new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
							} catch (ExecutionException | NotDefinedException | NotEnabledException
									| NotHandledException e) {
								MessageDialog.openError(getSite().getShell(), "Fehler",
									"Das Dokument konnte nicht geöffnet werden.");
								e.printStackTrace();
							}
						});
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, dh);
				}
			}
		};
	}
	
	private void hookDoubleClickAction(){
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event){
				doubleClickAction.run();
			}
		});
	}
}