package ch.elexis.core.fhir.mapper.r4.findings;

import org.apache.commons.lang3.StringUtils;

public class EnumMapping {

	@SuppressWarnings("rawtypes")
	private Class<? extends Enum> fhirEnum;

	private Enum<?> defaultFhirEnum;

	@SuppressWarnings("rawtypes")
	private Class<? extends Enum> localEnum;

	private Enum<?> defaultLocalEnum;

	public EnumMapping(Class<? extends Enum<?>> fhirEnum, Enum<?> defaultFhirEnum, Class<? extends Enum<?>> localEnum,
			Enum<?> defualtLocalEnum) {
		this.fhirEnum = fhirEnum;
		this.defaultFhirEnum = defaultFhirEnum;

		this.localEnum = localEnum;
		this.defaultLocalEnum = defualtLocalEnum;
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getLocalEnumValueByEnum(Enum<?> fhirEnumValue) {
		return Enum.valueOf(localEnum, fhirEnumValue.name());
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getLocalEnumValueByCode(String code) {
		try {
			return Enum.valueOf(localEnum, code.replaceAll("-", StringUtils.EMPTY));
		} catch (IllegalArgumentException ia) {
			return defaultLocalEnum;
		}
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getFhirEnumValueByEnum(Enum<?> localEnumValue) {
		return Enum.valueOf(fhirEnum, localEnumValue.name());
	}

	@SuppressWarnings("unchecked")
	public Enum<?> getFhirEnumValueByCode(String code) {
		try {
			return Enum.valueOf(fhirEnum, code.replaceAll("-", StringUtils.EMPTY));
		} catch (IllegalArgumentException ia) {
			return defaultFhirEnum;
		}

	}
}
