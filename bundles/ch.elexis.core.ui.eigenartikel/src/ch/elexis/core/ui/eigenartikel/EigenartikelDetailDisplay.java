package ch.elexis.core.ui.eigenartikel;

import java.text.MessageFormat;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IViewSite;

import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockRequestingRestrictedAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.views.IDetailDisplay;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class EigenartikelDetailDisplay implements IDetailDisplay {
	private IViewSite site;

	private EigenartikelProductComposite epc;
	private EigenartikelComposite ec;
	private IArticle selectedObject;
	private IArticle currentLock;
	private StackLayout layout;
	private Composite container;
	private Composite compProduct;
	private Composite compArticle;

	private RestrictedAction createAction = new RestrictedAction(new ObjectEvaluatableACE(IArticle.class, Right.CREATE),
			ch.elexis.core.ui.views.artikel.Messages.Core_New_ellipsis) {
		{

			setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			setToolTipText(ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_createProductToolTipText);
		}

		@Override
		public void doRun() {
			IArticle ea = CoreModelServiceHolder.get().create(IArticle.class);
			ea.setTyp(ArticleTyp.EIGENARTIKEL);
			ea.setName("Neues Produkt");
			CoreModelServiceHolder.get().save(ea);
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IArticle.class);
			selection(ea);
		}
	};

	private RestrictedAction toggleLockAction = new RestrictedAction(
			new ObjectEvaluatableACE(IArticle.class, Right.UPDATE), "lock", //$NON-NLS-1$
			SWT.TOGGLE) {
		{
			setImageDescriptor(Images.IMG_LOCK_CLOSED.getImageDescriptor());
		}

		@Override
		public void setChecked(boolean checked) {
			if (checked) {
				setImageDescriptor(Images.IMG_LOCK_OPEN.getImageDescriptor());
			} else {

				setImageDescriptor(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			}
			super.setChecked(checked);
		}

		@Override
		public void doRun() {
			if (selectedObject != null) {
				if (LocalLockServiceHolder.get().isLocked(selectedObject)) {
					LocalLockServiceHolder.get().releaseLock(selectedObject);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IArticle.class);
					currentLock = null;
				} else {
					LockResponse lr = LocalLockServiceHolder.get().acquireLock(selectedObject);
					if (lr.isOk()) {
						currentLock = selectedObject;
					} else {
						LockResponseHelper.showInfo(lr, selectedObject, null);
					}
				}
			}
			setChecked(LocalLockServiceHolder.get().isLocked(currentLock));
		}
	};

	private RestrictedAction deleteAction = new LockRequestingRestrictedAction<IArticle>(
			new ObjectEvaluatableACE(IArticle.class, Right.UPDATE),
			ch.elexis.core.ui.views.artikel.Messages.Core_Delete) {
		{
			setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
			setToolTipText(ch.elexis.core.ui.views.artikel.Messages.ArtikelContextMenu_deleteProductToolTipText);
		}

		@Override
		public IArticle getTargetedObject() {
			java.util.Optional<?> selected = ContextServiceHolder.get().getRootContext()
					.getNamed("ch.elexis.core.ui.eigenartikel.selection"); //$NON-NLS-1$
			return (IArticle) selected.orElse(null);
		}

		@Override
		public void doRun(IArticle act) {
			if (MessageDialog.openConfirm(site.getShell(), ch.elexis.core.ui.views.artikel.Messages.Core_Confirm_delete,
					MessageFormat.format(ch.elexis.core.ui.views.artikel.Messages.Core_Want_to_delete_0,
							act.getName()))) {
				CoreModelServiceHolder.get().delete(act);

				if (epc != null) {
					epc.setProductEigenartikel(null);
				}
			}
			ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD, IArticle.class);
		}
	};

	@Inject
	@Optional
	public void lockAquired(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_AQUIRED) IArticle typedArticle) {
		if (epc != null && !epc.isDisposed() && typedArticle.getId().equals(selectedObject.getId())) {
			epc.setUnlocked(true);
		}
	}

	@Inject
	@Optional
	public void lockReleased(@UIEventTopic(ElexisEventTopics.EVENT_LOCK_RELEASED) IArticle typedArticle) {
		if (epc != null && !epc.isDisposed() && typedArticle.getId().equals(selectedObject.getId())) {
			epc.setUnlocked(false);
		}
	}

	@Inject
	public void selection(@Optional @Named("ch.elexis.core.ui.eigenartikel.selection") IArticle typedArticle) {
		if (epc != null && !epc.isDisposed()) {
			display(typedArticle);
		}
	}

	/**
	 * @wbp.parser.entryPoint
	 */
	@Override
	public Composite createDisplay(Composite parent, IViewSite site) {
		this.site = site;

		container = new Composite(parent, SWT.NONE);
		// parent.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new StackLayout();
		container.setLayout(layout);

		compProduct = new Composite(container, SWT.None);
		compProduct.setLayout(new GridLayout(1, false));

		ToolBar toolBar = new ToolBar(compProduct, SWT.BORDER | SWT.FLAT | SWT.RIGHT);
		toolBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		final ToolBarManager manager = new ToolBarManager(toolBar);
		manager.add(createAction);
		if (ElexisServerServiceHolder.get().getConnectionStatus() != ConnectionStatus.STANDALONE) {
			manager.add(toggleLockAction);
		}
		manager.add(deleteAction);
		manager.update(true);
		toolBar.pack();

		epc = new EigenartikelProductComposite(compProduct, SWT.None);
		epc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		epc.setUnlocked(ElexisServerServiceHolder.get().getConnectionStatus() == ConnectionStatus.STANDALONE);

		compArticle = new Composite(container, SWT.None);
		compArticle.setLayout(new GridLayout(1, false));

		ec = new EigenartikelComposite(compArticle, SWT.None, false, null);
		ec.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		ec.setUnlocked(ElexisServerServiceHolder.get().getConnectionStatus() == ConnectionStatus.STANDALONE);

		layout.topControl = compProduct;
		container.layout();

		return container;
	}

	@Override
	public Class<?> getElementClass() {
		return IArticle.class;
	}

	@Override
	public void display(Object obj) {
		toggleLockAction.reflectRight();
		createAction.reflectRight();
		deleteAction.reflectRight();

		if (obj instanceof IArticle) {
			selectedObject = (IArticle) obj;
			if (currentLock != null) {
				LocalLockServiceHolder.get().releaseLock(currentLock);
				toggleLockAction.setChecked(false);
				currentLock = null;
			}
			IArticle article = (IArticle) obj;
			toggleLockAction.setEnabled(article.isProduct());

			if (article.isProduct()) {
				layout.topControl = compProduct;
				epc.setProductEigenartikel(article);
			} else {
				layout.topControl = compArticle;
				ec.setEigenartikel(article);
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
	public String getTitle() {
		return Messages.EigenartikelDisplay_displayTitle;
	}
}
