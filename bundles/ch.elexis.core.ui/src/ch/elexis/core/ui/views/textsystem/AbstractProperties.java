/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.textsystem;

import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

import ch.elexis.core.ui.util.Log;

/**
 * Diese Klasse ueberschreibt die load Funktionalitaet von java.util.Properties.
 * Der Code entspricht dem Code in java.util.Properties bis auf den kleinen
 * Unterschied, dass ":" nicht als Trennzeichen angesehen wird!
 *
 * @author nowhow
 *
 */
public abstract class AbstractProperties extends Properties {
	private static final long serialVersionUID = -9019950244305182074L;

	private static Log log = Log.get("AbstractProperties"); //$NON-NLS-1$

	private final static String DIRECTORY = "rsc/platzhalter"; //$NON-NLS-1$

	/**
	 * Filename
	 */
	protected abstract String getFilename();

	/**
	 * Returns location of the properties file inside of the elexis plugin
	 */
	private String getPlatzhalterFilenamePath() throws IOException {
		URL url = Platform.getBundle("ch.elexis.core.data").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
		url = FileLocator.toFileURL(url);
		String bundleLocation = url.getPath();
		return bundleLocation + File.separator + DIRECTORY + File.separator + getFilename();
	}

	public AbstractProperties() {
		super();
		String filenamePath = getFilename();
		FileReader reader = null;
		try {
			filenamePath = getPlatzhalterFilenamePath();
			reader = new FileReader(filenamePath);
			load(reader);
		} catch (IOException e) {
			log.log(e, MessageFormat.format(Messages.AbstractProperties_message_FileNotFound, filenamePath),
					Log.ERRORS);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Reads a property list (key and element pairs) from the input character stream
	 * in a simple line-oriented format.
	 * <p>
	 * Properties are processed in terms of lines. There are two kinds of line,
	 * <i>natural lines</i> and <i>logical lines</i>. A natural line is defined as a
	 * line of characters that is terminated either by a set of line terminator
	 * characters (<code>\n</code> or <code>\r</code> or <code>\r\n</code>) or by
	 * the end of the stream. A natural line may be either a blank line, a comment
	 * line, or hold all or some of a key-element pair. A logical line holds all the
	 * data of a key-element pair, which may be spread out across several adjacent
	 * natural lines by escaping the line terminator sequence with a backslash
	 * character <code>\</code>. Note that a comment line cannot be extended in this
	 * manner; every natural line that is a comment must have its own comment
	 * indicator, as described below. Lines are read from input until the end of the
	 * stream is reached.
	 *
	 * <p>
	 * A natural line that contains only white space characters is considered blank
	 * and is ignored. A comment line has an ASCII <code>'#'</code> or
	 * <code>'!'</code> as its first non-white space character; comment lines are
	 * also ignored and do not encode key-element information. In addition to line
	 * terminators, this format considers the characters space ( <code>' '</code>,
	 * <code>'&#92;u0020'</code>), tab (<code>'\t'</code>,
	 * <code>'&#92;u0009'</code>), and form feed (<code>'\f'</code>,
	 * <code>'&#92;u000C'</code>) to be white space.
	 *
	 * <p>
	 * If a logical line is spread across several natural lines, the backslash
	 * escaping the line terminator sequence, the line terminator sequence, and any
	 * white space at the start of the following line have no affect on the key or
	 * element values. The remainder of the discussion of key and element parsing
	 * (when loading) will assume all the characters constituting the key and
	 * element appear on a single natural line after line continuation characters
	 * have been removed. Note that it is <i>not</i> sufficient to only examine the
	 * character preceding a line terminator sequence to decide if the line
	 * terminator is escaped; there must be an odd number of contiguous backslashes
	 * for the line terminator to be escaped. Since the input is processed from left
	 * to right, a non-zero even number of 2<i>n</i> contiguous backslashes before a
	 * line terminator (or elsewhere) encodes <i>n</i> backslashes after escape
	 * processing.
	 *
	 * <p>
	 * The key contains all of the characters in the line starting with the first
	 * non-white space character and up to, but not including, the first unescaped
	 * <code>'='</code>, <code>':'</code>, or white space character other than a
	 * line terminator. All of these key termination characters may be included in
	 * the key by escaping them with a preceding backslash character; for example,
	 * <p>
	 *
	 * <code>\:\=</code>
	 * <p>
	 *
	 * would be the two-character key <code>":="</code>. Line terminator characters
	 * can be included using <code>\r</code> and <code>\n</code> escape sequences.
	 * Any white space after the key is skipped; if the first non-white space
	 * character after the key is <code>'='</code> or <code>':'</code>, then it is
	 * ignored and any white space characters after it are also skipped. All
	 * remaining characters on the line become part of the associated element
	 * string; if there are no remaining characters, the element is the empty string
	 * <code>&quot;&quot;</code>. Once the raw character sequences constituting the
	 * key and element are identified, escape processing is performed as described
	 * above.
	 *
	 * <p>
	 * As an example, each of the following three lines specifies the key
	 * <code>"Truth"</code> and the associated element value <code>"Beauty"</code>:
	 * <p>
	 *
	 * <pre>
	 * Truth = Beauty
	 * Truth:Beauty
	 * Truth			:Beauty
	 * </pre>
	 *
	 * As another example, the following three lines specify a single property:
	 * <p>
	 *
	 * <pre>
	 * fruits                           apple, banana, pear, \
	 *                                  cantaloupe, watermelon, \
	 *                                  kiwi, mango
	 * </pre>
	 *
	 * The key is <code>"fruits"</code> and the associated element is:
	 * <p>
	 *
	 * <pre>
	 * &quot;apple, banana, pear, cantaloupe, watermelon, kiwi, mango&quot;
	 * </pre>
	 *
	 * Note that a space appears before each <code>\</code> so that a space will
	 * appear after each comma in the final result; the <code>\</code>, line
	 * terminator, and leading white space on the continuation line are merely
	 * discarded and are <i>not</i> replaced by one or more other characters.
	 * <p>
	 * As a third example, the line:
	 * <p>
	 *
	 * <pre>
	 * cheeses
	 * </pre>
	 *
	 * specifies that the key is <code>"cheeses"</code> and the associated element
	 * is the empty string <code>StringUtils.EMPTY</code>.
	 * <p>
	 * <p>
	 *
	 * <a name="unicodeescapes"></a> Characters in keys and elements can be
	 * represented in escape sequences similar to those used for character and
	 * string literals (see <a href=
	 * "http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3"
	 * >&sect;3.3</a> and <a href=
	 * "http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.10.6"
	 * >&sect;3.10.6</a> of the <i>Java Language Specification</i>).
	 *
	 * The differences from the character escape sequences and Unicode escapes used
	 * for characters and strings are:
	 *
	 * <ul>
	 * <li>Octal escapes are not recognized.
	 *
	 * <li>The character sequence <code>\b</code> does <i>not</i> represent a
	 * backspace character.
	 *
	 * <li>The method does not treat a backslash character, <code>\</code>, before a
	 * non-valid escape character as an error; the backslash is silently dropped.
	 * For example, in a Java string the sequence <code>"\z"</code> would cause a
	 * compile time error. In contrast, this method silently drops the backslash.
	 * Therefore, this method treats the two character sequence <code>"\b"</code> as
	 * equivalent to the single character <code>'b'</code>.
	 *
	 * <li>Escapes are not necessary for single and double quotes; however, by the
	 * rule above, single and double quote characters preceded by a backslash still
	 * yield single and double quote characters, respectively.
	 *
	 * <li>Only a single 'u' character is allowed in a Uniocde escape sequence.
	 *
	 * </ul>
	 * <p>
	 * The specified stream remains open after this method returns.
	 *
	 * @param reader the input character stream.
	 * @throws IOException              if an error occurred when reading from the
	 *                                  input stream.
	 * @throws IllegalArgumentException if a malformed Unicode escape appears in the
	 *                                  input.
	 * @since 1.6
	 */
	public synchronized void load(Reader reader) throws IOException {
		load0(new LineReader(reader));
	}

	/**
	 * Reads a property list (key and element pairs) from the input byte stream. The
	 * input stream is in a simple line-oriented format as specified in
	 * {@link #load(java.io.Reader) load(Reader)} and is assumed to use the ISO
	 * 8859-1 character encoding; that is each byte is one Latin1 character.
	 * Characters not in Latin1, and certain special characters, are represented in
	 * keys and elements using <a href=
	 * "http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#3.3"
	 * >Unicode escapes</a>.
	 * <p>
	 * The specified stream remains open after this method returns.
	 *
	 * @param inStream the input stream.
	 * @exception IOException if an error occurred when reading from the input
	 *                        stream.
	 * @throws IllegalArgumentException if the input stream contains a malformed
	 *                                  Unicode escape sequence.
	 * @since 1.2
	 */
	public synchronized void load(InputStream inStream) throws IOException {
		load0(new LineReader(inStream));
	}

	private void load0(LineReader lr) throws IOException {
		char[] convtBuf = new char[1024];
		int limit;
		int keyLen;
		int valueStart;
		char c;
		boolean hasSep;
		boolean precedingBackslash;

		while ((limit = lr.readLine()) >= 0) {
			c = 0;
			keyLen = 0;
			valueStart = limit;
			hasSep = false;

			// System.out.println("line=<" + new String(lineBuf, 0, limit) +
			// ">");
			precedingBackslash = false;
			while (keyLen < limit) {
				c = lr.lineBuf[keyLen];
				// need check if escaped.
				if ((c == '=') && !precedingBackslash) {
					valueStart = keyLen + 1;
					hasSep = true;
					break;
				} else if ((c == '\t' || c == '\f') && !precedingBackslash) {
					valueStart = keyLen + 1;
					break;
				}
				if (c == '\\') {
					precedingBackslash = !precedingBackslash;
				} else {
					precedingBackslash = false;
				}
				keyLen++;
			}
			while (valueStart < limit) {
				c = lr.lineBuf[valueStart];
				if (c != '\t' && c != '\f') {
					if (!hasSep && (c == '=')) {
						hasSep = true;
					} else {
						break;
					}
				}
				valueStart++;
			}
			String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
			String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
			put(key, value);
		}
	}

	/*
	 * Read in a "logical line" from an InputStream/Reader, skip all comment and
	 * blank lines and filter out those leading whitespace characters ( , and ) from
	 * the beginning of a "natural line". Method returns the char length of the
	 * "logical line" and stores the line in "lineBuf".
	 */
	class LineReader {
		public LineReader(InputStream inStream) {
			this.inStream = inStream;
			inByteBuf = new byte[8192];
		}

		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192];
		}

		byte[] inByteBuf;
		char[] inCharBuf;
		char[] lineBuf = new char[1024];
		int inLimit = 0;
		int inOff = 0;
		InputStream inStream;
		Reader reader;

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				if (inStream != null) {
					// The line below is equivalent to calling a
					// ISO8859-1 decoder.
					c = (char) (0xff & inByteBuf[inOff++]);
				} else {
					c = inCharBuf[inOff++];
				}
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					// flip the preceding backslash flag
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}

	/*
	 * Converts encoded &#92;uxxxx to unicode chars and changes special saved chars
	 * to their original forms
	 */
	private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
		if (convtBuf.length < len) {
			int newLen = len * 2;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;
		int end = off + len;

		while (off < end) {
			aChar = in[off++];
			if (aChar == '\\') {
				aChar = in[off++];
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = in[off++];
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed \\uxxxx encoding."); //$NON-NLS-1$
						}
					}
					out[outLen++] = (char) value;
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = (char) aChar;
			}
		}
		return new String(out, 0, outLen);
	}
}
