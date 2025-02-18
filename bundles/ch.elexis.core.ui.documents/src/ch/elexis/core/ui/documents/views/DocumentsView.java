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

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
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
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.documents.FilterCategory;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.BriefConstants;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.types.DocumentStatus;
import ch.elexis.core.ui.documents.handler.DocumentCrudHandler;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import ch.rgw.tools.TimeTool;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * A class do receive documents by drag&drop. Documents are imported into the
 * database and linked to the selected patient. On double-click they are opened
 * with their associated application.
 */

public class DocumentsView extends ViewPart implements IRefreshable {

	public static final String ID = "ch.elexis.core.ui.documents.views.DocumentsView"; //$NON-NLS-1$
	public static final String SETTING_FLAT_VIEW = "documentsView/flatView"; //$NON-NLS-1$
	public static final String SETTING_COLUMN_WIDTH = "documentsView/columnwidths"; //$NON-NLS-1$

	private static Logger logger = LoggerFactory.getLogger(DocumentsView.class);

	private TreeViewer viewer;

	private IStructuredSelection currentDragSelection;

	private final String[] colLabels = { StringUtils.EMPTY, StringUtils.EMPTY, Messages.Core_Category,
			Messages.Core_Date, Messages.DocumentView_dateCreatedColumn, Messages.Core_Title, 
			Messages.Core_Keywords };
	private final String colWidth = "20,20,150,100,100,250,500"; //$NON-NLS-1$
	private final String sortSettings = "0,1,-1,false"; //$NON-NLS-1$
	private String searchTitle = StringUtils.EMPTY;

	private DocumentsViewerComparator ovComparator;
	private Action doubleClickAction;
	private boolean bFlat = false;

