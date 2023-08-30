package ch.elexis.core.ac;

import java.util.Map;

public class AccessControlListUtil {

	/**
	 * Merge the secondAcl into the firstAcl
	 * 
	 * @param firstAcl  the base
	 * @param secondAcl the merger, must not be a combined ACL
	 * @return
	 */
	public static AccessControlList merge(AccessControlList firstAcl, AccessControlList secondAcl) {
		AccessControlList clonedAcl = new AccessControlList(firstAcl);

		if (secondAcl.getRolesRepresented().size() != 1) {
			throw new IllegalArgumentException("Cannot merge a combined ACL");
		}
		if (clonedAcl.getRolesRepresented().contains(secondAcl.getRolesRepresented().iterator().next())) {
			// already merged
			return clonedAcl;
		}

		clonedAcl.getRolesRepresented().add(secondAcl.getRolesRepresented().iterator().next());
		mergeObjectACE(clonedAcl, secondAcl);
		return clonedAcl;
	}

	private static void mergeObjectACE(AccessControlList clonedAcl, AccessControlList secondAcl) {
		Map<String, ACEAccessBitMap> firstObjectAcl = clonedAcl.getObject();
		Map<String, ACEAccessBitMap> secondObjectAcl = secondAcl.getObject();
		if (secondObjectAcl != null) {
			secondObjectAcl.forEach((k, v) -> {
				if (firstObjectAcl.containsKey(k)) {
					// it already contains this key, do our rights raise the privileges
					// already within?
					byte[] firstObject = firstObjectAcl.get(k).getAccessRightMap();
					byte[] secondObject = secondObjectAcl.get(k).getAccessRightMap();
					mergeRight(Right.CREATE, firstObject, secondObject);
					mergeRight(Right.READ, firstObject, secondObject);
					mergeRight(Right.UPDATE, firstObject, secondObject);
					mergeRight(Right.DELETE, firstObject, secondObject);
					mergeRight(Right.EXECUTE, firstObject, secondObject);
					mergeRight(Right.VIEW, firstObject, secondObject);
					mergeRight(Right.EXPORT, firstObject, secondObject);
					mergeRight(Right.IMPORT, firstObject, secondObject);
					mergeRight(Right.REMOVE, firstObject, secondObject);
					// update with merged copy of the bitmap
					firstObjectAcl.put(k, new ACEAccessBitMap(firstObject));
				} else {
					firstObjectAcl.put(k, v);
				}
			});
		}
		Map<String, ACEAccessBitMap> firstSystemCommandAcl = clonedAcl.getSystemCommand();
		Map<String, ACEAccessBitMap> secondSystemCommandAcl = secondAcl.getSystemCommand();
		if (secondSystemCommandAcl != null) {
			secondSystemCommandAcl.forEach((k, v) -> {
				if (firstSystemCommandAcl.containsKey(k)) {
					// it already contains this key, do our rights raise the privileges
					// already within?
					byte[] firstSystemCommand = firstSystemCommandAcl.get(k).getAccessRightMap();
					byte[] secondSystemCommand = secondSystemCommandAcl.get(k).getAccessRightMap();
					mergeRight(Right.CREATE, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.READ, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.UPDATE, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.DELETE, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.EXECUTE, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.VIEW, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.EXPORT, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.IMPORT, firstSystemCommand, secondSystemCommand);
					mergeRight(Right.REMOVE, firstSystemCommand, secondSystemCommand);
					// update with merged copy of the bitmap
					firstSystemCommandAcl.put(k, new ACEAccessBitMap(firstSystemCommand));
				} else {
					firstSystemCommandAcl.put(k, v);
				}
			});
		}
	}

	private static void mergeRight(Right right, byte[] firstObject, byte[] secondObject) {
		// never change if already full right
		if (firstObject[right.ordinal()] != 4) {
			// only merge if secondObject has better right
			if (firstObject[right.ordinal()] < secondObject[right.ordinal()]) {
				firstObject[right.ordinal()] = secondObject[right.ordinal()];
			}
		}
	}
}
