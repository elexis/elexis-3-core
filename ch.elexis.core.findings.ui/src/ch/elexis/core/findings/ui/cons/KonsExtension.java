package ch.elexis.core.findings.ui.cons;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.ui.action.AddFindingAction;
import ch.elexis.core.findings.ui.handler.FindingEditHandler;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;

public class KonsExtension implements IKonsExtension {
	IRichTextDisplay mine;
	
	public static final String EXTENSION_ID = "ch.elexis.core.findings.ui.cons";
	private static final String FINDINGS_TITLE = "Befund: ";
	
	@Override
	public String connect(IRichTextDisplay tf){
		mine = tf;
		mine.addDropReceiver(IObservation.class, this);
		return EXTENSION_ID;
	}
	
	@Override
	public boolean doLayout(StyleRange styleRange, String provider, String id){
		styleRange.background = UiDesk.getColor(UiDesk.COL_LIGHTGREY);
		return true;
	}
	
	@Override
	public boolean doXRef(String refProvider, String refID){
		Optional<IObservation> observation =
			FindingsServiceComponent.getService().findById(refID, IObservation.class);
		observation.ifPresent(obs -> {
			// open edit dialog
			Boolean ret =
				(Boolean) FindingsUiUtil.executeCommand(FindingEditHandler.COMMAND_ID, obs);
			ElexisEventDispatcher.fireSelectionEvent((PersistentObject) obs);
		});
		return true;
	}
	
	@Override
	public String updateXRef(String provider, String id){
		Optional<IObservation> observation =
			FindingsServiceComponent.getService().findById(id, IObservation.class);
		if (observation.isPresent()) {
			return getXRefText(observation.get());
		}
		return null;
	}
	
	public void updateXRef(XRef xref){
		if (mine instanceof EnhancedTextField) {
			((EnhancedTextField) mine).updateXRef(xref);
		}
	}
	
	@Override
	public void insert(Object o, int pos){
		if (o instanceof IObservation) {
			IObservation observation = (IObservation) o;
			final Konsultation k =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			
			mine.insertXRef(pos, getXRefText(observation),
				EXTENSION_ID, ((PersistentObject) observation).getId());
			k.updateEintrag(mine.getContentsAsXML(), false);
			ElexisEventDispatcher.update(k);
		}
	}
	
	public String getXRefText(IFinding finding){
		return FINDINGS_TITLE + finding.getText().orElse("?");
	}
	
	@Override
	public IAction[] getActions(){
		IAction[] ret = new IAction[] {
			new AddFindingAction(this)
		};
		return ret;
	}
	
	@Override
	public void removeXRef(String refProvider, String refID){}
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName,
		Object data) throws CoreException{}
}
