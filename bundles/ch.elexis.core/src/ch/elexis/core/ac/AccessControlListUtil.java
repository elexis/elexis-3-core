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
		Map<String, ACEAccessBitMap> firstSystemCommandAcl = clonedAcl.getSystemCommand();
		Map<String, ACEAccessBitMap> secondSystemCommandAcl = secondAcl.getSystemCommand();
		if (secondSystemCommandAcl != null) {
			secondSystemCommandAcl.forEach((k, v) -> {
				if (firstSystemCommandAcl.containsKey(k)) {
					// it already contains this key, do our rights raise the privileges
					// already within?
					byte[] firstSystemCommand = firstSystemCommandAcl.get(k).getAccessRightMap();
					byte[] secondSystemCommand = secondSystemCommandAcl.get(k).getAccessRightMap();
					firstSystemCommand[Right.CREATE.ordinal()] |= secondSystemCommand[Right.CREATE.ordinal()];
					firstSystemCommand[Right.READ.ordinal()] |= secondSystemCommand[Right.READ.ordinal()];
					firstSystemCommand[Right.UPDATE.ordinal()] |= secondSystemCommand[Right.UPDATE.ordinal()];
					firstSystemCommand[Right.DELETE.ordinal()] |= secondSystemCommand[Right.DELETE.ordinal()];
					firstSystemCommand[Right.EXECUTE.ordinal()] |= secondSystemCommand[Right.EXECUTE.ordinal()];
					firstSystemCommand[Right.VIEW.ordinal()] |= secondSystemCommand[Right.VIEW.ordinal()];
					firstSystemCommand[Right.EXPORT.ordinal()] |= secondSystemCommand[Right.EXPORT.ordinal()];
					firstSystemCommand[Right.IMPORT.ordinal()] |= secondSystemCommand[Right.IMPORT.ordinal()];
					firstSystemCommand[Right.REMOVE.ordinal()] |= secondSystemCommand[Right.REMOVE.ordinal()];
					// update with merged copy of the bitmap
					firstSystemCommandAcl.put(k, new ACEAccessBitMap(firstSystemCommand));
				} else {
					firstSystemCommandAcl.put(k, v);
				}
			});
		}
	}

}
