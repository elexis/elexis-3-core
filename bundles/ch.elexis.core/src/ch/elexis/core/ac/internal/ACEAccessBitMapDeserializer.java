package ch.elexis.core.ac.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.Right;

public class ACEAccessBitMapDeserializer extends JsonDeserializer<ACEAccessBitMap> {

	@Override
	public ACEAccessBitMap deserialize(JsonParser jsonParser, DeserializationContext ctxt)
			throws IOException, JacksonException {

		JsonNode jsonNode = jsonParser.readValueAsTree();
		String[] rights;
		if (jsonNode.isArray()) {
			ArrayList<String> a = new ArrayList<>(1);
			Iterator<JsonNode> itr = jsonNode.iterator();
			while (itr.hasNext()) {
				a.add(itr.next().textValue());
			}
			rights = a.toArray(new String[a.size()]);
		} else {
			rights = new String[] { jsonNode.textValue() };
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
				String _constraint = entry.substring(indexOf+1);
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

}
