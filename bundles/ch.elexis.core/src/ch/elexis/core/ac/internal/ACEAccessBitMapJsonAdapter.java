package ch.elexis.core.ac.internal;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.Right;

public class ACEAccessBitMapJsonAdapter implements JsonSerializer<ACEAccessBitMap>, JsonDeserializer<ACEAccessBitMap> {

	@Override
	public JsonElement serialize(ACEAccessBitMap value, Type typeOfSrc, JsonSerializationContext context) {

		boolean[] has = new boolean[3];
		String[] constraint = new String[3];
		Arrays.fill(constraint, "");

		byte[] accessRightMap = value.getAccessRightMap();

		for (int i = 0; i < accessRightMap.length; i++) {
			byte b = accessRightMap[i];
			if ((b & ACEAccessBitMapConstraint.NONE.bitMapping) != 0) {
				constraint[0] += Right.values()[i].token;
				has[0] = true;
			}
			if ((b & ACEAccessBitMapConstraint.AOBO.bitMapping) != 0) {
				constraint[1] += Right.values()[i].token;
				has[1] = true;
			}
			if ((b & ACEAccessBitMapConstraint.SELF.bitMapping) != 0) {
				constraint[2] += Right.values()[i].token;
				has[2] = true;
			}
		}

		int count = 0;
		count += has[0] ? 1 : 0;
		count += has[1] ? 1 : 0;
		count += has[2] ? 1 : 0;

		JsonArray array = new JsonArray();
		if (count > 1) {
			performWrite(constraint, has, array);
		} else {
			performWrite(constraint, has, array);
		}
		return array.size() == 1 ? array.get(0) : array;
	}

	@Override
	public ACEAccessBitMap deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		String[] rights;
		if (jsonElement.isJsonArray()) {
			JsonArray array = jsonElement.getAsJsonArray();
			ArrayList<String> a = new ArrayList<>(1);
			array.forEach(entry -> a.add(entry.getAsString()));
			rights = a.toArray(new String[a.size()]);
		} else {
			rights = new String[] { jsonElement.getAsString() };
		}

		return new ACEAccessBitMap(buildAccessRightMap(rights));
	}

	private byte[] buildAccessRightMap(String[] rights) {
		byte[] accessRightMap = new byte[Right.values().length];
		Arrays.asList(rights).forEach(entry -> {
			int indexOf = entry.indexOf(':');
			ACEAccessBitMapConstraint constraint = null;
			String denominator;
			if (indexOf > 1) {
				// has constraint
				denominator = entry.substring(0, indexOf);
				String _constraint = entry.substring(indexOf + 1);
				constraint = ACEAccessBitMapConstraint.valueOf(_constraint.toUpperCase());
			} else {
				denominator = entry;
			}

			accessRightMap[Right.CREATE.ordinal()] ^= multiplyFindConstraint(Right.CREATE, constraint, denominator);
			accessRightMap[Right.READ.ordinal()] ^= multiplyFindConstraint(Right.READ, constraint, denominator);
			accessRightMap[Right.UPDATE.ordinal()] ^= multiplyFindConstraint(Right.UPDATE, constraint, denominator);
			accessRightMap[Right.DELETE.ordinal()] ^= multiplyFindConstraint(Right.DELETE, constraint, denominator);
			accessRightMap[Right.EXECUTE.ordinal()] ^= multiplyFindConstraint(Right.EXECUTE, constraint, denominator);
			accessRightMap[Right.VIEW.ordinal()] ^= multiplyFindConstraint(Right.VIEW, constraint, denominator);
			accessRightMap[Right.EXPORT.ordinal()] ^= multiplyFindConstraint(Right.EXPORT, constraint, denominator);
			accessRightMap[Right.IMPORT.ordinal()] ^= multiplyFindConstraint(Right.IMPORT, constraint, denominator);
			accessRightMap[Right.REMOVE.ordinal()] ^= multiplyFindConstraint(Right.REMOVE, constraint, denominator);
		});
		return accessRightMap;
	}

	private byte multiplyFindConstraint(Right right, ACEAccessBitMapConstraint constraint, String denominator) {
		int isApplied = denominator.indexOf(right.token);
		if (isApplied < 0) {
			return 0; // right not found in denominator
		}
		if (ACEAccessBitMapConstraint.SELF == constraint) {
			return ACEAccessBitMapConstraint.SELF.bitMapping;
		}
		if (ACEAccessBitMapConstraint.AOBO == constraint) {
			return ACEAccessBitMapConstraint.AOBO.bitMapping;
		}
		return ACEAccessBitMapConstraint.NONE.bitMapping; // '*'
	}

	private void performWrite(String[] constraint, boolean[] has, JsonArray array) {
		if (has[2]) {
			array.add(constraint[2] + ":self");
		}
		if (has[1]) {
			array.add(constraint[1] + ":aobo");
		}
		if (has[0]) {
			array.add(constraint[0]);
		}
	}
}
