package ch.elexis.core.ac.internal;

import java.io.IOException;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.Right;

public class ACEAccessBitMapSerializer extends JsonSerializer<ACEAccessBitMap> {

	@Override
	public void serialize(ACEAccessBitMap value, JsonGenerator jsonGenerator, SerializerProvider serializers)
			throws IOException {

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

		if (count > 1) {
			jsonGenerator.writeStartArray();
			performWrite(constraint, has, jsonGenerator);
			jsonGenerator.writeEndArray();
		} else {
			performWrite(constraint, has, jsonGenerator);
		}

	}

	private void performWrite(String[] constraint, boolean[] has, JsonGenerator jsonGenerator) throws IOException {
		if (has[2]) {
			jsonGenerator.writeString(constraint[2] + ":self");
		}
		if (has[1]) {
			jsonGenerator.writeString(constraint[1] + ":aobo");
		}
		if (has[0]) {
			jsonGenerator.writeString(constraint[0]);
		}
	}

}
