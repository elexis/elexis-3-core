package ch.elexis.core.mail.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.ui.icons.Images;

public class CopyVirtLocalAccountAction extends Action {

	private MailAccountComposite accountComposite;
	private Preference preference;
	
	public CopyVirtLocalAccountAction(MailAccountComposite accountComposite, Preference preference){
		this.accountComposite = accountComposite;
		this.preference = preference;
	}
	
	@Override
	public void run(){
		MailAccount account = accountComposite.getAccount();
		if (account != null) {
			// create local copy if non exists else select existing
			if (!MailClientComponent.getMailClient().getAccountsLocal()
				.contains(MailClientComponent.getVirtLocalId(account))) {
				MailAccount localAccount = account.copy();
				localAccount.setId(MailClientComponent.getVirtLocalId(account));
				MailClientComponent.getMailClient().saveAccountLocal(localAccount);
				preference.updateAccountsCombo();
			} else {
				MailAccount localAccount = MailClientComponent.getMailClient()
					.getAccount(MailClientComponent.getVirtLocalId(account)).orElse(null);
				accountComposite.setAccount(localAccount);
				preference.updateAccountsCombo();
			}
		}
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_COPY.getImageDescriptor();
	}

	@Override
	public String getToolTipText(){
		return "Kopie als lokale virtuelle Konfig";
	}
	
	@Override
	public boolean isEnabled(){
		return MailClientComponent.getMailClient() != null;
	}
}
