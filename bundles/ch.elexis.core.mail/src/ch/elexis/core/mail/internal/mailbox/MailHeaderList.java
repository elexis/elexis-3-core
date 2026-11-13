package ch.elexis.core.mail.internal.mailbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.Message;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.BodyTerm;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.FromStringTerm;
import jakarta.mail.search.OrTerm;
import jakarta.mail.search.RecipientStringTerm;
import jakarta.mail.search.SearchException;
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.SubjectTerm;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.ProtocolException;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.eclipse.angus.mail.imap.protocol.FetchResponse;
import org.eclipse.angus.mail.imap.protocol.MessageSet;
import org.eclipse.angus.mail.imap.protocol.UID;

import ch.elexis.core.model.IUser;

import ch.elexis.core.mail.internal.connection.MailDateIndex;
import ch.elexis.core.mail.internal.connection.MailReadFolder;
import ch.elexis.core.mail.internal.connection.MailboxConnectionManager;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailHeader;
import ch.elexis.core.mail.model.MailListFilter;
import ch.elexis.core.mail.model.MailListQuery;
import ch.elexis.core.mail.model.MailPage;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailSortOrder;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.IMailboxService;
import ch.elexis.core.mail.service.MailNotFoundException;

class MailHeaderList {
	private final MailboxService mailbox;

