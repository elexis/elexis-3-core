package ch.elexis.core.jpa.entities.converter;

import ch.rgw.tools.VersionedResource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class VersionedResourceConverter implements AttributeConverter<VersionedResource, byte[]> {

	@Override
	public byte[] convertToDatabaseColumn(VersionedResource objectValue) {
		if (objectValue instanceof VersionedResource) {
			VersionedResource vr = (VersionedResource) objectValue;
			return vr.serialize();
		}
		return null;
	}

	@Override
	public VersionedResource convertToEntityAttribute(byte[] dataValue) {
		return VersionedResource.load((byte[]) dataValue);
	}

}
