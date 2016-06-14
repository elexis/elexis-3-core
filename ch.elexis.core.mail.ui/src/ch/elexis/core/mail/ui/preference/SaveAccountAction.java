package ch.elexis.core.mail.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.ui.icons.Images;

public class SaveAccountAction extends Action {

	private MailAccountComposite accountComposite;
	private Preference preference;
	
	public SaveAccountAction(MailAccountComposite accountComposite, Preference preference){
		this.accountComposite = accountComposite;
		this.preference = preference;
	}
	
	@Override
	public void run(){
		MailAccount account = accountComposite.getAccount();
		if (account != null) {
			MailClientComponent.getMailClient().saveAccount(account);
			preference.updateAccountsCombo();
		}
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_DISK.getImageDescriptor();
	}

	@Override
	public boolean isEnabled(){
		return MailClientComponent.getMailClient() != null;
	}
}
