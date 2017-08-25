package ch.elexis.core.ui.views;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.Messages;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.data.Anwender;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.dto.MakroDTO;
import ch.rgw.io.SqlSettings;
import ch.rgw.tools.JdbcLink;

public class MakrosComposite extends Composite {
	
	private TableViewer viewer;
	private MakroDetailComposite detailComposite;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public MakrosComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(1, false));
		
		CLabel lblHeader = new CLabel(this, SWT.NONE);
		lblHeader.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		lblHeader.setText("Makros des Anwender " + CoreHub.actUser.getLabel());

		
		SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite selectionComposite = new Composite(sash, SWT.NONE);
		selectionComposite.setLayout(new GridLayout(1, true));
		ToolBarManager toolbar = new ToolBarManager();
		ToolBar toolbarControl = toolbar.createControl(selectionComposite);
		toolbarControl.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		
		viewer = new TableViewer(selectionComposite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new DefaultLabelProvider());
		viewer.setInput(getUserMakros(CoreHub.actUser));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					detailComposite.setMakro((MakroDTO) selection.getFirstElement());
				} else {
					detailComposite.setMakro(null);
				}
			}
		});
		viewer.setComparator(new ViewerComparator());
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(new RemoveMakroAction(viewer));
		MenuManager subMenu = new MenuManager("Marko zu Anwender kopieren");
		subMenu.setRemoveAllWhenShown(true);
		subMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				addCopyToUserActions(manager);
			}
		});
		menuManager.add(subMenu);
		
		Menu menu = menuManager.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);
		
		toolbar.add(new AddMakroAction(viewer));
		toolbar.add(new RemoveMakroAction(viewer));
		toolbar.add(new RefreshMakrosAction(viewer));
		toolbar.update(true);
		
		detailComposite = new MakroDetailComposite(sash, SWT.NONE);
		
		// can only be set after child components are available
		sash.setWeights(new int[] {
			1, 4
		});
	}
	
	private List<MakroDTO> getUserMakros(Anwender actUser){
		PreparedStatement statement = null;
		try {
			statement = PersistentObject.getDefaultConnection().getPreparedStatement(
				"SELECT Param, Value FROM USERCONFIG WHERE UserID=? AND Param LIKE ?");
			
			statement.setString(1, actUser.getId());
			statement.setString(2, "makros/%");
			
			List<MakroDTO> items = new ArrayList<>();
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// makro names are reversed
				String param = rs.getString(1);
				String name = rs.getString(1).substring(rs.getString(1).indexOf('/') + 1);
				StringBuilder sb = new StringBuilder(name);
				MakroDTO item =
					new MakroDTO(actUser.getId(), param, sb.reverse().toString(), rs.getString(2));
				items.add(item);
			}
			rs.close();
			statement.close();
			return items;
		} catch (SQLException e) {
			LoggerFactory.getLogger(getClass()).error("Could not fetch makros", e);
		} finally {
			if(statement != null) {
				PersistentObject.getDefaultConnection().releasePreparedStatement(statement);
			}
		}
		
		return Collections.emptyList();
	}
	
	private void addCopyToUserActions(IMenuManager manager){
		List<Anwender> users = CoreHub.getUserList();
		for (Anwender anwender : users) {
			if (anwender.equals(CoreHub.actUser)) {
				continue;
			}
			manager.add(new CopyToUserAction(anwender, viewer));
		}
	}
	
	private class AddMakroAction extends Action {
		private StructuredViewer viewer;
		
		public AddMakroAction(StructuredViewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public void run(){
			InputDialog in =
				new InputDialog(getShell(), Messages.EnhancedTextField_newMacro,
				Messages.EnhancedTextField_enterNameforMacro, null, null);
			if (in.open() == Dialog.OK) {
				StringBuilder name = new StringBuilder(in.getValue());
				name.reverse();
				MakroDetailComposite.saveMakro(new MakroDTO(CoreHub.actUser.getId(),
					"makros/" + name.toString(), in.getValue(), "Neues Makro"));
				if (viewer != null) {
					viewer.setInput(getUserMakros(CoreHub.actUser));
				}
			}
		}
	}
	
	private class RemoveMakroAction extends Action {
		private StructuredViewer viewer;
		
		public RemoveMakroAction(StructuredViewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "löschen";
		}
		
		@Override
		public void run(){
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			if(selection  != null && !selection.isEmpty()) {
				if (MessageDialog.openConfirm(getShell(), "Makros löschen",
					"Möchten Sie die Makros wirklich löschen?")) {
					for (Object obj : selection.toList()) {
						if (obj instanceof MakroDTO) {
							MakroDetailComposite.removeMakro((MakroDTO) obj);
						}
					}
					if (viewer != null) {
						viewer.setInput(getUserMakros(CoreHub.actUser));
					}
				}
			}
		}
	}
	
	private class RefreshMakrosAction extends Action {
		private StructuredViewer viewer;
		
		public RefreshMakrosAction(StructuredViewer viewer){
			this.viewer = viewer;
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_REFRESH.getImageDescriptor();
		}
		
		@Override
		public void run(){
			if (viewer != null) {
				viewer.setInput(getUserMakros(CoreHub.actUser));
			}
		}
	}
	
	private class CopyToUserAction extends Action {
		private StructuredViewer viewer;
		private Anwender user;
		
		public CopyToUserAction(Anwender anwender, StructuredViewer viewer){
			this.viewer = viewer;
			this.user = anwender;
		}
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_USER_SILHOUETTE.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "zu " + user.getLabel() + " kopieren";
		}
		
		@Override
		public void run(){
			StructuredSelection selection = (StructuredSelection) viewer.getSelection();
			if (selection != null && !selection.isEmpty()) {
				for (Object obj : selection.toList()) {
					if (obj instanceof MakroDTO) {
						MakroDTO makro = (MakroDTO) obj;
						if (copyExists(makro)) {
							if (MessageDialog.openConfirm(getShell(), "Makro kopieren",
								"Das Makro " + makro.getMakroName() + " existiert bei "
									+ user.getLabel()
									+ " bereits. Wollen Sie das Makro überschreiben?")) {
								copy(makro);
							}
						} else {
							copy(makro);
						}
					}
				}
			}
		}
		
		private void copy(MakroDTO makro){
			MakroDTO copy = new MakroDTO(user.getId(), makro.getMakroParam(), makro.getMakroName(),
				makro.getMakroContent());
			MakroDetailComposite.saveMakro(copy);
		}
		
		private boolean copyExists(MakroDTO makro){
			SqlSettings userSettings =
				new SqlSettings(PersistentObject.getDefaultConnection().getJdbcLink(), "USERCONFIG",
					"Param", "Value", "UserID=" + JdbcLink.wrap(user.getId()));
			
			return userSettings.get(makro.getMakroParam(), null) != null;
		}
	}
}
