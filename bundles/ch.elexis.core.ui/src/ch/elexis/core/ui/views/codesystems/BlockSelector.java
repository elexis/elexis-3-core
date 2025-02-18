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

package ch.elexis.core.ui.views.codesystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.builder.ICodeElementBlockBuilder;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.core.ui.actions.ToggleVerrechenbarFavoriteAction;
import ch.elexis.core.ui.commands.ExportiereBloeckeCommand;
import ch.elexis.core.ui.dialogs.BlockSelektor;
import ch.elexis.core.ui.dialogs.base.InputDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.selectors.FieldDescriptor;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.SelectorPanelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import jakarta.inject.Inject;

public class BlockSelector extends CodeSelectorFactory {
	protected static final String BLOCK_ONLY_FILTER_ENABLED = "blockselector/blockonlyfilter"; //$NON-NLS-1$
	protected static final String BLOCK_FILTER_ONLY_MANDATOR = "blockselector/blockfilteronlymandator"; //$NON-NLS-1$

	private IAction deleteAction, createAction, exportAction, copyAction, searchBlocksOnly, searchFilterMandator;
	private CommonViewer cv;
	private MenuManager mgr;
	static SelectorPanelProvider slp;
	int eventType = SWT.KeyDown;

	ToggleVerrechenbarFavoriteAction tvfa = new ToggleVerrechenbarFavoriteAction();
	ISelectionChangedListener selChangeListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			TreeViewer tv = (TreeViewer) event.getSource();
			StructuredSelection ss = (StructuredSelection) tv.getSelection();
			Object selected = null;
			Object firstElement = ss.isEmpty() ? null : ss.getFirstElement();
			if (firstElement instanceof BlockTreeViewerItem) {
				selected = ((BlockTreeViewerItem) firstElement).getBlock();
			}
			tvfa.updateSelection((Identifiable) selected);
			if (selected != null) {
				ContextServiceHolder.get().getRootContext().setTyped(selected);
			}
		}
	};

	@Override
	public ViewerConfigurer createViewerConfigurer(CommonViewer cv) {
		this.cv = cv;
		cv.setSelectionChangedListener(selChangeListener);
		makeActions();
		mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		mgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				Object selected = cv.getSelection()[0];
				if (selected instanceof BlockTreeViewerItem) {
					manager.add(tvfa);
					manager.add(deleteAction);
					manager.add(copyAction);
				}
				addPopupCommandContributions(manager, cv.getSelection());
			}
		});

		cv.setContextMenu(mgr);

		FieldDescriptor<?>[] lbName = new FieldDescriptor<?>[] { new FieldDescriptor<ICodeElementBlock>("Name") }; //$NON-NLS-1$

		// add keyListener to search field
		Listener keyListener = new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (event.type == eventType) {
					if (event.keyCode == SWT.CR || event.keyCode == SWT.KEYPAD_CR) {
						slp.fireChangedEvent();
					}
				}
			}
		};
		for (FieldDescriptor<?> lbn : lbName) {
			lbn.setAssignedListener(eventType, keyListener);
		}

		slp = new SelectorPanelProvider(lbName, true);
		slp.addActions(createAction, exportAction, searchBlocksOnly, searchFilterMandator);
		ViewerConfigurer vc = new ViewerConfigurer(new BlockContentProvider(this, cv),
				new BlockTreeViewerItem.ColorizedLabelProvider(), slp, new ViewerConfigurer.DefaultButtonProvider(),
				new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE, SWT.NONE, null));
		vc.addDragSourceSelectionRenderer(new GenericObjectDragSource.ISelectionRenderer() {

			@Override
			public List<Object> getSelection() {
				IStructuredSelection selection = cv.getViewerWidget().getStructuredSelection();
				if (!selection.isEmpty()) {
					return collectSelections(selection);
				}
				return Collections.emptyList();
			}
		});
		return vc.setContentType(ContentType.GENERICOBJECT);
	}

	@Override
	public Class<?> getElementClass() {
		return ICodeElementBlock.class;
	}

	@Override
	public void dispose() {

	}

	private void makeActions() {
		deleteAction = new Action("Block löschen") {
			@Override
			public void run() {
				Object selected = cv.getSelection()[0];
				if (selected instanceof BlockTreeViewerItem) {
					ICodeElementBlock block = ((BlockTreeViewerItem) selected).getBlock();
					CoreModelServiceHolder.get().delete(block);
					cv.notify(CommonViewer.Message.update);
				}
			}
		};
		createAction = new Action("neu erstellen") {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText("Neuen Block erstellen");
			}

			@Override
			public void run() {
				String[] v = cv.getConfigurer().getControlFieldProvider().getValues();
				if (v != null && v.length > 0 && v[0] != null && v[0].length() > 0) {
					IQuery<ICodeElementBlock> query = CoreModelServiceHolder.get().getQuery(ICodeElementBlock.class);
					query.and("name", COMPARATOR.EQUALS, v[0]); //$NON-NLS-1$
					if (!query.execute().isEmpty()) {
						MessageDialog.openError(Display.getDefault().getActiveShell(), "Fehler",
								"Ein Block mit dem Namen [" + v[0] + "] existiert bereits");
					} else {
						new ICodeElementBlockBuilder(CoreModelServiceHolder.get(), v[0])
								.mandator(ContextServiceHolder.get().getActiveMandator().orElse(null)).buildAndSave();

						cv.notify(CommonViewer.Message.update_keeplabels);
					}
				}
			}
		};
		exportAction = new Action("Blöcke exportieren") {
			{
				setImageDescriptor(Images.IMG_EXPORT.getImageDescriptor());
				setToolTipText("Exportiert alle Blöcke in eine SGAM-xChange-Datei");
			}

			@Override
			public void run() {
				// Handler.execute(null, ExportiereBloeckeCommand.ID, null);
				try {
					new ExportiereBloeckeCommand().execute(null);
				} catch (ExecutionException e) {
					LoggerFactory.getLogger(getClass()).error("Error exporting block", e); //$NON-NLS-1$
				}
			}
		};
		copyAction = new Action("Block kopieren") {
			{
				setImageDescriptor(Images.IMG_COPY.getImageDescriptor());
				setToolTipText("Den Block umbenennen und kopieren");
			}

			@Override
			public void run() {
				Object o = cv.getSelection()[0];
				if (o instanceof BlockTreeViewerItem) {
					ICodeElementBlock sourceBlock = ((BlockTreeViewerItem) o).getBlock();
					InputDialog inputDlg = new InputDialog(Display.getDefault().getActiveShell(), "Block kopieren",
							"Bitte den Namen der Kopie eingeben bzw. bestätigen", sourceBlock.getText() + " Kopie",
							new IInputValidator() {

								@Override
								public String isValid(String newText) {
									return (newText != null && !newText.isEmpty()) ? null : "Fehler, kein Name.";
								}
							}, SWT.BORDER);
					if (inputDlg.open() == Window.OK) {
						String newName = inputDlg.getValue();
						new ICodeElementBlockBuilder(CoreModelServiceHolder.get(), sourceBlock.getCode())
								.mandator(sourceBlock.getMandator()).text(newName).elements(sourceBlock.getElements())
								.buildAndSave();
						cv.notify(CommonViewer.Message.update);
					}
				}
			}
		};
		searchBlocksOnly = new Action("Blockinhalt nicht durchsuchen", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
				setToolTipText("Blockinhalt nicht durchsuchen");
				setChecked(ConfigServiceHolder.getUser(BLOCK_ONLY_FILTER_ENABLED, false));
			}

			public void run() {
				ConfigServiceHolder.setUser(BLOCK_ONLY_FILTER_ENABLED, isChecked());
			};
		};
		searchFilterMandator = new Action("Nur Blöcke des aktiven Mandanten", Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
				setToolTipText("Nur Blöcke des aktiven Mandanten");
				setChecked(ConfigServiceHolder.getUser(BLOCK_FILTER_ONLY_MANDATOR, false));
			}

			public void run() {
				ConfigServiceHolder.setUser(BLOCK_FILTER_ONLY_MANDATOR, isChecked());

				if (cv.getConfigurer().getContentProvider() instanceof BlockContentProvider) {
					BlockContentProvider blockContentProvider = (BlockContentProvider) cv.getConfigurer()
							.getContentProvider();
					blockContentProvider.refreshViewer();
				}
			};
		};
	}

	public static class BlockContentProvider
			implements ViewerConfigurer.ICommonViewerContentProvider, ITreeContentProvider {
		private BlockSelector selector;
		private CommonViewer cv;

		private String queryFilter;
		private HashMap<ICodeElementBlock, BlockTreeViewerItem> blockItemMap;

		@Optional
		@Inject
		public void udpateBlock(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) ICodeElementBlock block) {
			if (block != null && cv != null && cv.getViewerWidget() != null
					&& !cv.getViewerWidget().getControl().isDisposed()) {
				BlockTreeViewerItem item = blockItemMap.get(block);
				cv.getViewerWidget().refresh(item, true);
			}
		}

		BlockContentProvider(BlockSelector selector, CommonViewer cv) {
			this.cv = cv;
			this.selector = selector;

			CoreUiUtil.injectServicesWithContext(this);
		}

		public void startListening() {
			cv.getConfigurer().getControlFieldProvider().addChangeListener(this);
		}

		public void stopListening() {
			cv.getConfigurer().getControlFieldProvider().removeChangeListener(this);
		}

		public Object[] getElements(Object inputElement) {
			IQuery<ICodeElementBlock> query = CoreModelServiceHolder.get().getQuery(ICodeElementBlock.class);
			query.and("id", COMPARATOR.NOT_EQUALS, "Version"); //$NON-NLS-1$ //$NON-NLS-2$
			if ((queryFilter != null && queryFilter.length() > 2)) {
				if (selector.searchBlocksOnly.isChecked()) {
					query.and("name", COMPARATOR.LIKE, "%" + queryFilter + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				} else {
					query.startGroup();
					query.and("name", COMPARATOR.LIKE, "%" + queryFilter + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					query.or("services", COMPARATOR.LIKE, "%" + queryFilter + "%"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					query.andJoinGroups();
				}
			}
			query.orderBy("name", ORDER.ASC); //$NON-NLS-1$
			blockItemMap = new HashMap<>();
			List<BlockTreeViewerItem> list = query.execute().stream().filter(b -> applyMandatorFilter(b)).map(b -> {
				BlockTreeViewerItem item = BlockTreeViewerItem.of(b);
				blockItemMap.put(b, item);
				return item;
			}).collect(Collectors.toList());
			return list.toArray();
		}

		private boolean applyMandatorFilter(ICodeElementBlock b) {
			if (selector.searchFilterMandator.isChecked()) {
				IMandator mandator = ContextServiceHolder.get().getActiveMandator().orElse(null);
				IMandator blockMandator = b.getMandator();
				if (blockMandator != null && mandator != null) {
					return blockMandator.getId().equals(mandator.getId());
				}
			}
			return true;
		}

		public void dispose() {
			stopListening();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		/** Vom ControlFieldProvider */
		public void changed(HashMap<String, String> vals) {
			queryFilter = vals.get("Name"); //$NON-NLS-1$
			refreshViewer();
		}

		private void refreshViewer() {
			cv.getViewerWidget().getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					StructuredViewer viewer = cv.getViewerWidget();
					if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
						viewer.setSelection(new StructuredSelection());
						viewer.getControl().setRedraw(false);
						viewer.refresh();
						if ((queryFilter != null && queryFilter.length() > 2)) {
							if (!selector.searchBlocksOnly.isChecked()) {
								if (viewer instanceof TreeViewer) {
									((TreeViewer) viewer).expandAll();
								}
							}
						} else {
							((TreeViewer) viewer).collapseAll();
						}
						viewer.getControl().setRedraw(true);
					}
				}
			});
		}

		/** Vom ControlFieldProvider */
		public void reorder(String field) {

		}

		/** Vom ControlFieldProvider */
		public void selected() {
			// nothing to do
		}

		public Object[] getChildren(Object element) {
			if (element instanceof BlockTreeViewerItem) {
				BlockTreeViewerItem item = (BlockTreeViewerItem) element;
				return item.getChildren().toArray();
			}
			return Collections.emptyList().toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof BlockTreeViewerItem) {
				BlockTreeViewerItem item = (BlockTreeViewerItem) element;
				return item.hasChildren();
			}
			return false;
		}

		@Override
		public void init() {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public SelectionDialog getSelectionDialog(Shell parent, Object data) {
		return new BlockSelektor(parent, data);
	}

	@Override
	public String getCodeSystemName() {
		return "Block"; //$NON-NLS-1$
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return cv.getViewerWidget();
	}

	@Override
	public MenuManager getMenuManager() {
		return mgr;
	}

	@Override
	public IDoubleClickListener getDoubleClickListener() {
		return new BlockDoubleClickListener();
	}

	private List<Object> collectSelections(IStructuredSelection selection) {
		List<Object> ret = new ArrayList<>();
		for (Object selected : selection.toList()) {
			if (selected instanceof BlockTreeViewerItem) {
				ret.add(((BlockTreeViewerItem) selected).getBlock());
			} else if (selected instanceof BlockElementViewerItem) {
				if (((BlockElementViewerItem) selected).getFirstElement() instanceof Identifiable) {
					// compatibility for NOPO
					ret.add((Identifiable) ((BlockElementViewerItem) selected).getFirstElement());
				}
			}
		}
		return ret;
	}

	private class BlockDoubleClickListener implements IDoubleClickListener {
		@Override
		public void doubleClick(DoubleClickEvent event) {
			IStructuredSelection selection = cv.getViewerWidget().getStructuredSelection();
			if (!selection.isEmpty()) {
				List<Object> ret = collectSelections(selection);
				ICodeSelectorTarget target = CodeSelectorHandler.getInstance().getCodeSelectorTarget();
				for (Object o : ret) {
					target.codeSelected(o);
				}
			}
		}
	}
}
