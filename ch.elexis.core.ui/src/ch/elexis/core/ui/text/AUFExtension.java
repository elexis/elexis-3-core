package ch.elexis.core.ui.text;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.ui.PartInitException;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.views.AUF2;
import ch.elexis.data.AUF;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.ExHandler;

public class AUFExtension implements IKonsExtension {
	private IRichTextDisplay tx;
	
	@Override
	public String connect(IRichTextDisplay tf){
		tx = tf;
		tx.addDropReceiver(AUF.class, this);
		return XRefExtensionConstants.providerAUFID;
	}
	
	public boolean doLayout(StyleRange n, String provider, String id){
		
		n.background = UiDesk.getColor(UiDesk.COL_LIGHTBLUE);
		n.foreground = UiDesk.getColor(UiDesk.COL_GREY20);
		return true;
	}
	
	public boolean doXRef(String refProvider, String refID){
		AUF auf = AUF.load(refID);
		if (auf != null && auf.exists()) {
			//			new EditAUFDialog(Hub.getActiveShell(), auf, auf.getFall()).open();
			try {
				AUF2 aufView = (AUF2) Hub.plugin.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(AUF2.ID);
			} catch (PartInitException e) {
				ExHandler.handle(e);
			}
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void insert(Object o, int pos){
		if (o instanceof AUF) {
			AUF auf = (AUF) o;
			final Konsultation k =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			
			tx.insertXRef(pos, "AUF: " + auf.getLabel(),
				XRefExtensionConstants.providerAUFID, auf.getId());
			k.updateEintrag(tx.getContentsAsXML(), false);
			ElexisEventDispatcher.update(k);
		}
	}
	
	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName,
		Object data) throws CoreException{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public IAction[] getActions(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void removeXRef(String refProvider, String refID){
		// TODO Auto-generated method stub
		
	}
	
}
