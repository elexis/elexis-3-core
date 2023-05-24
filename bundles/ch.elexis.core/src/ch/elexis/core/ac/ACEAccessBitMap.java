package ch.elexis.core.ac;

import java.beans.Transient;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ch.elexis.core.ac.internal.ACEAccessBitMapDeserializer;
import ch.elexis.core.ac.internal.ACEAccessBitMapSerializer;

@JsonSerialize(using = ACEAccessBitMapSerializer.class)
@JsonDeserialize(using = ACEAccessBitMapDeserializer.class)
public class ACEAccessBitMap {

	private final byte[] accessRightMap;
	private final boolean hasConstraint;

	public ACEAccessBitMap(byte[] accessRightMap) {
		this.accessRightMap = accessRightMap;
		this.hasConstraint = evaluateHasConstraint();
	}

	public byte[] getAccessRightMap() {
		return accessRightMap;
	}

	public boolean isHasConstraint() {
		return hasConstraint;
	}

	/**
	 * Evaluate if the bitMap has a constraint applied, which we need to evaluate.
	 * That is, next to no access or all access we have
	 * {@link Constraint#AOBO#bitMapping} or {@link Constraint#SELF#bitMapping}
	 * 
	 * @return
	 */
	private boolean evaluateHasConstraint() {
		for (byte b : accessRightMap) {
			if (b > 0 & b < 4) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If this ACE globally grants the right
	 * 
	 * @param right
	 * @return
	 */
	@Transient
	public boolean grants(Right right) {
		return accessRightMap[right.ordinal()] == 4;
	}
}
