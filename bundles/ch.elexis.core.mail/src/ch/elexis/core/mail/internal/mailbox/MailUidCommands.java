package ch.elexis.core.mail.internal.mailbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.CommandFailedException;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.BASE64MailboxEncoder;
import org.eclipse.angus.mail.imap.protocol.FetchResponse;
import org.eclipse.angus.mail.imap.protocol.IMAPResponse;
import org.eclipse.angus.mail.imap.protocol.UID;
import org.eclipse.angus.mail.imap.protocol.UIDSet;

import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.service.MailNoTrashFolderException;
import ch.elexis.core.mail.service.MailNotFoundException;

class MailUidCommands {
	static void clearMessages(IMAPStore store, IMAPFolder selected, long[] uids) throws MessagingException {
		uidCommand(selected, "STORE", uids, null, Flags.Flag.DELETED, true);
		if (store.hasCapability("UIDPLUS")) {
			uidCommand(selected, "EXPUNGE", uids, null, null, false);
		} else {
			selected.doCommand(protocol -> {
				protocol.simpleCommand("EXPUNGE", null);
				return null;
			});
		}
	}

	static long[] searchUids(IMAPFolder folder, String criteria) throws MessagingException {
		return (long[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeAtom(criteria);
			Response[] responses = protocol.command("UID SEARCH", arguments);
			List<Long> uids = new ArrayList<>();
			for (Response response : responses) {
				if (response instanceof IMAPResponse search && search.keyEquals("SEARCH")) {
					long uid;
					while ((uid = search.readLong()) > 0) {
						uids.add(uid);
					}
				}
			}
			protocol.notifyResponseHandlers(responses);
			protocol.handleResult(responses[responses.length - 1]);
			return uids.stream().mapToLong(Long::longValue).sorted().toArray();
		});
	}

	static IMAPFolder targetFolder(IMAPStore store, String name) throws MessagingException {
		IMAPFolder target = (IMAPFolder) store.getFolder(name);
		if (!target.exists() || (target.getType() & Folder.HOLDS_MESSAGES) == 0) {
			throw new MailNotFoundException("Mail destination folder does not exist or is not selectable");
		}
		return target;
	}

	static IMAPFolder trashFolder(IMAPStore store) throws MessagingException {
		Folder[] folders = store.getDefaultFolder().list("*");
		Set<MailFolderRole> attributed = MailFolderRoles.attributedRoles(folders);
		for (Folder folder : folders) {
			if (MailFolderRoles.role((IMAPFolder) folder, attributed) == MailFolderRole.TRASH
					&& (folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
				return (IMAPFolder) folder;
			}
		}
		throw new MailNoTrashFolderException("Mail account has no trash folder");
	}

	static long[] moveMessages(IMAPStore store, IMAPFolder source, IMAPFolder target, long[] uids)
			throws MessagingException {
		long[] remaining = uids;
		for (int attempt = 1;; attempt++) {
			try {
				return transfer(store, source, target, remaining);
			} catch (MessagingException e) {
				if (!(e.getCause() instanceof CommandFailedException) || attempt == 3) {
					throw e;
				}
				remaining = existingUids(source, remaining);
				if (remaining.length == 0) {
					return remaining;
				}
			}
		}
	}

	private static long[] transfer(IMAPStore store, IMAPFolder source, IMAPFolder target, long[] uids)
			throws MessagingException {
		if (store.hasCapability("MOVE")) {
			return uidCommand(source, "MOVE", uids, target.getFullName(), null, false);
		}
		long[] copied = uidCommand(source, "COPY", uids, target.getFullName(), null, false);
		if (copied.length > 0) {
			uidCommand(source, "STORE", copied, null, Flags.Flag.DELETED, true);
			if (store.hasCapability("UIDPLUS")) {
				uidCommand(source, "EXPUNGE", copied, null, null, false);
			}
		}
		return copied;
	}

	static long[] existingUids(IMAPFolder folder, long[] uids) throws MessagingException {
		return (long[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeAtom(Arrays.stream(uids).mapToObj(Long::toString).collect(Collectors.joining(",")));
			Argument items = new Argument();
			items.writeAtom("UID");
			arguments.writeArgument(items);
			Response[] responses = protocol.command("UID FETCH", arguments);
			Set<Long> existing = new LinkedHashSet<>();
			for (Response response : responses) {
				if (response instanceof FetchResponse fetch) {
					UID uid = fetch.getItem(UID.class);
					if (uid != null && Arrays.binarySearch(uids, uid.uid) >= 0) {
						existing.add(uid.uid);
					}
				}
			}
			protocol.notifyResponseHandlers(responses);
			protocol.handleResult(responses[responses.length - 1]);
			return existing.stream().mapToLong(Long::longValue).sorted().toArray();
		});
	}

	static long[] uidCommand(IMAPFolder folder, String command, long[] uids, String target, Flags.Flag flag,
			boolean value) throws MessagingException {
		return uidCommand(folder, command,
				Arrays.stream(uids).mapToObj(Long::toString).collect(Collectors.joining(",")), uids, target, flag, value);
	}

	static long[] uidCommand(IMAPFolder folder, String command, String uidSet, long[] uids, String target,
			Flags.Flag flag, boolean value) throws MessagingException {
		return (long[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeAtom(uidSet);
			if (target != null) {
				arguments.writeString(protocol.supportsUtf8() ? target : BASE64MailboxEncoder.encode(target));
			}
			if (flag != null) {
				arguments.writeAtom(value ? "+FLAGS.SILENT" : "-FLAGS.SILENT");
				Argument flags = new Argument();
				flags.writeAtom(new Flags(flag).toString());
				arguments.writeArgument(flags);
			}
			Response[] responses = protocol.command("UID " + command, arguments);
			protocol.notifyResponseHandlers(responses);
			protocol.handleResult(responses[responses.length - 1]);
			if (target != null) {
				for (Response response : responses) {
					if (response != null) {
						Matcher copied = Pattern.compile("\\[COPYUID [0-9]+ ([0-9,:]+) [0-9,:]+\\]",
								Pattern.CASE_INSENSITIVE).matcher(response.toString());
						if (copied.find()) {
							UIDSet[] sets = UIDSet.parseUIDSets(copied.group(1));
							return Arrays.stream(uids).filter(uid -> Arrays.stream(sets)
									.anyMatch(set -> uid >= set.start && uid <= set.end)).toArray();
						}
					}
				}
			}
			return target != null && protocol.hasCapability("UIDPLUS") ? new long[0] : uids;
		});
	}
}
