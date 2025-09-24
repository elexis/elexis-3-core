package ch.elexis.core.ui.text;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.PartInitException;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.services.EncounterServiceHolder;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.AUF2;
import ch.rgw.tools.ExHandler;

public class AUFExtension implements IKonsExtension {
	private IRichTextDisplay tx;

	@Override
	public String connect(IRichTextDisplay tf) {
		tx = tf;
		tx.addDropReceiver(ISickCertificate.class, this);
		return XRefExtensionConstants.providerAUFID;
	}

	@Override
	public boolean doLayout(StyleRange n, String provider, String id) {

		n.background = UiDesk.getColor(UiDesk.COL_LIGHTBLUE);
		n.foreground = UiDesk.getColor(UiDesk.COL_GREY20);
		return true;
	}

	@Override
	public boolean doXRef(String refProvider, String refID) {
		Optional<ISickCertificate> loaded = CoreModelServiceHolder.get().load(refID, ISickCertificate.class);
		if (loaded.isPresent()) {
			// new EditAUFDialog(Hub.getActiveShell(), auf, auf.getFall()).open();
			try {
				AUF2 aufView = (AUF2) Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(AUF2.ID);
			} catch (PartInitException e) {
				ExHandler.handle(e);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void insert(Object o, int pos) {
		if (o instanceof ISickCertificate) {
			ISickCertificate auf = (ISickCertificate) o;
			ContextServiceHolder.get().getTyped(IEncounter.class).ifPresent(e -> {
				tx.insertXRef(pos, "AUF: " + auf.getLabel(), XRefExtensionConstants.providerAUFID, auf.getId()); //$NON-NLS-1$
				EncounterServiceHolder.get().updateVersionedEntry(e, new Samdas(tx.getContentsAsXML()));

				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, e);
			});
		}
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public IAction[] getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeXRef(String refProvider, String refID) {
		// TODO Auto-generated method stub

	}

}
