package ch.elexis.core.mail.ui.test.preference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ch.elexis.core.mail.MailAccountPresets;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.ui.preference.MailAccountForm;
import ch.elexis.core.mail.ui.preference.MailAccountForm.Field;

/**
 * Tests account form defaults, edit preservation, access lists and validation
 * without SWT.
 */
public class MailAccountFormTest {
	/** Loading a legacy account preserves its single endpoint when saving. */
	@Test
	public void loadsSingleEndpointAndPreservesIt() {
		MailboxAccount base = MailAccountPresets.preset(MailProvider.HIN, "mail@example.test", MailboxScope.SHARED, null);
		for (boolean smtp : List.of(false, true)) {
			MailboxAccount single = new MailboxAccount("legacy", base.displayName(), base.emailAddress(), base.provider(),
					base.scope(), null, List.of(), List.of(), List.of(), smtp ? null : base.imap(), smtp ? base.smtp() : null,
					base.username(), base.authMode(), false, "", base.sentCopyMode(), 2, 120);
			MailboxAccount built = new MailAccountForm(single, "owner", true).build();
			assertEquals(single.imap(), built.imap());
			assertEquals(single.smtp(), built.smtp());
		}
	}

	/** Address edits preserve a missing endpoint and ignore its unused port. */
	@Test
	public void singleEndpointSurvivesAddressEdit() {
		MailboxAccount base = MailAccountPresets.preset(MailProvider.HIN, "mail@example.test", MailboxScope.SHARED, null);
		for (boolean smtp : List.of(false, true)) {
			MailboxAccount single = new MailboxAccount("legacy", base.displayName(), base.emailAddress(), base.provider(),
					base.scope(), null, List.of(), List.of(), List.of(), smtp ? null : base.imap(), smtp ? base.smtp() : null,
					base.username(), base.authMode(), false, "", base.sentCopyMode(), 2, 120);
			MailAccountForm form = new MailAccountForm(single, "owner", true);
			form.set(Field.ADDRESS, "changed@example.test");
			form.set(smtp ? Field.IMAP_PORT : Field.SMTP_PORT, "");
			MailboxAccount built = form.build();
			assertEquals(single.imap(), built.imap());
			assertEquals(single.smtp(), built.smtp());
		}
	}

	/** Existing accounts still require at least one configured endpoint. */
	@Test
	public void removingBothEndpointsIsInvalid() {
		MailAccountForm form = new MailAccountForm(form(MailProvider.HIN).build(), "owner");
		form.set(Field.IMAP_HOST, "");
		form.set(Field.SMTP_HOST, "");
		assertEquals("MailSettings_InvalidHost", form.validationKey());
	}

	private MailAccountForm form(MailProvider provider) {
		MailAccountForm form = new MailAccountForm(null, "owner");
		form.set(Field.ADDRESS, "mail@example.test");
		form.provider(provider, true);
		return form;
	}

	/** Applies the complete provider preset. */
	@Test
	public void imapFillsPreset() {
		MailAccountForm form = form(MailProvider.IMAP);
		assertEquals("", form.get(Field.IMAP_HOST));
		assertEquals("993", form.get(Field.IMAP_PORT));
		assertEquals("SSL", form.get(Field.IMAP_SECURITY));
		assertEquals("", form.get(Field.SMTP_HOST));
		assertEquals("587", form.get(Field.SMTP_PORT));
		assertEquals("STARTTLS", form.get(Field.SMTP_SECURITY));
		assertEquals(AuthMode.PASSWORD, form.auth());
		assertEquals("AUTO", form.get(Field.SENT_COPY));
	}

	/** Applies the complete provider preset. */
	@Test
	public void hinFillsPreset() {
		MailAccountForm form = form(MailProvider.HIN);
		assertEquals("imap.mail.hin.ch", form.get(Field.IMAP_HOST));
		assertEquals("993", form.get(Field.IMAP_PORT));
		assertEquals("SSL", form.get(Field.IMAP_SECURITY));
		assertEquals("smtp.mail.hin.ch", form.get(Field.SMTP_HOST));
		assertEquals("587", form.get(Field.SMTP_PORT));
		assertEquals("STARTTLS", form.get(Field.SMTP_SECURITY));
		assertEquals(AuthMode.PASSWORD, form.auth());
		assertEquals("CLIENT", form.get(Field.SENT_COPY));
	}

