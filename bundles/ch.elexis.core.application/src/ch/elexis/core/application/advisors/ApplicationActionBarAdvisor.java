/*******************************************************************************

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.application.advisors;

import static ch.elexis.core.ui.actions.GlobalActions.fixLayoutAction;
import static ch.elexis.core.ui.actions.GlobalActions.perspectiveMenu;
import static ch.elexis.core.ui.actions.GlobalActions.resetPerspectiveAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import ch.elexis.core.ac.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.base.InputDialog;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

/**
 * Konstruktion der globalen Aktionen (Menu, Toolbar etc.)
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	public static final String IMPORTER_GROUP = "elexis.FileImports"; //$NON-NLS-1$
	public static final String ADDITIONS = "elexis.fileAdditions"; //$NON-NLS-1$
	
	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	
	private IWorkbenchWindow window;
	private IAction[] openPerspectiveActions = null;
	public static MenuManager fileMenu, editMenu, windowMenu, helpMenu;
	
	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer){
		super(configurer);
	}
	
	@Override
	protected void makeActions(final IWorkbenchWindow win){
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.
		window = win;
		Hub.mainActions = new GlobalActions(window);
		register(GlobalActions.exitAction);
		// register(GlobalActions.updateAction);
		/*
		 * register(GlobalActions.newWindowAction); register(GlobalActions.copyAction);
		 * register(GlobalActions.cutAction); register(GlobalActions.pasteAction);
		 * register(GlobalActions.loginAction); register(GlobalActions.importAction);
		 * register(GlobalActions.aboutAction); register(GlobalActions.helpAction);
		 * register(GlobalActions.prefsAction); register(GlobalActions.connectWizardAction);
		 */
		// register(GlobalActions.changeMandantAction);
		// register(GlobalActions.savePerspectiveAction);
		// register(GlobalActions.savePerspectiveAsAction);
		// register(GlobalActions.resetPerspectiveAction);
		// register(savePerspectiveAsDefaultAction);
		// register(MainMenuActions.showViewAction);
		// register(MainMenuActions.showPerspectiveAction);
		
		// create open perspective actions according to the list of Sidebar
		if (CoreHub.localCfg.get(Preferences.SHOWTOOLBARITEMS, Boolean.toString(true))
			.equalsIgnoreCase(Boolean.toString(true))) {
			List<IConfigurationElement> ex =
				Extensions.getExtensions(ExtensionPointConstantsUi.SIDEBAR);
			openPerspectiveActions = new IAction[ex.size()];
			int i = 0;
			for (IConfigurationElement ice : ex) {
				String name = ice.getAttribute("name"); //$NON-NLS-1$
				String id = ice.getAttribute("ID"); //$NON-NLS-1$
				String icon = ice.getAttribute("icon"); //$NON-NLS-1$
				IPerspectiveDescriptor perspectiveDescriptor =
					PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(id);
				if (perspectiveDescriptor != null) {
					openPerspectiveActions[i] =
						new OpenPerspectiveAction(perspectiveDescriptor, name, icon);
				}
				
				i++;
			}
		}
		
	}
	
	private final IMenuListener reflectRightsListener = new IMenuListener() {
		
		@Override
		public void menuAboutToShow(IMenuManager manager){
			IContributionItem[] items = manager.getItems();
			for (IContributionItem iContributionItem : items) {
				if(iContributionItem instanceof ActionContributionItem) {
					ActionContributionItem aci = (ActionContributionItem) iContributionItem;		
					IAction action = aci.getAction();
					if(action instanceof RestrictedAction) {
						RestrictedAction ra = (RestrictedAction) aci.getAction();
						ra.reflectRight();
						continue;
					}
					String id = action.getActionDefinitionId();
					if(id==null) {
						continue;
					}
					IAccessControlService acl = AccessControlServiceHolder.get();
					switch (id) {
					case IWorkbenchCommandConstants.FILE_EXIT:
						action.setEnabled(acl.request(AccessControlDefaults.AC_EXIT));
						break;
					case IWorkbenchCommandConstants.WINDOW_NEW_WINDOW:
						action.setEnabled(acl.request(AccessControlDefaults.AC_NEWWINDOW));
						break;
					case IWorkbenchCommandConstants.HELP_ABOUT:
						action.setEnabled(acl.request(AccessControlDefaults.AC_ABOUT));
						break;
					case IWorkbenchCommandConstants.WINDOW_PREFERENCES:
						action.setEnabled(acl.request(AccessControlDefaults.AC_PREFS));
						break;
					default:
						break;
					}
				}
			}
		}
	};
	
	@Override
	protected void fillMenuBar(IMenuManager menuBar){
		
		fileMenu =
			new MenuManager(Messages.ApplicationActionBarAdvisor_3,
				IWorkbenchActionConstants.M_FILE);
		fileMenu.addMenuListener(reflectRightsListener);
		editMenu =
			new MenuManager(Messages.ApplicationActionBarAdvisor_4,
				IWorkbenchActionConstants.M_EDIT);
		editMenu.addMenuListener(reflectRightsListener);
		windowMenu =
			new MenuManager(Messages.ApplicationActionBarAdvisor_5,
				IWorkbenchActionConstants.M_WINDOW);
		helpMenu =
			new MenuManager(Messages.ApplicationActionBarAdvisor_6,
				IWorkbenchActionConstants.M_HELP);
		helpMenu.addMenuListener(reflectRightsListener);
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(GlobalActions.loginAction);
		fileMenu.add(GlobalActions.changeMandantAction);
		fileMenu.add(GlobalActions.connectWizardAction);
		fileMenu.add(GlobalActions.prefsAction);
		fileMenu.add(new Separator());
		fileMenu.add(GlobalActions.importAction);
		fileMenu.add(new GroupMarker(IMPORTER_GROUP));
		fileMenu.add(new Separator());
		// fileMenu.add(GlobalActions.updateAction);
		fileMenu.add(new GroupMarker(ADDITIONS));
		fileMenu.add(new Separator());
		fileMenu.add(GlobalActions.exitAction);
		
		editMenu.add(GlobalActions.copyAction);
		editMenu.add(GlobalActions.cutAction);
		editMenu.add(GlobalActions.pasteAction);

		
		GlobalActions.perspectiveMenu =
			new MenuManager(Messages.ApplicationActionBarAdvisor_7, "openPerspective"); //$NON-NLS-1$
		perspectiveMenu.add(resetPerspectiveAction);
		perspectiveMenu.add(fixLayoutAction);
		windowMenu.add(perspectiveMenu);
		
		GlobalActions.viewMenu = new MenuManager(Messages.ApplicationActionBarAdvisor_9);
		GlobalActions.viewList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		GlobalActions.viewMenu.add(GlobalActions.viewList);
		windowMenu.add(GlobalActions.viewMenu);
		windowMenu.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				IContributionItem[] items = manager.getItems();
				for (IContributionItem iContributionItem : items) {
					if( "viewsShortlist".equals(iContributionItem.getId())) {
						iContributionItem.setVisible(AccessControlServiceHolder.get().request(AccessControlDefaults.AC_SHOWVIEW));
					}
				}
			}
		});
		
		/* helpMenu.add(testAction); */
		helpMenu.add(GlobalActions.helpAction);
		helpMenu.add(new Separator("additions"));
		helpMenu.add(new Separator());
		helpMenu.add(GlobalActions.aboutAction);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.application.ActionBarAdvisor#fillCoolBar(org.eclipse.jface
	 * .action.ICoolBarManager )
	 */
	@Override
	protected void fillCoolBar(ICoolBarManager coolBar){
		ToolBarManager tbm = new ToolBarManager();
		
		tbm.add(GlobalActions.homeAction);
		tbm.add(GlobalActions.resetPerspectiveAction);
		
		tbm.add(new Separator());
		tbm.add(GlobalActions.printEtikette);
		tbm.add(new Action("", Action.AS_DROP_DOWN_MENU) {
			
			private IMenuCreator menuCreator;
			
			@Override
			public ImageDescriptor getImageDescriptor(){
				return GlobalActions.printVersionedEtikette.getImageDescriptor();
			}
			
			@Override
			public String getText(){
				return GlobalActions.printVersionedEtikette.getText();
			}
			
			@Override
			public String getToolTipText(){
				return GlobalActions.printVersionedEtikette.getToolTipText();
			}
			
			@Override
			public IMenuCreator getMenuCreator(){
				if(menuCreator == null) {
					menuCreator = new IMenuCreator() {
						
						private Menu menu;
						
						@Override
						public Menu getMenu(Menu parent){
							// TODO Auto-generated method stub
							return null;
						}
						
						@Override
						public Menu getMenu(Control parent){
							if (menu == null) {
								menu = new Menu(parent);
								final MenuItem menuItem = new MenuItem(this.menu, SWT.PUSH);
								final Image image =
									GlobalActions.printVersionedEtikette.getImageDescriptor()
										.createImage();
								menuItem.setImage(image);
								menuItem.setText(
									"Mehrfach " + GlobalActions.printVersionedEtikette.getText());
								
								menuItem.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e){
										InputDialog inputDlg = new InputDialog(
											Display.getDefault().getActiveShell(), menuItem.getText(),
											"Bitte die Anzahl eingeben", "1", new IInputValidator() {
											@Override
											public String isValid(String newText){
													try {
														Integer.parseInt(newText);
													} catch (NumberFormatException e) {
														return newText + " ist keine gültige Anzahl";
													}
												return null;
											}
										}, SWT.BORDER);
										if (inputDlg.open() == Window.OK) {
											String amountStr = inputDlg.getValue();
											int amount = Integer.parseInt(amountStr);
											while (amount > 0) {
												GlobalActions.printVersionedEtikette.run();
												amount--;
											}
										}
									}
								});
								
							}
							return menu;
						}
						
						@Override
						public void dispose(){
							menu.dispose();
							menu = null;
						}
					}; 
				}
				return menuCreator;
			}
			
			@Override
			public void run(){
				GlobalActions.printVersionedEtikette.run();
			}
		});
		tbm.add(GlobalActions.printAdresse);
		
		if (CoreHub.localCfg.get(Preferences.SHOWTOOLBARITEMS, Boolean.toString(true))
			.equalsIgnoreCase(Boolean.toString(true))) {
			
			List<IAction> l = new ArrayList<>();
			for (IAction action : openPerspectiveActions) {
				if (action != null) {
					l.add(action);
				}
			}
			Collections.sort(l, new Comparator<IAction>() {
				@Override
				public int compare(IAction o1, IAction o2){
					if (o1.getToolTipText() != null && o2.getToolTipText() != null) {
						return o1.getToolTipText().compareTo(o2.getToolTipText());
					}
					return o1.getToolTipText() != null ? 1 : -1;
				}
			});
			
			if(!l.isEmpty()) {
				tbm.add(new Separator());
			}
			
			for (IAction action : l) {
				tbm.add(action);
			}
		}
		
		coolBar.add(tbm);
	}
	
	/**
	 * Action for opening a perspective
	 * 
	 * @author danlutz
	 */
	class OpenPerspectiveAction extends Action {
		private final IPerspectiveDescriptor perspectiveDescriptor;
		
		/**
		 * Create a new action for opening a perspective
		 * 
		 * @param perspectiveDescriptor
		 *            the perspective to be opened
		 */
		OpenPerspectiveAction(IPerspectiveDescriptor perspectiveDescriptor, String name, String icon){
			super(perspectiveDescriptor.getLabel());
			
			setId(perspectiveDescriptor.getId());
			if (!StringTool.isNothing(icon)) {
				setImageDescriptor(perspectiveDescriptor.getImageDescriptor());
			} else {
				
				setImageDescriptor(perspectiveDescriptor.getImageDescriptor());
			}
			
			setToolTipText((StringTool.isNothing(name) ? perspectiveDescriptor.getLabel() : name)
					+ StringTool.space + Messages.ApplicationActionBarAdvisor_10);
			
			this.perspectiveDescriptor = perspectiveDescriptor;
		}
		
		@Override
		public void run(){
			try {
				IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				PlatformUI.getWorkbench().showPerspective(perspectiveDescriptor.getId(), win);
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
		}
	}
}
