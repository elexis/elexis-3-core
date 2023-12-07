package ch.elexis.core.ac;

import java.beans.Transient;
import java.util.Arrays;

import com.google.gson.annotations.JsonAdapter;

import ch.elexis.core.ac.internal.ACEAccessBitMapJsonAdapter;

@JsonAdapter(value = ACEAccessBitMapJsonAdapter.class)
public class ACEAccessBitMap {

	private final byte[] accessRightMap;
	private final boolean hasConstraint;

	public ACEAccessBitMap(byte[] accessRightMap) {
		this.accessRightMap = accessRightMap;
		this.hasConstraint = evaluateHasConstraint();
	}

	/**
	 * Get a copy of the access right map.
	 * 
	 * @return
	 */
	public byte[] getAccessRightMap() {
		return Arrays.copyOf(accessRightMap, accessRightMap.length);
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
