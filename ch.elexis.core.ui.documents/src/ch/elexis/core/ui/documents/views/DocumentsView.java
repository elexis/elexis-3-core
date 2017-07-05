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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.documents.Messages;
import ch.elexis.core.ui.documents.handler.DocumentCrudHandler;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeTool;

/**
 * A class do receive documents by drag&drop. Documents are imported into the database and linked to
 * the selected patient. On double-click they are opened with their associated application.
 */

public class DocumentsView extends ViewPart implements IActivationListener {
	private static Logger logger = LoggerFactory.getLogger(DocumentsView.class);
	
	private TreeViewer viewer;
	private Tree table;
	
	public static String importAction_ID = "ch.elexis.omnivore.data.DocumentView.importAction";
	private final String[] colLabels =
		{
			"", Messages.DocumentView_categoryColumn, Messages.DocumentView_stateColumn,
			Messages.DocumentView_lastChangedColumn,
			Messages.DocumentView_dateCreatedColumn,
			Messages.DocumentView_titleColumn,
			Messages.DocumentsView_extensionColumn, Messages.DocumentView_keywordsColumn
		};
	private final String colWidth = "20,80,80,80,80,50,150,500";
	private final String sortSettings = "0,1,-1,false";
	private String searchTitle = "";
	
	private DocumentsViewerComparator ovComparator;
	private Action doubleClickAction;

	
	private final ElexisUiEventListenerImpl eeli_pat = new ElexisUiEventListenerImpl(Patient.class,
		ElexisEvent.EVENT_SELECTED) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			viewer.setInput(ev.getObject());
		}
		
	};
	
	private final ElexisUiEventListenerImpl eeli_doc_edit =
		new ElexisUiEventListenerImpl(IDocument.class, ElexisEvent.EVENT_UPDATE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				ViewContentProvider viewContentProvider =
					(ViewContentProvider) viewer.getContentProvider();
				viewContentProvider.updateElement((IDocument) ev.getGenericObject());
			}
			
		};
	
	private final ElexisUiEventListenerImpl eeli_doc_create =
		new ElexisUiEventListenerImpl(IDocument.class, ElexisEvent.EVENT_CREATE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				ViewContentProvider viewContentProvider =
					(ViewContentProvider) viewer.getContentProvider();
				viewContentProvider.createElement((IDocument) ev.getGenericObject());
			}
			
		};
	
	private final ElexisUiEventListenerImpl eeli_doc_delete =
		new ElexisUiEventListenerImpl(IDocument.class, ElexisEvent.EVENT_DELETE) {
			
		@Override
		public void runInUi(ElexisEvent ev){
				ViewContentProvider viewContentProvider =
					(ViewContentProvider) viewer.getContentProvider();
				viewContentProvider.removeElement((IDocument) ev.getGenericObject());
		}
			
	};
	
	private final ElexisUiEventListenerImpl eeli_doc_reload =
		new ElexisUiEventListenerImpl(IDocument.class, ElexisEvent.EVENT_RELOAD) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				viewer.refresh();
			}
			
		};


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
					if (iDocument.getKeywords().toLowerCase().contains(searchText.toLowerCase()))
					{
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
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput){
			if (newInput instanceof Patient) {
				documentsMap = DocumentStoreServiceHolder.getService()
					.getDocumentsByPatientId(((Patient) newInput).getId());
				
			} else {
				documentsMap.clear();
			}
		}
		
		public void updateElement(IDocument iDocument){
			if (iDocument != null) {
				ICategory cachedCategory = removeElementsByCachedCategory(iDocument);
				if (!iDocument.getCategory().equals(cachedCategory)) {
					loadElementsByCategory(iDocument, cachedCategory);
				}
				loadElementsByCategory(iDocument, iDocument.getCategory());
			}
		}
		
		public void createElement(IDocument iDocument){
			List<IDocument> iDocuments = documentsMap.get(iDocument.getCategory());
			if (iDocuments == null) {
				iDocuments = new ArrayList<>();
			}
			iDocuments.add(iDocument);
			documentsMap.put(iDocument.getCategory(), iDocuments);
			viewer.refresh(true);
		}
		
		private Entry<ICategory, IDocument> searchElementById(String id){
			for (Entry<ICategory, List<IDocument>> entry : documentsMap.entrySet()) {
				for (IDocument iDocument : entry.getValue()) {
					if (id.equals(iDocument.getId())) {
						return new AbstractMap.SimpleEntry<ICategory, IDocument>(entry.getKey(),
							iDocument);
					}
				}
			}
			return null;
		}
		
		private void loadElementsByCategory(IDocument iDocument, ICategory iCategory){
			List<IDocument> iDocuments = DocumentStoreServiceHolder.getService()
				.getDocumentsByCategory(iDocument.getPatientId(), iCategory);
			if (!iDocuments.isEmpty()) {
				documentsMap.put(iCategory, iDocuments);
				viewer.refresh(true);
			}
		}
		
		private void removeElement(IDocument iDocument){
			if (iDocument != null && iDocument.getId() != null && iDocument.getCategory() != null) {
				Entry<ICategory, IDocument> entry = searchElementById(iDocument.getId());
				if (entry != null) {
					List<IDocument> iDocuments = documentsMap.get(entry.getKey());
					if (iDocuments != null) {
						iDocuments.remove(iDocument);
						if (iDocuments.isEmpty()) {
							documentsMap.remove(entry.getKey());
						}
						viewer.remove(iDocument);
					}
				}
			}
		}
		
		private ICategory removeElementsByCachedCategory(IDocument iDocument){
			if (iDocument != null && iDocument.getId() != null && iDocument.getCategory() != null) {
				Entry<ICategory, IDocument> entry = searchElementById(iDocument.getId());
				if (entry != null) {
					documentsMap.remove(entry.getKey());
					return entry.getKey();
				}
			}
			return null;
		}
		
		public void dispose(){}
		
		
		public Object[] getElements(Object parent){
			List<ICategory> keys = new ArrayList<>(documentsMap.keySet());
			return keys.toArray();
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
				return dh.getCategory();
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
				if (index == 1) {
					return ((ICategory) obj).getName();
				}
				return null;
				
			}
			
			IDocument dh = (IDocument) obj;
			switch (index) {
			case 0:
				return "";
			case 1:
				return "";
			case 2:
				return dh.getStatus().getName();
			case 3:
				return new TimeTool(dh.getLastchanged()).toString(TimeTool.FULL_GER);
			case 4:
				return new TimeTool(dh.getCreated()).toString(TimeTool.DATE_GER);
			case 5:
				return dh.getTitle();
			case 6:
				return dh.getExtension();
			case 7:
				return dh.getKeywords();
			default:
				return "?";
			}
		}
		
		public Image getColumnImage(Object obj, int index){
			if (index == 5) {
				if (obj instanceof IDocument
					&& LocalDocumentServiceHolder.getService().isPresent()) {
					Optional<IPersistentObject> opt = DocumentStoreServiceHolder.getService()
						.getPersistenceObject((IDocument) obj);
					if (opt.isPresent()
						&& LocalDocumentServiceHolder.getService().get().contains(opt.get())) {
						return Images.IMG_EDIT.getImage();
					}
				}
			}
			else if (index == 1 && obj instanceof ICategory) {
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
		
		final Text tSearch = new Text(parent, SWT.BORDER);
		tSearch.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
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
		viewer.setContentProvider(new ViewContentProvider());
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
					
					viewer.refresh();
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
				}
				else {
					SWTHelper.showError(Messages.DocumentView_exportErrorCaption,
						Messages.DocumentView_exportErrorEmptyText);
				}
				
			}
		});
		

		GlobalEventDispatcher.addActivationListener(this, this);

		MenuManager menuManager = new MenuManager();
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuManager,
			viewer);
		getSite().setSelectionProvider(viewer);
		
		viewer.setInput(ElexisEventDispatcher.getSelectedPatient());
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
		GlobalEventDispatcher.removeActivationListener(this, this);
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
	
	@Override
	public void visible(boolean mode){
		if (mode) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_doc_delete,
				eeli_doc_edit, eeli_doc_create, eeli_doc_reload);
			refresh();
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_doc_delete,
				eeli_doc_edit, eeli_doc_create, eeli_doc_reload);
		}
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
							PlatformUI.getWorkbench().getService(IEclipseContext.class)
								.set(command.getId().concat(".selection"),
									new StructuredSelection(po));
							try {
								command.executeWithChecks(
									new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
							} catch (ExecutionException | NotDefinedException | NotEnabledException
									| NotHandledException e) {
								e.printStackTrace();
							}
						});
					viewer.refresh();
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