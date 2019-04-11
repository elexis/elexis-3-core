package ch.elexis.core.ui.importer.div.importers;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.importer.div.importers.IContactResolver;
import ch.elexis.core.importer.div.importers.Messages;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;

public class DefaultLinkLabContactResolver implements IContactResolver<ILaboratory> {
	
	private String identifier;
	
	@Override
	public ILaboratory getContact(String message){
		KontaktSelektor ks =
			new KontaktSelektor(Display.getDefault().getActiveShell(), Labor.class,
				Messages.LabImporterUtil_Select,
				message, Kontakt.DEFAULT_SORT);
		if (ks.open() == Dialog.OK) {
			Labor labor = (Labor) ks.getSelection();
			labor.addXid(XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY, identifier, true);
			return CoreModelServiceHolder.get().load(labor.getId(), ILaboratory.class).orElse(null);
		}
		return null;
	}
	
	public IContactResolver<ILaboratory> identifier(String identifier){
		this.identifier = identifier;
		return this;
	}
	
}