	/** Applies the complete provider preset. */
	@Test
	public void m365FillsPreset() {
		MailAccountForm form = form(MailProvider.M365);
		assertEquals("outlook.office365.com", form.get(Field.IMAP_HOST));
		assertEquals("993", form.get(Field.IMAP_PORT));
		assertEquals("SSL", form.get(Field.IMAP_SECURITY));
		assertEquals("smtp.office365.com", form.get(Field.SMTP_HOST));
		assertEquals("587", form.get(Field.SMTP_PORT));
		assertEquals("STARTTLS", form.get(Field.SMTP_SECURITY));
		assertEquals(AuthMode.OAUTH2, form.auth());
		assertEquals("SERVER", form.get(Field.SENT_COPY));
	}

	/** Applies the complete provider preset. */
	@Test
	public void googleFillsPreset() {
		MailAccountForm form = form(MailProvider.GOOGLE);
		assertEquals("imap.gmail.com", form.get(Field.IMAP_HOST));
		assertEquals("993", form.get(Field.IMAP_PORT));
		assertEquals("SSL", form.get(Field.IMAP_SECURITY));
		assertEquals("smtp.gmail.com", form.get(Field.SMTP_HOST));
		assertEquals("465", form.get(Field.SMTP_PORT));
		assertEquals("SSL", form.get(Field.SMTP_SECURITY));
		assertEquals(AuthMode.OAUTH2, form.auth());
		assertEquals("SERVER", form.get(Field.SENT_COPY));
	}

