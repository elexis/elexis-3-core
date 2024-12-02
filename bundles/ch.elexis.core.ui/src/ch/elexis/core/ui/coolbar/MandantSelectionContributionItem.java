/*******************************************************************************
 * Copyright (c) 2006-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Descher - initial implementation
 *    MEDEVIT 	 - adaptations according to #2112
 *******************************************************************************/
package ch.elexis.core.ui.coolbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.UserServiceHolder;
import ch.elexis.core.ui.data.UiMandant;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.data.Mandant;

/**
 * This class implements the {@link Mandant} selection button bar within the
 * application toolbar (coolbar). The list of colors represents the available
 * colors to distinguish the currently selected mandant.
 *
 * @since 3.11 do enable items according to
 *        {@link IUserService#getExecutiveDoctorsWorkingFor()}
 */
public class MandantSelectionContributionItem {

	private ToolItem item;
	private Menu menu;
	private IMandator[] mandants;
	private MenuItem[] menuItems;
	private ToolBar fParent;

	@Inject
	public void activeMandator(@Optional IMandator mandator) {
		if (fParent != null && !fParent.isDisposed()) {
			CoreUiUtil.runAsyncIfActive(() -> {
				CoreModelServiceHolder.get().load(mandator.getId(), IMandator.class).ifPresent(m -> {
					if (item != null) {
						item.setText(m.getLabel());
						fParent.setBackground(UiMandant.getColorForIMandator(m));
						if (menuItems == null) {
							// We have a read-only coolbar item entry
							fParent.pack();
							return;
						}
						for (int i = 0; i < menuItems.length; i++) {
							String id = (String) menuItems[i].getData();
							if (m.getId().equalsIgnoreCase(id)) {
								fParent.pack();
								// TODO: Anordnung Elemente in Coolbar speicherbar?
								// TODO: Programmatische Anordnung Elemente coolbar
								menuItems[i].setSelection(true);
							} else {
								menuItems[i].setSelection(false);
							}
						}
					}
					fParent.getParent().layout();
				});
			}, fParent);
		}
	}


	@Inject
	void activeUser(@Optional IUser user) {
		Display.getDefault().asyncExec(() -> {
			if (item != null && !item.isDisposed()) {
				adaptForUser(user);
			}
		});
	}

	private void adaptForUser(IUser user) {
		if (user == null) {
			user = ContextServiceHolder.get().getActiveUser().orElse(null);
			if (user == null) {
				return;
			}
		}
		List<String> workingForIds = UserServiceHolder.get().getExecutiveDoctorsWorkingFor(user).stream()
				.map(a -> a.getId()).collect(Collectors.toList());

		rebuildMenuItems(workingForIds);
	}

	public MandantSelectionContributionItem() {
	}

	@PostConstruct
	protected Control createControl(Composite parent) {
		ToolBar toolbar = new ToolBar(parent, SWT.NONE);

		// dispose old items first
		disposeItems();
		if (item != null) {
			item.dispose();
		}
		if (menu != null) {
			menu.dispose();
		}

		fParent = toolbar;
		menu = new Menu(fParent);

		List<IMandator> lMandator = CoreModelServiceHolder.get().getQuery(IMandator.class).execute();
		lMandator.sort(new Comparator<IMandator>() {

			@Override
			public int compare(IMandator m1, IMandator m2) {
				return m1.getLabel().compareTo(m2.getLabel());
			}
		});
		mandants = lMandator.toArray(new IMandator[] {});
		if (mandants.length < 2)
			return null;

		item = new ToolItem(toolbar, SWT.DROP_DOWN);
		item.setToolTipText("Aktuell ausgewählter Mandant bzw. Mandantenauswahl");

		menuItems = new MenuItem[mandants.length];

		for (int i = 0; i < mandants.length; i++) {
			final IMandator m = mandants[i];
			menuItems[i] = new MenuItem(menu, SWT.RADIO);
			menuItems[i].setText(m.getLabel());
			menuItems[i].setImage(getBoxSWTColorImage(UiMandant.getColorForIMandator(m)));
			menuItems[i].setData(m.getId());
			menuItems[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ContextServiceHolder.get().setActiveMandator(m);
				}
			});
			if (ContextServiceHolder.getActiveMandatorOrNull() != null) {
				IMandator activeMandator = ContextServiceHolder.getActiveMandatorOrNull();
				menuItems[i].setSelection(activeMandator != null && activeMandator.equals(m));
			}
		}

		item.addListener(SWT.Selection, selectionListener);

		if (ContextServiceHolder.getActiveMandatorOrNull() != null && item != null) {
			item.setText(ContextServiceHolder.getActiveMandatorOrNull().getLabel());
			fParent.setBackground(
					UiMandant.getColorForIMandator(ContextServiceHolder.getActiveMandatorOrNull()));
		}

		adaptForUser(null);

		toolbar.pack();
		return toolbar;
	}

	private void rebuildMenuItems(List<String> workingForIds) {
		if (menuItems != null && menuItems.length > 0) {
			for (int i = 0; i < menuItems.length; i++) {
				if (menuItems[i] != null) {
					menuItems[i].dispose();
				}
			}
		}
		if (menu != null && menu.isDisposed()) {
			menu.dispose();
		}

		menu = new Menu(fParent);
		List<MenuItem> menuItemList = new ArrayList<>();

		for (IMandator m : mandants) {
			if (workingForIds.contains(m.getId())) {
				MenuItem item = buildMenuItem(m);
				menuItemList.add(item);
			}
		}
		menuItems = menuItemList.toArray(new MenuItem[0]);
	}

	private MenuItem buildMenuItem(IMandator m) {
		MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
		menuItem.setText(m.getLabel());
		Image img = getBoxSWTColorImage(UiMandant.getColorForIMandator(m));
		if (img != null && !img.isDisposed()) {
			menuItem.setImage(img);
		}
		menuItem.setData(m.getId());
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContextServiceHolder.get().setActiveMandator(m);
			}
		});

		IMandator activeMandator = ContextServiceHolder.getActiveMandatorOrNull();
		if (activeMandator != null) {
			menuItem.setSelection(activeMandator != null && activeMandator.equals(m));
		}
		return menuItem;
	}

	private final Listener selectionListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			if (event.detail == SWT.ARROW || event.type == SWT.Selection) {
				Rectangle rect = item.getBounds();
				Point pt = new Point(rect.x, rect.y + rect.height);
				pt = fParent.toDisplay(pt);
				menu.setLocation(pt.x, pt.y);
				menu.setVisible(true);
			}
		}
	};

	private void disposeItems() {
		if (menuItems != null && menuItems.length > 0) {
			for (int i = 0; i < menuItems.length; i++) {
				if (menuItems[i] != null) {
					Image img = menuItems[i].getImage();
					if (img != null) {
						img.dispose();
					}
					menuItems[i].dispose();
				}
			}
		}
	}

	public static Image getBoxSWTColorImage(Color color) {
		String colorName = String.valueOf(color.hashCode());
	
		if (JFaceResources.getImageRegistry().get(colorName) == null) {
			Display display = Display.getCurrent();
			Image image = new Image(display, 16, 16);
			GC gc = new GC(image);
			gc.setBackground(color);
			gc.fillRoundRectangle(0, 0, 16, 16, 8, 8);
			gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
			gc.dispose();
			JFaceResources.getImageRegistry().put(colorName, image);
		}
		
		return JFaceResources.getImageRegistry().get(colorName);
	}
}
