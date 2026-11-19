package ch.elexis.core.mail.internal.send;

import java.io.IOException;

public final class TooLargeException extends IOException {
	private static final long serialVersionUID = 1L;

	public TooLargeException() {
		super("Outgoing message exceeds the 25 MB size limit");
	}
}