	/** Preserves user edits while changing untouched fields. */
	@Test
	public void providerSwitchKeepsExplicitFields() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.IMAP_HOST, "custom.test");
		form.set(Field.SMTP_PORT, "2525");
		form.set(Field.SMTP_SECURITY, "SSL");
		form.set(Field.SENT_COPY, "CLIENT");
		form.provider(MailProvider.GOOGLE, true);
		assertEquals("custom.test", form.get(Field.IMAP_HOST));
		assertEquals("2525", form.get(Field.SMTP_PORT));
		assertEquals("SSL", form.get(Field.SMTP_SECURITY));
		assertEquals("CLIENT", form.get(Field.SENT_COPY));
		assertEquals("smtp.gmail.com", form.get(Field.SMTP_HOST));
	}

	/** A deliberately entered preset value remains an edit. */
	@Test
	public void explicitValueMatchingPresetIsStillKept() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.SMTP_PORT, "587");
		form.provider(MailProvider.GOOGLE, true);
		assertEquals("587", form.get(Field.SMTP_PORT));
	}

	/** Falls back to Google app-password mode when OAuth is unavailable. */
	@Test
	public void googleWithoutSignInUsesPassword() {
		MailAccountForm form = form(MailProvider.HIN);
		form.provider(MailProvider.GOOGLE, false);
		assertEquals(AuthMode.PASSWORD, form.build().authMode());
	}

	/** Builds personal access lists. */
	@Test
	public void personalAccountIncludesOwnerAndDelegates() {
		MailAccountForm form = form(MailProvider.HIN);
		form.access(MailboxScope.PERSONAL, "owner", List.of("delegate"), List.of("ignored"), List.of("ignored"));
		MailboxAccount account = form.build();
		assertEquals(MailboxScope.PERSONAL, account.scope());
		assertEquals("owner", account.ownerUserId());
		assertEquals(List.of("delegate"), account.delegateUserIds());
		assertTrue(account.visibleUserIds().isEmpty());
		assertTrue(account.visibleGroupIds().isEmpty());
	}

	/** Builds shared visibility with no personal owner or delegates. */
	@Test
	public void sharedAccountIncludesVisibility() {
		MailAccountForm form = form(MailProvider.HIN);
		form.access(MailboxScope.SHARED, "owner", List.of("ignored"), List.of("user"), List.of("group"));
		MailboxAccount account = form.build();
		assertEquals(MailboxScope.SHARED, account.scope());
		assertNull(account.ownerUserId());
		assertTrue(account.delegateUserIds().isEmpty());
		assertEquals(List.of("user"), account.visibleUserIds());
		assertEquals(List.of("group"), account.visibleGroupIds());
	}

	/** Keeps existing scope and ownership unchanged without the shared management right. */
	@Test
	public void existingScopeAndOwnerStayWithoutRight() {
		MailboxAccount existing = MailAccountPresets.preset(MailProvider.HIN, "mail@example.test",
				MailboxScope.PERSONAL, "old");
		MailAccountForm form = new MailAccountForm(existing, "new");
		form.access(MailboxScope.SHARED, "new", List.of(), List.of(), List.of());
		assertEquals(MailboxScope.PERSONAL, form.build().scope());
		assertEquals("old", form.build().ownerUserId());
		assertFalse(form.canChangeScope(false));
	}

	/** Keeps a newly saved scope unchanged without the shared management right. */
	@Test
	public void savedNewScopeStaysWithoutRight() {
		MailAccountForm form = form(MailProvider.HIN);
		form.saved("saved");
		form.access(MailboxScope.SHARED, "different", List.of(), List.of(), List.of());
		assertEquals(MailboxScope.PERSONAL, form.build().scope());
		assertEquals("owner", form.build().ownerUserId());
		assertFalse(form.canChangeScope(false));
	}

	/** Derives shared visibility from owner and delegates, without duplicates. */
	@Test
	public void personalToSharedConvertsLists() {
		MailAccountForm initial = form(MailProvider.HIN);
		initial.access(MailboxScope.PERSONAL, "owner", List.of("delegate", "owner", "delegate"), List.of(), List.of());
		MailAccountForm form = new MailAccountForm(initial.build(), "owner");
		assertTrue(form.canChangeScope(true));
		form.access(MailboxScope.SHARED, "owner", form.delegates(), List.of(), List.of("group"), true);
		assertEquals(MailboxScope.SHARED, form.build().scope());
		assertNull(form.build().ownerUserId());
		assertTrue(form.delegates().isEmpty());
		assertEquals(List.of("owner", "delegate"), form.visibleUsers());
		assertEquals(List.of("group"), form.visibleGroups());
		assertFalse(form.groupsDropped());
	}

	/** Derives delegates from visibility, assigns the active user and reports dropped groups. */
	@Test
	public void sharedToPersonalConvertsListsAndShowsHint() {
		MailAccountForm initial = form(MailProvider.HIN);
		initial.access(MailboxScope.SHARED, "owner", List.of(), List.of("owner", "delegate", "delegate"), List.of("group"));
		MailAccountForm form = new MailAccountForm(initial.build(), "delegate");
		form.access(MailboxScope.PERSONAL, "delegate", List.of(), form.visibleUsers(), form.visibleGroups(), true);
		assertEquals("delegate", form.build().ownerUserId());
		assertEquals(List.of("owner"), form.delegates());
		assertTrue(form.visibleUsers().isEmpty());
		assertTrue(form.visibleGroups().isEmpty());
		assertTrue(form.groupsDropped());
		form.access(MailboxScope.PERSONAL, "delegate", form.delegates(), List.of(), List.of(), true);
		assertTrue(form.groupsDropped());
	}

	/** Does not report dropped groups when shared visibility contained only users. */
	@Test
	public void switchingWithoutGroupsHasNoHint() {
		MailAccountForm form = form(MailProvider.HIN);
		form.access(MailboxScope.SHARED, "owner", List.of(), List.of("owner", "delegate"), List.of());
		form.saved("saved");
		form.access(MailboxScope.PERSONAL, "owner", List.of(), form.visibleUsers(), List.of(), true);
		assertFalse(form.groupsDropped());
	}

	/** Allows authorized switching after saving a new account for browser sign-in. */
	@Test
	public void savedNewScopeCanChangeWithRight() {
		MailAccountForm form = form(MailProvider.HIN);
		form.saved("saved");
		assertTrue(form.canChangeScope(true));
		form.access(MailboxScope.SHARED, "owner", List.of(), List.of(), List.of(), true);
		assertEquals(MailboxScope.SHARED, form.build().scope());
		assertEquals(List.of("owner"), form.visibleUsers());
	}

	/** Preserves explicitly supplied target visibility. */
	@Test
	public void explicitVisibilityIsKeptWhenSwitching() {
		MailAccountForm form = form(MailProvider.HIN);
		form.saved("saved");
		form.access(MailboxScope.SHARED, "owner", List.of("delegate"), List.of("custom"), List.of("group"), true);
		assertEquals(List.of("custom"), form.build().visibleUserIds());
	}

	/** Preserves explicitly supplied target delegates. */
	@Test
	public void explicitDelegatesAreKeptWhenSwitching() {
		MailAccountForm form = form(MailProvider.HIN);
		form.access(MailboxScope.SHARED, "owner", List.of(), List.of("previous"), List.of());
		form.saved("saved");
		form.access(MailboxScope.PERSONAL, "owner", List.of("custom"), form.visibleUsers(), List.of(), true);
		assertEquals(List.of("custom"), form.build().delegateUserIds());
	}

	/** Keeps existing shared controls locked without the management right. */
	@Test
	public void existingSharedScopeStaysWithoutRight() {
		MailAccountForm initial = form(MailProvider.HIN);
		initial.access(MailboxScope.SHARED, "owner", List.of(), List.of("visible"), List.of("group"));
		MailAccountForm form = new MailAccountForm(initial.build(), "owner");
		assertFalse(form.canChangeScope(false));
		form.access(MailboxScope.PERSONAL, "owner", List.of(), form.visibleUsers(), form.visibleGroups(), false);
		assertEquals(MailboxScope.SHARED, form.build().scope());
		assertNull(form.build().ownerUserId());
		assertEquals(List.of("group"), form.build().visibleGroupIds());
		assertFalse(form.groupsDropped());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void emptyAddressHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.ADDRESS, "");
		assertEquals("MailSettings_InvalidAddress", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void invalidAddressHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.ADDRESS, "bad");
		assertEquals("MailSettings_InvalidAddress", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void emptyImapHostHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.IMAP_HOST, "");
		assertEquals("MailSettings_InvalidHost", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void emptySmtpHostHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.SMTP_HOST, " ");
		assertEquals("MailSettings_InvalidHost", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void nonNumericPortHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.IMAP_PORT, "abc");
		assertEquals("MailSettings_InvalidPort", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void negativePortHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.SMTP_PORT, "-1");
		assertEquals("MailSettings_InvalidPort", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void zeroPortHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.IMAP_PORT, "0");
		assertEquals("MailSettings_InvalidPort", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void tooLargePortHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.SMTP_PORT, "65536");
		assertEquals("MailSettings_InvalidPort", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void invalidBudgetHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.BUDGET, "4");
		assertEquals("MailSettings_InvalidLimits", form.validationKey());
	}

	/** Returns a stable localized validation key. */
	@Test
	public void invalidPollHasValidationKey() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.POLL, "59");
		assertEquals("MailSettings_InvalidLimits", form.validationKey());
	}

	/** Filters executable and unsupported signature markup. */
	@Test
	public void signatureIsFiltered() {
		MailAccountForm form = form(MailProvider.HIN);
		form.set(Field.SIGNATURE, "<strong>Signature</strong><script>alert(1)</script>");
		assertEquals("<strong>Signature</strong>", form.build().signatureHtml());
	}

	/** Reapplies untouched SMTP defaults when the address changes. */
	@Test
	public void personalMicrosoftUsesConsumerSmtp() {
		MailAccountForm form = form(MailProvider.M365);
		form.set(Field.ADDRESS, "person@outlook.com");
		assertEquals("smtp-mail.outlook.com", form.get(Field.SMTP_HOST));
	}

	/** Accepts valid local settings. */
	@Test
	public void validFormHasNoValidationKey() {
		assertNull(form(MailProvider.HIN).validationKey());
	}

	/** Preselects the active owner for a new account and keeps the selection disabled. */
	@Test
	public void newAccountPreselectsOwnerWithoutEnablingTransfer() {
		MailAccountForm form = form(MailProvider.HIN);
		assertEquals("owner", form.owner());
		assertFalse(form.canChangeOwner());
		assertFalse(form.ownerChanged());
		form.owner("other");
		assertEquals("owner", form.owner());
	}

	/** Existing owners can select another owner without requiring shared management rights. */
	@Test
	public void existingOwnerCanSelectTransfer() {
		MailAccountForm form = new MailAccountForm(form(MailProvider.HIN).build(), "owner");
		assertEquals("owner", form.owner());
		assertTrue(form.canChangeOwner());
		assertFalse(form.ownerChanged());
		form.access(MailboxScope.PERSONAL, "owner", List.of("other", "delegate"), List.of(), List.of());
		form.owner("other");
		assertEquals("other", form.build().ownerUserId());
		assertEquals(List.of("delegate"), form.delegates());
		assertTrue(form.ownerChanged());
		form.access(MailboxScope.PERSONAL, "owner", form.delegates(), List.of(), List.of());
		assertEquals("other", form.owner());
		form.owner("owner");
		assertFalse(form.ownerChanged());
	}

	/** Delegates cannot change the owner of an existing personal account. */
	@Test
	public void nonOwnerCannotSelectTransfer() {
		MailAccountForm form = new MailAccountForm(form(MailProvider.HIN).build(), "delegate");
		assertFalse(form.canChangeOwner());
		form.owner("delegate");
		assertEquals("owner", form.owner());
		assertFalse(form.ownerChanged());
	}

	/** Shared scope hides ownership; switching to personal enables choosing the target owner. */
	@Test
	public void sharedToPersonalEnablesOwnerSelection() {
		MailAccountForm initial = form(MailProvider.HIN);
		initial.access(MailboxScope.SHARED, "owner", List.of(), List.of("other"), List.of());
		MailAccountForm form = new MailAccountForm(initial.build(), "owner");
		assertFalse(form.canChangeOwner());
		form.access(MailboxScope.PERSONAL, "owner", List.of(), form.visibleUsers(), List.of(), true);
		assertEquals("owner", form.owner());
		assertTrue(form.canChangeOwner());
		assertFalse(form.ownerChanged());
		form.owner("other");
		assertTrue(form.ownerChanged());
		assertTrue(form.delegates().isEmpty());
		assertEquals("other", form.build().ownerUserId());
		form.access(MailboxScope.SHARED, "owner", form.delegates(), List.of(), List.of(), true);
		assertFalse(form.canChangeOwner());
		assertFalse(form.ownerChanged());
	}

	/** Saving an account establishes the owner baseline for subsequent edits. */
	@Test
	public void savedNewAccountEnablesOwnerSelection() {
		MailAccountForm form = form(MailProvider.HIN);
		form.saved("saved");
		assertTrue(form.canChangeOwner());
		form.owner("other");
		assertTrue(form.ownerChanged());
		form.saved("saved");
		assertFalse(form.ownerChanged());
	}

	/** Administrators can choose the owner of foreign personal accounts. */
	@Test
	public void administratorCanSelectForeignOwner() {
		MailAccountForm form = new MailAccountForm(form(MailProvider.M365).build(), "admin", true);
		assertTrue(form.canChangeOwner());
		form.owner("other");
		assertEquals("other", form.build().ownerUserId());
	}

	/** Foreign account authentication stays disabled even after unsaved scope conversion. */
	@Test
	public void foreignAuthenticationStaysDisabled() {
		MailAccountForm form = new MailAccountForm(form(MailProvider.M365).build(), "admin", true);
		assertFalse(form.canAuthenticate());
		form.access(MailboxScope.SHARED, "admin", List.of(), List.of(), List.of(), true);
		assertFalse(form.canAuthenticate());
	}

	/** Own and shared accounts retain authentication actions. */
	@Test
	public void ownAndSharedAuthenticationIsAllowed() {
		MailAccountForm form = form(MailProvider.M365);
		assertTrue(form.canAuthenticate());
		form.access(MailboxScope.SHARED, "owner", List.of(), List.of(), List.of(), true);
		assertTrue(form.canAuthenticate());
	}

	/** Personal owner labels match the user selector's username. */
	@Test
	public void personalOwnerLabelUsesUserLabel() {
		assertEquals("Owner Name", MailAccountForm.ownerLabel(form(MailProvider.HIN).build(),
				id -> "owner".equals(id) ? "Owner Name" : null, "Shared"));
	}

	/** Missing users fall back to their stored owner id. */
	@Test
	public void personalOwnerLabelFallsBackToId() {
		assertEquals("owner", MailAccountForm.ownerLabel(form(MailProvider.HIN).build(), id -> null, "Shared"));
	}

	/** Shared accounts use the localized shared label without resolving a personal owner. */
	@Test
	public void sharedOwnerLabelUsesSharedText() {
		MailAccountForm form = form(MailProvider.HIN);
		form.access(MailboxScope.SHARED, "owner", List.of(), List.of(), List.of(), true);
		assertEquals("Shared", MailAccountForm.ownerLabel(form.build(), id -> {
			throw new AssertionError("Shared accounts have no personal owner");
		}, "Shared"));
	}
}
