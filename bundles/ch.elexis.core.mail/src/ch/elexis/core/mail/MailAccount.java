package ch.elexis.core.mail;

import java.util.Base64;

import ch.elexis.core.model.IMandator;
import jakarta.mail.Address;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

/**
 * Class representing the configuration of a mail account.
 *
 * @author thomas
 *
 */
public class MailAccount {

	private static final String SEPARATOR = ",";

	/**
	 * Definition of the different types of mail accounts. SMTP for sending mail,
	 * IMAP for receiving mails.
	 *
	 * @author thomas
	 *
	 */
	public enum TYPE {
		SMTP, IMAP, IMAPS
	}

	private String id;
	private TYPE type;

	private String username;
	private String password;

	private String from;

	private String host;
	private String port;

	private String mandants;

	private boolean starttls;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public boolean isStarttls() {
		return starttls;
	}

	public void setStarttls(boolean starttls) {
		this.starttls = starttls;
	}

	public String getMandants() {
		return mandants;
	}

	public void setMandants(String mandants) {
		this.mandants = mandants;
	}

	/**
	 * Set all fields from the String. Use {@link MailAccount#toString()} to
	 * generate such a String.
	 *
	 * @param csv
	 * @return
	 */
	public static MailAccount from(String csv) {
		MailAccount ret = null;
		String[] parts = csv.split(SEPARATOR);
		if (parts != null && parts.length > 2) {
			ret = new MailAccount();
			for (String string : parts) {
				String[] subParts = string.split("=");
				if (subParts != null && subParts.length == 2) {
					setField(subParts[0], subParts[1], ret);
				}
			}
		}
		return ret;
	}

	private static void setField(String field, String value, MailAccount account) {
		switch (field) {
		case "id":
			account.id = value;
			return;
		case "type":
			account.type = TYPE.valueOf(value);
			return;
		case "username":
			account.username = value;
			return;
		case "password":
			account.password = new String(Base64.getDecoder().decode(value));
			return;
		case "from":
			account.from = value;
			return;
		case "host":
			account.host = value;
			return;
		case "port":
			account.port = value;
			return;
		case "mandants":
			account.mandants = value;
			return;
		case "starttls":
			account.starttls = Boolean.parseBoolean(value);
		}
	}

	/**
	 * Generated a String that can be saved, and used to reload the object with
	 * {@link MailAccount#from(String)}.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (id != null) {
			sb.append("id=").append(id);
			sb.append(SEPARATOR);
		}
		if (type != null) {
			sb.append("type=").append(type.name());
			sb.append(SEPARATOR);
		}
		if (username != null) {
			sb.append("username=").append(username);
			sb.append(SEPARATOR);
		}
		if (password != null) {
			sb.append("password=").append(Base64.getEncoder().encodeToString(password.getBytes()));
			sb.append(SEPARATOR);
		}
		if (from != null) {
			sb.append("from=").append(from);
			sb.append(SEPARATOR);
		}
		if (host != null) {
			sb.append("host=").append(host);
			sb.append(SEPARATOR);
		}
		if (port != null) {
			sb.append("port=").append(port);
			sb.append(SEPARATOR);
		}
		if (mandants != null) {
			sb.append("mandants=").append(mandants);
			sb.append(SEPARATOR);
		}
		sb.append("starttls=").append(starttls);
		return sb.toString();
	}

	/**
	 * Get the from address to use. If field is not set, the username is checked if
	 * it can be used as from address.
	 *
	 * @return
	 * @throws AddressException
	 */
	public Address getFromAddress() throws AddressException {
		if (from != null) {
			if (from.contains("@")) {
				InternetAddress[] fromAdresses = InternetAddress.parse(from);
				if (fromAdresses != null && fromAdresses.length > 0) {
					return fromAdresses[0];
				}
			}
		}
		if (username != null) {
			if (username.contains("@")) {
				InternetAddress[] fromAdresses = InternetAddress.parse(username);
				if (fromAdresses != null && fromAdresses.length > 0) {
					return fromAdresses[0];
				}
			}
		}
		throw new AddressException("From [" + from + "] Username [" + username + "] are no mail addresses.");
	}

	/**
	 * Test if this account is linked to the mandant identified by the id.
	 *
	 * @param mandantId
	 * @return
	 */
	public boolean isForMandant(String mandantId) {
		if (mandants != null && !mandants.isEmpty()) {
			String[] ids = mandants.split("\\|\\|");
			for (String string : ids) {
				if (string.equals(mandantId)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Link the mandant to this account.
	 *
	 * @param mandant
	 */
	public void addMandant(IMandator mandant) {
		String newId = mandant.getId();
		if (mandants == null) {
			mandants = new String(newId);
		} else if (!mandants.contains(newId)) {
			mandants += "||" + newId;
		}
	}

	/**
	 * Remove a link between this account and the mandant.
	 *
	 * @param mandant
	 */
	public void removeMandant(IMandator mandant) {
		if (mandants != null) {
			StringBuilder sb = new StringBuilder();
			String[] ids = mandants.split("\\|\\|");
			for (String string : ids) {
				if (!string.equals(mandant.getId())) {
					if (sb.length() == 0) {
						sb.append(string);
					} else {
						sb.append("||").append(string);
					}
				}
			}
			mandants = sb.toString();
		}
	}

	public MailAccount copy() {
		return MailAccount.from(toString());
	}
}
