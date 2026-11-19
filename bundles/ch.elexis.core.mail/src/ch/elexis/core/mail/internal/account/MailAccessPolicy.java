package ch.elexis.core.mail.internal.account;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.service.MailAccessException;

/** Decides account visibility and management without configuration or network access. */
public final class MailAccessPolicy {
	private final Function<IUser, List<IUserGroup>> groups;
	private final Predicate<IUser> manageRight;

	/**
	 * Creates a policy using injected membership and permission lookups.
	 * @param groups returns the groups of the given user
	 * @param manageRight checks the shared-account management right for the given user
	 */
	public MailAccessPolicy(Function<IUser, List<IUserGroup>> groups, Predicate<IUser> manageRight) {
		this.groups = groups;
		this.manageRight = manageRight;
	}

	/**
	 * Checks visibility, including the restricted empty shared visibility case.
	 * @param user the user
	 * @param account the account
	 * @return whether the user may see the account
	 */
	public boolean canSee(IUser user, MailboxAccount account) {
		if (account.scope() == MailboxScope.PERSONAL) {
			return user.getId().equals(account.ownerUserId()) || account.delegateUserIds().contains(user.getId());
		}
		if (account.visibleUserIds().isEmpty() && account.visibleGroupIds().isEmpty()) {
			return manageRight.test(user);
		}
		return account.visibleUserIds().contains(user.getId())
				|| groups.apply(user).stream().anyMatch(group -> account.visibleGroupIds().contains(group.getId()));
	}

	/**
	 * Checks management permission for existing or new accounts.
	 * @param user the user
	 * @param account the account, possibly without an id
	 * @return whether the user may manage the account
	 */
	public boolean canManage(IUser user, MailboxAccount account) {
		return manageRight.test(user)
				|| account.scope() == MailboxScope.PERSONAL && user.getId().equals(account.ownerUserId());
	}

	/**
	 * Requires visibility.
	 * @param user the user
	 * @param account the account
	 * @throws MailAccessException if access is denied
	 */
	public void checkSee(IUser user, MailboxAccount account) {
		if (!canSee(user, account)) {
			throw new MailAccessException("Mail account access denied");
		}
	}

	/**
	 * Requires management permission.
	 * @param user the user
	 * @param account the account
	 * @throws MailAccessException if management is denied
	 */
	public void checkManage(IUser user, MailboxAccount account) {
		if (!canManage(user, account)) {
			throw new MailAccessException("Mail account management denied");
		}
	}
}
