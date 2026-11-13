package ch.elexis.core.mail.model;

import java.util.List;

/**
 * A bounded ascending scan of one folder generation.
 * @param uidValidity the folder generation
 * @param refs the message references, at most the requested limit
 * @param more whether further matching UIDs exist
 */
public record MailUidScan(long uidValidity, List<MailRef> refs, boolean more) {
	/**
	 * Copies references so callers cannot mutate the scan.
	 * @param uidValidity the folder generation
	 * @param refs the ascending references
	 * @param more whether more matches exist
	 */
	public MailUidScan {
		refs = List.copyOf(refs);
	}
}
