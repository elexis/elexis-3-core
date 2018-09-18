package ch.elexis.core.findings.ui.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.ui.cons.KonsExtension;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;

public class AddFindingAction extends Action implements IAction {
	
	private KonsExtension konsExtension;
	
	public AddFindingAction(KonsExtension konsExtension){
		this.konsExtension = konsExtension;
	}
	
	@Override
	public String getText(){
		return "Befund anlegen";
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_NEW.getImageDescriptor();
	}
	
	@Override
	public void run(){
		Object created = FindingsUiUtil
			.executeCommand("ch.elexis.core.findings.templates.ui.command.finding.create", null);
		if (created instanceof IObservation) {
			Konsultation kons =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			if (kons != null) {
				IObservation observation = (IObservation) created;
				if (LocalLockServiceHolder.get().acquireLock(kons).isOk()) {
					kons.addXRef(KonsExtension.EXTENSION_ID,
						((PersistentObject) observation).getId(), -1,
						konsExtension.getXRefText(observation));
					LocalLockServiceHolder.get().releaseLock(kons);
				}
			}
		}
	}
}
