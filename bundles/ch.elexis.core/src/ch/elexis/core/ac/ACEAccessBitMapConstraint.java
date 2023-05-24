package ch.elexis.core.ac;

public enum ACEAccessBitMapConstraint {

	/**
	 * No constraints, all objects can be handled with the given Right
	 */
	NONE((byte) 4),
	/**
	 * Only objects one owns can be handled with the given Right
	 */
	SELF((byte) 1),
	/**
	 * Only objects of ownership by somebody this user Acts On Behalf Of can be
	 * handled. AOBO always includes the objects SELF
	 */
	AOBO((byte) 2);

	public final byte bitMapping;

	ACEAccessBitMapConstraint(byte bitMapping) {
		this.bitMapping = bitMapping;
	}

}