	MailHeaderList(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	/** {@inheritDoc} */
	MailPage listHeaders(IUser user, String accountId, String folderName, int pageIndex)
			throws MessagingException {
		return listHeaders(user, accountId, folderName,
				new MailListQuery(null, MailSortOrder.NEWEST_FIRST, MailListFilter.ALL), pageIndex);
	}

	/** {@inheritDoc} */
	MailPage listHeaders(IUser user, String accountId, String folderName, MailListQuery query, int pageIndex)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		if (query == null || pageIndex < 0) {
			throw new IllegalArgumentException("Invalid mail list parameters");
		}
		SearchTerm term = searchTerm(query);
		return mailbox.connections.executeRead(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, folderName)) {
				IMAPFolder folder = opened.folder();
				MailboxConnectionManager.checkReadRequest();
				int count = folder instanceof MailReadFolder retained ? retained.liveCount() : folder.getMessageCount();
				if (term == null && count > MailboxService.PROVISIONAL_THRESHOLD && !mailbox.connections.dates.ready(accountId, folder)) {
					long validity = folder.getUIDValidity();
					int offset = (int) Math.min((long) pageIndex * IMailboxService.PAGE_SIZE, count);
					int first = query.order() == MailSortOrder.NEWEST_FIRST ? Math.max(1, count - offset - IMailboxService.PAGE_SIZE + 1)
							: offset + 1;
					int last = query.order() == MailSortOrder.NEWEST_FIRST ? count - offset
							: Math.min(count, offset + IMailboxService.PAGE_SIZE);
					List<Long> sequences = new ArrayList<>();
					for (int sequence = first; sequence <= last; sequence++) {
						sequences.add((long) sequence);
					}
					if (query.order() == MailSortOrder.NEWEST_FIRST) {
						sequences = sequences.reversed();
					}
					List<MailHeader> headers = pageHeaders(folder, accountId, validity, sequences, false);
					MailboxConnectionManager.checkReadRequest();
					mailbox.connections.buildDateIndex(account, folderName);
					return new MailPage(headers, pageIndex, count, validity, true);
				}
				Set<Long> matches = term == null ? null : matchingUids(folder, term);
				MailDateIndex.Snapshot snapshot = mailbox.connections.dates.snapshot(accountId, folder, pageIndex == 0);
				if (matches == null) {
					List<MailDateIndex.Entry> entries = query.order() == MailSortOrder.NEWEST_FIRST
							? snapshot.entries().reversed() : snapshot.entries();
					int start = (int) Math.min((long) pageIndex * IMailboxService.PAGE_SIZE, entries.size());
					List<Long> page = entries.subList(start, Math.min(start + IMailboxService.PAGE_SIZE, entries.size())).stream()
							.map(MailDateIndex.Entry::uid).toList();
					return new MailPage(pageHeaders(folder, accountId, snapshot.validity(), page), pageIndex,
							entries.size(), snapshot.validity());
				}
				List<Long> ordered = snapshot.entries().stream()
						.filter(entry -> matches.contains(entry.uid()))
						.map(MailDateIndex.Entry::uid).toList();
				if (query.order() == MailSortOrder.NEWEST_FIRST) {
					ordered = ordered.reversed();
				}
				int start = (int) Math.min((long) pageIndex * IMailboxService.PAGE_SIZE, ordered.size());
				List<Long> page = ordered.subList(start, Math.min(start + IMailboxService.PAGE_SIZE, ordered.size()));
				List<MailHeader> headers = pageHeaders(folder, accountId, snapshot.validity(), page);
				return new MailPage(headers, pageIndex, ordered.size(), snapshot.validity());
			}
		});
	}

	private SearchTerm searchTerm(MailListQuery query) {
		List<SearchTerm> terms = new ArrayList<>();
		String text = query.text();
		if (text != null) {
			Arrays.stream(text.split("\\s+")).map(word -> new OrTerm(new SearchTerm[] {
					new FromStringTerm(word), new RecipientStringTerm(Message.RecipientType.TO, word),
					new RecipientStringTerm(Message.RecipientType.CC, word), new SubjectTerm(word), new BodyTerm(word) }))
					.forEach(terms::add);
		}
		if (query.filter() == MailListFilter.UNREAD) {
			terms.add(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
		} else if (query.filter() == MailListFilter.FLAGGED) {
			terms.add(new FlagTerm(new Flags(Flags.Flag.FLAGGED), true));
		}
		return terms.isEmpty() ? null : new AndTerm(terms.toArray(SearchTerm[]::new));
	}

	private Set<Long> matchingUids(IMAPFolder folder, SearchTerm term) throws MessagingException {
		int[] sequences = (int[]) folder.doCommand(protocol -> {
			try {
				return protocol.search(term);
			} catch (SearchException e) {
				throw new ProtocolException("Server mail search failed", e);
			}
		});
		if (sequences == null) {
			throw new MessagingException("Server mail search failed");
		}
		Set<Long> matches = new LinkedHashSet<>();
		if (sequences.length > 0) {
			MailboxConnectionManager.checkReadRequest();
			int[] selected = Arrays.stream(sequences).distinct().sorted().toArray();
			folder.doCommand(protocol -> {
				Argument arguments = new Argument();
				arguments.writeAtom(MessageSet.toString(MessageSet.createMessageSets(selected)));
				Argument items = new Argument();
				items.writeAtom("UID");
				arguments.writeArgument(items);
				Response[] responses = protocol.command("FETCH", arguments);
				for (Response response : responses) {
					if (response instanceof FetchResponse fetch) {
						UID uid = fetch.getItem(UID.class);
						if (uid != null && Arrays.binarySearch(selected, fetch.getNumber()) >= 0) {
							matches.add(uid.uid);
						}
					}
				}
				protocol.notifyResponseHandlers(responses);
				protocol.handleResult(responses[responses.length - 1]);
				return null;
			});
		}
		return matches;
	}

	private List<MailHeader> pageHeaders(IMAPFolder folder, String accountId, long validity, List<Long> page)
			throws MessagingException {
		return pageHeaders(folder, accountId, validity, page, true);
	}

	private List<MailHeader> pageHeaders(IMAPFolder folder, String accountId, long validity, List<Long> page,
			boolean byUid) throws MessagingException {
		MailboxConnectionManager.checkReadRequest();
		if (page.isEmpty()) {
			return List.of();
		}
		FetchProfile profile = MailHeaderMapper.fetchProfile();
		Response[] responses = (Response[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeAtom(page.stream().sorted().map(Object::toString).collect(Collectors.joining(",")));
			Argument items = new Argument();
			items.writeAtom("UID ENVELOPE INTERNALDATE RFC822.SIZE FLAGS BODY.PEEK[HEADER.FIELDS ("
					+ String.join(" ", profile.getHeaderNames()) + ")]");
			arguments.writeArgument(items);
			Response[] fetched = protocol.command(byUid ? "UID FETCH" : "FETCH", arguments);
			protocol.notifyResponseHandlers(fetched);
			protocol.handleResult(fetched[fetched.length - 1]);
			return fetched;
		});
		Map<Long, MailHeader> fetched = new HashMap<>();
		for (Response response : responses) {
			if (response instanceof FetchResponse fetch) {
				UID uid = fetch.getItem(UID.class);
				if (uid != null && page.contains(byUid ? uid.uid : (long) fetch.getNumber())) {
					Message message = new FetchedMessage(folder, fetch, profile);
					try {
						fetched.put(byUid ? uid.uid : (long) fetch.getNumber(),
								MailHeaderMapper.header(message, new MailRef(accountId, folder.getFullName(), validity, uid.uid), false));
					} catch (MessageRemovedException e) {
						fetched.remove(byUid ? uid.uid : (long) fetch.getNumber());
					}
				}
			}
		}
		List<MailHeader> headers = new ArrayList<>();
		for (long uid : page) {
			MailHeader header = fetched.get(uid);
			if (header != null) {
				headers.add(header);
			}
		}
		return List.copyOf(headers);
	}

	static final class FetchedMessage extends IMAPMessage {
		FetchedMessage(IMAPFolder folder, FetchResponse response, FetchProfile profile) throws MessagingException {
			super(folder, folder instanceof MailReadFolder retained
					? retained.messageNumber(response.getNumber()) : response.getNumber());
			for (int i = 0; i < response.getItemCount(); i++) {
				handleFetchItem(response.getItem(i), profile.getHeaderNames(), false);
			}
		}
	}

	/** {@inheritDoc} */
	MailHeader locate(IUser user, String accountId, String folderName, long uid) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		if (uid <= 0) {
			throw new IllegalArgumentException("A positive mail UID is required");
		}
		return mailbox.connections.executeRead(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, folderName)) {
				IMAPFolder folder = opened.folder();
				MailboxConnectionManager.checkReadRequest();
				MailRef ref = new MailRef(accountId, folderName, folder.getUIDValidity(), uid);
				Message message = MailHeaderMapper.message(folder, ref);
				folder.fetch(new Message[] { message }, MailHeaderMapper.fetchProfile());
				return MailHeaderMapper.header(message, ref, false);
			} catch (MessageRemovedException | FolderNotFoundException e) {
				throw new MailNotFoundException("Mail message no longer exists");
			}
		});
	}

	/** {@inheritDoc} */
	MailPage search(IUser user, String accountId, String folderName, String text, int pageIndex)
			throws MessagingException {
		String query = text == null ? "" : text.trim();
		if (query.length() < 2 || query.length() > 200 || pageIndex < 0) {
			throw new IllegalArgumentException("Invalid mail search parameters");
		}
		return listHeaders(user, accountId, folderName,
				new MailListQuery(query, MailSortOrder.NEWEST_FIRST, MailListFilter.ALL), pageIndex);
	}
}
