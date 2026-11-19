package ch.elexis.core.mail.internal.mailbox;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.imap.IMAPFolder;

import ch.elexis.core.mail.model.MailFolder;
import ch.elexis.core.mail.model.MailFolderRole;

/** Derives IMAP folder roles from special-use attributes and well-known names. */
public class MailFolderRoles {
	static String treeKey(MailFolder folder, List<MailFolder> folders) {
		StringBuilder key = new StringBuilder();
		for (MailFolder current = folder; current != null;) {
			key.insert(0, current.role() == MailFolderRole.INBOX ? "" : current.name()).insert(0, '\0');
			String parent = current.parentFullName();
			current = folders.stream().filter(candidate -> candidate.fullName().equals(parent)).findFirst()
					.orElse(null);
		}
		return key.toString();
	}

	/**
	 * Collects the roles announced by special-use attributes.
	 * @param folders the listed folders
	 * @return the attributed roles
	 * @throws MessagingException if the attributes cannot be read
	 */
	public static Set<MailFolderRole> attributedRoles(Folder[] folders) throws MessagingException {
		Set<MailFolderRole> roles = EnumSet.noneOf(MailFolderRole.class);
		for (Folder folder : folders) {
			roles.add(attributeRole((IMAPFolder) folder));
		}
		roles.remove(MailFolderRole.NONE);
		return roles;
	}

	private static MailFolderRole attributeRole(IMAPFolder folder) throws MessagingException {
		for (String attribute : folder.getAttributes()) {
			MailFolderRole role = switch (attribute.toLowerCase(Locale.ROOT)) {
			case "\\sent" -> MailFolderRole.SENT;
			case "\\trash" -> MailFolderRole.TRASH;
			case "\\drafts" -> MailFolderRole.DRAFTS;
			case "\\junk" -> MailFolderRole.JUNK;
			case "\\archive" -> MailFolderRole.ARCHIVE;
			default -> MailFolderRole.NONE;
			};
			if (role != MailFolderRole.NONE) {
				return role;
			}
		}
		return MailFolderRole.NONE;
	}

	/**
	 * Determines the role of one folder.
	 * @param folder the folder
	 * @param attributed the roles already announced by attributes
	 * @return the folder role
	 * @throws MessagingException if the attributes cannot be read
	 */
	public static MailFolderRole role(IMAPFolder folder, Set<MailFolderRole> attributed) throws MessagingException {
		MailFolderRole attribute = attributeRole(folder);
		if (attribute != MailFolderRole.NONE) {
			return attribute;
		}
		if (folder.getFullName().equalsIgnoreCase("INBOX")) {
			return MailFolderRole.INBOX;
		}
		MailFolderRole named = switch (folder.getName().toLowerCase(Locale.ROOT)) {
		case "envoyés", "éléments envoyés", "posta inviata", "elementi inviati", "sent", "sent items", "gesendet",
				"gesendete elemente", "gesendete objekte" -> MailFolderRole.SENT;
		case "corbeille", "éléments supprimés", "cestino", "elementi eliminati", "trash", "deleted items", "papierkorb",
				"gelöschte elemente" -> MailFolderRole.TRASH;
		case "brouillons", "bozze", "drafts", "entwürfe" -> MailFolderRole.DRAFTS;
		case "courrier indésirable", "posta indesiderata", "junk", "spam", "junk-e-mail" -> MailFolderRole.JUNK;
		case "archive", "archiv" -> MailFolderRole.ARCHIVE;
		default -> MailFolderRole.NONE;
		};
		return attributed.contains(named) ? MailFolderRole.NONE : named;
	}
}
