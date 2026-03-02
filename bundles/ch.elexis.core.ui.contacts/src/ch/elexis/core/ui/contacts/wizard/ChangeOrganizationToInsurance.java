package ch.elexis.core.ui.contacts.wizard;

import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ChangeOrganizationToInsurance implements Runnable {

	private IOrganization organization;
	private IOrganization insurance;

	private static final String UPDATE_GUARANTORID = "update faelle f set f.GarantID = '[NEWID]' where f.GarantID = '[OLDID]'";
	private static final String UPDATE_COSTBEARERID = "update faelle f set f.KostentrID = '[NEWID]' where f.KostentrID = '[OLDID]'";

	public ChangeOrganizationToInsurance(IOrganization organization, IOrganization insurance) {
		this.organization = organization;
		this.insurance = insurance;
	}

	@Override
	public void run() {
		String sql = UPDATE_GUARANTORID.replace("[NEWID]", insurance.getId()).replace("[OLDID]", organization.getId());
		LoggerFactory.getLogger(getClass()).info(sql);
		CoreModelServiceHolder.get().executeNativeUpdate(sql, true);

		sql = UPDATE_COSTBEARERID.replace("[NEWID]", insurance.getId()).replace("[OLDID]", organization.getId());
		LoggerFactory.getLogger(getClass()).info(sql);
		CoreModelServiceHolder.get().executeNativeUpdate(sql, true);

		CoreModelServiceHolder.get().delete(organization);
	}
}
