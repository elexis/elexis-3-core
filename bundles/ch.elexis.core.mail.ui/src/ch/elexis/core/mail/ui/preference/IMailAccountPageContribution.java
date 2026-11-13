package ch.elexis.core.mail.ui.preference;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.model.IUser;

/** Optional OSGi contribution to the mail account preference page. */
public interface IMailAccountPageContribution {
	/**
	 * Describes an account action evaluated on the UI thread.
	 * @param label localized button label
	 * @param enabled whether the selected account permits the action
	 * @param run action receiving the page shell and selected account
	 */
	record Action(String label, Predicate<MailboxAccount> enabled, BiConsumer<Shell, MailboxAccount> run) {
	}

	/**
	 * Supplies buttons for the active user.
	 * @param user active user
	 * @return contributed actions
	 */
	List<Action> actions(IUser user);

	/** @return localized account status column title */
	String columnTitle();

	/**
	 * Reads the contributed status without connecting to the mailbox.
	 * @param user active user
	 * @param account account to display
	 * @return localized status
	 */
	String columnText(IUser user, MailboxAccount account);

	/**
	 * Creates additional settings on the UI thread for this page instance.
	 * @param parent settings container
	 * @param user active user
	 * @return action saving this instance's settings on Apply or OK
	 */
	Runnable createSettings(Composite parent, IUser user);
}
