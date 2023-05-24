package ch.elexis.core.ac;

public enum Right {

	CREATE('c'), READ('r'), UPDATE('u'), DELETE('d'), EXECUTE('x'), VIEW('v'), EXPORT('e'), IMPORT('i'),
	/** Purge or forced delete */
	REMOVE('z');

	public final char token;

	Right(char token) {
		this.token = token;
	}
}
