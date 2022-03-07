/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.textsystem;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IDataAccess;
import ch.elexis.core.data.interfaces.IDataAccess.Element;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.data.util.SortedList;
import ch.elexis.core.ui.util.CoreUiUtil;
import ch.elexis.core.ui.views.TextView;

public class PlatzhalterView extends ViewPart {
	public static final String ID = "ch.elexis.views.textsystem.Platzhalterview"; //$NON-NLS-1$
	
	private TreeViewer viewer;
	
	class PlatzhalterContentProvider implements ITreeContentProvider {
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput){}
		
		public void dispose(){}
		
		public Object[] getElements(Object inputElement){
			return ((List<?>) inputElement).toArray();
		}
		
		public Object[] getChildren(Object parentElement){
			PlatzhalterTreeData entry = (PlatzhalterTreeData) parentElement;
			List<PlatzhalterTreeData> childrenList = entry.getChildren();
			return childrenList.toArray(new PlatzhalterTreeData[childrenList.size()]);
		}
		
		public Object getParent(Object element){
			PlatzhalterTreeData entry = (PlatzhalterTreeData) element;
			return entry.getParent();
		}
		
		public boolean hasChildren(Object element){
			return getChildren(element).length > 0;
		}
	}
	
	private class PlatzhalterLabelProvider extends CellLabelProvider {
		@Override
		public String getToolTipText(Object element){
			PlatzhalterTreeData data = (PlatzhalterTreeData) element;
			if (data != null) {
				return data.getDescription();
			}
			return null;
		}
		
		@Override
		public Point getToolTipShift(Object object){
			return new Point(5, 5);
		}
		
		@Override
		public int getToolTipDisplayDelayTime(Object object){
			return 500;
		}
		
		@Override
		public int getToolTipTimeDisplayed(Object object){
			return 5000;
		}
		
		@Override
		public void update(ViewerCell cell){
			PlatzhalterTreeData data = (PlatzhalterTreeData) cell.getElement();
			if (data != null) {
				cell.setText(data.getName());
			}
		}
	};
	
	/**
	 * Copy selected key to clipboard
	 */
	private void copyToClipboard(){
		String key = getSelectedKey();
		if (key != null) {
			Clipboard clipboard = new Clipboard(getViewSite().getShell().getDisplay());
			clipboard.setContents(new Object[] {
				key
			}, new Transfer[] {
				TextTransfer.getInstance()
			});
			clipboard.dispose();
		}
	}
	
	/**
	 * Returns the key of the selection or null
	 */
	private String getSelectedKey(){
		TreeItem[] items = viewer.getTree().getSelection();
		if (items != null && items.length > 0) {
			String key = ((PlatzhalterTreeData) items[0].getData()).getKey();
			if (key != null && key.length() > 0) {
				return key;
			}
		}
		return null;
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Text txtInfo = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP);
		txtInfo.setText(Messages.PlatzhalterView_message_Info);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtInfo);
		
		viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTree());
		
		viewer.setLabelProvider(new PlatzhalterLabelProvider());
		viewer.setContentProvider(new PlatzhalterContentProvider());
		viewer.setInput(getTreeData());
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		
		// Popup-Menu "Kopieren"
		MenuManager popupMenuManager = new MenuManager();
		Menu menu = popupMenuManager.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		
		final Action copyAction = new Action(Messages.PlatzhalterView_menu_copy) {
			@Override
			public String getId(){
				return "copyId"; //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				copyToClipboard();
			}
		};
		popupMenuManager.add(copyAction);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				copyAction.setEnabled(getSelectedKey() != null);
			}
		});
		
		// CTRL+C
		viewer.getTree().addKeyListener(new KeyAdapter() {
			private final static int C = 99;
			
			private boolean isActive(final int stateMask, final int keyCode){
				boolean modifiersOk = true;
				if ((stateMask & SWT.CTRL) == 0) {
					modifiersOk = false;
				}
				return modifiersOk && keyCode == C;
			}
			
			@Override
			public void keyPressed(KeyEvent e){
				if (isActive(e.stateMask, e.keyCode)) {
					copyToClipboard();
				}
			}
		});
		
		// Doubleclick
		viewer.getTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e){
				String key = getSelectedKey();
				if (key != null) {
					for (IViewReference viewRef : getViewSite().getPage().getViewReferences()) {
						if (TextView.ID.equals(viewRef.getId())) {
							TextView txtView = (TextView) viewRef.getPart(false);
							if (txtView != null) {
								txtView.getTextContainer().getPlugin().insertText((Object) null,
									key, SWT.LEFT);
							}
						}
					}
				}
			}
		});
		
		// Drag & Drop
		DragSource dragSource = new DragSource(viewer.getTree(), DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] {
			TextTransfer.getInstance()
		});
		dragSource.addDragListener(new DragSourceAdapter() {
			public void dragStart(DragSourceEvent event){
				event.doit = getSelectedKey() != null;
			}
			
			public void dragSetData(DragSourceEvent event){
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					String key = getSelectedKey();
					if (key != null) {
						event.data = key;
					}
				}
			}
		});
	}
	
	/**
	 * Retourniert Liste aller Platzhalter als Tree
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SortedList<PlatzhalterTreeData> getTreeData(){
		PlatzhalterTreeData root = new PlatzhalterTreeData("Root", "", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		// Basis Platzhalter
		PlatzhalterProperties props = new PlatzhalterProperties();
		root.addChildren(props.getList());
		
		// DataAccess TextPlaceHolder implementations
		List<IConfigurationElement> textPlaceHolderList =
			Extensions.getExtensions(ExtensionPointConstantsData.DATA_ACCESS, "TextPlaceHolder");//$NON-NLS-1$ //$NON-NLS-2$
		for (IConfigurationElement iConfigurationElement : textPlaceHolderList) {
			boolean found = false;
			String name = iConfigurationElement.getAttribute("name");
			String type = iConfigurationElement.getAttribute("type");
			String typeName = type.substring(type.lastIndexOf('.') + 1);
			if (name != null && type != null) {
				PlatzhalterTreeData treeData = root.getChild(typeName);
				if (treeData != null) {
					PlatzhalterTreeData childData = treeData.getChild(name);
					if (childData != null) {
						found = true;
					}
				}
			}
			if (!found) {
				PlatzhalterTreeData treeData = root.getChild(typeName);
				if (treeData == null) {
					treeData = new PlatzhalterTreeData(typeName, "", "");
				}
				treeData
					.addChild(new PlatzhalterTreeData(name, "[" + typeName + "." + name + "]", ""));
			}
		}
		
		// IDataAccess Implementations
		List<IDataAccess> dataAccessList =
			Extensions.getClasses(ExtensionPointConstantsData.DATA_ACCESS, "DataAccess", "class");//$NON-NLS-1$ //$NON-NLS-2$
		for (IDataAccess dataAccess : dataAccessList) {
			PlatzhalterTreeData treeData =
				new PlatzhalterTreeData(dataAccess.getName(), "", dataAccess.getDescription()); //$NON-NLS-1$
			if (dataAccess.getList() != null) {
				for (Element element : dataAccess.getList()) {
					treeData.addChild(new PlatzhalterTreeData(element.getName(),
						element.getPlaceholder(), element.getName()));
				}
			}
			root.addChild(treeData);
		}
		
		return root.getChildren();
	}
	
	@Override
	public void setFocus(){
		viewer.getTree().setFocus();
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
	
}
