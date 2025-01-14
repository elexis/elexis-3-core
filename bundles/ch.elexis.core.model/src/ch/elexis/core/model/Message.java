package ch.elexis.core.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;

import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.INamedQuery;
import jakarta.persistence.Transient;

public class Message extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Message>
		implements Identifiable, IMessage {

	private final Gson gson;

	public Message(ch.elexis.core.jpa.entities.Message entity) {
		super(entity);
		gson = new Gson();
	}

	@Override
	public String getSender() {
		return getEntity().getOrigin();
	}

	@Override
	public void setSender(String identifier) {
		getEntityMarkDirty().setOrigin(identifier);
	}

	/**
	 * convenience method
	 *
	 * @param user
	 */
	@Override
	public void setSender(IUser user) {
		setSender(user.getId());
	}

	@Override
	public List<String> getReceiver() {
		return Collections.singletonList(getEntity().getDestination());
	}

	@Override
	public void addReceiver(String receiver) {
		getEntityMarkDirty().setDestination(receiver);
	}

	@Override
	public void addReceiver(IUser receiver) {
		addReceiver(receiver.getId());
	}

	@Override
	public boolean isSenderAcceptsAnswer() {
		// TODO support
		return true;
	}

	@Override
	public void setSenderAcceptsAnswer(boolean value) {
		// TODO support
	}

	@Override
	public LocalDateTime getCreateDateTime() {
		return getEntity().getDateTime();
	}

	@Override
	public void setCreateDateTime(LocalDateTime value) {
		getEntityMarkDirty().setDateTime(value);
	}

	@Override
	public String getMessageText() {
		return getEntity().getMsg();
	}

	@Override
	public void setMessageText(String value) {
		getEntityMarkDirty().setMsg(value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getMessageCodes() {
		String json = getEntity().getMessageCodes();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}

	@Override
	public void setMessageCodes(Map<String, String> value) {
		String json = gson.toJson(value);
		getEntityMarkDirty().setMessageCodes(json);
	}

	@Override
	public void addMessageCode(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMessagePriority() {
		// TODO support
		return 0;
	}

	@Override
	public void setMessagePriority(int value) {
		// TODO support
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		// not supported
		return false;
	}

	@Override
	public IXid getXid(String domain) {
		// not supported
		return null;
	}

	@Transient
	private Optional<IUser> findFirstUserForContact(Kontakt kontakt) {
		INamedQuery<IUser> namedQuery = ModelUtil.getModelService().getNamedQuery(IUser.class, "kontakt");
		List<IUser> users = namedQuery.executeWithParameters(namedQuery.getParameterMap("kontakt", kontakt));
		if (!users.isEmpty()) {
			return Optional.of(users.get(0));
		}
		return Optional.empty();
	}

	@Override
	public List<String> getPreferredTransporters() {
		return Collections.emptyList();
	}

}
