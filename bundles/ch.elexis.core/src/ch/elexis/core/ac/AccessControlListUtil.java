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
		// TODO gets a copy, modification is not persisted
		Map<String, ACEAccessBitMap> secondObjectAcl = secondAcl.getObject();
		secondObjectAcl.forEach((k, v) -> {
			if (firstObjectAcl.containsKey(k)) {
				// it already contains this key, do our rights raise the privileges
				// already within?
				byte[] firstObject = firstObjectAcl.get(k).getAccessRightMap();
				byte[] secondObject = secondObjectAcl.get(k).getAccessRightMap();
				firstObject[Right.CREATE.ordinal()] |= secondObject[Right.CREATE.ordinal()];
				firstObject[Right.READ.ordinal()] |= secondObject[Right.READ.ordinal()];
				firstObject[Right.UPDATE.ordinal()] |= secondObject[Right.UPDATE.ordinal()];
				firstObject[Right.DELETE.ordinal()] |= secondObject[Right.DELETE.ordinal()];
				firstObject[Right.EXECUTE.ordinal()] |= secondObject[Right.EXECUTE.ordinal()];
				firstObject[Right.VIEW.ordinal()] |= secondObject[Right.VIEW.ordinal()];
				firstObject[Right.EXPORT.ordinal()] |= secondObject[Right.EXPORT.ordinal()];
				firstObject[Right.IMPORT.ordinal()] |= secondObject[Right.IMPORT.ordinal()];
				firstObject[Right.REMOVE.ordinal()] |= secondObject[Right.REMOVE.ordinal()];
				// update with merged copy of the bitmap
				firstObjectAcl.put(k, new ACEAccessBitMap(firstObject));
			} else {
				firstObjectAcl.put(k, v);
			}
		});
	}

}
