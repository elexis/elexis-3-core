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

package ch.elexis.core.ui.util.viewers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IViewSite;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDragSource.ISelectionRenderer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ContentType;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldProvider;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.Tree;

/**
 * Basis des Viewer-Systems. Ein Viewer zeigt eine Liste von Objekten einer
 * bestimmten PersistentObject -Unterklasse an und ermöglicht das Filtern der
 * Anzeige sowie das Erstellen neuer Objekte dieser Klasse. Der CommonViewer
 * stellt nur die Oberfläche bereit (oben ein Feld zum Filtern, in der Mitte die
 * Liste und unten ein Button zum Erstellen eines neuen Objekts). Die
 * Funktionalität muss von einem ViewerConfigurer bereitgestellt werden. Dieser
 * ist wiederum nur ein Container zur Breitstellung verschiedener Provider. NB:
 * CommonViewer ist eigentlich ein Antipattern (nämlich ein Golden Hammer). Er
 * verkürzt Entwicklungszeit, aber auf Kosten der Flexibilität und der optimalen
 * Anpassung Wann immer Zeit und Ressourcen genügen, sollte einer individuellen
 * Lösung der Vorzug gegeben werden.
 *
 * @see ViewerConfigurer
 * @author Gerry
 */
public class CommonViewer implements ISelectionChangedListener, IDoubleClickListener {

	private static final boolean OS_IS_WIN = System.getProperty("os.name").toLowerCase().indexOf("win") >= 0; //$NON-NLS-1$ //$NON-NLS-2$

	protected ViewerConfigurer viewerConfigurer;
	protected StructuredViewer viewer;
	protected Button bNew;
	private IAction createObjectAction;
	private Composite parent;
	private ISelectionChangedListener selChangeListener;

	private String namedSelection;

	/**
	 *
	 * @since 3.7 updateSingle to refresh a single object
	 */
	public enum Message {
		update, empty, notempty, update_keeplabels, updateSingle
	}

	private HashSet<PoDoubleClickListener> dlListeners;
	private MenuManager mgr;
	private Composite composite;
	private String viewName = null;

	private boolean scrolledToBottom;
	private boolean showDisableLimit;
	private Button disableLimitBtn;

	private boolean changeContextSelection = true;

	public Composite getParent() {
		return parent;
	}

	public CommonViewer() {
		viewName = "unknown"; //$NON-NLS-1$
	}

	/**
	 * Set the name that is used to update the selection in the current root
	 * {@link IContext}.
	 *
	 * @param name
	 */
	public void setNamedSelection(String name) {
		this.namedSelection = name;
	}

	/**
	 * Sets the view name. Mainly used for GUI-Jubula tests. The view name is used
	 * to uniquely identify the toolbar items by setting the TEST_COMP_NAME
	 * accordingly
	 *
	 * @param s
	 */
	public void setViewName(String s) {
		viewName = s;
	}

	/**
	 * Gets the view name. Mainly used for GUI-Jubula tests.
	 *
	 */
	public String getViewName() {
		return viewName;
	}

	public void setObjectCreateAction(IViewSite site, IAction action) {
		site.getActionBars().getToolBarManager().add(action);
		action.setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
		createObjectAction = action;
	}

