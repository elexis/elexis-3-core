package ch.elexis.core.mail.model;

/**
 * One folder of an account, as an entry of a flat list that describes the folder tree.
 *
 * @param fullName
 *            the full folder name including all parents, used to address the folder
 * @param name
 *            the last segment of the full name, used for display
 * @param parentFullName
 *            the full name of the parent folder, <code>null</code> for top level folders
 * @param role
 *            the special purpose of the folder
 * @param selectable
 *            <code>false</code> if the folder only holds other folders and cannot be opened
 * @param unreadCount
 *            number of messages without the seen flag
 * @param totalCount
 *            number of messages in the folder
 */
public record MailFolder(String fullName, String name, String parentFullName, MailFolderRole role,
		boolean selectable, int unreadCount, int totalCount) {
}
