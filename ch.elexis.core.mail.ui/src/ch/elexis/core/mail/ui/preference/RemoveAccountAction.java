package ch.elexis.core.mail.ui.preference;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.ui.client.MailClientComponent;
import ch.elexis.core.ui.icons.Images;

public class RemoveAccountAction extends Action {
	
	private MailAccountComposite accountComposite;
	private Preference preference;
	
	public RemoveAccountAction(MailAccountComposite accountComposite, Preference preference){
		this.accountComposite = accountComposite;
		this.preference = preference;
	}
	
	@Override
	public void run(){
		MailAccount account = accountComposite.getAccount();
		if (account != null) {
			MailClientComponent.getMailClient().removeAccount(account);
			accountComposite.setAccount(null);
			preference.updateAccountsCombo();
		}
	}
	
	@Override
	public ImageDescriptor getImageDescriptor(){
		return Images.IMG_DELETE.getImageDescriptor();
	}
	
	@Override
	public boolean isEnabled(){
		return MailClientComponent.getMailClient() != null;
	}
}