	/**
	 * Den Viewer erstellen
	 *
	 * @param viewerConfigurer ViewerConfigurer, der die Funktionalität
	 *                         bereitstellt. Alle Felder des Configurers müssen vor
	 *                         Aufruf von create() gültig gesetzt sein.
	 * @param parent           Parent.Komponente
	 * @param style            SWT-Stil für das umgebende Composite
	 * @param input            Input Objekt für den Viewer
	 */
	public void create(ViewerConfigurer viewerConfigurer, Composite parent, int style, Object input) {
		this.viewerConfigurer = viewerConfigurer;
		this.parent = parent;
		Composite ret = new Composite(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		ret.setLayout(layout);

		if (parent.getLayout() instanceof GridLayout) {
			GridData gd = new GridData(GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL | GridData.GRAB_HORIZONTAL
					| GridData.FILL_HORIZONTAL);
			ret.setLayoutData(gd);
		}
		ControlFieldProvider cfp = viewerConfigurer.getControlFieldProvider();
		if (cfp != null) {
			ret.setData("TEST_COMP_NAME", "cv_ret_" + viewName); // for Jubula //$NON-NLS-1$ //$NON-NLS-2$
			Composite ctlf = viewerConfigurer.getControlFieldProvider().createControl(ret);
			ctlf.setData("TEST_COMP_NAME", "cv_ctlf_" + viewName); // for Jubula //$NON-NLS-1$ //$NON-NLS-2$
			ctlf.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		}
		viewer = viewerConfigurer.getWidgetProvider().createViewer(ret);
		GridData gdView = new GridData(
				GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.FILL_VERTICAL);
		gdView.verticalAlignment = SWT.FILL;
		viewer.setUseHashlookup(true);
		viewer.getControl().setLayoutData(gdView);
		viewer.setContentProvider(viewerConfigurer.getContentProvider());
		viewer.setLabelProvider(viewerConfigurer.getLabelProvider());
		if (viewerConfigurer.getSelectionChangedListener() != null) {
			viewer.addSelectionChangedListener(viewerConfigurer.getSelectionChangedListener());
		} else {
			viewer.addSelectionChangedListener(this);
		}
		if (viewer.getControl() instanceof Table) {
			Table table = (Table) viewer.getControl();
			ScrollBar verticalBar = table.getVerticalBar();
			verticalBar.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (showDisableLimit && table.getItemCount() > 25) {
						scrolledToBottom = verticalBar.getSelection() + verticalBar.getThumb() == verticalBar
								.getMaximum();
						if (scrolledToBottom) {
							showDisableLimitButton(); // scrolled to end
						} else {
							hideDisableLimitButton();
						}
					}
				}
			});
		}
		if (viewerConfigurer.getDoubleClickListener() != null) {
			viewer.addDoubleClickListener(viewerConfigurer.getDoubleClickListener());
		}
		bNew = viewerConfigurer.getButtonProvider().createButton(ret);
		if (bNew != null) {
			if (viewName != null) {
				bNew.setData("TEST_COMP_NAME", "cv_bNew_" + viewName + "_btn"); // for //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
																				// Jubula
			}
			GridData gdNew = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
			bNew.setLayoutData(gdNew);
			if (viewerConfigurer.getButtonProvider().isAlwaysEnabled() == false) {
				bNew.setEnabled(false);
			}
		}
		/*
		 * 3 viewer.getControl().addMouseListener(new MouseAdapter(){ public void
		 * mouseDoubleClick(MouseEvent e) { log.log("Doppelklick",Log.DEBUGMSG);
		 * ctl.doubleClicked(getSelection()); }});
		 */
		/*
		 * viewer.addDragSupport(DND.DROP_COPY,new Transfer[]
		 * {TextTransfer.getInstance()},
		 */
		if (viewerConfigurer.getContentType() == ContentType.PERSISTENTOBJECT) {
			if (viewerConfigurer.poSelectionRenderer != null) {
				new PersistentObjectDragSource(viewer.getControl(), viewerConfigurer.poSelectionRenderer);
			} else {
				new PersistentObjectDragSource(viewer.getControl(), new ISelectionRenderer() {
					public List<PersistentObject> getSelection() {
						Object[] sel = CommonViewer.this.getSelection();
						ArrayList<PersistentObject> ret = new ArrayList<>(sel.length);
						for (Object o : sel) {
							if (o instanceof PersistentObject) {
								ret.add((PersistentObject) o);
							} else if (o instanceof Tree<?>) {
								Object b = ((Tree<?>) o).contents;
								if (b instanceof PersistentObject) {
									ret.add((PersistentObject) b);
								}
							}
						}
						return ret;
					}
				});
			}
		} else if (viewerConfigurer.getContentType() == ContentType.GENERICOBJECT) {
			if (viewerConfigurer.goSelectionRenderer != null) {
				new GenericObjectDragSource(viewer, viewerConfigurer.goSelectionRenderer);
			} else {
				new GenericObjectDragSource(viewer);
			}
		}
		if (mgr != null) {
			viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		}
		viewerConfigurer.getContentProvider().init();
		viewer.setInput(input);
		viewer.getControl().pack();
		composite = ret;
	}

	public Composite getComposite() {
		return composite;
	}

	/**
	 * Die aktuelle Auswahl des Viewers liefern
	 *
	 * @return null oder ein Array mit den selektierten Objekten-
	 */
	public Object[] getSelection() {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel != null) {
			return sel.toArray();
		}
		return null;

	}

	/**
	 * Das selektierte Element des Viewers einstellen
	 *
	 * @param o Das Element
	 */
	public void setSelection(Object o, boolean fireEvents) {
		if (fireEvents == false) {
			viewer.removeSelectionChangedListener(this);
			viewer.setSelection(new StructuredSelection(o), true);
			viewer.addSelectionChangedListener(this);
		} else {
			viewer.setSelection(new StructuredSelection(o), true);
		}

	}

	/**
	 * Den darunterliegenden JFace-Viewer liefern
	 */
	public StructuredViewer getViewerWidget() {
		return viewer;
	}

	/**
	 * @return the {@link #getViewerWidget()} current selections first element or
	 *         <code>null</code>
	 * @since 3.2.0
	 */
	public Object getViewerWidgetFirstSelection() {
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		if (selection == null || selection.size() == 0) {
			return null;
		}
		return selection.getFirstElement();
	}

	public ViewerConfigurer getConfigurer() {
		return viewerConfigurer;
	}

	/**
	 * den Viewer über eine Änderung benachrichtigen
	 *
	 * @param m eine Message: update: der Viewer muss neu eingelesen werden empty:
	 *          Die Auswahl ist leer. notempty: Die Auswahl ist nicht (mehr) leer.
	 */
	public void notify(final Message m) {
		notify(m, null);
	}

	/**
	 *
	 * @param m
	 * @param object an optional object (only used for {@link Message#updateSingle}
	 * @since 3.7
	 * @see CommonViewer#notify(Message)
	 */
	public void notify(final Message m, Object object) {
		if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed()) {
			return;
		}
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run() {
				switch (m) {
				case update:
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh(true);
					}
					break;
				case updateSingle:
					if (!viewer.getControl().isDisposed() && object != null) {
						viewer.refresh(object, true);
					}
					break;
				case update_keeplabels:
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh(false);
					}
					break;
				case empty:
					if (bNew != null) {
						if (viewerConfigurer.getButtonProvider().isAlwaysEnabled() == false) {
							bNew.setEnabled(false);
						}
					}
					if (createObjectAction != null) {
						createObjectAction.setEnabled(false);
					}
					break;
				case notempty:
					if (bNew != null) {
						bNew.setEnabled(true);
					}
					if (createObjectAction != null) {
						createObjectAction.setEnabled(true);
					}
					break;
				}
			}
		});

	}

	public void selectionChanged(SelectionChangedEvent event) {
		Object[] sel = getSelection();
		if (sel != null && sel.length != 0) {
			if (sel[0] instanceof Tree<?>) {
				sel[0] = ((Tree<?>) sel[0]).contents;
			}
			if (changeContextSelection) {
				if (sel[0] instanceof PersistentObject) {
					Optional<Class<?>> modelClass = ElexisEventDispatcher
							.getCoreModelInterfaceForElexisClass(sel[0].getClass());
					if (modelClass.isPresent()) {
						NoPoUtil.loadAsIdentifiable((PersistentObject) sel[0], modelClass.get())
								.ifPresent(indentifiable -> {
									ContextServiceHolder.get().getRootContext().setTyped(indentifiable);
								});
					} else {
						LoggerFactory.getLogger(getClass())
								.warn("PersistentObject selection [" + sel[0] + "] in context");
						ContextServiceHolder.get().getRootContext().setTyped(sel[0]);
					}
				} else {
					if (StringUtils.isNotBlank(namedSelection)) {
						ContextServiceHolder.get().getRootContext().setNamed(namedSelection, sel[0]);
					} else {
						if (sel[0] instanceof Identifiable) {
							ContextServiceHolder.get().getRootContext().setTyped(sel[0]);
						}
					}
				}
			}
		}
		if (selChangeListener != null)
			selChangeListener.selectionChanged(event);
	}

	public void dispose() {
		if (viewerConfigurer.getDoubleClickListener() != null)
			viewer.removeDoubleClickListener(viewerConfigurer.getDoubleClickListener());
		viewer.removeSelectionChangedListener(this);
	}

	public void addDoubleClickListener(PoDoubleClickListener dl) {
		if (dlListeners == null) {
			dlListeners = new HashSet<>();
			getViewerWidget().addDoubleClickListener(this);
		}
		dlListeners.add(dl);
	}

	/**
	 * Register an additional selection changed listener to get informed
	 *
	 * @param selChangeListener the {@link ISelectionChangedListener} or
	 *                          <code>null</code> to unset
	 * @since 3.1
	 */
	public void setSelectionChangedListener(@Nullable ISelectionChangedListener selChangeListener) {
		this.selChangeListener = selChangeListener;
	}

	public void removeDoubleClickListener(PoDoubleClickListener dl) {
		if (dlListeners == null) {
			return;
		}
		dlListeners.remove(dl);
		if (dlListeners.isEmpty()) {
			getViewerWidget().removeDoubleClickListener(this);
			dlListeners = null;
		}
	}

	/**
	 * Kontextmenu an den unterliegenden Viewer binden. Falls dieser zum Zeitpunkt
	 * des Aufrufs dieser Methode noch nicht existiert, wird das Einbinden
	 * verzögert.
	 *
	 * @param mgr ein fertig konfigurierter jface-MenuManager
	 */
	public void setContextMenu(MenuManager mgr) {
		this.mgr = mgr;
		if (viewer != null) {
			viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		}
	}

	public Button getButton() {
		return bNew;
	}

	public interface PoDoubleClickListener {
		public void doubleClicked(PersistentObject obj, CommonViewer cv);
	}

	public void doubleClick(DoubleClickEvent event) {
		if (dlListeners != null) {
			Iterator<PoDoubleClickListener> it = dlListeners.iterator();
			while (it.hasNext()) {
				PoDoubleClickListener dl = it.next();
				if (viewerConfigurer.poSelectionRenderer != null) {
					List<PersistentObject> selected = viewerConfigurer.poSelectionRenderer.getSelection();
					if (!selected.isEmpty()) {
						dl.doubleClicked(selected.get(0), this);
					}
				} else {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if ((sel != null) && (!sel.isEmpty())) {
						Object element = sel.getFirstElement();
						if (element instanceof Tree<?>) {
							element = ((Tree<?>) element).contents;
						}
						if (element instanceof PersistentObject) {
							dl.doubleClicked((PersistentObject) element, this);
						}
					}
				}
			}
		}

	}

	public MenuManager getMgr() {
		return mgr;
	}

	public boolean isDisposed() {
		return viewer == null || viewer.getControl().isDisposed();
	}

	public void setLimitReached(boolean value, int limit) {
		showDisableLimit = value;
		Display.getDefault().asyncExec(() -> {
			if (disableLimitBtn == null) {
				addLimitButton(limit);
			}
			if (value && isNoScroll()) {
				showDisableLimitButton();
			} else {
				hideDisableLimitButton();
			}
		});
	}

	/**
	 * Resets the scrollbar position.
	 * 
	 * @param tv
	 * @param limit
	 */
	public void resetScrollbarPosition(TableViewer tv, boolean limit) {
		Table table = (Table) tv.getControl();
		ScrollBar scrollbar = table.getVerticalBar();
		int position = scrollbar.getSelection() + scrollbar.getThumb();
		if (!limit && position > scrollbar.getMinimum()) {
			table.setSelection(0);
		}
	}

	/**
	 * Returns whether the current position in the list or rather scrollbar has
	 * reached the bottom.
	 * 
	 * @return
	 */
	private boolean isNoScroll() {
		if (viewer.getControl() instanceof Table && !viewer.getControl().isDisposed()) {
			// test if table elements do not reach bottom
			Table table = (Table) viewer.getControl();
			ScrollBar verticalBar = table.getVerticalBar();
			if (verticalBar != null) {
				return verticalBar.getSelection() + verticalBar.getThumb() == verticalBar.getMaximum();
			}
		}
		return false;
	}

	private void addLimitButton(int limit) {
		if (disableLimitBtn == null) {
			disableLimitBtn = new Button(composite, SWT.FLAT);
			disableLimitBtn.setText("Mehr als " + limit + " laden ..."); //$NON-NLS-2$
			disableLimitBtn.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					((CommonViewerContentProvider) viewer.getContentProvider()).setIgnoreLimit(true);
					CommonViewer.this.notify(CommonViewer.Message.update);
					showDisableLimit = false;
					hideDisableLimitButton();
				}
			});
			disableLimitBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		}
	}

	private void showDisableLimitButton() {
		if (disableLimitBtn != null) {
			((GridData) disableLimitBtn.getLayoutData()).exclude = false;
			disableLimitBtn.setVisible(true);
		}
		composite.layout();
		viewer.getControl().getParent().layout();
	}

	private void hideDisableLimitButton() {
		if (disableLimitBtn != null) {
			((GridData) disableLimitBtn.getLayoutData()).exclude = true;
			disableLimitBtn.setVisible(false);
		}
		composite.layout();
		viewer.getControl().getParent().layout();
	}

	public void disableContextSelection() {
		this.changeContextSelection = false;
	}
}
