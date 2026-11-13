package ch.elexis.core.mail.internal.send;

import java.io.IOException;
import java.io.OutputStream;

final class SizeLimitOutputStream extends OutputStream {

	private final OutputStream target;
	private long written;

	SizeLimitOutputStream(OutputStream target) {
		this.target = target;
	}

	/** {@inheritDoc} */
	@Override
	public void write(int value) throws IOException {
		reserve(1);
		target.write(value);
	}

	/** {@inheritDoc} */
	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		reserve(length);
		target.write(bytes, offset, length);
	}

	private void reserve(int count) throws TooLargeException {
		if (count > MailMessageBuilder.MAX_MESSAGE_BYTES - written) {
			throw new TooLargeException();
		}
		written += count;
	}
}
