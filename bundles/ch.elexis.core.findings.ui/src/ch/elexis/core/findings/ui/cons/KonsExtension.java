package ch.elexis.core.findings.ui.cons;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.ui.action.AddFindingAction;
import ch.elexis.core.findings.ui.handler.FindingEditHandler;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.EncounterServiceHolder;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.text.model.Samdas.XRef;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.text.IRichTextDisplay;
import ch.elexis.core.ui.util.IKonsExtension;

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
	
	public IRichTextDisplay getRichTextDisplay(){
		return mine;
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
			FindingsUiUtil.executeCommand(FindingEditHandler.COMMAND_ID, obs);
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
			final IObservation observation = (IObservation) o;
			final Optional<IEncounter> encounterOpt =
				ContextServiceHolder.get().getTyped(IEncounter.class);
			
			encounterOpt.ifPresent(encounter -> {
				mine.insertXRef(pos, getXRefText(observation), EXTENSION_ID,
					((Identifiable) observation).getId());
				Samdas samdas = new Samdas(mine.getContentsAsXML());
				EncounterServiceHolder.get().updateVersionedEntry(encounter, samdas);
				CoreModelServiceHolder.get().save(encounter);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, encounter);
			});
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
