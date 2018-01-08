package ch.elexis.core.ui.eigenartikel;

import java.text.MessageFormat;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewSite;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.eigenartikel.acl.ACLContributor;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockRequestingRestrictedAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.views.IDetailDisplay;
import ch.elexis.data.PersistentObject;

public class EigenartikelDetailDisplay implements IDetailDisplay {
	private IViewSite site;
	
	private EigenartikelProductComposite epc;
	private EigenartikelComposite ec;
	private Eigenartikel selectedObject;
	private Eigenartikel currentLock;
	private StackLayout layout;
	private Composite container;
	private Composite compProduct;
	private Composite compArticle;
	
	private RestrictedAction createAction = new RestrictedAction(ACLContributor.EIGENARTIKEL_MODIFY,
		ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_newAction) {
		{
			setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			setToolTipText(
				ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_createProductToolTipText);
		}
		
		@Override
		public void doRun(){
			Eigenartikel ea = new Eigenartikel("New Product", StringConstants.EMPTY);
			ElexisEventDispatcher.reload(Eigenartikel.class);
			ElexisEventDispatcher.fireSelectionEvent(ea);
		}
	};
	
	private RestrictedAction toggleLockAction =
		new RestrictedAction(ACLContributor.EIGENARTIKEL_MODIFY, "lock", SWT.TOGGLE) {
			{
				setImageDescriptor(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			}
			
			@Override
			public void setChecked(boolean checked){
				if (checked) {
					setImageDescriptor(Images.IMG_LOCK_OPEN.getImageDescriptor());
				} else {
					
					setImageDescriptor(Images.IMG_LOCK_CLOSED.getImageDescriptor());
				}
				super.setChecked(checked);
			}
			
			@Override
			public void doRun(){
				if (selectedObject != null) {
					if (CoreHub.getLocalLockService().isLocked(selectedObject)) {
						CoreHub.getLocalLockService().releaseLock(selectedObject);
						ElexisEventDispatcher.reload(Eigenartikel.class);
						currentLock = null;
					} else {
						LockResponse lr = CoreHub.getLocalLockService().acquireLock(selectedObject);
						if (lr.isOk()) {
							currentLock = selectedObject;
						} else {
							LockResponseHelper.showInfo(lr, selectedObject, null);
						}
					}
				}
				setChecked(CoreHub.getLocalLockService().isLocked(currentLock));
			}
		};
	
	private RestrictedAction deleteAction =
		new LockRequestingRestrictedAction<Eigenartikel>(ACLContributor.EIGENARTIKEL_MODIFY,
			ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_deleteAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(
					ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_deleteProductToolTipText);
			}
			
			@Override
			public Eigenartikel getTargetedObject(){
				return (Eigenartikel) ElexisEventDispatcher.getSelected(Eigenartikel.class);
			}
			
			@Override
			public void doRun(Eigenartikel act){
				if (MessageDialog.openConfirm(site.getShell(),
					ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_deleteActionConfirmCaption,
					MessageFormat.format(
						ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_deleteConfirmBody,
						act.getName()))) {
					act.delete();
					
					if (epc != null) {
						epc.setProductEigenartikel(null);
					}
				}
				ElexisEventDispatcher.reload(Eigenartikel.class);
			}
		};
	
	private ElexisEventListener eeli_egartikel = new ElexisUiEventListenerImpl(Eigenartikel.class) {
		public void runInUi(ElexisEvent ev){
			Eigenartikel egArtikel = (Eigenartikel) ev.getObject();
			switch (ev.getType()) {
			case ElexisEvent.EVENT_LOCK_AQUIRED:
			case ElexisEvent.EVENT_LOCK_RELEASED:
				if (egArtikel != null && selectedObject != null
					&& egArtikel.getId().equals(selectedObject.getId())
					&& ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED) {
					epc.setUnlocked(true);
				} else {
					epc.setUnlocked(false);
				}
				break;
			default:
				break;
			}
		}
	};
	
	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public Composite createDisplay(Composite parent, IViewSite site){
		this.site = site;
		
		container = new Composite(parent, SWT.NONE);
 		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
 		layout = new StackLayout();
 		container.setLayout(layout);
		
		compProduct = new Composite(container, SWT.None);		
		compProduct.setLayout(new GridLayout(1, false));
		
		ToolBar toolBar = new ToolBar(compProduct, SWT.BORDER | SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		final ToolBarManager manager = new ToolBarManager(toolBar);
		manager.add(createAction);
		if (CoreHub.getLocalLockService().getStatus() != Status.STANDALONE) {
			manager.add(toggleLockAction);
		}
		manager.add(deleteAction);
		manager.update(true);
		toolBar.pack();
		
		epc = new EigenartikelProductComposite(compProduct, SWT.None);
		epc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		epc.setUnlocked(CoreHub.getLocalLockService().getStatus() == Status.STANDALONE);
		
		if (CoreHub.getLocalLockService().getStatus() != Status.STANDALONE) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_egartikel);
		}
		
		compArticle = new Composite(container, SWT.None);		
		compArticle.setLayout(new GridLayout(1, false));
		
		ec = new EigenartikelComposite(compArticle, SWT.None, false, null);
		ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		ec.setUnlocked(false);
		
		layout.topControl = compProduct;
		container.layout();
		
		return container;
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	@Override
	public void display(Object obj){
		toggleLockAction.reflectRight();
		createAction.reflectRight();
		deleteAction.reflectRight();
		
		if (obj instanceof Eigenartikel) {
			selectedObject = (Eigenartikel) obj;
			if (currentLock != null) {
				CoreHub.getLocalLockService().releaseLock(currentLock);
				toggleLockAction.setChecked(false);
				currentLock = null;
			}
			Eigenartikel ea = (Eigenartikel) obj;
			toggleLockAction.setEnabled(ea.isProduct());
			
			if(ea.isProduct()) {
				layout.topControl = compProduct;
				epc.setProductEigenartikel(ea);
			} else {
				layout.topControl = compArticle;
				ec.setEigenartikel(ea);
			}
			
		} else {
			selectedObject = null;
			toggleLockAction.setEnabled(false);
			epc.setProductEigenartikel(null);
			ec.setEigenartikel(null);
			layout.topControl = compProduct;
		}
		
		container.layout();
	}
	
	@Override
	public String getTitle(){
		return Messages.EigenartikelDisplay_displayTitle;
	}
	
	@Override
	protected void finalize() throws Throwable{
		if (CoreHub.getLocalLockService().getStatus() != Status.STANDALONE) {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_egartikel);
		}
		
		super.finalize();
	}
}