	private IPatient actPatient;

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this) {
		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
			if (isMatchingPart(partRef)) {
				saveColumnWidthSettings();
			}
		}

		private void saveColumnWidthSettings() {
			TreeColumn[] treeColumns = viewer.getTree().getColumns();
			StringBuilder sb = new StringBuilder();
			for (TreeColumn tc : treeColumns) {
				sb.append(tc.getWidth());
				sb.append(","); //$NON-NLS-1$
			}
			ConfigServiceHolder.setUser(SETTING_COLUMN_WIDTH, sb.toString());
		}
	};


	@Inject
	void activePatient(@Optional IPatient patient) {
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(viewer.getControl())) {
				if (actPatient != patient) {
					viewer.setInput(patient);
					viewer.expandAll();
					actPatient = patient;
				}
			}
		});
	}

	@Optional
	@Inject
	void udpateDocument(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IDocument document) {
		if (document != null && viewer != null && !viewer.getControl().isDisposed()) {
			// reload to refresh entity from database
			document = DocumentStoreServiceHolder.getService().loadDocument(document.getId(), document.getStoreId())
					.get();
			contentProvider.updateElement(document);
			// the selection of TreeItem is disposed after updating a document with a dialog
			viewer.getTree().deselectAll();
			viewer.refresh();
		}
	}

	@Optional
	@Inject
	void deleteDocument(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) IDocument document) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			contentProvider.updateElement(document);
			viewer.setSelection(new StructuredSelection());
			viewer.refresh();
		}
	}

	@Optional
	@Inject
	void createDocument(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) IDocument document) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			contentProvider.updateElement(document);
			viewer.refresh();
		}
	}

	@Optional
	@Inject
	void reloadDocument(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) IDocument document) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.refresh();
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	private DocumentsTreeContentProvider contentProvider;

	class ViewFilterProvider extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {

			if (searchTitle != null && !searchTitle.isEmpty()) {
				String searchText = searchTitle.toLowerCase();

				if (element instanceof ICategory) {
					StructuredViewer sviewer = (StructuredViewer) viewer;
					ITreeContentProvider provider = (ITreeContentProvider) sviewer.getContentProvider();
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
					if (iDocument.getKeywords() != null
							&& iDocument.getKeywords().toLowerCase().contains(searchText.toLowerCase())) {
						return true;
					}
				}
				return false;
			}
			return true;
		}

	}

	/**
	 * The constructor.
	 */
	public DocumentsView() {

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
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
			@Override
			public void modifyText(ModifyEvent e) {
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
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		List<TreeViewerColumn> viewerColumns = new ArrayList<>();
		for (int i = 0; i < colLabels.length; i++) {
			final TreeViewerColumn viewerColumn = new TreeViewerColumn(viewer, SWT.NONE);
			viewerColumns.add(viewerColumn);
			final TreeColumn column = viewerColumn.getColumn();
			column.setText(colLabels[i]);
			column.setData(Integer.valueOf(i));
			column.setResizable(true);
			column.setMoveable(false);
		}
		viewerColumns.get(0).setLabelProvider(new ColumnLabelProvider());
		viewerColumns.get(1).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					if (!doc.getStatus().isEmpty()) {
						return doc.getStatus().stream().map(s -> s.getName())
								.reduce((u, t) -> u + StringConstants.COMMA + t).get();
					}
				}
				return super.getToolTipText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					java.util.Optional<DocumentStatus> sent = doc.getStatus().stream()
							.filter(s -> s == DocumentStatus.SENT).findFirst();
					if (sent.isPresent()) {
						return Images.IMG_OUTBOX.getImage();
					}
					return Images.IMG_INBOX.getImage();
				}
				return super.getImage(element);
			}
		});
		viewerColumns.get(2).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return bFlat && doc.getCategory() != null ? doc.getCategory().getName() : StringUtils.EMPTY;
				} else if (element instanceof ICategory) {
					ICategory cat = (ICategory) element;
					return cat.getName();
				}
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof ICategory) {
					return Images.IMG_FOLDER.getImage();
				}
				return super.getImage(element);
			};
		});
		viewerColumns.get(3).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getLastchanged()).toString(TimeTool.DATE_GER);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getLastchanged()).toString(TimeTool.LARGE_GER);
				}
				return super.getToolTipText(element);
			}
		});
		viewerColumns.get(4).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getCreated()).toString(TimeTool.DATE_GER);
				}
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return new TimeTool(doc.getCreated()).toString(TimeTool.LARGE_GER);
				}
				return super.getToolTipText(element);
			}
		});
		viewerColumns.get(5).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return doc.getTitle();
				}
				return StringUtils.EMPTY;
			};

			@Override
			public Image getImage(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					java.util.Optional<Identifiable> opt = DocumentStoreServiceHolder.getService()
							.getPersistenceObject(doc);
					if (opt.isPresent() && LocalDocumentServiceHolder.getService().get().contains(opt.get())) {
						return Images.IMG_EDIT.getImage();
					}
				}
				return super.getImage(element);
			};
		});
		viewerColumns.get(6).setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IDocument) {
					IDocument doc = (IDocument) element;
					return java.util.Optional.ofNullable(doc.getKeywords()).orElse(StringUtils.EMPTY);
				}
				return StringUtils.EMPTY;
			}
		});

		applyUsersColumnWidthSetting();

		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		DocumentsFilterBarComposite filterBarComposite = addFilterBar(parent);

		contentProvider = new DocumentsTreeContentProvider(viewer)
				.selectFilterCategory(filterBarComposite.getSelection());
		viewer.setContentProvider(contentProvider);
		viewer.setUseHashlookup(true);
		viewer.addFilter(new ViewFilterProvider());

		bFlat = ConfigServiceHolder.getUser(SETTING_FLAT_VIEW, false);
		contentProvider.setFlat(bFlat);

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

		final Transfer[] dropTransferTypes = new Transfer[] { FileTransfer.getInstance() };

		viewer.addDropSupport(DND.DROP_COPY, dropTransferTypes, new DropTargetAdapter() {

			@Override
			public void dragEnter(DropTargetEvent event) {
				event.detail = DND.DROP_COPY;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (dropTransferTypes[0].isSupportedType(event.currentDataType)) {
					// if flat drop from same view makes no sense
					if (bFlat && currentDragSelection != null) {
						return;
					}

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

					ICommandService commandService = PlatformUI.getWorkbench()
							.getService(ICommandService.class);
					Command cmd = commandService.getCommand(DocumentCrudHandler.CMD_NEW_DOCUMENT);
					if (files != null) {
						for (String file : files) {
							Object created = null;
							try {
								Map<String, String> params = new HashMap<>();
								params.put(DocumentCrudHandler.PARAM_FILE_PATH, file);
								if (category != null) {
									params.put(DocumentCrudHandler.PARAM_DOC_CATEGORY, category.getName());
								}
								ExecutionEvent ev = new ExecutionEvent(cmd, params, null, null);
								created = cmd.executeWithChecks(ev);
							} catch (Exception e) {
								logger.error("drop error", e); //$NON-NLS-1$
							}
							// handle drop from same view
							if (currentDragSelection != null) {
								// if new document was created, delete source
								if (created instanceof java.util.Optional
										&& ((java.util.Optional<?>) created).isPresent()) {
									// TODO change if drag of list is implemented in drag support
									IDocument sourceDocument = (IDocument) currentDragSelection.getFirstElement();
									DocumentStoreServiceHolder.getService().removeDocument(sourceDocument);
									ElexisEventDispatcher.getInstance().fire(new ElexisEvent(sourceDocument,
											IDocument.class, ElexisEvent.EVENT_DELETE, ElexisEvent.PRIORITY_NORMAL));
								}
							}
						}
					}
				}
			}

		});

		final Transfer[] dragTransferTypes = new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() };
		viewer.addDragSupport(DND.DROP_COPY, dragTransferTypes, new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				for (Object object : selection.toList()) {
					IDocument document = (IDocument) object;
					if (document.getContentLength() < 1) {
						event.doit = false;
						SWTHelper.showError(Messages.Core_Error_while_exporting,
								Messages.DocumentView_exportErrorEmptyText + "\nDokument: " + document.getTitle());
						break;
					}
				}
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				currentDragSelection = selection;
				if (FileTransfer.getInstance().isSupportedType(event.dataType)) {
					String[] documents = new String[selection.size()];
					Set<String> titles = new HashSet<>();
					for (int i = 0; i < selection.size(); i++) {
						IDocument dh = (IDocument) selection.toList().get(i);
						try {
							String _titel = dh.getTitle();
							if (titles.contains(dh.getTitle())) {
								_titel = dh.getTitle() + "(" + i + ")";
							} else {
								titles.add(dh.getTitle());
							}
							String absPath = DocumentStoreServiceHolder.getService().saveContentToTempFile(dh, _titel,
									dh.getExtension(), true);
							documents[i] = absPath;
						} catch (ElexisException e) {
							event.doit = false;
							break;
						}
						event.data = documents;
					}
				}
			}
		});

		MenuManager menuManager = new MenuManager();
		viewer.getControl().setMenu(menuManager.createContextMenu(viewer.getControl()));
		getSite().registerContextMenu(menuManager, viewer);
		getSite().setSelectionProvider(viewer);

		viewer.setInput(ContextServiceHolder.get().getActivePatient().orElse(null));
		getSite().getPage().addPartListener(udpateOnVisible);
	}

	private void createFlatMenu(Composite filterComposite) {
		ToolBarManager tMenuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
		tMenuManager.add(new Action("Neuer Brief", Action.AS_PUSH_BUTTON) {
			{
				setImageDescriptor(Images.IMG_DOCUMENT_ADD.getImageDescriptor());
			}

			@Override
			public void run() {
				ParameterizedCommand cmd = commandService.createCommand("ch.elexis.core.ui.commands.BriefNew", null); //$NON-NLS-1$
				if (cmd != null) {
					handlerService.executeHandler(cmd);
				}
				super.run();
			}
		});
		tMenuManager.add(new Action("AUF erstellen und drucken") {
			{
				setImageDescriptor(Images.IMG_VIEW_WORK_INCAPABLE.getImageDescriptor());
			}

			@Override
			public void run() {
				ParameterizedCommand cmd = commandService.createCommand("ch.elexis.core.ui.commands.AufNew", null); //$NON-NLS-1$
				if (cmd != null) {
					Object createdAuf = handlerService.executeHandler(cmd);
					if (createdAuf instanceof ISickCertificate) {
						ContextServiceHolder.get().getRootContext().setTyped(createdAuf);
						// print
						cmd = commandService.createCommand("ch.elexis.core.ui.commands.AufPrint", null); //$NON-NLS-1$
						if (cmd != null) {
							handlerService.executeHandler(cmd);
						}
						ContextServiceHolder.get().getRootContext().removeTyped(ISickCertificate.class);
					}
				}
				super.run();
			}
		});
		tMenuManager.createControl(filterComposite);
	}

	private DocumentsFilterBarComposite addFilterBar(Composite parent) {

		List<FilterCategory> filters = new ArrayList<>();
		filters.add(new FilterCategory(null, "Alle"));
		filters.add(new FilterCategory(BriefConstants.UNKNOWN, "Briefe"));
		filters.add(new FilterCategory(BriefConstants.AUZ, "Auf"));
		filters.add(new FilterCategory(BriefConstants.RP, "Rezepte"));

		DocumentsFilterBarComposite filterBarComposite = new DocumentsFilterBarComposite(parent, SWT.NONE, filters);
		filterBarComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		filterBarComposite.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (contentProvider != null) {
					contentProvider.selectFilterCategory(event.getSelection());
				}
			}
		});
		return filterBarComposite;
	}

	private SelectionListener getSelectionAdapter(final TreeColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ovComparator.setBFlat(bFlat);
				ovComparator.setColumn(index);
				viewer.getTree().setSortDirection(ovComparator.getDirection());
				viewer.getTree().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	private void applySortDirection() {
		String[] usrSortSettings = sortSettings.split(","); //$NON-NLS-1$

		int propertyIdx = Integer.parseInt(usrSortSettings[0]);
		int direction = Integer.parseInt(usrSortSettings[1]);
		if (propertyIdx != 0) {
			sortViewer(propertyIdx, direction);
		}

	}

	private void sortViewer(int propertyIdx, int direction) {
		TreeColumn column = viewer.getTree().getColumn(propertyIdx);
		ovComparator.setColumn(propertyIdx);
		ovComparator.setDirection(direction);
		viewer.getTree().setSortDirection(ovComparator.getDirection());
		viewer.getTree().setSortColumn(column);
		viewer.refresh();
	}

	private void applyUsersColumnWidthSetting() {
		TreeColumn[] treeColumns = viewer.getTree().getColumns();
		String[] userColWidth = colWidth.split(","); //$NON-NLS-1$

		if (ConfigServiceHolder.getUser(SETTING_COLUMN_WIDTH, null) != null) {
			String ucw = ConfigServiceHolder.getUser(SETTING_COLUMN_WIDTH, colWidth);
			userColWidth = ucw.split(",");
		}

		for (int i = 0; i < treeColumns.length; i++) {
			treeColumns[i].setWidth(Integer.parseInt(userColWidth[i]));
		}
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		if (viewer != null) {
			viewer.getControl().setFocus();
		}
	}

	@Override
	public void refresh() {
		activePatient(ContextServiceHolder.get().getActivePatient().orElse(null));
	}

	private void makeActions() {
		doubleClickAction = new Action() {
			@Override
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				if (obj instanceof IDocument) {
					IDocument dh = (IDocument) obj;
					DocumentStoreServiceHolder.getService().getPersistenceObject(dh).ifPresent(po -> {
						ICommandService commandService = PlatformUI.getWorkbench()
								.getService(ICommandService.class);
						Command command = commandService.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"); //$NON-NLS-1$
						PlatformUI.getWorkbench().getService(IEclipseContext.class)
								.set(command.getId().concat(".selection"), new StructuredSelection(po)); //$NON-NLS-1$
						try {
							command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, null, null));
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

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	public void switchFlatView(boolean bFlat) {
		this.bFlat = bFlat;
		if (viewer != null) {
			ConfigServiceHolder.setUser(SETTING_FLAT_VIEW, bFlat);
			contentProvider.setFlat(bFlat);
			viewer.refresh();
		}
	}
}